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

package net.adoptopenjdk.icedteaweb.jnlp.element.security;

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.NullJnlpFileException;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.UrlUtils;

import java.awt.AWTPermission;
import java.io.FilePermission;
import java.lang.reflect.Constructor;
import java.net.SocketPermission;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.security.URIParameter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.PropertyPermission;
import java.util.Set;

import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.ARRAY_LEGACY_MERGE_SORT;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.AWT_AA_FONT_SETTINGS;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.BROWSER;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.BROWSER_STAR;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.EXIT_VM;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.FILE_SEPARATOR;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.HTTP_AGENT;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.HTTP_KEEP_ALIVE;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.JAVAWS;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.JAVA_CLASS_VERSION;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.JAVA_PLUGIN;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.JAVA_SPEC_NAME;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.JAVA_SPEC_VENDOR;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.JAVA_SPEC_VERSION;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.JAVA_VENDOR;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.JAVA_VENDOR_URL;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.JAVA_VERSION;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.JNLP;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.LINE_SEPARATOR;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.OS_ARCH;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.OS_NAME;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.OS_VERSION;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.PATH_SEPARATOR;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.STOP_THREAD;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.VM_NAME;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.VM_SPEC_NAME;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.VM_SPEC_VENDOR;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.VM_VENDOR;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.VM_VERSION;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.WEBSTART_VERSION;
import static sun.security.util.SecurityConstants.PROPERTY_READ_ACTION;
import static sun.security.util.SecurityConstants.PROPERTY_RW_ACTION;

/**
 * The security element.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.7 $
 */
public class SecurityDesc {
    private final static Logger LOG = LoggerFactory.getLogger(SecurityDesc.class);

    public static final String SECURITY_ELEMENT = "security";

    private ApplicationPermissionLevel applicationPermissionLevel;
    private AppletPermissionLevel appletPermissionLevel;

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
    final private URL downloadHost;

    /** whether sandbox applications should get the show window without banner permission */
    private final boolean grantAwtPermissions;

    /** the JNLP file */
    private final JNLPFile file;

    private final Policy customTrustedPolicy;

    /**
     * URLPermission is new in Java 8, so we use reflection to check for it to keep compatibility
     * with Java 6/7. If we can't find the class or fail to construct it then we continue as usual
     * without.
     * 
     * These are saved as fields so that the reflective lookup only needs to be performed once
     * when the SecurityDesc is constructed, rather than every time a call is made to
     * {@link SecurityDesc#getSandBoxPermissions()}, which is called frequently.
     */
    private static Class<Permission> urlPermissionClass;
    private static Constructor<Permission> urlPermissionConstructor;
    
    static {
        try {
            urlPermissionClass = (Class<Permission>) Class.forName("java.net.URLPermission");
            urlPermissionConstructor = urlPermissionClass.getDeclaredConstructor(String.class);
        } catch (final ReflectiveOperationException | SecurityException e) {
            LOG.error("Exception while reflectively finding URLPermission - host is probably not running Java 8+", e);
            urlPermissionClass = null;
            urlPermissionConstructor = null;
        }
    }

    // We go by the rules here:
    // http://java.sun.com/docs/books/tutorial/deployment/doingMoreWithRIA/properties.html

    // Since this is security sensitive, take a conservative approach:
    // Allow only what is specifically allowed, and deny everything else

    /**
     * basic permissions for restricted mode
     */
    private static final Permission[] j2eePermissions = {
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
            new PropertyPermission("*", PROPERTY_READ_ACTION),
    };

