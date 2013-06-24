/* TinyHttpdImpl.java
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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.SocketException;
import java.net.URLDecoder;
import java.util.StringTokenizer;

/**
 * based on http://www.mcwalter.org/technology/java/httpd/tiny/index.html
 * Very small implementation of http return headers for our served resources
 * Originally Licenced under GPLv2.0 
 *
 * When resource starts with XslowX prefix, then resouce (without XslowX)
 * is returned, but its delivery is delayed
 */
public class TinyHttpdImpl extends Thread {

    Socket c;
    private final File dir;
    private final int port;
    private boolean canRun = true;
    private static final String XSX = "/XslowX";
    private boolean supportingHeadRequest = true;
    
    public TinyHttpdImpl(Socket s, File f, int port) {
        this(s, f, port, true);
    }
    public TinyHttpdImpl(Socket s, File f, int port, boolean start) {
        c = s;
        this.dir = f;
        this.port = port;
        if (start){
            start();
        }
    }

    public void setCanRun(boolean canRun) {
        this.canRun = canRun;
    }

    public void setSupportingHeadRequest(boolean supportsHead) {
        this.supportingHeadRequest = supportsHead;
    }

    public boolean isSupportingHeadRequest() {
        return supportingHeadRequest;
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

                    boolean isGetRequest = s.startsWith("GET");
                    boolean isHeadRequest = s.startsWith("HEAD");
                    
                    if (isHeadRequest && !isSupportingHeadRequest()){
                        o.writeBytes("HTTP/1.0 "+HttpURLConnection.HTTP_NOT_IMPLEMENTED+" Not Implemented\n");
                        continue;
                    }
                    
                    String request = "unknown";
                    if (isGetRequest || isHeadRequest ) {
                        if (isGetRequest){
                             request = "GET";
                        }
                        if (isHeadRequest){
                             request = "HEAD";
                        }
                        StringTokenizer t = new StringTokenizer(s, " ");
                        t.nextToken();
                        String op = t.nextToken();
                        String p = op;
                        if (p.startsWith(XSX)) {
                            p = p.replace(XSX, "/");
                        }
                        ServerAccess.logOutputReprint("Getting- " + request + ": " + p);
                        p = urlToFilePath(p);
                        ServerAccess.logOutputReprint("Serving- " + request + ": " + p);
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
                        o.writeBytes("HTTP/1.0 "+HttpURLConnection.HTTP_OK+" OK\nContent-Length:" + l + "\n" + content + "\n");

                        if (isHeadRequest) {
                            continue; // Skip sending body
                        }

                        if (op.startsWith(XSX)) {
                            byte[][] bb = splitArray(b, 10);
                            for (int j = 0; j < bb.length; j++) {
                                Thread.sleep(2000);
                                byte[] bs = bb[j];
                                o.write(bs, 0, bs.length);
                            }
                        } else {
                            o.write(b, 0, l);
                        }
                    }
                }
            } catch (SocketException e) {
                ServerAccess.logException(e, false);
            } catch (Exception e) {
                o.writeBytes("HTTP/1.0 404 ERROR\n\n\n");
                ServerAccess.logException(e, false);
            }
            o.close();
        } catch (Exception e) {
            ServerAccess.logException(e, false);
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
        if (rest % pieces > 0) {
            rowLength++;
        }
        if (pieces * rowLength >= rest + rowLength) {
            pieces--;
        }
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
    
    /**
    * This function transforms a request URL into a path to a file which the server
    * will return to the requester.
    * @param url - the request URL
    * @return a String representation of the local path to the file
    * @throws UnsupportedEncodingException
    */
    public static String urlToFilePath(String url) throws UnsupportedEncodingException {
        url = URLDecoder.decode(url, "UTF-8"); // Decode URL encoded charaters, eg "%3B" b    ecomes ';'
        if (url.startsWith(XSX)) {
            url = url.replace(XSX, "/");
        }
        url = url.replaceAll("\\?.*", ""); // Remove query string from URL
        url = ".".concat(url); // Change path into relative path
        if (url.endsWith("/")) {
            url += "index.html";
        }
        url = url.replace('/', File.separatorChar); // If running on Windows, replace '/'     in path with "\\"
        url = stripHttpPathParams(url);
        return url;
    }

    /**
     * This function removes the HTTP Path Parameter from a given JAR URL, assuming that the
     * path param delimiter is a semicolon
     * @param url - the URL from which to remove the path parameter
     * @return the URL with the path parameter removed
     */
    public static String stripHttpPathParams(String url) {
    	if (url == null) {
    		return null;
    	}
    	
    	// If JNLP specifies JAR URL with .JAR extension (as it should), then look for any semicolons
    	// after this position. If one is found, remove it and any following characters.
    	int fileExtension = url.toUpperCase().lastIndexOf(".JAR");
    	if (fileExtension != -1) {
    		int firstSemiColon = url.indexOf(';', fileExtension);
    		if (firstSemiColon != -1) {    			
    			url = url.substring(0, firstSemiColon);
    		}
    	}
    	return url;
    }
}
