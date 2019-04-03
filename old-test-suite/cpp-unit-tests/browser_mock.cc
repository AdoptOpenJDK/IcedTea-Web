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

// Browser mock functions. Add more as needed.

#include <new>
#include <map>
#include <cstring>

#include <npapi.h>

#include "checked_allocations.h"

#include "UnitTest++.h"

#include "browser_mock.h"
#include "browser_mock_npidentifier.h"

// 'Browser' global data

// Stores NPAPI allocations
static AllocationSet __allocations;

// Mocked functions

// It is expected that these will only run during a unit test
static void* mock_memalloc(uint32_t size) {
    void* mem = malloc(size);
    __allocations.insert(mem);
    return mem;
}

static void mock_memfree(void* ptr) {
    if (__allocations.erase(ptr)) {
        free(ptr);
    } else {
        printf("Attempt to free memory with browserfunctions.memfree that was not allocated by the browser!\n");
        CHECK(false);
    }
}

static NPObject* mock_retainobject(NPObject* obj) {
    obj->referenceCount++;
    return obj;
}

static void mock_releaseobject(NPObject* obj) {
    if (--(obj->referenceCount) == 0) {
        if (obj->_class->deallocate) {
            obj->_class->deallocate(obj);
        } else {
            mock_memfree(obj);
        }
    }
}

static void mock_releasevariantvalue(NPVariant* variant) {
    if (variant->type == NPVariantType_String) {
    /* Only string and object values require freeing */
        mock_memfree((void*)variant->value.stringValue.UTF8Characters);
    } else if (variant->type == NPVariantType_Object) {
        mock_releaseobject(variant->value.objectValue);
    }
}

static NPObject* mock_createobject(NPP instance, NPClass* np_class) {
	NPObject* obj;
	if (np_class->allocate) {
		obj = np_class->allocate(instance, np_class);
	} else {
		obj = (NPObject*) mock_memalloc(sizeof(NPObject));
	}
	obj->referenceCount = 1;
	obj->_class = np_class;
	return obj;
}

static bool mock_getproperty(NPP npp, NPObject* obj, NPIdentifier property_name, NPVariant* result) {
    if (obj->_class->getProperty) {
        return obj->_class->getProperty(obj, property_name, result);
    } else {
        return false;
    }
}

static bool mock_setproperty(NPP npp, NPObject* obj, NPIdentifier property_name, const NPVariant* value) {
    if (obj->_class->setProperty) {
        return obj->_class->setProperty(obj, property_name, value);
    } else {
        return false;
    }
}

NPNetscapeFuncs browsermock_create_table() {
    NPNetscapeFuncs browser_functions;
    memset(&browser_functions, 0, sizeof(NPNetscapeFuncs));
    browser_functions.size = sizeof(NPNetscapeFuncs);

    browser_functions.memalloc = &mock_memalloc;
    browser_functions.memfree = &mock_memfree;

    browser_functions.createobject = &mock_createobject;
    browser_functions.retainobject = &mock_retainobject;
    browser_functions.releaseobject = &mock_releaseobject;
    browser_functions.releasevariantvalue = &mock_releasevariantvalue;

    browser_functions.setproperty = &mock_setproperty;
    browser_functions.getproperty = &mock_getproperty;

    browser_functions.utf8fromidentifier = &browsermock_utf8fromidentifier;
    browser_functions.getstringidentifier = &browsermock_getstringidentifier;
    browser_functions.identifierisstring = &browsermock_identifierisstring;

    return browser_functions;
}

void browsermock_clear_state() {
    __allocations.clear();
}

int browsermock_unfreed_allocations() {
    return __allocations.size();
}
