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

#include <fstream>
#include <UnitTest++.h>
#include <stdio.h>

#include <npapi.h>


#include "browser_mock.h"
#include "MemoryLeakDetector.h"

#include "IcedTeaPluginUtils.h"
#include "IcedTeaNPPlugin.h"


void doDebugErrorRun();

TEST(NPVariantAsString) {
    NPVariant var;
    STRINGZ_TO_NPVARIANT("test", var);

    std::string cppstr = IcedTeaPluginUtilities::NPVariantAsString(var);
    CHECK_EQUAL("test", cppstr);

}

TEST(NPStringCopy) {
    std::string cppstr = "test";
    NPString npstr = IcedTeaPluginUtilities::NPStringCopy(cppstr);

    CHECK_EQUAL(4, npstr.UTF8Length);
    CHECK_EQUAL("test", npstr.UTF8Characters);

    // NPAPI states that browser allocation function should be used for NPString/NPVariant
    CHECK_EQUAL(1, browsermock_unfreed_allocations());

    browser_functions.memfree((void*) npstr.UTF8Characters);

    CHECK_EQUAL(0, browsermock_unfreed_allocations());
}

TEST(NPVariantStringCopy) {
    std::string cppstr = "test";
    NPVariant npvar = IcedTeaPluginUtilities::NPVariantStringCopy(cppstr);

    CHECK_EQUAL(NPVariantType_String, npvar.type);

    CHECK_EQUAL(4, npvar.value.stringValue.UTF8Length);
    CHECK_EQUAL("test", npvar.value.stringValue.UTF8Characters);

    CHECK_EQUAL(1, browsermock_unfreed_allocations());

    browser_functions.memfree((void*) npvar.value.stringValue.UTF8Characters);

    CHECK_EQUAL(0, browsermock_unfreed_allocations());
}

TEST(NPIdentifierAsString) {
    const char test_string[] = "foobar";
    MemoryLeakDetector leak_detector;
    /* Ensure destruction */{
        std::string str = IcedTeaPluginUtilities::NPIdentifierAsString(
                browser_functions.getstringidentifier(test_string));
        CHECK_EQUAL(test_string, str);
    }
    CHECK_EQUAL(0, leak_detector.memory_leaks());
}

TEST(trim) {
	std::string toBeTrimmed = std::string(" testX ");
	IcedTeaPluginUtilities::trim (toBeTrimmed);
	CHECK_EQUAL("testX", toBeTrimmed);
	
	std::string toBeTrimmed2 = std::string(" \t testX\n");
	IcedTeaPluginUtilities::trim (toBeTrimmed2);
	CHECK_EQUAL("testX", toBeTrimmed2);

	std::string toBeTrimmed3 = std::string(" \t \n te \n stX\n");
	IcedTeaPluginUtilities::trim (toBeTrimmed3);
	CHECK_EQUAL("te \n stX", toBeTrimmed3);
}

TEST(unescape1) {
	std::string toBeEscaped = std::string("he\\\\=llo\\=my=boy\\\\ who :liv\\es in\\: space \\ and \\\\likes\\");
    /*he\\=llo\=my=boy\\ who :liv\es in\: space \ and \\likes\  */ 
	IcedTeaPluginUtilities::unescape(toBeEscaped);
    /* \\= -> \= , \= -> = ,  \\ -> \ , \e -> \e ,  \: -> : ,  \ -> \ ,  \\ -> \*/
    /*he\=llo=my=boy\ who :liv\es in: space \ and \likes\  */ 
	CHECK_EQUAL("he\\=llo=my=boy\\ who :liv\\es in: space \\ and \\likes\\", toBeEscaped);
}

TEST(unescape2) {
	std::string toBeEscaped = std::string("w1\\tw2\\\\tw3\\nw4\\\\nw5\\=");
    /*w1\tw2\\tw3\nw4\\nw5\=*/ 
	IcedTeaPluginUtilities::unescape(toBeEscaped);
    /*w1TABw2\tw3NWLINEw4\nw5=*/
	CHECK_EQUAL("w1\tw2\\tw3\nw4\\nw5=", toBeEscaped);
}

TEST(unescape3) {
	std::string toBeEscaped = std::string("w1\\rw2\\\\rw3=");
    IcedTeaPluginUtilities::unescape(toBeEscaped);
	CHECK_EQUAL("w1\rw2\\rw3=", toBeEscaped);
}


/* Creates a temporary file with the specified contents */
static std::string temporary_file(const std::string& contents) {
	std::string path = tmpnam(NULL); /* POSIX function, fine for test suite */
	std::ofstream myfile;
	myfile.open (path.c_str());
	myfile << contents;
	myfile.close();
	return path;
}


