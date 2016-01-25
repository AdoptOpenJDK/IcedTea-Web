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
import net.sourceforge.jnlp.JNLPFile;

public class UrlUtils {
    private static final String UTF8 = "utf-8";

    public static URL normalizeUrlAndStripParams(URL url, boolean encodeFileUrls) {
        if (url == null) {
            return null;
        }
        try {
            String[] urlParts = url.toString().split("\\?");
            URL strippedUrl = new URL(urlParts[0]); 
            return normalizeUrl(strippedUrl, encodeFileUrls);
        } catch (IOException | URISyntaxException e) {
            OutputController.getLogger().log(e);
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
        } catch (MalformedURLException | UnsupportedEncodingException | URISyntaxException e) {
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
  
    /**
     * This function i striping part behind last path delimiter.
     * 
     * Expected is input like protcol://som.url/some/path/file.suff
     * Then output will bee protcol://som.url/some/path
     * 
     * Be aware of input like  protcol://som.url/some/path/
     * then input will be just  protcol://som.url/some/path
     * 
     * You can use sanitizeLastSlash and see also unittests
     * Both unix and windows salshes are supported
     * 
     * @param src src to be stripped
     * @return  src without file
     */
    public static URL removeFileName(final URL src) {
        if (src == null) {
            return src;
        }
        URL nsrc = normalizeUrlAndStripParams(src);
        String s = nsrc.getPath();
        int i1 = s.lastIndexOf("/");
        int i2 = s.lastIndexOf("\\");
        int i = Math.max(i1, i2);
        if (i < 0) {
            return src;
        }
        s = s.substring(0, i);
        try {
            return sanitizeLastSlash(new URL(src.getProtocol(), src.getHost(), src.getPort(), s));
        } catch (MalformedURLException ex) {
            OutputController.getLogger().log(ex);
            return nsrc;
        }
    }

    /**
     * Small utility function creating li list from collection of urls
     * @param remoteUrls list of urls
     * @return String containing html item list of those urls
     */
    public static String setOfUrlsToHtmlList(Iterable<URL> remoteUrls) {
        StringBuilder sb = new StringBuilder();
        sb.append("<ul>");
        for (URL url : remoteUrls) {
            sb.append("<li>").append(url.toExternalForm()).append("</li>");
        }
        sb.append("</ul>");
        return sb.toString();
    }

    /**
     * This function is removing all tailing slashes of url and 
     * both unix and windows salshes are supported.
     * See tests for valid and invalid inputs/outputs
     * Shortly   protcol://som.url/some/path/ or  protcol://som.url/some/path////
     * (and same for windows  protcol://som.url/some\path\\) will become  protcol://som.url/some/path
     * Even  protcol://som.url/ is reduced to  protcol://som.url
     * 
     * 
     * When input is like 
     * @param in url t be sanitized
     * @return url without trailing slash (if any)
     * @throws MalformedURLException if original url was wrong 
     */
    public static URL sanitizeLastSlash(URL in) throws MalformedURLException {
        if (in == null) {
            return null;
        }
        String s = sanitizeLastSlash(in.toExternalForm());
        return new URL(s);
    }

    public static String sanitizeLastSlash(final String in) {
        if (in == null) {
            return null;
        }
        String s = in;
        while (s.endsWith("/") || s.endsWith("\\")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    /**
     * both urls are processed by sanitizeLastSlash before actual equals.
     * So protcol://som.url/some/path/ is same as protcol://som.url/some/path.
     * Even protcol://som.url/some/path\ is same as protcol://som.url/some/path/
     * 
     * @param u1 first url to comapre
     * @param u2 second
     * @return  true if urls are equals no matter of trailing slash
     */
    public static boolean equalsIgnoreLastSlash(URL u1, URL u2) {
        try {
            if (u1 == null && u2 == null) {
                return true;
            }
            if (u1 == null && u2 != null) {
                return false;
            }
            if (u1 != null && u2 == null) {
                return false;
            }
            return sanitizeLastSlash(u1).equals(sanitizeLastSlash(u2));
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }

     public static URL guessCodeBase(JNLPFile file) {
        if (file.getCodeBase() != null) {
            return file.getCodeBase();
        } else {
            //Fixme: codebase should be the codebase of the Main Jar not
            //the location. Although, it still works in the current state.
            return file.getResources().getMainJAR().getLocation();
        }
    }
     
     
     /**
     * Compares a URL using string compareNullableStrings of its protocol, host, port, path,
 query, and anchor. This method avoids the host name lookup that
     * URL.equals does for http: protocol URLs. It may not return the same value
     * as the URL.equals method (different hostnames that resolve to the same IP
     * address, ie sourceforge.net and www.sourceforge.net).
     *
     * @param u1 first url to compareNullableStrings
     * @param u2 second url to compareNullableStrings
     * @return whether the u1 and u2 points to same resource or not
     */
    public static boolean urlEquals(URL u1, URL u2) {
        if (u1 == u2) {
            return true;
        }
        if (u1 == null || u2 == null) {
            return false;
        }

        if (notNullUrlEquals(u1, u2)) {
            return true;
        }
        try {
            URL nu1 = UrlUtils.normalizeUrl(u1);
            URL nu2 = UrlUtils.normalizeUrl(u2);
            if (notNullUrlEquals(nu1, nu2)) {
                return true;
            }
        } catch (Exception ex) {
            OutputController.getLogger().log(ex);
        }
        return false;
    }

    static boolean notNullUrlEquals(URL u1, URL u2) {
        return compareNullableStrings(u1.getProtocol(), u2.getProtocol(), true) 
                && compareNullableStrings(u1.getHost(), u2.getHost(), true) 
                && compareNullableStrings(u1.getPath(), u2.getPath(), false) 
                && compareNullableStrings(u1.getQuery(), u2.getQuery(), false) 
                && compareNullableStrings(u1.getRef(), u2.getRef(), false);
                // && u1.getPort() ==  u2.getPort(); errornous?
    }
    
    
    /**
     * Compare strings that can be {@code null}.
     * @param s1 first string to compareNullableStrings with s2
     * @param s2 second string to compareNullableStrings with s1
     * @param ignore switch to ignore case
     */
    static boolean compareNullableStrings(String s1, String s2, boolean ignore) {
        //this check is need to evaluate two nulls correctly
        if (s1 == s2) {
            return true;
        }
        if (s1 == null || s2 == null) {
            return false;
        }
        if (ignore) {
            return s1.equalsIgnoreCase(s2);
        } else {
            return s1.equals(s2);
        }
    }
    
    public static int getSanitizedPort(final URL u) {
        if (u.getPort() < 0) {
            return u.getDefaultPort();
        }
        return u.getPort();
    }

    public static int getPort(final URL url) {
        return getSanitizedPort(url);
    }

    public static String getHostAndPort(final URL url) {
        return url.getHost() + ":" + getSanitizedPort(url);
    }
    
    public static URL ensureSlashTail(URL u) {
        if (u == null) {
            return null;
        }
        String s = ensureSlashTail(u.toExternalForm());
        try {
            return new URL(s);
        } catch (MalformedURLException ex) {
            OutputController.getLogger().log(ex);
            return u;
        }

    }

    public static String ensureSlashTail(String s) {
        if (s.endsWith("/")) {
            return s;
        }
        if (s.endsWith("\\")) {
            return s;
        }
        if (s.contains("/")) {
            return s + "/";
        }
        if (s.contains("\\")) {
            return s + "\\";
        }
        return s + "/";
    }

    public static String stripFile(URL documentbase) {
        //whenused in generation of regec, the trailing slash is very important
        //see the result between http:/some.url/path.* and http:/some.url/path/.*
        return UrlUtils.ensureSlashTail(stripFileImp(documentbase));
    }

    private static String stripFileImp(URL documentbase) {
        try {
            String normalized = UrlUtils.normalizeUrlAndStripParams(documentbase).toExternalForm().trim();
            if (normalized.endsWith("/") || normalized.endsWith("\\")) {
                return normalized;
            }
            URL middleway = new URL(normalized);
            String file = middleway.getFile();
            int i = Math.max(file.lastIndexOf('/'), file.lastIndexOf('\\'));
            if (i < 0) {
                return normalized;
            }
            String parent = file.substring(0, i + 1);
            String stripped = normalized.replace(file, parent);
            return stripped;
        } catch (Exception ex) {
            OutputController.getLogger().log(ex);
            return documentbase.toExternalForm();
        }

    }

}
