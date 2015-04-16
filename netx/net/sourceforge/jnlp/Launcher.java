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

package net.sourceforge.jnlp;

import static net.sourceforge.jnlp.runtime.Translator.R;

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
import net.sourceforge.jnlp.util.JarFile;

import net.sourceforge.jnlp.cache.CacheUtil;
import net.sourceforge.jnlp.cache.UpdatePolicy;
import net.sourceforge.jnlp.runtime.AppletInstance;
import net.sourceforge.jnlp.runtime.ApplicationInstance;
import net.sourceforge.jnlp.runtime.JNLPClassLoader;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.services.InstanceExistsException;
import net.sourceforge.jnlp.services.ServiceUtil;

import javax.swing.SwingUtilities;
import javax.swing.text.html.parser.ParserDelegator;
import net.sourceforge.jnlp.splashscreen.SplashUtils;
import net.sourceforge.jnlp.util.logging.OutputController;

import sun.awt.SunToolkit;

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

    // defines class Launcher.BgRunner, Launcher.TgThread

    /** shared thread group */
    /*package*/static final ThreadGroup mainGroup = new ThreadGroup(R("LAllThreadGroup"));

    /** the handler */
    private LaunchHandler handler = null;

    /** the update policy */
    private UpdatePolicy updatePolicy = JNLPRuntime.getDefaultUpdatePolicy();

    /** whether to create an AppContext (if possible) */
    private boolean context = true;

    /** If the application should call JNLPRuntime.exit on fatal errors */
    private boolean exitOnFailure = true;

    private ParserSettings parserSettings = new ParserSettings();

    private Map<String, List<String>> extra = null;

    /**
     * Create a launcher with the runtime's default update policy
     * and launch handler.
     */
    public Launcher() {
        this(null, null);

        if (handler == null) {
            handler = JNLPRuntime.getDefaultLaunchHandler();
        }
    }

    /**
     * Create a launcher with the runtime's default update policy
     * and launch handler.
     *
     * @param exitOnFailure Exit if there is an error (usually default, but false when being used from the plugin)
     */
    public Launcher(boolean exitOnFailure) {
        this(null, null);

        if (handler == null) {
            handler = JNLPRuntime.getDefaultLaunchHandler();
        }

        this.exitOnFailure = exitOnFailure;
    }

    /**
     * Create a launcher with the specified handler and the
     * runtime's default update policy.
     *
     * @param handler the handler to use or null for no handler.
     */
    public Launcher(LaunchHandler handler) {
        this(handler, null);
    }

    /**
     * Create a launcher with an optional handler using the
     * specified update policy and launch handler.
     *
     * @param handler the handler to use or null for no handler.
     * @param policy the update policy to use or null for default policy.
     */
    public Launcher(LaunchHandler handler, UpdatePolicy policy) {
        if (policy == null)
            policy = JNLPRuntime.getDefaultUpdatePolicy();

        this.handler = handler;
        this.updatePolicy = policy;

    }

    /**
     * Sets the update policy used by launched applications.
     * @param policy to be used for resources
     */
    public void setUpdatePolicy(UpdatePolicy policy) {
        if (policy == null) {
            throw new IllegalArgumentException(R("LNullUpdatePolicy"));
        }

        this.updatePolicy = policy;
    }

    /**
     * @return the update policy used when launching applications.
     */
    public UpdatePolicy getUpdatePolicy() {
        return updatePolicy;
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
     * @return whether applications are launched in their own
     * AppContext.
     */
    public boolean isCreateAppContext() {
        return this.context;
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
    public ApplicationInstance launch(JNLPFile file) throws LaunchException {
        return launch(file, null);
    }

    /**
     * Launches a JNLP file inside the given container if it is an applet.  Specifying a
     * container has no effect for Applcations and Installers.
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
                    OutputController.getLogger().log(OutputController.Level.ERROR_ALL, "Remote systems unreachable, and client application is not able to run offline. Exiting.");
                    return null;
                }
            }
        } else {
            //Xoffline IS specified
            if (!file.getInformation().isOfflineAllowed() && !JNLPRuntime.isOnlineDetected()) {
                {
                    OutputController.getLogger().log(OutputController.Level.ERROR_ALL, "Remote systems unreachable, and client application is not able to run offline. However, you specified -Xoffline argument. Attmpting to run.");
                }
            }
        }

        if (file instanceof PluginBridge && cont != null) {
            tg = new TgThread(file, cont, true);
        }
        else if (cont == null) {
            tg = new TgThread(file);
        }
        else {
            tg = new TgThread(file, cont);
        }

        tg.start();

        try {
            tg.join();
        } catch (InterruptedException ex) {
            //By default, null is thrown here, and the message dialog is shown.
            throw launchWarning(new LaunchException(file, ex, R("LSMinor"), R("LCSystem"), R("LThreadInterrupted"), R("LThreadInterruptedInfo")));
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
                throw launchError(new LaunchException(R("BBadParam", input)));
            }

            String name = input.substring(0, equals);
            String value = input.substring(equals + 1, input.length());

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
     * Launches the JNLP file in a new JVM instance.  The launched
     * application's output is sent to the system out and it's
     * standard input channel is closed.
     *
     * @param vmArgs the arguments to pass to the new JVM. Can be empty but
     *        must not be null.
     * @param file the JNLP file to launch
     * @param javawsArgs the arguments to pass to the javaws command. Can be
     *        an empty list but must not be null.
     * @throws LaunchException if there was an exception
     */
    public void launchExternal(List<String> vmArgs, JNLPFile file, List<String> javawsArgs) throws LaunchException {
        List<String> updatedArgs = new LinkedList<String>(javawsArgs);

        if (file.getFileLocation() != null) {
            updatedArgs.add(file.getFileLocation().toString());
        }
        else if (file.getSourceLocation() != null) {
            updatedArgs.add(file.getFileLocation().toString());
        }
        else {
            launchError(new LaunchException(file, null, R("LSFatal"), R("LCExternalLaunch"), R("LNullLocation"), R("LNullLocationInfo")));
        }

        launchExternal(vmArgs, updatedArgs);

    }

    /**
     * Launches the JNLP file in a new JVM instance.  The launched
     * application's output is sent to the system out and it's
     * standard input channel is closed.
     *
     * @param url the URL of the JNLP file to launch
     * @throws LaunchException if there was an exception
     */
    public void launchExternal(URL url) throws LaunchException {
        List<String> javawsArgs = new LinkedList<String>();
        javawsArgs.add(url.toString());
        launchExternal(new LinkedList<String>(), javawsArgs);
    }

    /**
     * Launches the JNLP file at the specified location in a new JVM
     * instance.  The launched application's output is sent to the
     * system out and it's standard input channel is closed.
     * @param vmArgs the arguments to pass to the jvm
     * @param javawsArgs the arguments to pass to javaws (aka Netx)
     * @throws LaunchException if there was an exception
     */
    public void launchExternal(List<String> vmArgs, List<String> javawsArgs) throws LaunchException {
        try {

            List<String> commands = new LinkedList<String>();

            // this property is set by the javaws launcher to point to the javaws binary
            String pathToWebstartBinary = System.getProperty("icedtea-web.bin.location");
            commands.add(pathToWebstartBinary);
            // use -Jargument format to pass arguments to the JVM through the launcher
            for (String arg : vmArgs) {
                commands.add("-J" + arg);
            }
            commands.addAll(javawsArgs);

            String[] command = commands.toArray(new String[] {});

            Process p = Runtime.getRuntime().exec(command);
            new StreamEater(p.getErrorStream()).start();
            new StreamEater(p.getInputStream()).start();
            p.getOutputStream().close();

        } catch (NullPointerException ex) {
            throw launchError(new LaunchException(null, null, R("LSFatal"), R("LCExternalLaunch"), R("LNetxJarMissing"), R("LNetxJarMissingInfo")));
        } catch (Exception ex) {
            throw launchError(new LaunchException(null, ex, R("LSFatal"), R("LCExternalLaunch"), R("LCouldNotLaunch"), R("LCouldNotLaunchInfo")));
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
                    //mark local true, so the folowing condition will be true and 
                    //new jnlp file will be downlaoded
                    isLocal = true;
                    //maybe this check is to strict, and will force redownlaod to often
                    //another check can be just on jnlp name. But it will not work
                    //if the href will be the file of same name, but on diferent path (or even domain)
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
                throw launchError(new LaunchException(null, ex, R("LSFatal"), R("LCReadError"), R("LCantRead"), R("LCantReadInfo")));
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
    protected ApplicationInstance launchApplication(JNLPFile file) throws LaunchException {
        if (!file.isApplication()) {
            throw launchError(new LaunchException(file, null, R("LSFatal"), R("LCClient"), R("LNotApplication"), R("LNotApplicationInfo")));
        }

        try {

            try {
                ServiceUtil.checkExistingSingleInstance(file);
            } catch (InstanceExistsException e) {
                OutputController.getLogger().log("Single instance application is already running.");
                return null;
            }

            if (JNLPRuntime.getForksAllowed() && file.needsNewVM()) {
                if (!JNLPRuntime.isHeadless()){
                    SplashScreen sp = SplashScreen.getSplashScreen();
                    if (sp!=null) {
                        sp.close();
                    }
                }
                List<String> netxArguments = new LinkedList<String>();
                netxArguments.add("-Xnofork");
                netxArguments.addAll(JNLPRuntime.getInitialArguments());
                launchExternal(file.getNewVMArgs(), netxArguments);
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
                File f = CacheUtil.getCacheFile(mainJarDesc.getLocation(), null);
                if (f != null) {
                    JarFile mainJar = new JarFile(f);
                    mainName = mainJar.getManifest().
                                getMainAttributes().getValue("Main-Class");
                }
            }

            if (mainName == null) {
                throw launchError(new LaunchException(file, null,
                        R("LSFatal"), R("LCClient"), R("LCantDetermineMainClass"),
                        R("LCantDetermineMainClassInfo")));
            }

            Class<?> mainClass = app.getClassLoader().loadClass(mainName);

            Method main = mainClass.getMethod("main", new Class<?>[] { String[].class });
            String args[] = file.getApplication().getArguments();

            SwingUtilities.invokeAndWait(new Runnable() {
                // dummy method to force Event Dispatch Thread creation
                @Override
                public void run() {
                }
            });

            setContextClassLoaderForAllThreads(app.getThreadGroup(), app.getClassLoader());

            handler.launchStarting(app);

            main.setAccessible(true);

            OutputController.getLogger().log("Invoking main() with args: " + Arrays.toString(args));
            main.invoke(null, new Object[] { args });

            return app;
        } catch (LaunchException lex) {
            throw launchError(lex);
        } catch (Exception ex) {
            throw launchError(new LaunchException(file, ex, R("LSFatal"), R("LCLaunching"), R("LCouldNotLaunch"), R("LCouldNotLaunchInfo")));
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
                OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "Setting " + classLoader + " as the classloader for thread " + thread.getName());
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
    protected ApplicationInstance launchApplet(JNLPFile file, boolean enableCodeBase, Container cont) throws LaunchException {
        if (!file.isApplet()) {
            throw launchError(new LaunchException(file, null, R("LSFatal"), R("LCClient"), R("LNotApplet"), R("LNotAppletInfo")));
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
            OutputController.getLogger().log("Single instance applet is already running.");
            throw launchError(new LaunchException(file, ieex, R("LSFatal"), R("LCLaunching"), R("LCouldNotLaunch"), R("LSingleInstanceExists")), applet);
        } catch (LaunchException lex) {
            throw launchError(lex, applet);
        } catch (Exception ex) {
            throw launchError(new LaunchException(file, ex, R("LSFatal"), R("LCLaunching"), R("LCouldNotLaunch"), R("LCouldNotLaunchInfo")), applet);
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
    protected ApplicationInstance getApplet(JNLPFile file, boolean enableCodeBase, Container cont) throws LaunchException {
        if (!file.isApplet()) {
            throw launchError(new LaunchException(file, null, R("LSFatal"), R("LCClient"), R("LNotApplet"), R("LNotAppletInfo")));
        }
        AppletInstance applet = null;
        try {
            ServiceUtil.checkExistingSingleInstance(file);
            applet = createApplet(file, enableCodeBase, cont);
            applet.initialize();
            return applet;

        } catch (InstanceExistsException ieex) {
            OutputController.getLogger().log("Single instance applet is already running.");
            throw launchError(new LaunchException(file, ieex, R("LSFatal"), R("LCLaunching"), R("LCouldNotLaunch"), R("LSingleInstanceExists")), applet);
        } catch (LaunchException lex) {
            throw launchError(lex, applet);
        } catch (Exception ex) {
            throw launchError(new LaunchException(file, ex, R("LSFatal"), R("LCLaunching"), R("LCouldNotLaunch"), R("LCouldNotLaunchInfo")), applet);
        }
    }

    /**
     * Launches a JNLP installer.  This method should be called from
     * a thread in the application's thread group.
     * @param file jnlp file to read installer from
     * @return  application
     * @throws net.sourceforge.jnlp.LaunchException if deploy unrecoverably die
     */
    protected ApplicationInstance launchInstaller(JNLPFile file) throws LaunchException {
        // TODO Check for an existing single instance once implemented.
        // ServiceUtil.checkExistingSingleInstance(file);
        throw launchError(new LaunchException(file, null, R("LSFatal"), R("LCNotSupported"), R("LNoInstallers"), R("LNoInstallersInfo")));
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
    //and then appelts creates in little bit strange manner. This issue is visible with
    //randomly showing/notshowing spalshscreens.
    //See also PluginAppletViewer.framePanel
    protected  AppletInstance createApplet(JNLPFile file, boolean enableCodeBase, Container cont) throws LaunchException {
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
            
            setContextClassLoaderForAllThreads(appletInstance.getThreadGroup(), appletInstance.getClassLoader());

            return appletInstance;
        } catch (Exception ex) {
            throw launchError(new LaunchException(file, ex, R("LSFatal"), R("LCInit"), R("LInitApplet"), R("LInitAppletInfo")), appletInstance);
        }
    }

    /**
     * Creates an Applet object from a JNLPFile. This is mainly to be used with
     * gcjwebplugin.
     * @param file the PluginBridge to be used.
     * @param enableCodeBase whether to add the code base URL to the classloader.
     * @param cont container where to put applet
     * @return applet
     * @throws net.sourceforge.jnlp.LaunchException if deploy unrecoverably dien
     */
    protected Applet createAppletObject(JNLPFile file, boolean enableCodeBase, Container cont) throws LaunchException {
        try {
            JNLPClassLoader loader = JNLPClassLoader.getInstance(file, updatePolicy, enableCodeBase);

            if (enableCodeBase) {
                loader.enableCodeBase();
            } else if (file.getResources().getJARs().length == 0) {
                throw new ClassNotFoundException("Can't do a codebase look up and there are no jars. Failing sooner rather than later");
            }

            String appletName = file.getApplet().getMainClass();
            Class<?> appletClass = loader.loadClass(appletName);
            Applet applet = (Applet) appletClass.newInstance();

            return applet;
        } catch (Exception ex) {
            throw launchError(new LaunchException(file, ex, R("LSFatal"), R("LCInit"), R("LInitApplet"), R("LInitAppletInfo")));
        }
    }

    /**
     * Creates an Application.
     * @param file the JNLP file
     * @return application
     * @throws net.sourceforge.jnlp.LaunchException if deploy unrecoverably die
     */
    protected ApplicationInstance createApplication(JNLPFile file) throws LaunchException {
        try {
            JNLPClassLoader loader = JNLPClassLoader.getInstance(file, updatePolicy, false);
            ThreadGroup group = Thread.currentThread().getThreadGroup();

            ApplicationInstance app = new ApplicationInstance(file, group, loader);
            loader.setApplication(app);

            return app;
        } catch (Exception ex) {
            throw new LaunchException(file, ex, R("LSFatal"), R("LCInit"), R("LInitApplication"), R("LInitApplicationInfo"));
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
    protected ThreadGroup createThreadGroup(JNLPFile file) {
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
         * This dtd is created by the ParserDelgate. However, the code in
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
        private JNLPFile file;
        private ApplicationInstance application;
        private LaunchException exception;
        private Container cont;
        private boolean isPlugin = false;

        TgThread(JNLPFile file) {
            this(file, null);
        }

        TgThread(JNLPFile file, Container cont) {
            super(createThreadGroup(file), file.getTitle());

            this.file = file;
            this.cont = cont;
        }

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
                                                R("LSFatal"), R("LCClient"), R("LNotLaunchable"),
                                                R("LNotLaunchableInfo")));
                    }
                }
            } catch (LaunchException ex) {
                OutputController.getLogger().log(OutputController.Level.ERROR_ALL, ex);
                exception = ex;
                // Exit if we can't launch the application.
                if (exitOnFailure) {
                    JNLPRuntime.exit(1);
                }
            }  catch (Throwable ex) {
                OutputController.getLogger().log(OutputController.Level.ERROR_ALL, ex);
                throw new RuntimeException(ex);
            }
        }

        public LaunchException getException() {
            return exception;
        }

        public ApplicationInstance getApplication() {
            return application;
        }

    };

}
