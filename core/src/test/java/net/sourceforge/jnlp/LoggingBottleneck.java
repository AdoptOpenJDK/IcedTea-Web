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

package net.sourceforge.jnlp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
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
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;

public class LoggingBottleneck {

    /**
     * default singleton
     */
    private static LoggingBottleneck loggingBottleneck;
    private static final File DEFAULT_LOG_FILE = new File("ServerAccess-logs.xml");
    private static final File DEFAULT_STDERR_FILE = new File("stderr.log");
    private static final File DEFAULT_STDOUT_FILE = new File("stdout.log");
    private static final File DEFAULT_STDLOGS_FILE = new File("all.log");
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
     * map of classes, each have map of methods, each have errorlist, outLIst, and allList (allist contains also not std or err messages)
     * class.testMethod.logs
     */
    final Map<String, Map<String, TestsLogs>> processLogs = Collections.synchronizedMap(new HashMap<String, Map<String, TestsLogs>>(1000));
    private boolean added = false;

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

   synchronized void writeXmlLog() throws FileNotFoundException, IOException {
        writeXmlLog(DEFAULT_LOG_FILE);
    }

    synchronized void writeXmlLog(File f) throws FileNotFoundException, IOException {
        writeXmlLog(f, Collections.unmodifiableMap(processLogs));
    }
        
    synchronized static void writeXmlLog(File f, Map<String, Map<String, TestsLogs>> processLogs) throws FileNotFoundException, IOException {
        Writer w = new OutputStreamWriter(new FileOutputStream(f));
        Set<Entry<String, Map<String, TestsLogs>>> classes = processLogs.entrySet();
        w.write("<" + LOGS_ELEMENT + ">");
        for (Entry<String, Map<String, TestsLogs>> classLog : classes) {
            String className = classLog.getKey();
            w.write("<" + CLASSLOG_ELEMENT + " " + CLASSNAME_ATTRIBUTE + "=\"" + className + "\">");
            Set<Entry<String, TestsLogs>> testsLogs = classLog.getValue().entrySet();
            for (Entry<String, TestsLogs> testLog : testsLogs) {
                String testName = testLog.getKey();
                String testLogs = testLog.getValue().toString();
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

    synchronized  void addToXmlLog(String message, boolean printToOut, boolean printToErr, StackTraceElement ste) {
        Map<String, TestsLogs> classLog = processLogs.get(ste.getClassName());
        if (classLog == null) {
            classLog = new HashMap<String, TestsLogs>(50);
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
            Runtime.getRuntime().addShutdownHook(new Thread() {

                @Override
                public void run() {
                    try {
                        LoggingBottleneck.getDefaultLoggingBottleneck().writeXmlLog();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
            added = true;
        }
        methodLog.add(printToErr, printToOut, message);
    }

   synchronized public String modifyMethodWithForBrowser(String methodBrowseredName, String className) {
        try {
            Class<?> clazz = Class.forName(className);
            /*
             * By using this isAssignable to ensure corect class before invocation,
             * then we lost possibility to track manualy set browsers, but it is correct,
             * as method description is set only when annotation is used
             */
            if (clazz != null && BrowserTest.class.isAssignableFrom(clazz)) {
                Method testMethod = clazz.getMethod(methodBrowseredName);
                if (testMethod != null) {
                    TestInBrowsers tib = testMethod.getAnnotation(TestInBrowsers.class);
                    if (tib != null) {
                        methodBrowseredName = methodBrowseredName + " - " + loggedBrowser;
                    }
                }
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return methodBrowseredName;
    }

   synchronized public void setLoggedBrowser(String loggedBrowser) {
        this.loggedBrowser = loggedBrowser;
    }

  synchronized  public void logIntoPlaintextLog(String message, boolean printToOut, boolean printToErr) {
        try {
            if (printToOut) {
                LoggingBottleneck.getDefaultLoggingBottleneck().stdout(message);
            }
            if (printToErr) {
                LoggingBottleneck.getDefaultLoggingBottleneck().stderr(message);
            }
            LoggingBottleneck.getDefaultLoggingBottleneck().stdeall(message);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void stdout(String idded) throws IOException {
        DEFAULT_STDOUT_WRITER.write(idded);
        DEFAULT_STDOUT_WRITER.newLine();
        DEFAULT_STDOUT_WRITER.flush();
    }

    private void stderr(String idded) throws IOException {
        DEFAULT_STDERR_WRITER.write(idded);
        DEFAULT_STDERR_WRITER.newLine();
        DEFAULT_STDERR_WRITER.flush();
    }

    private void stdeall(String idded) throws IOException {
        DEFAULT_STDLOGS_WRITER.write(idded);
        DEFAULT_STDLOGS_WRITER.newLine();
        DEFAULT_STDLOGS_WRITER.flush();
    }
    
   synchronized public  static String clearChars(String ss) {
        StringBuilder s = new StringBuilder(ss);
        for (int i = 0; i < s.length(); i++) {
            char q = s.charAt(i);
            if (q == '\n') {
                continue;
            }
            if (q == '\t') {
                continue;
            }
            int iq = (int) q;
            if ((iq <= 31 || iq > 65533)||(iq >= 64976 && iq <= 65007)) {
                s.setCharAt(i, 'I');
                s.insert(i + 1, "NVALID_CHAR_" + iq);
                i--;
            }
        }
        return s.toString();
    }
}
