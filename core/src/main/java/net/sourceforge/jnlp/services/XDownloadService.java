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

import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.resources.cache.Cache;
import net.sourceforge.jnlp.runtime.classloader.JNLPClassLoader;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.runtime.classloader.ManageJnlpResources;

import javax.jnlp.DownloadService;
import javax.jnlp.DownloadServiceListener;
import java.io.IOException;
import java.net.URL;

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
    private ClassLoader getApplicationClassLoader() {
        return JNLPRuntime.getApplication().getClassLoader();
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
    @Override
    public boolean isExtensionPartCached(final URL ref, final String version, final String part) {
        boolean allCached = true;
        final VersionString resourceVersion = (version == null) ? null : VersionString.fromString(version);

        final JARDesc[] jars = ManageJnlpResources.findJars(this.getApplicationClassLoader(), ref, part, resourceVersion);

        if (jars.length <= 0)
            return false;

        for (int i = 0; i < jars.length && allCached; i++) {
            allCached = Cache.isAnyCached(jars[i].getLocation(), resourceVersion);
        }

        return allCached;
    }

    /**
     * Returns whether the parts in an extension (specified by the
     * url and version) are cached locally.
     */
    @Override
    public boolean isExtensionPartCached(final URL ref, final String version, final String[] parts) {
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
    @Override
    public boolean isPartCached(final String part) {
        boolean allCached = true;
        final JARDesc[] jars = ManageJnlpResources.findJars(this.getApplicationClassLoader(), null, part, null);

        if (jars.length <= 0)
            return false;

        for (int i = 0; i < jars.length && allCached; i++) {
            allCached = Cache.isAnyCached(jars[i].getLocation(), jars[i].getVersion());
        }

        return allCached;
    }

    /**
     * Returns whether all of the parts of the calling application
     * are cached locally.  If called by code in an extension, the
     * part refers the the part of the extension not the
     * application.
     */
    @Override
    public boolean isPartCached(final String[] parts) {
        boolean allCached = true;
        if (parts.length <= 0)
            return false;

        for (final String eachPart : parts)
            allCached = this.isPartCached(eachPart);

        return allCached;
    }

    /**
     * Returns whether the resource is cached locally.  This method
     * only returns true if the resource is specified by the calling
     * application or extension.
     */
    @Override
    public boolean isResourceCached(final URL ref, final String version) {
        return ManageJnlpResources.isExternalResourceCached(this.getApplicationClassLoader(), ref, version);
    }

    /**
     * Downloads the parts of an extension.
     *
     * @throws IOException
     */
    @Override
    public void loadExtensionPart(final URL ref, final String version, final String[] parts, final DownloadServiceListener progress) throws IOException {
        for (final String eachPart : parts)
            this.loadExtensionPart(ref, version, eachPart, progress);
    }

    /**
     * Downloads a part of an extension.
     *
     * @throws IOException
     */
    @Override
    public void loadExtensionPart(final URL ref, final String version, final String part, final DownloadServiceListener progress) throws IOException {
        final VersionString resourceVersion = (version == null) ? null : VersionString.fromString(version);
        ManageJnlpResources.downloadJars(this.getApplicationClassLoader(), ref, part, resourceVersion);
    }

    /**
     * Downloads the parts.
     *
     * @throws IOException
     */
    @Override
    public void loadPart(final String[] parts, final DownloadServiceListener progress) throws IOException {
        for (String eachPart : parts)
            this.loadPart(eachPart, progress);
    }

    /**
     * Downloads the part.
     *
     * @throws IOException
     */
    @Override
    public void loadPart(final String part, final DownloadServiceListener progress) throws IOException {
        ManageJnlpResources.downloadJars(this.getApplicationClassLoader(), null, part, null);
    }

    /**
     * Downloads a resource.
     *
     * @throws IOException
     */
    @Override
    public void loadResource(final URL ref, final String version, final DownloadServiceListener progress) throws IOException {
        ManageJnlpResources.loadExternalResourceToCache(this.getApplicationClassLoader(), ref, version);
    }

    /**
     * Notify the system that an extension's part is no longer
     * important to cache.
     *
     * @throws IOException
     */
    @Override
    public void removeExtensionPart(final URL ref, final String version, final String part) throws IOException {
        final VersionString resourceVersion = (version == null) ? null : VersionString.fromString(version);
        final JARDesc[] jars = ManageJnlpResources.findJars(this.getApplicationClassLoader(), ref, part, resourceVersion);
        ManageJnlpResources.removeCachedJars(this.getApplicationClassLoader(), ref, jars);
    }

    /**
     * Notify the system that an extension's parts are no longer
     * important to cache.
     *
     * @throws IOException
     */
    @Override
    public void removeExtensionPart(final URL ref, final String version, final String[] parts) throws IOException {
        for (String eachPart : parts)
            this.removeExtensionPart(ref, version, eachPart);
    }

    /**
     * Notifies the system that a part  is no longer important to
     * cache.
     *
     * @throws IOException
     */
    @Override
    public void removePart(final String part) throws IOException {
        final JARDesc[] jars = ManageJnlpResources.findJars(this.getApplicationClassLoader(), null, part, null);
        ManageJnlpResources.removeCachedJars(this.getApplicationClassLoader(), null, jars);
    }

    /**
     * Notifies the system that the parts  is no longer important to
     * cache.
     *
     * @throws IOException
     */
    @Override
    public void removePart(final String[] parts) throws IOException {
        for (String eachPart : parts)
            this.removePart(eachPart);
    }

    /**
     * Notifies the system that the resource is no longer important
     * to cache.
     *
     * @throws IOException
     */
    @Override
    public void removeResource(final URL ref, final String version) throws IOException {
        ManageJnlpResources.removeExternalCachedResource(this.getApplicationClassLoader(), ref, version);
    }
}
