/* ServerAccess.java
Copyright (C) 2011, 2012 Red Hat, Inc.

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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.browsertesting.Browser;
import net.sourceforge.jnlp.browsertesting.BrowserFactory;
import net.sourceforge.jnlp.browsertesting.Browsers;
import net.sourceforge.jnlp.closinglisteners.AutoErrorClosingListener;
import net.sourceforge.jnlp.closinglisteners.AutoOkClosingListener;
import net.sourceforge.jnlp.util.FileUtils;
import net.sourceforge.jnlp.util.logging.OutputController;
import org.junit.Assert;

/**
 *
 * This class provides access to virtual server and stuff around.
 * It can find unoccupied port, start server, provides its singleton instantiation, launch parallel instantiations,
 * read location of installed (tested javaws) see javaws.build.bin java property,
 * location of server www root on file system (see test.server.dir java property),
 * stubs for launching javaws and for locating resources and read resources.
 *
 * It can also execute processes with timeout (@see PROCESS_TIMEOUT) (used during launching javaws)
 * Some protected apis are exported because public classes in this package are put to be tested by makefile.
 *
 * There are included test cases which show some basic usages.
 *
 *
 */
public class ServerAccess {

    public static enum AutoClose {

        CLOSE_ON_EXCEPTION, CLOSE_ON_CORRECT_END, CLOSE_ON_BOTH
    }

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
    private static final String version = "5";
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
     * this flag is indicating whether output of executeProcess should be logged. By default true.
     */
    public static boolean PROCESS_LOG = true;
    public static boolean LOGS_REPRINT = false;

