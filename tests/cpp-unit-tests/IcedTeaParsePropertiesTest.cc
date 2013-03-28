/* Copyright (C) 2012 Red Hat

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

#include <UnitTest++.h>
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
#include "IcedTeaParseProperties.h"

using namespace std;

//not exposed via IcedTeaParseProperties but needed
extern void remove_all_spaces(string& str);
extern bool  get_property_value(string c, string& dest);
extern bool starts_with(string c1, string c2);
extern string  user_properties_file();
extern string  main_properties_file();
extern string default_java_properties_file();
//for passing three dummy files
bool find_system_config_file(string main_file, string custom_jre_file, bool usecustom_jre, string default_java_file, string& dest);
bool find_property(string filename, string property, string& dest);
//for passing two dummy files
bool  read_deploy_property_value(string user_file, string system_file,  bool usesystem_file, string property, string& dest);
//for passing two dummy files
bool  find_custom_jre(string user_file, string main_file,string& dest);
//end of non-public IcedTeaParseProperties api

/* Creates a temporary file with the specified contents */
static string temporary_file(const string& contents) {
	string path = tmpnam(NULL); /* POSIX function, fine for test suite */
	ofstream myfile;
	myfile.open (path.c_str());
	myfile << contents;
	myfile.close();
	return path;
}



/*private api fundamental tests*/
TEST(RemoveAllSpaces) {
	string toBeTrimmed = string(" te st X ");
	remove_all_spaces (toBeTrimmed);
	CHECK_EQUAL("testX", toBeTrimmed);

	string toBeTrimmed1 = string("  te  st  X  ");
	remove_all_spaces (toBeTrimmed1);
	CHECK_EQUAL("testX", toBeTrimmed1);
	
	string toBeTrimmed2 = string(" \t t e\nst\tX\n");
	remove_all_spaces (toBeTrimmed2);
	CHECK_EQUAL("testX", toBeTrimmed2);

	string toBeTrimmed3 = string(" \t \n te \n stX\n");
	remove_all_spaces (toBeTrimmed3);
	CHECK_EQUAL("testX", toBeTrimmed3);

}


TEST(get_property_value) {
	string dest = string("");
	bool a = get_property_value("key.key=value+eulav ",dest);
	CHECK_EQUAL("value+eulav", dest);
	CHECK_EQUAL(a, true);
	
	dest = string("");
	a = get_property_value("horrible key = value/value",dest);
	CHECK_EQUAL("value/value", dest);
	CHECK_EQUAL(a, true);

	dest = string("");
	a = get_property_value("better.key = but very horrible value  ",dest);
	CHECK_EQUAL("but very horrible value", dest);
	CHECK_EQUAL(a, true);

	dest = string("");
	a = get_property_value("better.key but errornous value  ",dest);
	CHECK_EQUAL("", dest);
	CHECK_EQUAL(a, false);
}

TEST(starts_with) {
	bool a = starts_with("aa bb cc","aa b");
	CHECK_EQUAL(a, true);
	
	a = starts_with("aa bb cc","aab");
	CHECK_EQUAL(a, false);
}


TEST(user_properties_file) {
	string f = user_properties_file();
	CHECK_EQUAL(f.find(".icedtea") >= 0, true);
	CHECK_EQUAL(f.find(default_file_ITW_deploy_props_name) >= 0, true);
}

TEST(main_properties_file) {
	string f = main_properties_file();
	CHECK_EQUAL(f.find(default_file_ITW_deploy_props_name) >= 0, true);
	CHECK_EQUAL(f.find(".java") >= 0, true);
}

TEST(default_java_properties_file) {
	string f = default_java_properties_file();
	CHECK_EQUAL(f.find(default_file_ITW_deploy_props_name) >= 0, true);
	CHECK_EQUAL(f.find("lib") >= 0, true);
}


TEST(find_system_config_file) {
	string f1 = temporary_file("key1=value1");
	string f2 = temporary_file("key2=value2");
	string f3 = temporary_file("key3=value3");
	string dest=string("");
	find_system_config_file(f1, f2, true, f3, dest);
	CHECK_EQUAL(f1, dest);
	dest=string("");
	find_system_config_file(f1, f2, false, f3, dest);
	CHECK_EQUAL(f1, dest);

	remove(f1.c_str());
	dest=string("");
	find_system_config_file(f1, f2, true, f3, dest);
	CHECK_EQUAL(f2, dest);
	dest=string("");
	find_system_config_file(f1, f2, false, f3, dest);
	CHECK_EQUAL(f3, dest);
	
	remove(f2.c_str());
	dest=string("");
	find_system_config_file(f1, f2, true, f3, dest);
	CHECK_EQUAL("", dest); //forcing not existing f2
	dest=string("");
	find_system_config_file(f1, f2, false, f3, dest);
	CHECK_EQUAL(f3, dest);
	
	remove(f3.c_str());
	dest=string("");
	find_system_config_file(f1, f2, true, f3, dest);
	CHECK_EQUAL("", dest);
	dest=string("");
	find_system_config_file(f1, f2, true, f3, dest);
	CHECK_EQUAL("", dest);
}

