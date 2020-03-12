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

package net.sourceforge.jnlp.runtime;

import net.adoptopenjdk.icedteaweb.classloader.JnlpApplicationClassLoader;
import net.adoptopenjdk.icedteaweb.classloader.PartExtractor;
import net.adoptopenjdk.icedteaweb.classloader.PartsHandler;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.PropertyDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.security.ApplicationEnvironment;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.resources.DefaultResourceTrackerFactory;
import net.adoptopenjdk.icedteaweb.resources.ResourceTracker;
import net.adoptopenjdk.icedteaweb.resources.ResourceTrackerFactory;
import net.adoptopenjdk.icedteaweb.security.PermissionsManager;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.JNLPFileFactory;
import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.util.JarFile;
import sun.awt.AppContext;

import java.io.File;
import java.io.IOException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.jar.Attributes;
import java.util.stream.Stream;

import static net.adoptopenjdk.icedteaweb.jnlp.element.security.ApplicationEnvironment.ALL;
import static net.sourceforge.jnlp.LaunchException.FATAL;

/**
 * Represents a running instance of an application described in a
 * JNLPFile. This class provides a way to track the application's
 * resources and destroy the application.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.15 $
 */
public class ApplicationInstance {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationInstance.class);

    private static final String DEPLOYMENT_SYSPROP = "deployment.javaws";
    private static final String DEPLOYMENT_SYSPROP_VALUE = "IcedTea-Web";

    // todo: should attempt to unload the environment variables
    // installed by the application.

    private final JNLPFile file;
    private final String mainClass;
    private final ThreadGroup group;
    private final JnlpApplicationClassLoader loader;
    private final ApplicationPermissions applicationPermissions;

    /**
     * whether the application has stopped running
     */
    private boolean stopped = false;

    private ApplicationEnvironment applicationEnvironment;

    /**
     * Create an application instance for the file. This should be done in the
     * appropriate {@link ThreadGroup} only.
     *
     * @param file jnlpfile for which the instance do exists
     */
    public ApplicationInstance(final JNLPFile file, final ThreadGroup applicationThreadGroup) {
        this(file, new DefaultResourceTrackerFactory(), applicationThreadGroup);
    }

    /**
     * Visible for testing. For productive code please use {@link #ApplicationInstance(JNLPFile, ThreadGroup)} (JNLPFile)}.
     */
    public ApplicationInstance(final JNLPFile file, ResourceTrackerFactory trackerFactory, final ThreadGroup applicationThreadGroup) {
        this.file = file;
        this.applicationEnvironment = file.getSecurity().getApplicationEnvironment();
        this.group = applicationThreadGroup;
        final ResourceTracker tracker = trackerFactory.create(true, file.getDownloadOptions(), JNLPRuntime.getDefaultUpdatePolicy());
        this.applicationPermissions = new ApplicationPermissions(tracker);
        final JNLPFileFactory fileFactory = new JNLPFileFactory();
        final PartExtractor extractor = new PartExtractor(file, fileFactory);

        final PartsHandler partsHandler = new PartsHandler(extractor.getParts(), file, tracker);
        this.loader = new JnlpApplicationClassLoader(partsHandler);

        this.mainClass = determineMainClass(file, tracker);
    }

    /**
     * Initialize the application's environment (installs
     * environment variables, etc).
     */
    public void initialize() throws LaunchException {

        loader.initializeEagerJars();

        ApplicationManager.addApplication(this);

        AppContext.getAppContext();

        installSandboxIfRequested();

        installEnvironment();
        installShortcutsIfRequested();
    }

    private void installSandboxIfRequested() throws LaunchException {
        if (JNLPRuntime.isSecurityEnabled() && applicationEnvironment != ALL) {
            throw new LaunchException(file, null, FATAL, "Not Supported", "Sandbox not supported",
                    "Currently Icedtea-Web does not support sandboxing of a managed application");
        }
    }

    /**
     * Releases the application's resources before it is collected.
     * Only collectable if classloader and thread group are
     * also collectable so basically is almost never called (an
     * application would have to close its windows and exit its
     * threads but not call JNLPRuntime.exit).
     */
    @Override
    public void finalize() {
        destroy();
    }

    /**
     * Install the environment variables.
     */
    private void installEnvironment() {
        final PropertyDesc[] props = file.getResources().getProperties();

        if (!(props.length == 0)) {
            final CodeSource cs = new CodeSource(null, (java.security.cert.Certificate[]) null);

            final ProtectionDomain pd = new ProtectionDomain(cs, PermissionsManager.getPermissions(file, cs, applicationEnvironment), null, null);
            final AccessControlContext acc = new AccessControlContext(new ProtectionDomain[]{pd});

            final PrivilegedAction<Object> setPropertiesAction = () -> {
                for (PropertyDesc propDesc : props) {
                    System.setProperty(propDesc.getKey(), propDesc.getValue());
                }
                return null;
            };

            LOG.info("about to set system properties");
            AccessController.doPrivileged(setPropertiesAction, acc);
        }

        // The "deployment.javaws" flag is always set to "IcedTea-Web" to make it possible
        // for the started application to detect the execution context.
        System.setProperty(DEPLOYMENT_SYSPROP, DEPLOYMENT_SYSPROP_VALUE);
    }

    private void installShortcutsIfRequested() {
        final DeploymentConfiguration configuration = JNLPRuntime.getConfiguration();
        JNLPRuntime.getExtensionPoint().createMenuAndDesktopIntegration(configuration).addMenuAndDesktopEntries(file);
    }

    /**
     * Returns the jnlpfile on which is this application based
     *
     * @return JNLP file for this task.
     */
    public JNLPFile getJNLPFile() {
        return file;
    }

    /**
     * @return the environment for the application to run in.
     */
    public ApplicationEnvironment getApplicationEnvironment() {
        return applicationEnvironment;
    }

    public void setApplicationEnvironment(ApplicationEnvironment applicationEnvironment) {
        this.applicationEnvironment = applicationEnvironment;
    }

    /**
     * Stop the application and destroy its resources.
     */
    public synchronized void destroy() {
        if (stopped)
            return;

        try {
            Thread[] threads = new Thread[group.activeCount() * 2];
            group.enumerate(threads);
            Stream.of(threads).forEach(t -> {
                try {
                    t.interrupt();
                } catch (final Exception e) {
                    LOG.error("Unable to interrupt application Thread '" + t.getName() + "'", e);
                }
            });

        } catch (final Exception e) {
            LOG.error("ERROR IN DESTROYING APP!", e);
        } finally {
            stopped = true;
        }
    }

    /**
     * Returns the thread group.
     *
     * @return thread group of this application, unless it is stopped
     * @throws IllegalStateException if the app is not running
     */
    public ThreadGroup getThreadGroup() throws IllegalStateException {
        if (stopped) {
            throw new IllegalStateException();
        }

        return group;
    }

    /**
     * Returns the classloader.
     *
     * @return the classloader of this application, unless it is stopped
     * @throws IllegalStateException if the app is not running
     */
    public ClassLoader getClassLoader() throws IllegalStateException {
        if (stopped) {
            throw new IllegalStateException();
        }

        return loader;
    }


    public PermissionCollection getPermissions(CodeSource cs) {
        return applicationPermissions.getPermissions(file, cs);
    }

    public String getMainClassName() {
        return mainClass;
    }

    private String determineMainClass(JNLPFile file, ResourceTracker tracker) {
        final String mainName = file.getApplication().getMainClass();

        // When the application-desc field is empty, we should take a
        // look at the main jar for the main class.
        if (mainName != null) {
            return mainName;
        }

        final File f = tracker.getCacheFile(file.getResources().getMainJAR().getLocation());
        if (f != null) {
            try (final JarFile mainJar = new JarFile(f)) {
                return mainJar.getManifest().getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return null;
    }
}
