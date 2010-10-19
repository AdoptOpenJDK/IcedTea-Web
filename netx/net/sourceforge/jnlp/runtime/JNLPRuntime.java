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

import java.io.*;
import java.nio.channels.FileLock;
import java.awt.*;
import java.text.*;
import java.util.*;
import java.util.List;
import java.security.*;
import javax.jnlp.*;

import net.sourceforge.jnlp.*;
import net.sourceforge.jnlp.cache.*;
import net.sourceforge.jnlp.services.*;
import net.sourceforge.jnlp.util.*;


/**
 * Configure and access the runtime environment.  This class
 * stores global jnlp properties such as default download
 * indicators, the install/base directory, the default resource
 * update policy, etc.  Some settings, such as the base directory,
 * cannot be changed once the runtime has been initialized.<p>
 *
 * The JNLP runtime can be locked to prevent further changes to
 * the runtime environment except by a specified class.  If set,
 * only instances of the <i>exit class</i> can exit the JVM or
 * change the JNLP runtime settings once the runtime has been
 * initialized.<p>
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.19 $
 */
public class JNLPRuntime {

    static {
        loadResources();
    }

    /** the localized resource strings */
    private static ResourceBundle resources;

    /** the security manager */
    private static JNLPSecurityManager security;

    /** the security policy */
    private static JNLPPolicy policy;

    /** the base dir for cache, etc */
    private static File baseDir;

    /** a default launch handler */
    private static LaunchHandler handler = null;

    /** default download indicator */
    private static DownloadIndicator indicator = null;

    /** update policy that controls when to check for updates */
    private static UpdatePolicy updatePolicy = UpdatePolicy.ALWAYS;

    /** netx window icon */
    private static Image windowIcon = null;

    /** whether initialized */
    private static boolean initialized = false;

    /** whether netx is in command-line mode (headless) */
    private static boolean headless = false;

        /** whether we'll be checking for jar signing */
        private static boolean verify = true;

    /** whether the runtime uses security */
    private static boolean securityEnabled = true;

    /** whether debug mode is on */
    private static boolean debug = false; // package access by Boot

    /** mutex to wait on, for initialization */
    public static Object initMutex = new Object();

    /** set to true if this is a webstart application. */
    private static boolean isWebstartApplication;

    /** set to false to indicate another JVM should not be spawned, even if necessary */
    private static boolean forksAllowed = true;

    /** contains the arguments passed to the jnlp runtime */
    private static List<String> initialArguments;

    /** Username */
    public static final String USER = System.getProperty("user.name");

    /** User's home directory */
    public static final String HOME_DIR = System.getProperty("user.home");

    /** the ~/.netxrc file containing netx settings */
    public static final String NETXRC_FILE = HOME_DIR + File.separator + ".netxrc";

    /** the ~/.netx directory containing user-specific data */
    public static final String NETX_DIR = HOME_DIR + File.separator + ".netx";

    /** the ~/.netx/security directory containing security related information */
    public static final String SECURITY_DIR = NETX_DIR + File.separator + "security";

    /** the ~/.netx/security/trusted.certs file containing trusted certificates */
    public static final String CERTIFICATES_FILE = SECURITY_DIR + File.separator + "trusted.certs";

    /** the /tmp/ directory used for temporary files */
    public static final String TMP_DIR = System.getProperty("java.io.tmpdir");

    /**
     * the /tmp/$USER/netx/locks/ directory containing locks for single instance
     * applications
     */
    public static final String LOCKS_DIR = TMP_DIR + File.separator + USER + File.separator
            + "netx" + File.separator + "locks";

    /**
     * The /tmp/$USER/netx/locks/netx_running file is used to indicate if any
     * instances of netx are running (this file may exist even if no instances
     * are running). All netx instances acquire a shared lock on this file. If
     * this file can be locked (using a {@link FileLock}) in exclusive mode, then
     * other netx instances are not running
     */
    public static final String NETX_RUNNING_FILE = LOCKS_DIR + File.separator + "netx_running";

    /** the java.home directory */
    public static final String JAVA_HOME_DIR = System.getProperty("java.home");

    /** the JNLP file to open to display the network-based about window */
    public static final String NETX_ABOUT_FILE = JAVA_HOME_DIR + File.separator + "lib"
            + File.separator + "about.jnlp";



