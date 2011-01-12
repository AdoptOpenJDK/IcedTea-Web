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

/**
 * The DownloadService JNLP service.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.7 $
 */
class XDownloadService implements DownloadService {

    protected XDownloadService() {
    }

    // comments copied from DownloadService interface

    /**
     * Returns a listener that will automatically display download
     * progress to the user.
     */
    public DownloadServiceListener getDefaultProgressWindow() {
        return null;
    }

    /**
     * Returns whether the part in an extension (specified by the
     * url and version) is cached locally.
     */
    public boolean isExtensionPartCached(URL ref, String version, String part) {
        return true;
    }

    /**
     * Returns whether the parts in an extension (specified by the
     * url and version) are cached locally.
     */
    public boolean isExtensionPartCached(URL ref, String version, String[] parts) {
        return true;
    }

    /**
     * Returns whether the part of the calling application is cached
     * locally.  If called by code specified by an extension
     * descriptor, the specified part refers to the extension not
     * the application.
     */
    public boolean isPartCached(String part) {
        return true;
    }

    /**
     * Returns whether all of the parts of the calling application
     * are cached locally.  If called by code in an extension, the
     * part refers the the part of the extension not the
     * application.
     */
    public boolean isPartCached(String[] parts) {
        return true;
    }

    /**
     * Returns whether the resource is cached locally.  This method
     * only returns true if the resource is specified by the calling
     * application or extension.
     */
    public boolean isResourceCached(URL ref, String version) {
        return true;
    }

    /**
     * Downloads the parts of an extension.
     *
     * @throws IOException
     */
    public void loadExtensionPart(URL ref, String version, String[] parts, DownloadServiceListener progress) throws IOException {
    }

    /**
     * Downloads a part of an extension.
     *
     * @throws IOException
     */
    public void loadExtensionPart(URL ref, String version, String part, DownloadServiceListener progress) throws IOException {
    }

    /**
     * Downloads the parts.
     *
     * @throws IOException
     */
    public void loadPart(String[] parts, DownloadServiceListener progress) throws IOException {
    }

    /**
     * Downloads the part.
     *
     * @throws IOException
     */
    public void loadPart(String part, DownloadServiceListener progress) throws IOException {
    }

    /**
     * Downloads a resource.
     *
     * @throws IOException
     */
    public void loadResource(URL ref, String version, DownloadServiceListener progress) throws IOException {
    }

    /**
     * Notify the system that an extension's part is no longer
     * important to cache.
     *
     * @throws IOException
     */
    public void removeExtensionPart(URL ref, String version, String part) throws IOException {
    }

    /**
     * Notify the system that an extension's parts are no longer
     * important to cache.
     *
     * @throws IOException
     */
    public void removeExtensionPart(URL ref, String version, String[] parts) throws IOException {
    }

    /**
     * Notifies the system that a part  is no longer important to
     * cache.
     *
     * @throws IOException
     */
    public void removePart(String part) throws IOException {
    }

    /**
     * Notifies the system that the parts  is no longer important to
     * cache.
     *
     * @throws IOException
     */
    public void removePart(String[] parts) throws IOException {
    }

    /**
     * Notifies the system that the resource is no longer important
     * to cache.
     *
     * @throws IOException
     */
    public void removeResource(URL ref, String version) throws IOException {
    }

}
