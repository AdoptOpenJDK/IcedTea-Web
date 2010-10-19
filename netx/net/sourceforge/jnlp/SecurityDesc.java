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


package net.sourceforge.jnlp;

import java.io.*;
import java.net.*;
import java.util.*;
import java.security.*;
import java.awt.AWTPermission;

/**
 * The security element.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.7 $
 */
public class SecurityDesc {

    /*
     * We do not verify security here, the classloader deals with security
     */

    /** All permissions. */
    public static final Object ALL_PERMISSIONS = "All";

    /** Applet permissions. */
    public static final Object SANDBOX_PERMISSIONS = "Sandbox";

    /** J2EE permissions. */
    public static final Object J2EE_PERMISSIONS = "J2SE";

    /** permissions type */
    private Object type;

    /** the download host */
    private String downloadHost;

    /** the JNLP file */
    private JNLPFile file;

    // We go by the rules here:
    // http://java.sun.com/docs/books/tutorial/deployment/doingMoreWithRIA/properties.html

    // Since this is security sensitive, take a conservative approach:
    // Allow only what is specifically allowed, and deny everything else

    /** basic permissions for restricted mode */
    private static Permission j2eePermissions[] = {
        new AWTPermission("accessClipboard"),
        // disabled because we can't at this time prevent an
        // application from accessing other applications' event
        // queues, or even prevent access to security dialog queues.
        //
        // new AWTPermission("accessEventQueue"),
        new AWTPermission("showWindowWithoutWarningBanner"),
        new RuntimePermission("exitVM"),
        new RuntimePermission("loadLibrary"),
        new RuntimePermission("queuePrintJob"),
        new SocketPermission("*", "connect"),
        new SocketPermission("localhost:1024-", "accept, listen"),
        new FilePermission("*", "read, write"),
        new PropertyPermission("*", "read"),
    };

    /** basic permissions for restricted mode */
    private static Permission sandboxPermissions[] = {
        new SocketPermission("localhost:1024-", "listen"),
        // new SocketPermission("<DownloadHost>", "connect, accept"), // added by code
        new PropertyPermission("java.version", "read"),
        new PropertyPermission("java.vendor", "read"),
        new PropertyPermission("java.vendor.url", "read"),
        new PropertyPermission("java.class.version", "read"),
        new PropertyPermission("os.name", "read"),
        new PropertyPermission("os.version", "read"),
        new PropertyPermission("os.arch", "read"),
        new PropertyPermission("file.separator", "read"),
        new PropertyPermission("path.separator", "read"),
        new PropertyPermission("line.separator", "read"),
        new PropertyPermission("java.specification.version", "read"),
        new PropertyPermission("java.specification.vendor", "read"),
        new PropertyPermission("java.specification.name", "read"),
        new PropertyPermission("java.vm.specification.vendor", "read"),
        new PropertyPermission("java.vm.specification.name", "read"),
        new PropertyPermission("java.vm.version", "read"),
        new PropertyPermission("java.vm.vendor", "read"),
        new PropertyPermission("java.vm.name", "read"),
        new PropertyPermission("javawebstart.version", "read"),
        new PropertyPermission("javaplugin.*", "read"),
        new PropertyPermission("jnlp.*", "read,write"),
        new PropertyPermission("javaws.*", "read,write"),
        new RuntimePermission("exitVM"),
        new RuntimePermission("stopThread"),
        new AWTPermission("showWindowWithoutWarningBanner"),
        // disabled because we can't at this time prevent an
        // application from accessing other applications' event
        // queues, or even prevent access to security dialog queues.
        //
        // new AWTPermission("accessEventQueue"),
    };

    /** basic permissions for restricted mode */
    private static Permission jnlpRIAPermissions[] = {
        new PropertyPermission("awt.useSystemAAFontSettings", "read,write"),
        new PropertyPermission("http.agent", "read,write"),
        new PropertyPermission("http.keepAlive", "read,write"),
        new PropertyPermission("java.awt.syncLWRequests", "read,write"),
        new PropertyPermission("java.awt.Window.locationByPlatform", "read,write"),
        new PropertyPermission("javaws.cfg.jauthenticator", "read,write"),
        new PropertyPermission("javax.swing.defaultlf", "read,write"),
        new PropertyPermission("sun.awt.noerasebackground", "read,write"),
        new PropertyPermission("sun.awt.erasebackgroundonresize", "read,write"),
        new PropertyPermission("sun.java2d.d3d", "read,write"),
        new PropertyPermission("sun.java2d.dpiaware", "read,write"),
        new PropertyPermission("sun.java2d.noddraw", "read,write"),
        new PropertyPermission("sun.java2d.opengl", "read,write"),
        new PropertyPermission("swing.boldMetal", "read,write"),
        new PropertyPermission("swing.metalTheme", "read,write"),
        new PropertyPermission("swing.noxp", "read,write"),
        new PropertyPermission("swing.useSystemFontSettings", "read,write"),
    };

    /**
     * Create a security descriptor.
     *
     * @param file the JNLP file
     * @param type the type of security
     * @param downloadHost the download host (can always connect to)
     */
    public SecurityDesc(JNLPFile file, Object type, String downloadHost) {
        this.file = file;
        this.type = type;
        this.downloadHost = downloadHost;
    }

    /**
     * Returns the permissions type, one of: ALL_PERMISSIONS,
     * SANDBOX_PERMISSIONS, J2EE_PERMISSIONS.
     */
    public Object getSecurityType() {
        return type;
    }

    /**
     * Returns a PermissionCollection containing the basic
     * permissions granted depending on the security type.
     */
    public PermissionCollection getPermissions() {
        PermissionCollection permissions = getSandBoxPermissions();

        // discard sandbox, give all
        if (type == ALL_PERMISSIONS) {
            permissions = new Permissions();
            permissions.add(new AllPermission());
            return permissions;
        }

        // add j2ee to sandbox if needed
        if (type == J2EE_PERMISSIONS)
            for (int i=0; i < j2eePermissions.length; i++)
                permissions.add(j2eePermissions[i]);

        return permissions;
    }

    /**
     * Returns a PermissionCollection containing the sandbox permissions
     */
    public PermissionCollection getSandBoxPermissions() {

        Permissions permissions = new Permissions();

        for (int i=0; i < sandboxPermissions.length; i++)
            permissions.add(sandboxPermissions[i]);

        if (file.isApplication())
            for (int i=0; i < jnlpRIAPermissions.length; i++)
                permissions.add(jnlpRIAPermissions[i]);

        if (downloadHost != null)
            permissions.add(new SocketPermission(downloadHost,
                                                 "connect, accept"));

        return permissions;
    }

}
