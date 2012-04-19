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
import java.net.Authenticator;
import java.net.ProxySelector;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.awt.*;
import java.text.*;
import java.util.*;
import java.util.List;
import java.security.*;
import javax.jnlp.*;
import javax.naming.ConfigurationException;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.swing.UIManager;
import javax.swing.text.html.parser.ParserDelegator;

import sun.net.www.protocol.jar.URLJarFile;

import net.sourceforge.jnlp.*;
import net.sourceforge.jnlp.browser.BrowserAwareProxySelector;
import net.sourceforge.jnlp.cache.*;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.security.JNLPAuthenticator;
import net.sourceforge.jnlp.security.KeyStores;
import net.sourceforge.jnlp.security.SecurityDialogMessageHandler;
import net.sourceforge.jnlp.security.VariableX509TrustManager;
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

    private static final DeploymentConfiguration config = new DeploymentConfiguration();

    /** the security manager */
    private static JNLPSecurityManager security;

    /** the security policy */
    private static JNLPPolicy policy;

    /** handles all security message to show appropriate security dialogs */
    private static SecurityDialogMessageHandler securityDialogMessageHandler;

    /** a default launch handler */
    private static LaunchHandler handler = null;

    /** default download indicator */
    private static DownloadIndicator indicator = null;

    /** update policy that controls when to check for updates */
    private static UpdatePolicy updatePolicy = UpdatePolicy.ALWAYS;

    /** whether initialized */
    private static boolean initialized = false;

    /** whether netx is in command-line mode (headless) */
    private static boolean headless = false;

    /** whether we'll be checking for jar signing */
    private static boolean verify = true;

    /** whether the runtime uses security */
    private static boolean securityEnabled = true;

    /** whether debug mode is on */
    private static boolean debug = false;

    /** whether streams should be redirected */
    private static boolean redirectStreams = false;

    /** mutex to wait on, for initialization */
    public static Object initMutex = new Object();

    /** set to true if this is a webstart application. */
    private static boolean isWebstartApplication;

    /** set to false to indicate another JVM should not be spawned, even if necessary */
    private static boolean forksAllowed = true;

    /** all security dialogs will be consumed and pretented as beeing verified by user and allowed.*/
    private static boolean trustAll=false;

    /** contains the arguments passed to the jnlp runtime */
    private static List<String> initialArguments;

    /** a lock which is held to indicate that an instance of netx is running */
    private static FileLock fileLock;

    public static final String STDERR_FILE = "java.stderr";
    public static final String STDOUT_FILE = "java.stdout";


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
     * This method should be called from the main AppContext/Thread. <p>
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

        try {
            config.load();
        } catch (ConfigurationException e) {
            /* exit if there is a fatal exception loading the configuration */
            if (isApplication) {
                System.out.println(getMessage("RConfigurationError"));
                System.exit(1);
            }
        }

        KeyStores.setConfiguration(config);

        initializeStreams();

        isWebstartApplication = isApplication;

        //Setting the system property for javawebstart's version.
        //The version stored will be the same as java's version.
        System.setProperty("javawebstart.version", "javaws-" +
                System.getProperty("java.version"));

        if (headless == false)
            checkHeadless();

        if (!headless && indicator == null)
            indicator = new DefaultDownloadIndicator();

        if (handler == null) {
            if (headless) {
                handler = new DefaultLaunchHandler(System.err);
            } else {
                handler = new GuiLaunchHandler(System.err);
            }
        }

        ServiceManager.setServiceManagerStub(new XServiceManagerStub()); // ignored if we're running under Web Start

        policy = new JNLPPolicy();
        security = new JNLPSecurityManager(); // side effect: create JWindow

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // ignore it
        }

        doMainAppContextHacks();

        if (securityEnabled) {
            Policy.setPolicy(policy); // do first b/c our SM blocks setPolicy
            System.setSecurityManager(security);
        }

        securityDialogMessageHandler = startSecurityThreads();

        // wire in custom authenticator for SSL connections
        try {
            SSLSocketFactory sslSocketFactory;
            SSLContext context = SSLContext.getInstance("SSL");
            KeyStore ks = KeyStores.getKeyStore(KeyStores.Level.USER, KeyStores.Type.CLIENT_CERTS);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, KeyStores.getPassword());
            TrustManager[] trust = new TrustManager[] { VariableX509TrustManager.getInstance() };
            context.init(kmf.getKeyManagers(), trust, null);
            sslSocketFactory = context.getSocketFactory();

            HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory);
        } catch (Exception e) {
            System.err.println("Unable to set SSLSocketfactory (may _prevent_ access to sites that should be trusted)! Continuing anyway...");
            e.printStackTrace();
        }

        // plug in a custom authenticator and proxy selector
        Authenticator.setDefault(new JNLPAuthenticator());
        ProxySelector.setDefault(new BrowserAwareProxySelector());

        // Restrict access to netx classes
        Security.setProperty("package.access", 
                             Security.getProperty("package.access")+",net.sourceforge.jnlp");

        URLJarFile.setCallBack(CachedJarFileCallback.getInstance());

        initialized = true;

    }

    /**
     * This must NOT be called form the application ThreadGroup. An application
     * can inject events into its {@link EventQueue} and bypass the security
     * dialogs.
     *
     * @return a {@link SecurityDialogMessageHandler} that can be used to post
     * security messages
     */
    private static SecurityDialogMessageHandler startSecurityThreads() {
        ThreadGroup securityThreadGroup = new ThreadGroup("NetxSecurityThreadGroup");
        SecurityDialogMessageHandler runner = new SecurityDialogMessageHandler();
        Thread securityThread = new Thread(securityThreadGroup, runner, "NetxSecurityThread");
        securityThread.setDaemon(true);
        securityThread.start();
        return runner;
    }

    /**
     * Performs a few hacks that are needed for the main AppContext
     *
     * @see Launcher#doPerApplicationAppContextHacks
     */
    private static void doMainAppContextHacks() {

        /*
         * With OpenJDK6 (but not with 7) a per-AppContext dtd is maintained.
         * This dtd is created by the ParserDelgate. However, the code in
         * HTMLEditorKit (used to render HTML in labels and textpanes) creates
         * the ParserDelegate only if there are no existing ParserDelegates. The
         * result is that all other AppContexts see a null dtd.
         */
        new ParserDelegator();
    }

    /**
     * Initializes the standard output and error streams, redirecting them or
     * duplicating them as required.
     */
    private static void initializeStreams() {
        Boolean enableLogging = Boolean.valueOf(config
                .getProperty(DeploymentConfiguration.KEY_ENABLE_LOGGING));
        if (redirectStreams || enableLogging) {
            String logDir = config.getProperty(DeploymentConfiguration.KEY_USER_LOG_DIR);

            try {
                File errFile = new File(logDir, JNLPRuntime.STDERR_FILE);
                FileUtils.createParentDir(errFile);
                FileUtils.createRestrictedFile(errFile, true);
                File outFile = new File(logDir, JNLPRuntime.STDOUT_FILE);
                FileUtils.createParentDir(outFile);
                FileUtils.createRestrictedFile(outFile, true);

                if (redirectStreams) {
                    System.setErr(new PrintStream(new FileOutputStream(errFile)));
                    System.setOut(new PrintStream(new FileOutputStream(outFile)));
                } else {
                    System.setErr(new TeeOutputStream(new FileOutputStream(errFile), System.err));
                    System.setOut(new TeeOutputStream(new FileOutputStream(outFile), System.out));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Gets the Configuration associated with this runtime
     * @return a {@link DeploymentConfiguration} object that can be queried to
     * find relevant configuration settings
     */
    public static DeploymentConfiguration getConfiguration() {
        return config;
    }

    /**
     * Returns true if a webstart application has been initialized, and false
     * for a plugin applet.
     */
    public static boolean isWebstartApplication() {
        return isWebstartApplication;
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
     *
     * @return the {@link SecurityDialogMessageHandler} that should be used to
     * post security dialog messages
     */
    public static SecurityDialogMessageHandler getSecurityDialogHandler() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new AllPermission());
        }
        return securityDialogMessageHandler;
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
     * Sets whether the standard output/error streams should be redirected to
     * the loggging files.
     *
     * @throws IllegalStateException if the runtime has already been initialized
     */
    public static void setRedirectStreams(boolean redirect) {
        checkInitialized();
        redirectStreams = redirect;
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
        } catch (Exception ex) {
            if (!key.equals("RNoResource"))
                return getMessage("RNoResource", new Object[] { key });
            else
                return "Missing resource: " + key;
        }
    }

    /**
     * Returns the localized resource string using the specified
     * arguments.
     *
     * @param args the formatting arguments to the resource string
     */
    public static String getMessage(String key, Object... args) {
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
        } catch (SecurityException ex) {
        }
    }

    /**
     * Load the resources.
     */
    private static void loadResources() {
        try {
            resources = ResourceBundle.getBundle("net.sourceforge.jnlp.resources.Messages");
        } catch (Exception ex) {
            throw new IllegalStateException("Missing resource bundle in netx.jar:net/sourceforge/jnlp/resource/Messages.properties");
        }
    }

    /**
     * @return true if running on Windows
     */
    public static boolean isWindows() {
        String os = System.getProperty("os.name");
        return (os != null && os.startsWith("Windows"));
    }

    /**
     * @return true if running on a Unix or Unix-like system (including Linux
     * and *BSD)
     */
    public static boolean isUnix() {
        String sep = System.getProperty("file.separator");
        return (sep != null && sep.equals("/"));
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

    /**
     * Indicate that netx is running by creating the {@link JNLPRuntime#INSTANCE_FILE} and
     * acquiring a shared lock on it
     */
    public synchronized static void markNetxRunning() {
        if (fileLock != null) return;
        try {
            String message = "This file is used to check if netx is running";

            File netxRunningFile = new File(JNLPRuntime.getConfiguration()
                    .getProperty(DeploymentConfiguration.KEY_USER_NETX_RUNNING_FILE));
            if (!netxRunningFile.exists()) {
                FileUtils.createParentDir(netxRunningFile);
                FileUtils.createRestrictedFile(netxRunningFile, true);
                FileOutputStream fos = new FileOutputStream(netxRunningFile);
                try {
                    fos.write(message.getBytes());
                } finally {
                    fos.close();
                }
            }

            FileInputStream is = new FileInputStream(netxRunningFile);
            FileChannel channel = is.getChannel();
            fileLock = channel.lock(0, 1, true);
            if (!fileLock.isShared()){ // We know shared locks aren't offered on this system.
                FileLock temp = null;
                for (long pos = 1; temp == null && pos < Long.MAX_VALUE - 1; pos++){
                    temp = channel.tryLock(pos, 1, false); // No point in requesting for shared lock.
                }
                fileLock.release(); // We can release now, since we hold another lock.
                fileLock = temp; // Keep the new lock so we can release later.
            }
            
            if (fileLock != null && fileLock.isShared()) {
                if (JNLPRuntime.isDebug()) {
                    System.out.println("Acquired shared lock on " +
                            netxRunningFile.toString() + " to indicate javaws is running");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                markNetxStopped();
                CacheUtil.cleanCache();
            }
        });
    }

    /**
     * Indicate that netx is stopped by releasing the shared lock on
     * {@link JNLPRuntime#INSTANCE_FILE}.
     */
    private static void markNetxStopped() {
        if (fileLock == null) {
            return;
        }
        try {
            fileLock.release();
            fileLock.channel().close();
            fileLock = null;
            if (JNLPRuntime.isDebug()) {
                String file = JNLPRuntime.getConfiguration()
                        .getProperty(DeploymentConfiguration.KEY_USER_NETX_RUNNING_FILE);
                System.out.println("Release shared lock on " + file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void setTrustAll(boolean b) {
        trustAll=b;
    }

    public static boolean isTrustAll() {
        return trustAll;
    }

}