    /**
     * Returns whether the JNLP runtime environment has been
     * initialized.  Once initialized, some properties such as the
     * base directory cannot be changed.  Before
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Initialize the JNLP runtime environment by installing the
     * security manager and security policy, initializing the JNLP
     * standard services, etc.<p>
     *
     * This method cannot be called more than once.  Once
     * initialized, methods that alter the runtime can only be
     * called by the exit class.<p>
     *
     * @param isApplication is true if a webstart application is being initialized
     *
     * @throws IllegalStateException if the runtime was previously initialized
     */
    public static void initialize(boolean isApplication) throws IllegalStateException {
        checkInitialized();

        isWebstartApplication = isApplication;

        //Setting the system property for javawebstart's version.
        //The version stored will be the same as java's version.
        System.setProperty("javawebstart.version", "javaws-" +
            System.getProperty("java.version"));

        if (headless == false)
            checkHeadless();

        if (!headless && windowIcon == null)
            loadWindowIcon();

        if (!headless && indicator == null)
            indicator = new DefaultDownloadIndicator();

        if (handler == null)
            handler = new DefaultLaunchHandler();

        if (baseDir == null)
            baseDir = getDefaultBaseDir();

        if (baseDir == null)
            throw new IllegalStateException(JNLPRuntime.getMessage("BNoBase"));

        ServiceManager.setServiceManagerStub(new XServiceManagerStub()); // ignored if we're running under Web Start

        policy = new JNLPPolicy();
        security = new JNLPSecurityManager(); // side effect: create JWindow

        if (securityEnabled) {
            Policy.setPolicy(policy); // do first b/c our SM blocks setPolicy
            System.setSecurityManager(security);
        }

        initialized = true;
    }

    /**
     * Returns true if a webstart application has been initialized, and false
     * for a plugin applet.
     */
    public static boolean isWebstartApplication() {
        return isWebstartApplication;
    }

    /**
     * Returns the window icon.
     */
    public static Image getWindowIcon() {
        return windowIcon;
    }

    /**
     * Sets the window icon that is displayed in Java applications
     * and applets instead of the default Java icon.
     *
     * @throws IllegalStateException if caller is not the exit class
     */
    public static void setWindowIcon(Image image) {
        checkExitClass();
        windowIcon = image;
    }

    /**
     * Returns whether the JNLP client will use any AWT/Swing
     * components.
     */
    public static boolean isHeadless() {
        return headless;
    }

        /**
         * Returns whether we are verifying code signing.
         */
        public static boolean isVerifying() {
                return verify;
        }
    /**
     * Sets whether the JNLP client will use any AWT/Swing
     * components.  In headless mode, client features that use the
     * AWT are disabled such that the client can be used in
     * headless mode (<code>java.awt.headless=true</code>).
     *
     * @throws IllegalStateException if the runtime was previously initialized
     */
    public static void setHeadless(boolean enabled) {
        checkInitialized();
        headless = enabled;
    }

   /**
        * Sets whether we will verify code signing.
        * @throws IllegalStateException if the runtime was previously initialized
        */
    public static void setVerify(boolean enabled) {
                checkInitialized();
                verify = enabled;
    }

    /**
     * Return the base directory containing the cache, persistence
     * store, etc.
     */
    public static File getBaseDir() {
        return baseDir;
    }

    /**
     * Sets the base directory containing the cache, persistence
     * store, etc.
     *
     * @throws IllegalStateException if caller is not the exit class
     */
    public static void setBaseDir(File baseDirectory) {
        checkInitialized();
        baseDir = baseDirectory;
    }

    /**
     * Returns whether the secure runtime environment is enabled.
     */
    public static boolean isSecurityEnabled() {
        return securityEnabled;
    }

    /**
     * Sets whether to enable the secure runtime environment.
     * Disabling security can increase performance for some
     * applications, and can be used to use netx with other code
     * that uses its own security manager or policy.
     *
     * Disabling security is not recommended and should only be
     * used if the JNLP files opened are trusted.  This method can
     * only be called before initalizing the runtime.<p>
     *
     * @param enabled whether security should be enabled
     * @throws IllegalStateException if the runtime is already initialized
     */
    public static void setSecurityEnabled(boolean enabled) {
        checkInitialized();
        securityEnabled = enabled;
    }

    /**
     * Returns the system default base dir for or if not set,
     * prompts the user for the location.
     *
     * @return the base dir, or null if the user canceled the dialog
     * @throws IOException if there was an io exception
     */
    public static File getDefaultBaseDir() {
        PropertiesFile props = JNLPRuntime.getProperties();

        String baseStr = props.getProperty("basedir");
        if (baseStr != null)
            return new File(baseStr);

        String homeDir = HOME_DIR;
        File baseDir = new File(NETX_DIR);
        if (homeDir == null || (!baseDir.isDirectory() && !baseDir.mkdir()))
            return null;

        props.setProperty("basedir", baseDir.toString());
        props.store();

        return baseDir;
    }

    /**
     * Set a class that can exit the JVM; if not set then any class
     * can exit the JVM.
     *
     * @throws IllegalStateException if caller is not the exit class
     */
    public static void setExitClass(Class exitClass) {
        checkExitClass();
        security.setExitClass(exitClass);
    }

    /**
     * Disables applets from calling exit.
     *
     * Once disabled, exit cannot be re-enabled for the duration of the JVM instance
     */
    public static void disableExit() {
        security.disableExit();
    }

