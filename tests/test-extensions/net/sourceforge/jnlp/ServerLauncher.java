/* ServerLauncher.java
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

import java.io.File;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * wrapper around tiny http server to separate lunch configurations and servers.
 * to allow terminations and stuff around.
 */
public class ServerLauncher implements Runnable, Authentication511Requester {

    public static enum ServerNaming {

        LOCALHOST, LOCALHOST_IP, HOSTNAME
    }

    /**
     * default url name part. This can be changed in runtime, but will affect
     * all following tasks upon those server
     */
    private String serverName = ServerAccess.DEFAULT_LOCALHOST_NAME;
    private final String protocol = ServerAccess.DEFAULT_LOCALHOST_PROTOCOL;
    private boolean running;
    private final Integer port;
    private final File dir;
    private ServerSocket serverSocket;
    private boolean supportingHeadRequest = true;
    private ServerNaming serverNaming = ServerNaming.LOCALHOST;

    public void setSupportingHeadRequest(boolean supportsHead) {
        this.supportingHeadRequest = supportsHead;
    }

    public boolean isSupportingHeadRequest() {
        return supportingHeadRequest;
    }

    public void setServerNaming(ServerNaming naming) {
        this.serverNaming = naming;
    }

    public String getServerName() {
        if (serverNaming == ServerNaming.HOSTNAME) {
            try {
                return InetAddress.getLocalHost().getHostName();
            } catch (Exception ex) {
                ServerAccess.logException(ex);
            }
        }
        if (serverNaming == ServerNaming.LOCALHOST_IP) {
            return ServerAccess.DEFAULT_LOCALHOST_IP;

        }
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

    /**
     * When redirect is set, the requests to this server will just redirect to
     * the underlying ServerLauncher
     */
    private ServerLauncher redirect = null;
    /**
     * one of: 301, 302,303, 307, 308,
     */
    private int redirectCode = 302;

    public void setRedirect(ServerLauncher redirect) {
        this.redirect = redirect;

    }

    public void setRedirectCode(int redirectPort) {
        this.redirectCode = redirectPort;
    }

    //resoource -> request -> number of requests on of this rsource on this server
    // eg   simpletest1.jnlp -> GET -> 3
    private Map<String, Map<String, Integer>> requestsCounter;

    public void setRequestsCounter(Map<String, Map<String, Integer>> requestsCounter) {
        this.requestsCounter = requestsCounter;
    }

    @Override
    public void run() {
        running = true;
        try {
            serverSocket = new ServerSocket(port);
            while (running) {
                TinyHttpdImpl server = new TinyHttpdImpl(serverSocket.accept(), dir, false);
                server.setRedirect(redirect);
                server.setRedirectCode(redirectCode);
                server.setRequestsCounter(requestsCounter);
                server.setSupportingHeadRequest(isSupportingHeadRequest());
                if (isNeedsAuthentication511()) {
                    server.setAuthenticator(this);
                }
                server.start();
            }
        } catch (Exception e) {
            ServerAccess.logException(e);
        } finally {
            running = false;
        }
    }

    private String sanitizeResource(String resource) {
        if (resource == null) {
            resource = "";
        }
        if (resource.trim().length() > 0 && !resource.startsWith("/")) {
            resource = "/" + resource;
        }
        return resource;
    }

    public URL getUrl(String resource) throws MalformedURLException {
        return new URL(protocol, getServerName(), getPort(), sanitizeResource(resource));
    }

    public URL getUrlLocalhost(String resource) throws MalformedURLException {
        return new URL(protocol, serverName, getPort(), sanitizeResource(resource));
    }

    public URL getUrlLocalhostIp(String resource) throws MalformedURLException {
        return new URL(protocol, ServerAccess.DEFAULT_LOCALHOST_IP, getPort(), sanitizeResource(resource));
    }

    public URL getUrlHostName(String resource) throws MalformedURLException, UnknownHostException {
        return new URL(protocol, InetAddress.getLocalHost().getHostName(), getPort(), sanitizeResource(resource));
    }

    public List<URL> getUrlAliases(String resource) throws MalformedURLException, UnknownHostException {
        List<URL> l = new ArrayList<>(3);
        l.add(getUrlLocalhost(resource));
        l.add(getUrlLocalhostIp(resource));
        l.add(getUrlHostName(resource));
        if (l.size() != ServerNaming.values().length) {
            throw new RuntimeException("Not all aliases returned! (returned " + l.size() + " expected " + ServerNaming.values().length + ")");
        }
        return l;
    }

    public URL getUrl() throws MalformedURLException {
        return getUrl("");
    }

    public void stop() {
        this.running = false;
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (Exception ex) {
                ServerAccess.logException(ex);
            }
        }
        System.err.println("stopped : " + port);
    }

