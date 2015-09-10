/* SOPBypass.java
Copyright (C) 2015 Red Hat, Inc.

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

import java.applet.Applet;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.AccessControlException;
import java.security.PrivilegedAction;

public class SOPBypassSigned extends Applet {

    @Override
    public void start() {
        System.out.println("Applet Started");

        System.out.println("Codebase URL: " + getCodeBase());
        System.out.println("DocumentBase URL: " + getDocumentBase());

        AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            @Override
            public Boolean run() {
                checkPrivilege();
                attemptSocketConnectionToCodebase();
                attemptSocketConnectionToDocumentBase();
                attemptSocketConnectionToUnrelated();
                attemptUrlConnectionToCodebase();
                attemptUrlConnectionToDocumentBase();
                attemptUrlConnectionToUnrelated();
                return true;
            }
        });

        System.out.println("*** APPLET FINISHED ***");
    }

    void checkPrivilege() {
        boolean canRead = false;
        try {
            System.getProperty("user.home");
            canRead = true;
        } catch (AccessControlException ace) {
            ace.printStackTrace();
            canRead = false;
        }
        System.out.println("Elevated privileges: " + canRead);
    }

    void attemptSocketConnectionToCodebase() {
        String host = getCodeBase().getHost();
        int port = getCodeBase().getPort();;
        attemptSocketConnection(host, port, "codeBase", true);
    }

    void attemptSocketConnectionToDocumentBase() {
        String host = getDocumentBase().getHost();
        int port = getDocumentBase().getPort();
        attemptSocketConnection(host, port, "documentBase", true);
    }

    void attemptSocketConnectionToUnrelated() {
        String host = "example.com";
        int port = 80;
        attemptSocketConnection(host, port, "unrelated", false);
    }

    void attemptSocketConnection(String host, int port, String s, boolean sendData) {
        boolean connected = true;
        try {
            Socket local = new Socket();
            local.bind(null);
            local.connect(new InetSocketAddress(host, port));
            if (sendData) {
                try (PrintWriter writer = new PrintWriter(local.getOutputStream(), true)) {
                    writer.println("test");
                }
            }
        } catch (Exception e) {
            connected = false;
            e.printStackTrace();
        }
        System.out.println("SocketConnection:" + s + " " + connected);
    }

    void attemptUrlConnectionToCodebase() {
        attemptUrlConnection(getCodeBase(), "codeBase");
    }

    void attemptUrlConnectionToDocumentBase() {
        attemptUrlConnection(getDocumentBase(), "documentBase");
    }

    void attemptUrlConnectionToUnrelated() {
        try {
            attemptUrlConnection(new URL("http://example.com:80"), "unrelated");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            System.out.println("Unrelated URL test failed due to MalformedURLException");
            System.out.println("URLConnection:unrelated false");
        }
    }

    void attemptUrlConnection(URL url, String s) {
        boolean connected = true;
        try {
            URLConnection conn = url.openConnection();
            conn.connect();
            conn.getContentEncoding();
            conn.getContentLength();
            conn.getContentType();
        } catch (Exception e) {
            connected = false;
            e.printStackTrace();
        }
        System.out.println("URLConnection:" + s + " " + connected);
    }

    public static void main(String[] args) {
        new SOPBypassSigned().start();
    }

}
