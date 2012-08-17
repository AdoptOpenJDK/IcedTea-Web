/* PluginCookieManager -- Cookie manager for the plugin
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

import java.io.IOException;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.sun.jndi.toolkit.url.UrlUtil;

public class PluginCookieManager extends CookieManager {
    private PluginStreamHandler streamHandler;

    public PluginCookieManager(PluginStreamHandler streamHandler) {
        this.streamHandler = streamHandler;
    }

    @Override
    public Map<String, List<String>> get(URI uri,
            Map<String, List<String>> requestHeaders) throws IOException {
        // pre-condition check
        if (uri == null || requestHeaders == null) {
            throw new IllegalArgumentException("Argument is null");
        }

        Map<String, List<String>> cookieMap = new java.util.HashMap<String, List<String>>();

        String cookies = (String) PluginAppletViewer
                .requestPluginCookieInfo(uri);
        List<String> cookieHeader = new java.util.ArrayList<String>();

        if (cookies != null && cookies.length() > 0)
            cookieHeader.add(cookies);

        // Add anything else that mozilla didn't add
        for (HttpCookie cookie : getCookieStore().get(uri)) {
            // apply path-matches rule (RFC 2965 sec. 3.3.4)
            if (pathMatches(uri.getPath(), cookie.getPath())) {
                cookieHeader.add(cookie.toString());
            }
        }

        cookieMap.put("Cookie", cookieHeader);
        return Collections.unmodifiableMap(cookieMap);
    }

    private boolean pathMatches(String path, String pathToMatchWith) {
        if (path == pathToMatchWith)
            return true;
        if (path == null || pathToMatchWith == null)
            return false;
        if (path.startsWith(pathToMatchWith))
            return true;

        return false;
    }

    @Override
    public void put(URI uri,
            Map<String, List<String>> responseHeaders) throws IOException {
        super.put(uri, responseHeaders);

        for (Map.Entry<String, List<String>> headerEntry : responseHeaders.entrySet()) {
            String type = headerEntry.getKey();
            if ("Set-Cookie".equalsIgnoreCase(type) || "Set-Cookie2".equalsIgnoreCase(type)) {
                List<String> cookies = headerEntry.getValue();
                for (String cookie : cookies) {
                    streamHandler.write("plugin PluginSetCookie reference -1 " + UrlUtil.encode(uri.toString(), "UTF-8") + " " + cookie);
                }
            }

        }
    }
}
