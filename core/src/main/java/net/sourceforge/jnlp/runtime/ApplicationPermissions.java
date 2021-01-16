package net.sourceforge.jnlp.runtime;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.http.CloseableConnection;
import net.adoptopenjdk.icedteaweb.http.ConnectionFactory;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.ResourcesDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.security.ApplicationEnvironment;
import net.adoptopenjdk.icedteaweb.jnlp.element.security.SecurityDesc;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.resources.ResourceTracker;
import net.adoptopenjdk.icedteaweb.security.PermissionsManager;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.cache.CacheUtil;
import net.sourceforge.jnlp.util.UrlUtils;

import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.net.SocketPermission;
import java.net.URL;
import java.security.AccessControlContext;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.adoptopenjdk.icedteaweb.security.PermissionsManager.getSandBoxPermissions;
import static sun.security.util.SecurityConstants.FILE_READ_ACTION;

public class ApplicationPermissions {
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationPermissions.class);

    private final ResourceTracker tracker;

    /**
     * Map of specific original (remote) CodeSource Urls to securitydesc
     * Synchronized since this field may become shared data between multiple
     * classloading threads. See loadClass(String) and
     * CodebaseClassLoader.findClassNonRecursive(String).
     */
    private final Map<URL, SecurityDesc> jarLocationSecurityMap = Collections.synchronizedMap(new HashMap<>());

    /**
     * the permissions for the cached jar files
     */
    private final List<Permission> resourcePermissions = new ArrayList<>();

    /**
     * Permissions granted by the user during runtime.
     */
    private final List<Permission> runtimePermissions = new ArrayList<>();

    private final Set<URL> alreadyTried = Collections.synchronizedSet(new HashSet<>());

    public ApplicationPermissions(final ResourceTracker tracker) {
        this.tracker = tracker;
    }

    public void addRuntimePermission(Permission p) {
        runtimePermissions.add(p);
    }

    /**
     * Make permission objects for the classpath.
     */
    public void addReadPermissionsForAllJars(final ResourcesDesc resources) {

        JARDesc[] jars = resources.getJARs();
        for (JARDesc jar : jars) {
            Permission p = getReadPermission(jar.getLocation());

            if (p == null) {
                LOG.info("Unable to add permission for {}", jar.getLocation());
            } else {
                resourcePermissions.add(p);
                LOG.info("Permission added: {}", p.toString());
            }
        }
    }

    public void addReadPermissionForJar(final URL location) {
        // Give read permissions to the cached jar file
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            Permission p = getReadPermission(location);
            resourcePermissions.add(p);
            return null;
        });
    }

    public PermissionCollection getPermissions(final JNLPFile file, CodeSource codeSource) {
        try {
            Assert.requireNonNull(codeSource, "codeSource");

            final Permissions result = new Permissions();

            // should check for extensions or boot, automatically give all
            // access w/o security dialog once we actually check certificates.

            // start with sandbox permissions as default
            PermissionCollection permissions = getSandBoxPermissions(file);

            // if more than default is needed:
            // 1. Code must be signed
            // 2. ALL or J2EE permissions must be requested (note: plugin requests ALL automatically)
            if (codeSource.getCodeSigners() != null) {
                if (codeSource.getLocation() == null) {
                    throw new IllegalStateException("Code source location was null");
                }
                final SecurityDesc codeSourceSecurity = getCodeSourceSecurity(codeSource.getLocation());
                if (codeSourceSecurity == null) {
                    throw new IllegalStateException("Code source security was null");
                }
                final ApplicationEnvironment applicationEnvironment = codeSourceSecurity.getApplicationEnvironment();
                if (applicationEnvironment == null) {
                    LOG.error("Warning! Code source security application environment was null");
                }
                if (applicationEnvironment == ApplicationEnvironment.ALL || applicationEnvironment == ApplicationEnvironment.J2EE) {
                    permissions = PermissionsManager.getPermissions(file, codeSource, applicationEnvironment);
                }
            }

            Collections.list(permissions.elements()).forEach(result::add);
            resourcePermissions.forEach(result::add); // add in permission to read the cached JAR files
            runtimePermissions.forEach(result::add); // add in the permissions that the user granted

            // Class from host X should be allowed to connect to host X
            if (codeSource.getLocation() != null && codeSource.getLocation().getHost().length() > 0) {
                result.add(new SocketPermission(UrlUtils.getHostAndPort(codeSource.getLocation()),
                        "connect, accept"));
            }

            return result;
        } catch (RuntimeException ex) {
            LOG.error("Failed to get permissions", ex);
            throw new RuntimeException("Failed to get permissions", ex);
        }
    }

    public void addSecurityForJarLocation(final URL location, SecurityDesc securityDesc) {
        jarLocationSecurityMap.put(location, securityDesc);
    }

    public SecurityDesc getSecurityForJarLocation(final URL location) {
        return jarLocationSecurityMap.get(location);
    }

    public Set<URL> getAllJarLocations() {
        return jarLocationSecurityMap.keySet();
    }

    public AccessControlContext getAccessControlContextForClassLoading(final JNLPFile file, final List<URL> codeBaseLoaderUrls) {
        AccessControlContext context = AccessController.getContext();

        try {
            context.checkPermission(new AllPermission());
            return context; // If context already has all permissions, don't bother
        } catch (AccessControlException ace) {
            // continue below
        }

        // Since this is for class-loading, technically any class from one jar
        // should be able to access a class from another, therefore making the
        // original context code source irrelevant
        PermissionCollection permissions = getSandBoxPermissions(file);

        // Local cache access permissions
        for (Permission resourcePermission : resourcePermissions) {
            permissions.add(resourcePermission);
        }

        synchronized (this) {
            getAllJarLocations().stream()
                    .map(l -> new SocketPermission(UrlUtils.getHostAndPort(l), "connect, accept"))
                    .forEach(permissions::add);
        }

        // Permissions for codebase urls (if there is a loader)
        codeBaseLoaderUrls.forEach(u -> permissions.add(new SocketPermission(UrlUtils.getHostAndPort(u), "connect, accept")));

        ProtectionDomain pd = new ProtectionDomain(null, permissions);

        return new AccessControlContext(new ProtectionDomain[]{pd});
    }

    private SecurityDesc getCodeSourceSecurity(final URL source) {
        final SecurityDesc storedValue = jarLocationSecurityMap.get(source);
        if (storedValue == null) {
            synchronized (alreadyTried) {
                if (!alreadyTried.contains(source)) {
                    alreadyTried.add(source);
                    // FIXME: try to load the jar which is requesting the permissions, but was NOT downloaded by standard way
                    LOG.info("Application is trying to get permissions for {}, which was not added by standard way. Trying to download and verify!", source.toString());
                    try {
                        final SecurityDesc newValue = jarLocationSecurityMap.get(source);
                        if (newValue != null) {
                            return newValue;
                        }
                    } catch (Throwable t) {
                        LOG.error("Error while getting security", t);
                    }
                }
            }
            LOG.info("Error: No security instance for {}. The application may have trouble continuing", source.toString());
            return null;
        } else {
            return storedValue;
        }
    }

    private Permission getReadPermission(final URL location) {
        if (CacheUtil.isCacheable(location)) {
            final File cacheFile = tracker.getCacheFile(location);
            if (cacheFile != null) {
                return new FilePermission(cacheFile.getPath(), FILE_READ_ACTION);
            } else {
                LOG.debug("No cache file for cacheable resource '{}' found.", location);
                return null;
            }
        } else {
            // this is what URLClassLoader does
            try (final CloseableConnection conn = ConnectionFactory.openConnection(location)) {
                return conn.getPermission();
            } catch (IOException ioe) {
                LOG.error("Exception while retrieving permissions from connection to " + location, ioe);
            }
        }
        // should try to figure out the permission
        return null;
    }

}
