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

#include <npapi.h>

#include "browser_mock.h"
#include "MemoryLeakDetector.h"

#include "IcedTeaPluginUtils.h"
#include "IcedTeaNPPlugin.h"

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
    // NB: Mocked definition of 'utf8fromidentifier' simply reads NPIdentifier as a char* string.
    const char test_string[] = "foobar";
    MemoryLeakDetector leak_detector;
    /* Ensure destruction */{
        std::string str = IcedTeaPluginUtilities::NPIdentifierAsString((NPIdentifier)test_string);
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
}
