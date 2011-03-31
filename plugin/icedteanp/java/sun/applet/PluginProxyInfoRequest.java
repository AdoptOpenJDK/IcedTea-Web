/* PluginProxyInfoRequest -- Object representing a request for proxy information from the browser
   Copyright (C) 2009  Red Hat

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

package sun.applet;

import java.net.URI;

/**
 * This class represents a request object for proxy information for a given URI
 */

public class PluginProxyInfoRequest extends PluginCallRequest {

    URI internal = null;

    public PluginProxyInfoRequest(String message, Long reference) {
        super(message, reference);
    }

    public void parseReturn(String proxyInfo) {

        // try to parse the proxy information. If things go wrong, do nothing .. 
        // this will keep internal = null which forces a direct connection

        PluginDebug.debug("PluginProxyInfoRequest GOT: ", proxyInfo);
        String[] messageComponents = proxyInfo.split(" ");

        try {
            String protocol = messageComponents[4].equals("PROXY") ? "http" : "socks";
            String host = messageComponents[5].split(":")[0];
            int port = Integer.parseInt(messageComponents[5].split(":")[1]);

            internal = new URI(protocol, null, host, port, null, null, null);
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            // Nothing.. this is expected if there is no proxy
        } catch (Exception e) {
            e.printStackTrace();
        }

        setDone(true);
    }

    public URI getObject() {
        return this.internal;
    }
}
