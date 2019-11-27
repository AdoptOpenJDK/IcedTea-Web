/* ResourceUrlCreator.java
   Copyright (C) 2011 Red Hat, Inc

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
package net.adoptopenjdk.icedteaweb.resources.initializer;

import net.adoptopenjdk.icedteaweb.http.CloseableConnection;
import net.adoptopenjdk.icedteaweb.http.ConnectionFactory;
import net.adoptopenjdk.icedteaweb.http.HttpMethod;
import net.adoptopenjdk.icedteaweb.http.HttpUtils;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import static net.adoptopenjdk.icedteaweb.resources.JnlpDownloadProtocolConstants.VERSION_ID_HEADER;

class UrlProber {
    private static final Logger LOG = LoggerFactory.getLogger(UrlProber.class);

    /**
     * Connects to the given URL, and grabs a response code and redirection if
     * the URL uses the HTTP protocol, or returns an arbitrary valid HTTP
     * response code.
     *
     * @return the response code if HTTP connection and redirection value, or
     * HttpURLConnection.HTTP_OK and null if not.
     * @throws IOException if an I/O exception occurs.
     */
    static UrlRequestResult getUrlResponseCodeWithRedirectionResult(final URL url, final Map<String, String> requestProperties, final HttpMethod requestMethod) throws IOException {
        try (final CloseableConnection connection = ConnectionFactory.openConnection(url, requestMethod, requestProperties)) {
            connection.setConnectTimeout(5000);

            /* Fully consuming current request helps with connection re-use
             * See http://docs.oracle.com/javase/1.5.0/docs/guide/net/http-keepalive.html */
            HttpUtils.consumeAndCloseConnectionSilently(connection);

            LOG.debug("URL connection '{}' header fields: {}", url, connection.getHeaderFields());

            return new UrlRequestResult(
                    connection.getURL(),
                    connection.getResponseCode(),
                    connection.getLocationHeaderFieldUrl(),
                    getJnlpVersionHeader(connection),
                    connection.getLastModified(),
                    connection.getContentLength());
        }
    }

    private static VersionId getJnlpVersionHeader(CloseableConnection connection) {
        final String version = connection.getHeaderField(VERSION_ID_HEADER);
        return version != null ? VersionId.fromString(version) : null;
    }
}
