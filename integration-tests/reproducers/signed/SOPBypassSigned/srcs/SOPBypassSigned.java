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
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class SOPBypassSigned extends Applet {
    private String unrelatedUrl;
    private String reachableResource;
    private String resourcesUrl;

    
    @Override
    public void init(){
        setUnrelatedUrl(this.getParameter("unrelatedUrl"));
        setReachableResource(this.getParameter("reachableResource"));
        setResourcesUrl(this.getParameter("resourceUrl"));
    }
    
    @Override
    public void start() {
        System.out.println("Applet Started");

        System.out.println("Codebase URL: " + getCodeBase());
        System.out.println("DocumentBase URL: " + getDocumentBase());
        System.out.println("unrelatedUrl: " + unrelatedUrl);

        AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            @Override
            public Boolean run() {
                checkPrivilege();
                attemptSocketConnectionToCodebase();
                attemptSocketConnectionToDocumentBase();
                attemptSocketConnectionToUnrelated();
                attemptSocketConnectionToResourcesLoc();
                attemptUrlConnectionToCodebase();
                attemptUrlConnectionToDocumentBase();
                attemptUrlConnectionToUnrelated();
                attemptUrlConnectionToResourcesLoc();
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
        int port = getCodeBase().getPort();
        attemptSocketConnection(host, port, reachableResource, "codeBase", true);
    }

    void attemptSocketConnectionToDocumentBase() {
        String host = getDocumentBase().getHost();
        int port = getDocumentBase().getPort();
        attemptSocketConnection(host, port, reachableResource,  "documentBase", true);
    }

    void attemptSocketConnectionToUnrelated() {
        String host = "example.com";
        int port = 80;
        if (unrelatedUrl != null){
            host=extractHost(unrelatedUrl);
            port=extractPort(unrelatedUrl);
        }
        attemptSocketConnection(host, port, reachableResource, "unrelated", true);
    }
    
    void attemptSocketConnectionToResourcesLoc() {
        String host = getCodeBase().getHost();
        int port = getCodeBase().getPort();
        //if resources url was null, then it was probably from codebase
        if (resourcesUrl != null){
            host=extractHost(resourcesUrl);
            port=extractPort(resourcesUrl);
        }
        attemptSocketConnection(host, port, reachableResource, "resource's", true);
    }

    void attemptSocketConnection(String host, int port, String resource, String id, boolean sendData) {
        boolean connected = true;
        try {
            final Socket local = new Socket();
            local.bind(null);
            local.connect(new InetSocketAddress(host, port));
            final BufferedReader br = new BufferedReader(new InputStreamReader(local.getInputStream()));
            final PrintWriter writer = new PrintWriter(local.getOutputStream(), true);
            if (sendData) {
                writer.println("GET /" + reachableResource + " HTTP/1.1");
                writer.println("Host: " + "itwTest");
                writer.println("Accept: */*");
                writer.println("User-Agent: Java"); //used to it to much
                writer.println(""); // Important, else the server will expect that there's more into the request.
                writer.flush();
                String s = getText(br);
                System.out.println("" + s);
                if (s == null || s.trim().isEmpty()) {
                    connected = false;
                }
                local.close();
            }
        } catch (Exception e) {
            connected = false;
            e.printStackTrace();
        }
        System.out.println("SocketConnection:" + id + " " + connected);
    }

    void attemptUrlConnectionToCodebase() {
        attemptUrlConnection(getCodeBase(), reachableResource, "codeBase");
    }

    void attemptUrlConnectionToDocumentBase() {
        attemptUrlConnection(getDocumentBase(), reachableResource,  "documentBase");
    }

    void attemptUrlConnectionToUnrelated() {
        try {
            if (unrelatedUrl  == null) {
                attemptUrlConnection(new URL("http://example.com:80"), reachableResource,  "unrelated");
            } else {
                attemptUrlConnection(new URL(unrelatedUrl), reachableResource,  "unrelated");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            System.out.println("Unrelated URL test failed due to MalformedURLException");
            System.out.println("URLConnection:unrelated false");
        }
    }
    void attemptUrlConnectionToResourcesLoc() {
        try {
            if (resourcesUrl  == null) {
                //if resources url was null, then it was probably from codebase
                attemptUrlConnection(getCodeBase(), reachableResource,  "resource's");
            } else {
                attemptUrlConnection(new URL(resourcesUrl), reachableResource,  "resource's");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            System.out.println("resource's URL test failed due to MalformedURLException");
            System.out.println("URLConnection:resource's false");
        }
    }

    void attemptUrlConnection(URL url, String resource,  String id) {
        boolean connected = true;
        try {
            URLConnection conn = url.openConnection();
            conn.connect();
            conn.getContentEncoding();
            conn.getContentLength();
            conn.getContentType();
            if (resource != null) {
                URLConnection connn = concateUrlAndResource(url, resource).openConnection();
                connn.connect();
                connn.getContentEncoding();
                connn.getContentLength();
                connn.getContentType();
                String s = getText(connn.getInputStream());
                System.out.println("" + s);
                if (s == null || s.trim().isEmpty()) {
                    connected = false;
                }
            }
        } catch (Exception e) {
            connected = false;
            e.printStackTrace();
        }
        System.out.println("URLConnection:" + id + " " + connected);
    }

    public static void main(String[] args) {
        //args = new String[]{"SOPBypass-filtered.html", "http://localhost:44321"};
        SOPBypassSigned sop = new SOPBypassSigned();
        if (args.length > 0) {
            sop.setReachableResource(args[0]);
        }
        if (args.length > 1) {
            sop.setUnrelatedUrl(args[1]);
        }
        if (args.length > 2) {
            sop.setResourcesUrl(args[2]);
        }
        sop.start();
    }

    private void setUnrelatedUrl(String s) {
        unrelatedUrl = s;
    }

    private void setReachableResource(String s) {
        reachableResource = s;
    }

    public void setResourcesUrl(String resourcesUrl) {
        this.resourcesUrl = resourcesUrl;
    }
    
    

    static private String extractHost(String unrelatedUrl) {
        String s[] = unrelatedUrl.split(":");
        while (s[1].startsWith("/")) {
            s[1] = s[1].substring(1);
        }
        return s[1];
    }

    static private int extractPort(String unrelatedUrl) {
        String s[] = unrelatedUrl.split(":");
        if (s.length < 3) {
            return 80;
        }
        //protocol:host:port or
        //protocol:i:p:...:v:6:port
        int i = s.length - 1;
        try {
            return new Integer(s[i]);
        } catch (NumberFormatException ex) {
            //nope
        }
        return 80;
    }

    private URL concateUrlAndResource(URL url, String resource) {
        String s = url.toExternalForm();
        String badResource1 = resource.replace("-filtered", "");
        String badResource2 = badResource1.replace("Signed", "");
        //see testcases for usages
        s = s.replace("/codebase/", "/");
        s = s.replace("/"+badResource1, "/");
        s = s.replace("/"+badResource2, "/");
        try {
            //docbase may have it
            if (s.endsWith(resource)) {
                return new URL(s);
            }
            if (s.endsWith("/")) {
                s = s + resource;
            } else {
                s = s + "/" + resource;
            }
            return new URL(s);
        } catch (MalformedURLException ex) {
            //jsut adding resource to vlaisd url. 
            //should not happen, if so, die later
            return null;
        }
    }

    public static String getText(InputStream is) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        try {
            return getText(in);
        } finally {
            in.close();
        }
    }

    public static String getText(BufferedReader in) throws Exception {

        StringBuilder response = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        return response.toString();
    }

}