    /**
     * basic permissions for restricted mode
     */
    private static final Permission[] sandboxPermissions = {
            new SocketPermission("localhost:1024-", "listen"),
            // new SocketPermission("<DownloadHost>", "connect, accept"), // added by code
            new PropertyPermission(ARRAY_LEGACY_MERGE_SORT, PROPERTY_RW_ACTION),
            new PropertyPermission(JAVA_VERSION, PROPERTY_READ_ACTION),
            new PropertyPermission(JAVA_VENDOR, PROPERTY_READ_ACTION),
            new PropertyPermission(JAVA_VENDOR_URL, PROPERTY_READ_ACTION),
            new PropertyPermission(JAVA_CLASS_VERSION, PROPERTY_READ_ACTION),
            new PropertyPermission(OS_NAME, PROPERTY_READ_ACTION),
            new PropertyPermission(OS_VERSION, PROPERTY_READ_ACTION),
            new PropertyPermission(OS_ARCH, PROPERTY_READ_ACTION),
            new PropertyPermission(FILE_SEPARATOR, PROPERTY_READ_ACTION),
            new PropertyPermission(PATH_SEPARATOR, PROPERTY_READ_ACTION),
            new PropertyPermission(LINE_SEPARATOR, PROPERTY_READ_ACTION),
            new PropertyPermission(JAVA_SPEC_VERSION, PROPERTY_READ_ACTION),
            new PropertyPermission(JAVA_SPEC_VENDOR, PROPERTY_READ_ACTION),
            new PropertyPermission(JAVA_SPEC_NAME, PROPERTY_READ_ACTION),
            new PropertyPermission(VM_SPEC_VENDOR, PROPERTY_READ_ACTION),
            new PropertyPermission(VM_SPEC_NAME, PROPERTY_READ_ACTION),
            new PropertyPermission(VM_VERSION, PROPERTY_READ_ACTION),
            new PropertyPermission(VM_VENDOR, PROPERTY_READ_ACTION),
            new PropertyPermission(VM_NAME, PROPERTY_READ_ACTION),
            new PropertyPermission(WEBSTART_VERSION, PROPERTY_READ_ACTION),
            new PropertyPermission(JAVA_PLUGIN, PROPERTY_READ_ACTION),
            new PropertyPermission(JNLP, PROPERTY_RW_ACTION),
            new PropertyPermission(JAVAWS, PROPERTY_RW_ACTION),
            new PropertyPermission(BROWSER, PROPERTY_READ_ACTION),
            new PropertyPermission(BROWSER_STAR, PROPERTY_READ_ACTION),
            new RuntimePermission(EXIT_VM),
            new RuntimePermission(STOP_THREAD),
            // disabled because we can't at this time prevent an
            // application from accessing other applications' event
            // queues, or even prevent access to security dialog queues.
            //
            // new AWTPermission("accessEventQueue"),
    };

    /**
     * basic permissions for restricted mode
     */
    private static final Permission[] jnlpRIAPermissions = {
            new PropertyPermission(AWT_AA_FONT_SETTINGS, PROPERTY_RW_ACTION),
            new PropertyPermission(HTTP_AGENT, PROPERTY_RW_ACTION),
            new PropertyPermission(HTTP_KEEP_ALIVE, PROPERTY_RW_ACTION),
            new PropertyPermission("java.awt.syncLWRequests", PROPERTY_RW_ACTION),
            new PropertyPermission("java.awt.Window.locationByPlatform", PROPERTY_RW_ACTION),
            new PropertyPermission("javaws.cfg.jauthenticator", PROPERTY_RW_ACTION),
            new PropertyPermission("javax.swing.defaultlf", PROPERTY_RW_ACTION),
            new PropertyPermission("sun.awt.noerasebackground", PROPERTY_RW_ACTION),
            new PropertyPermission("sun.awt.erasebackgroundonresize", PROPERTY_RW_ACTION),
            new PropertyPermission("sun.java2d.d3d", PROPERTY_RW_ACTION),
            new PropertyPermission("sun.java2d.dpiaware", PROPERTY_RW_ACTION),
            new PropertyPermission("sun.java2d.noddraw", PROPERTY_RW_ACTION),
            new PropertyPermission("sun.java2d.opengl", PROPERTY_RW_ACTION),
            new PropertyPermission("swing.boldMetal", PROPERTY_RW_ACTION),
            new PropertyPermission("swing.metalTheme", PROPERTY_RW_ACTION),
            new PropertyPermission("swing.noxp", PROPERTY_RW_ACTION),
            new PropertyPermission("swing.useSystemFontSettings", PROPERTY_RW_ACTION),
    };

