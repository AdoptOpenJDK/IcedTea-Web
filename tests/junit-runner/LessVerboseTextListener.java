/*
 * Copyright 2011 Red Hat, Inc.
 *
 * This file is made available under the terms of the Common Public License
 * v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import net.sourceforge.jnlp.annotations.KnownToFail;
import net.sourceforge.jnlp.annotations.Remote;
import net.sourceforge.jnlp.browsertesting.Browsers;

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
        printRemote(writer, description);
    }


    @Override
    public void testFailure(Failure failure) {
        testFailed = true;
        writer.println("FAILED: " + failure.getTestHeader() + " " + failure.getMessage());
        printK2F(writer,true,failure.getDescription());
        printRemote(writer, failure.getDescription());
    }

    @Override
    public void testFinished(org.junit.runner.Description description) throws Exception {
        if (!testFailed) {
            writer.println("Passed: " + description.getClassName() + "." + description.getMethodName());
            printK2F(writer,false,description);
            printRemote(writer, description);
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
            boolean thisTestIsK2F = false;
            if (k2f != null){
                //determine if k2f in the current browser
                Browsers[] br = k2f.failsIn();
                if(0 == br.length){ //@KnownToFail with default optional parameter failsIn={}
                    thisTestIsK2F = true;
                }else{
                    for(Browsers b : br){
                        if(description.toString().contains(b.toString())){
                            thisTestIsK2F = true;
                        }
                    }
                }
            }

            if( thisTestIsK2F ){
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


    @SuppressWarnings("unchecked")
    public static <T extends Annotation> T getAnnotation(Class<?> q, String methodName, Class<T> a) {
        try {
            if (q != null) {
                T rem = q.getAnnotation(a);
                if (rem != null) {
                    return rem;
                }
                String qs = methodName;
                if (qs.contains(" - ")) {
                    qs = qs.replaceAll(" - .*", "");
                }
                Method qm = q.getMethod(qs);
                if (qm != null) {
                    rem = qm.getAnnotation(a);
                    return rem;

                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static KnownToFail getK2F(Description description) {
        return getAnnotation(description.getTestClass(), description.getMethodName(), KnownToFail.class);
    }

    public static Remote getRemote(Description description) {
        return getAnnotation(description.getTestClass(), description.getMethodName(), Remote.class);

    }

    private void printRemote(PrintStream writer, Description description) {
        try {
            Remote rem = getRemote(description);
            if (rem != null) {
                writer.println(" - This test is running remote content, note that failures may be caused by broken target application or connection");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
