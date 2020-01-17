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

import net.adoptopenjdk.icedteaweb.JavaSystemProperties;
import net.adoptopenjdk.icedteaweb.classloader.JarExtractor;
import net.adoptopenjdk.icedteaweb.classloader.JnlpApplicationClassLoader;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.PropertyDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.security.SecurityDesc;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.resources.ResourceTracker;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.JNLPFileFactory;
import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.classloader.JNLPClassLoader;
import net.sourceforge.jnlp.util.JarFile;
import net.sourceforge.jnlp.util.WeakList;
import sun.awt.AppContext;

import javax.swing.event.EventListenerList;
import java.awt.Window;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Policy;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.jar.Attributes;

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

    /**
     * the file
     */
    private final JNLPFile file;

    /**
     * the thread group
     */
    private final ThreadGroup group;

    /**
     * the classloader
     */
    private final JnlpApplicationClassLoader loader;

    /**
     * whether the application has stopped running
     */
    private boolean stopped = false;

    /**
     * weak list of windows opened by the application
     */
    private final WeakList<Window> weakWindows = new WeakList<>();

    /**
     * list of application listeners
     */
    private final EventListenerList listeners = new EventListenerList();

    /**
     * whether or not this application is signed
     */
    private boolean isSigned;

    private final ResourceTracker tracker;

    private final ApplicationPermissions applicationPermissions;

    private void print(final String message) {
        final File tmpDir = new File(JavaSystemProperties.getJavaTempDir());
        final File logFile = new File(tmpDir, "itw-log.txt");
        if (!tmpDir.exists()) {
            tmpDir.mkdirs();
        }
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try(FileOutputStream out = new FileOutputStream(logFile, true)) {
            out.write((message + System.lineSeparator()).getBytes());
        } catch (final Exception e) {
            throw new RuntimeException("Can not write message to file!", e);
        } finally {
            System.out.println(message);
        }
    }


    final Consumer<JARDesc> addJarConsumer = jarDesc -> print("addJarConsumer called for " + jarDesc);

    final Function<JARDesc, URL> localCacheAccess;

    /**
     * Create an application instance for the file. This should be done in the
     * appropriate {@link ThreadGroup} only.
     *
     * @param file jnlpfile for which the instance do exists
     */
    public ApplicationInstance(JNLPFile file, boolean enableCodeBase) throws LaunchException {
        this.file = file;
        this.group = Thread.currentThread().getThreadGroup();
        this.tracker = new ResourceTracker(true, file.getDownloadOptions(), JNLPRuntime.getDefaultUpdatePolicy());
        this.applicationPermissions = new ApplicationPermissions(tracker);

        localCacheAccess = jarDesc -> {
            print("Try to load JAR at " + jarDesc.getLocation());
            try {
                tracker.addResource(jarDesc.getLocation(), jarDesc.getVersion());
                final URL url = tracker.getCacheFile(jarDesc.getLocation()).toURI().toURL();
                print("Local URL: " + url);
                return url;
            } catch (final Exception e) {
                print("ERROR: " + e);
                throw new RuntimeException("ARGH", e);
            }
        };

        JNLPFileFactory fileFactory = new JNLPFileFactory();
        JarExtractor extractor = new JarExtractor(file, fileFactory);

        try {
            this.loader = new JnlpApplicationClassLoader(extractor.getParts(), localCacheAccess);
        } catch (final Exception e) {
            throw new RuntimeException("ARGH!!!", e);
        }

        JNLPClassLoader.getInstance(file, JNLPRuntime.getDefaultUpdatePolicy(), enableCodeBase, tracker, applicationPermissions);
        ApplicationManager.addApplication(this);

        this.isSigned = true; // TODO: REFACTOR!!!!!!!!

        AppContext.getAppContext();

        if (JNLPRuntime.isSecurityEnabled() && JNLPRuntime.getForksStrategy().mayRunManagedApplication()) {
            final JNLPSecurityManager security = new JNLPSecurityManager();
            final JNLPPolicy policy = new JNLPPolicy(security);

            Policy.setPolicy(policy);
            System.setSecurityManager(security);
        }
    }

    /**
     * Notify listeners that the application has been terminated.
     */
    private void fireDestroyed() {
        Object[] list = listeners.getListenerList();
        ApplicationEvent event = null;

        for (int i = list.length - 1; i > 0; i -= 2) { // last to first required
            if (event == null)
                event = new ApplicationEvent(this);

            ((ApplicationListener) list[i]).applicationDestroyed(event);
        }
    }

    /**
     * Initialize the application's environment (installs
     * environment variables, etc).
     */
    public void initialize() {
        installEnvironment();
        final DeploymentConfiguration configuration = JNLPRuntime.getConfiguration();
        JNLPRuntime.getExtensionPoint().createMenuAndDesktopIntegration(configuration).addMenuAndDesktopEntries(file);
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

            final SecurityDesc s = applicationPermissions.getSecurity();
            final ProtectionDomain pd = new ProtectionDomain(cs, s.getPermissions(cs), null, null);
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

    /**
     * Returns the jnlpfile on which is this application based
     *
     * @return JNLP file for this task.
     */
    public JNLPFile getJNLPFile() {
        return file;
    }

    /**
     * Returns the application title.
     *
     * @return the title of this application
     */
    public String getTitle() {
        return file.getTitle();
    }

    /**
     * Returns whether the application is running.
     *
     * @return state of application
     */
    public boolean isRunning() {
        return !stopped;
    }

    /**
     * Stop the application and destroy its resources.
     */
    @SuppressWarnings("deprecation")
    public void destroy() {
        if (stopped)
            return;

        try {
            // destroy resources
            for (Window w : weakWindows) {
                if (w != null)
                    w.dispose();
            }

            weakWindows.clear();

            // interrupt threads
            Thread[] threads = new Thread[group.activeCount() * 2];
            int nthreads = group.enumerate(threads);
            for (int i = 0; i < nthreads; i++) {
                LOG.info("Interrupt thread: {}", threads[i]);
                threads[i].interrupt();
            }

            // then stop
            Thread.yield();
            nthreads = group.enumerate(threads);
            for (int i = 0; i < nthreads; i++) {
                LOG.info("Stop thread: {}", threads[i]);
                threads[i].stop();
            }

            // then destroy - except Thread.destroy() not implemented in jdk

        } finally {
            stopped = true;
            fireDestroyed();
        }
    }

    /**
     * Returns the thread group.
     *
     * @return thread group of this application, unless it is stopped
     * @throws IllegalStateException if the app is not running
     */
    public ThreadGroup getThreadGroup() throws IllegalStateException {
        if (stopped)
            throw new IllegalStateException();

        return group;
    }

    /**
     * Returns the classloader.
     *
     * @return the classloader of this application, unless it is stopped
     * @throws IllegalStateException if the app is not running
     */
    public ClassLoader getClassLoader() throws IllegalStateException {
        if (stopped)
            throw new IllegalStateException();

        return loader;
    }

    public String getMainClassName() throws IOException {
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
            }
        }

        return null;
    }

    /**
     * Adds a window that this application opened.  When the
     * application is disposed, these windows will also be disposed.
     *
     * @param window to be added
     */
    void addWindow(Window window) {
        weakWindows.add(window);
        weakWindows.trimToSize();
    }

    /**
     * @return whether or not this application is signed.
     */
    public boolean isSigned() {
        return isSigned;
    }

    public ApplicationPermissions getApplicationPermissions() {
        return applicationPermissions;
    }

    public PermissionCollection getPermissions(CodeSource cs) {
        return applicationPermissions.getPermissions(cs, addJarConsumer);
    }
}
