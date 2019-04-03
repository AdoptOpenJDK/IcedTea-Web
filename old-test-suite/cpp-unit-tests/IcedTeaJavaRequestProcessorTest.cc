/* Copyright (C) 2013 Red Hat

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

#include <vector>
#include <string>

#include <npapi.h>

#include <UnitTest++.h>

#include "MemoryLeakDetector.h"

#include "IcedTeaJavaRequestProcessor.h"

/******************************************************************************
 *         Simple helper methods to keep the tests clean.                     *
 ******************************************************************************/

static const char* TEST_SOURCE = "[System]";

static std::string checked_return(JavaResultData* result) {
    CHECK(!result->error_occurred);
    return *result->return_string;
}

// Packages

static bool jrp_has_package(std::string package_name) {
    JavaRequestProcessor processor;
    JavaResultData* result = processor.hasPackage(0, package_name);
    CHECK(!result->error_occurred);
    return (result->return_identifier != 0);
}

// Classes

static std::string jrp_find_class(std::string name) {
    return checked_return(
            JavaRequestProcessor().findClass(0, name)
    );
}

// Object creation

static std::string jrp_new_object_with_constructor(std::string class_id,
        std::string method_id,
        std::vector<std::string> args = std::vector<std::string>()) {
    return checked_return(
            JavaRequestProcessor().newObjectWithConstructor(TEST_SOURCE,
                    class_id, method_id, args)
    );
}

static std::string jrp_new_array(std::string class_id, std::string len) {
    return checked_return(
            JavaRequestProcessor().newArray(class_id, len)
    );
}

static std::string jrp_new_string(std::string str) {
    return checked_return(
            JavaRequestProcessor().newString(str)
    );
}

static std::string jrp_new_object(std::string class_id,
        std::vector<std::string> args = std::vector<std::string>()) {
    return checked_return(
            JavaRequestProcessor().newObject(TEST_SOURCE, class_id, args)
    );
}

static std::string jrp_get_value(std::string object_id) {
    return checked_return(
            JavaRequestProcessor().getValue(object_id)
    );
}

// Inheritance

static bool jrp_is_instance_of(std::string object_id, std::string class_id) {
    JavaRequestProcessor processor;
    JavaResultData* result = processor.isInstanceOf(object_id, class_id);

    CHECK(!result->error_occurred);
    return (result->return_identifier != 0);
}


// Java methods operations.

static std::string jrp_get_method_id(std::string class_id,
        std::string method_name,
        std::vector<std::string> args = std::vector<std::string>()) {
    return checked_return(
            JavaRequestProcessor().getMethodID(class_id,
                    browser_functions.getstringidentifier(method_name.c_str()), args)
    );
}

static std::string jrp_get_static_method_id(std::string class_id,
        std::string method_name,
        std::vector<std::string> args = std::vector<std::string>()) {
    return checked_return(
            JavaRequestProcessor().getStaticMethodID(class_id,
                    browser_functions.getstringidentifier(method_name.c_str()), args)
    );
}

static std::string jrp_call_method(std::string object_id,
        std::string method_name, std::vector<std::string> args = std::vector<std::string>()) {
    return checked_return(
            JavaRequestProcessor().callMethod(TEST_SOURCE, object_id,
                    method_name, args)
    );
}

static std::string jrp_call_static_method(std::string class_id,
        std::string method_name, std::vector<std::string> args = std::vector<std::string>()) {
    return checked_return(
            JavaRequestProcessor().callStaticMethod(TEST_SOURCE, class_id,
                    method_name, args)
    );
}

static std::string jrp_get_string(std::string object_id) {
    return checked_return(
            JavaRequestProcessor().getString(object_id)
    );
}

static std::string jrp_get_class_id(std::string object_id) {
    return checked_return(
            JavaRequestProcessor().getClassID(object_id)
    );
}


// Java field operations.

static std::string jrp_get_field(std::string object_id,
        std::string field_name) {
    return checked_return(
            JavaRequestProcessor().getField(TEST_SOURCE,
                    jrp_get_class_id(object_id), object_id, field_name)
    );
}

static std::string jrp_get_field_id(std::string class_id,
        std::string field_name) {
    return checked_return(
            JavaRequestProcessor().getFieldID(class_id, field_name)
    );
}

