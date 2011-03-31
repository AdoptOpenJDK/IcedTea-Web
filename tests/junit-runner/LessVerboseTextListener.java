/*
 * Copyright 2011 Red Hat, Inc.
 *
 * This file is made available under the terms of the Common Public License
 * v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
import java.io.PrintStream;

import org.junit.internal.JUnitSystem;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class LessVerboseTextListener extends RunListener {

    private PrintStream writer;
    private boolean testFailed = false;

    public LessVerboseTextListener(JUnitSystem system) {
        writer= system.out();
    }

    @Override
    public void testStarted(Description description) throws Exception {
        testFailed = false;
    }

    @Override
    public void testFailure(Failure failure) {
        testFailed = true;
        writer.println("FAILED: " + failure.getTestHeader() + " " + failure.getMessage());
    }

    @Override
    public void testFinished(org.junit.runner.Description description) throws Exception {
        if (!testFailed) {
            writer.println("Passed: " + description.getClassName() + "." + description.getMethodName());
        }
    }

    @Override
    public void testRunFinished(Result result) throws Exception {
        int passed = result.getRunCount() - result.getFailureCount() - result.getIgnoreCount();
        int failed = result.getFailureCount();
        int ignored = result.getIgnoreCount();
        writer.println("Test results: passed: " + passed + "; failed: " + failed + "; ignored: " + ignored);
    }

}