    /**
     * Create a security descriptor.
     *
     * @param file the JNLP file
     * @param applicationPermissionLevel the permissions specified in the JNLP
     * @param type the type of security
     * @param downloadHost the download host (can always connect to)
     */
    public SecurityDesc(final JNLPFile file, final ApplicationPermissionLevel applicationPermissionLevel, final Object type, final URL downloadHost) {
        if (file == null) {
            throw new NullJnlpFileException();
        }
        this.file = file;
        this.applicationPermissionLevel = applicationPermissionLevel;
        this.type = type;
        this.downloadHost = downloadHost;

        String key = ConfigurationConstants.KEY_SECURITY_ALLOW_HIDE_WINDOW_WARNING;
        grantAwtPermissions = Boolean.valueOf(JNLPRuntime.getConfiguration().getProperty(key));

        customTrustedPolicy = getCustomTrustedPolicy();
    }

    /**
     * Create a security descriptor.
     *
     * @param file the JNLP file
     * @param appletPermissionLevel the permissions specified in the JNLP
     * @param type the type of security
     * @param downloadHost the download host (can always connect to)
     */
    public SecurityDesc(final JNLPFile file, final AppletPermissionLevel appletPermissionLevel, final Object type, final URL downloadHost) {
        if (file == null) {
            throw new NullJnlpFileException();
        }
        this.file = file;
        this.appletPermissionLevel = appletPermissionLevel;
        this.type = type;
        this.downloadHost = downloadHost;

        String key = ConfigurationConstants.KEY_SECURITY_ALLOW_HIDE_WINDOW_WARNING;
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
        String key = ConfigurationConstants.KEY_SECURITY_TRUSTED_POLICY;
        String policyLocation = JNLPRuntime.getConfiguration().getProperty(key);

        Policy policy = null;
        if (policyLocation != null) {
            try {
                URI policyUri = new URI("file://" + policyLocation);
                policy = Policy.getInstance("JavaPolicy", new URIParameter(policyUri));
            } catch (Exception e) {
                LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
            }
        }
        // return the appropriate policy, or null
        return policy;
    }

    /**
     * @return the permissions type, one of: ALL,
     * SANDBOX_PERMISSIONS, J2EE_PERMISSIONS.
     */
    public Object getSecurityType() {
        return type;
    }

    /**
     * @return a PermissionCollection containing the basic
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
            for (Permission j2eePermission : j2eePermissions) {
                permissions.add(j2eePermission);
        }

        return permissions;
    }

    public ApplicationPermissionLevel getApplicationPermissionLevel() {
        return applicationPermissionLevel;
    }

    public AppletPermissionLevel getAppletPermissionLevel() {
        return appletPermissionLevel;
    }

    /**
     * @return a PermissionCollection containing the sandbox permissions
     */
    public PermissionCollection getSandBoxPermissions() {
        final Permissions permissions = new Permissions();

        for (Permission sandboxPermission : sandboxPermissions) {
            permissions.add(sandboxPermission);
        }

        if (grantAwtPermissions) {
            permissions.add(new AWTPermission("showWindowWithoutWarningBanner"));
        }
        if (JNLPRuntime.isWebstartApplication()) {
            if (file == null) {
                throw new NullJnlpFileException("Can not return sandbox permissions, file is null");
            }
            if (file.isApplication()) {
                for (Permission jnlpRIAPermission : jnlpRIAPermissions) {
                    permissions.add(jnlpRIAPermission);
                }
            }
        }

        if (downloadHost != null && downloadHost.getHost().length() > 0) {
            permissions.add(new SocketPermission(UrlUtils.getHostAndPort(downloadHost),
                    "connect, accept"));
        }

        final Collection<Permission> urlPermissions = getUrlPermissions();
        for (final Permission permission : urlPermissions) {
            permissions.add(permission);
        }
        
        return permissions;
    }

