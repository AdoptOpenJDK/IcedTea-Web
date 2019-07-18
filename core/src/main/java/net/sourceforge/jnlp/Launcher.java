// Copyright (C) 2001-2003 Jon A. Maxwell (JAM)
// Copyright (C) 2019 Karakun AG
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

package net.sourceforge.jnlp;

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.client.parts.splashscreen.SplashUtils;
import net.adoptopenjdk.icedteaweb.jnlp.element.application.AppletDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.application.ApplicationDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.PropertyDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.ResourcesDesc;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.ui.swing.SwingUtils;
import net.sourceforge.jnlp.cache.CacheUtil;
import net.sourceforge.jnlp.cache.UpdatePolicy;
import net.sourceforge.jnlp.runtime.AppletInstance;
import net.sourceforge.jnlp.runtime.ApplicationInstance;
import net.sourceforge.jnlp.runtime.JNLPClassLoader;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.services.InstanceExistsException;
import net.sourceforge.jnlp.services.ServiceUtil;
import net.sourceforge.jnlp.util.JarFile;
import sun.awt.SunToolkit;

import javax.swing.text.html.parser.ParserDelegator;
import java.applet.Applet;
import java.applet.AppletStub;
import java.awt.Container;
import java.awt.SplashScreen;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;
import static net.adoptopenjdk.icedteaweb.launch.JvmLauncherHolder.getLauncher;

/**
 * Launches JNLPFiles either in the foreground or background.
 * <p>
 * An optional LaunchHandler can be specified that is notified of
 * warning and error condition while launching and that indicates
 * whether a launch may proceed after a warning has occurred.  If
 * specified, the LaunchHandler is notified regardless of whether
 * the file is launched in the foreground or background.
 * </p>
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.22 $
 */
public class Launcher {

    private final static Logger LOG = LoggerFactory.getLogger(Launcher.class);

    // defines class Launcher.BgRunner, Launcher.TgThread

    /** shared thread group */
    public static final ThreadGroup mainGroup = new ThreadGroup(R("LAllThreadGroup"));

    /** the handler */
    private final LaunchHandler handler;

    /** the update policy */
    private final UpdatePolicy updatePolicy;

    /** whether to create an AppContext (if possible) */
    private boolean context = true;

    /** If the application should call JNLPRuntime.exit on fatal errors */
    private final boolean exitOnFailure;

    private ParserSettings parserSettings = new ParserSettings();

    private Map<String, List<String>> extra = null;

    public static final String KEY_JAVAWS_LOCATION = "icedtea-web.bin.location";

    /**
     * Create a launcher with the runtime's default update policy
     * and launch handler.
     *
     * @param exitOnFailure exit if there is an error (usually default, but false when being used from the plugin)
     */
    public Launcher(boolean exitOnFailure) {
        this(null, null, exitOnFailure);
    }

    /**
     * Create a launcher with an optional handler using the
     * specified update policy and launch handler.
     *
     * @param handler the handler to use or null for no handler.
     * @param policy the update policy to use or null for default policy.
     * @param exitOnFailure exit if there is an error (usually default, but false when being used from the plugin)
     */
    public Launcher(final LaunchHandler handler, final UpdatePolicy policy, boolean exitOnFailure) {
        this.handler = handler == null ? JNLPRuntime.getDefaultLaunchHandler() : handler;
        this.updatePolicy = policy == null ? JNLPRuntime.getDefaultUpdatePolicy() : policy;
        this.exitOnFailure = exitOnFailure;
    }

    /**
     * Sets whether to launch the application in a new AppContext
     * (a separate event queue, look and feel, etc).  If the
     * sun.awt.SunToolkit class is not present then this method
     * has no effect.  The default value is true.
     * @param context appcontext to be set
     */
    public void setCreateAppContext(boolean context) {
        this.context = context;
    }

    /**
     * @param settings  the parser settings to use when the Launcher initiates parsing of
     * a JNLP file.
     */
    public void setParserSettings(ParserSettings settings) {
        parserSettings = settings;
    }