TEST(file_exists) {
	std::string f1 = temporary_file("dummy content");
	bool a = IcedTeaPluginUtilities::file_exists(f1);
	CHECK_EQUAL(a, true);
	
	remove(f1.c_str());
	bool b = IcedTeaPluginUtilities::file_exists(f1);
	CHECK_EQUAL(b, false);

	std::string dir = tmpnam(NULL);
	const int PERMISSIONS_MASK = S_IRWXU | S_IRGRP | S_IXGRP | S_IROTH | S_IXOTH; // 0755
	bool created_dir = g_mkdir(dir.c_str(), PERMISSIONS_MASK);
	CHECK_EQUAL(created_dir, false);
	CHECK_EQUAL(IcedTeaPluginUtilities::file_exists(dir), true);
}


TEST(is_directory) {
	std::string n = tmpnam(NULL);
	bool no_file = IcedTeaPluginUtilities::is_directory(n);
	CHECK_EQUAL(no_file, false);

	std::string f = temporary_file("dummy content");
	bool is_directory = IcedTeaPluginUtilities::is_directory(f);
	CHECK_EQUAL(is_directory, false);

	std::string d = tmpnam(NULL);
	const int PERMISSIONS_MASK = S_IRWXU | S_IRGRP | S_IXGRP | S_IROTH | S_IXOTH; // 0755
	bool created_test_dir = g_mkdir(d.c_str(), PERMISSIONS_MASK);
	CHECK_EQUAL(created_test_dir, false);
	bool is_directory2 = IcedTeaPluginUtilities::is_directory(d);
	CHECK_EQUAL(is_directory2, true);
}


TEST(create_dir) {
	FILE* old1 = stdout;
	FILE* old2 = stderr;
	char* buf1 = " 	                         ";
	char* buf2 = "                           ";
	stdout = fmemopen (buf1, strlen (buf1), "rw");
	stderr = fmemopen (buf2, strlen (buf2), "rw");

	std::string f1 = tmpnam(NULL);
	bool res1 = IcedTeaPluginUtilities::create_dir(f1);
	CHECK_EQUAL(res1, true);
	CHECK_EQUAL(IcedTeaPluginUtilities::is_directory(f1), true);

	std::string f2 = temporary_file("dummy content");
	bool res2 = IcedTeaPluginUtilities::create_dir(f2);
	CHECK_EQUAL(res2, false);
	CHECK_EQUAL(IcedTeaPluginUtilities::is_directory(f2), false);

	std::string f3 = tmpnam(NULL);
	const int PERMISSIONS_MASK = S_IRWXU | S_IRGRP | S_IXGRP | S_IROTH | S_IXOTH; // 0755
	bool created_test_dir = g_mkdir(f3.c_str(), PERMISSIONS_MASK);
	CHECK_EQUAL(created_test_dir, false);
	bool res3 = IcedTeaPluginUtilities::create_dir(f3);
	CHECK_EQUAL(res3, true);
	CHECK_EQUAL(IcedTeaPluginUtilities::is_directory(f3), true);

	std::string f4 = tmpnam(NULL);
	const int READONLY_PERMISSIONS_MASK = S_IRUSR | S_IRGRP | S_IROTH; // 0444
	bool created_test_dir2 = g_mkdir(f4.c_str(), READONLY_PERMISSIONS_MASK);
	CHECK_EQUAL(created_test_dir2, false);
	std::string subdir = f4 + "/test";
	bool res4 = IcedTeaPluginUtilities::create_dir(subdir);
	CHECK_EQUAL(res4, false);
	CHECK_EQUAL(IcedTeaPluginUtilities::is_directory(subdir), false);

	fclose(stdout);
	fclose(stderr);
	stdout = old1;
	stderr = old2;
}


void doDebugErrorRun(int max) {
	FILE* old1 = stdout;
	FILE* old2 = stderr;
	char* buf1 = " 	                         ";
	char* buf2 = "                           ";
	stdout = fmemopen (buf1, strlen (buf1), "rw");
	stderr = fmemopen (buf2, strlen (buf2), "rw");
	
	clock_t begin1, end1;
	clock_t begin2, end2;
	int i;
	std::string hello = std::string("hello");
	std::string eello = std::string("eello");
	
	begin1 = clock();
	for (i = 0 ; i < max ; i++ ) {
		PLUGIN_DEBUG("hello \n");
		PLUGIN_DEBUG("hello %s\n", hello.c_str());
		PLUGIN_DEBUG("hello %d %d\n", 10 , 0.5);
		PLUGIN_DEBUG("hello %s %s \n", hello.c_str() , hello.c_str());
		PLUGIN_DEBUG("hello %s %d %s %d\n", hello.c_str() ,10, hello.c_str(), 0.5);
	}
	end1 = clock();
	begin2 = clock();
	for (i = 0 ; i < max ; i++ ) {
		PLUGIN_ERROR("eello \n");
		PLUGIN_ERROR("eello %s\n", eello.c_str());
		PLUGIN_ERROR("eello %d %d\n", 10 , 0.5);
		PLUGIN_ERROR("eello %s %s \n", eello.c_str() , eello.c_str());
		PLUGIN_ERROR("eello %s %d %s %d\n", eello.c_str() ,10, eello.c_str(), 0.5);
	}
	end2 = clock();
	fclose(stdout);
	fclose(stderr);
	stdout = old1;
	stderr = old2;
	long time_spent1 = ((end1 - begin1));
	long time_spent2 = ((end2 - begin2));
	fprintf  (stdout, "  PLUGIN_DEBUG %d, ", time_spent1);
	fprintf  (stdout, "PLUGIN_ERROR %d\n", time_spent2);
}

