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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.jnlp.DownloadOptions;

public class ResourceUrlCreator {

    protected final Resource resource;
    protected final DownloadOptions downloadOptions;

    public ResourceUrlCreator(Resource resource, DownloadOptions downloadOptions) {
        this.resource = resource;
        this.downloadOptions = downloadOptions;
    }

    /**
     * Returns a list of URLs that the resources might be downloadable from.
     * The Resources may not be downloadable from any of them. The returned order is the order
     * the urls should be attempted in.
     * @return a list of URLs that the resources might be downloadable from
     */
    public List<URL> getUrls() {
        List<URL> urls = new LinkedList<>();
        URL url;

        if (downloadOptions.useExplicitPack() && downloadOptions.useExplicitVersion()) {
            url = getUrl(resource, true, true);
            if (url != null) {
                urls.add(url);
            }
            url = getUrl(resource, false, true);
            if (url != null) {
                urls.add(url);
            }
            url = getUrl(resource, true, false);
            if (url != null) {
                urls.add(url);
            }
        } else if (downloadOptions.useExplicitPack()) {
            url = getUrl(resource, true, false);
            if (url != null) {
                urls.add(url);
            }
        } else if (downloadOptions.useExplicitVersion()) {
            url = getUrl(resource, false, true);
            if (url != null) {
                urls.add(url);
            }
        }

        url = getVersionedUrl();
        urls.add(url);

        urls.add(resource.getLocation());

        return urls;
    }

    /**
     * Returns a url for the resource.
     * @param resource the resource
     * @param usePack whether the URL should point to the pack200 file
     * @param useVersion whether the URL should be modified to include the version
     * @return a URL for the resource or null if an appropriate URL can not be found
     */
    static URL getUrl(Resource resource, boolean usePack, boolean useVersion) {
        if (!(usePack || useVersion)) {
            throw new IllegalArgumentException("either pack200 or version required");
        }

        String location = resource.getLocation().toString();
        int lastSlash = resource.getLocation().toString().lastIndexOf('/');
        if (lastSlash == -1) {
            return resource.getLocation();
        }
        String filename = location.substring(lastSlash + 1);
        if (useVersion && resource.getRequestVersion() != null) {
            // With 'useVersion', j2-commons-cli.jar becomes, for example, j2-commons-cli__V1.0.jar
            String parts[] = filename.split("\\.", -1 /* Keep blank strings*/);

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < parts.length; i++) {
                sb.append(parts[i]);
                // Append __V<number> before last '.'
                if (i == parts.length -2) {
                    sb.append("__V").append(resource.getRequestVersion());
                }
                sb.append('.');
            }
            sb.setLength(sb.length() - 1); // remove last '.'

            filename = sb.toString();
        }
        if (usePack) {
            filename = filename + ".pack.gz";
        }

        location = location.substring(0, lastSlash + 1) + filename;
        try {
            URL newUrl = new URL(location);
            return newUrl;
        } catch (MalformedURLException e) {
            return null;
        }
    }

    /**
     * Returns the URL for this resource, including the resource's version number in the query string
     * @return  url with version cared about
     */
    protected URL getVersionedUrl() {
        URL resourceUrl = resource.getLocation();
        String protocol = uriPartToString(resourceUrl.getProtocol()) + "://";
        String userInfo = uriPartToString(resourceUrl.getUserInfo());
        if (!userInfo.isEmpty()) {
            userInfo += "@";
        }
        String host = uriPartToString(resourceUrl.getHost());
        String port;
        if (resourceUrl.getPort() == -1) {
            port = "";
        } else {
            port = ":" + String.valueOf(resourceUrl.getPort());
        }
        String path = uriPartToString(resourceUrl.getPath());
        String query = uriPartToString(resourceUrl.getQuery());
        if (!query.isEmpty()) {
            query = "?" + query;
        }
        if (resource.getRequestVersion() != null && resource.getRequestVersion().isVersionId()) {
            if (!query.isEmpty()) {
                query += "&";
            } else {
                query = "?" + query;
            }
            query += "version-id=" + resource.getRequestVersion();
        }
        try {
            URL url = new URL(protocol + userInfo + host + port + path + query);
            return url;
        } catch (MalformedURLException e) {
            return resourceUrl;
        }
    }

    private static String uriPartToString(String part) {
        if (part == null)
            return "";
        return part;
    }

}
