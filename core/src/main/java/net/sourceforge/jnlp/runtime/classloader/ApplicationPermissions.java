package net.sourceforge.jnlp.runtime.classloader;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.http.CloseableConnection;
import net.adoptopenjdk.icedteaweb.http.ConnectionFactory;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.ResourcesDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.security.SecurityDesc;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.resources.ResourceTracker;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.cache.CacheUtil;
import net.sourceforge.jnlp.util.UrlUtils;

import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.net.SocketPermission;
import java.net.URL;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static sun.security.util.SecurityConstants.FILE_READ_ACTION;

public class ApplicationPermissions {

    private final static Logger LOG = LoggerFactory.getLogger(ApplicationPermissions.class);

    /**
     * the security section
     */
    private SecurityDesc security;

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
    private final ArrayList<Permission> runtimePermissions = new ArrayList<>();

    public void setSecurity(final JNLPFile file, final SecurityDelegate securityDelegate) throws LaunchException {
        URL codebase = UrlUtils.guessCodeBase(file);
        this.security = securityDelegate.getClassLoaderSecurity(codebase);
    }

    public SecurityDesc getSecurity() {
        return security;
    }

    public void addRuntimePermission(Permission p) {
        runtimePermissions.add(p);
    }

    /**
     * Make permission objects for the classpath.
     */
    public void initializeReadJarPermissions(ResourcesDesc resources, ResourceTracker tracker) {

        JARDesc[] jars = resources.getJARs();
        for (JARDesc jar : jars) {
            Permission p = getReadPermission(jar, tracker);

            if (p == null) {
                LOG.info("Unable to add permission for {}", jar.getLocation());
            } else {
                resourcePermissions.add(p);
                LOG.info("Permission added: {}", p.toString());
            }
        }
    }

    public void addForJar(final JARDesc desc, ResourceTracker tracker) {
        // Give read permissions to the cached jar file
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            Permission p = getReadPermission(desc, tracker);

            resourcePermissions.add(p);

            return null;
        });
    }

    public Permission getReadPermission(JARDesc jar, ResourceTracker tracker) {
        final URL location = jar.getLocation();

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

    public List<Permission> getResourcePermissions() {
        return Collections.unmodifiableList(resourcePermissions);
    }

    public List<Permission> getRuntimePermissions() {
        return Collections.unmodifiableList(runtimePermissions);
    }

    public PermissionCollection getPermissions(final JNLPClassLoader classLoader, CodeSource codeSource) {
        try {
            Assert.requireNonNull(codeSource, "codeSource");

            final Permissions result = new Permissions();

            // should check for extensions or boot, automatically give all
            // access w/o security dialog once we actually check certificates.
            // copy security permissions from SecurityDesc element
            if (security != null) {
                // Security desc. is used only to track security settings for the
                // application. However, an application may comprise of multiple
                // jars, and as such, security must be evaluated on a per jar basis.

                // set default perms
                PermissionCollection permissions = security.getSandBoxPermissions();

                // If more than default is needed:
                // 1. Code must be signed
                // 2. ALL or J2EE permissions must be requested (note: plugin requests ALL automatically)
                if (codeSource.getCodeSigners() != null) {
                    if (codeSource.getLocation() == null) {
                        throw new IllegalStateException("Code source location was null");
                    }
                    if (getCodeSourceSecurity(codeSource.getLocation(), jar -> classLoader.addNewJar(jar)) == null) {
                        throw new IllegalStateException("Code source security was null");
                    }
                    if (getCodeSourceSecurity(codeSource.getLocation(), jar -> classLoader.addNewJar(jar)).getSecurityType() == null) {
                        LOG.error("Warning! Code source security type was null");
                    }
                    Object securityType = getCodeSourceSecurity(codeSource.getLocation(), jar -> classLoader.addNewJar(jar)).getSecurityType();
                    if (SecurityDesc.ALL_PERMISSIONS.equals(securityType) || SecurityDesc.J2EE_PERMISSIONS.equals(securityType)) {
                        permissions = getCodeSourceSecurity(codeSource.getLocation(), jar -> classLoader.addNewJar(jar)).getPermissions(codeSource);
                    }
                }
                for (Permission perm : Collections.list(permissions.elements())) {
                    result.add(perm);
                }
            }

            // add in permission to read the cached JAR files
            for (Permission perm : getResourcePermissions()) {
                result.add(perm);
            }

            // add in the permissions that the user granted.
            for (Permission perm : getRuntimePermissions()) {
                result.add(perm);
            }

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

    private final Set<URL> alreadyTried = Collections.synchronizedSet(new HashSet<>());

    private SecurityDesc getCodeSourceSecurity(final URL source, final Consumer<JARDesc> addJarConsumer) {
        final SecurityDesc storedValue = jarLocationSecurityMap.get(source);
        if (storedValue == null) {
            synchronized (alreadyTried) {
                if (!alreadyTried.contains(source)) {
                    alreadyTried.add(source);
                    //try to load the jar which is requesting the permissions, but was NOT downloaded by standard way
                    LOG.info("Application is trying to get permissions for {}, which was not added by standard way. Trying to download and verify!", source.toString());
                    try {
                        final JARDesc des = new JARDesc(source, null, null, false, false, false, false);
                        addJarConsumer.accept(des);
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

    public void addSecurityDesc(final URL location, SecurityDesc securityDesc) {
        jarLocationSecurityMap.put(location, securityDesc);
    }

    public SecurityDesc getSecurityDesc(final URL location) {
        return jarLocationSecurityMap.get(location);
    }

    public Set<URL> getAllSecurityDescLocations() {
        return jarLocationSecurityMap.keySet();
    }

    public List<SocketPermission> createSocketPermissionsForAllSecurityDescLocations() {
        return getAllSecurityDescLocations().stream()
                .map(l -> new SocketPermission(UrlUtils.getHostAndPort(l),"connect, accept"))
                .collect(Collectors.toList());
    }
}