TEST(find_property) {
	string f1 = temporary_file("key1=value1");
	string dest=string("");
	find_property(f1, "key1", dest);
	CHECK_EQUAL("value1", dest);
	dest=string("");
	find_property(f1, "key2", dest);
	CHECK_EQUAL("", dest);
	dest=string("");
	find_property(f1, "value1", dest);
	CHECK_EQUAL("", dest);
	remove(f1.c_str());

	string f2 = temporary_file("key2 =value2 key3=value3\n key5 = value5");
	dest=string("");
	find_property(f2, "key2", dest);
	CHECK_EQUAL("value2 key3=value3", dest);
	dest=string("");
	find_property(f2, "key1", dest);
	CHECK_EQUAL("", dest);
	dest=string("");
	find_property(f2, "key3", dest);
	CHECK_EQUAL("", dest);
	dest=string("");
	find_property(f2, "key5", dest);
	CHECK_EQUAL("value5", dest);
	remove(f2.c_str());

	string f3 = temporary_file("ke.y3= value3\nkey4=value4");
	dest=string("");
	find_property(f3, "ke.y3", dest);
	CHECK_EQUAL("value3", dest);
	dest=string("");
	find_property(f3, "key4", dest);
	CHECK_EQUAL("value4", dest);
	remove(f3.c_str());
}

TEST(read_deploy_property_value1) {
	string f1 = temporary_file("key1=value11");
	string f2 = temporary_file("key1=value12");
	string f3 = temporary_file("key2=value23");
	string dest=string("");
	read_deploy_property_value(f1,f2,true, "key1", dest);
	CHECK_EQUAL("value11", dest);
	dest=string("");
	read_deploy_property_value(f2,f1,true, "key1", dest);
	CHECK_EQUAL("value12", dest);
	dest=string("");
	read_deploy_property_value(f1,f3,true, "key1", dest);
	CHECK_EQUAL("value11", dest);
	dest=string("");
	read_deploy_property_value(f3,f1,true, "key1", dest);
	CHECK_EQUAL("value11", dest);
	read_deploy_property_value(f3,f2,true, "key1", dest);
	CHECK_EQUAL("value12", dest);
	dest=string("");
	read_deploy_property_value(f2,f3,true, "key1", dest);
	CHECK_EQUAL("value12", dest);

	dest=string("");
	read_deploy_property_value(f1,f2,true, "key2", dest);
	CHECK_EQUAL("", dest);
	dest=string("");
	read_deploy_property_value(f2,f1,true, "key2", dest);
	CHECK_EQUAL("", dest);
	dest=string("");
	read_deploy_property_value(f1,f3,true, "key2", dest);
	CHECK_EQUAL("value23", dest);
	dest=string("");
	read_deploy_property_value(f3,f1,true, "key2", dest);
	CHECK_EQUAL("value23", dest);
	read_deploy_property_value(f3,f2,true, "key2", dest);
	CHECK_EQUAL("value23", dest);
	dest=string("");
	read_deploy_property_value(f2,f3,true, "key2", dest);
	CHECK_EQUAL("value23", dest);

	remove(f1.c_str());/////////////////////////////////
	dest=string("");
	read_deploy_property_value(f1,f2,true, "key1", dest);
	CHECK_EQUAL("value12", dest);
	dest=string("");
	read_deploy_property_value(f2,f1,true, "key1", dest);
	CHECK_EQUAL("value12", dest);
	dest=string("");
	read_deploy_property_value(f1,f3,true, "key1", dest);
	CHECK_EQUAL("", dest);
	dest=string("");
	read_deploy_property_value(f3,f1,true, "key1", dest);
	CHECK_EQUAL("", dest);
	read_deploy_property_value(f3,f2,true, "key1", dest);
	CHECK_EQUAL("value12", dest);
	dest=string("");
	read_deploy_property_value(f2,f3,true, "key1", dest);
	CHECK_EQUAL("value12", dest);

	dest=string("");
	read_deploy_property_value(f1,f2,true, "key2", dest);
	CHECK_EQUAL("", dest);
	dest=string("");
	read_deploy_property_value(f2,f1,true, "key2", dest);
	CHECK_EQUAL("", dest);
	dest=string("");
	read_deploy_property_value(f1,f3,true, "key2", dest);
	CHECK_EQUAL("value23", dest);
	dest=string("");
	read_deploy_property_value(f3,f1,true, "key2", dest);
	CHECK_EQUAL("value23", dest);
	read_deploy_property_value(f3,f2,true, "key2", dest);
	CHECK_EQUAL("value23", dest);
	dest=string("");
	read_deploy_property_value(f2,f3,true, "key2", dest);
	CHECK_EQUAL("value23", dest);

	remove(f3.c_str());/////////////////////////////////
	dest=string("");
	read_deploy_property_value(f1,f2,true, "key1", dest);
	CHECK_EQUAL("value12", dest);
	dest=string("");
	read_deploy_property_value(f2,f1,true, "key1", dest);
	CHECK_EQUAL("value12", dest);
	dest=string("");
	read_deploy_property_value(f1,f3,true, "key1", dest);
	CHECK_EQUAL("", dest);
	dest=string("");
	read_deploy_property_value(f3,f1,true, "key1", dest);
	CHECK_EQUAL("", dest);
	read_deploy_property_value(f3,f2,true, "key1", dest);
	CHECK_EQUAL("value12", dest);
	dest=string("");
	read_deploy_property_value(f2,f3,true, "key1", dest);
	CHECK_EQUAL("value12", dest);

	dest=string("");
	read_deploy_property_value(f1,f2,true, "key2", dest);
	CHECK_EQUAL("", dest);
	dest=string("");
	read_deploy_property_value(f2,f1,true, "key2", dest);
	CHECK_EQUAL("", dest);
	dest=string("");
	read_deploy_property_value(f1,f3,true, "key2", dest);
	CHECK_EQUAL("", dest);
	dest=string("");
	read_deploy_property_value(f3,f1,true, "key2", dest);
	CHECK_EQUAL("", dest);
	read_deploy_property_value(f3,f2,true, "key2", dest);
	CHECK_EQUAL("", dest);
	dest=string("");
	read_deploy_property_value(f2,f3,true, "key2", dest);
	CHECK_EQUAL("", dest);

	remove(f2.c_str());/////////////////////////////////

}



