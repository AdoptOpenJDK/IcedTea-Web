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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * This class provides access to virtual server and stuff arround.
 * It can find unoccupied port, start server, provides its singleton instantion, lunch paralel instantions,
 * read location of installed (tested javaws) see javaws.build.bin java poperty,
 * location of server www root on filesystem (see test.server.dir java property),
 * stubs for lunching javaws and for locating resources and read resources.
 *
 * It can also execute processes with timeout (@see PROCESS_TIMEOUT) (used during lunching javaws)
 * Some protected apis are exported because public clases in this package are put to be tested by makefile.
 *
 * There are inclued tescases which show some basic usages.
 *
 *
 */
public class ServerAccess {

    public static final long NANO_TIME_DELIMITER=1000000l;
    /**
     * java property which value containig path to default (makefile by) directory with deployed resources
     */
    public static final String TEST_SERVER_DIR = "test.server.dir";
    /**
     * java property which value containig path to installed (makefile by) javaws binary
     */
    public static final String JAVAWS_BUILD_BIN = "javaws.build.bin";
    public static final String DEFAULT_LOCALHOST_NAME = "localhost";
    /**
     * server instance singeton
     */
    private static ServerLauncher server;
    /**
     * inner version if engine
     */
    private static final String version = "4";
    /**
     * timeout to read 'remote' resources
     * This can be changed in runtime, but will affect all following tasks
     */
    public static int READ_TIMEOUT = 1000;
    /**
     * timeout in ms to let process to finish, before assasin wil kill it.
     * This can be changed in runtime, but will affect all following tasks
     */
    public static long PROCESS_TIMEOUT = 10 * 1000;//ms
    /**
     * all terminated processes are stored here. As wee need to 'wait' to termination to be finished.
     */
    private static Set<Thread> terminated = new HashSet<Thread>();

    /**
     * main method of thos class prints out random free port
     */
    public static void main(String[] args) throws IOException {
        int i = findFreePort();
        System.out.println(i);

    }

    /**
     * utility method to find random free port
     *
     * @return - foud random free port
     * @throws IOException - if socket can't be openned or no free port exists
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
     * we would like to have an singleton instance asap
     */
    public ServerAccess() {

        getInstance();


    }

    /**
     *
     * @return cahed instance. If none, then creates new
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
     * usefull for testing application loading from different url then base
     */
    public static ServerLauncher getIndependentInstance() {
        String dir = (System.getProperty(TEST_SERVER_DIR));
        return getIndependentInstance(dir);
    }