    private Browser currentBrowser;
    public static final String UNSET_BROWSER="unset_browser";

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
            if (args.length > 0 && args[0].equalsIgnoreCase("randomport")) {
                port = findFreePort();
            } else if (args.length > 0) {
                port = new Integer(args[0]);
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
    public static final  String VERBOSE_OPTION="-verbose";

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
       List<String> l= new ArrayList<String>();
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
           LoggingBottleneck.getDefaultLoggingBottleneck().setLoggedBrowser(UNSET_BROWSER);
        } else {
            LoggingBottleneck.getDefaultLoggingBottleneck().setLoggedBrowser(this.currentBrowser.getID().toString());
        }
    }

    public void setCurrentBrowser(Browser currentBrowser) {
        this.currentBrowser = currentBrowser;
        if (this.currentBrowser == null) {
            LoggingBottleneck.getDefaultLoggingBottleneck().setLoggedBrowser(UNSET_BROWSER);
        } else {
            LoggingBottleneck.getDefaultLoggingBottleneck().setLoggedBrowser(this.currentBrowser.getID().toString());
        }
    }



    /**
     *
     * @return - value passed inside as javaws binary location as file. See JAVAWS_BUILD_BIN
     */
    public File getJavawsFile() {
        return new File(System.getProperty(JAVAWS_BUILD_BIN));
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
    public static String getContentOfStream(InputStream is, String encoding) throws IOException {
        return FileUtils.getContentOfStream(is, encoding);
    }

    /**
     * utility method which can read from any stream as one long String
     *
     * @param input stream
     * @return stream as string
     * @throws IOException if connection can't be established or resource does not exist
     */
    public static String getContentOfStream(InputStream is) throws IOException {
        return FileUtils.getContentOfStream(is);
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
        FileUtils.saveFile(content, f);
    }
    public static void saveFile(String content, File f,String encoding) throws IOException {
        FileUtils.saveFile(content, f, encoding);
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
        return executeJavawsHeadless(null, resource,stdoutl,stderrl,null);
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
         return executeJavawsHeadless(otherargs, resource,null,null,null);
     }
    public ProcessResult executeJavawsHeadless(List<String> otherargs, String resource, String[] vars) throws Exception {
         return executeJavawsHeadless(otherargs, resource,null,null,vars);
     }

    public ProcessResult executeJavawsHeadless(List<String> otherargs, String resource,ContentReaderListener stdoutl,ContentReaderListener stderrl,String[] vars) throws Exception {
        if (otherargs == null) {
            otherargs = new ArrayList<String>(1);
        }
        List<String> headlesList = new ArrayList<String>(otherargs);
        headlesList.add(HEADLES_OPTION);
        return executeJavaws(headlesList, resource,stdoutl,stderrl,vars);
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

    public net.sourceforge.jnlp.ProcessResult executeBrowser(String string, AutoClose autoClose) throws Exception {
        ClosingListener errClosing = null;
        ClosingListener outClosing = null;
        if (autoClose == AutoClose.CLOSE_ON_BOTH || autoClose == AutoClose.CLOSE_ON_EXCEPTION){
            errClosing=new AutoErrorClosingListener();
        }
        if (autoClose == AutoClose.CLOSE_ON_BOTH || autoClose == AutoClose.CLOSE_ON_CORRECT_END){
            outClosing=new AutoOkClosingListener();
        }
        return executeBrowser(string, outClosing, errClosing);
    }
    

    public ProcessResult executeBrowser(String resource) throws Exception {
        return executeBrowser(getBrowserParams(), resource);
    }
    public ProcessResult executeBrowser(String resource,ContentReaderListener stdoutl,ContentReaderListener stderrl) throws Exception {
        return executeBrowser(getBrowserParams(), resource, stdoutl, stderrl);
    }

    public ProcessResult executeBrowser(String resource, List<ContentReaderListener> stdoutl, List<ContentReaderListener> stderrl) throws Exception {
        return executeBrowser(getBrowserParams(), resource, stdoutl, stderrl);
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
    public ProcessResult executeJavaws(List<String> otherargs, String resource,ContentReaderListener stdoutl,ContentReaderListener stderrl,String[] vars) throws Exception {
        return executeProcessUponURL(getJavawsLocation(), otherargs, getUrlUponThisInstance(resource),stdoutl,stderrl,vars);
    }

    public ProcessResult executeBrowser(List<String> otherargs, String resource) throws Exception {
        return executeBrowser(otherargs, getUrlUponThisInstance(resource));        
    }
    
     public ProcessResult executeBrowser(List<String> otherargs, URL url) throws Exception {
        ProcessWrapper rpw = new ProcessWrapper(getBrowserLocation(), otherargs, url);
        rpw.setReactingProcess(getCurrentBrowser());//current browser may be null, but it does not metter
        return rpw.execute();
    }

    public ProcessResult executeBrowser(List<String> otherargs, String resource, ContentReaderListener stdoutl, ContentReaderListener stderrl) throws Exception {
        ProcessWrapper rpw = new ProcessWrapper(getBrowserLocation(), otherargs, getUrlUponThisInstance(resource), stdoutl, stderrl, null);
        rpw.setReactingProcess(getCurrentBrowser());//current browser may be null, but it does not metter
        return rpw.execute();
    }

    public ProcessResult executeBrowser(List<String> otherargs,    String resource, List<ContentReaderListener> stdoutl, List<ContentReaderListener> stderrl) throws Exception {
        ProcessWrapper rpw = new ProcessWrapper(getBrowserLocation(), otherargs, getUrlUponThisInstance(resource), stdoutl, stderrl, null);
        rpw.setReactingProcess(getCurrentBrowser());// current browser may be null, but it does not matter
        return rpw.execute();
    }

    public ProcessResult executeBrowser(Browser b, List<String> otherargs, String resource) throws Exception {
        ProcessWrapper rpw = new ProcessWrapper(b.getBin(), otherargs, getUrlUponThisInstance(resource));
        rpw.setReactingProcess(b);
        return rpw.execute();
    }

    public ProcessResult executeBrowser(Browser b, List<String> otherargs, String resource, ContentReaderListener stdoutl, ContentReaderListener stderrl) throws Exception {
        ProcessWrapper rpw = new ProcessWrapper(b.getBin(), otherargs, getUrlUponThisInstance(resource), stdoutl, stderrl, null);
        rpw.setReactingProcess(b);
        return rpw.execute();
    }

    public ProcessResult executeBrowser(Browser b, List<String> otherargs, String resource, List<ContentReaderListener> stdoutl, List<ContentReaderListener> stderrl) throws Exception {
        ProcessWrapper rpw = new ProcessWrapper(b.getBin(), otherargs, getUrlUponThisInstance(resource), stdoutl, stderrl, null);
        rpw.setReactingProcess(b);
        return rpw.execute();
    }

    /**
     * Create resource on http, on 'localhost' on port on which this cached instance is running
     * @param resource
     * @return
     * @throws MalformedURLException
     */
    public URL getUrlUponThisInstance(String resource) throws MalformedURLException {
        getInstance();
        return getUrlUponInstance(server, resource);
    }

    /**
     * Ctreate resource on http, on 'localhost' on port on which this instance is running
     * @param resource
     * @return
     * @throws MalformedURLException
     */
    public static URL getUrlUponInstance(ServerLauncher instance,String resource) throws MalformedURLException {
       return instance.getUrl(resource);
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
        return new ProcessWrapper(toBeExecuted, otherargs, u).execute();
    }

    public static ProcessResult executeProcessUponURL(String toBeExecuted, List<String> otherargs, URL u, ContentReaderListener stdoutl, ContentReaderListener stderrl) throws Exception {
        return new ProcessWrapper(toBeExecuted, otherargs, u, stdoutl, stderrl, null).execute();
    }

    public static ProcessResult executeProcessUponURL(String toBeExecuted, List<String> otherargs, URL u, ContentReaderListener stdoutl, ContentReaderListener stderrl, String[] vars) throws Exception {
        return new ProcessWrapper(toBeExecuted, otherargs, u, stdoutl, stderrl, vars).execute();
    }

     public static ProcessResult executeProcess(final List<String> args) throws Exception {
         return  executeProcess(args, null);
     }
      public static ProcessResult executeProcess(final List<String> args,ContentReaderListener stdoutl,ContentReaderListener stderrl) throws Exception {
         return  executeProcess(args, null,stdoutl,stderrl);
     }
      public static ProcessResult executeProcess(final List<String> args,ContentReaderListener stdoutl,ContentReaderListener stderrl,String[] vars) throws Exception {
         return  executeProcess(args, null,stdoutl,stderrl,vars);
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

    static void log(String message, boolean printToOut, boolean printToErr) {
        String idded;
        StackTraceElement ste = getTestMethod();
        String fullId = ste.getClassName() + "." + ste.getMethodName();
        fullId = LoggingBottleneck.getDefaultLoggingBottleneck().modifyMethodWithForBrowser(ste.getMethodName(), ste.getClassName());
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
        LoggingBottleneck.getDefaultLoggingBottleneck().logIntoPlaintextLog(idded, printToOut,printToErr);
        LoggingBottleneck.getDefaultLoggingBottleneck().addToXmlLog(message,printToOut,printToErr,ste);
    }

    public static void logException(Throwable t){
        logException(t, true);
    }
    public static void logException(Throwable t, boolean print){
        log(OutputController.exceptionToString(t), false, print);
    }

    private static StackTraceElement getTestMethod() {
        return getTestMethod(Thread.currentThread().getStackTrace());
    }

    private static StackTraceElement getTestMethod(StackTraceElement[] stack) {
        //0 is always thread
        //1 is net.sourceforge.jnlp.*
        //we need to get out of all  of classes from this package or pick last of it
        StackTraceElement result = stack[1];
        String baseClass = stack[1].getClassName();
        int i = 2;
        for (; i < stack.length; i++) {
            result = stack[i];//at least moving up
            if (stack[i].getClassName().contains("$")) {
                continue;
            }
            //probablky it is necessary to get out of net.sourceforge.jnlp.
            //package where are right now all test-extensions
            //for now keeping exactly the three classes helping you  access the log
            try {
                Class<?> clazz = Class.forName(stack[i].getClassName());
                String path = null;
                try {
                    path = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
                } catch (NullPointerException ex) {
                    //silently ignoring and continuing with null path
                }
                if (path != null && path.contains("/tests.build/")) {
                    if (!path.contains("/test-extensions/")) {
                        break;
                    }
                } else {
                    //running from ide 
                    if (!stack[i].getClassName().startsWith("net.sourceforge.jnlp.")) {
                        break;
                    }
                }
            } catch (ClassNotFoundException ex) {
                ///should not happen, searching  only for already loaded class
                ex.printStackTrace();
            }
        }
        //if nothing left in stack then we have been in ServerAccess already
        //so the target method is the highest form it and better to return it
        //rather then die to ArrayOutOfBounds
        if (i >= stack.length) {
            return result;
        }
        //now we are out of test-extensions
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
        return executeProcess(args, dir, stdoutl, stderrl,null);

    }
    public static ProcessResult executeProcess(final List<String> args, File dir, ContentReaderListener stdoutl, ContentReaderListener stderrl, String[] vars) throws Exception {
        return new ProcessWrapper(args, dir, stdoutl, stderrl, vars).execute();
    }

}