TEST(read_deploy_property_value2) {
	string f1 = temporary_file("key1=value11");
	string f2 = temporary_file("key1=value12");
	string f3 = temporary_file("key2=value23");
	string dest=string("");
	read_deploy_property_value(f1,f2,false, "key1", dest);
	CHECK_EQUAL("value11", dest);
	dest=string("");
	read_deploy_property_value(f2,f1,false, "key1", dest);
	CHECK_EQUAL("value12", dest);
	dest=string("");
	read_deploy_property_value(f1,f3,false, "key1", dest);
	CHECK_EQUAL("value11", dest);
	dest=string("");
	read_deploy_property_value(f3,f1,false, "key1", dest);
	CHECK_EQUAL("", dest);
	read_deploy_property_value(f3,f2,false, "key1", dest);
	CHECK_EQUAL("", dest);
	dest=string("");
	read_deploy_property_value(f2,f3,false, "key1", dest);
	CHECK_EQUAL("value12", dest);

	dest=string("");
	read_deploy_property_value(f1,f2,false, "key2", dest);
	CHECK_EQUAL("", dest);
	dest=string("");
	read_deploy_property_value(f2,f1,false, "key2", dest);
	CHECK_EQUAL("", dest);
	dest=string("");
	read_deploy_property_value(f1,f3,false, "key2", dest);
	CHECK_EQUAL("", dest);
	dest=string("");
	read_deploy_property_value(f3,f1,false, "key2", dest);
	CHECK_EQUAL("value23", dest);
	read_deploy_property_value(f3,f2,false, "key2", dest);
	CHECK_EQUAL("value23", dest);
	dest=string("");
	read_deploy_property_value(f2,f3,false, "key2", dest);
	CHECK_EQUAL("", dest);

	remove(f1.c_str());/////////////////////////////////
	dest=string("");
	read_deploy_property_value(f1,f2,false, "key1", dest);
	CHECK_EQUAL("", dest);
	dest=string("");
	read_deploy_property_value(f2,f1,false, "key1", dest);
	CHECK_EQUAL("value12", dest);
	dest=string("");
	read_deploy_property_value(f1,f3,false, "key1", dest);
	CHECK_EQUAL("", dest);
	dest=string("");
	read_deploy_property_value(f3,f1,false, "key1", dest);
	CHECK_EQUAL("", dest);
	read_deploy_property_value(f3,f2,false, "key1", dest);
	CHECK_EQUAL("", dest);
	dest=string("");
	read_deploy_property_value(f2,f3,false, "key1", dest);
	CHECK_EQUAL("value12", dest);

	dest=string("");
	read_deploy_property_value(f1,f2,false, "key2", dest);
	CHECK_EQUAL("", dest);
	dest=string("");
	read_deploy_property_value(f2,f1,false, "key2", dest);
	CHECK_EQUAL("", dest);
	dest=string("");
	read_deploy_property_value(f1,f3,false, "key2", dest);
	CHECK_EQUAL("", dest);
	dest=string("");
	read_deploy_property_value(f3,f1,false, "key2", dest);
	CHECK_EQUAL("value23", dest);
	read_deploy_property_value(f3,f2,false, "key2", dest);
	CHECK_EQUAL("value23", dest);
	dest=string("");
	read_deploy_property_value(f2,f3,false, "key2", dest);
	CHECK_EQUAL("", dest);

	remove(f3.c_str());/////////////////////////////////
	dest=string("");
	read_deploy_property_value(f1,f2,false, "key1", dest);
	CHECK_EQUAL("", dest);
	dest=string("");
	read_deploy_property_value(f2,f1,false, "key1", dest);
	CHECK_EQUAL("value12", dest);
	dest=string("");
	read_deploy_property_value(f1,f3,false, "key1", dest);
	CHECK_EQUAL("", dest);
	dest=string("");
	read_deploy_property_value(f3,f1,false, "key1", dest);
	CHECK_EQUAL("", dest);
	read_deploy_property_value(f3,f2,false, "key1", dest);
	CHECK_EQUAL("", dest);
	dest=string("");
	read_deploy_property_value(f2,f3,false, "key1", dest);
	CHECK_EQUAL("value12", dest);

	dest=string("");
	read_deploy_property_value(f1,f2,false, "key2", dest);
	CHECK_EQUAL("", dest);
	dest=string("");
	read_deploy_property_value(f2,f1,false, "key2", dest);
	CHECK_EQUAL("", dest);
	dest=string("");
	read_deploy_property_value(f1,f3,false, "key2", dest);
	CHECK_EQUAL("", dest);
	dest=string("");
	read_deploy_property_value(f3,f1,false, "key2", dest);
	CHECK_EQUAL("", dest);
	read_deploy_property_value(f3,f2,false, "key2", dest);
	CHECK_EQUAL("", dest);
	dest=string("");
	read_deploy_property_value(f2,f3,false, "key2", dest);
	CHECK_EQUAL("", dest);

	remove(f2.c_str());/////////////////////////////////
}


