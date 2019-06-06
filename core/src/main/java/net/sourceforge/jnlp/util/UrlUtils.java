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

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.JNLPFile;

import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

public class UrlUtils {

    private final static Logger LOG = LoggerFactory.getLogger(UrlUtils.class);

    public static final String FILE_PROTOCOL = "file";
    public static final String SLASH = "/";
    public static final String BACKSLASH = "\\";
    public static final String UL_TAG_OPEN = "<ul>";
    public static final String LI_TAG_OPEN = "<li>";
    public static final String LI_TAG_CLOSE = "</li>";
    public static final String UL_TAG_CLOSE = "</ul>";
    public static final int HTTP_STATUS_200 = 200;
    public static final String BACKSLASH_REGEX = "\\?";
    public static final String HTTPS = "https";
    public static final String BACKSLASH_N = "\n";
    public static final String PARENT_DIR = "..";

    public static URL normalizeUrlAndStripParams(final URL url, final boolean encodeFileUrls) {
        if (url == null) {
            return null;
        }
        try {
            final String[] urlParts = url.toString().split(BACKSLASH_REGEX);
            final URL strippedUrl = new URL(urlParts[0]);
            return normalizeUrl(strippedUrl, encodeFileUrls);
        } catch (final IOException | URISyntaxException e) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
        }
        return url;
    }

    public static URL normalizeUrlAndStripParams(final URL url) {
        return normalizeUrlAndStripParams(url, false);
    }

    public static boolean isLocalFile(final URL url) {
        Objects.requireNonNull(url);
        if (Objects.equals(url.getProtocol(), FILE_PROTOCOL)
                && (url.getAuthority() == null || Objects.equals(url.getAuthority(), ""))
                && (url.getHost() == null || Objects.equals(url.getHost(), ""))) {
            return true;
        }
        return false;
    }

    /* Decode a percent-encoded URL. Catch checked exceptions and log. */
    public static URL decodeUrlQuietly(final URL url) {
        try {
            return new URL(URLDecoder.decode(url.toString(), UTF_8.name()));
        } catch (IOException e) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
            return url;
        }
    }

    /* Use the URI syntax check of 'toURI' to see if it matches RFC2396.
     * See http://www.ietf.org/rfc/rfc2396.txt */
    public static boolean isValidRFC2396Url(final URL url) {
        try {
            url.toURI();
            return true;
        } catch (final URISyntaxException e) {
            return false;
        }
    }

    /* Ensure a URL is properly percent-encoded.
     * Certain usages require local-file URLs to be encoded, eg for code-base & document-base. */
    public static URL normalizeUrl(final URL url, final boolean encodeFileUrls) throws MalformedURLException, UnsupportedEncodingException, URISyntaxException {
        if (url == null) {
            return null;
        }

        final String protocol = url.getProtocol();
        final boolean shouldEncode = (encodeFileUrls || !Objects.equals(FILE_PROTOCOL, protocol));

        // PR1465: We should not call 'URLDecoder.decode' on RFC2396-compliant URLs
        if (protocol == null || !shouldEncode || url.getPath() == null || isValidRFC2396Url(url)) {
            return url;
        }

        //Decode the URL before encoding
        final URL decodedURL = new URL(URLDecoder.decode(url.toString(), UTF_8.name()));

        //Create URI with the decoded URL
        final URI uri = new URI(decodedURL.getProtocol(), null, decodedURL.getHost(), decodedURL.getPort(), decodedURL.getPath(), decodedURL.getQuery(), null);

        //Returns the encoded URL
        final URL encodedURL = new URL(uri.toASCIIString());

        return encodedURL;
    }

    /* Ensure a URL is properly percent-encoded. Does not encode local-file URLs. */
    public static URL normalizeUrl(final URL url) throws MalformedURLException, UnsupportedEncodingException, URISyntaxException {
        return normalizeUrl(url, false);
    }

    /* Ensure a URL is properly percent-encoded. Catch checked exceptions and log. */
    public static URL normalizeUrlQuietly(final URL url, final boolean encodeFileUrls) {
        try {
            return normalizeUrl(url, encodeFileUrls);
        } catch (MalformedURLException | UnsupportedEncodingException | URISyntaxException e) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
        }
        return url;
    }

    /* Ensure a URL is properly percent-encoded. Catch checked exceptions and log. */
    public static URL normalizeUrlQuietly(final URL url) {
        return normalizeUrlQuietly(url, false);
    }

    /* Decode a URL as a file, being tolerant of URLs with mixed encoded & decoded portions. */
    public static File decodeUrlAsFile(final URL url) {
        return new File(decodeUrlQuietly(url).getFile());
    }

    /**
     * This function i striping part behind last path delimiter.
     *
     * Expected is input like protocol://som.url/some/path/file.suff Then output
     * will bee protocol://som.url/some/path
     *
     * Be aware of input like protocol://som.url/some/path/ then input will be
     * just protocol://som.url/some/path
     *
     * You can use sanitizeLastSlash and see also unit tests Both unix and
     * windows slashes are supported
     *
     * @param src src to be stripped
     * @return src without file
     */
    public static URL removeFileName(final URL src) {
        if (src == null) {
            return src;
        }
        final URL nsrc = normalizeUrlAndStripParams(src);
        String s = nsrc.getPath();
        final int i1 = s.lastIndexOf(SLASH);
        final int i2 = s.lastIndexOf(BACKSLASH);
        final int i = Math.max(i1, i2);
        if (i < 0) {
            return src;
        }
        s = s.substring(0, i);
        try {
            return sanitizeLastSlash(new URL(src.getProtocol(), src.getHost(), src.getPort(), s));
        } catch (MalformedURLException ex) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
            return nsrc;
        }
    }

    /**
     * Small utility function creating li list from collection of urls
     *
     * @param remoteUrls list of urls
     * @return String containing html item list of those urls
     */
    public static String setOfUrlsToHtmlList(final Iterable<URL> remoteUrls) {
        if (remoteUrls == null) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        sb.append(UL_TAG_OPEN);
        for (final URL url : remoteUrls) {
            sb.append(LI_TAG_OPEN).append(url.toExternalForm()).append(LI_TAG_CLOSE);
        }
        sb.append(UL_TAG_CLOSE);
        return sb.toString();
    }

    /**
     * This function is removing all tailing slashes of url and both unix and
     * windows slashes are supported. See tests for valid and invalid
     * inputs/outputs Shortly protocol://som.url/some/path/ or
     * protocol://som.url/some/path//// (and same for windows
     * protocol://som.url/some\path\\) will become protocol://som.url/some/path
     * Even protocol://som.url/ is reduced to protocol://som.url
     *
     *
     * When input is like
     *
     * @param in url t be sanitized
     * @return url without trailing slash (if any)
     * @throws MalformedURLException if original url was wrong
     */
    public static URL sanitizeLastSlash(final URL in) throws MalformedURLException {
        if (in == null) {
            return null;
        }
        final String s = sanitizeLastSlash(in.toExternalForm());
        return new URL(s);
    }

    public static String sanitizeLastSlash(final String in) {
        if (in == null) {
            return null;
        }
        String s = in;
        while (s.endsWith(SLASH) || s.endsWith(BACKSLASH)) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    public static URL guessCodeBase(final JNLPFile file) {
        if (file.getCodeBase() != null) {
            return file.getCodeBase();
        } else {
            //Fixme: codebase should be the codebase of the Main Jar not
            //the location. Although, it still works in the current state.
            return file.getResources().getMainJAR().getLocation();
        }
    }

    /**
     * Compares a URL using string compareNullableStrings of its protocol, host,
     * port, path, query, and anchor. This method avoids the host name lookup
     * that URL.equals does for http: protocol URLs. It may not return the same
     * value as the URL.equals method (different hostnames that resolve to the
     * same IP address, ie sourceforge.net and www.sourceforge.net).
     *
     * @param u1 first url to compareNullableStrings
     * @param u2 second url to compareNullableStrings
     * @return whether the u1 and u2 points to same resource or not
     */
    public static boolean urlEquals(final URL u1, final URL u2) {
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
            final URL nu1 = UrlUtils.normalizeUrl(u1);
            final URL nu2 = UrlUtils.normalizeUrl(u2);
            if (notNullUrlEquals(nu1, nu2)) {
                return true;
            }
        } catch (Exception ex) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
        }
        return false;
    }
    /**
     * Checks whether <code>url</code> is relative (or equal) to <code>codebaseUrl</code>.
     * both urls are processed by sanitizeLastSlash before actual equals. So
     * protocol://som.url/some/path/ is same as protocol://som.url/some/path. Even
     * protocol://som.url/some/path\ is same as protocol://som.url/some/path/
     *
     * This method returns false in case <code>url</code> contains parent directory notation "..".
     * See JNLP specification version 9, 3.4: 'A relative URL cannot contain parent directory notations, such as "..". It must denote a file that is stored in a subdirectory of the codebase.'
     * @param url the url to check
     * @param codebaseUrl the url to check against
     * @return true if <code>url</code> is relative to <code>codebaseUrl</code>
     */
    public static boolean urlRelativeTo(final URL url, final URL codebaseUrl) {
        if (codebaseUrl == url) {
            return true;
        }
        if (codebaseUrl == null || url == null) {
            return false;
        }
        try {
            final URL sanUrl = sanitizeLastSlash(normalizeUrl(url));
            final URL sanCodebase = sanitizeLastSlash(normalizeUrl(codebaseUrl));
            if (!getHostAndPort(sanUrl).equals(getHostAndPort(sanCodebase))) {
                return false;
            }
            if (!sanUrl.getProtocol().equals(sanCodebase.getProtocol())) {
                return false;
            }
            if (sanUrl.getPath().contains(PARENT_DIR)) {
                return false;
            }
            if (sanUrl.getPath().startsWith(sanCodebase.getPath())) {
                return true;
            }
        } catch (MalformedURLException | UnsupportedEncodingException | URISyntaxException e) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
        }
        return false;
    }

    static boolean notNullUrlEquals(final URL u1, final URL u2) {
        Objects.requireNonNull(u1);
        Objects.requireNonNull(u2);
        return compareNullableStrings(u1.getProtocol(), u2.getProtocol(), true)
                && compareNullableStrings(u1.getHost(), u2.getHost(), true)
                && compareNullableStrings(u1.getPath(), u2.getPath(), false)
                && compareNullableStrings(u1.getQuery(), u2.getQuery(), false)
                && compareNullableStrings(u1.getRef(), u2.getRef(), false);
        // && u1.getPort() ==  u2.getPort(); erroneous?
    }

    /**
     * Compare strings that can be {@code null}.
     *
     * @param s1 first string to compareNullableStrings with s2
     * @param s2 second string to compareNullableStrings with s1
     * @param ignore switch to ignore case
     */
    static boolean compareNullableStrings(final String s1, final String s2, final boolean ignore) {
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

    public static URL ensureSlashTail(final URL u) {
        if (u == null) {
            return null;
        }
        final String s = ensureSlashTail(u.toExternalForm());
        try {
            return new URL(s);
        } catch (MalformedURLException ex) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
            return u;
        }

    }

    public static String ensureSlashTail(final String s) {
        if (s.endsWith(SLASH)) {
            return s;
        }
        if (s.endsWith(BACKSLASH)) {
            return s;
        }
        if (s.contains(SLASH)) {
            return s + SLASH;
        }
        if (s.contains(BACKSLASH)) {
            return s + BACKSLASH;
        }
        return s + SLASH;
    }

    public static String stripFile(final URL documentbase) {
        //when used in generation of regec, the trailing slash is very important
        //see the result between http:/some.url/path.* and http:/some.url/path/.*
        return UrlUtils.ensureSlashTail(stripFileImp(documentbase));
    }

    private static String stripFileImp(final URL documentbase) {
        try {
            final String normalized = UrlUtils.normalizeUrlAndStripParams(documentbase).toExternalForm().trim();
            if (normalized.endsWith(SLASH) || normalized.endsWith(BACKSLASH)) {
                return normalized;
            }
            final URL middleway = new URL(normalized);
            final String file = middleway.getFile();
            int i = Math.max(file.lastIndexOf(SLASH), file.lastIndexOf(BACKSLASH));
            if (i < 0) {
                return normalized;
            }
            final String parent = file.substring(0, i + 1);
            final String stripped = normalized.replace(file, parent);
            return stripped;
        } catch (Exception ex) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
            return documentbase.toExternalForm();
        }

    }

    public static String loadUrl(final URL url) throws IOException {
        return loadUrl(url, StandardCharsets.UTF_8);
    }
    public static String loadUrl(final URL url, final Charset ch) throws IOException {
        final StringBuilder all = new StringBuilder();
        int tries = 0;
        InputStream is = null;
        while (true) {
            URLConnection connection = url.openConnection();
            //from time to time we get
            //java.io.IOException: Invalid Http response, which leads to null is
            //maybe this is happening only with test server, but trying few more times should not harm
            tries++;
            try {
                is = connection.getInputStream();
            } catch (IOException ioe) {
                LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ioe);
                if (connection instanceof HttpURLConnection) {
                    HttpURLConnection httpConn = (HttpURLConnection) connection;
                    int statusCode = httpConn.getResponseCode();
                    if (statusCode != HTTP_STATUS_200) {
                        is = httpConn.getErrorStream();
                    }
                }
            }
            if (tries > 6) {
                throw new IOException("Failed " + url + " on " + tries + " attempts");
            }
            if (is != null) {
                break;
            } else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }
        }
        try (final BufferedReader br = new BufferedReader(new InputStreamReader(is, ch))) {
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                all.append(line).append("\n");
            }
        }

        return all.toString();
    }
    
    
      private static byte[] getRemainingBytes(final InputStream is) throws IOException {
        Objects.requireNonNull(is);
        byte[] buf = new byte[2048];
          final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int len;
        while ((len = is.read(buf)) != -1) {
            bos.write(buf, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }

    public static Object[] loadUrlWithInvalidHeaderBytes(final URL url) throws IOException {
        try (final Socket s = UrlUtils.createSocketFromUrl(url)) {
            writeRequest(s.getOutputStream(), url);
            //StringBuilder do not have endsWith method. Check on that are more expensive then recreations
            String head = new String();
            byte[] body = new byte[0];
            //we cant use bufferedreader, otherwise buffer consume also part of body
            try (InputStream is = s.getInputStream()) {
                while (true) {
                    int readChar = is.read();
                    if (readChar < 0) {
                        break;
                    }
                    head = head + ((char) readChar);
                    if (head.endsWith("\n\n")
                            || head.endsWith("\r\n\r\n")
                            || head.endsWith("\n\r\n\r")
                            || head.endsWith("\r\r")) {
                        body = getRemainingBytes(is);
                    }
                }
            }
            return new Object[]{head, body};
        }
    }
    
    
    public static String[] loadUrlWithInvalidHeader(final URL url) throws IOException {
        return loadUrlWithInvalidHeader(url, StandardCharsets.US_ASCII);
    }
    public static String[] loadUrlWithInvalidHeader(final URL url, final Charset ch) throws IOException {
        try (final Socket s = UrlUtils.createSocketFromUrl(url)) {
            writeRequest(s.getOutputStream(), url);
            final StringBuilder all = new StringBuilder();
            final StringBuilder head = new StringBuilder();
            final StringBuilder body = new StringBuilder();
            try (final BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream(), ch))) {
                StringBuilder second = head;
                while (true) {
                    String line = br.readLine();
                    if (line == null) {
                        break;
                    }
                    all.append(line).append(BACKSLASH_N);
                    if (line.isEmpty()) {
                        second = body;
                    } else {
                        second.append(line).append(BACKSLASH_N);
                    }
                }
            }
            return new String[]{all.toString(), head.toString(), body.toString()};
        }
    }

    private static void writeRequest(final OutputStream s, final URL url) throws IOException {
        final Writer w = new OutputStreamWriter(s, StandardCharsets.US_ASCII);
        String file = url.getFile();
        if (file.isEmpty()) {
            file = "/";
        }
        w.write("GET " + file + " HTTP/1.0\r\n");
        w.write("Host: " + url.getHost() + "\r\n");
        w.write("User-Agent: javaws (icedtea-web)\r\n");
        w.write("Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n");
        w.write("Referer: " + url.toExternalForm() + "\r\n");
        w.write("\r\n");
        
        w.flush();
    }

    private static Socket createSocketFromUrl(final URL url) throws IOException {
        Objects.requireNonNull(url);
        int p = url.getPort();
        if (p < 0) {
            p = url.getDefaultPort();
        }
        Socket s;
        if (url.getProtocol().equals(HTTPS)) {
            s = SSLSocketFactory.getDefault().createSocket(url.getHost(), p);
        } else {
            s = new Socket(url.getHost(), p);
        }

        return s;
    }

}