static std::string jrp_get_static_field_id(std::string class_id,
        std::string field_name) {
    return checked_return(
            JavaRequestProcessor().getStaticFieldID(class_id, field_name)
    );
}

static std::string jrp_get_static_field(std::string class_id,
        std::string field_name) {
    return checked_return(
            JavaRequestProcessor().getStaticField(TEST_SOURCE, class_id, field_name)
    );
}

static std::string jrp_set_field(std::string object_id, std::string field_name,
        std::string value_id) {
    return checked_return(
            JavaRequestProcessor().setField(TEST_SOURCE,
                    jrp_get_class_id(object_id), object_id, field_name, value_id)
    );
}

static std::string jrp_set_static_field(std::string class_id, std::string field_name,
        std::string value_id) {
    return checked_return(
            JavaRequestProcessor().setStaticField(TEST_SOURCE, class_id, field_name, value_id)
    );
}

// Java array operations.

static std::string jrp_set_slot(std::string object_id, std::string index,
        std::string value_id) {
    return checked_return(
            JavaRequestProcessor().setSlot(object_id, index, value_id)
    );
}

static std::string jrp_get_slot(std::string object_id, std::string index) {
    return checked_return(
            JavaRequestProcessor().getSlot(object_id, index)
    );
}

static std::string jrp_get_array_length(std::string object_id) {
    return checked_return(
            JavaRequestProcessor().getArrayLength(object_id)
    );
}

// Result of toString()

static std::string jrp_get_to_string_value(std::string object_id) {
    return checked_return(
            JavaRequestProcessor().getToStringValue(object_id)
    );
}

/******************************************************************************
 *         Compound helper methods.                                           *
 ******************************************************************************/

static NPP_t dummy_npp = {0,0};

static std::string create_java_integer(int value) {
    // Prepare a java integer object with the value 1
    NPVariant integer_variant;
    std::string integer_id;
    INT32_TO_NPVARIANT(value, integer_variant);
    createJavaObjectFromVariant(&dummy_npp, integer_variant, &integer_id);
    return integer_id;
}

static std::string create_null() {
    // Prepare a null object
    NPVariant null_variant;
    std::string null_id;
    NULL_TO_NPVARIANT(null_variant);
    createJavaObjectFromVariant(&dummy_npp, null_variant, &null_id);
    return null_id;
}

static NPVariant java_result_to_variant(std::string object_id) {
    NPVariant variant;
    IcedTeaPluginUtilities::javaResultToNPVariant(&dummy_npp, &object_id, &variant);
    return variant;
}

/* Call the no-argument constructor of an object */
static std::string jrp_noarg_construct(std::string classname) {
    std::string class_id = jrp_find_class(classname);
    std::string constructor_id = jrp_get_method_id(class_id, "<init>");
    return jrp_new_object_with_constructor(class_id, constructor_id);
}


/******************************************************************************
 * Test cases. Note that the tests exercise a variety of functions to first   *
 * create the appropriate conditions for the intended test.                   *
 ******************************************************************************/

