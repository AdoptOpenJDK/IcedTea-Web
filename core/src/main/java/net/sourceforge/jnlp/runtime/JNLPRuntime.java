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

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.JavaSystemProperties;
import net.adoptopenjdk.icedteaweb.client.BasicExceptionDialog;
import net.adoptopenjdk.icedteaweb.client.GuiLaunchHandler;
import net.adoptopenjdk.icedteaweb.client.console.JavaConsole;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.SecurityDialogMessageHandler;
import net.adoptopenjdk.icedteaweb.client.parts.downloadindicator.DownloadIndicator;
import net.adoptopenjdk.icedteaweb.extensionpoint.ExtensionPoint;
import net.adoptopenjdk.icedteaweb.io.FileUtils;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.resources.UpdatePolicy;
import net.adoptopenjdk.icedteaweb.resources.cache.Cache;
import net.sourceforge.jnlp.DefaultLaunchHandler;
import net.sourceforge.jnlp.LaunchHandler;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.config.PathsAndFiles;
import net.sourceforge.jnlp.security.JNLPAuthenticator;
import net.sourceforge.jnlp.security.KeyStores;
import net.sourceforge.jnlp.security.SecurityUtil;
import net.sourceforge.jnlp.services.XServiceManagerStub;
import net.sourceforge.jnlp.util.RestrictedFileUtils;
import net.sourceforge.jnlp.util.logging.LogConfig;
import net.sourceforge.jnlp.util.logging.OutputController;
import sun.net.www.protocol.jar.URLJarFile;

import javax.jnlp.ServiceManager;
import javax.naming.ConfigurationException;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.text.html.parser.ParserDelegator;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.Authenticator;
import java.net.InetAddress;
import java.net.ProxySelector;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.security.AllPermission;
import java.security.KeyStore;
import java.security.Policy;
import java.security.Security;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import static net.sourceforge.jnlp.runtime.ForkingStrategy.IF_JNLP_REQUIRES;
import static net.sourceforge.jnlp.util.UrlUtils.FILE_PROTOCOL;

/**
 * <p>
 * Configure and access the runtime environment.  This class
 * stores global jnlp properties such as default download
 * indicators, the install/base directory, the default resource
 * update policy, etc.  Some settings, such as the base directory,
 * cannot be changed once the runtime has been initialized.
 * </p>
 * <p>
 * The JNLP runtime can be locked to prevent further changes to
 * the runtime environment except by a specified class.  If set,
 * only instances of the <i>exit class</i> can exit the JVM or
 * change the JNLP runtime settings once the runtime has been
 * initialized.
 * </p>
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.19 $
 */
public class JNLPRuntime {

    private final static Logger LOG = LoggerFactory.getLogger(JNLPRuntime.class);

    /**
     * java-abrt-connector can print out specific application String method, it is good to save visited urls for reproduce purposes.
     * For javaws we can read the destination jnlp from commandline
     * However for plugin (url arrive via pipes). Also for plugin we can not be sure which opened tab/window
     * have caused the crash. That's why the individual urls are added, not replaced.
     */
    private static String history = "";

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
    private static boolean headlessChecked = false;

    /** whether we'll be checking for jar signing */
    private static boolean verify = true;

    /** whether the runtime uses security */
    private static boolean securityEnabled = true;

    /** whether debug mode is on */
    private static boolean debug = false;

    /**
     * whether plugin debug mode is on
     */
    private static Boolean pluginDebug = null;

    /** mutex to wait on, for initialization */
    public static Object initMutex = new Object();

    /** set to NEVER to indicate another JVM should not be spawned, even if necessary */
    private static ForkingStrategy forkingStrategy = IF_JNLP_REQUIRES;

    /** all security dialogs will be consumed and pretented as being verified by user and allowed.*/
    private static boolean trustAll=false;

    /** all security dialogs will be consumed and we will pretend the Sandbox NumberOfArguments was chosen */
    private static boolean trustNone = false;

    /** allows 301.302.303.307.308 redirects to be followed when downloading resources*/
    private static boolean allowRedirect = false;;

    /** when this is true, ITW will not attempt any inet connections and will work only with what is in cache*/
    private static boolean offlineForced = false;