    /**
     * Set a map to use when trying to extract extra information, including
     * arguments, properties and parameters, to be merged into the main JNLP
     * @param input a map containing extra information to add to the main JNLP.
     * the values for keys "arguments", "parameters", and "properties" are
     * used.
     */
    public void setInformationToMerge(Map<String, List<String>> input) {
        this.extra = input;
    }

    /**
     * Launches a JNLP file by calling the launch method for the
     * appropriate file type.  The application will be started in
     * a new window.
     *
     * @param file the JNLP file to launch
     * @return the application instance
     * @throws LaunchException if an error occurred while launching (also sent to handler)
     */
    public ApplicationInstance launch(final JNLPFile file) throws LaunchException {
        return launch(file, null);
    }

    /**
     * Launches a JNLP file inside the given container if it is an applet.  Specifying a
     * container has no effect for Applications and Installers.
     *
     * @param file the JNLP file to launch
     * @param cont the container in which to place the application, if it is an applet
     * @return the application instance
     * @throws LaunchException if an error occurred while launching (also sent to handler)
     */
    public ApplicationInstance launch(JNLPFile file, Container cont) throws LaunchException {
        TgThread tg;

        mergeExtraInformation(file, extra);

        JNLPRuntime.markNetxRunning();

        if (!JNLPRuntime.isOfflineForced()) {
            //Xoffline NOT specified
            //First checks whether offline-allowed tag is specified inside the jnlp file.
            if (!file.getInformation().isOfflineAllowed() && !JNLPRuntime.isOnlineDetected()) {
                {
                    LOG.error("Remote systems unreachable, and client application is not able to run offline. Exiting.");
                    return null;
                }
            }
        } else {
            //Xoffline IS specified
            if (!file.getInformation().isOfflineAllowed() && !JNLPRuntime.isOnlineDetected()) {
                {
                    LOG.error("Remote systems unreachable, and client application is not able to run offline. However, you specified -Xoffline argument. Attempting to run.");
                }
            }
        }

        tg = new TgThread(file, cont, file instanceof PluginBridge);
        tg.start();

        try {
            tg.join();
        } catch (InterruptedException ex) {
            //By default, null is thrown here, and the message dialog is shown.
            throw launchWarning(new LaunchException(file, ex, "Minor", "System Error", "Thread interrupted while waiting for file to launch.", "This can lead to deadlock or yield other damage during execution. Please restart your application/browser."));
        }

        if (tg.getException() != null) {
            throw tg.getException();
        } // passed to handler when first created

        if (handler != null) {
            handler.launchCompleted(tg.getApplication());
        }

        return tg.getApplication();
    }


    /**
     * Launches a JNLP file by calling the launch method for the
     * appropriate file type.
     *
     * @param location the URL of the JNLP file to launch
     * location to get the pristine version
     * @throws LaunchException if there was an exception
     * @return the application instance
     */
    public ApplicationInstance launch(URL location) throws LaunchException {
        JNLPRuntime.saveHistory(location.toExternalForm());
        return launch(fromUrl(location));
    }

    /**
     * Merges extra information into the jnlp file
     *
     * @param file the JNLPFile
     * @param extra extra information to merge into the JNLP file
     * @throws LaunchException if an exception occurs while extracting
     * extra information
     */
    private void mergeExtraInformation(JNLPFile file, Map<String, List<String>> extra) throws LaunchException {
        if (extra == null) {
            return;
        }

        List<String> properties = extra.get("properties");
        if (properties != null) {
            addProperties(file, properties);
        }

        List<String> arguments = extra.get("arguments");
        if (arguments != null && file.isApplication()) {
            addArguments(file, arguments);
        }

        List<String> parameters = extra.get("parameters");
        if (parameters != null && file.isApplet()) {
            addParameters(file, parameters);
        }
    }