    private Set<Permission> getUrlPermissions() {
        if (urlPermissionClass == null || urlPermissionConstructor == null) {
            return Collections.emptySet();
        }
        final Set<Permission> permissions = new HashSet<>();
        for (final JARDesc jar : file.getResources().getJARs()) {
            try {
                // Allow applets all HTTP methods (ex POST, GET) with any request headers
                // on resources anywhere recursively in or below the applet codebase, only on
                // default ports and ports explicitly specified in resource locations
                final URI resourceLocation = jar.getLocation().toURI().normalize();
                final URI host = getHost(resourceLocation);
                final String hostUriString = host.toString();
                final String urlPermissionUrlString = appendRecursiveSubdirToCodebaseHostString(hostUriString);
                final Permission p = urlPermissionConstructor.newInstance(urlPermissionUrlString);
                permissions.add(p);
            } catch (final ReflectiveOperationException e) {
                LOG.error("Exception while attempting to reflectively generate a URLPermission, probably not running on Java 8+?", e);
            } catch (final URISyntaxException e) {
                LOG.error("Could not determine codebase host for resource at " + jar.getLocation() +  " while generating URLPermissions", e);
            }
        }
        try {
            final URI codebase = file.getNotNullProbableCodeBase().toURI().normalize();
            final URI host = getHost(codebase);
            final String codebaseHostUriString = host.toString();
            final String urlPermissionUrlString = appendRecursiveSubdirToCodebaseHostString(codebaseHostUriString);
            final Permission p = urlPermissionConstructor.newInstance(urlPermissionUrlString);
            permissions.add(p);
        } catch (final ReflectiveOperationException e) {
            LOG.error("Exception while attempting to reflectively generate a URLPermission, probably not running on Java 8+?", e);
        } catch (final URISyntaxException e) {
            LOG.error("Could not determine codebase host for codebase " + file.getCodeBase() +  "  while generating URLPermissions", e);
        }
        return permissions;
    }

    /**
     * Gets the host domain part of an applet's codebase. Removes path, query, and fragment, but preserves scheme,
     * user info, and host. The port used is overridden with the specified port.
     * @param codebase the applet codebase URL
     * @param port
     * @return the host domain of the codebase
     * @throws URISyntaxException
     */
    static URI getHostWithSpecifiedPort(final URI codebase, final int port) throws URISyntaxException {
        Objects.requireNonNull(codebase);
        return new URI(codebase.getScheme(), codebase.getUserInfo(), codebase.getHost(), port, null, null, null);
    }

    /**
     * Gets the host domain part of an applet's codebase. Removes path, query, and fragment, but preserves scheme,
     * user info, host, and port.
     * @param codebase the applet codebase URL
     * @return the host domain of the codebase
     * @throws URISyntaxException
     */
    static URI getHost(final URI codebase) throws URISyntaxException {
        Objects.requireNonNull(codebase);
        return getHostWithSpecifiedPort(codebase, codebase.getPort());
    }

    /**
     * Appends a recursive access marker to a codebase host, for granting Java 8 URLPermissions which are no
     * more restrictive than the existing SocketPermissions
     * See http://docs.oracle.com/javase/8/docs/api/java/net/URLPermission.html
     * @param codebaseHost the applet's codebase's host domain URL as a String. Expected to be formatted as eg
     *                     "http://example.com:8080" or "http://example.com/"
     * @return the resulting String eg "http://example.com:8080/-
     */
    static String appendRecursiveSubdirToCodebaseHostString(final String codebaseHost) {
        Objects.requireNonNull(codebaseHost);
        String result = codebaseHost;
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        // See http://docs.oracle.com/javase/8/docs/api/java/net/URLPermission.html
        result = result + "/-"; // allow access to any resources recursively on the host domain
        return result;
    }

    /**
     * @return all the names of the basic JNLP system properties accessible by RIAs
     */
    public static String[] getJnlpRIAPermissions() {
        String[] jnlpPermissions = new String[jnlpRIAPermissions.length];

        for (int i = 0; i < jnlpRIAPermissions.length; i++)
            jnlpPermissions[i] = jnlpRIAPermissions[i].getName();

        return jnlpPermissions;
    }

}