    private static Boolean onlineDetected = null;


    /**
     * Header is not checked and so eg
     * <a href="https://en.wikipedia.org/wiki/Gifar">gifar</a> exploit is
     * possible.<br/>
     * However if jar file is a bit corrupted, then it sometimes can work so
     * this switch can disable the header check.
     * @see <a href="https://en.wikipedia.org/wiki/Gifar">Gifar attack</a>
     */
    private static boolean ignoreHeaders=false;

    /** contains the arguments passed to the jnlp runtime */
    private static List<String> initialArguments;
    private static String jnlpPath;

    /** a lock which is held to indicate that an instance of netx is running */
    private static FileLock fileLock;

    /** flag to prevent show the splash screen at start of webstart application **/
    private static boolean showWebSplash = true;

    /**
     * Returns whether the JNLP runtime environment has been
     * initialized. Once initialized, some properties such as the
     * base directory cannot be changed. Before
     * @return whether this runtime was already initialized
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Initialize the JNLP runtime environment by installing the
     * security manager and security policy, initializing the JNLP
     * standard services, etc.
     * <p>
     * This method should be called from the main AppContext/Thread.
     * </p>
     * <p>
     * This method cannot be called more than once. Once
     * initialized, methods that alter the runtime can only be
     * called by the exit class.
     * </p>
     *
     * @throws IllegalStateException if the runtime was previously initialized
     */
    public static void initialize() throws IllegalStateException {
        checkInitialized();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (final Exception e) {
            LOG.error("Unable to set system look and feel", e);
        }

        if (JavaConsole.canShowOnStartup()) {
            JavaConsole.getConsole().showConsoleLater();
        }
        /* exit if there is a fatal exception loading the configuration */
        final ConfigurationException loadingException = getConfiguration().getLoadingException();
        if (loadingException != null) {
            throw new RuntimeException(loadingException);
        }

        //Setting the system property for javawebstart's version.
        //The version stored will be the same as java's version.
        System.setProperty("javawebstart.version", "javaws-" +
                JavaSystemProperties.getJavaVersion());

        if (!isHeadless()) {
            indicator = JNLPRuntime.getExtensionPoint().createDownloadIndicator(getConfiguration());
        }

        if (handler == null) {
            if (isHeadless()) {
                handler = new DefaultLaunchHandler(OutputController.getLogger());
            } else {
                handler = new GuiLaunchHandler(OutputController.getLogger());
            }
        }

        ServiceManager.setServiceManagerStub(new XServiceManagerStub()); // ignored if we're running under Web Start

        policy = new JNLPPolicy();
        security = new JNLPSecurityManager(); // side effect: create JWindow

        doMainAppContextHacks();

        if (securityEnabled && forkingStrategy.mayRunManagedApplication()) {
            Policy.setPolicy(policy); // do first b/c our SM blocks setPolicy
            System.setSecurityManager(security);
        }

        securityDialogMessageHandler = startSecurityThreads();

        // wire in custom authenticator for SSL connections
        try {
            SSLSocketFactory sslSocketFactory;
            SSLContext context = SSLContext.getInstance("SSL");
            KeyStore ks = KeyStores.getWrapContainer(KeyStores.Level.USER, KeyStores.Type.CLIENT_CERTS).getWrap().getKs();
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            SecurityUtil.initKeyManagerFactory(kmf, ks);
            TrustManager[] trust = new TrustManager[] { getSSLSocketTrustManager() };
            context.init(kmf.getKeyManagers(), trust, null);
            sslSocketFactory = context.getSocketFactory();

            HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory);
        } catch (Exception e) {
            LOG.error("Unable to set SSLSocketfactory (may _prevent_ access to sites that should be trusted)! Continuing anyway...", e);
        }

        // plug in a custom authenticator and proxy selector
        Authenticator.setDefault(new JNLPAuthenticator());
        ProxySelector proxySelector = getExtensionPoint().createProxySelector(getConfiguration());
        ProxySelector.setDefault(proxySelector);

        // Restrict access to netx classes
        Security.setProperty("package.access",
                             Security.getProperty("package.access")+",net.sourceforge.jnlp");