    /**
     * Return the current Application, or null if none can be
     * determined.
     */
    public static ApplicationInstance getApplication() {
        return security.getApplication();
    }

    /**
     * Return a PropertiesFile object backed by the runtime's
     * properties file.
     */
    public static PropertiesFile getProperties() {
        File netxrc = new File(NETXRC_FILE);
        return new PropertiesFile(netxrc);
    }

    /**
     * Return whether debug statements for the JNLP client code
     * should be printed.
     */
    public static boolean isDebug() {
        return debug;
    }

    /**
     * Sets whether debug statements for the JNLP client code
     * should be printed to the standard output.
     *
     * @throws IllegalStateException if caller is not the exit class
     */
    public static void setDebug(boolean enabled) {
        checkExitClass();
        debug = enabled;
    }

    /**
     * Sets the default update policy.
     *
     * @throws IllegalStateException if caller is not the exit class
     */
    public static void setDefaultUpdatePolicy(UpdatePolicy policy) {
        checkExitClass();
        updatePolicy = policy;
    }

    /**
     * Returns the default update policy.
     */
    public static UpdatePolicy getDefaultUpdatePolicy() {
        return updatePolicy;
    }

    /**
     * Sets the default launch handler.
     */
    public static void setDefaultLaunchHandler(LaunchHandler handler) {
        checkExitClass();
        JNLPRuntime.handler = handler;
    }

    /**
     * Returns the default launch handler.
     */
    public static LaunchHandler getDefaultLaunchHandler() {
        return handler;
    }

    /**
     * Sets the default download indicator.
     *
     * @throws IllegalStateException if caller is not the exit class
     */
    public static void setDefaultDownloadIndicator(DownloadIndicator indicator) {
        checkExitClass();
        JNLPRuntime.indicator = indicator;
    }

    /**
     * Returns the default download indicator.
     */
    public static DownloadIndicator getDefaultDownloadIndicator() {
        return indicator;
    }

    /**
     * Returns the localized resource string identified by the
     * specified key.  If the message is empty, a null is
     * returned.
     */
    public static String getMessage(String key) {
        try {
            String result = resources.getString(key);
            if (result.length() == 0)
                return null;
            else
                return result;
        }
        catch (Exception ex) {
            if (!key.equals("RNoResource"))
                return getMessage("RNoResource", new Object[] {key});
            else
                return "Missing resource: "+key;
        }
    }

    /**
     * Returns the localized resource string using the specified
     * arguments.
     *
     * @param args the formatting arguments to the resource string
     */
    public static String getMessage(String key, Object args[]) {
        return MessageFormat.format(getMessage(key), args);
    }

    /**
     * Returns true if the current runtime will fork
     */
    public static boolean getForksAllowed() {
        return forksAllowed;
    }

    public static void setForksAllowed(boolean value) {
        checkInitialized();
        forksAllowed = value;
    }

    /**
     * Throws an exception if called when the runtime is
     * already initialized.
     */
    private static void checkInitialized() {
        if (initialized)
            throw new IllegalStateException("JNLPRuntime already initialized.");
    }

    /**
     * Throws an exception if called with security enabled but
     * a caller is not the exit class and the runtime has been
     * initialized.
     */
    private static void checkExitClass() {
        if (securityEnabled && initialized)
            if (!security.isExitClass())
                throw new IllegalStateException("Caller is not the exit class");
    }

    /**
     * Check whether the VM is in headless mode.
     */
    private static void checkHeadless() {
        //if (GraphicsEnvironment.isHeadless()) // jdk1.4+ only
        //    headless = true;
        try {
            if ("true".equalsIgnoreCase(System.getProperty("java.awt.headless")))
                headless = true;
        }
        catch (SecurityException ex) {
        }
    }

    /**
     * Load the resources.
     */
    private static void loadResources() {
        try {
            resources = ResourceBundle.getBundle("net.sourceforge.jnlp.resources.Messages");
        }
        catch (Exception ex) {
            throw new IllegalStateException("Missing resource bundle in netx.jar:net/sourceforge/jnlp/resource/Messages.properties");
        }
    }

    /**
     * Load the window icon.
     */
    private static void loadWindowIcon() {
        if (windowIcon != null)
            return;

        try {
            windowIcon = new javax.swing.ImageIcon((new sun.misc.Launcher())
                        .getClassLoader().getResource("net/sourceforge/jnlp/resources/netx-icon.png")).getImage();
        }
        catch (Exception ex) {
            if (JNLPRuntime.isDebug())
                ex.printStackTrace();
        }
    }


    public static void setInitialArgments(List<String> args) {
        checkInitialized();
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null)
            securityManager.checkPermission(new AllPermission());
        initialArguments = args;
    }

    public static List<String> getInitialArguments() {
        return initialArguments;
    }

}
