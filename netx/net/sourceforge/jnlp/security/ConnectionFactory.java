/* 
   Copyright (C) 2014  Red Hat

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

IcedTea is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
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
exception statement from your version. */

package net.sourceforge.jnlp.security;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;
import net.sourceforge.jnlp.util.logging.OutputController;


public class ConnectionFactory {

    private final List<URLConnection> httpsConnections = new ArrayList<>();

    private boolean isSyncForced() {
        return false;
    }

    public static ConnectionFactory getConnectionFactory() {
        return ConnectionFactoryHolder.INSTANCE;
    }

    private static class ConnectionFactoryHolder {

        //https://en.wikipedia.org/wiki/Double-checked_locking#Usage_in_Java
        //https://en.wikipedia.org/wiki/Initialization_on_demand_holder_idiom
        private static volatile ConnectionFactory INSTANCE = new ConnectionFactory();
    }

    public URLConnection openConnection(URL url) throws IOException {
        OutputController.getLogger().log("Connecting " + url.toExternalForm());
        if (url.getProtocol().equalsIgnoreCase("https")) {
            if (isSyncForced()) {
                OutputController.getLogger().log("Waiting for " + httpsConnections.size() + " connections to finish");
                while (!httpsConnections.isEmpty()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        throw new IOException(ex);
                    }
                }
            }
            return openHttpsConnection(url);
        } else {
            URLConnection conn = url.openConnection();
            OutputController.getLogger().log("done " + url.toExternalForm());
            return conn;
        }
    }

    private synchronized URLConnection openHttpsConnection(URL url) throws IOException {
        URLConnection conn = null;
        conn = url.openConnection();
        OutputController.getLogger().log("Adding " + conn.toString());
        httpsConnections.add(conn);
        OutputController.getLogger().log("done " + url.toExternalForm());
        return conn;
    }

    public void disconnect(URLConnection conn) {
        if (conn != null) {
            OutputController.getLogger().log("Disconnecting " + conn.toString());
            if (conn instanceof HttpsURLConnection) {
                closeHttpsConnection((HttpsURLConnection) conn);
            } else {
                if (conn instanceof HttpURLConnection) {
                    ((HttpURLConnection) conn).disconnect();
                }
            }
        } else {
            OutputController.getLogger().log("\"Disconnecting\" null connection. This is ok if you are offline.");
        }
    }

    private synchronized void closeHttpsConnection(HttpsURLConnection conn) {
        conn.disconnect();
        //this s intentional search by object value. equals do not work
        for (int i = 0; i < httpsConnections.size(); i++) {
            URLConnection urlConnection = httpsConnections.get(i);
            if (urlConnection == conn) {
                httpsConnections.remove(i);
                OutputController.getLogger().log("Removed " + urlConnection.toString());
                i--;

            }

        }
    }
  
}
