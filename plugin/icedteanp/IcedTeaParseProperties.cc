/* IcedTeaRunnable.cc

   Copyright (C) 2013  Red Hat

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

IcedTea is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

#include <unistd.h>
#include <sys/types.h>
#include <pwd.h>
#include <cstdio>
#include <cstdlib>
#include <string>
#include <functional> 
#include <cctype>
#include <locale>
#include <iostream>
#include <fstream>


#include "IcedTeaPluginUtils.h"
#include "IcedTeaNPPlugin.h"
#include "IcedTeaParseProperties.h"
/*
 The public api is nearly impossible to test due to "hardcoded paths"
 All public methods have theirs equivalents wit set-up-able files, and those are 
 tested.
*/

using namespace std;
//private api

void remove_all_spaces(string& str);
bool  get_property_value(string c, string& dest);
bool starts_with(string c1, string c2);
string  user_properties_file();
string  main_properties_file();
string default_java_properties_file();
//for passing three dummy files
bool find_system_config_file(string main_file, string custom_jre_file, bool usecustom_jre, string default_java_file, string& dest);
bool find_property(string filename, string property, string& dest);
//for passing two dummy files
bool  read_deploy_property_value(string user_file, string system_file,  bool usesystem_file, string property, string& dest);
//for passing two dummy files
bool  find_custom_jre(string user_file, string main_file,string& dest);
//end of non-public IcedTeaParseProperties api
const std::string default_file_ITW_deploy_props_name = "deployment.properties";
const std::string default_itw_log_dir_name = "log";
const std::string custom_jre_key = "deployment.jre.dir";

void remove_all_spaces(string& str)
{
	for(int i=0; i<str.length(); i++){
		if(str[i] == ' '  || str[i] == '\n' || str[i] == '\t') {
			str.erase(i,1);
			i--;
		}
	}
}

bool  get_property_value(string c, string& dest){
	int i = c.find("=");
	if (i < 0) return false;
	int l = c.length();
	dest = c.substr(i+1, l-i);
	IcedTeaPluginUtilities::trim(dest);
	IcedTeaPluginUtilities::unescape(dest);
	return true;
}


bool starts_with(string c1, string c2){
        return (c1.find(c2) == 0);
}


string  user_properties_file(){
	int myuid = getuid();
	struct passwd *mypasswd = getpwuid(myuid);
	// try pre 1.5  file location
	string old_name = string(mypasswd->pw_dir)+"/.icedtea/"+default_file_ITW_deploy_props_name;
	//exists? then itw was not yet migrated. Use it
	if (IcedTeaPluginUtilities::file_exists(old_name)) {
		PLUGIN_ERROR("IcedTea-Web plugin is using out-dated configuration\n");
		return old_name;
	}
	//we are probably  on XDG specification now
	//is specified custom value?
	if (getenv ("XDG_CONFIG_HOME") != NULL){
		return string(getenv ("XDG_CONFIG_HOME"))+"/icedtea-web/"+default_file_ITW_deploy_props_name;
	}
	//if not then use default
	return string(mypasswd->pw_dir)+"/.config/icedtea-web/"+default_file_ITW_deploy_props_name;
}

string get_log_dir(){
	string value;
	if (!read_deploy_property_value("deployment.user.logdir", value)) {
		string config_dir;
		if (getenv ("XDG_CONFIG_HOME") != NULL){
			config_dir = string(getenv("XDG_CONFIG_HOME"));
		} else {
			int myuid = getuid();
			struct passwd *mypasswd = getpwuid(myuid);
			config_dir = string(mypasswd->pw_dir) + "/.config";
		}
		string itw_dir = config_dir+"/icedtea-web";
		string log_dir = itw_dir+"/"+default_itw_log_dir_name;
		bool created_config = IcedTeaPluginUtilities::create_dir(itw_dir);
		bool created_log = IcedTeaPluginUtilities::create_dir(log_dir);
		if (!created_config || !created_log){
			PLUGIN_ERROR("IcedTea-Web log directory creation failed. IcedTea-Web may fail to work!");
		}
		return log_dir;
	}
	return value;
}

string main_properties_file(){
	return "/etc/.java/deployment/"+default_file_ITW_deploy_props_name;
}

string default_java_properties_file(){
	return  ICEDTEA_WEB_JRE "/lib/"+default_file_ITW_deploy_props_name;
}


/* this is the same search done by icedtea-web settings:
   try  the main file in /etc/.java/deployment
   if found, then return this file
   try to find setUp jre
   if found, then try if this file exists and end
   if no jre custom jvm is set, then tries default jre
   if its  deploy file exists, then return
   not found otherwise*/
