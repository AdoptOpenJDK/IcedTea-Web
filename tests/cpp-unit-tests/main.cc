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

// The test runner.
// Note that all modules compiled with the TEST macro will append tests to
// a global test list, that is accessible via Test::GetTestList().
#include <cstdio>

#include <UnitTest++.h>
#include <TestReporter.h>

#include <npfunctions.h>

#include "IcedTeaNPPlugin.h"
#include "browser_mock.h"
#include "MemoryLeakDetector.h"
#include "checked_allocations.h"

using namespace UnitTest;

static std::string full_testname(const TestDetails& details) {
    std::string suite = details.suiteName;
    if (suite == "DefaultSuite") {
        return details.testName;
    } else {
        return suite + "." + details.testName;
    }
}

// Important for testing purposes of eg leaks between tests
static void reset_global_state() {
    browsermock_clear_state();
    MemoryLeakDetector::reset_global_state();
}

class IcedteaWebUnitTestReporter: public TestReporter {
public:

    IcedteaWebUnitTestReporter() {
        // Unfortunately, there is no 'ReportSuccess'
        // We use 'did_finish_correctly' to track successes
        did_finish_correctly = false;
    }

    virtual void ReportTestStart(const TestDetails& test) {
        reset_global_state();
        pretest_allocs = cpp_unfreed_allocations();
        did_finish_correctly = true;
    }

    virtual void ReportFailure(const TestDetails& details,
            char const* failure) {
        std::string testname = full_testname(details);

        printf("FAILED: %s line %d (%s)\n", testname.c_str(),
                details.lineNumber, failure);

        did_finish_correctly = false;
    }

    virtual void ReportTestFinish(const TestDetails& details,
            float secondsElapsed) {

        reset_global_state();
        int posttest_allocs = cpp_unfreed_allocations();
        std::string testname = full_testname(details);

        if (browsermock_unfreed_allocations() > 0) {
            printf("*** WARNING: %s has a memory leak! %d more NPAPI allocations than frees!\n",
                    testname.c_str(), browsermock_unfreed_allocations());
        }
        if (posttest_allocs > pretest_allocs) {
            printf("*** WARNING: %s has a memory leak! %d more operator 'new' allocations than 'delete's!\n",
                    testname.c_str(), posttest_allocs - pretest_allocs);
        }

        if (did_finish_correctly) {
            printf("Passed: %s\n", testname.c_str());
        }
    }

    virtual void ReportSummary(int totalTestCount, int failedTestCount,
            int failureCount, float secondsElapsed) {

        if (failedTestCount > 0) {
            printf("TEST SUITE FAILURE: Not all tests have passed!\n");
        }

        printf("Total tests run: %d\n", totalTestCount);
        printf("Test results: passed: %d; failed: %d\n",
                totalTestCount - failedTestCount, failedTestCount);
    }

private:
    int pretest_allocs;
    bool did_finish_correctly;
};

static int run_icedtea_web_unit_tests() {
    IcedteaWebUnitTestReporter reporter;
    TestRunner runner(reporter);

    return runner.RunTestsIf(Test::GetTestList(), NULL /*All suites*/,
            True() /*All tests*/, 0 /*No time limit*/);
}

/* Spawns the Java-side of the plugin, create request processing threads,
 * and sets up a mocked 'browser' environment. */
static void initialize_plugin_components() {
    NPNetscapeFuncs mocked_browser_functions = browsermock_create_table();
    NPPluginFuncs unused_plugin_functions;
    memset(&unused_plugin_functions, 0, sizeof(NPPluginFuncs));
    unused_plugin_functions.size = sizeof(NPPluginFuncs);

    NP_Initialize (&mocked_browser_functions, &unused_plugin_functions);
    start_jvm_if_needed();
}

int main() {
    initialize_plugin_components();

    int exitcode = run_icedtea_web_unit_tests();

    return exitcode;
}