void doDebugErrorRun() {
	doDebugErrorRun(1000000);
}

/*
 *The family of PLUGIN_DEBUG_ERROR_PROFILING tests actually do not test.
 *It is just messure that the mechanisms around do not break soething fataly.
 */

TEST(PLUGIN_DEBUG_ERROR_PROFILING_debug_on_headers_off) {
	bool plugin_debug_backup = plugin_debug;
	bool plugin_debug_headers_backup = plugin_debug_headers;
	bool plugin_debug_console_backup = plugin_debug_to_console;
	bool plugin_debug_system_backup = plugin_debug_to_system;
	plugin_debug_to_console = false;
	plugin_debug = true;
	plugin_debug_to_system = false; //no need to torture system log in testing
	doDebugErrorRun();
	plugin_debug = plugin_debug_backup;
	plugin_debug_headers = plugin_debug_headers_backup;
	plugin_debug_to_console =  plugin_debug_console_backup;
	plugin_debug_to_system = plugin_debug_system_backup;
}
TEST(PLUGIN_DEBUG_ERROR_PROFILING_debug_off_headers_off) {
	bool plugin_debug_backup = plugin_debug;
	bool plugin_debug_headers_backup = plugin_debug_headers;
	bool plugin_debug_console_backup = plugin_debug_to_console;
	bool plugin_debug_system_backup = plugin_debug_to_system;
	plugin_debug_to_console = false;
	plugin_debug = false;
	plugin_debug_to_system = false; //no need to torture system log in testing
	doDebugErrorRun();
	plugin_debug = plugin_debug_backup;
	plugin_debug_headers = plugin_debug_headers_backup;
	plugin_debug_to_console =  plugin_debug_console_backup;
	plugin_debug_to_system = plugin_debug_system_backup;
}


TEST(PLUGIN_DEBUG_ERROR_PROFILING_debug_on_headers_on) {
	bool plugin_debug_backup = plugin_debug;
	bool plugin_debug_headers_backup = plugin_debug_headers;
	bool plugin_debug_console_backup = plugin_debug_to_console;
	bool plugin_debug_system_backup = plugin_debug_to_system;
	plugin_debug_to_console = false;
	plugin_debug = true;
	plugin_debug_headers = true;
	plugin_debug_to_system = false; //no need to torture system log in testing
	doDebugErrorRun();
	plugin_debug = plugin_debug_backup;
	plugin_debug_headers = plugin_debug_headers_backup;
	plugin_debug_to_console =  plugin_debug_console_backup;
	plugin_debug_to_system = plugin_debug_system_backup;
}

TEST(PLUGIN_DEBUG_ERROR_PROFILING_debug_off_headers_on) {
	bool plugin_debug_backup = plugin_debug;
	bool plugin_debug_headers_backup = plugin_debug_headers;
	bool plugin_debug_console_backup = plugin_debug_to_console;
	bool plugin_debug_system_backup = plugin_debug_to_system;
	plugin_debug_to_console = false;
	plugin_debug = false;
	plugin_debug_headers = true;
	plugin_debug_to_system = false; //no need to torture system log in testing
	doDebugErrorRun();
	plugin_debug = plugin_debug_backup;
	plugin_debug_headers = plugin_debug_headers_backup;
	plugin_debug_to_console =  plugin_debug_console_backup;
	plugin_debug_to_system = plugin_debug_system_backup;
}

TEST(PLUGIN_DEBUG_ERROR_PROFILING_debug_on_headers_on_syslog_on) {
	bool plugin_debug_backup = plugin_debug;
	bool plugin_debug_headers_backup = plugin_debug_headers;
	bool plugin_debug_console_backup = plugin_debug_to_console;
	bool plugin_debug_system_backup = plugin_debug_to_system;
	plugin_debug_to_console = false;
	plugin_debug = true;
	plugin_debug_headers = true;
	plugin_debug_to_system = true;
	doDebugErrorRun(50);
	plugin_debug = plugin_debug_backup;
	plugin_debug_headers = plugin_debug_headers_backup;
	plugin_debug_to_console =  plugin_debug_console_backup;
	plugin_debug_to_system = plugin_debug_system_backup;
}

