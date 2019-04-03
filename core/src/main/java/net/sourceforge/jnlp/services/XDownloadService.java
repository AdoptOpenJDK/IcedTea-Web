// Copyright (C) 2001-2003 Jon A. Maxwell (JAM)
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

package net.sourceforge.jnlp.services;

import java.io.*;
import java.net.*;
import javax.jnlp.*;

import net.sourceforge.jnlp.JARDesc;
import net.sourceforge.jnlp.Version;
import net.sourceforge.jnlp.cache.CacheUtil;
import net.sourceforge.jnlp.runtime.JNLPClassLoader;
import net.sourceforge.jnlp.runtime.ManageJnlpResources;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

/**
 * The DownloadService JNLP service.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.7 $
 */
class XDownloadService implements DownloadService {

    /**
     * Returns the {@link JNLPClassLoader} of the application
     * @return the {@link JNLPClassLoader} of the application
     */
    JNLPClassLoader getClassLoader() {
        return (JNLPClassLoader) JNLPRuntime.getApplication().getClassLoader();
    }

    /**
     * Returns a listener that will automatically display download
     * progress to the user.
     * @return always {@code null}
     */
    public DownloadServiceListener getDefaultProgressWindow() {
        return null;
    }

    /**
     * Returns whether the part in an extension (specified by the
     * url and version) is cached locally.
     */
    public boolean isExtensionPartCached(URL ref, String version, String part) {
        boolean allCached = true;
        Version resourceVersion = (version == null) ? null : new Version(version);

        JARDesc[] jars = ManageJnlpResources.findJars(this.getClassLoader(), ref, part, resourceVersion);

        if (jars.length <= 0)
            return false;

        for (int i = 0; i < jars.length && allCached; i++) {
            allCached = CacheUtil.isCached(jars[i].getLocation(), resourceVersion);
        }

        return allCached;
    }

    /**
     * Returns whether the parts in an extension (specified by the
     * url and version) are cached locally.
     */
    public boolean isExtensionPartCached(URL ref, String version, String[] parts) {
        boolean allCached = true;
        if (parts.length <= 0)
            return false;

        for (String eachPart : parts)
            allCached = this.isExtensionPartCached(ref, version, eachPart);

        return allCached;
    }

    /**
     * Returns whether the part of the calling application is cached
     * locally.  If called by code specified by an extension
     * descriptor, the specified part refers to the extension not
     * the application.
     */
    public boolean isPartCached(String part) {
        boolean allCached = true;
        JARDesc[] jars = ManageJnlpResources.findJars(this.getClassLoader(), null, part, null);

        if (jars.length <= 0)
            return false;

        for (int i = 0; i < jars.length && allCached; i++) {
            allCached = CacheUtil.isCached(jars[i].getLocation(), null);
        }

        return allCached;
    }

    /**
     * Returns whether all of the parts of the calling application
     * are cached locally.  If called by code in an extension, the
     * part refers the the part of the extension not the
     * application.
     */
    public boolean isPartCached(String[] parts) {
        boolean allCached = true;
        if (parts.length <= 0)
            return false;

        for (String eachPart : parts)
            allCached = this.isPartCached(eachPart);

        return allCached;
    }

    /**
     * Returns whether the resource is cached locally.  This method
     * only returns true if the resource is specified by the calling
     * application or extension.
     */
    public boolean isResourceCached(URL ref, String version) {
        return ManageJnlpResources.isExternalResourceCached(this.getClassLoader(), ref, version);
    }

    /**
     * Downloads the parts of an extension.
     *
     * @throws IOException
     */
    public void loadExtensionPart(URL ref, String version, String[] parts, DownloadServiceListener progress) throws IOException {
        for (String eachPart : parts)
            this.loadExtensionPart(ref, version, eachPart, progress);
    }

    /**
     * Downloads a part of an extension.
     *
     * @throws IOException
     */
    public void loadExtensionPart(URL ref, String version, String part, DownloadServiceListener progress) throws IOException {
        Version resourceVersion = (version == null) ? null : new Version(version);
        ManageJnlpResources.downloadJars(this.getClassLoader(), ref, part, resourceVersion);
    }

    /**
     * Downloads the parts.
     *
     * @throws IOException
     */
    public void loadPart(String[] parts, DownloadServiceListener progress) throws IOException {
        for (String eachPart : parts)
            this.loadPart(eachPart, progress);
    }

    /**
     * Downloads the part.
     *
     * @throws IOException
     */
    public void loadPart(String part, DownloadServiceListener progress) throws IOException {
        ManageJnlpResources.downloadJars(this.getClassLoader(), null, part, null);
    }

    /**
     * Downloads a resource.
     *
     * @throws IOException
     */
    public void loadResource(URL ref, String version, DownloadServiceListener progress) throws IOException {
        ManageJnlpResources.loadExternalResouceToCache(this.getClassLoader(), ref, version);
    }

    /**
     * Notify the system that an extension's part is no longer
     * important to cache.
     *
     * @throws IOException
     */
    public void removeExtensionPart(URL ref, String version, String part) throws IOException {
        Version resourceVersion = (version == null) ? null : new Version(version);
        JARDesc[] jars = ManageJnlpResources.findJars(this.getClassLoader(), ref, part, resourceVersion);
        ManageJnlpResources.removeCachedJars(this.getClassLoader(), ref, jars);
    }

    /**
     * Notify the system that an extension's parts are no longer
     * important to cache.
     *
     * @throws IOException
     */
    public void removeExtensionPart(URL ref, String version, String[] parts) throws IOException {
        for (String eachPart : parts)
            this.removeExtensionPart(ref, version, eachPart);
    }

    /**
     * Notifies the system that a part  is no longer important to
     * cache.
     *
     * @throws IOException
     */
    public void removePart(String part) throws IOException {
        JARDesc[] jars = ManageJnlpResources.findJars(this.getClassLoader(), null, part, null);
        ManageJnlpResources.removeCachedJars(this.getClassLoader(), null, jars);
    }

    /**
     * Notifies the system that the parts  is no longer important to
     * cache.
     *
     * @throws IOException
     */
    public void removePart(String[] parts) throws IOException {
        for (String eachPart : parts)
            this.removePart(eachPart);
    }

    /**
     * Notifies the system that the resource is no longer important
     * to cache.
     *
     * @throws IOException
     */
    public void removeResource(URL ref, String version) throws IOException {
        ManageJnlpResources.removeExternalCachedResource(this.getClassLoader(), ref, version);
    }

}
