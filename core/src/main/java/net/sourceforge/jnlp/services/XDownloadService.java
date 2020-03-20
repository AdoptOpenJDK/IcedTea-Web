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

import net.adoptopenjdk.icedteaweb.classloader.Extension;
import net.sourceforge.jnlp.runtime.ApplicationInstance;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

import javax.jnlp.DownloadService;
import javax.jnlp.DownloadServiceListener;
import java.io.IOException;
import java.net.URL;

/**
 * The {@link DownloadService} service allows an application to control how its own resources are cached.
 *
 * @implSpec See <b>JSR-56, Section 7.2 The DownloadService Service</b> for a details.
 */
class XDownloadService implements DownloadService {

    /**
     * Returns a listener that will automatically display download
     * progress to the user.
     *
     * @return always {@code null}
     */
    public DownloadServiceListener getDefaultProgressWindow() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isExtensionPartCached(final URL ref, final String version, final String part) {
        final ApplicationInstance applicationInstance = getApplication();

        return applicationInstance.getPartsCache().isPartDownloaded(part, new Extension(ref, version));
    }

    private ApplicationInstance getApplication() {
        return JNLPRuntime.getApplication()
                    .orElseThrow(() -> new IllegalStateException("Could not find application."));
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
        throw new RuntimeException("Not implemented yet!");
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
        throw new RuntimeException("Not implemented yet!");
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
        throw new RuntimeException("Not implemented yet!");
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
        throw new RuntimeException("Not implemented yet!");
    }

    /**
     * Downloads a resource.
     *
     * @throws IOException
     */
    @Override
    public void loadResource(final URL ref, final String version, final DownloadServiceListener progress) throws IOException {
        throw new RuntimeException("Not implemented yet!");
    }

    /**
     * Notify the system that an extension's part is no longer
     * important to cache.
     *
     * @throws IOException
     */
    @Override
    public void removeExtensionPart(final URL ref, final String version, final String part) throws IOException {
        throw new RuntimeException("Not implemented yet!");
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
        throw new RuntimeException("Not implemented yet!");
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
        throw new RuntimeException("Not implemented yet!");
    }
}
