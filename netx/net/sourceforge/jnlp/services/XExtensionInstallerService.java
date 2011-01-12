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

import java.net.*;
import javax.jnlp.*;


/**
 * The ExtensionInstallerService JNLP service.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.6 $
 */
class XExtensionInstallerService implements ExtensionInstallerService {

    protected XExtensionInstallerService() {
    }

    /**
     *
     */
    public URL getExtensionLocation() {
        return null;
    }

    /**
     *
     */
    public String getExtensionVersion() {
        return null;
    }

    /**
     *
     */
    public String getInstalledJRE(java.net.URL url, java.lang.String version) {
        return null;
    }

    /**
     *
     */
    public String getInstallPath() {
        return null;
    }

    /**
     *
     */
    public void hideProgressBar() {
    }

    /**
     *
     */
    public void hideStatusWindow() {
    }

    /**
     *
     */
    public void installFailed() {
    }

    /**
     *
     */
    public void installSucceeded(boolean needsReboot) {
    }

    /**
     *
     */
    public void setHeading(java.lang.String heading) {
    }

    /**
     *
     */
    public void setJREInfo(java.lang.String platformVersion, java.lang.String jrePath) {
    }

    /**
     *
     */
    public void setNativeLibraryInfo(java.lang.String path) {
    }

    /**
     *
     */
    public void setStatus(java.lang.String status) {
    }

    /**
     *
     */
    public void updateProgress(int value) {
    }

}