    @Override
    public String toString() {
        try {
            if (redirect != null) {
                return getUrl() + " - " + super.toString() + "; redirecting via: " + redirectCode + " to " + redirect.toString();
            } else {
                return getUrl() + " - " + super.toString();
            }
        } catch (Exception ex) {
            ServerAccess.logException(ex);
        }
        return super.toString();
    }

    private boolean needsAuthentication511 = false;
    private boolean wasuthenticated511 = false;
    private boolean remberLastUrl = false;

    @Override
    public boolean isRememberOrigianlUrl() {
        return remberLastUrl;
    }

    @Override
    public void setRememberOrigianlUrl(boolean remberUrl) {
        remberLastUrl = remberUrl;
    }

    @Override
    public void setNeedsAuthentication511(boolean needsAuthentication511) {
        this.needsAuthentication511 = needsAuthentication511;
    }

    @Override
    public void setWasuthenticated511(boolean wasuthenticated011) {
        this.wasuthenticated511 = wasuthenticated011;
    }

    @Override
    public boolean isNeedsAuthentication511() {
        return needsAuthentication511;
    }

    @Override
    public boolean isWasuthenticated011() {
        return wasuthenticated511;
    }

    public static final String login501_1 = "login501_1";
    public static final String login501_2 = "login501_2";

    @Override
    public String createReply1(String memory) throws MalformedURLException {
        String hidden = "";
        if (memory != null) {
            hidden = "?memory=" + memory;
        }
        return "<html>\n"
                + "<head>\n"
                + " <title>Network Authentication Required</title>\n"
                + "   <meta http-equiv='refresh'\n"
                + "        content='2; url=" + getUrl(login501_1).toExternalForm() + hidden + "'>\n"
                + "</head>\n"
                + "<body>\n"
                + "   <p>You need to <a href='" + getUrl(login501_1).toExternalForm() + hidden + "'>\n"
                + "   authenticate with the local network</a> in order to gain\n"
                + "   access.</p>\n"
                + "</body>\n"
                + "</html>\n";

    }

    @Override
    public String createReply2(String memory) throws MalformedURLException {
        String s1 = "<html>\n"
                + "<head>\n"
                + " <title>Network Authentication Required</title>\n"
                + "</head>\n"
                + "<body>\n"
                + "   <p>Itw 511 simulator use itw/itw  for successful login</p><p>\n"
                //+ "   <form action='" + login501_2 + "'  method=\"get\">\n"
                //+TODO test on post "   <form action='" + login501_2 + "'  method=\"post \">\n"
                + "   <form action='" + login501_2 + "' >\n"
                + "   First name: <input type='text' name='name'><br>\n"
                + "   password: <input type='text' name='passwd'><br>\n"
                + "   <input type='submit' value='Submit'>\n";
        String hidden = "";
        if (memory != null) {
            hidden = "<input type='hidden' name='memory' value='" + memory + "'>\n";
        }
        return s1 + hidden
                + "   </form> \n"
                + "   </p>\n"
                + "</body>\n"
                + "</html>\n";

    }

}