        URLJarFile.setCallBack(CachedJarFileCallback.getInstance());

        initialized = true;

    }

    public static void reloadPolicy() {
        policy.refresh();

    }

    /**
     * Returns a TrustManager ideal for the running VM.
     *
     * @return TrustManager the trust manager to use for verifying https certificates
     */
    private static TrustManager getSSLSocketTrustManager() throws
                                ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException {

        try {

            Class<?> trustManagerClass;
            Constructor<?> tmCtor;

            if (JavaSystemProperties.getJavaVersion().startsWith("1.6")) { // Java 6
                try {
                    trustManagerClass = Class.forName("net.sourceforge.jnlp.security.VariableX509TrustManagerJDK6");
                 } catch (ClassNotFoundException cnfe) {
                     LOG.warn("Unable to find class net.sourceforge.jnlp.security.VariableX509TrustManagerJDK6");
                     return null;
                 }
            } else { // Java 7 or more (technically could be <= 1.5 but <= 1.5 is unsupported)
                try {
                    trustManagerClass = Class.forName("net.sourceforge.jnlp.security.VariableX509TrustManagerJDK7");
                 } catch (ClassNotFoundException cnfe) {
                     LOG.error("Unable to find class net.sourceforge.jnlp.security.VariableX509TrustManagerJDK7", cnfe);
                     return null;
                 }
            }

            Constructor<?>[] tmCtors = trustManagerClass.getDeclaredConstructors();
            tmCtor = tmCtors[0];

            for (Constructor<?> ctor : tmCtors) {
                if (tmCtor.getGenericParameterTypes().length == 0) {
                    tmCtor = ctor;
                    break;
                }
            }

            return (TrustManager) tmCtor.newInstance();
        } catch (RuntimeException e) {
            LOG.error("Unable to load JDK-specific TrustManager. Was this version of IcedTea-Web compiled with JDK 6 or 7?", e);
            throw e;
        }
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
     * see Launcher#doPerApplicationAppContextHacks
     */
    private static void doMainAppContextHacks() {

        /*
         * With OpenJDK6 (but not with 7) a per-AppContext dtd is maintained.
         * This dtd is created by the ParserDelegate. However, the code in
         * HTMLEditorKit (used to render HTML in labels and textpanes) creates
         * the ParserDelegate only if there are no existing ParserDelegates. The
         * result is that all other AppContexts see a null dtd.
         */
        new ParserDelegator();
    }


    public static void setOfflineForced(boolean b) {
        offlineForced = b;
        LOG.debug("Forcing of offline set to: {}", offlineForced);
    }

    public static boolean isOfflineForced() {
        return offlineForced;
    }

    public static void setOnlineDetected(boolean online) {
        onlineDetected = online;
        LOG.debug("Detected online set to: {}", onlineDetected);
    }

    public static boolean isOnlineDetected() {
        if (onlineDetected == null) {
            //"file" protocol do not do online check
            //suggest online for this case
            return true;
        }
        return onlineDetected;
    }

    public static boolean isOnline() {
        if (isOfflineForced()) {
            return false;
        }
        return isOnlineDetected();
    }

    public static void detectOnline(URL location) {
        if (onlineDetected != null) {
            return;
        }

        JNLPRuntime.setOnlineDetected(isConnectable(location));
    }

    public static boolean isConnectable(URL location) {
        if (location.getProtocol().equals(FILE_PROTOCOL)) {
            return true;
        }

        try {
            InetAddress.getByName(location.getHost());
        } catch (UnknownHostException e) {
            LOG.error("The host of " + location.toExternalForm() + " file seems down, or you are simply offline.", e);
            return false;
        }

        return true;
    }

    /**
     * see <a href="https://en.wikipedia.org/wiki/Double-checked_locking#Usage_in_Java">Double-checked locking in Java</a>
     * for cases how not to do lazy initialization
     * and <a href="https://en.wikipedia.org/wiki/Initialization_on_demand_holder_idiom">Initialization on demand holder idiom</a>
     * for ITW approach
     */
    private static class DeploymentConfigurationHolder {

        private static final DeploymentConfiguration INSTANCE;

        static {
            DeploymentConfiguration config = new DeploymentConfiguration();
            try {
                config.load();
                config.copyTo(System.getProperties());
            } catch (ConfigurationException ex) {
                LOG.info("Fatal error while reading the configuration, continuing with empty. Please fix");
                //mark this exceptionas we can die on it later
                config.setLoadingException(ex);
                //to be sure - we MUST die - http://docs.oracle.com/javase/6/docs/technotes/guides/deployment/deployment-guide/properties.html
            } catch (Exception t) {
                LOG.error("Failing to default configuration", t);
                if (!JNLPRuntime.isHeadless()) {
                    JOptionPane.showMessageDialog(null, "Failing to default configuration\n" + t.toString());
                }
                //try to survive this unlikely exception
                config.resetToDefaults();
            } finally {
                INSTANCE = config;
                OutputController.getLogger().startConsumer();
            }
        }
    }

    /**
     * Gets the Configuration associated with this runtime
     *
     * @return a {@link DeploymentConfiguration} object that can be queried to
     * find relevant configuration settings
     */
    public static DeploymentConfiguration getConfiguration() {
        return DeploymentConfigurationHolder.INSTANCE;
    }

    /**
     * @return whether the JNLP client will use any AWT/Swing
     * components.
     */
    public static boolean isHeadless() {
        if (!headless && !headlessChecked) {
            checkHeadless();

        }
        return headless;
    }

    /**
     * @return whether we are verifying code signing.
     */
    public static boolean isVerifying() {
        return verify;
    }

    /**
     * Sets whether the JNLP client will use any AWT/Swing
     * components.  In headless mode, client features that use the
     * AWT are disabled such that the client can be used in
     * headless mode ({@code java.awt.headless=true}).
     *
     * @param enabled true if application do not wont/need gui or X at all
     * @throws IllegalStateException if the runtime was previously initialized
     */
    public static void setHeadless(boolean enabled) {
        checkInitialized();
        headless = enabled;
    }

    public static void setAllowRedirect(boolean enabled) {
        checkInitialized();
        allowRedirect = enabled;
    }

    public static boolean isAllowRedirect() {
        return allowRedirect;
    }


    /**
     * Sets whether we will verify code signing.
     *
     * @param enabled true if app should verify signatures
     * @throws IllegalStateException if the runtime was previously initialized
     */
    public static void setVerify(boolean enabled) {
        checkInitialized();
        verify = enabled;
    }

    /**
     * Returns whether the secure runtime environment is enabled.
     * @return true if security manager is created
     */
    public static boolean isSecurityEnabled() {
        return securityEnabled;
    }

    /**
     * Sets whether to enable the secure runtime environment.
     * Disabling security can increase performance for some
     * applications, and can be used to use netx with other code
     * that uses its own security manager or policy.
     * <p>
     * Disabling security is not recommended and should only be
     * used if the JNLP files opened are trusted. This method can
     * only be called before initializing the runtime.
     * </p>
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
     * @param exitClass a class that can exit the JVM
     * @throws IllegalStateException if caller is not the exit class
     */
    public static void setExitClass(Class<?> exitClass) {
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
     * @return the current Application, or null if none can be
     * determined.
     */
    public static ApplicationInstance getApplication() {
        return security.getApplication();
    }

    /**
     * @return whether debug statements for the JNLP client code
     * should be printed.
     */
    public static boolean isDebug() {
        return isSetDebug() ||  isPluginDebug() || LogConfig.getLogConfig().isDebugEnable();
    }

     public static boolean isSetDebug() {
        return debug;
    }

    /**
     * Sets whether debug statements for the JNLP client code
     * should be printed to the standard output.
     *
     * @param enabled set to true if you need full debug output
     * @throws IllegalStateException if caller is not the exit class
     */
    public static void setDebug(boolean enabled) {
        checkExitClass();
        debug = enabled;
    }


    /**
     * Sets the default update policy.
     *
     * @param policy global update policy of environment
     * @throws IllegalStateException if caller is not the exit class
     */
    public static void setDefaultUpdatePolicy(UpdatePolicy policy) {
        checkExitClass();
        updatePolicy = policy;
    }

    /**
     * @return the default update policy.
     */
    public static UpdatePolicy getDefaultUpdatePolicy() {
        return updatePolicy;
    }

    /**
     * Sets the default launch handler.
     * @param handler default handler
     */
    public static void setDefaultLaunchHandler(LaunchHandler handler) {
        checkExitClass();
        JNLPRuntime.handler = handler;
    }

    /**
     * Returns the default launch handler.
     * @return default handler
     */
    public static LaunchHandler getDefaultLaunchHandler() {
        return handler;
    }

    /**
     * Sets the default download indicator.
     *
     * @param indicator where to show progress
     * @throws IllegalStateException if caller is not the exit class
     */
    public static void setDefaultDownloadIndicator(DownloadIndicator indicator) {
        LOG.debug("Trying to set download indicator");
        checkExitClass();
        LOG.debug("Setting download indicator to " + indicator);
        JNLPRuntime.indicator = indicator;
    }

    /**
     * @return the default download indicator.
     */
    public static DownloadIndicator getDefaultDownloadIndicator() {
        return indicator;
    }

    public static String getLocalisedTimeStamp(Date timestamp) {
        return DateFormat.getInstance().format(timestamp);
    }

    /**
     * @return {@code true} if the current runtime will fork
     */
    public static ForkingStrategy getForksStrategy() {
        return forkingStrategy;
    }

    public static void setForkingStrategy(ForkingStrategy strategy) {
        checkInitialized();
        forkingStrategy = strategy;
    }

    /**
     * Throws an exception if called when the runtime is already initialized.
     */
    private static void checkInitialized() {
        if (initialized)
            throw new IllegalStateException("JNLPRuntime already initialized.");
    }

    /**
     * Throws an exception if called with security enabled but a caller is not
     * the exit class and the runtime has been initialized.
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
            if ("true".equalsIgnoreCase(JavaSystemProperties.getAwtHeadless())) {
                headless = true;
            }
            if (!headless) {
                boolean noCheck = Boolean.valueOf(JNLPRuntime.getConfiguration().getProperty(ConfigurationConstants.IGNORE_HEADLESS_CHECK));
                if (noCheck) {
                    headless = false;
                    LOG.debug("{} set to {}. Avoiding headless check.", ConfigurationConstants.IGNORE_HEADLESS_CHECK, noCheck);
                } else {
                    try {
                        if (GraphicsEnvironment.isHeadless()) {
                            throw new HeadlessException();
                        }
                    } catch (HeadlessException ex) {
                        headless = true;
                        LOG.error("Headless check failed. You are forced to run without any graphics. IcedTea-Web can run like this, but your app probably not. This is likely bug in your system.", ex);
                    }
                }
            }
        } catch (SecurityException ex) {
        } finally {
            headlessChecked = true;
        }
    }

    /**
     * @return {@code true} if running on a Unix or Unix-like system (including
     * Linux and *BSD)
     */
    @Deprecated
    public static boolean isUnix() {
        String sep = JavaSystemProperties.getFileSeparator();
        return (sep != null && sep.equals("/"));
    }

    public static void setInitialArguments(List<String> args) {
        checkInitialized();
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null)
            securityManager.checkPermission(new AllPermission());
        initialArguments = args;
    }

    static void setJnlpPath(String jnlpPath) {
        JNLPRuntime.jnlpPath = jnlpPath;
    }

    public static String getJnlpPath() {
        return jnlpPath;
    }

    public static List<String> getInitialArguments() {
        return initialArguments;
    }

    /**
     * Indicate that netx is running by creating the
     * {@link ConfigurationConstants#KEY_USER_NETX_RUNNING_FILE} and
     * acquiring a shared lock on it
     */
    public synchronized static void markNetxRunning() {
        if (fileLock != null) return;
        try {
            String message = "This file is used to check if netx is running";

            File netxRunningFile = PathsAndFiles.MAIN_LOCK.getFile();
            if (!netxRunningFile.exists()) {
                FileUtils.createParentDir(netxRunningFile);
                RestrictedFileUtils.createRestrictedFile(netxRunningFile);
                try (FileOutputStream fos = new FileOutputStream(netxRunningFile)) {
                    fos.write(message.getBytes());
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
                LOG.debug("Acquired shared lock on {} to indicate javaws is running", netxRunningFile);
            }
        } catch (IOException e) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
        }

        Runtime.getRuntime().addShutdownHook(new Thread("JNLPRuntimeShutdownHookThread") {
            @Override
            public void run() {
                markNetxStopped();
                Cache.cleanCache();
            }
        });
    }

    /**
     * Indicate that netx is stopped by releasing the shared lock on
     * {@link ConfigurationConstants#KEY_USER_NETX_RUNNING_FILE}.
     */
    private static void markNetxStopped() {
        if (fileLock == null) {
            return;
        }
        try {
            fileLock.release();
            fileLock.channel().close();
            fileLock = null;
            LOG.debug("Release shared lock on {}", PathsAndFiles.MAIN_LOCK.getFullPath());
        } catch (IOException e) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
        }
    }

    public static void setTrustAll(boolean b) {
        trustAll=b;
    }

    public static boolean isTrustAll() {
        return trustAll;
    }

    public static void setTrustNone(final boolean b) {
        trustNone = b;
    }

    public static boolean isTrustNone() {
        return trustNone;
    }

    public static boolean isIgnoreHeaders() {
        return ignoreHeaders;
    }

    public static void setIgnoreHeaders(boolean ignoreHeaders) {
        JNLPRuntime.ignoreHeaders = ignoreHeaders;
    }

    private static boolean isPluginDebug() {
        if (pluginDebug == null) {
            try {
                //there are cases when this itself is not allowed by security manager, and so
                //throws exception. Under some conditions it can cause deadlock
                pluginDebug = System.getenv().containsKey("ICEDTEAPLUGIN_DEBUG");
            } catch (Exception ex) {
                pluginDebug = false;
                LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
            }
        }
        return pluginDebug;
    }

    public static <T> T exit(int i) {
        waitForExceptionDialogsToBeClosed();
        System.exit(i);
        return null;
    }

    public static void waitForExceptionDialogsToBeClosed() {
        try {
            if (BasicExceptionDialog.areShown()) {
                LOG.debug("Waiting for exception dialog to be closed");
            }
            while (BasicExceptionDialog.areShown()){
                Thread.sleep(100);
            }
        } catch (Exception ex) {
            LOG.debug("Exception while waiting for ExceptionDialog to close", ex);
        }
    }


    public static void saveHistory(String documentBase) {
        JNLPRuntime.history += " " + documentBase + " ";
    }

    /**
     * Used by java-abrt-connector via reflection
     * @return history
     */
    private static String getHistory() {
        return history;
    }

	/**
	 * @param showWebSplash show splash screen at start of webstart application
	 */
	public static void setShowWebSplash(boolean showWebSplash) {
		JNLPRuntime.showWebSplash = showWebSplash;
	}

    /**
     * @return show splash screen at start of webstart application
     */
    public static boolean isShowWebSplash() {
        return showWebSplash;
    }

    public static ExtensionPoint getExtensionPoint() {
        return ExtensionPointHolder.INSTANCE;
    }

    private static class ExtensionPointHolder {
        private static final ExtensionPoint INSTANCE;

        static {
            final List<ExtensionPoint> providers = new ArrayList<>();
            final ServiceLoader<ExtensionPoint> serviceLoader = ServiceLoader.load(ExtensionPoint.class);
            serviceLoader.iterator().forEachRemaining(providers::add);

            if (providers.size() == 0) {
                INSTANCE = ExtensionPoint.DEFAULT;
                LOG.debug("using DEFAULT extension point");
            } else if (providers.size() == 1) {
                INSTANCE = providers.get(0);
                LOG.debug("using {} extension point", INSTANCE.getClass().getName());
            } else {
                final List<String> implNames = providers.stream().map(ep -> ep.getClass().getName()).collect(Collectors.toList());
                final String msg = "Found more than one ExtensionPoint implementation: " + implNames;
                LOG.error(msg);
                throw new IllegalStateException(msg);
            }
        }
    }
}