TEST(find_custom_jre) {
	string f1 = temporary_file(custom_jre_key+"=value11");
	string f2 = temporary_file(custom_jre_key+"=value12");
	string f3 = temporary_file("key2=value23");
	string f4 = temporary_file("key2=value24");
	string dest=string("");
	find_custom_jre(f1,f2, dest);
	CHECK_EQUAL("value11", dest);
	dest=string("");
	find_custom_jre(f2,f1, dest);
	CHECK_EQUAL("value12", dest);
	dest=string("");
	find_custom_jre(f1,f3, dest);
	CHECK_EQUAL("value11", dest);
	dest=string("");
	find_custom_jre(f3,f1, dest);
	CHECK_EQUAL("value11", dest);
	dest=string("");
	find_custom_jre(f3,f4, dest);
	CHECK_EQUAL("", dest);
	remove(f1.c_str());/////////////////////////////////
	dest=string("");
	find_custom_jre(f1,f2, dest);
	CHECK_EQUAL("value12", dest);
	dest=string("");
	find_custom_jre(f2,f1, dest);
	CHECK_EQUAL("value12", dest);
	dest=string("");
	find_custom_jre(f1,f3, dest);
	CHECK_EQUAL("", dest);
	dest=string("");
	find_custom_jre(f3,f1, dest);
	CHECK_EQUAL("", dest);
	remove(f3.c_str());/////////////////////////////////
	dest=string("");
	find_custom_jre(f1,f2, dest);
	CHECK_EQUAL("value12", dest);
	dest=string("");
	find_custom_jre(f2,f1, dest);
	CHECK_EQUAL("value12", dest);
	dest=string("");
	find_custom_jre(f1,f3, dest);
	CHECK_EQUAL("", dest);
	dest=string("");
	find_custom_jre(f3,f1, dest);
	CHECK_EQUAL("", dest);
	remove(f2.c_str());/////////////////////////////////
	dest=string("");
	find_custom_jre(f1,f2, dest);
	CHECK_EQUAL("", dest);
	dest=string("");
	find_custom_jre(f2,f1, dest);
	CHECK_EQUAL("", dest);
	dest=string("");
	find_custom_jre(f1,f3, dest);
	CHECK_EQUAL("", dest);
	dest=string("");
	find_custom_jre(f3,f1, dest);
	CHECK_EQUAL("", dest);
	remove(f4.c_str());/////////////////////////////////

}
