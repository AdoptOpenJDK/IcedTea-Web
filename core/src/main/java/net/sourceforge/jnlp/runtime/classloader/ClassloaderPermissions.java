package net.sourceforge.jnlp.runtime.classloader;

import net.adoptopenjdk.icedteaweb.http.CloseableConnection;
import net.adoptopenjdk.icedteaweb.http.ConnectionFactory;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.ResourcesDesc;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.resources.ResourceTracker;
import net.sourceforge.jnlp.cache.CacheUtil;

import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static sun.security.util.SecurityConstants.FILE_READ_ACTION;

public class ClassloaderPermissions {

    private final static Logger LOG = LoggerFactory.getLogger(ClassloaderPermissions.class);


    /**
     * the permissions for the cached jar files
     */
    private final List<Permission> resourcePermissions = new ArrayList<>();

    /**
     * Permissions granted by the user during runtime.
     */
    private final ArrayList<Permission> runtimePermissions = new ArrayList<>();

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
}