SUITE(JavaRequestProcessor) {

    TEST(callMethod) {
        std::string object_id = jrp_noarg_construct("java.lang.Object");
        std::string tostring_result = jrp_get_string(
                jrp_call_method(object_id, "toString"));
        const char substr[] = "java.lang.Object@";
        // Check that the result of toString is as expected
        CHECK(strncmp(tostring_result.c_str(), substr, strlen(substr)) == 0);
    }

    /* Create a java.awt.Point, since it is one of the few standard classes with public fields. */
    TEST(getField_and_setField) {
        std::string object_id = jrp_noarg_construct("java.awt.Point");

        // Set the field 'x' to 1
        jrp_set_field(object_id, "x", create_java_integer(1));

        // Get the field 'x'
        NPVariant field_value = java_result_to_variant(jrp_get_field(object_id, "x"));

        // Ensure that the received field is 1
        CHECK(NPVARIANT_IS_INT32(field_value) && NPVARIANT_TO_INT32(field_value) == 1);
    }

    TEST(getStaticField_and_setStaticField) {
        // One of the few classes with a public & non-final static field that we can tinker with.
        // If it moves, this test will fail, in which-case this test should be updated to another appropriate field.
        std::string class_id = jrp_find_class("net.sourceforge.jnlp.controlpanel.DebuggingPanel");
        std::string properties_id = jrp_get_static_field(class_id, "properties");

        // Check that the field is initially a non-null object
        NPVariant sh_variant = java_result_to_variant(properties_id);
        CHECK(!NPVARIANT_IS_NULL(sh_variant) && NPVARIANT_IS_OBJECT(sh_variant));
        browser_functions.releasevariantvalue(&sh_variant);

        jrp_set_static_field(class_id, "properties", create_null());
        sh_variant = java_result_to_variant(jrp_get_static_field(class_id, "properties"));
        CHECK(NPVARIANT_IS_NULL(sh_variant));

        // Reset the field to its original contents
        jrp_set_static_field(class_id, "properties", properties_id);
        sh_variant = java_result_to_variant(jrp_get_static_field(class_id, "properties"));
        CHECK(!NPVARIANT_IS_NULL(sh_variant) && NPVARIANT_IS_OBJECT(sh_variant));
        browser_functions.releasevariantvalue(&sh_variant);
    }

    TEST(arrayIndexing) {
        const int ARRAY_LEN = 1;

        // We will create an Integer array of ARRAY_LEN
        std::vector<std::string> args;
        args.push_back(jrp_find_class("java.lang.Integer"));
        args.push_back(create_java_integer(ARRAY_LEN));

        // Create an array 'the hard way' (ie not using 'newArray') to test more of the API
        std::string array_id = jrp_call_static_method(jrp_find_class("java.lang.reflect.Array"), "newInstance", args);

        // Attempt to set the first element to 1
        jrp_set_slot(array_id, "0", create_java_integer(1));
        // Note we get an integer _object_, not a plain int literal
        std::string integer_id = jrp_get_slot(array_id, "0");
        NPVariant unboxed_slot_value = java_result_to_variant(jrp_call_method(integer_id, "intValue"));

        // Ensure that the received slot is 1
        CHECK(NPVARIANT_IS_INT32(unboxed_slot_value) && NPVARIANT_TO_INT32(unboxed_slot_value) == 1);
    }

    // Also exercises 'getToStringValue'
    TEST(newObject) {
        std::string object_id = jrp_new_object(jrp_find_class("java.lang.Object"));
        const char substr[] = "java.lang.Object@";
        // Check that the result of toString is as expected
        CHECK(strncmp(jrp_get_to_string_value(object_id).c_str(), substr, strlen(substr)) == 0);
    }

    TEST(hasPackage) {
        CHECK(jrp_has_package("java.lang"));
        CHECK(!jrp_has_package("not.an.icedtea_web.package"));
    }

    TEST(newArray) {
        const char ARRAY_LEN[] = "10";
        std::string array_id = jrp_new_array(jrp_find_class("java.lang.Integer"), ARRAY_LEN);
        CHECK_EQUAL(ARRAY_LEN, jrp_get_array_length(array_id));
    }

    TEST(newString) {
        const char TEST_STRING[] = "foobar";
        std::string string_id = jrp_new_string(TEST_STRING);
        CHECK_EQUAL(TEST_STRING, jrp_get_string(string_id));
        CHECK_EQUAL(TEST_STRING, jrp_get_to_string_value(string_id));
    }

    // Can only really do sanity checks with given API
    TEST(getFieldID_getStaticFieldID) {
        CHECK(!jrp_get_field_id(jrp_find_class("java.awt.Point"), "x").empty());
        CHECK(!jrp_get_static_field_id(jrp_find_class("java.lang.Integer"), "MAX_VALUE").empty());
    }

    // Can only really do sanity checks with given API
    TEST(getStaticMethodID) {
        std::string class_id = jrp_find_class("java.lang.Integer");

        std::vector<std::string> argtypes;
        argtypes.push_back("Ljava.lang.String;");

        CHECK(!jrp_get_static_method_id(class_id, "valueOf", argtypes).empty());
    }

    TEST(isInstanceOf) {
        std::string point_id = jrp_noarg_construct("java.awt.Point");
        std::string object_class_id = jrp_find_class("java.lang.Object");
        CHECK(jrp_is_instance_of(point_id, object_class_id));
    }

    // Wasn't sure what the point of this method is to be honest, but it is used.
    // Here it simply returns back a passed string object.
    TEST(getValue) {
        const char TEST_STRING[] = "foobar";

        std::string str = jrp_get_string(jrp_get_value(jrp_new_string(TEST_STRING)));
        CHECK_EQUAL(TEST_STRING, str);
    }
}