    /**
     * Add the properties to the JNLP file.
     * @throws LaunchException if an exception occurs while extracting
     * extra information
     */
    private void addProperties(JNLPFile file, List<String> props) throws LaunchException {
        ResourcesDesc resources = file.getResources();
        for (String input : props) {
            try{
                resources.addResource(PropertyDesc.fromString(input));
            }catch (LaunchException ex){
                throw launchError(ex);
            }
        }
    }

    /**
     * Add the params to the JNLP file; only call if file is
     * actually an applet file.
     * @throws LaunchException if an exception occurs while extracting
     * extra information
     */
    private void addParameters(JNLPFile file, List<String> params) throws LaunchException {
        AppletDesc applet = file.getApplet();

        for (String input : params) {
            // allows empty param, not sure about validity of that.
            int equals = input.indexOf("=");
            if (equals == -1) {
                throw launchError(new LaunchException("Incorrect parameter format " + input + " (should be name=value)"));
            }

            String name = input.substring(0, equals);
            String value = input.substring(equals + 1);

            applet.addParameter(name, value);
        }
    }

    /**
     * Add the arguments to the JNLP file; only call if file is
     * actually an application (not installer).
     */
    private void addArguments(JNLPFile file, List<String> args) {
        ApplicationDesc app = file.getApplication();

        for (String input : args ) {
            app.addArgument(input);
        }
    }

    /**
     * Launches the JNLP file at the specified location in a new JVM
     * instance. All streams are properly redirected.
     * @param file the JNLP file to read arguments and JVM details from
     * @param javawsArgs the arguments to pass to javaws (aka Netx)
     * @throws LaunchException if there was an exception
     */
    private void launchExternal(final JNLPFile file, final List<String> javawsArgs) throws LaunchException {
        try {
            getLauncher().launchExternal(file, javawsArgs);
        } catch (NullPointerException ex) {
            throw launchError(new LaunchException(null, null, "Fatal", "External Launch Error", "Could not determine location of javaws.jar.", "An attempt was made to launch a JNLP file in another JVM, but the javaws.jar could not be located.  In order to launch in an external JVM, the runtime must be able to locate the javaws.jar file."));
        } catch (Exception ex) {
            throw launchError(new LaunchException(null, ex, "Fatal", "External Launch Error", "Could not launch JNLP file.", "The application has not been initialized, for more information execute javaws/browser from the command line and send a bug report."));
        }
    }

    /**
     * Returns the JNLPFile for the URL, with error handling.
     */
    private JNLPFile fromUrl(URL location) throws LaunchException {
        try {
            JNLPFile file = new JNLPFile(location, parserSettings);

            boolean isLocal = false;
            boolean haveHref = false;
            if ("file".equalsIgnoreCase(location.getProtocol()) && new File(location.getFile()).exists()) {
                isLocal = true;
            }
            if (file.getSourceLocation() != null) {
                haveHref = true;
            }
            if (!isLocal && haveHref){
                //this is case when remote file have href to different file
                if (!location.equals(file.getSourceLocation())){
                    //mark local true, so the following condition will be true and
                    //new jnlp file will be downloaded
                    isLocal = true;
                    //maybe this check is to strict, and will force redownload to often
                    //another check can be just on jnlp name. But it will not work
                    //if the href will be the file of same name, but on different path (or even domain)
                }
            }

            if (isLocal && haveHref) {
                JNLPFile fileFromHref = new JNLPFile(file.getSourceLocation(), parserSettings);
                if (fileFromHref.getCodeBase() == null) {
                    fileFromHref.codeBase = file.getCodeBase();
                }
                file = fileFromHref;

            }
            return file;
        } catch (Exception ex) {
            if (ex instanceof LaunchException) {
                throw (LaunchException) ex; // already sent to handler when first thrown
            } else {
                // IO and Parse
                throw launchError(new LaunchException(null, ex, "Fatal", "Read Error", "Could not read or parse the JNLP file.", "You can try to download this file manually and send it as bug report to IcedTea-Web team."));
            }
        }
    }

