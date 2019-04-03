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

#include <cstdlib>

#include <map>

#include "IcedTeaNPPlugin.h"
#include "checked_allocations.h"
#include "browser_mock_npidentifier.h"

struct MockedNPIdentifier_t; // foward declare
typedef std::basic_string<char, std::char_traits<char>, SafeAllocator> SafeString;
typedef std::map<int, MockedNPIdentifier_t*, std::less<int>, SafeAllocator> SafeIntToIDMap;
typedef std::map<SafeString, MockedNPIdentifier_t*, std::less<SafeString>, SafeAllocator> SafeStringToIDMap;


// Handles creation of NPIdentifier

// Carefully avoids operator new so as not to interfere with leak detection.
// This mimics browser internal state.
struct MockedNPIdentifier_t {
    SafeString string;
    int integer;
    bool is_integer; // If false, it is a string

    // Carefully avoids operator new so as not to interfere with leak detection
    static MockedNPIdentifier_t* safe_allocate(SafeString str) {
        MockedNPIdentifier_t* mem = (MockedNPIdentifier_t*)malloc(sizeof(MockedNPIdentifier_t));
        new (&mem->string) SafeString(str);
        mem->integer = -1;
        mem->is_integer = false;
        return mem;
    }

    // Carefully avoids operator new so as not to interfere with leak detection
    static MockedNPIdentifier_t* safe_allocate(int i) {
        MockedNPIdentifier_t* mem = (MockedNPIdentifier_t*) malloc(
                sizeof(MockedNPIdentifier_t));
        new (&mem->string) SafeString();
        mem->integer = i;
        mem->is_integer = true;
        return mem;
    }
};

// Mimics global browser data. OK if not cleared in-between tests, does not change semantics.
// Used to ensure NPIdentifiers are unique. Never freed.
static SafeIntToIDMap __np_int_identifiers;
static SafeStringToIDMap __np_string_identifiers;

// Carefully avoids operator new so as not to interfere with leak detection
NPIdentifier browsermock_getstringidentifier(const NPUTF8* name) {
    SafeString safe_copy(name);
    if (__np_string_identifiers.find(safe_copy) == __np_string_identifiers.end()) {
        __np_string_identifiers[safe_copy] = MockedNPIdentifier_t::safe_allocate(safe_copy);
    }
    return __np_string_identifiers[safe_copy];
}

// Carefully avoids operator new so as not to interfere with leak detection
NPIdentifier browsermock_getintidentifier(int i) {
    if (__np_int_identifiers.find(i) == __np_int_identifiers.end()) {
        __np_int_identifiers[i] = MockedNPIdentifier_t::safe_allocate(i);
    }
    return __np_int_identifiers[i];
}

bool browsermock_identifierisstring(NPIdentifier identifier) {
    MockedNPIdentifier_t* contents = (MockedNPIdentifier_t*)identifier;
    return !contents->is_integer;
}

NPUTF8* browsermock_utf8fromidentifier(NPIdentifier identifier) {
    MockedNPIdentifier_t* contents = (MockedNPIdentifier_t*)identifier;
    if (contents->is_integer) {
        return NULL;
    }

    // We expect this string to be freed with 'memfree'
    NPUTF8* copy = (NPUTF8*) browser_functions.memalloc(contents->string.size() + 1);
    memcpy(copy, contents->string.c_str(), contents->string.size() + 1);
    return copy;
}
