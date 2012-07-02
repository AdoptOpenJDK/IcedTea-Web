/* BrowserTestRunner.java
Copyright (C) 2012 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 2.

IcedTea is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to
the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
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
exception statement from your version.
 */

package net.sourceforge.jnlp.browsertesting;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import net.sourceforge.jnlp.ServerAccess;
import org.junit.Ignore;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

public class BrowserTestRunner extends BlockJUnit4ClassRunner {

    public BrowserTestRunner(java.lang.Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        Method mm = method.getMethod();
        TestInBrowsers tib = mm.getAnnotation(TestInBrowsers.class);
        injectBrowserCatched(method, Browsers.none);
        boolean browserIgnoration = false;
        if (tib != null) {
            try {
                List<Browsers> testableBrowsers = BrowserFactory.getFactory().getBrowsers(tib);
                String mbr = System.getProperty("modified.browsers.run");
                if (mbr != null) {
                    if (mbr.equalsIgnoreCase("all")) {
                        if (!isBrowsersNoneSet(tib)) {
                            testableBrowsers = BrowserFactory.getFactory().getBrowsers(new Browsers[]{Browsers.all});
                        }
                    } else if (mbr.equalsIgnoreCase("one")) {
                        //this complication here is for case like
                        // namely enumerated concrete browsers, so we want to pick up
                        // random one from those already enumerated
                        if (isBrowsersNoneSet(tib)) {
                            testableBrowsers = Arrays.asList(new Browsers[]{testableBrowsers.get(new Random().nextInt(testableBrowsers.size()))});
                        }
                    } else if (mbr.equalsIgnoreCase("ignore")) {
                        testableBrowsers = BrowserFactory.getFactory().getBrowsers(new Browsers[]{Browsers.none});
                        browserIgnoration = true;
                    } else {
                        ServerAccess.logErrorReprint("unrecognized value of modified.browsers.run - " + mbr);
                    }
                }
                for (Browsers browser : testableBrowsers) {
                    try {
                        injcetBrowser(method, browser);
                        runChildX(method, notifier, browser, browserIgnoration);
                    } catch (Exception ex) {
                        //throw new RuntimeException("unabled to inject browser", ex);
                        ServerAccess.logException(ex, true);
                    }
                }
            } finally {
                injectBrowserCatched(method, Browsers.none);
            }
        } else {
            runChildX(method, notifier, null, false);
        }
    }

    private boolean isBrowsersNoneSet(TestInBrowsers tib) {
        if (tib.testIn().length == 1 && tib.testIn()[0] == Browsers.none) {
            return true;
        }
        return false;
    }

    private void injectBrowserCatched(FrameworkMethod method, Browsers browser) {
        try {
            injcetBrowser(method, browser);
        } catch (Exception ex) {
            //throw new RuntimeException("unabled to inject browser", ex);
            ServerAccess.logException(ex, true);
        }
    }

    private void injcetBrowser(FrameworkMethod method, Browsers browser) throws IllegalAccessException, SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        Method ff = method.getMethod().getDeclaringClass().getMethod("setBrowser", Browsers.class);
        ff.invoke(null, browser);
    }

    protected void runChildX(final FrameworkMethod method, RunNotifier notifier, Browsers browser, boolean browserIgnoration) {
        Description description = describeChild(method, browser);
        if (method.getAnnotation(Ignore.class) != null) {
            notifier.fireTestIgnored(description);
        } else {
            try {
                runLeaf(methodBlock(method), description, notifier, browserIgnoration);
//                ServerAccess.logOutputReprint("trying leaf");
//                Method m = this.getClass().getMethod("runLeaf", Statement.class, Description.class, RunNotifier.class);
//                m.setAccessible(true);
//                m.invoke(this, methodBlock(method), description, notifier);
//                ServerAccess.logOutputReprint("leaf invoked");
            } catch (Exception ex) {
                //throw new RuntimeException("unabled to lunch test on leaf", ex);
                ServerAccess.logException(ex, true);
            }
        }
    }

    /**
     * Runs a {@link Statement} that represents a leaf (aka atomic) test.
     */
    protected final void runLeaf(Statement statement, Description description,
            RunNotifier notifier, boolean ignore) {
        EachTestNotifier eachNotifier = new EachTestNotifier(notifier, description);
        eachNotifier.fireTestStarted();
          if (ignore) {
                eachNotifier.fireTestIgnored();
                return;
            }
        try {
          statement.evaluate();
        } catch (AssumptionViolatedException e) {
            eachNotifier.addFailedAssumption(e);
        } catch (Throwable e) {
            eachNotifier.addFailure(e);
        } finally {
            eachNotifier.fireTestFinished();
        }
    }

    protected Description describeChild(FrameworkMethod method, Browsers browser) {
        if (browser == null) {
            return super.describeChild(method);
        } else {
            try {
                return Description.createTestDescription(getTestClass().getJavaClass(),
                        testName(method) + " - " + browser.toString(), method.getAnnotations());
            } catch (Exception ex) {
                ServerAccess.logException(ex, true);
                return super.describeChild(method);
            }
        }
    }
}
