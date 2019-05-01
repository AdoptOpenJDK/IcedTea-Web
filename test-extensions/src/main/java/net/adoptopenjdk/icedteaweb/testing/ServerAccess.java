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
package net.adoptopenjdk.icedteaweb.testing;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import net.adoptopenjdk.icedteaweb.BasicFileUtils;
import net.adoptopenjdk.icedteaweb.OutputUtils;
import net.adoptopenjdk.icedteaweb.testing.browsertesting.Browser;
import net.adoptopenjdk.icedteaweb.testing.browsertesting.BrowserFactory;
import net.adoptopenjdk.icedteaweb.testing.browsertesting.Browsers;

/**
 * This class provides access to virtual server and stuff around.
 * It can find unoccupied port, start server, provides its singleton instantiation, launch parallel instantiations,
 * read location of installed (tested javaws) see javaws.build.bin java property,
 * location of server www root on file system (see test.server.dir java property),
 * stubs for launching javaws and for locating resources and read resources.
 * <p>
 * It can also execute processes with timeout (@see PROCESS_TIMEOUT) (used during launching javaws)
 * Some protected apis are exported because public classes in this package are put to be tested by makefile.
 * <p>
 * There are included test cases which show some basic usages.
 */
public class ServerAccess {

    public static final long NANO_TIME_DELIMITER = 1000000L;
    /**
     * java property which value containing path to default (makefile by) directory with deployed resources
     */
    private static final String TEST_SERVER_DIR = "test.server.dir";
    /**
     * java property which value containing path to installed (makefile by) javaws binary
     */
    private static final String JAVAWS_BUILD_BIN = "javaws.build.bin";
    /**
     * property to set the different then default browser
     */
    public static final String DEFAULT_LOCALHOST_NAME = "localhost";
    public static final String DEFAULT_LOCALHOST_IP = "127.0.0.1";
    public static final String DEFAULT_LOCALHOST_PROTOCOL = "http";

    /**
     * server instance singleton
     */
    private static ServerLauncher server;
    /**
     * timeout to read 'remote' resources
     * This can be changed in runtime, but will affect all following tasks
     */
    private static final int READ_TIMEOUT = 1000;
    /**
     * timeout in ms to let process to finish, before assassin will kill it.
     * This can be changed in runtime, but will affect all following tasks
     */
    public static final long PROCESS_TIMEOUT = 10 * 1000;//ms
    /**
     * this flag is indicating whether output of executeProcess should be logged. By default true.
     */
    public static final boolean PROCESS_LOG = true;
    private static final boolean LOGS_REPRINT = false;

    private Browser currentBrowser;
    private static final String UNSET_BROWSER = "unset_browser";

    /**
     * main method of this class prints out random free port
     * or runs server
     * param "port" prints out the port
     * nothing or number will run server on random(or on number specified)
     * port in -Dtest.server.dir
     *
     * @param args params from commandline. recognized params are port and randomport
     * @throws java.lang.Exception if anything happens
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
        final ServerSocket findPortTestingSocket = new ServerSocket(0);
        final int port = findPortTestingSocket.getLocalPort();
        findPortTestingSocket.close();
        return port;
    }

    /**
     * we would like to have an singleton instance ASAP
     */
    public ServerAccess() {

        getInstance();


    }

    /**
     * @return cached instance. If none, then creates new
     */
    public static ServerLauncher getInstance() {
        if (server == null) {
            server = getIndependentInstance();
        }
        return server;
    }

