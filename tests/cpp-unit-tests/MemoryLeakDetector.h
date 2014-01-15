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

// Memory leak detection helper class.
// This utilizes checked_allocations.h & browser_mock.h to query how many unfreed allocations exist.
// As well, it clears global state that is problematic for accurate measure of memory leaks.

#ifndef MEMORYLEAKDETECTOR_H_
#define MEMORYLEAKDETECTOR_H_

#include <cstdio>
#include "browser_mock.h"
#include "checked_allocations.h"
#include "IcedTeaPluginUtils.h"

class MemoryLeakDetector {
public:
    MemoryLeakDetector() {
        reset();
    }

    /* Reset allocation counts and certain global state touched by the tests.
     * This is necessary to ensure accurate leak reporting for some functions. */
    void reset() {
        reset_global_state();
        initial_cpp_allocations = cpp_unfreed_allocations();
        initial_npapi_allocations = browsermock_unfreed_allocations();
    }

    /* Return allocation counts, after clearing global state that can conflict with the
     * leak detection. */
    int memory_leaks() {
        reset_global_state();
        int cpp_leaks = cpp_unfreed_allocations() - initial_cpp_allocations;
        int npapi_leaks = browsermock_unfreed_allocations() - initial_npapi_allocations;

        return cpp_leaks + npapi_leaks;
    }

    static void reset_global_state() {
        /* Clears allocations caused by storeInstanceID */
        IcedTeaPluginUtilities::clearInstanceIDs();
        /* Clears allocations caused by storeObjectMapping */
        IcedTeaPluginUtilities::clearObjectMapping();
        /*reset messages*/
        reset_pre_init_messages();
    }

    int initial_cpp_allocations;
    int initial_npapi_allocations;
};


#endif /* MEMORYLEAKDETECTOR_H_ */
