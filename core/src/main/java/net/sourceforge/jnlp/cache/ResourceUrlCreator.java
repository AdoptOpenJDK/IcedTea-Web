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
package net.sourceforge.jnlp.cache;

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.StringUtils;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.DownloadOptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_HTTPS_DONT_ENFORCE;
import static net.sourceforge.jnlp.runtime.JNLPRuntime.getConfiguration;

public class ResourceUrlCreator {
    private static final Logger LOG = LoggerFactory.getLogger(ResourceUrlCreator.class);

    /**
     * Returns a list of URLs that the resources might be downloadable from. The
     * Resources may not be downloadable from any of them. The returned order is
     * the order the urls should be attempted in.
     *
     * @return a list of URLs that the resources might be downloadable from
     */
    static List<URL> getUrls(final Resource resource, final DownloadOptions downloadOptions) {
        final List<URL> urls = new LinkedList<>();

        if (downloadOptions.useExplicitPack() && downloadOptions.useExplicitVersion()) {
            final URL url = getUrl(resource, true, true);
            if (url != null) {
                urls.add(url);
            }
        }
        if (downloadOptions.useExplicitVersion()) {
            final URL url = getUrl(resource, false, true);
            if (url != null) {
                urls.add(url);
            }
        }
        if (downloadOptions.useExplicitPack()) {
            final URL url = getUrl(resource, true, false);
            if (url != null) {
                urls.add(url);
            }
        }

        urls.add(getVersionedUrl(resource));
        urls.add(resource.getLocation());

        final List<URL> result = new ArrayList<>();

        final boolean noHttpsPreferred = Boolean.parseBoolean(getConfiguration().getProperty(KEY_HTTPS_DONT_ENFORCE));
        if (!noHttpsPreferred) {
            //preferring https and  overriding case, when application was moved to https, but the jnlp stayed intact
            for (final URL url : urls) {
                if (url.getProtocol().equals("http") && url.getPort() < 0) { // port < 0 means default port
                    try {
                        result.add(0, new URL("https", url.getHost(), url.getFile()));
                    } catch (Exception ex) {
                        LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
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
        if (!(usePack || useVersion)) {
            throw new IllegalArgumentException("either pack200 or version required");
        }

        final String location = resource.getLocation().toString();
        final int lastSlash = resource.getLocation().toString().lastIndexOf('/');
        if (lastSlash == -1) {
            return resource.getLocation();
        }
        final String filename = location.substring(lastSlash + 1);

        final String filenameWithVersion;
        if (useVersion && resource.getRequestVersion() != null) {
            // With 'useVersion', j2-commons-cli.jar becomes, for example, j2-commons-cli__V1.0.jar
            final String[] parts = filename.split("\\.", -1 /* Keep blank strings*/);

            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < parts.length; i++) {
                sb.append(parts[i]);
                // Append __V<number> before last '.'
                if (i == parts.length - 2) {
                    sb.append("__V").append(resource.getRequestVersion());
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

        final String urlLocation = location.substring(0, lastSlash + 1) + filenameWithVersionAndEnding;
        try {
            return new URL(urlLocation);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    /**
     * Returns the URL for this resource, including the resource's version
     * number in the query string
     *
     * @return url with version cared about
     */
    static URL getVersionedUrl(final Resource resource) {
        final URL resourceUrl = resource.getLocation();
        final String protocol = emptyIfNull(resourceUrl.getProtocol()) + "://";
        final String userInfoPart = emptyIfNull(resourceUrl.getUserInfo());
        final String userInfo = !userInfoPart.isEmpty() ? userInfoPart + "@" : userInfoPart;
        final String host = emptyIfNull(resourceUrl.getHost());
        final String port = resourceUrl.getPort() < 0 ? "" : ":" + resourceUrl.getPort();
        final String path = emptyIfNull(resourceUrl.getPath());

        final List<String> queryParts = Arrays.stream(emptyIfNull(resourceUrl.getQuery()).split("&"))
                .filter(s -> !StringUtils.isBlank(s))
                .collect(Collectors.toList());
        if (resource.getRequestVersion() != null && resource.getRequestVersion().containsSingleVersionId()) {
            queryParts.add("version-id=" + resource.getRequestVersion());
        }
        final String query = queryParts.isEmpty() ? "" : "?" + String.join("&", queryParts);

        try {
            return new URL(protocol + userInfo + host + port + path + query);
        } catch (MalformedURLException e) {
            return resourceUrl;
        }
    }

    private static String emptyIfNull(final String part) {
        return part == null ? "" : part;
    }
}