    /**
     *
     * @return new not cached iserver instance on random port upon custom www root directory,
     * usefull for testing application loading from different url then base
     */
    public static ServerLauncher getIndependentInstance(String dir) {


        if (dir == null || dir.trim().length() == 0 || !new File(dir).exists() || !new File(dir).isDirectory()) {
            throw new RuntimeException("test.server.dir property must be set to valid directory!");
        }
        try {
            int port = findFreePort();
            ServerLauncher lServerLuncher = new ServerLauncher(port, new File(dir));
            new Thread(lServerLuncher).start();
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
     * @return - value passed inside as javaws binary location as file. See JAVAWS_BUILD_BIN
     */
    public File getJavawsFile() {
        return new File(System.getProperty(JAVAWS_BUILD_BIN));
    }

    @Test
    public void testsProcessResultFiltering() throws Exception {
        ProcessResult pn = new ProcessResult(null, null, null, true, 0);
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
        ProcessResult p2 = new ProcessResult(fakeOut2, fakeOut2, null, true, 0);
        Assert.assertEquals(p2.notFilteredStdout, fakeOut2);
        Assert.assertEquals(p2.stdout, filteredOut2);
        Assert.assertEquals(p2.stderr, fakeOut2);
        fakeOut2+="\n";
        p2 = new ProcessResult(fakeOut2, fakeOut2, null, true, 0);
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
        ProcessResult p = new ProcessResult(fakeOut, fakeOut, null, true, 0);
        Assert.assertEquals(p.notFilteredStdout, fakeOut);
        Assert.assertEquals(p.stdout, filteredOut);
        Assert.assertEquals(p.stderr, fakeOut);
        fakeOut+="\n";
        filteredOut+="\n";
        p = new ProcessResult(fakeOut, fakeOut, null, true, 0);
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

    }

    /**
     *
     * @return port on which is runing cached server. If non singleton instance is runnig, new is created.
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
     * @return directory upon which is runing cached server. If non singleton instance is runnig, new is created.
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
     * @return url pointing to cached server resource. If non singleton instance is runnig, new is created.
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
     * @return url pointing to cached server . If non singleton instance is runnig, new is created.
     */
    public URL getUrl() throws MalformedURLException {
        return getUrl("");

    }

    /**
     *
     * @return weather cached server is alive. If non singleton instance is runnig, new is created.
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
     * @throws IOException if connection cant be established or resource do not exists
     */
    public ByteArrayOutputStream getResourceAsBytes(String resource) throws IOException {
        return getResourceAsBytes(getUrl(resource));
    }

    /**
     * Return resource from cached server
     * 
     * @param resource to be located on cached server
     * @return string constructed from  resource
     * @throws IOException if connection cant be established or resource do not exists
     */
    public String getResourceAsString(String resource) throws IOException {
        return getResourceAsString(getUrl(resource));
    }

    /**
     * utility method which can read bytes of any stream
     * 
     * @param input stream to be read
     * @return individual bytes of resource
     * @throws IOException if connection cant be established or resource do not exists
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
     * @throws IOException if connection cant be established or resource do not exists
     */
    public static String getContentOfStream(InputStream is) throws IOException {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
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
     * utility method which can read bytes of resource from any url
     * 
     * @param resource to be located on any url
     * @return individual bytes of resource
     * @throws IOException if connection cant be established or resource do not exists
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
     * @param resource to be located onany url
     * @return resource as string
     * @throws IOException if connection cant be established or resource do not exists
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
        Writer output = new BufferedWriter(new FileWriter(f));
        output.write(content);
        output.flush();
        output.close();
    }

    /**
     * wrapping method to executeProcess (eg: javaws -headless http://localhost:port/resource)
     * will execute default javaws (@see JAVAWS_BUILD_BIN) upon default url upon cached server (@see SERVER_NAME @see getPort(), @see getInstance())
     * with parameter -headles (no gui, no asking)
     * @param resource  name of resource
     * @return result what left after running this process
     * @throws Exception
     */
    public ProcessResult executeJavawsHeadless(String resource) throws Exception {
        return executeJavawsHeadless(null, resource);
    }

    /**
     * wrapping method to executeProcess (eg: javaws arg arg -headless http://localhost:port/resource)
     * will execute default javaws (@see JAVAWS_BUILD_BIN) upon default url upon cached server (@see SERVER_NAME @see getPort(), @see getInstance())
     * with parameter -headles (no gui, no asking)
     * @param resource  name of resource
     * @param otherargs other arguments to be added to hedales one
     * @return result what left after running this process
     * @throws Exception
     */
    public ProcessResult executeJavawsHeadless(List<String> otherargs, String resource) throws Exception {
        if (otherargs == null) {
            otherargs = new ArrayList<String>(1);
        }
        List<String> headlesList = new ArrayList<String>(otherargs);
        headlesList.add(HEADLES_OPTION);
        return executeJavaws(headlesList, resource);
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

    /**
     *  wrapping method to executeProcess (eg: javaws arg arg http://localhost:port/resource)
     * will execute default javaws (@see JAVAWS_BUILD_BIN) upon default url upon cached server (@see SERVER_NAME @see getPort(), @see getInstance()))
     * @param resource  name of resource
     * @param otherargs other arguments to be added
     * @return result what left after running this process
     * @throws Exception
     */
    public ProcessResult executeJavaws(List<String> otherargs, String resource) throws Exception {
        return executeProcessUponURL(getJavawsLocation(), otherargs, new URL("http", server.getServerName(), getPort(), resource));
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
        Assert.assertNotNull(u);
        Assert.assertNotNull(toBeExecuted);
        Assert.assertTrue(toBeExecuted.trim().length() > 1);
        if (otherargs == null) {
            otherargs = new ArrayList<String>(1);
        }
        List<String> urledArgs = new ArrayList<String>(otherargs);
        urledArgs.add(0, toBeExecuted);
        urledArgs.add(u.toString());
        return executeProcess(urledArgs);
    }

     public static ProcessResult executeProcess(final List<String> args) throws Exception {
         return  executeProcess(args, null);
     }
    /**
     * utility method to lunch process, get its stdou/stderr, its return value and to kill it if runing to long (@see PROCESS_TIMEOUT)
     *
     *
     * Small bacground:
     * This method creates thread inside which exec will be executed. Then creates assasin thread with given timeout to kill the previously created thread if necessary.
     * Starts assasin thread, starts process thread. Wait untill process is running, then starts content readers.
     * Closes input of process.
     * Wait until process is running (no metter if it teminate itself (correctly or badly), or is terminated by its assasin.
     * Construct result from readed stdout, stderr, process return value, assasin sucessfulity
     *
     * @param args binary with args to be executed
     * @param dir optional, directory where this process will run
     * @return what left from process - proces sitself, its stdout, stderr and return value and weather it was terminated by assasin.
     * @throws Exception
     */
    public static ProcessResult executeProcess(final List<String> args,File dir) throws Exception {

        ThreadedProcess t = new ThreadedProcess(args,dir);
        ProcessAssasin pa = new ProcessAssasin(t, PROCESS_TIMEOUT);
        pa.start();
        t.start();
        while (t.getP() == null) {
            Thread.sleep(100);
        }
        ContentReader crs = new ContentReader(t.getP().getInputStream());
        ContentReader cre = new ContentReader(t.getP().getErrorStream());

        OutputStream out = t.getP().getOutputStream();
        if (out != null) {
            out.close();
        }

        new Thread(crs).start();
        new Thread(cre).start();
        while (t.isRunning()) {
            Thread.sleep(100);
        }
        pa.setCanRun(false);
        // System.out.println(t.getP().exitValue()); when process is killed, this throws exception

        return new ProcessResult(crs.getContent(), cre.getContent(), t.getP(), pa.wasTerminated(), t.getExitCode());
    }

    /**
     *
     * wrapper arround Runtime.getRuntime().exec(...) which ensures taht process is run inside its own, by us controlled, thread.
     * Proces sbuilder caused som einexpected and wired behaviour:/
     */
    private static class ThreadedProcess extends Thread {

        Process p = null;
        List<String> args;
        Integer exitCode;
        Boolean running;
        File dir;

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
                exitCode = p.waitFor();
                Thread.sleep(500);//this is giving to fastly done proecesses's e/o readers time to read all. I would like to know better solution :-/
            } catch (Exception ex) {
                if (ex instanceof InterruptedException) {
                    //add to the set of terminated threadedproceses
                    terminated.add(this);
                } else {
                    throw new RuntimeException(ex);
                }
            } finally {
                running = false;
            }
        }
    }

    /**
     * wrapper arround tiny http server to separate lunch configgurations and servers.
     * to allow terminations and stuff arround.
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
         */
        private class TinyHttpdImpl extends Thread {

            Socket c;
            private final File dir;
            private final int port;
            private boolean canRun = true;

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
                                String p = t.nextToken();
                                System.err.println("Getting: "+p);
                                p=URLDecoder.decode(p, "UTF-8");
                                System.err.println("Serving: "+p);
                                p = (".".concat(((p.endsWith("/")) ? p.concat(
                                        "index.html") : p))).replace('/', File.separatorChar);
                                File pp = new File(dir, p);
                                int l = (int) pp.length();
                                byte[] b = new byte[l];
                                FileInputStream f = new FileInputStream(pp);
                                f.read(b);
                                o.writeBytes("HTTP/1.0 200 OK\nConten"
                                        + "t-Length:" + l + "\n\n");
                                o.write(b, 0, l);
                            }
                        }
                    } catch (Exception e) {
                        o.writeBytes("HTTP/1.0 404 ERROR\n\n\n");
                        e.printStackTrace();
                    }
                    o.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * class which timeout any ThreadedProcess. This killing of 'theread with process' replaced not working process.destroy().
     */
    private static class ProcessAssasin extends Thread {

        long timeout;
        private final ThreadedProcess p;
        //false == is disabled:(
        private boolean canRun = true;
        private boolean wasTerminated = false;

        /**
         *
         * @param p
         * @param timeout - time to die in milis
         */
        public ProcessAssasin(ThreadedProcess p, long timeout) {
            this.p = (p);
            this.timeout = timeout;


        }

        public void setCanRun(boolean canRun) {
            this.canRun = canRun;
            System.err.println("Stopping assasin for" + p.toString() + " " + p.getP().toString() + " " + p.getCommandLine() + ": ");
            System.err.flush();
        }

        public boolean isCanRun() {
            return canRun;
        }

        public boolean wasTerminated() {
            return wasTerminated;
        }

        @Override
        public void run() {

            long startTime = System.nanoTime() / NANO_TIME_DELIMITER;
            while (canRun) {
                try {

                    long time = System.nanoTime() / NANO_TIME_DELIMITER;
                    //System.out.println(time - startTime);
                    //System.out.println((time - startTime) > timeout);
                    if ((time - startTime) > timeout) {
                        System.err.println("Timeouted " + p.toString() + " " + p.getP().toString() + " .. killing " + p.getCommandLine() + ": ");
                        System.err.flush();
                        wasTerminated = true;
                        p.interrupt();
                        while (!terminated.contains(p)) {
                            Thread.sleep(100);
                        }
                        System.err.println("Timeouted " + p.toString() + " " + p.getP().toString() + " .. killed " + p.getCommandLine());
                        System.err.flush();
                        break;


                    }
                    Thread.sleep(100);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            System.err.println("assasin for" + p.toString() + " " + p.getP().toString() + " .. done " + p.getCommandLine() + "  termination " + wasTerminated);
            System.err.flush();
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

        public ProcessResult(String stdout, String stderr, Process process, boolean wasTerminated, Integer r) {
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
        }
    }

    /**
     * Class to read content of stdou/err of process, and to cooperate with its running/terminated/finished statuses.
     */
    private static class ContentReader implements Runnable {

        StringBuilder sb = new StringBuilder();
        private final InputStream is;
        private boolean done;

        public String getContent() {
            return sb.toString();
        }

        public ContentReader(InputStream is) throws IOException {
            this.is = is;
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
                System.out.println("Stream copier: throwing InterruptedException");
                //throw new InterruptedException();
            }
        }

        @Override
        public void run() {
            try {
                Reader br = new InputStreamReader(is, "UTF-8");

                while (true) {
                    int s = br.read();
                    if (s < 0) {
                        break;
                    }

                    sb.append(((char) s));

                }
                //do not want to bother output with terminations
            } catch (Exception ex) {
                ex.printStackTrace();
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
