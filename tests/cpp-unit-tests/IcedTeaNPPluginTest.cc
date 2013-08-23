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

#include <cstdio>

#include <npapi.h>

#include <UnitTest++.h>

#include "MemoryLeakDetector.h"

#include "IcedTeaNPPlugin.h"
#include "IcedTeaScriptablePluginObject.h"
#include "IcedTeaPluginUtils.h"

TEST(NP_GetMIMEDescription) {
	std::string MIME_type = NP_GetMIMEDescription();
	CHECK(MIME_type.find("application/x-java-applet") != std::string::npos);
	CHECK(MIME_type.find("application/x-java-vm") != std::string::npos);
}

/* Not normally exposed */
std::vector<std::string*>* get_jvm_args();

TEST(get_jvm_args) {
	std::vector<std::string*>* args = get_jvm_args();
	CHECK(args != NULL);

	IcedTeaPluginUtilities::freeStringPtrVector(args);
}


static IcedTeaScriptableJavaPackageObject* get_scriptable_package_object() {
    NPP_t instance = { /*Plugin data*/plugin_data_new(), /* Browser data*/0 };

    /* Get the packages object (since instance.pdata->is_applet_instance == false) */
    NPObject* obj = get_scriptable_object(&instance);

    /* Make sure we got an IcedTeaScriptableJavaPackageObject */
    CHECK(obj->_class->deallocate == IcedTeaScriptableJavaPackageObject::deAllocate);

    plugin_data_destroy(&instance);
    return (IcedTeaScriptableJavaPackageObject*)obj;
}

TEST(get_scriptable_object) {
    MemoryLeakDetector leak_detector;
    // We test without an applet context, pending mocking of applet instances.
    IcedTeaScriptableJavaPackageObject* obj = get_scriptable_package_object(); // Calls get_scriptable_object

    browser_functions.releaseobject(obj);

    CHECK(leak_detector.memory_leaks() == 0);
}

TEST(NP_GetValue) {
  void* __unused = NULL;
  gchar* char_value = NULL;

  /* test plugin name */ {
	CHECK_EQUAL(NPERR_NO_ERROR, 
		NP_GetValue(__unused, NPPVpluginNameString, &char_value));
	CHECK(std::string(char_value).find(PLUGIN_NAME) != std::string::npos);
	g_free(char_value);
	char_value = NULL;
  }
  /* test plugin desc */ {
	CHECK_EQUAL(NPERR_NO_ERROR, 
		NP_GetValue(__unused, NPPVpluginDescriptionString, &char_value));
	CHECK(std::string(char_value).find("executes Java applets") != std::string::npos);
	g_free(char_value);
	char_value = NULL;
  }
  /* test plugin unknown */ {
    printf("NOTE: Next error expected\n"); // the following will print an error message
	CHECK_EQUAL(NPERR_GENERIC_ERROR, 
		NP_GetValue(__unused, NPPVformValue, &char_value));
	g_free(char_value);
	char_value = NULL;
  }
}
