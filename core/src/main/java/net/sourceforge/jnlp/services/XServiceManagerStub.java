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

import javax.jnlp.BasicService;
import javax.jnlp.ClipboardService;
import javax.jnlp.DownloadService;
import javax.jnlp.DownloadService2;
import javax.jnlp.ExtendedService;
import javax.jnlp.ExtensionInstallerService;
import javax.jnlp.FileOpenService;
import javax.jnlp.FileSaveService;
import javax.jnlp.PersistenceService;
import javax.jnlp.PrintService;
import javax.jnlp.ServiceManagerStub;
import javax.jnlp.UnavailableServiceException;


/**
 * Lookup table for services.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.6 $
 */
public class XServiceManagerStub implements ServiceManagerStub {

    // todo: only include ExtensionInstallerService if an installer
    // is getting the service, otherwise return null.

    // todo: fix services to do their own privileged actions that
    // run less code in the secure environment (or avoid privileged
    // actions by giving permission to the code source).

    private static String serviceNames[] = {
            "javax.jnlp.BasicService", // required
            "javax.jnlp.DownloadService", // required
            "javax.jnlp.DownloadService2",
            "javax.jnlp.ExtendedService",
            "javax.jnlp.ExtensionInstallerService", // required
            "javax.jnlp.PersistenceService",
            "javax.jnlp.FileOpenService",
            "javax.jnlp.FileSaveService",
            "javax.jnlp.ClipboardService",
            "javax.jnlp.PrintService",
            "javax.jnlp.SingleInstanceService"
    };

    private static Object services[] = {
            ServiceUtil.createPrivilegedProxy(BasicService.class, new XBasicService()),
            ServiceUtil.createPrivilegedProxy(DownloadService.class, new XDownloadService()),
            ServiceUtil.createPrivilegedProxy(DownloadService2.class, new XDownloadService2()),
            ServiceUtil.createPrivilegedProxy(ExtendedService.class, new XExtendedService()),
            ServiceUtil.createPrivilegedProxy(ExtensionInstallerService.class, new XExtensionInstallerService()),
            ServiceUtil.createPrivilegedProxy(PersistenceService.class, new XPersistenceService()),
            ServiceUtil.createPrivilegedProxy(FileOpenService.class, new XFileOpenService()),
            ServiceUtil.createPrivilegedProxy(FileSaveService.class, new XFileSaveService()),
            ServiceUtil.createPrivilegedProxy(ClipboardService.class, new XClipboardService()),
            ServiceUtil.createPrivilegedProxy(PrintService.class, new XPrintService()),
            ServiceUtil.createPrivilegedProxy(ExtendedSingleInstanceService.class, new XSingleInstanceService())
    };

    public XServiceManagerStub() {
    }

    /**
     * Returns the service names.
     */
    public String[] getServiceNames() {
        // make sure it is a copy because we might be returning to
        // code we don't own.
        String result[] = new String[serviceNames.length];
        System.arraycopy(serviceNames, 0, result, 0, serviceNames.length);

        return result;
    }

    /**
     * Returns the service.
     *
     * @throws UnavailableServiceException if service is not available
     */
    public Object lookup(String name) throws UnavailableServiceException {
        // exact match
        for (int i = 0; i < serviceNames.length; i++)
            if (serviceNames[i].equals(name))
                return services[i];

        // substring match
        for (int i = 0; i < serviceNames.length; i++)
            if (-1 != serviceNames[i].indexOf(name))
                return services[i];

        throw new UnavailableServiceException("" + name);
    }

}