bool find_system_config_file(string& dest){
	string jdest;
	bool found = find_custom_jre(jdest);
	if (found) {
		jdest = jdest + "/lib/"+default_file_ITW_deploy_props_name;
	}
	return find_system_config_file(main_properties_file(), jdest, found, default_java_properties_file(), dest);
}

bool  is_java_console_enabled(){
	string value;
	if (!read_deploy_property_value("deployment.console.startup.mode", value)) {
		return true;
	}
	if (value == "DISABLE") {
		return false;
	} else {
		return true;
	}
}

bool  read_bool_property(string key, bool defaultValue){
	string value;
	if (!read_deploy_property_value(key, value)) {
		return defaultValue;
	}
	if (value == "true") {
		return true;
	} else {
		return false;
	}
}	

bool  is_debug_on(){
	return 	read_bool_property("deployment.log",false);
}
bool  is_debug_header_on(){
	return 	read_bool_property("deployment.log.headers",false);
}
bool  is_logging_to_file(){
	return 	read_bool_property("deployment.log.file",false);
}
bool  is_logging_to_stds(){
	return 	read_bool_property("deployment.log.stdstreams",true);
}
bool  is_logging_to_system(){
	return 	read_bool_property("deployment.log.system",true);
}


//abstraction for testing purposes
bool find_system_config_file(string main_file, string custom_jre_file, bool usecustom_jre, string default_java_file, string& dest){
	if (IcedTeaPluginUtilities::file_exists(main_file)) {
		dest = main_file;
		return true;
	} else {
		if (usecustom_jre){
			if(IcedTeaPluginUtilities::file_exists(custom_jre_file) ) {
				dest = custom_jre_file;
				return true;
			} 
		} else {
			if(IcedTeaPluginUtilities::file_exists(default_java_file)) {
			dest = default_java_file;
			return true;
			} 
		}	
	}
return false; //nothing of above found
}

//Returns whether property was found, if found stores result in 'dest'
bool find_property(string filename, string property, string& dest){
	string  property_matcher(property);
	IcedTeaPluginUtilities::trim( property_matcher);
	property_matcher= property_matcher+"=";
	ifstream input( filename.c_str() );
	for( string line; getline( input, line ); ){ /* read a line */
		string copy = line;
		//java tolerates spaces around = char, remove them for matching
		remove_all_spaces(copy);
		if (starts_with(copy, property_matcher)) {
			//provide non-spaced value, trimming is  done in get_property_value
			get_property_value(line, dest);
			return true;
			}
		}

	return false;
	}


/* this is reimplementation of itw-settings operations
   first check in user's settings, if found, return
   then check in global file (see the magic of find_system_config_file)*/
bool  read_deploy_property_value(string property, string& dest){
	string futurefile;
	bool found = find_system_config_file(futurefile);
	return read_deploy_property_value(user_properties_file(), futurefile, found, property, dest);
}
//abstraction for testing purposes
bool  read_deploy_property_value(string user_file, string system_file, bool usesystem_file, string property, string& dest){
	//is it in user's file?
	bool found = find_property(user_file, property, dest);
	if (found) {
		return true;
	}
	//is it in global file?
	if (usesystem_file) {
		return find_property(system_file, property, dest);
	}
	return false;
}

//This is different from common get property, as it is avoiding to search in *java*
//properties files
bool  find_custom_jre(string& dest){
	return find_custom_jre(user_properties_file(), main_properties_file(), dest);
}
//abstraction for testing purposes
bool  find_custom_jre(string user_file, string main_file,string& dest){
	string key = custom_jre_key;
	if(IcedTeaPluginUtilities::file_exists(user_file)) {
		bool a = find_property(user_file, key, dest);
		if (a) {
			return true;
		}
	}
	if(IcedTeaPluginUtilities::file_exists(main_file)) {
		return find_property(main_file, key, dest);
	}
return false;
}



int test_main(void){
        cout << ("user's settings file\n");
	cout << user_properties_file();
	cout << ("\nmain settings file:\n");
	cout << (main_properties_file());
	cout << ("\njava settings file \n");
	cout << (default_java_properties_file());
	cout << ("\nsystem config file\n");
	string a1;
	find_system_config_file(a1);
	cout <<  a1;
	cout << ("\ncustom jre\n");
	string a2;
	find_custom_jre(a2);
	cout << a2;
	cout << ("\nsome custom property\n");
	string a3;
	read_deploy_property_value("deployment.security.level", a3);
	cout << a3;
	cout << ("\n");
  return 0;
}
