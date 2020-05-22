/* ResourceUrlCreator.java
   Copyright (C) 2011 Red Hat, Inc

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify it under the
terms of the GNU General Public License as published by the Free Software
Foundation, version 2.

IcedTea is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
IcedTea; see the file COPYING. If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is making a
combined work based on this library. Thus, the terms and conditions of the GNU
General Public License cover the whole combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent modules, and
to copy and distribute the resulting executable under terms of your choice,
provided that you also meet, for each linked independent module, the terms and
conditions of the license of that module. An independent module is a module
which is not derived from or based on this library. If you modify this library,
you may extend this exception to your version of the library, but you are not
obligated to do so. If you do not wish to do so, delete this exception
statement from your version. */
package net.adoptopenjdk.icedteaweb.resources.initializer;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.resources.Resource;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static net.adoptopenjdk.icedteaweb.StringUtils.isBlank;
import static net.adoptopenjdk.icedteaweb.StringUtils.urlEncode;
import static net.adoptopenjdk.icedteaweb.resources.JnlpDownloadProtocolConstants.CURRENT_VERSION_ID_QUERY_PARAM;
import static net.adoptopenjdk.icedteaweb.resources.JnlpDownloadProtocolConstants.VERSION_ID_QUERY_PARAM;
import static net.adoptopenjdk.icedteaweb.resources.JnlpDownloadProtocolConstants.VERSION_PREFIX;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_HTTPS_DONT_ENFORCE;
import static net.sourceforge.jnlp.runtime.JNLPRuntime.getConfiguration;
import static net.sourceforge.jnlp.util.UrlUtils.HTTPS_PROTOCOL;
import static net.sourceforge.jnlp.util.UrlUtils.HTTP_PROTOCOL;

class ResourceUrlCreator {
    private static final Logger LOG = LoggerFactory.getLogger(ResourceUrlCreator.class);

    static List<URL> prependHttps(List<URL> urls) {
        final List<URL> result = new ArrayList<>();
        final boolean noHttpsPreferred = Boolean.parseBoolean(getConfiguration().getProperty(KEY_HTTPS_DONT_ENFORCE));
        if (!noHttpsPreferred) {
            //preferring https and  overriding case, when application was moved to https, but the jnlp stayed intact
            for (final URL url : urls) {
                if (url.getProtocol().equals(HTTP_PROTOCOL) && url.getPort() < 0) { // port < 0 means default port
                    try {
                        result.add(new URL(HTTPS_PROTOCOL + url.toExternalForm().substring(HTTP_PROTOCOL.length())));
                    } catch (MalformedURLException ex) {
                        LOG.error("Error while creating HTTPS URL from '" + url + "'", ex);
                    }
                }
            }
        }

        result.addAll(urls);
        return result;
    }

    /**
     * Returns a url for the resource.
     *
     * @param resource   the resource
     * @param usePack    whether the URL should point to the pack200 file
     * @param useVersion whether the URL should be modified to include the
     *                   version
     * @return a URL for the resource or null if an appropriate URL can not be
     * found
     */
    static URL getUrl(final Resource resource, final boolean usePack, final boolean useVersion) {
        final boolean appendVersionToFileName = useVersion && resource.getRequestVersion() != null;

        if (!usePack && !appendVersionToFileName) {
            return null;
        }

        final String url = resource.getLocation().toExternalForm();
        final int firstQuestionmark = url.indexOf('?');

        final String location;
        final String query;
        if (firstQuestionmark < 0) {
            location = url;
            query = "";
        } else {
            location = url.substring(0, firstQuestionmark);
            query = url.substring(firstQuestionmark);
        }

        final int lastSlash = location.lastIndexOf('/');
        if (lastSlash == -1) {
            return null;
        }
        final String filename = location.substring(lastSlash + 1);

        final String filenameWithVersion;
        if (appendVersionToFileName) {
            // With 'useVersion', j2-commons-cli.jar becomes, for example, j2-commons-cli__V1.0.jar
            final String[] parts = filename.split("\\.", -1 /* Keep blank strings*/);

            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < parts.length; i++) {
                sb.append(parts[i]);
                // Append __V<number> before last '.'
                if (i == parts.length - 2) {
                    sb.append(VERSION_PREFIX).append(resource.getRequestVersion());
                }
                sb.append('.');
            }
            sb.setLength(sb.length() - 1); // remove last '.'

            filenameWithVersion = sb.toString();
        } else {
            filenameWithVersion = filename;
        }

        final String filenameWithVersionAndEnding;
        if (usePack) {
            filenameWithVersionAndEnding = filenameWithVersion + ".pack.gz";
        } else {
            filenameWithVersionAndEnding = filenameWithVersion;
        }

        final String urlLocation = location.substring(0, lastSlash + 1) + filenameWithVersionAndEnding + query;
        try {
            return new URL(urlLocation);
        } catch (MalformedURLException e) {
            LOG.warn("Could not create versioned URL for {} and {} - Reason: {}", resource, e.getMessage());
            return null;
        }
    }

    /**
     * Returns the URL for this resource, including the resource's version
     * number in the query string
     *
     * @return url with version cared about
     */
    static URL getVersionedUrl(URL resourceUrl, VersionString requestVersion, VersionId currentVersion) {
        if (resourceUrl == null || requestVersion == null) {
            return null;
        }

        final String protocol = emptyIfNull(resourceUrl.getProtocol()) + "://";
        final String userInfoPart = emptyIfNull(resourceUrl.getUserInfo());
        final String userInfo = !userInfoPart.isEmpty() ? userInfoPart + "@" : userInfoPart;
        final String host = emptyIfNull(resourceUrl.getHost());
        final String port = resourceUrl.getPort() < 0 ? "" : ":" + resourceUrl.getPort();
        final String path = emptyIfNull(resourceUrl.getPath());

        final List<String> queryParts = Arrays.stream(emptyIfNull(resourceUrl.getQuery()).split("&"))
                .filter(s -> !isBlank(s))
                .filter(s -> !s.startsWith(VERSION_ID_QUERY_PARAM + "="))
                .filter(s -> !s.startsWith(CURRENT_VERSION_ID_QUERY_PARAM + "="))
                .collect(Collectors.toList());
        queryParts.add(VERSION_ID_QUERY_PARAM + "=" + urlEncode(requestVersion.toString()));
        if (currentVersion != null) {
            queryParts.add(CURRENT_VERSION_ID_QUERY_PARAM + "=" + urlEncode(currentVersion.toString()));
        }
        final String query = queryParts.isEmpty() ? "" : "?" + String.join("&", queryParts);

        try {
            return new URL(protocol + userInfo + host + port + path + query);
        } catch (MalformedURLException e) {
            LOG.warn("Could not create versioned URL for {} and {} - Reason: {}", resourceUrl, requestVersion, e.getMessage());
            return null;
        }
    }

    private static String emptyIfNull(final String part) {
        return part == null ? "" : part;
    }
}