    /**
     * @return new not cached iserver instance on random port,
     * useful for testing application loading from different url then base
     */
    private static ServerLauncher getIndependentInstance() {
        final String dir = (System.getProperty(TEST_SERVER_DIR));
        try {
            return getIndependentInstance(dir, findFreePort());
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    /**
     * @param port specific port on which this server is accepting requests
     * @return new not cached iserver instance on random port,
     * useful for testing application loading from different url then base
     */

    private static ServerLauncher getIndependentInstance(int port) {
        String dir = (System.getProperty(TEST_SERVER_DIR));
        return getIndependentInstance(dir, port);
    }



    public static ServerLauncher getIndependentInstance(final String dir, final int port) {


        if (dir == null || dir.trim().length() == 0 || !new File(dir).exists() || !new File(dir).isDirectory()) {
            throw new RuntimeException("test.server.dir property must be set to valid directory!");
        }
        try {
            final ServerLauncher lServerLuncher = new ServerLauncher(port, new File(dir));
            final Thread r = new Thread(lServerLuncher);
            r.setDaemon(true);
            r.start();
            return lServerLuncher;
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    /**
     * @return - value passed inside as javaws binary location. See JAVAWS_BUILD_BIN
     */
    public String getJavawsLocation() {
        return System.getProperty(JAVAWS_BUILD_BIN);
    }

    /**
     * @return - binary from where to lunch current browser
     */
    private String getBrowserLocation() {
        if (this.currentBrowser == null) return UNSET_BROWSER;
        return this.currentBrowser.getBin();
    }

    private List<String> getBrowserParams() {
        if (this.currentBrowser == null) {
            return null;
        }

        final List<String> l1 = this.currentBrowser.getCompatibilitySwitches();
        final List<String> l = new ArrayList<>();
        if (l1 != null) l.addAll(l1);
        return l;

    }

    public Browsers getCurrentBrowsers() {
        if (currentBrowser == null) return null;
        return currentBrowser.getID();
    }

    public Browser getCurrentBrowser() {
        return currentBrowser;
    }

    public void setCurrentBrowser(final Browsers currentBrowser) {
        this.currentBrowser = BrowserFactory.getFactory().getBrowser(currentBrowser);
        if (this.currentBrowser == null) {
            LoggingBottleneck.getDefaultLoggingBottleneck().setLoggedBrowser(UNSET_BROWSER);
        } else {
            LoggingBottleneck.getDefaultLoggingBottleneck().setLoggedBrowser(this.currentBrowser.getID().toString());
        }
    }

    /**
     * @return - value passed inside as javaws binary location as file. See JAVAWS_BUILD_BIN
     */
    public File getJavawsFile() {
        return new File(System.getProperty(JAVAWS_BUILD_BIN));
    }

    /**
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
     * @param resource relative path  pointing to server resource. If non singleton instance is running, new is created.
     * @return complete url for this resource on this server
     * @throws java.net.MalformedURLException if url for this resource can not be constructed
     */
    private URL getUrl(final String resource) throws MalformedURLException {
        if (server == null) {
            getInstance();
        }
        //if (!server.isRunning()) throw new RuntimeException("Server mysteriously died");
        return server.getUrl(resource);
    }


    /**
     * Return resource from cached server
     *
     * @param resource to be located on cached server
     * @return individual bytes of resource
     * @throws IOException if connection can't be established or resource does not exist
     */
    public ByteArrayOutputStream getResourceAsBytes(final String resource) throws IOException {
        return getResourceAsBytes(getUrl(resource));
    }

    /**
     * Return resource from cached server
     *
     * @param resource to be located on cached server
     * @return string constructed from  resource
     * @throws IOException if connection can't be established or resource does not exist
     */
    public String getResourceAsString(final String resource) throws IOException {
        return getResourceAsString(getUrl(resource));
    }

    /**
     * utility method which can read bytes of any stream
     *
     * @param is stream to be read
     * @return individual bytes of resource
     * @throws IOException if connection can't be established or resource does not exist
     */
    private static ByteArrayOutputStream getBytesFromStream(final InputStream is) throws IOException {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        final byte[] data = new byte[16384];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer;
    }

    /**
     * utility method which can read bytes of resource from any url
     *
     * @param u full url to read from
     * @return individual bytes of resource
     * @throws IOException if connection can't be established or resource does not exist
     */
    private static ByteArrayOutputStream getResourceAsBytes(final URL u) throws IOException {
        URLConnection connection = null;
        try {
            connection = u.openConnection();
            if (connection instanceof HttpURLConnection) {
                ((HttpURLConnection) connection).setRequestMethod("GET");
            }
            connection.setDoOutput(true);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.connect();
            return getBytesFromStream(connection.getInputStream());
        } finally {
            if (connection != null && connection instanceof HttpURLConnection) {
                ((HttpURLConnection) connection).disconnect();
            }
        }

    }

    /**
     * utility method which can read string of resource from any url
     *
     * @param u full url to read from
     * @return resource as string
     * @throws IOException if connection can't be established or resource does
     *                     not exist
     */
    public static String getResourceAsString(final URL u) throws IOException {
        URLConnection connection = null;
        try {
            connection = u.openConnection();
            if (connection instanceof HttpURLConnection) {
                ((HttpURLConnection) connection).setRequestMethod("GET");
            }
            connection.setDoOutput(true);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.connect();

            try (final InputStream is = connection.getInputStream()) {
                return BasicFileUtils.toString(is);
            }
        } finally {
            if (connection instanceof HttpURLConnection) {
                ((HttpURLConnection) connection).disconnect();
            }
        }
    }

    /**
     * helping dummy  method to save String as file in UTF-8 encoding.
     *
     * @param content which will be saved as it is saved in this String
     * @param f       file to be saved. No warnings provided
     * @throws IOException
     */
    public static void saveFile(final String content, final File f) throws IOException {
        BasicFileUtils.saveFile(content, f);
    }


    /**
     * @param resource relative resource to be opened in browser for current server instance.
     * @return result of browser run
     */
    public ProcessResult executeBrowser(final String resource) throws Exception {
        return executeBrowser(getBrowserParams(), resource);
    }

    /**
     * @param resource relative resource to be opened in browser for current server instance.
     * @param stdoutl  listener for stdout
     * @param stderrl  listener for stderr
     * @return result of browser run
     */
    public ProcessResult executeBrowser(final String resource, final ContentReaderListener stdoutl, final ContentReaderListener stderrl) throws Exception {
        return executeBrowser(getBrowserParams(), resource, stdoutl, stderrl);
    }


    private ProcessResult executeBrowser(final List<String> otherargs, final String resource) throws Exception {
        return executeBrowser(otherargs, getUrlUponThisInstance(resource));
    }

    private ProcessResult executeBrowser(final List<String> otherargs, final URL url) throws Exception {
        final ProcessWrapper rpw = new ProcessWrapper(getBrowserLocation(), otherargs, url);
        rpw.setReactingProcess(getCurrentBrowser());//current browser may be null, but it does not metter
        return rpw.execute();
    }

    private ProcessResult executeBrowser(final List<String> otherargs, final String resource, final ContentReaderListener stdoutl, final ContentReaderListener stderrl) throws Exception {
        final ProcessWrapper rpw = new ProcessWrapper(getBrowserLocation(), otherargs, getUrlUponThisInstance(resource), stdoutl, stderrl, null);
        rpw.setReactingProcess(getCurrentBrowser()); //current browser may be null, but it does not matter
        return rpw.execute();
    }

    /**
     * Create resource on http, on 'localhost' on port on which this cached instance is running
     *
     * @param resource
     * @return
     * @throws MalformedURLException
     */
    public URL getUrlUponThisInstance(final String resource) throws MalformedURLException {
        getInstance();
        return getUrlUponInstance(server, resource);
    }

    /**
     * Create resource on http, on 'localhost' on port on which this instance is running
     *
     * @param instance of server to return the resource
     * @param resource relative path to resource to be loaded from specified instance
     * @return the absolute url
     * @throws MalformedURLException
     */
    private static URL getUrlUponInstance(final ServerLauncher instance, final String resource) throws MalformedURLException {
        return instance.getUrl(resource);
    }


    /**
     * Proceed message s to logging with request to reprint to System.err
     *
     * @param s
     */
    public static void logErrorReprint(final String s) {
        log(s, false, true);
    }

    /**
     * Proceed message s to logging with request to reprint to System.out
     *
     * @param s
     */
    public static void logOutputReprint(final String s) {
        log(s, true, false);
    }

    /**
     * Proceed message s to logging without request to reprint
     *
     * @param s
     */
    public static void logNoReprint(final String s) {
        log(s, false, false);
    }

    static void log(final String message, final boolean printToOut, final boolean printToErr) {
        String idded;
        final StackTraceElement ste = getTestMethod();
        final String fullId = LoggingBottleneck.getDefaultLoggingBottleneck().modifyMethodWithForBrowser(ste.getMethodName(), ste.getClassName());
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
        LoggingBottleneck.getDefaultLoggingBottleneck().logIntoPlaintextLog(idded, printToOut, printToErr);
        LoggingBottleneck.getDefaultLoggingBottleneck().addToXmlLog(message, printToOut, printToErr, ste);
    }

    public static void logException(final Throwable t) {
        logException(t, true);
    }

    public static void logException(final Throwable t, final boolean print) {
        log(OutputUtils.exceptionToString(t), false, print);
    }

    private static StackTraceElement getTestMethod() {
        return getTestMethod(Thread.currentThread().getStackTrace());
    }

    private static StackTraceElement getTestMethod(final StackTraceElement[] stack) {
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
            //probably it is necessary to get out of net.sourceforge.jnlp.
            //package where are right now all test-extensions
            //for now keeping exactly the three classes helping you  access the log
            try {
                final Class<?> clazz = Class.forName(stack[i].getClassName());
                String path = null;
                try {
                    path = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
                } catch (final NullPointerException ex) {
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
            } catch (final ClassNotFoundException ex) {
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
            if (stack[i].getClassName().contains("$")) {
                continue;
            }
            if (!baseClass.equals(stack[i].getClassName())) {
                break;
            }
            result = stack[i];
        }

        return result;
    }

    public static ProcessResult executeProcess(final List<String> args) throws Exception {
        return new ProcessWrapper(args, null, null, null).execute();
    }

}
