/* ServerAccess.java
Copyright (C) 2011 Red Hat, Inc.

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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.Browser;
import net.sourceforge.jnlp.browsertesting.BrowserFactory;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * This class provides access to virtual server and stuff around.
 * It can find unoccupied port, start server, provides its singleton instantiation, lunch parallel instantiations,
 * read location of installed (tested javaws) see javaws.build.bin java property,
 * location of server www root on file system (see test.server.dir java property),
 * stubs for lunching javaws and for locating resources and read resources.
 *
 * It can also execute processes with timeout (@see PROCESS_TIMEOUT) (used during lunching javaws)
 * Some protected apis are exported because public classes in this package are put to be tested by makefile.
 *
 * There are included test cases which show some basic usages.
 *
 *
 */
public class ServerAccess {

    public static final long NANO_TIME_DELIMITER=1000000l;
    /**
     * java property which value containing path to default (makefile by) directory with deployed resources
     */
    public static final String TEST_SERVER_DIR = "test.server.dir";
    /**
     * java property which value containing path to installed (makefile by) javaws binary
     */
    public static final String JAVAWS_BUILD_BIN = "javaws.build.bin";
    /** property to set the different then default browser
     */
    public static final String USED_BROWSERS = "used.browsers";
    public static final String DEFAULT_LOCALHOST_NAME = "localhost";
    /**
     * server instance singleton
     */
    private static ServerLauncher server;
    /**
     * inner version of engine
     */
    private static final String version = "4";
    /**
     * timeout to read 'remote' resources
     * This can be changed in runtime, but will affect all following tasks
     */
    public static int READ_TIMEOUT = 1000;
    /**
     * timeout in ms to let process to finish, before assassin will kill it.
     * This can be changed in runtime, but will affect all following tasks
     */
    public static long PROCESS_TIMEOUT = 20 * 1000;//ms
    /**
     * all terminated processes are stored here. As wee need to 'wait' to termination to be finished.
     */
    private static Set<Thread> terminated = new HashSet<Thread>();
    /**
     * this flag is indicating whether output of executeProcess should be logged. By default true.
     */
    public static boolean PROCESS_LOG = true;
    public static boolean LOGS_REPRINT = false;

    private Browser currentBrowser;
    /**
     * This is static copy of name of id of currentBrowser for logging purposes
     */
    private static String loggedBrowser=Browsers.none.toString();
    public static final String UNSET_BROWSER="unset_browser";

    /**
     * map of classes, each have map of methods, each have errorlist, outLIst, and allList (allist contains also not std or err messages)
     * class.testMethod.logs
     */
    private static final Map<String, Map<String, TestsLogs>> processLogs = new HashMap<String, Map<String, TestsLogs>>(100);
    private static final File DEFAULT_LOG_FILE = new File("ServerAccess-logs.xml");
    private static final File DEFAULT_STDERR_FILE = new File("stderr.log");
    private static final File DEFAULT_STDOUT_FILE = new File("stdout.log");
    private static final File DEFAULT_STDLOGS_FILE = new File("all.log");
    private static BufferedWriter DEFAULT_STDERR_WRITER;
    private static BufferedWriter DEFAULT_STDOUT_WRITER;
    private static BufferedWriter DEFAULT_STDLOGS_WRITER;