   /**
     * Launches a JNLP application.  This method should be called
     * from a thread in the application's thread group.
     * @param file jnlpfile - source of application
     * @return application to be launched
     * @throws net.sourceforge.jnlp.LaunchException if launch fails on unrecoverable exception
     */
   private ApplicationInstance launchApplication(final JNLPFile file) throws LaunchException {
        if (!file.isApplication()) {
            throw launchError(new LaunchException(file, null, "Fatal", "Application Error", "Not an application file.", "An attempt was made to load a non-application file as an application."));
        }

        try {

            try {
                ServiceUtil.checkExistingSingleInstance(file);
            } catch (final InstanceExistsException e) {
                LOG.error("Single instance application is already running.", e);
                return null;
            }

            if (JNLPRuntime.getForksAllowed()) {
                if (!JNLPRuntime.isHeadless()){
                    SplashScreen sp = SplashScreen.getSplashScreen();
                    if (sp!=null) {
                        sp.close();
                    }
                }
                final List<String> javawsArgs = new LinkedList<>();
                javawsArgs.add("-Xnofork");
                javawsArgs.addAll(JNLPRuntime.getInitialArguments());
                launchExternal(file, javawsArgs);
                return null;
            }

            handler.launchInitialized(file);

            ApplicationInstance app = createApplication(file);
            app.initialize();

            String mainName = file.getApplication().getMainClass();

            // When the application-desc field is empty, we should take a
            // look at the main jar for the main class.
            if (mainName == null) {
                JARDesc mainJarDesc = file.getResources().getMainJAR();
                File f = CacheUtil.getCacheFile(mainJarDesc.getLocation(), mainJarDesc.getVersion());
                if (f != null) {
                    JarFile mainJar = new JarFile(f);
                    mainName = mainJar.getManifest().
                                getMainAttributes().getValue("Main-Class");
                }
            }

            if (mainName == null) {
                throw launchError(new LaunchException(file, null,
                        "Fatal", "Application Error", "Unknown Main-Class.",
                        "Could not determine the main class for this application."));
            }

            LOG.info("Starting application [{}] ...", mainName);

            Class<?> mainClass = app.getClassLoader().loadClass(mainName);

            final Method main = mainClass.getMethod("main", String[].class);
            final String[] args = file.getApplication().getArguments();

            // create EDT within application context:
            // dummy method to force Event Dispatch Thread creation
            SwingUtils.callOnAppContext(() -> {});

            setContextClassLoaderForAllThreads(app.getThreadGroup(), app.getClassLoader());

            handler.launchStarting(app);

            main.setAccessible(true);

            LOG.info("Invoking main() with args: {}", Arrays.toString(args));
            main.invoke(null, new Object[] { args });

            return app;
        } catch (LaunchException lex) {
            throw launchError(lex);
        } catch (Exception ex) {
            throw launchError(new LaunchException(file, ex, "Fatal", "Launch Error", "Could not launch JNLP file.", "The application has not been initialized, for more information execute javaws/browser from the command line and send a bug report."));
        }
    }

    /**
     * Set the classloader as the context classloader for all threads in
     * the given threadgroup. This is required to make some applications
     * work. For example, an application that provides a custom Swing LnF
     * may ask the swing thread to load resources from their JNLP, which
     * would only work if the Swing thread knows about the JNLPClassLoader.
     *
     * @param tg The threadgroup for which the context classloader should be set
     * @param classLoader the classloader to set as the context classloader
     */
    private void setContextClassLoaderForAllThreads(ThreadGroup tg, ClassLoader classLoader) {

        /* be prepared for change in thread size */
        int threadCountGuess = tg.activeCount();
        Thread[] threads;
        do {
            threadCountGuess = threadCountGuess * 2;
            threads = new Thread[threadCountGuess];
            tg.enumerate(threads, true);
        } while (threads[threadCountGuess - 1] != null);

        for (Thread thread : threads) {
            if (thread != null) {
                LOG.debug("Setting {} as the classloader for thread {}", classLoader, thread.getName());
                thread.setContextClassLoader(classLoader);
            }
        }

    }

