/* UrlUtils.java
   Copyright (C) 2011 Red Hat, Inc.

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

package net.sourceforge.jnlp.util;

import net.sourceforge.jnlp.util.logging.OutputController;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;

public class UrlUtils {
    private static final String UTF8 = "utf-8";

    public static URL normalizeUrlAndStripParams(URL url, boolean encodeFileUrls) {
        try {
            String[] urlParts = url.toString().split("\\?");
            URL strippedUrl = new URL(urlParts[0]); 
            return normalizeUrl(strippedUrl, encodeFileUrls);
        } catch (IOException e) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
        } catch (URISyntaxException e) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
        }
        return url;
    }

    public static URL normalizeUrlAndStripParams(URL url) {
        return normalizeUrlAndStripParams(url, false);
    }

    public static boolean isLocalFile(URL url) {

        if (url.getProtocol().equals("file") &&
                (url.getAuthority() == null || url.getAuthority().equals("")) &&
                (url.getHost() == null || url.getHost().equals(("")))) {
            return true;
        }
        return false;
    }

    /* Decode a percent-encoded URL. Catch checked exceptions and log. */
    public static URL decodeUrlQuietly(URL url) {
        try {
            return new URL(URLDecoder.decode(url.toString(), UTF8));
        } catch (IOException e) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
            return url;
        }
    }

    /* Use the URI syntax check of 'toURI' to see if it matches RFC2396.
     * See http://www.ietf.org/rfc/rfc2396.txt */
    public static boolean isValidRFC2396Url(URL url) {
        try {
            url.toURI();
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    /* Ensure a URL is properly percent-encoded.
     * Certain usages require local-file URLs to be encoded, eg for code-base & document-base. */
    public static URL normalizeUrl(URL url, boolean encodeFileUrls) throws MalformedURLException, UnsupportedEncodingException, URISyntaxException {
        if (url == null) {
            return null;
        }

        String protocol = url.getProtocol();
        boolean shouldEncode = (encodeFileUrls || !"file".equals(protocol));

        // PR1465: We should not call 'URLDecoder.decode' on RFC2396-compliant URLs
        if (protocol == null || !shouldEncode || url.getPath() == null || isValidRFC2396Url(url)) {
            return url;
        }

        //Decode the URL before encoding
        URL decodedURL = new URL(URLDecoder.decode(url.toString(), UTF8));

        //Create URI with the decoded URL
        URI uri = new URI(decodedURL.getProtocol(), null, decodedURL.getHost(), decodedURL.getPort(), decodedURL.getPath(), decodedURL.getQuery(), null);

        //Returns the encoded URL
        URL encodedURL = new URL(uri.toASCIIString());

        return encodedURL;
    }

    /* Ensure a URL is properly percent-encoded. Does not encode local-file URLs. */
    public static URL normalizeUrl(URL url) throws MalformedURLException, UnsupportedEncodingException, URISyntaxException {
        return normalizeUrl(url, false);
    }

    /* Ensure a URL is properly percent-encoded. Catch checked exceptions and log. */
    public static URL normalizeUrlQuietly(URL url, boolean encodeFileUrls) {
        try {
            return normalizeUrl(url, encodeFileUrls);
        } catch (MalformedURLException e) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
        } catch (UnsupportedEncodingException e) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
        } catch (URISyntaxException e) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
        }
        return url;
    }

    /* Ensure a URL is properly percent-encoded. Catch checked exceptions and log. */
    public static URL normalizeUrlQuietly(URL url) {
        return normalizeUrlQuietly(url, false);
    }

    /* Decode a URL as a file, being tolerant of URLs with mixed encoded & decoded portions. */
    public static File decodeUrlAsFile(URL url) {
        return new File(decodeUrlQuietly(url).getFile());
    }
}
