/*
 * Copyright 2011 Red Hat, Inc.
 *
 * This file is made available under the terms of the Common Public License
 * v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
import java.io.PrintStream;
import java.lang.reflect.Method;
import net.sourceforge.jnlp.annotations.KnownToFail;

import org.junit.internal.JUnitSystem;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class LessVerboseTextListener extends RunListener {

    private PrintStream writer;
    private boolean testFailed = false;
    private int  totalK2F=0;
    private int  failedK2F=0;
    private int  passedK2F=0;
    private int  ignoredK2F=0;

    public LessVerboseTextListener(JUnitSystem system) {
        writer= system.out();
    }

    @Override
    public void testStarted(Description description) throws Exception {
        testFailed = false;
    }

    @Override
    public void testIgnored(Description description) throws Exception {
        writer.println("Ignored: " + description.getClassName() + "." + description.getMethodName());
        printK2F(writer, null, description);
    }


    @Override
    public void testFailure(Failure failure) {
        testFailed = true;
        writer.println("FAILED: " + failure.getTestHeader() + " " + failure.getMessage());
        printK2F(writer,true,failure.getDescription());
    }

    @Override
    public void testFinished(org.junit.runner.Description description) throws Exception {
        if (!testFailed) {
            writer.println("Passed: " + description.getClassName() + "." + description.getMethodName());
            printK2F(writer,false,description);
        }
    }

    @Override
    public void testRunFinished(Result result) throws Exception {
        int passed = result.getRunCount() - result.getFailureCount() - result.getIgnoreCount();
        int failed = result.getFailureCount();
        int ignored = result.getIgnoreCount();
        writer.println("Total tests run: "+result.getRunCount()+"; From  those : " + totalK2F + " known to fail");
        writer.println("Test known to fail: passed: " + passedK2F + "; failed: " + failedK2F + "; ignored: " + ignoredK2F);
        writer.println("Test results: passed: " + passed + "; failed: " + failed + "; ignored: " + ignored);

    }

    private void printK2F(PrintStream writer, Boolean failed, Description description) {
        try {
            KnownToFail k2f = getK2F(description);
            if (k2f != null) {
                totalK2F++;
                if (failed != null) {
                    if (failed) {
                        failedK2F++;
                    } else {
                        passedK2F++;
                    }
                } else {
                    ignoredK2F++;
                }
                if (failed != null && !failed) {
                    writer.println(" - WARNING This test is known to fail, but have passed!");
                } else {
                    writer.println(" - This test is known to fail");
                }
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static  KnownToFail getK2F(Description description) {
        try {
            Class q = description.getTestClass();
            if (q != null) {
                String qs = description.getMethodName();
                if (qs.contains(" - ")) {
                    qs = qs.replaceAll(" - .*", "");
                }
                Method qm = q.getMethod(qs);
                if (qm != null) {
                    KnownToFail k2f = qm.getAnnotation(KnownToFail.class);
                    return k2f;

                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

}