    /**
     * Launches a JNLP applet. This method should be called from a
     * thread in the application's thread group.
     * <p>
     * The enableCodeBase parameter adds the applet's codebase to
     * the locations searched for resources and classes.  This can
     * slow down the applet loading but allows browser-style applets
     * that don't use JAR files exclusively to be run from a applet
     * JNLP file.  If the applet JNLP file does not specify any
     * resources then the code base will be enabled regardless of
     * the specified value.
     * </p>
     *
     * @param file the JNLP file
     * @param enableCodeBase whether to add the codebase URL to the classloader
     * @param cont container where to put application
     * @return application
     * @throws net.sourceforge.jnlp.LaunchException if deploy unrecoverably die
     */
    private ApplicationInstance launchApplet(final JNLPFile file, final boolean enableCodeBase, final Container cont) throws LaunchException {
        if (!file.isApplet()) {
            throw launchError(new LaunchException(file, null, "Fatal", "Application Error", "Not an applet file.", "An attempt was made to load a non-applet file as an applet."));
        }

        if (JNLPRuntime.getForksAllowed() && file.needsNewVM()) {
            if (!JNLPRuntime.isHeadless()) {
                SplashScreen sp = SplashScreen.getSplashScreen();
                if (sp != null) {
                    sp.close();
                }
            }
        }
        if (handler != null) {
            handler.launchInitialized(file);
        }

        AppletInstance applet = null;
        try {
            ServiceUtil.checkExistingSingleInstance(file);
            applet = createApplet(file, enableCodeBase, cont);
            applet.initialize();
            applet.getAppletEnvironment().startApplet(); // this should be a direct call to applet instance
            return applet;
        } catch (InstanceExistsException ieex) {
            LOG.error("Single instance applet is already running.", ieex);
            throw launchError(new LaunchException(file, ieex, "Fatal", "Launch Error", "Could not launch JNLP file.", "Another instance of this applet already exists and only one may be run at the same time."), applet);
        } catch (LaunchException lex) {
            throw launchError(lex, applet);
        } catch (Exception ex) {
            throw launchError(new LaunchException(file, ex, "Fatal", "Launch Error", "Could not launch JNLP file.", "The application has not been initialized, for more information execute javaws/browser from the command line and send a bug report."), applet);
        }finally{
            if (handler != null) {
                handler.launchStarting(applet);
            }
        }
    }

    /**
     * Gets an ApplicationInstance, but does not launch the applet.
     * @param file the JNLP file
     * @param enableCodeBase whether to add the codebase URL to the classloader
     * @param cont container where to put applet
     * @return applet
     * @throws net.sourceforge.jnlp.LaunchException if deploy unrecoverably die
     */
    private ApplicationInstance getApplet(final JNLPFile file, final boolean enableCodeBase, final Container cont) throws LaunchException {
        if (!file.isApplet()) {
            throw launchError(new LaunchException(file, null, "Fatal", "Application Error", "Not an applet file.", "An attempt was made to load a non-applet file as an applet."));
        }
        AppletInstance applet = null;
        try {
            ServiceUtil.checkExistingSingleInstance(file);
            applet = createApplet(file, enableCodeBase, cont);
            applet.initialize();
            return applet;

        } catch (InstanceExistsException ieex) {
            LOG.info("Single instance applet is already running.");
            throw launchError(new LaunchException(file, ieex, "Fatal", "Launch Error", "Could not launch JNLP file.", "Another instance of this applet already exists and only one may be run at the same time."), applet);
        } catch (LaunchException lex) {
            throw launchError(lex, applet);
        } catch (Exception ex) {
            throw launchError(new LaunchException(file, ex, "Fatal", "Launch Error", "Could not launch JNLP file.", "The application has not been initialized, for more information execute javaws/browser from the command line and send a bug report."), applet);
        }
    }

