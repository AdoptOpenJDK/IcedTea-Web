// Copyright (C) 2001-2003 Jon A. Maxwell (JAM)
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

package net.sourceforge.jnlp.runtime;

import net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants;
import net.adoptopenjdk.icedteaweb.StringUtils;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.ExtensionDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.PropertyDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.security.SecurityDesc;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.xmlparser.ParseException;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.classloader.JNLPClassLoader;
import net.sourceforge.jnlp.util.WeakList;
import sun.awt.AppContext;

import javax.swing.event.EventListenerList;
import java.awt.Window;
import java.io.IOException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    public static final String BLACKLIST_FOR_JNLP_PROPERTIES = "blacklistForJnlpProperties";
    public static final String BLACKLISTED_PROPERTIES_SEPARATOR = "=";

    public static Set<String> getBlackListedJnlpProperties() {
        final String blackListedProprtiesString = System.getenv(BLACKLIST_FOR_JNLP_PROPERTIES);
        if (StringUtils.isBlank(blackListedProprtiesString)) {
            return Collections.emptySet();
        }
        return new HashSet<>(Arrays.asList(blackListedProprtiesString.split(BLACKLISTED_PROPERTIES_SEPARATOR)));
    }

    // todo: should attempt to unload the environment variables
    // installed by the application.

    /** the file */
    private final JNLPFile file;

    /** the thread group */
    private final ThreadGroup group;

    /** the classloader */
    private final JNLPClassLoader loader;

    /** whether the application has stopped running */
    private boolean stopped = false;

    /** weak list of windows opened by the application */
    private final WeakList<Window> weakWindows = new WeakList<>();

    /** list of application listeners  */
    private final EventListenerList listeners = new EventListenerList();

    /** whether or not this application is signed */
    private boolean isSigned;

    /**
     * Create an application instance for the file. This should be done in the
     * appropriate {@link ThreadGroup} only.
     * @param file jnlpfile for which the instance do exists
     * @param group thread group to which it belongs
     * @param loader loader for this application
     */
    public ApplicationInstance(JNLPFile file, ThreadGroup group, JNLPClassLoader loader) {
        this.file = file;
        this.group = group;
        this.loader = loader;
        this.isSigned = loader.getSigning();
        AppContext.getAppContext();
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
        setSystemPropertiesFromJnlp();

        // The "deployment.javaws" flag is always set to "IcedTea-Web" to make it possible
        // for the started application to detect the execution context.
        System.setProperty(DEPLOYMENT_SYSPROP, DEPLOYMENT_SYSPROP_VALUE);
    }

    private void setSystemPropertiesFromJnlp() {
        final List<PropertyDesc> props = collectPropertiesFromJnlpHierarchy(new ArrayList<>(), file);

        if (!(props.size() == 0)) {
            final CodeSource cs = new CodeSource(null, (java.security.cert.Certificate[]) null);

            final JNLPClassLoader loader = this.loader;
            final SecurityDesc s = loader.getSecurity();
            final ProtectionDomain pd = new ProtectionDomain(cs, s.getPermissions(cs), null, null);
            final AccessControlContext acc = new AccessControlContext(new ProtectionDomain[] { pd });

            final PrivilegedAction<Object> setPropertiesAction = () -> {
                final Set<String> blackListedJnlpProperties = getBlackListedJnlpProperties();
                for (PropertyDesc propDesc : props) {
                    if (!blackListedJnlpProperties.contains(propDesc.getKey())) {
                        setSystemProperty(propDesc);
                    }
                }
                return null;
            };

            LOG.info("About to set system properties");
            AccessController.doPrivileged(setPropertiesAction, acc);
        }
    }

    private void setSystemProperty(PropertyDesc propDesc) {
        LOG.debug("Setting System Property {} with value {}", propDesc.getKey(), propDesc.getValue());
        System.setProperty(propDesc.getKey(), propDesc.getValue());
    }

    /**
     * Returns the jnlpfile on which is this application based
     * @return JNLP file for this task.
     */
    public JNLPFile getJNLPFile() {
        return file;
    }

    /**
     * Returns the application title.
     * @return the title of this application
     */
    public String getTitle() {
        return file.getTitle();
    }

    /**
     * Returns whether the application is running.
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
    public JNLPClassLoader getClassLoader() throws IllegalStateException {
        if (stopped)
            throw new IllegalStateException();

        return loader;
    }

    public String getMainClassName() throws IOException {
        String mainName = file.getApplication().getMainClass();

        // When the application-desc field is empty, we should take a
        // look at the main jar for the main class.
        if (mainName == null) {
            mainName = getClassLoader().getMainClassNameFromManifest(file.getResources().getMainJAR());
        }

        return mainName;
    }

    /**
     * Adds a window that this application opened.  When the
     * application is disposed, these windows will also be disposed.
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

    /**
     * Collects properties from the full hierarchy of JNLP files
     *
     * @param props a list of all properties collected so far
     * @param jnlpFile the current tip of the jnlp hierarchy
     * @return a list of properties collected from the full hierarchy of JNLP files
     */
    private List<PropertyDesc> collectPropertiesFromJnlpHierarchy(List<PropertyDesc> props, JNLPFile jnlpFile) {
        // Collect properties from the current jnlp file
        props.addAll(Arrays.asList(jnlpFile.getResources().getProperties()));
        for (ExtensionDesc ext : jnlpFile.getResources().getExtensions()) {
            // Recursively look for extension jnlp files to collect their properties
            try {
                ext.resolve();
                JNLPFile extFile = ext.getJNLPFile();
                if (extFile != null) {
                    collectPropertiesFromJnlpHierarchy(props, extFile);
                }
            } catch (ParseException | IOException e) {
                LOG.debug("Could not resolve JNLP file {} (declared properties won't be set): {} ", ext.getName(),  e.getMessage());
            }
        }
        return props;
    }
}
