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

import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

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

    /** whether sandbox applications should get the show window without banner permission */
    private final boolean grantAwtPermissions;

    /** the JNLP file */
    private JNLPFile file;

    private final Policy customTrustedPolicy;

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
            new PropertyPermission("browser", "read"),
            new PropertyPermission("browser.*", "read"),
            new RuntimePermission("exitVM"),
            new RuntimePermission("stopThread"),
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

        String key = DeploymentConfiguration.KEY_SECURITY_ALLOW_HIDE_WINDOW_WARNING;
        grantAwtPermissions = Boolean.valueOf(JNLPRuntime.getConfiguration().getProperty(key));

        customTrustedPolicy = getCustomTrustedPolicy();
    }

    /**
     * Returns a Policy object that represents a custom policy to use instead
     * of granting {@link AllPermission} to a {@link CodeSource}
     *
     * @return a {@link Policy} object to delegate to. May be null, which
     * indicates that no policy exists and AllPermissions should be granted
     * instead.
     */
    private Policy getCustomTrustedPolicy() {
        String key = DeploymentConfiguration.KEY_SECURITY_TRUSTED_POLICY;
        String policyLocation = JNLPRuntime.getConfiguration().getProperty(key);

        Policy policy = null;
        if (policyLocation != null) {
            try {
                URI policyUri = new URI("file://" + policyLocation);
                policy = Policy.getInstance("JavaPolicy", new URIParameter(policyUri));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // return the appropriate policy, or null
        return policy;
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
     *
     * @param cs the CodeSource to get permissions for
     */
    public PermissionCollection getPermissions(CodeSource cs) {
        PermissionCollection permissions = getSandBoxPermissions();

        // discard sandbox, give all
        if (ALL_PERMISSIONS.equals(type)) {
            permissions = new Permissions();
            if (customTrustedPolicy == null) {
                permissions.add(new AllPermission());
                return permissions;
            } else {
                return customTrustedPolicy.getPermissions(cs);
            }
        }

        // add j2ee to sandbox if needed
        if (J2EE_PERMISSIONS.equals(type))
            for (int i = 0; i < j2eePermissions.length; i++)
                permissions.add(j2eePermissions[i]);

        return permissions;
    }

    /**
     * Returns a PermissionCollection containing the sandbox permissions
     */
    public PermissionCollection getSandBoxPermissions() {

        Permissions permissions = new Permissions();

        for (int i = 0; i < sandboxPermissions.length; i++)
            permissions.add(sandboxPermissions[i]);

        if (grantAwtPermissions) {
            permissions.add(new AWTPermission("showWindowWithoutWarningBanner"));
        }

        if (file.isApplication())
            for (int i = 0; i < jnlpRIAPermissions.length; i++)
                permissions.add(jnlpRIAPermissions[i]);

        if (downloadHost != null && downloadHost.length() > 0)
            permissions.add(new SocketPermission(downloadHost,
                                                 "connect, accept"));

        return permissions;
    }
    
    /**
     * Returns all the names of the basic JNLP system properties accessible by RIAs
     */
    public static String[] getJnlpRIAPermissions() {
        String[] jnlpPermissions = new String[jnlpRIAPermissions.length];

        for (int i = 0; i < jnlpRIAPermissions.length; i++)
            jnlpPermissions[i] = jnlpRIAPermissions[i].getName();

        return jnlpPermissions;
    }

}
