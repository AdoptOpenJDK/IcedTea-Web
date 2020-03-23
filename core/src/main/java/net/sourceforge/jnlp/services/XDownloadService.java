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
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.resources.cache.Cache;
import net.sourceforge.jnlp.runtime.ApplicationInstance;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.UrlUtils;

import javax.jnlp.DownloadService;
import javax.jnlp.DownloadServiceListener;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import static net.adoptopenjdk.icedteaweb.jnlp.element.security.ApplicationEnvironment.ALL;

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
        return getApplication().getPartsCache().isPartDownloaded(part, new Extension(ref, version));
    }

    private ApplicationInstance getApplication() {
        return JNLPRuntime.getApplication()
                    .orElseThrow(() -> new IllegalStateException("Could not find application."));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isExtensionPartCached(final URL ref, final String version, final String[] parts) {
        return Arrays.stream(parts).allMatch(part -> isExtensionPartCached(ref, version, part));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPartCached(final String part) {
        return getApplication().getPartsCache().isPartDownloaded(part);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPartCached(final String[] parts) {
        return Arrays.stream(parts).allMatch(part -> isPartCached(part));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isResourceCached(final URL ref, final String version) {

        final VersionString resourceVersion = (version == null) ? null : VersionString.fromString(version);
        boolean isAllowedToCheckCache = getApplication().getApplicationEnvironment() == ALL;

        if (!isAllowedToCheckCache) {
            isAllowedToCheckCache = getApplication().getPartsCache().isInAnyPart(ref, resourceVersion);
        }

        if (!isAllowedToCheckCache) {
            final URL codeBase = getApplication().getJNLPFile().getCodeBase();
            isAllowedToCheckCache = UrlUtils.urlRelativeTo(ref, codeBase);
        }

        return isAllowedToCheckCache && Cache.isAnyCached(ref, resourceVersion);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadExtensionPart(final URL ref, final String version, final String[] parts, final DownloadServiceListener progress) throws IOException {
        for (String part : parts) {
            this.loadExtensionPart(ref, version, part, progress);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadExtensionPart(final URL ref, final String version, final String part, final DownloadServiceListener progress) throws IOException {
        getApplication().getPartsCache().downloadPart(part, new Extension(ref, version));
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