    /**
     * Launches a JNLP installer.  This method should be called from
     * a thread in the application's thread group.
     * @param file jnlp file to read installer from
     * @return  application
     * @throws net.sourceforge.jnlp.LaunchException if deploy unrecoverably die
     */
    private ApplicationInstance launchInstaller(final JNLPFile file) throws LaunchException {
        // TODO Check for an existing single instance once implemented.
        // ServiceUtil.checkExistingSingleInstance(file);
        throw launchError(new LaunchException(file, null, "Fatal", "Unsupported Feature", "Installers not supported.", "JNLP installer files are not yet supported."));
    }

    /**
     * Create an AppletInstance.
     *
     * @param file the JNLP file
     * @param enableCodeBase whether to add the codebase URL to the classloader
     * @param cont container where to put applet
     * @return applet
     * @throws net.sourceforge.jnlp.LaunchException if deploy unrecoverably die
     */
     //FIXME - when multiple applets are on one page, this method is visited simultaneously
    //and then applets creates in little bit strange manner. This issue is visible with
    //randomly showing/notshowing splashscreens.
    //See also PluginAppletViewer.framePanel
    private AppletInstance createApplet(final JNLPFile file, final boolean enableCodeBase, final Container cont) throws LaunchException {
         AppletInstance appletInstance = null;
         try {
            JNLPClassLoader loader = JNLPClassLoader.getInstance(file, updatePolicy, enableCodeBase);

            if (enableCodeBase) {
                loader.enableCodeBase();
            } else if (file.getResources().getJARs().length == 0) {
                throw new ClassNotFoundException("Can't do a codebase look up and there are no jars. Failing sooner rather than later");
            }

            ThreadGroup group = Thread.currentThread().getThreadGroup();

            // appletInstance is needed by ServiceManager when looking up
            // services. This could potentially be done in applet constructor
            // so initialize appletInstance before creating applet.
            if (cont == null) {
                 appletInstance = new AppletInstance(file, group, loader, null);
             } else {
                 appletInstance = new AppletInstance(file, group, loader, null, cont);
             }

             /*
              * Due to PR2968, moved to earlier phase, so early stages of applet
              * can access Thread.currentThread().getContextClassLoader().
              *
              * However it is notable, that init and start still do not have access to right classloader.
              * See LoadResources test.
              */
             setContextClassLoaderForAllThreads(appletInstance.getThreadGroup(), appletInstance.getClassLoader());

            loader.setApplication(appletInstance);

            // Initialize applet now that ServiceManager has access to its
            // appletInstance.
            String appletName = file.getApplet().getMainClass();
            Class<?> appletClass = loader.loadClass(appletName);
            Applet applet = (Applet) appletClass.newInstance();
            applet.setStub((AppletStub)cont);
            // Finish setting up appletInstance.
            appletInstance.setApplet(applet);
            appletInstance.getAppletEnvironment().setApplet(applet);

            return appletInstance;
        } catch (Exception ex) {
            throw launchError(new LaunchException(file, ex, "Fatal", "Initialization Error", "Could not initialize applet.", "For more information click \"more information button\"."), appletInstance);
        }
    }

    /**
     * Creates an Application.
     * @param file the JNLP file
     * @return application
     * @throws net.sourceforge.jnlp.LaunchException if deploy unrecoverably die
     */
    private ApplicationInstance createApplication(final JNLPFile file) throws LaunchException {
        try {
            JNLPClassLoader loader = JNLPClassLoader.getInstance(file, updatePolicy, false);
            ThreadGroup group = Thread.currentThread().getThreadGroup();

            ApplicationInstance app = new ApplicationInstance(file, group, loader);
            loader.setApplication(app);

            return app;
        } catch (Exception ex) {
            throw new LaunchException(file, ex, "Fatal", "Initialization Error", "Could not initialize application.", "The application has not been initialized, for more information execute javaws from the command line.");
        }
    }

