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

package net.adoptopenjdk.icedteaweb.resources;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.sourceforge.jnlp.cache.CacheUtil;

import java.io.File;
import java.net.URL;

/**
 * This class tracks the downloading of various resources of a
 * JNLP file to local files in the cache. It can be used to
 * download icons, jnlp and extension files, jars, and jardiff
 * files using the version based protocol or any file using the
 * basic download protocol (jardiff and version not implemented
 * yet).
 * <p>
 * The resource tracker can be configured to prefetch resources,
 * which are downloaded in the order added to the media
 * tracker.
 * </p>
 * <p>
 * Multiple threads are used to download and cache resources that
 * are actively being waited for (blocking a caller) or those that
 * have been started downloading by calling the startDownload
 * method.  Resources that are prefetched are downloaded one at a
 * time and only if no other trackers have requested downloads.
 * This allows the tracker to start downloading many items without
 * using many system resources, but still quickly download items
 * as needed.
 * </p>
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.22 $
 */
public interface ResourceTracker {

    void addResource(URL location, final VersionString version);

    /**
     * Add a resource identified by the specified location and
     * version.  The tracker only downloads one version of a given
     * resource per instance (ie cannot download both versions 1 and
     * 2 of a resource in the same tracker).
     *
     * @param location     the location of the resource
     * @param version      the resource version
     * @param updatePolicy whether to check for updates if already in cache
     */
    void addResource(URL location, final VersionString version, final UpdatePolicy updatePolicy);

    /**
     * Returns a file containing the downloaded resource.  If the
     * resource is non-cacheable then null is returned unless the
     * resource is a local file (the original file is returned).
     * <p>
     * If the resource has not downloaded yet, the method will block
     * until it has been transferred to the cache.
     * </p>
     *
     * @param location the resource location
     * @return a local file containing the resource, or null
     * @throws IllegalResourceDescriptorException if the resource is not being tracked
     * @see CacheUtil#isCacheable
     */
    File getCacheFile(URL location);

    boolean isResourceAdded(URL location);
}