    static{
        try{
            DEFAULT_STDOUT_WRITER=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DEFAULT_STDOUT_FILE)));
            DEFAULT_STDERR_WRITER=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DEFAULT_STDERR_FILE)));
            DEFAULT_STDLOGS_WRITER=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DEFAULT_STDLOGS_FILE)));
        }catch(Throwable t){
            t.printStackTrace();
        }
    }
    private static final String LOGS_ELEMENT = "logs";
    private static final String CLASSLOG_ELEMENT = "classlog";
    private static final String CLASSNAME_ATTRIBUTE = "className";
    private static final String TESTLOG_ELEMENT = "testLog";
    private static final String TESTMETHOD_ATTRIBUTE = "testMethod";
    private static final String FULLID_ATTRIBUTE = "fullId";
    private static final String LOG_ELEMENT = "log";
    private static final String LOG_ID_ATTRIBUTE = "id";
    private static final String ITEM_ELEMENT = "item";
    private static final String ITEM_ID_ATTRIBUTE = "id";
    private static final String STAMP_ELEMENT = "stamp";
    private static final String TEXT_ELEMENT = "text";
    private static final String FULLTRACE_ELEMENT = "fulltrace";

    private static void writeXmlLog() throws FileNotFoundException, IOException {
        writeXmlLog(DEFAULT_LOG_FILE);
    }

    private static void writeXmlLog(File f) throws FileNotFoundException, IOException {
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
                w.write(testLogs);
                w.write("</" + TESTLOG_ELEMENT + ">");
            }
            w.write("</" + CLASSLOG_ELEMENT + ">");
        }
        w.write("</" + LOGS_ELEMENT + ">");
        w.flush();
        w.close();
    }

    private static void addToXmlLog(String message, boolean printToOut, boolean printToErr, StackTraceElement ste) {
        Map<String, TestsLogs> classLog = processLogs.get(ste.getClassName());
        if (classLog == null) {
            classLog = new HashMap<String, TestsLogs>(50);
            processLogs.put(ste.getClassName(), classLog);
        }
        String methodBrowseredName = ste.getMethodName();
        methodBrowseredName = modifyMethodWithForBrowser(methodBrowseredName,ste.getClassName());
        TestsLogs methodLog = classLog.get(methodBrowseredName);
        if (methodLog == null) {
            methodLog = new TestsLogs();
            classLog.put(methodBrowseredName, methodLog);
        }
        methodLog.add(printToErr, printToOut, message);
    }

     private static String modifyMethodWithForBrowser(String methodBrowseredName, String className) {
        try {
            Class clazz = Class.forName(className);
            /*
             * By using this isAssignable to ensure corect class before invocation,
             * then we lost possibility to track manualy set browsers, but it is correct,
             * as method description is set only when annotation is used
             */
             if (clazz != null && BrowserTest.class.isAssignableFrom(clazz)){
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

    private static class TestsLogs {

        public final List<LogItem> outs = new LinkedList<LogItem>();
        public final List<LogItem> errs = new LinkedList<LogItem>();
        public final List<LogItem> all = new LinkedList<LogItem>();
        private static boolean added = false;

        public synchronized void add(boolean err, boolean out, String text) {
            if (text == null) {
                text = "null";
            }
            LogItem li = new LogItem(text);
            if (out) {
                outs.add(li);
            }
            if (err) {
                errs.add(li);
            }
            all.add(li);
            if (!added) {
                Runtime.getRuntime().addShutdownHook(new Thread() {

                    @Override
                    public void run() {
                        try {
                            writeXmlLog();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                added = true;
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = listToStringBuilder(outs, "out");
            sb.append(listToStringBuilder(errs, "err"));
            sb.append(listToStringBuilder(all, "all"));
            return sb.toString();
        }

        private StringBuilder listToStringBuilder(List<LogItem> l, String id) {
            StringBuilder sb = new StringBuilder();
            sb.append("<" + LOG_ELEMENT + " " + LOG_ID_ATTRIBUTE + "=\"").append(id).append("\">\n");
            int i = 0;
            for (LogItem logItem : l) {
                i++;
                sb.append(logItem.toStringBuilder(i));
            }
            sb.append("</" + LOG_ELEMENT + ">\n");
            return sb;
        }
    };

    private static class LogItem {

        public final Date timeStamp = new Date();
        public final StackTraceElement[] fullTrace = Thread.currentThread().getStackTrace();
        public final String text;

        public LogItem(String text) {
            this.text = text;
        }

        public StringBuilder toStringBuilder(int id) {
            StringBuilder sb = new StringBuilder();
            sb.append("  <" + ITEM_ELEMENT + " " + ITEM_ID_ATTRIBUTE + "=\"").append(id).append("\">\n");
            sb.append("    <" + STAMP_ELEMENT + "><![CDATA[").append(timeStamp.toString()).append("]]></" + STAMP_ELEMENT + ">\n");
            sb.append("    <" + TEXT_ELEMENT + "><![CDATA[\n").append(text).append("\n]]></" + TEXT_ELEMENT + ">\n");
            sb.append("    <" + FULLTRACE_ELEMENT + "><![CDATA[\n");
            //five methods since call in log methods + getStacktrace method
            for (int i = 6; i < fullTrace.length; i++) {
                sb.append(fullTrace[i].toString()).append("\n");
            }
            sb.append("\n]]>    </" + FULLTRACE_ELEMENT + ">\n");
            sb.append("  </" + ITEM_ELEMENT + ">\n");
            return sb;
        }
    }
    /**
     * main method of this class prints out random free port
     * or runs server
     * param "port" prints out the port
     * nothing or number will run server on random(or on number specified)
     * port in -Dtest.server.dir
     */
    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equalsIgnoreCase("port")) {
            int i = findFreePort();
            System.out.println(i);
            System.exit(0);
        } else {
            int port = 44321;
            if (args.length > 0) {
                port=new Integer(args[0]);
            }
            getIndependentInstance(port);
            while (true) {
                Thread.sleep(1000);
            }

        }
    }

    /**
     * utility method to find random free port
     *
     * @return - found random free port
     * @throws IOException - if socket can't be opened or no free port exists
     */
    public static int findFreePort()
            throws IOException {
        ServerSocket findPortTestingSocket = new ServerSocket(0);
        int port = findPortTestingSocket.getLocalPort();
        findPortTestingSocket.close();
        return port;
    }
    public static final  String HEADLES_OPTION="-headless";

    /**
     * we would like to have an singleton instance ASAP
     */
    public ServerAccess() {

        getInstance();


    }

    /**
     *
     * @return cached instance. If none, then creates new
     */
    public static ServerLauncher getInstance() {
        if (server == null) {
            server = getIndependentInstance();
        }
        return server;
    }

    /**
     *
     * @return new not cached iserver instance on random port,
     * useful for testing application loading from different url then base
     */
    public static ServerLauncher getIndependentInstance() {
        return getIndependentInstance(true);
    }
    public static ServerLauncher getIndependentInstance(boolean daemon) {
        String dir = (System.getProperty(TEST_SERVER_DIR));
        try{
            return getIndependentInstance(dir, findFreePort(),daemon);
        }catch (Exception ex){
            throw  new RuntimeException(ex);
        }
    }


    /**
     *
     * @return new not cached iserver instance on random port,
     * useful for testing application loading from different url then base
     */
    
    public static ServerLauncher getIndependentInstance(int port) {
        return getIndependentInstance(port, true);
    }
    public static ServerLauncher getIndependentInstance(int port,boolean daemon) {
        String dir = (System.getProperty(TEST_SERVER_DIR));
        return getIndependentInstance(dir,port,daemon);
    }

    /**
     *
     * @return new not cached iserver instance on random port upon custom www root directory,
     * useful for testing application loading from different url then base
     */

    public static ServerLauncher getIndependentInstance(String dir, int port) {
        return getIndependentInstance(dir, port, true);
    }
    public static ServerLauncher getIndependentInstance(String dir, int port,boolean daemon) {


        if (dir == null || dir.trim().length() == 0 || !new File(dir).exists() || !new File(dir).isDirectory()) {
            throw new RuntimeException("test.server.dir property must be set to valid directory!");
        }
        try {
            ServerLauncher lServerLuncher = new ServerLauncher(port, new File(dir));
            Thread r=new Thread(lServerLuncher);
            r.setDaemon(daemon);
            r.start();
            return lServerLuncher;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    /**
     *
     * @return - value passed inside as javaws binary location. See JAVAWS_BUILD_BIN
     */
    public String getJavawsLocation() {
        return System.getProperty(JAVAWS_BUILD_BIN);
    }

      /**
     *
     * @return - bianry from where to lunch current browser
     */
    public String getBrowserLocation() {
       if (this.currentBrowser==null) return UNSET_BROWSER;
       return this.currentBrowser.getBin();
    }

    public List<String> getBrowserParams() {
       if (this.currentBrowser==null) return null;
       List<String> l1=this.currentBrowser.getComaptibilitySwitches();
       List<String> l2=this.currentBrowser.getDefaultSwitches();
       List<String> l= new ArrayList();
       if (l1!=null)l.addAll(l1);
       if (l2!=null)l.addAll(l2);
       return l;

    }

    public Browsers getCurrentBrowsers() {
        if (currentBrowser==null) return null;
        return currentBrowser.getID();
    }
    public Browser getCurrentBrowser() {
        return currentBrowser;
    }

    public void setCurrentBrowser(Browsers currentBrowser) {
        this.currentBrowser = BrowserFactory.getFactory().getBrowser(currentBrowser);
        if (this.currentBrowser == null) {
            loggedBrowser = UNSET_BROWSER;
        } else {
            loggedBrowser = this.currentBrowser.getID().toString();
        }
    }

    public void setCurrentBrowser(Browser currentBrowser) {
        this.currentBrowser = currentBrowser;
        if (this.currentBrowser == null) {
            loggedBrowser = UNSET_BROWSER;
        } else {
            loggedBrowser = this.currentBrowser.getID().toString();
        }
    }



    /**
     *
     * @return - value passed inside as javaws binary location as file. See JAVAWS_BUILD_BIN
     */
    public File getJavawsFile() {
        return new File(System.getProperty(JAVAWS_BUILD_BIN));
    }

    @Test
    public void testsProcessResultFiltering() throws Exception {
        ProcessResult pn = new ProcessResult(null, null, null, true, 0, null);
        Assert.assertNull(pn.notFilteredStdout);
        Assert.assertNull(pn.stdout);
        Assert.assertNull(pn.stderr);
        String fakeOut2 =
                "EMMA: processing instrumentation path ...\n"
                + "EMMA: package [net.sourceforge.filebrowser] contains classes [ArrayOfString] without full debug info\n"
                + "EMMA: instrumentation path processed in 1407 ms\n"
                + "test stage 1\n"
                + "test stage 2\n"
                + "EMMA: The intruder!\n"
                + "test stage 3\n"
                + "EMMA: [45 class(es) instrumented, 13 resource(s) copied]\n"
                + "EMMA: metadata merged into [icedtea-web/inc] {in 105 ms}\n"
                + "EMMA: processing instrumentation path ...";
        String filteredOut2 =
                "test stage 1\n"
                + "test stage 2\n"
                + "test stage 3\n";
        ProcessResult p2 = new ProcessResult(fakeOut2, fakeOut2, null, true, 0, null);
        Assert.assertEquals(p2.notFilteredStdout, fakeOut2);
        Assert.assertEquals(p2.stdout, filteredOut2);
        Assert.assertEquals(p2.stderr, fakeOut2);
        fakeOut2+="\n";
        p2 = new ProcessResult(fakeOut2, fakeOut2, null, true, 0, null);
        Assert.assertEquals(p2.notFilteredStdout, fakeOut2);
        Assert.assertEquals(p2.stdout, filteredOut2);
        Assert.assertEquals(p2.stderr, fakeOut2);
        String fakeOut =
                "test string\n"
                + "EMMA: processing instrumentation path ...\n"
                + "EMMA: package [net.sourceforge.filebrowser] contains classes [ArrayOfString] without full debug info\n"
                + "EMMA: instrumentation path processed in 1407 ms\n"
                + "test stage 1\n"
                + "test stage 2\n"
                + "test stage 3\n"
                + "EMMA: [45 class(es) instrumented, 13 resource(s) copied]\n"
                + "EMMA: metadata merged into [icedtea-web/inc] {in 105 ms}\n"
                + "EMMA: processing instrumentation path ...\n"
                + "test ends";
        String filteredOut =
                "test string\n"
                + "test stage 1\n"
                + "test stage 2\n"
                + "test stage 3\n"
                + "test ends";
        ProcessResult p = new ProcessResult(fakeOut, fakeOut, null, true, 0, null);
        Assert.assertEquals(p.notFilteredStdout, fakeOut);
        Assert.assertEquals(p.stdout, filteredOut);
        Assert.assertEquals(p.stderr, fakeOut);
        fakeOut+="\n";
        filteredOut+="\n";
        p = new ProcessResult(fakeOut, fakeOut, null, true, 0, null);
        Assert.assertEquals(p.notFilteredStdout, fakeOut);
        Assert.assertEquals(p.stdout, filteredOut);
        Assert.assertEquals(p.stderr, fakeOut);
    }

    @Test
    public void ensureJavaws() throws Exception {
        String javawsValue = getJavawsLocation();
        Assert.assertNotNull(javawsValue);
        Assert.assertTrue(javawsValue.trim().length() > 2);
        File javawsFile = getJavawsFile();
        Assert.assertTrue(javawsFile.exists());
        Assert.assertFalse(javawsFile.isDirectory());
    }

    @Test
    public void ensureServer() throws Exception {

        Assert.assertNotNull(server.getPort());
        Assert.assertNotNull(server.getDir());
        Assert.assertTrue(server.getPort() > 999);
        Assert.assertTrue(server.getDir().toString().trim().length() > 2);

        Assert.assertTrue(server.getDir().exists());
        Assert.assertTrue(server.getDir().isDirectory());

        File portFile = new File(server.getDir(), "server.port");
        File dirFile = new File(server.getDir(), "server.dir");

        saveFile(server.getDir().getAbsolutePath(), dirFile);
        saveFile(server.getPort().toString(), portFile);
        saveFile(server.getPort().toString(), portFile);

        Assert.assertTrue(portFile.exists());
        Assert.assertTrue(dirFile.exists());
        Assert.assertTrue(server.getDir().listFiles().length > 1);

        String portFileContent = getContentOfStream(new FileInputStream(portFile));
        String dirFileContent = getContentOfStream(new FileInputStream(dirFile));

        URL portUrl = new URL("http", "localhost", server.getPort(), "/server.port");
        URL dirUrl = new URL("http", "localhost", server.getPort(), "/server.dir");

        String portUrlContent = getContentOfStream(portUrl.openConnection().getInputStream());
        String dirUrlContent = getContentOfStream(dirUrl.openConnection().getInputStream());

        Assert.assertEquals(portUrlContent.trim(), portFileContent.trim());
        Assert.assertEquals(dirUrlContent.trim(), dirFileContent.trim());
        Assert.assertEquals(new File(dirUrlContent.trim()), server.getDir());
        Assert.assertEquals(new Integer(portUrlContent.trim()), server.getPort());

         URL fastUrl = new URL("http", "localhost", server.getPort(), "/simpletest1.jnlp");
         URL slowUrl = new URL("http", "localhost", server.getPort(), "/XslowXsimpletest1.jnlp");

        String fastUrlcontent = getContentOfStream(fastUrl.openConnection().getInputStream());
        String slowUrlContent = getContentOfStream(slowUrl.openConnection().getInputStream());
        Assert.assertEquals(fastUrlcontent, slowUrlContent);

    }

    /**
     *
     * @return port on which is running cached server. If non singleton instance is running, new is created.
     */
    public int getPort() {
        if (server == null) {
            getInstance();
        }
        //if (!server.isRunning()) throw new RuntimeException("Server mysteriously died");
        return server.getPort();

    }

    /**
     *
     * @return directory upon which is running cached server. If non singleton instance is running, new is created.
     */
    public File getDir() {
        if (server == null) {
            getInstance();
        }
        // if (!server.isRunning()) throw new RuntimeException("Server mysteriously died");
        return server.getDir();
    }

    /**
     *
     * @return url pointing to cached server resource. If non singleton instance is running, new is created.
     */
    public URL getUrl(String resource) throws MalformedURLException {
        if (server == null) {
            getInstance();
        }
        //if (!server.isRunning()) throw new RuntimeException("Server mysteriously died");
        return server.getUrl(resource);
    }

    /**
     *
     * @return url pointing to cached server . If non singleton instance is running, new is created.
     */
    public URL getUrl() throws MalformedURLException {
        return getUrl("");

    }

    /**
     *
     * @return whether cached server is alive. If non singleton instance is running, new is created.
     */
    public boolean isRunning() {
        if (server == null) {
            getInstance();
        }
        //if (!server.isRunning()) throw new RuntimeException("Server mysteriously died");
        return server.isRunning();

    }

    /**
     * Return resource from cached server
     * 
     * @param resource to be located on cached server
     * @return individual bytes of resource
     * @throws IOException if connection can't be established or resource does not exist
     */
    public ByteArrayOutputStream getResourceAsBytes(String resource) throws IOException {
        return getResourceAsBytes(getUrl(resource));
    }

    /**
     * Return resource from cached server
     * 
     * @param resource to be located on cached server
     * @return string constructed from  resource
     * @throws IOException if connection can't be established or resource does not exist
     */
    public String getResourceAsString(String resource) throws IOException {
        return getResourceAsString(getUrl(resource));
    }

    /**
     * utility method which can read bytes of any stream
     * 
     * @param input stream to be read
     * @return individual bytes of resource
     * @throws IOException if connection can't be established or resource does not exist
     */
    public static ByteArrayOutputStream getBytesFromStream(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer;
    }

    /**
     * utility method which can read from any stream as one long String
     * 
     * @param input stream
     * @return stream as string
     * @throws IOException if connection can't be established or resource does not exist
     */
    public static String getContentOfStream(InputStream is,String encoding) throws IOException {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, encoding));
            StringBuilder sb = new StringBuilder();
            while (true) {
                String s = br.readLine();
                if (s == null) {
                    break;
                }
                sb.append(s).append("\n");

            }
            return sb.toString();
        } finally {
            is.close();
        }

    }

    /**
     * utility method which can read from any stream as one long String
     *
     * @param input stream
     * @return stream as string
     * @throws IOException if connection can't be established or resource does not exist
     */
    public static String getContentOfStream(InputStream is) throws IOException {
        return getContentOfStream(is, "UTF-8");

    }

    /**
     * utility method which can read bytes of resource from any url
     * 
     * @param resource to be located on any url
     * @return individual bytes of resource
     * @throws IOException if connection can't be established or resource does not exist
     */
    public static ByteArrayOutputStream getResourceAsBytes(URL u) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) u.openConnection();
        connection = (HttpURLConnection) u.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoOutput(true);
        connection.setReadTimeout(READ_TIMEOUT);
        connection.connect();
        return getBytesFromStream(connection.getInputStream());

    }

    /**
     * utility method which can read string of resource from any url
     * 
     * @param resource to be located on any url
     * @return resource as string
     * @throws IOException if connection can't be established or resource does not exist
     */
    public static String getResourceAsString(URL u) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) u.openConnection();
        connection = (HttpURLConnection) u.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoOutput(true);
        connection.setReadTimeout(READ_TIMEOUT);
        connection.connect();
        return getContentOfStream(connection.getInputStream());
    }

    /**
     * helping dummy  method to save String as file
     * 
     * @param content
     * @param f
     * @throws IOException
     */
    public static void saveFile(String content, File f) throws IOException {
        saveFile(content, f, "utf-8");
    }
    public static void saveFile(String content, File f,String encoding) throws IOException {
        Writer output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f),encoding));
        output.write(content);
        output.flush();
        output.close();
    }

    /**
     * wrapping method to executeProcess (eg: javaws -headless http://localhost:port/resource)
     * will execute default javaws (@see JAVAWS_BUILD_BIN) upon default url upon cached server (@see SERVER_NAME @see getPort(), @see getInstance())
     * with parameter -headless (no gui, no asking)
     * @param resource  name of resource
     * @return result what left after running this process
     * @throws Exception
     */
    public ProcessResult executeJavawsHeadless(String resource) throws Exception {
        return executeJavawsHeadless(null, resource);
    }
    public ProcessResult executeJavawsHeadless(String resource,ContentReaderListener stdoutl,ContentReaderListener stderrl) throws Exception {
        return executeJavawsHeadless(null, resource,stdoutl,stderrl);
    }
     
    /**
     * wrapping method to executeProcess (eg: javaws arg arg -headless http://localhost:port/resource)
     * will execute default javaws (@see JAVAWS_BUILD_BIN) upon default url upon cached server (@see SERVER_NAME @see getPort(), @see getInstance())
     * with parameter -headless (no gui, no asking)
     * @param resource  name of resource
     * @param otherargs other arguments to be added to headless one
     * @return result what left after running this process
     * @throws Exception
     */
    public ProcessResult executeJavawsHeadless(List<String> otherargs, String resource) throws Exception {
         return executeJavawsHeadless(otherargs, resource,null,null);
     }
    public ProcessResult executeJavawsHeadless(List<String> otherargs, String resource,ContentReaderListener stdoutl,ContentReaderListener stderrl) throws Exception {
        if (otherargs == null) {
            otherargs = new ArrayList<String>(1);
        }
        List<String> headlesList = new ArrayList<String>(otherargs);
        headlesList.add(HEADLES_OPTION);
        return executeJavaws(headlesList, resource,stdoutl,stderrl);
    }


    /**
     * wrapping method to executeProcess (eg: javaws  http://localhost:port/resource)
     * will execute default javaws (@see JAVAWS_BUILD_BIN) upon default url upon cached server (@see SERVER_NAME @see getPort(), @see getInstance())
     * @param resource  name of resource
     * @return result what left after running this process
     * @throws Exception
     */
    public ProcessResult executeJavaws(String resource) throws Exception {
        return executeJavaws(null, resource);
    }
    public ProcessResult executeJavaws(String resource,ContentReaderListener stdoutl,ContentReaderListener stderrl) throws Exception {
        return executeJavaws(null, resource,stdoutl,stderrl);
    }
    public ProcessResult executeBrowser(String resource) throws Exception {
        return executeBrowser(getBrowserParams(), resource);
    }
    public ProcessResult executeBrowser(String resource,ContentReaderListener stdoutl,ContentReaderListener stderrl) throws Exception {
        return executeBrowser(getBrowserParams(), resource,stderrl,stdoutl);
    }

    /**
     *  wrapping method to executeProcess (eg: javaws arg arg http://localhost:port/resource)
     * will execute default javaws (@see JAVAWS_BUILD_BIN) upon default url upon cached server (@see SERVER_NAME @see getPort(), @see getInstance()))
     * @param resource  name of resource
     * @param otherargs other arguments to be added
     * @return result what left after running this process
     * @throws Exception
     */
    public ProcessResult executeJavaws(List<String> otherargs, String resource) throws Exception {
        return executeProcessUponURL(getJavawsLocation(), otherargs, getUrlUponThisInstance(resource));
    }
    public ProcessResult executeJavaws(List<String> otherargs, String resource,ContentReaderListener stdoutl,ContentReaderListener stderrl) throws Exception {
        return executeProcessUponURL(getJavawsLocation(), otherargs, getUrlUponThisInstance(resource),stdoutl,stderrl);
    }

    public ProcessResult executeBrowser(List<String> otherargs, String resource) throws Exception {
        return executeProcessUponURL(getBrowserLocation(), otherargs, getUrlUponThisInstance(resource));
    }
    public ProcessResult executeBrowser(List<String> otherargs, String resource,ContentReaderListener stdoutl,ContentReaderListener stderrl) throws Exception {
        return executeProcessUponURL(getBrowserLocation(), otherargs, getUrlUponThisInstance(resource),stdoutl,stderrl);
    }

     public ProcessResult executeBrowser(Browser b,List<String> otherargs, String resource) throws Exception {
        return executeProcessUponURL(b.getBin(), otherargs, getUrlUponThisInstance(resource));
    }
    public ProcessResult executeBrowser(Browser b,List<String> otherargs, String resource,ContentReaderListener stdoutl,ContentReaderListener stderrl) throws Exception {
        return executeProcessUponURL(b.getBin(), otherargs, getUrlUponThisInstance(resource),stdoutl,stderrl);
    }

    /**
     * Ctreate resource on http, on 'localhost' on port on which this instance is running
     * @param resource
     * @return
     * @throws MalformedURLException
     */
    public URL getUrlUponThisInstance(String resource) throws MalformedURLException {
        if (!resource.startsWith("/")) {
            resource = "/" + resource;
        }
        return new URL("http", server.getServerName(), getPort(), resource);
    }

    /**
     * wrapping method to executeProcess (eg: javaws arg arg arg url)
     * will execute default javaws (@see JAVAWS_BUILD_BIN) upon any server
     * @param u url of resource upon any server
     * @param javaws arguments
     * @return result what left after running this process
     * @throws Exception
     */
    public ProcessResult executeJavawsUponUrl(List<String> otherargs, URL u) throws Exception {
        return executeProcessUponURL(getJavawsLocation(), otherargs, u);
    }
    public ProcessResult executeJavawsUponUrl(List<String> otherargs, URL u,ContentReaderListener stdoutl,ContentReaderListener stderrl) throws Exception {
        return executeProcessUponURL(getJavawsLocation(), otherargs, u,stdoutl,stderrl);
    }

    /**
     * wrapping utility method to executeProcess (eg: any_binary arg arg arg url)
     *
     * will execute  any process upon  url upon any server
     * @param u url of resource upon any server
     * @param javaws arguments
     * @return result what left after running this process
     * @throws Exception
     */
    public static ProcessResult executeProcessUponURL(String toBeExecuted, List<String> otherargs, URL u) throws Exception {
        return executeProcessUponURL(toBeExecuted, otherargs, u,null,null);
    }

    public static ProcessResult executeProcessUponURL(String toBeExecuted, List<String> otherargs, URL u,ContentReaderListener stdoutl,ContentReaderListener stderrl) throws Exception {
        Assert.assertNotNull(u);
        Assert.assertNotNull(toBeExecuted);
        Assert.assertTrue(toBeExecuted.trim().length() > 1);
        if (otherargs == null) {
            otherargs = new ArrayList<String>(1);
        }
        List<String> urledArgs = new ArrayList<String>(otherargs);
        urledArgs.add(0, toBeExecuted);
        urledArgs.add(u.toString());
        return executeProcess(urledArgs, stdoutl, stderrl);
    }

     public static ProcessResult executeProcess(final List<String> args) throws Exception {
         return  executeProcess(args, null);
     }
      public static ProcessResult executeProcess(final List<String> args,ContentReaderListener stdoutl,ContentReaderListener stderrl) throws Exception {
         return  executeProcess(args, null,stdoutl,stderrl);
     }
    /**
     * utility method to lunch process, get its stdout/stderr, its return value and to kill it if running to long (@see PROCESS_TIMEOUT)
     *
     *
     * Small bacground:
     * This method creates thread inside which exec will be executed. Then creates assassin thread with given timeout to kill the previously created thread if necessary.
     * Starts assassin thread, starts process thread. Wait until process is running, then starts content readers.
     * Closes input of process.
     * Wait until process is running (no matter if it terminate itself (correctly or badly), or is terminated by its assassin.
     * Construct result from readed stdout, stderr, process return value, assassin successfully
     *
     * @param args binary with args to be executed
     * @param dir optional, directory where this process will run
     * @return what left from process - process itself, its stdout, stderr and return value and whether it was terminated by assassin.
     * @throws Exception
     */
    public static ProcessResult executeProcess(final List<String> args,File dir) throws Exception {
        return executeProcess(args, dir, null, null);
    }

    private static String createConnectionMessage(ThreadedProcess t) {
        return "Connecting " + t.getCommandLine();
    }

    /**
     * Proceed message s to logging with request to reprint to System.err
     * @param s
     */
    public static void logErrorReprint(String s) {
        log(s, false, true);
    }

    /**
     * Proceed message s to logging with request to reprint to System.out
     * @param s
     */
    public static void logOutputReprint(String s) {
        log(s, true, false);
    }

    /**
     * Proceed message s to logging withhout request to reprint
     * @param s
     */
    public static void logNoReprint(String s) {
        log(s, false, false);
    }

    private static void log(String message, boolean printToOut, boolean printToErr) {
        String idded;
        StackTraceElement ste = getTestMethod();
        String fullId = ste.getClassName() + "." + ste.getMethodName();
        fullId = modifyMethodWithForBrowser(ste.getMethodName(), ste.getClassName());
        if (message.contains("\n")) {
            idded = fullId + ": \n" + message + "\n" + fullId + " ---";
        } else {
            idded = fullId + ": " + message;

        }
        if (LOGS_REPRINT) {
            if (printToOut) {
                System.out.println(idded);
            }
            if (printToErr) {
                System.err.println(idded);
            }
        }
        try{
        if (printToOut) {
            DEFAULT_STDOUT_WRITER.write(idded);
            DEFAULT_STDOUT_WRITER.newLine();
        }
        if (printToErr) {
            DEFAULT_STDERR_WRITER.write(idded);
            DEFAULT_STDERR_WRITER.newLine();
        }
        DEFAULT_STDLOGS_WRITER.write(idded);
        DEFAULT_STDLOGS_WRITER.newLine();
        }catch (Throwable t){
            t.printStackTrace();
        }

        addToXmlLog(message,printToOut,printToErr,ste);
    }

    public static void logException(Throwable t){
        logException(t, true);
    }
    public static void logException(Throwable t, boolean print){
        try{
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        log(sw.toString(), false, print);
        pw.close();
        sw.close();
        }catch(Exception ex){
           throw new RuntimeException(ex);
        }
    }

    private static StackTraceElement getTestMethod() {
        return getTestMethod(Thread.currentThread().getStackTrace());
    }

    private static StackTraceElement getTestMethod(StackTraceElement[] stack) {
        //0 is always thread
        //1 is net.sourceforge.jnlp.ServerAccess
        StackTraceElement result = stack[1];
        String baseClass = stack[1].getClassName();
        int i = 2;
        for (; i < stack.length; i++) {
            result = stack[i];//at least moving up
            if(stack[i].getClassName().contains("$")){
                continue;
            }
            if (!baseClass.equals(stack[i].getClassName())) {
                break;
            }
        }
        //if nothing left in stack then we have been in ServerAccess already
        //so the target method is the highest form it and better to return it
        //rather then die to ArrayOutOfBounds
        if(i >= stack.length){
            return result;
        }
        //now we are out of net.sourceforge.jnlp.ServerAccess
        //method we need (the test)  is highest from following class
        baseClass = stack[i].getClassName();
        for (; i < stack.length; i++) {
            if(stack[i].getClassName().contains("$")){
                continue;
            }
            if (!baseClass.equals(stack[i].getClassName())) {
                break;
            }
            result = stack[i];
        }

        return result;
    }

    public static ProcessResult executeProcess(final List<String> args, File dir, ContentReaderListener stdoutl, ContentReaderListener stderrl) throws Exception {
        ThreadedProcess t = new ThreadedProcess(args, dir);
        if (PROCESS_LOG) {
            String connectionMesaage = createConnectionMessage(t);
            log(connectionMesaage, true, true);
        }
        ProcessAssasin pa = new ProcessAssasin(t, PROCESS_TIMEOUT);
        pa.start();
        t.start();
        while (t.getP() == null && t.deadlyException == null) {
            Thread.sleep(100);
        }
        if (t.deadlyException != null) {
            pa.setCanRun(false);
            return new ProcessResult("", "", null, true, Integer.MIN_VALUE, t.deadlyException);
        }
        ContentReader crs = new ContentReader(t.getP().getInputStream(),stdoutl);
        ContentReader cre = new ContentReader(t.getP().getErrorStream(),stderrl);

        OutputStream out = t.getP().getOutputStream();
        if (out != null) {
            out.close();
        }

        new Thread(crs).start();
        new Thread(cre).start();
        while (t.isRunning()) {
            Thread.sleep(100);
        }

        while (!t.isDestoyed()) {
            Thread.sleep(100);
        }
        pa.setCanRun(false);
        // ServerAccess.logOutputReprint(t.getP().exitValue()); when process is killed, this throws exception

        ProcessResult pr=new ProcessResult(crs.getContent(), cre.getContent(), t.getP(), pa.wasTerminated(), t.getExitCode(), null);
        if (PROCESS_LOG) {
            log(pr.stdout, true, false);
            log(pr.stderr, false, true);
        }
        return pr;
    }

    /**
     *
     * wrapper around Runtime.getRuntime().exec(...) which ensures that process is run inside its own, by us controlled, thread.
     * Process builder caused some unexpected and weird behavior :/
     */
    private static class ThreadedProcess extends Thread {

        Process p = null;
        List<String> args;
        Integer exitCode;
        Boolean running;
        File dir;
        Throwable deadlyException = null;
        /*
         * before removing this "useless" variable
         * check DeadLockTestTest.testDeadLockTestTerminated2
         */
        private boolean destoyed = false;

        public boolean isDestoyed() {
            return destoyed;
        }

        public void setDestoyed(boolean destoyed) {
            this.destoyed = destoyed;
        }

        public Boolean isRunning() {
            return running;
        }

        public Integer getExitCode() {
            return exitCode;
        }

        public ThreadedProcess(List<String> args) {
            this.args = args;
        }
         public ThreadedProcess(List<String> args,File dir) {
            this.args = args;
            this.dir=dir;
        }

        public String getCommandLine() {
            String commandLine = "unknown command";
            try {
                if (args != null && args.size() > 0) {
                    commandLine = "";
                    for (String string : args) {
                        commandLine = commandLine + " " + string;

                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                return commandLine;
            }
        }

        public Process getP() {
            return p;
        }

        @Override
        public void run() {
            try {
                running = true;
                Runtime r = Runtime.getRuntime();
                if (dir==null){
                    p = r.exec(args.toArray(new String[0]));
                }else{
                    p = r.exec(args.toArray(new String[0]),new String[0], dir);
                }
                try {
                    exitCode = p.waitFor();
                    Thread.sleep(500); //this is giving to fast done proecesses's e/o readers time to read all. I would like to know better solution :-/
                } finally {
                    destoyed = true;
                }
            } catch (Exception ex) {
                if (ex instanceof InterruptedException) {
                    //add to the set of terminated threaded processes
                    deadlyException = ex;
                    logException(deadlyException, false);
                    terminated.add(this);
                } else {
                    //happens when non-existing process is launched, is causing p null!
                    terminated.add(this);
                    deadlyException = ex;
                    logException(deadlyException, false);
                    throw new RuntimeException(ex);
                }
            } finally {
                running = false;
            }
        }
    }

    /**
     * wrapper around tiny http server to separate lunch configurations and servers.
     * to allow terminations and stuff around.
     */
    public static class ServerLauncher implements Runnable {

        /**
         * default url name part.
         * This can be changed in runtime, but will affect all following tasks upon those server
         */
        private String serverName = DEFAULT_LOCALHOST_NAME;
        private boolean running;
        private final Integer port;
        private final File dir;

        public String getServerName() {
            return serverName;
        }

        public void setServerName(String serverName) {
            this.serverName = serverName;
        }

        public ServerLauncher(Integer port, File dir) {
            this.port = port;
            this.dir = dir;
            System.err.println("port: " + port);
            System.err.println("dir: " + dir);
        }

        public boolean isRunning() {
            return running;
        }

        public Integer getPort() {
            return port;
        }

        public File getDir() {
            return dir;
        }

        public ServerLauncher(File dir) {
            this(8181, dir);
        }

        public ServerLauncher(Integer port) {

            this(port, new File(System.getProperty("user.dir")));
        }

        public ServerLauncher() {
            this(8181, new File(System.getProperty("user.dir")));

        }

        public void run() {
            running = true;
            try {
                ServerSocket s = new ServerSocket(
                        port);
                while (running) {
                    new TinyHttpdImpl(s.accept(), dir, port);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                running = false;
            }

        }

        public URL getUrl(String resource) throws MalformedURLException {
            return new URL("http", getServerName(), getPort(), resource);
        }

        public URL getUrl() throws MalformedURLException {
            return getUrl("");
        }

        /**
         * based on http://www.mcwalter.org/technology/java/httpd/tiny/index.html
         * Very small implementation of http return headers for our served resources
         *
         * When resource starts with XslowX prefix, then resouce (without XslowX)
         * is returned, but its delivery is delayed
         */
        private class TinyHttpdImpl extends Thread {

            Socket c;
            private final File dir;
            private final int port;
            private boolean canRun = true;
            private static final String XSX="/XslowX";

            public void setCanRun(boolean canRun) {
                this.canRun = canRun;
            }

            public TinyHttpdImpl(Socket s, File f, int port) {
                c = s;
                this.dir = f;
                this.port = port;
                start();
            }

            public int getPort() {
                return port;
            }

            @Override
            public void run() {
                try {
                    BufferedReader i = new BufferedReader(new InputStreamReader(c.getInputStream()));
                    DataOutputStream o = new DataOutputStream(c.getOutputStream());
                    try {
                        while (canRun) {
                            String s = i.readLine();
                            if (s.length() < 1) {
                                break;
                            }
                            if (s.startsWith("GET")) {
                                StringTokenizer t = new StringTokenizer(s, " ");
                                t.nextToken();
                                String op = t.nextToken();
                                String p = op;
                                if (p.startsWith(XSX))p=p.replace(XSX, "/");
                                logNoReprint("Getting: "+p);
                                p=URLDecoder.decode(p, "UTF-8");
                                logNoReprint("Serving: "+p);
                                p = (".".concat(((p.endsWith("/")) ? p.concat(
                                        "index.html") : p))).replace('/', File.separatorChar);
                                File pp = new File(dir, p);
                                int l = (int) pp.length();
                                byte[] b = new byte[l];
                                FileInputStream f = new FileInputStream(pp);
                                f.read(b);
                                String content = "";
                                String ct = "Content-Type: ";
                                if (p.toLowerCase().endsWith(".jnlp")) {
                                    content = ct + "application/x-java-jnlp-file\n";
                                } else if (p.toLowerCase().endsWith(".html")) {
                                    content = ct + "text/html\n";
                                } else if (p.toLowerCase().endsWith(".jar")) {
                                    content = ct + "application/x-jar\n";
                                }
                                o.writeBytes("HTTP/1.0 200 OK\nConten"
                                        + "t-Length:" + l + "\n" + content + "\n");
                                if (op.startsWith(XSX)){
                                    byte[][] bb=ServerAccess.splitArray(b,10);
                                    for (int j = 0; j < bb.length; j++) {
                                        Thread.sleep(2000);
                                        byte[] bs = bb[j];
                                        o.write(bs, 0, bs.length);
                                    }
                                }else{
                                o.write(b, 0, l);
                                }
                            }
                        }
                    }catch (SocketException e) {
                        logException(e, false);
                    } catch (Exception e) {
                        o.writeBytes("HTTP/1.0 404 ERROR\n\n\n");
                        logException(e, false);
                    }
                    o.close();
                } catch (Exception e) {
                    logException(e, false);
                }
            }
        }
    }


    /**
     * This function splits input array to severasl pieces
     * from byte[length] splitt to n pieces s is retrunrd byte[n][length/n], except
     * last piece which contains length%n
     *
     * @param input - array to be splitted
     * @param pieces - to how many pieces it should be broken
     * @return inidividual pices of original array, which concatet again givs original array
     */
  public static byte[][] splitArray(byte[] input, int pieces) {
        int rest = input.length;
        int rowLength = rest / pieces;
        if (rest % pieces > 0) rowLength++;
        if (pieces * rowLength >= rest + rowLength) pieces--;
        int i = 0, j = 0;
        byte[][] array = new byte[pieces][];
        array[0] = new byte[rowLength];
        for (byte b : input) {
            if (i >= rowLength) {
                i = 0;
                array[++j] = new byte[Math.min(rowLength, rest)];
            }
            array[j][i++] = b;
            rest--;
        }
        return array;
    }


     @Test
    public void splitArrayTest0() throws Exception {
         byte[] b={1,2,3,4,5,6,7,8,9,10,11,12,13,14};
         byte[][] bb=splitArray(b, 3);
        //printArrays(bb);
        byte[] b1={1,2,3,4,5};
        byte[] b2={6,7,8,9,10};
        byte[] b3={11,12,13,14};
        Assert.assertEquals(3,bb.length);
        Assert.assertArrayEquals(b1,bb[0]);
        Assert.assertArrayEquals(b2,bb[1]);
        Assert.assertArrayEquals(b3,bb[2]);
     }

     @Test
    public void splitArrayTest1() throws Exception {
         byte[] b={1,2,3,4,5,6,7,8,9,10,11,12,13};
         byte[][] bb=splitArray(b, 3);
        //printArrays(bb);
         byte[] b1={1,2,3,4,5};
        byte[] b2={6,7,8,9,10};
        byte[] b3={11,12,13};
        Assert.assertEquals(3,bb.length);
        Assert.assertArrayEquals(b1,bb[0]);
        Assert.assertArrayEquals(b2,bb[1]);
        Assert.assertArrayEquals(b3,bb[2]);
     }

      @Test
    public void splitArrayTest2() throws Exception {
         byte[] b={1,2,3,4,5,6,7,8,9,10,11,12};
         byte[][] bb=splitArray(b, 3);
        //printArrays(bb);
        byte[] b1={1,2,3,4};
        byte[] b2={5,6,7,8};
        byte[] b3={9,10,11,12};
        Assert.assertEquals(3,bb.length);
        Assert.assertArrayEquals(b1,bb[0]);
        Assert.assertArrayEquals(b2,bb[1]);
        Assert.assertArrayEquals(b3,bb[2]);
     }

    private void printArrays(byte[][] bb) {
        System.out.println("[][] l=" + bb.length);
        for (int i = 0; i < bb.length; i++) {
            byte[] bs = bb[i];
            System.out.println(i + ": l=" + bs.length);
            for (int j = 0; j < bs.length; j++) {
                byte c = bs[j];
                System.out.print(" " + j + ":" + c + " ");
            }
            System.out.println("");
        }
    }


    /**
     * class which timeout any ThreadedProcess. This killing of 'thread with process' replaced not working process.destroy().
     */
    private static class ProcessAssasin extends Thread {

        long timeout;
        private final ThreadedProcess p;
        //false == is disabled:(
        private boolean canRun = true;
        private boolean wasTerminated = false;
        /**
         * if this is true, then process is not destroyed after timeout, but just left to its own destiny.
         * Its stdout/err is no longer recorded, and it is leaking system resources until it dies by itself
         * The contorl is returned to main thread with all informations recorded  untill now.
         * You will be able to listen to std out from listeners still
         */
        private boolean skipInstedOfDesroy = false;

        /**
         *
         * @param p
         * @param timeout - time to die in milliseconds
         */
        public ProcessAssasin(ThreadedProcess p, long timeout) {
            this.p = (p);
            this.timeout = timeout;


        }

        public ProcessAssasin(ThreadedProcess p, long timeout, boolean skipInstedOfDesroy) {
            this.p = (p);
            this.timeout = timeout;
            this.skipInstedOfDesroy = skipInstedOfDesroy;


        }

        public void setCanRun(boolean canRun) {
            this.canRun = canRun;
            if (p != null) {
                if (p.getP() != null) {
                    logNoReprint("Stopping assassin for" + p.toString() + " " + p.getP().toString() + " " + p.getCommandLine() + ": ");
                } else {
                    logNoReprint("Stopping assassin for" + p.toString() + " " + p.getCommandLine() + ": ");
                }
            } else {
                logNoReprint("Stopping assassin for null job: ");
            }
        }

        public boolean isCanRun() {
            return canRun;
        }

        public boolean wasTerminated() {
            return wasTerminated;
        }

        public void setSkipInstedOfDesroy(boolean skipInstedOfDesroy) {
            this.skipInstedOfDesroy = skipInstedOfDesroy;
        }

        public boolean isSkipInstedOfDesroy() {
            return skipInstedOfDesroy;
        }

        @Override
        public void run() {

            long startTime = System.nanoTime() / NANO_TIME_DELIMITER;
            while (canRun) {
                try {

                    long time = System.nanoTime() / NANO_TIME_DELIMITER;
                    //ServerAccess.logOutputReprint(time - startTime);
                    //ServerAccess.logOutputReprint((time - startTime) > timeout);
                    if ((time - startTime) > timeout) {
                        try {
                            if (p != null) {
                                if (p.getP() != null) {
                                    logErrorReprint("Timed out " + p.toString() + " " + p.getP().toString() + " .. killing " + p.getCommandLine() + ": ");
                                } else {
                                    logErrorReprint("Timed out " + p.toString() + " " + "null  .. killing " + p.getCommandLine() + ": ");
                                }
                                wasTerminated = true;
                                p.interrupt();
                                while (!terminated.contains(p)) {
                                    Thread.sleep(100);
                                }
                                if (p.getP() != null) {
                                    try {
                                        if (!skipInstedOfDesroy) {
                                            p.getP().destroy();
                                        }
                                    } catch (Throwable ex) {
                                        if (p.deadlyException == null) {
                                            p.deadlyException = ex;
                                        }
                                        ex.printStackTrace();
                                    }
                                }
                                if (p.getP() != null) {
                                    logErrorReprint("Timed out " + p.toString() + " " + p.getP().toString() + " .. killed " + p.getCommandLine());
                                } else {
                                    logErrorReprint("Timed out " + p.toString() + " null  .. killed " + p.getCommandLine());
                                }
                            } else {
                                logErrorReprint("Timed out null job");
                            }
                            break;
                        } finally {
                            p.setDestoyed(true);
                        }


                    }
                    Thread.sleep(100);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            if (p != null) {
                if (p.getP() != null) {
                    logNoReprint("assassin for" + p.toString() + " " + p.getP().toString() + " .. done " + p.getCommandLine() + "  termination " + wasTerminated);
                } else {
                    logNoReprint("assassin for" + p.toString() + " null .. done " + p.getCommandLine() + "  termination " + wasTerminated);
                }
            } else {
                logNoReprint("assassin for non existing job  termination " + wasTerminated);
            }
        }
    }

    /**
     * artefacts what are left by finished process
     */
    public static class ProcessResult {

        public final String stdout;
        public final String notFilteredStdout;
        public final String stderr;
        public final Process process;
        public final Integer returnValue;
        public final boolean wasTerminated;
        /*
         * possible exception which caused Process not to be launched
         */
        public final Throwable deadlyException;

        public ProcessResult(String stdout, String stderr, Process process, boolean wasTerminated, Integer r, Throwable deadlyException) {
            this.notFilteredStdout = stdout;
            if (stdout == null) {
                this.stdout = null;
            } else {
                this.stdout = stdout.replaceAll("EMMA:.*\n?", "");
            }
            this.stderr = stderr;
            this.process = process;
            this.wasTerminated = wasTerminated;
            this.returnValue = r;
            this.deadlyException = deadlyException;
        }
    }

    /**
     * Class to read content of stdout/stderr of process, and to cooperate with its running/terminated/finished statuses.
     */
    private static class ContentReader implements Runnable {

        StringBuilder sb = new StringBuilder();
        private final InputStream is;
        private boolean done;
        ContentReaderListener listener;

        public String getContent() {
            return sb.toString();
        }

        public ContentReader(InputStream is) throws IOException {
            this.is = is;
        }
        public ContentReader(InputStream is,ContentReaderListener l) throws IOException {
            this.is = is;
            this.listener=l;
        }

        public void setListener(ContentReaderListener listener) {
            this.listener = listener;
        }

        public ContentReaderListener getListener() {
            return listener;
        }


        /**
         * Blocks until the copy is complete, or until the thread is interrupted
         */
        public synchronized void waitUntilDone() throws InterruptedException {
            boolean interrupted = false;

            // poll interrupted flag, while waiting for copy to complete
            while (!(interrupted = Thread.interrupted()) && !done) {
                wait(1000);
            }

            if (interrupted) {
                logNoReprint("Stream copier: throwing InterruptedException");
                //throw new InterruptedException();
            }
        }

        @Override
        public void run() {
            try {
                Reader br = new InputStreamReader(is, "UTF-8");
                StringBuilder line = new StringBuilder();
                while (true) {
                    int s = br.read();
                    if (s < 0) {
                        if (line.length() > 0 && listener != null) {
                            listener.lineReaded(line.toString());
                        }
                        break;
                    }
                    char ch = ((char) s);
                    sb.append(ch);
                    line.append(ch);
                    if (ch == '\n') {
                        if (listener != null) {
                            listener.lineReaded(line.toString());
                        }
                        line = new StringBuilder();
                    }
                    if (listener != null) {
                        listener.charReaded(ch);
                    }

                }
                //do not want to bother output with terminations
                //mostly compaling when assassin kill the process about StreamClosed
            } catch (Exception ex) {
                // logException(ex);
            } finally {
                try {
                    is.close();
                } catch (Exception ex) {
                    // ex.printStackTrace();
                } finally {
                    done = true;
                }
            }

        }
    }
}