    /**
     * Create a thread group for the JNLP file.
     * @param file the JNLP file
     * Note: if the JNLPFile is an applet (ie it is a subclass of PluginBridge)
     * then this method simply returns the existing ThreadGroup. The applet
     * ThreadGroup has to be created at an earlier point in the applet code.
     * @return  ThreadGroup for this app/applet
     */
    private ThreadGroup createThreadGroup(final JNLPFile file) {
        final ThreadGroup tg;

        if (file instanceof PluginBridge) {
            tg = Thread.currentThread().getThreadGroup();
        } else {
            tg = new ThreadGroup(mainGroup, file.getTitle());
        }

        return tg;
    }

    /**
     * Send n launch error to the handler, if set, and also to the
     * caller.
     */
    private LaunchException launchError(LaunchException ex) {
        return launchError(ex, null);
    }

    private LaunchException launchError(LaunchException ex, AppletInstance applet) {
        if (applet != null) {
            SplashUtils.showErrorCaught(ex, applet);
        }
        if (handler != null) {
            handler.launchError(ex);
        }

        return ex;
    }

    /**
     * Send a launch error to the handler, if set, and to the
     * caller only if the handler indicated that the launch should
     * continue despite the warning.
     *
     * @return an exception to throw if the launch should be aborted, or null otherwise
     */
    private LaunchException launchWarning(LaunchException ex) {
        if (handler != null) {
            if (!handler.launchWarning(ex))
                // no need to destroy the app b/c it hasn't started
                return ex;
        } // chose to abort

        return null; // chose to continue, or no handler
    }

    /**
     * Do hacks on per-application level to allow different AppContexts to work
     *
     * @see JNLPRuntime#doMainAppContextHacks
     */
    private static void doPerApplicationAppContextHacks() {

        /*
         * With OpenJDK6 (but not with 7) a per-AppContext dtd is maintained.
         * This dtd is created by the ParserDelegate. However, the code in
         * HTMLEditorKit (used to render HTML in labels and textpanes) creates
         * the ParserDelegate only if there are no existing ParserDelegates. The
         * result is that all other AppContexts see a null dtd.
         */
        new ParserDelegator();
    }

       /**
     * This runnable is used to call the appropriate launch method
     * for the application, applet, or installer in its thread group.
     */
    private class TgThread extends Thread { // ThreadGroupThread
        private final JNLPFile file;
        private ApplicationInstance application;
        private LaunchException exception;
        private final Container cont;
        private final boolean isPlugin;

        TgThread(JNLPFile file, Container cont, boolean isPlugin) {
            super(createThreadGroup(file), file.getTitle());
            this.file = file;
            this.cont = cont;
            this.isPlugin = isPlugin;
        }

        @Override
        public void run() {
            try {
                // Do not create new AppContext if we're using NetX and icedteaplugin.
                // The plugin needs an AppContext too, but it has to be created earlier.
                if (context && !isPlugin) {
                    SunToolkit.createNewAppContext();
                }

                doPerApplicationAppContextHacks();

                if (isPlugin) {
                    // Do not display download indicators if we're using gcjwebplugin.
                    JNLPRuntime.setDefaultDownloadIndicator(null);
                    application = getApplet(file, ((PluginBridge)file).codeBaseLookup(), cont);
                } else {
                    if (file.isApplication()) {
                        application = launchApplication(file);
                    }
                    else if (file.isApplet()) {
                        application = launchApplet(file, true, cont);
                    } // enable applet code base
                    else if (file.isInstaller()) {
                        application = launchInstaller(file);
                    }
                    else {
                        throw launchError(new LaunchException(file, null,
                                "Fatal", "Application Error", "Not a launchable JNLP file.",
                                "File must be a JNLP application, applet, or installer type."));
                    }
                }
            } catch (LaunchException ex) {
                LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
                exception = ex;
                // Exit if we can't launch the application.
                if (exitOnFailure) {
                    JNLPRuntime.exit(1);
                }
            }  catch (Throwable ex) {
                LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
                throw new RuntimeException(ex);
            }
        }

        LaunchException getException() {
            return exception;
        }

        ApplicationInstance getApplication() {
            return application;
        }

    }

}
