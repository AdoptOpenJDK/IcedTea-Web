/* LoggingBottleneck.java
Copyright (C) 2011,2012 Red Hat, Inc.

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

package net.adoptopenjdk.icedteaweb.testing;

import net.adoptopenjdk.icedteaweb.testing.annotations.TestInBrowsers;
import net.adoptopenjdk.icedteaweb.testing.browsertesting.BrowserTest;
import net.adoptopenjdk.icedteaweb.testing.browsertesting.Browsers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class LoggingBottleneck {

    /**
     * default singleton
     */
    private static LoggingBottleneck loggingBottleneck;
    private static final File LOG_DIR = new File("target/logs");
    private static final File DEFAULT_LOG_FILE = new File(LOG_DIR, "ServerAccess-logs.xml");
    private static final File DEFAULT_STDERR_FILE = new File(LOG_DIR, "stderr.log");
    private static final File DEFAULT_STDOUT_FILE = new File(LOG_DIR, "stdout.log");
    private static final File DEFAULT_STDLOGS_FILE = new File(LOG_DIR, "all.log");
    private static final String LOGS_ELEMENT = "logs";
    private static final String CLASSLOG_ELEMENT = "classlog";
    private static final String CLASSNAME_ATTRIBUTE = "className";
    private static final String TESTLOG_ELEMENT = "testLog";
    private static final String TESTMETHOD_ATTRIBUTE = "testMethod";
    private static final String FULLID_ATTRIBUTE = "fullId";
    private BufferedWriter DEFAULT_STDERR_WRITER;
    private BufferedWriter DEFAULT_STDOUT_WRITER;
    private BufferedWriter DEFAULT_STDLOGS_WRITER;
    /**
     * This is static copy of name of id of currentBrowser for logging purposes
     */
    private String loggedBrowser = Browsers.none.toString();
    /**
     * map of classes, each have map of methods, each have errorlist, outLIst, and allList (allList contains also not std or err messages)
     * class.testMethod.logs
     */
    private final Map<String, Map<String, TestsLogs>> processLogs = Collections.synchronizedMap(new HashMap<String, Map<String, TestsLogs>>(1000));
    private boolean added = false;

    static {
        LOG_DIR.mkdirs();
    }

    synchronized public static LoggingBottleneck getDefaultLoggingBottleneck() {
        if (loggingBottleneck == null) {
            loggingBottleneck = new LoggingBottleneck();
        }
        return loggingBottleneck;

    }

    private LoggingBottleneck() {
        try {
            DEFAULT_STDOUT_WRITER = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DEFAULT_STDOUT_FILE)));
            DEFAULT_STDERR_WRITER = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DEFAULT_STDERR_FILE)));
            DEFAULT_STDLOGS_WRITER = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DEFAULT_STDLOGS_FILE)));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

   private synchronized void writeXmlLog() throws IOException {
       writeXmlLog(Collections.unmodifiableMap(processLogs));
   }

    private synchronized static void writeXmlLog(final Map<String, Map<String, TestsLogs>> processLogs) throws IOException {
        final Writer w = new OutputStreamWriter(new FileOutputStream(DEFAULT_LOG_FILE));
        final Set<Entry<String, Map<String, TestsLogs>>> classes = processLogs.entrySet();
        w.write("<" + LOGS_ELEMENT + ">");
        for (final Entry<String, Map<String, TestsLogs>> classLog : classes) {
            final String className = classLog.getKey();
            w.write("<" + CLASSLOG_ELEMENT + " " + CLASSNAME_ATTRIBUTE + "=\"" + className + "\">");
            final Set<Entry<String, TestsLogs>> testsLogs = classLog.getValue().entrySet();
            for (final Entry<String, TestsLogs> testLog : testsLogs) {
                final String testName = testLog.getKey();
                final String testLogs = testLog.getValue().toString();
                w.write("<" + TESTLOG_ELEMENT + " " + TESTMETHOD_ATTRIBUTE + "=\"" + testName + "\" " + FULLID_ATTRIBUTE + "=\"" + className + "." + testName + "\"  >");
                w.write(clearChars(testLogs));
                w.write("</" + TESTLOG_ELEMENT + ">");
            }
            w.write("</" + CLASSLOG_ELEMENT + ">");
        }
        w.write("</" + LOGS_ELEMENT + ">");
        w.flush();
        w.close();
    }

    synchronized  void addToXmlLog(final String message, final boolean printToOut, final boolean printToErr, final StackTraceElement ste) {
        Map<String, TestsLogs> classLog = processLogs.get(ste.getClassName());
        if (classLog == null) {
            classLog = new HashMap<>(50);
            processLogs.put(ste.getClassName(), classLog);
        }
        String methodBrowseredName = ste.getMethodName();
        methodBrowseredName = modifyMethodWithForBrowser(methodBrowseredName, ste.getClassName());
        TestsLogs methodLog = classLog.get(methodBrowseredName);
        if (methodLog == null) {
            methodLog = new TestsLogs();
            classLog.put(methodBrowseredName, methodLog);
        }
        if (!added) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    LoggingBottleneck.getDefaultLoggingBottleneck().writeXmlLog();
                } catch (final Exception ex) {
                    ex.printStackTrace();
                }
            }));
            added = true;
        }
        methodLog.add(printToErr, printToOut, message);
    }

   synchronized public String modifyMethodWithForBrowser(final String methodBrowseredName, final String className) {
        try {
            final Class<?> clazz = Class.forName(className);
            /*
             * By using this isAssignable to ensure correct class before invocation,
             * then we lost possibility to track manually set browsers, but it is correct,
             * as method description is set only when annotation is used
             */
            if (clazz != null && BrowserTest.class.isAssignableFrom(clazz)) {
                final Method testMethod = clazz.getMethod(methodBrowseredName);
                if (testMethod != null) {
                    final TestInBrowsers tib = testMethod.getAnnotation(TestInBrowsers.class);
                    if (tib != null) {
                        return methodBrowseredName + " - " + loggedBrowser;
                    }
                }
            }
        } catch (final Throwable ex) {
            ex.printStackTrace();
        }
        return methodBrowseredName;
    }

   synchronized public void setLoggedBrowser(final String loggedBrowser) {
        this.loggedBrowser = loggedBrowser;
    }

  synchronized  public void logIntoPlaintextLog(final String message, final boolean printToOut, final boolean printToErr) {
        try {
            if (printToOut) {
                LoggingBottleneck.getDefaultLoggingBottleneck().stdout(message);
            }
            if (printToErr) {
                LoggingBottleneck.getDefaultLoggingBottleneck().stderr(message);
            }
            LoggingBottleneck.getDefaultLoggingBottleneck().stdeall(message);
        } catch (final Throwable t) {
            t.printStackTrace();
        }
    }

    private void stdout(final String idded) throws IOException {
        DEFAULT_STDOUT_WRITER.write(idded);
        DEFAULT_STDOUT_WRITER.newLine();
        DEFAULT_STDOUT_WRITER.flush();
    }

    private void stderr(final String idded) throws IOException {
        DEFAULT_STDERR_WRITER.write(idded);
        DEFAULT_STDERR_WRITER.newLine();
        DEFAULT_STDERR_WRITER.flush();
    }

    private void stdeall(final String idded) throws IOException {
        DEFAULT_STDLOGS_WRITER.write(idded);
        DEFAULT_STDLOGS_WRITER.newLine();
        DEFAULT_STDLOGS_WRITER.flush();
    }
    
   private synchronized static String clearChars(final String ss) {
       final StringBuilder s = new StringBuilder(ss);
        for (int i = 0; i < s.length(); i++) {
            final char q = s.charAt(i);
            if (q == '\n') {
                continue;
            }
            if (q == '\t') {
                continue;
            }
            final int iq = (int) q;
            if ((iq <= 31 || iq > 65533)||(iq >= 64976 && iq <= 65007)) {
                s.setCharAt(i, 'I');
                s.insert(i + 1, "NVALID_CHAR_" + iq);
                i--;
            }
        }
        return s.toString();
    }
}
