package net.adoptopenjdk.icedteaweb.security;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.JNLPFile;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.PropertyPermission;
import java.util.Set;

import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.ARRAY_LEGACY_MERGE_SORT;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.AWT_AA_FONT_SETTINGS;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.AWT_DISABLE_MIXING;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.AWT_ERASE_BACKGROUND_ON_RESIZE;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.AWT_NO_ERASE_BACKGROUND;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.AWT_SYNC_LWREQUESTS;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.AWT_WINDOW_LOCATION_BY_PLATFORM;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.BROWSER;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.BROWSER_STAR;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.EXIT_VM;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.FILE_SEPARATOR;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.HTTP_AGENT;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.HTTP_KEEP_ALIVE;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.JAVA2D_D3D;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.JAVA2D_DPI_AWARE;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.JAVA2D_NO_DDRAW;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.JAVA2D_OPENGL;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.JAVAWS;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.JAVA_CLASS_VERSION;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.JAVA_PLUGIN;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.JAVA_SPEC_NAME;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.JAVA_SPEC_VENDOR;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.JAVA_SPEC_VERSION;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.JAVA_VENDOR;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.JAVA_VENDOR_URL;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.JAVA_VERSION;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.JNLP;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.LINE_SEPARATOR;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.OS_ARCH;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.OS_NAME;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.OS_VERSION;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.PATH_SEPARATOR;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.STOP_THREAD;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.SWING_BOLD_METAL;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.SWING_DEFAULT_LF;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.SWING_METAL_THEME;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.SWING_NO_XP;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.SWING_USE_SYSTEM_FONT;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.VM_NAME;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.VM_SPEC_NAME;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.VM_SPEC_VENDOR;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.VM_VENDOR;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.VM_VERSION;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.WEBSTART_JAUTHENTICATOR;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.WEBSTART_VERSION;
import static sun.security.util.SecurityConstants.PROPERTY_READ_ACTION;
import static sun.security.util.SecurityConstants.PROPERTY_RW_ACTION;

public class PermissionsManager {
    private static final Logger LOG = LoggerFactory.getLogger(PermissionsManager.class);

    private final URL downloadHost;
    private final JNLPFile file;

    /**
     * URLPermission is new in Java 8, so we use reflection to check for it to keep compatibility
     * with Java 6/7. If we can't find the class or fail to construct it then we continue as usual
     * without.
     * <p>
     * These are saved as fields so that the reflective lookup only needs to be performed once
     * when the SecurityDesc is constructed, rather than every time a call is made to
     * {@link PermissionsManager#getSandBoxPermissions()}, which is called frequently.
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
            new PropertyPermission(AWT_SYNC_LWREQUESTS, PROPERTY_RW_ACTION),
            new PropertyPermission(AWT_WINDOW_LOCATION_BY_PLATFORM, PROPERTY_RW_ACTION),
            new PropertyPermission(AWT_DISABLE_MIXING, PROPERTY_RW_ACTION),
            new PropertyPermission(WEBSTART_JAUTHENTICATOR, PROPERTY_RW_ACTION),
            new PropertyPermission(SWING_DEFAULT_LF, PROPERTY_RW_ACTION),
            new PropertyPermission(AWT_NO_ERASE_BACKGROUND, PROPERTY_RW_ACTION),
            new PropertyPermission(AWT_ERASE_BACKGROUND_ON_RESIZE, PROPERTY_RW_ACTION),
            new PropertyPermission(JAVA2D_D3D, PROPERTY_RW_ACTION),
            new PropertyPermission(JAVA2D_DPI_AWARE, PROPERTY_RW_ACTION),
            new PropertyPermission(JAVA2D_NO_DDRAW, PROPERTY_RW_ACTION),
            new PropertyPermission(JAVA2D_OPENGL, PROPERTY_RW_ACTION),
            new PropertyPermission(SWING_BOLD_METAL, PROPERTY_RW_ACTION),
            new PropertyPermission(SWING_METAL_THEME, PROPERTY_RW_ACTION),
            new PropertyPermission(SWING_NO_XP, PROPERTY_RW_ACTION),
            new PropertyPermission(SWING_USE_SYSTEM_FONT, PROPERTY_RW_ACTION),
    };

    /**
     * Create a security descriptor.
     *
     * @param file         the JNLP file
     * @param downloadHost the download host (can always connect to)
     */
    public PermissionsManager(final JNLPFile file, final URL downloadHost) {
        Assert.requireNonNull(file, "file");

        this.file = file;
        this.downloadHost = downloadHost;
    }

