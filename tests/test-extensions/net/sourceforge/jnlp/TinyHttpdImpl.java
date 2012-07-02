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
class TinyHttpdImpl extends Thread {

    Socket c;
    private final File dir;
    private final int port;
    private boolean canRun = true;
    private static final String XSX = "/XslowX";

    public TinyHttpdImpl(Socket s, File f, int port) {
        c = s;
        this.dir = f;
        this.port = port;
        start();
    }

    public void setCanRun(boolean canRun) {
        this.canRun = canRun;
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
                        if (p.startsWith(XSX)) {
                            p = p.replace(XSX, "/");
                        }
                        ServerAccess.logNoReprint("Getting: " + p);
                        p = URLDecoder.decode(p, "UTF-8");
                        ServerAccess.logNoReprint("Serving: " + p);
                        p = (".".concat((p.endsWith("/")) ? p.concat("index.html") : p)).replace('/', File.separatorChar);
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
                        o.writeBytes("HTTP/1.0 200 OK\nConten" + "t-Length:" + l + "\n" + content + "\n");
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
}
