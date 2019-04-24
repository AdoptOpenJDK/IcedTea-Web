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
package net.adoptopenjdk.icedteaweb.testing;

import java.io.File;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.URL;

/**
 * wrapper around tiny http server to separate lunch configurations and servers.
 * to allow terminations and stuff around.
 */
public class ServerLauncher implements Runnable {

    public enum ServerNaming {
        LOCALHOST, LOCALHOST_IP, HOSTNAME
    }

    private boolean running;
    private final Integer port;
    private final File dir;
    private ServerSocket serverSocket;
    private boolean supportingHeadRequest = true;
    private final ServerNaming serverNaming = ServerNaming.LOCALHOST;

    public void setSupportingHeadRequest(final boolean supportsHead) {
        this.supportingHeadRequest = supportsHead;
    }

    private boolean isSupportingHeadRequest() {
        return supportingHeadRequest;
    }


    private String getServerName() {
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
        /*
      default url name part. This can be changed in runtime, but will affect
      all following tasks upon those server
     */
        return ServerAccess.DEFAULT_LOCALHOST_NAME;
    }

    public ServerLauncher(final Integer port, final File dir) {
        this.port = port;
        this.dir = dir;
        System.err.println("port: " + port);
        System.err.println("dir: " + dir);
    }

    public Integer getPort() {
        return port;
    }

    public File getDir() {
        return dir;
    }

    @Override
    public void run() {
        running = true;
        try {
            serverSocket = new ServerSocket(port);
            while (running) {
                final TinyHttpdImpl server = new TinyHttpdImpl(serverSocket.accept(), dir, false);
                server.setRequestsCounter(null);
                server.setSupportingHeadRequest(isSupportingHeadRequest());

                server.start();
            }
        } catch (final Exception e) {
            ServerAccess.logException(e);
        } finally {
            running = false;
        }
    }

    private String sanitizeResource(final String resource) {
        if (resource == null) {
            return  "";
        }
        if (resource.trim().length() > 0 && !resource.startsWith("/")) {
            return  "/" + resource;
        }
        return resource;
    }

    public URL getUrl(final String resource) throws MalformedURLException {
        final String protocol = ServerAccess.DEFAULT_LOCALHOST_PROTOCOL;
        return new URL(protocol, getServerName(), getPort(), sanitizeResource(resource));
    }

    public URL getUrl() throws MalformedURLException {
        return getUrl("");
    }

    public void stop() {
        this.running = false;
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (final Exception ex) {
                ServerAccess.logException(ex);
            }
        }
        System.err.println("stopped : " + port);
    }

    @Override
    public String toString() {
        try {
            return getUrl() + " - " + super.toString();
        } catch (final Exception ex) {
            ServerAccess.logException(ex);
        }
        return super.toString();
    }
}