    /**
     * Returns a Policy object that represents a custom policy to use instead
     * of granting {@link AllPermission} to a {@link CodeSource}
     *
     * @return a {@link Policy} object to delegate to. May be null, which
     * indicates that no policy exists and AllPermissions should be granted
     * instead.
     */
    public static Policy getCustomTrustedPolicy() {
        final String key = ConfigurationConstants.KEY_SECURITY_TRUSTED_POLICY;
        final String policyLocation = JNLPRuntime.getConfiguration().getProperty(key);

        Policy policy = null;

        if (policyLocation != null) {
            try {
                final URI policyUri = new URI("file://" + policyLocation);
                policy = Policy.getInstance("JavaPolicy", new URIParameter(policyUri));
            } catch (Exception e) {
                LOG.error("Unable to create trusted policy for policy file at " + policyLocation, e);
            }
        }

        return policy;
    }

    /**
     * @return a PermissionCollection containing the sandbox permissions
     */
    public PermissionCollection getSandBoxPermissions() {
        final Permissions permissions = new Permissions();

        Arrays.stream(sandboxPermissions).forEach(permissions::add);

        if (shouldAddShowWindowWithoutWarningBannerAwtPermission()) {
            permissions.add(new AWTPermission("showWindowWithoutWarningBanner"));
        }

        if (file.isApplication()) {
            Arrays.stream(jnlpRIAPermissions).forEach(permissions::add);
        }

        if (downloadHost != null && downloadHost.getHost().length() > 0) {
            permissions.add(new SocketPermission(UrlUtils.getHostAndPort(downloadHost), "connect, accept"));
        }

        final Collection<Permission> urlPermissions = getUrlPermissions(file);
        urlPermissions.forEach(permissions::add);

        return permissions;
    }

    /**
     * @return a PermissionCollection containing the J2EE permissions
     */
    public static PermissionCollection getJ2EEPermissions() {
        final Permissions permissions = new Permissions();
        Arrays.stream(j2eePermissions).forEach(permissions::add);

        return permissions;
    }

    public static PermissionCollection getJnlpRiaPermissions() {
        final Permissions permissions = new Permissions();
        Arrays.stream(jnlpRIAPermissions).forEach(permissions::add);

        return permissions;
    }

    /**
     * Check whether {@link AWTPermission} should be added, as a property is defined in the {@link JNLPRuntime}
     * {@link net.sourceforge.jnlp.config.DeploymentConfiguration}.
     *
     * @return true, if permission should be added
     * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/awt/AWTPermission.html"></a>
     */
    private static boolean shouldAddShowWindowWithoutWarningBannerAwtPermission() {
        return Boolean.parseBoolean(JNLPRuntime.getConfiguration().getProperty(ConfigurationConstants.KEY_SECURITY_ALLOW_HIDE_WINDOW_WARNING));
    }

    private static Set<Permission> getUrlPermissions(final JNLPFile file) {
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
                LOG.error("Could not determine codebase host for resource at " + jar.getLocation() + " while generating URLPermissions", e);
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
            LOG.error("Could not determine codebase host for codebase " + file.getCodeBase() + "  while generating URLPermissions", e);
        }
        return permissions;
    }

    /**
     * Gets the host domain part of an applet's codebase. Removes path, query, and fragment, but preserves scheme,
     * user info, and host. The port used is overridden with the specified port.
     *
     * @param codebase the applet codebase URL
     * @param port     the port
     * @return the host domain of the codebase
     */
    static URI getHostWithSpecifiedPort(final URI codebase, final int port) throws URISyntaxException {
        Objects.requireNonNull(codebase);
        return new URI(codebase.getScheme(), codebase.getUserInfo(), codebase.getHost(), port, null, null, null);
    }

    /**
     * Gets the host domain part of an applet's codebase. Removes path, query, and fragment, but preserves scheme,
     * user info, host, and port.
     *
     * @param codebase the applet codebase URL
     * @return the host domain of the codebase
     */
    static URI getHost(final URI codebase) throws URISyntaxException {
        Objects.requireNonNull(codebase);
        return getHostWithSpecifiedPort(codebase, codebase.getPort());
    }

    /**
     * Appends a recursive access marker to a codebase host, for granting Java 8 URLPermissions which are no
     * more restrictive than the existing SocketPermissions
     * See http://docs.oracle.com/javase/8/docs/api/java/net/URLPermission.html
     *
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
}
