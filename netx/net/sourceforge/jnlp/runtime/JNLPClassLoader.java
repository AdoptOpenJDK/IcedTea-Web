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

import static net.sourceforge.jnlp.runtime.Translator.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.SocketPermission;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessControlContext;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.Manifest;

import net.sourceforge.jnlp.AppletDesc;
import net.sourceforge.jnlp.ApplicationDesc;
import net.sourceforge.jnlp.ExtensionDesc;
import net.sourceforge.jnlp.JARDesc;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.JNLPMatcher;
import net.sourceforge.jnlp.JNLPMatcherException;
import net.sourceforge.jnlp.LaunchDesc;
import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.NullJnlpFileException;
import net.sourceforge.jnlp.ParseException;
import net.sourceforge.jnlp.ParserSettings;
import net.sourceforge.jnlp.PluginBridge;
import net.sourceforge.jnlp.ResourcesDesc;
import net.sourceforge.jnlp.SecurityDesc;
import net.sourceforge.jnlp.Version;
import net.sourceforge.jnlp.cache.CacheUtil;
import net.sourceforge.jnlp.cache.IllegalResourceDescriptorException;
import net.sourceforge.jnlp.cache.NativeLibraryStorage;
import net.sourceforge.jnlp.cache.ResourceTracker;
import net.sourceforge.jnlp.cache.UpdatePolicy;
import net.sourceforge.jnlp.security.AppVerifier;
import net.sourceforge.jnlp.security.JNLPAppVerifier;
import net.sourceforge.jnlp.security.PluginAppVerifier;
import net.sourceforge.jnlp.security.appletextendedsecurity.UnsignedAppletTrustConfirmation;
import net.sourceforge.jnlp.tools.JarCertVerifier;
import net.sourceforge.jnlp.util.JarFile;
import net.sourceforge.jnlp.util.StreamUtils;
import net.sourceforge.jnlp.util.UrlUtils;
import net.sourceforge.jnlp.util.logging.OutputController;
import sun.misc.JarIndex;

/**
 * Classloader that takes it's resources from a JNLP file. If the
 * JNLP file defines extensions, separate classloaders for these
 * will be created automatically. Classes are loaded with the
 * security context when the classloader was created.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.20 $
 */
public class JNLPClassLoader extends URLClassLoader {

    // todo: initializePermissions should get the permissions from
    // extension classes too so that main file classes can load
    // resources in an extension.

    /** Signed JNLP File and Template */
    final public static String TEMPLATE = "JNLP-INF/APPLICATION_TEMPLATE.JNLP";
    final public static String APPLICATION = "JNLP-INF/APPLICATION.JNLP";

    /** Actions to specify how cache is to be managed **/
    public static enum DownloadAction {
        DOWNLOAD_TO_CACHE, REMOVE_FROM_CACHE, CHECK_CACHE
    }

    public static enum SigningState {
        FULL, PARTIAL, NONE
    }

    /** True if the application has a signed JNLP File */
    private boolean isSignedJNLP = false;
    
    /** map from JNLPFile unique key to shared classloader */
    private static Map<String, JNLPClassLoader> uniqueKeyToLoader = new ConcurrentHashMap<String, JNLPClassLoader>();

    /** map from JNLPFile unique key to lock, the lock is needed to enforce correct 
     * initialization of applets that share a unique key*/
    private static Map<String, ReentrantLock> uniqueKeyToLock = new HashMap<String, ReentrantLock>();

    /** Provides a search path & temporary storage for native code */
    private NativeLibraryStorage nativeLibraryStorage;

    /** security context */
    private AccessControlContext acc = AccessController.getContext();

    /** the permissions for the cached jar files */
    private List<Permission> resourcePermissions;

    /** the app */
    private ApplicationInstance app = null; // here for faster lookup in security manager

    /** list of this, local and global loaders this loader uses */
    private JNLPClassLoader loaders[] = null; // ..[0]==this

    /** whether to strictly adhere to the spec or not */
    private boolean strict = true;

    /** loads the resources */
    private ResourceTracker tracker = new ResourceTracker(true); // prefetch

    /** the update policy for resources */
    private UpdatePolicy updatePolicy;

    /** the JNLP file */
    private JNLPFile file;

    /** the resources section */
    private ResourcesDesc resources;

    /** the security section */
    private SecurityDesc security;

    /** Permissions granted by the user during runtime. */
    private ArrayList<Permission> runtimePermissions = new ArrayList<Permission>();

    /** all jars not yet part of classloader or active
     * Synchronized since this field may become shared data between multiple classloading threads.
     * See loadClass(String) and CodebaseClassLoader.findClassNonRecursive(String).
     */
    private List<JARDesc> available = Collections.synchronizedList(new ArrayList<JARDesc>());

    /** the jar cert verifier tool to verify our jars */
    private final JarCertVerifier jcv;

    private SigningState signing = SigningState.NONE;

    /** ArrayList containing jar indexes for various jars available to this classloader
     * Synchronized since this field may become shared data between multiple classloading threads/
     * See loadClass(String) and CodebaseClassLoader.findClassNonRecursive(String).
     */
    private List<JarIndex> jarIndexes = Collections.synchronizedList(new ArrayList<JarIndex>());

    /** Set of classpath strings declared in the manifest.mf files
     * Synchronized since this field may become shared data between multiple classloading threads.
     * See loadClass(String) and CodebaseClassLoader.findClassNonRecursive(String).
     */
    private Set<String> classpaths = Collections.synchronizedSet(new HashSet<String>());

    /** File entries in the jar files available to this classloader
     * Synchronized sinc this field may become shared data between multiple classloading threads.
     * See loadClass(String) and CodebaseClassLoader.findClassNonRecursive(String).
     */
    private Set<String> jarEntries = Collections.synchronizedSet(new TreeSet<String>());

    /** Map of specific original (remote) CodeSource Urls  to securitydesc
     *  Synchronized since this field may become shared data between multiple classloading threads.
     *  See loadClass(String) and CodebaseClassLoader.findClassNonRecursive(String).
     */
    private Map<URL, SecurityDesc> jarLocationSecurityMap =
            Collections.synchronizedMap(new HashMap<URL, SecurityDesc>());

    /*Set to prevent once tried-to-get resources to be tried again*/
    private Set<URL> alreadyTried = Collections.synchronizedSet(new HashSet<URL>());
    
    /** Loader for codebase (which is a path, rather than a file) */
    private CodeBaseClassLoader codeBaseLoader;
    
    /** True if the jar with the main class has been found
     * */
    private boolean foundMainJar= false;

    /** Name of the application's main class */
    private String mainClass = null;
    
    /**
     * Variable to track how many times this loader is in use
     */
    private int useCount = 0;

    private boolean enableCodeBase = false;

    private final SecurityDelegate securityDelegate;

    /**
     * Create a new JNLPClassLoader from the specified file.
     *
     * @param file the JNLP file
     */
    protected JNLPClassLoader(JNLPFile file, UpdatePolicy policy) throws LaunchException {
        this(file, policy, null, false);
    }

    /**
     * Create a new JNLPClassLoader from the specified file.
     *
     * @param file the JNLP file
     * @param policy the UpdatePolicy for this class loader
     * @param mainName name of the application's main class
     */
    protected JNLPClassLoader(JNLPFile file, UpdatePolicy policy, String mainName, boolean enableCodeBase) throws LaunchException {
        super(new URL[0], JNLPClassLoader.class.getClassLoader());

        OutputController.getLogger().log("New classloader: " + file.getFileLocation());

        this.file = file;
        this.updatePolicy = policy;
        this.resources = file.getResources();

        this.nativeLibraryStorage = new NativeLibraryStorage(tracker);

        this.mainClass = mainName;

        this.enableCodeBase = enableCodeBase;

        
        AppVerifier verifier;

        if (file instanceof PluginBridge && !((PluginBridge)file).useJNLPHref()) {
            verifier = new PluginAppVerifier();
        } else {
            verifier = new JNLPAppVerifier();
        }

        jcv = new JarCertVerifier(verifier);

        if (this.enableCodeBase) {
            addToCodeBaseLoader(this.file.getCodeBase());
        }

        this.securityDelegate = new SecurityDelegateImpl(this);

        // initialize extensions
        initializeExtensions();

        initializeResources();
        
        //loading mainfests before resources are initialised may cause waiting for resources
        file.getManifestsAttributes().setLoader(this);

        // initialize permissions
        initializePermissions();

        setSecurity();

        ManifestAttributesChecker mac = new ManifestAttributesChecker(security, file, signing, securityDelegate);
        mac.checkAll();
        
        installShutdownHooks();
        

    }

    /**
     * Install JVM shutdown hooks to clean up resources allocated by this
     * ClassLoader.
     */
    private void installShutdownHooks() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                /*
                 * Delete only the native dir created by this classloader (if
                 * there is one). Other classloaders (parent, peers) will all
                 * cleanup things they created
                 */
                nativeLibraryStorage.cleanupTemporaryFolder();
            }
        });
    }

    private void setSecurity() throws LaunchException {
        URL codebase = UrlUtils.guessCodeBase(file);
        this.security = securityDelegate.getClassLoaderSecurity(codebase.getHost());
    }

    /**
     * Gets the lock for a given unique key, creating one if it does not yet exist.
     * This operation is atomic & thread-safe.
     * 
     * @param uniqueKey the file whose unique key should be used
     * @return the lock
     */
    private static ReentrantLock getUniqueKeyLock(String uniqueKey) {
        synchronized (uniqueKeyToLock) {
            ReentrantLock storedLock = uniqueKeyToLock.get(uniqueKey);

            if (storedLock == null) {
                storedLock = new ReentrantLock();
                uniqueKeyToLock.put(uniqueKey, storedLock);
            }

            return storedLock;
        }
    }

    /**
     * Creates a fully initialized JNLP classloader for the specified JNLPFile, 
     * to be used as an applet/application's classloader.
     * In contrast, JNLP classloaders can also be constructed simply to merge 
     * its resources into another classloader.
     *
     * @param file the file to load classes for
     * @param policy the update policy to use when downloading resources
     * @param mainName Overrides the main class name of the application
     */
    private static JNLPClassLoader createInstance(JNLPFile file, UpdatePolicy policy, String mainName, boolean enableCodeBase) throws LaunchException {
        String uniqueKey = file.getUniqueKey();
        JNLPClassLoader baseLoader = uniqueKeyToLoader.get(uniqueKey);
        JNLPClassLoader loader = new JNLPClassLoader(file, policy, mainName, enableCodeBase);

        // If security level is 'high' or greater, we must check if the user allows unsigned applets 
        // when the JNLPClassLoader is created. We do so here, because doing so in the constructor 
        // causes unwanted side-effects for some applets. However, if the loader has been tagged
        // with "runInSandbox", then we do not show this dialog - since this tag indicates that
        // the user was already shown a CertWarning dialog and has chosen to run the applet sandboxed.
        // This means they've already agreed to running the applet and have specified with which
        // permission level to do it!
        if (loader.getSigningState() == SigningState.PARTIAL) {
            loader.securityDelegate.promptUserOnPartialSigning();
        } else if (!loader.getSigning() && !loader.securityDelegate.userPromptedForSandbox() && file instanceof PluginBridge) {
            UnsignedAppletTrustConfirmation.checkUnsignedWithUserIfRequired((PluginBridge)file);
        }

        // New loader init may have caused extentions to create a
        // loader for this unique key. Check.
        JNLPClassLoader extLoader = uniqueKeyToLoader.get(uniqueKey);

        if (extLoader != null && extLoader != loader) {
            if (loader.getSigning() != extLoader.getSigning()) {
                loader.securityDelegate.promptUserOnPartialSigning();
            }
            loader.merge(extLoader);
            extLoader.decrementLoaderUseCount(); // loader urls have been merged, ext loader is no longer used
        }

        // loader is now current + ext. But we also need to think of
        // the baseLoader
        if (baseLoader != null && baseLoader != loader) {
           loader.merge(baseLoader);
        }

        return loader;
    }

    /**
     * Returns a JNLP classloader for the specified JNLP file.
     * 
     * @param file the file to load classes for
     * @param policy the update policy to use when downloading resources
     */
    public static JNLPClassLoader getInstance(JNLPFile file, UpdatePolicy policy, boolean enableCodeBase) throws LaunchException {
        return getInstance(file, policy, null, enableCodeBase);
    }

    /**
     * Returns a JNLP classloader for the specified JNLP file.
     *
     * @param file the file to load classes for
     * @param policy the update policy to use when downloading resources
     * @param mainName Overrides the main class name of the application
     */
    public static JNLPClassLoader getInstance(JNLPFile file, UpdatePolicy policy, String mainName, boolean enableCodeBase) throws LaunchException {
        JNLPClassLoader baseLoader = null;
        JNLPClassLoader loader = null;
        String uniqueKey = file.getUniqueKey();

        synchronized ( getUniqueKeyLock(uniqueKey) ) {
            baseLoader = uniqueKeyToLoader.get(uniqueKey);

            // A null baseloader implies that no loader has been created 
            // for this codebase/jnlp yet. Create one.
            if (baseLoader == null ||
                    (file.isApplication() && 
                     !baseLoader.getJNLPFile().getFileLocation().equals(file.getFileLocation()))) {

                loader = createInstance(file, policy, mainName, enableCodeBase);
            } else {
                // if key is same and locations match, this is the loader we want
                if (!file.isApplication()) {
                    // If this is an applet, we do need to consider its loader
                    loader = new JNLPClassLoader(file, policy, mainName, enableCodeBase);

                    if (baseLoader != null)
                        baseLoader.merge(loader);
                }
                loader = baseLoader;
            }

            // loaders are mapped to a unique key. Only extensions and parent
            // share a key, so it is safe to always share based on it

            loader.incrementLoaderUseCount();

            uniqueKeyToLoader.put(uniqueKey, loader);
        }

        return loader;
    }

    /**
     * Returns a JNLP classloader for the JNLP file at the specified
     * location.
     *
     * @param location the file's location
     * @param version the file's version
     * @param policy the update policy to use when downloading resources
     * @param mainName Overrides the main class name of the application
     */
    public static JNLPClassLoader getInstance(URL location, String uniqueKey, Version version, ParserSettings settings, UpdatePolicy policy, String mainName, boolean enableCodeBase)
            throws IOException, ParseException, LaunchException {

        JNLPClassLoader loader;

        synchronized ( getUniqueKeyLock(uniqueKey) ) {
            loader = uniqueKeyToLoader.get(uniqueKey);

            if (loader == null || !location.equals(loader.getJNLPFile().getFileLocation())) {
                JNLPFile jnlpFile = new JNLPFile(location, uniqueKey, version, settings, policy);

                loader = getInstance(jnlpFile, policy, mainName, enableCodeBase);
            }
        }

        return loader;
    }

    /**
     * Load the extensions specified in the JNLP file.
     */
    void initializeExtensions() {
        ExtensionDesc[] extDescs = resources.getExtensions();

        List<JNLPClassLoader> loaderList = new ArrayList<JNLPClassLoader>();

        loaderList.add(this);

        if (mainClass == null) {
            Object obj = file.getLaunchInfo();

            if (obj instanceof ApplicationDesc) {
                ApplicationDesc ad = (ApplicationDesc) file.getLaunchInfo();
                mainClass = ad.getMainClass();
            } else if (obj instanceof AppletDesc) {
                AppletDesc ad = (AppletDesc) file.getLaunchInfo();
                mainClass = ad.getMainClass();
            }
        }

        //if (ext != null) {
        for (ExtensionDesc ext : extDescs) {
            try {
                String uniqueKey = this.getJNLPFile().getUniqueKey();
                JNLPClassLoader loader = getInstance(ext.getLocation(), uniqueKey, ext.getVersion(), file.getParserSettings(), updatePolicy, mainClass, this.enableCodeBase);
                loaderList.add(loader);
            } catch (Exception ex) {
                OutputController.getLogger().log(OutputController.Level.ERROR_ALL, ex);
            }
        }
        //}

        loaders = loaderList.toArray(new JNLPClassLoader[loaderList.size()]);
    }

    /**
     * Make permission objects for the classpath.
     */
    void initializePermissions() {
        resourcePermissions = new ArrayList<Permission>();

        JARDesc jars[] = resources.getJARs();
        for (JARDesc jar : jars) {
            Permission p = CacheUtil.getReadPermission(jar.getLocation(), jar.getVersion());

            if (p == null) {
                OutputController.getLogger().log("Unable to add permission for " + jar.getLocation());
            } else {
                OutputController.getLogger().log("Permission added: " + p.toString());
            }
            if (p != null)
                resourcePermissions.add(p);
        }
    }

    /**
     * Check if a described jar file is invalid
     * @param jar the jar to check
     * @return true if file exists AND is an invalid jar, false otherwise
     */
    boolean isInvalidJar(JARDesc jar){
        File cacheFile = tracker.getCacheFile(jar.getLocation());
        if (cacheFile == null)
            return false;//File cannot be retrieved, do not claim it is an invalid jar
        boolean isInvalid = false;
        try {
            JarFile jarFile = new JarFile(cacheFile.getAbsolutePath());
            jarFile.close();
        } catch (IOException ioe){
            //Catch a ZipException or any other read failure
            isInvalid = true;
        }
        return isInvalid;
    }

    /**
     * Determine how invalid jars should be handled
     * @return whether to filter invalid jars, or error later on
     */
    private boolean shouldFilterInvalidJars(){
        if (file instanceof PluginBridge){
            PluginBridge pluginBridge = (PluginBridge)file;
            /*Ignore on applet, ie !useJNLPHref*/
            return !pluginBridge.useJNLPHref();
        }
        return false;//Error is default behaviour
    }

    /**
     * Load all of the JARs used in this JNLP file into the
     * ResourceTracker for downloading.
     */
    void initializeResources() throws LaunchException {
        if (file instanceof PluginBridge){
            PluginBridge bridge = (PluginBridge)file;

            for (String codeBaseFolder : bridge.getCodeBaseFolders()){
                try {
                    addToCodeBaseLoader(new URL(file.getCodeBase(), codeBaseFolder));
                } catch (MalformedURLException mfe) {
                    OutputController.getLogger().log(OutputController.Level.WARNING_ALL, "Problem trying to add folder to code base:");
                    OutputController.getLogger().log(OutputController.Level.ERROR_ALL, mfe);
                }
            }
        }

        JARDesc jars[] = resources.getJARs();

        if (jars.length == 0) {

            boolean allSigned = (loaders.length > 1) /* has extensions */;
            for (int i = 1; i < loaders.length; i++) {
                if (!loaders[i].getSigning()) {
                    allSigned = false;
                    break;
                }
            }

            if (allSigned)
                signing = SigningState.FULL;

            //Check if main jar is found within extensions
            foundMainJar = foundMainJar || hasMainInExtensions();

            return;
        }
        /*
        if (jars == null || jars.length == 0) {
                throw new LaunchException(null, null, R("LSFatal"),
                                    R("LCInit"), R("LFatalVerification"), "No jars!");
        }
        */
        List<JARDesc> initialJars = new ArrayList<JARDesc>();

        for (JARDesc jar : jars) {

            available.add(jar);

            if (jar.isEager())
                initialJars.add(jar); // regardless of part

            tracker.addResource(jar.getLocation(),
                    jar.getVersion(), file.getDownloadOptions(),
                    jar.isCacheable() ? JNLPRuntime.getDefaultUpdatePolicy() : UpdatePolicy.FORCE);
        }

        //If there are no eager jars, initialize the first jar
        if(initialJars.size() == 0)
            initialJars.add(jars[0]);

        if (strict)
            fillInPartJars(initialJars); // add in each initial part's lazy jars

        waitForJars(initialJars); //download the jars first.

        //A ZipException will propagate later on if the jar is invalid and not checked here
        if (shouldFilterInvalidJars()){
            //We filter any invalid jars
            Iterator<JARDesc> iterator = initialJars.iterator();
            while (iterator.hasNext()){
                JARDesc jar = iterator.next();
                if (isInvalidJar(jar)) {
                    //Remove this jar as an available jar
                    iterator.remove();
                    tracker.removeResource(jar.getLocation());
                    available.remove(jar);
                }
            }
        }

        if (JNLPRuntime.isVerifying()) {

            try {
                jcv.add(initialJars, tracker);
            } catch (Exception e) {
                //we caught an Exception from the JarCertVerifier class.
                //Note: one of these exceptions could be from not being able
                //to read the cacerts or trusted.certs files.
                OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
                throw new LaunchException(null, null, R("LSFatal"),
                                        R("LCInit"), R("LFatalVerification"), R("LFatalVerificationInfo") + ": " +e.getMessage());
            }

            //Case when at least one jar has some signing
            if (jcv.isFullySigned()) {
                signing = SigningState.FULL;

                // Check for main class in the downloaded jars, and check/verify signed JNLP fill
                checkForMain(initialJars);

                // If jar with main class was not found, check available resources
                while (!foundMainJar && available != null && available.size() != 0)
                    addNextResource();

                // If the jar with main class was not found, check extension
                // jnlp's resources
                foundMainJar = foundMainJar || hasMainInExtensions();

                boolean externalAppletMainClass = (file.getLaunchInfo() != null && !foundMainJar
                        && (available == null || available.size() == 0));

                // We do this check here simply to ensure that if there are no JARs at all,
                // and also no main-class in the codebase (ie the applet doesn't really exist), we
                // fail ASAP rather than continuing (and showing the NotAllSigned dialog for no applet)
                if (externalAppletMainClass) {
                    if (codeBaseLoader != null) {
                        try {
                            codeBaseLoader.findClass(mainClass);
                        } catch (ClassNotFoundException extCnfe) {
                            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, extCnfe);
                            throw new LaunchException(file, extCnfe, R("LSFatal"), R("LCInit"), R("LCantDetermineMainClass"), R("LCantDetermineMainClassInfo"));
                        }
                    } else {
                        throw new LaunchException(file, null, R("LSFatal"), R("LCInit"), R("LCantDetermineMainClass"), R("LCantDetermineMainClassInfo"));
                    }
                }

                // If externalAppletMainClass is true and a LaunchException was not thrown above,
                // then the main-class can be loaded from the applet codebase, but is obviously not signed
                if (!jcv.allJarsSigned()) {
                    checkPartialSigningWithUser();
                }

                // If main jar was found, but a signed JNLP file was not located
                if (!isSignedJNLP && foundMainJar)
                    file.setSignedJNLPAsMissing();

                //user does not trust this publisher
                if (!jcv.isTriviallySigned()) {
                    checkTrustWithUser();
                } else {
                    /**
                     * If the user trusts this publisher (i.e. the publisher's certificate
                     * is in the user's trusted.certs file), we do not show any dialogs.
                     */
                }
            } else {

                // Otherwise this jar is simply unsigned -- make sure to ask
                // for permission on certain actions
                signing = SigningState.NONE;
            }
        }

        boolean containsSignedJar = false, containsUnsignedJar = false;
        for (JARDesc jarDesc : file.getResources().getJARs()) {
            File cachedFile;

            try {
                cachedFile = tracker.getCacheFile(jarDesc.getLocation());
            } catch (IllegalResourceDescriptorException irde) {
                //Caused by ignored resource being removed due to not being valid
                OutputController.getLogger().log(OutputController.Level.ERROR_ALL, "JAR " + jarDesc.getLocation() + " is not a valid jar file. Continuing.");
                continue;
            }

            if (cachedFile == null) {
                OutputController.getLogger().log(OutputController.Level.ERROR_ALL, "JAR " + jarDesc.getLocation() + " not found. Continuing.");
                continue; // JAR not found. Keep going.
            }

            final URL codebase;
            if (file.getCodeBase() != null) {
                codebase = file.getCodeBase();
            } else {
                // FIXME: codebase should be the codebase of the Main Jar not
                // the location. Although, it still works in the current state.
                codebase = file.getResources().getMainJAR().getLocation();
            }

            final SecurityDesc jarSecurity = securityDelegate.getCodebaseSecurityDesc(jarDesc, codebase.getHost());
            if (jarSecurity.getSecurityType().equals(SecurityDesc.SANDBOX_PERMISSIONS)) {
                containsUnsignedJar = true;
            } else {
                containsSignedJar = true;
            }

            jarLocationSecurityMap.put(jarDesc.getLocation(), jarSecurity);
        }

        if (containsSignedJar && containsUnsignedJar) {
            checkPartialSigningWithUser();
        }

        activateJars(initialJars);
    }
    
     /***
     * Checks for the jar that contains the attribute. 
     * 
     * @param jars Jars that are checked to see if they contain the main class
     * @param  name attribute to be found
     */
    public String checkForAttributeInJars(List<JARDesc> jars, Attributes.Name name) {
       
        if (jars.isEmpty()) {
            return null;
        }

        String result = null;
        
        // Check main jar
        JARDesc mainJarDesc = ResourcesDesc.getMainJAR(jars);
        result = getManifestAttribute(mainJarDesc.getLocation(), name);

        if (result != null) {
            return result;
        }
        
        // Check first jar
        JARDesc firstJarDesc = jars.get(0);
        result = getManifestAttribute(firstJarDesc.getLocation(),name);
        
        if (result != null) {
            return result;
        }

        // Still not found? Iterate and set if only 1 was found
        for (JARDesc jarDesc: jars) {
            String attributeInThisJar = getManifestAttribute(jarDesc.getLocation(), name);
                if (attributeInThisJar != null) {
                    if (result == null) { // first main class
                        result = attributeInThisJar;
                    } else { // There is more than one main class. Set to null and break.
                        result = null;
                        break;
                }
            }
        }
        return result;
    }
    /***
     * Checks for the jar that contains the main class. If the main class was
     * found, it checks to see if the jar is signed and whether it contains a
     * signed JNLP file
     * 
     * @param jars Jars that are checked to see if they contain the main class
     * @throws LaunchException Thrown if the signed JNLP file, within the main jar, fails to be verified or does not match
     */
    void checkForMain(List<JARDesc> jars) throws LaunchException {

        // Check launch info
        if (mainClass == null) {
            LaunchDesc launchDesc = file.getLaunchInfo();
            if (launchDesc != null) {
                mainClass = launchDesc.getMainClass();
            }
        }

        // The main class may be specified in the manifest

        if (mainClass == null) {
            mainClass = checkForAttributeInJars(jars, Attributes.Name.MAIN_CLASS);
        }

        String desiredJarEntryName = mainClass + ".class";

        for (JARDesc jar : jars) {

            try {
                File localFile = tracker
                        .getCacheFile(jar.getLocation());

                if (localFile == null) {
                    OutputController.getLogger().log(OutputController.Level.ERROR_ALL, "JAR " + jar.getLocation() + " not found. Continuing.");
                    continue; // JAR not found. Keep going.
                }

                JarFile jarFile = new JarFile(localFile);

                for (JarEntry entry : Collections.list(jarFile.entries())) {
                    String jeName = entry.getName().replaceAll("/", ".");
                    if (jeName.equals(desiredJarEntryName)) {
                        foundMainJar = true;
                        verifySignedJNLP(jar, jarFile);
                        break;
                    }
                }

                jarFile.close();
            } catch (IOException e) {
                /*
                 * After this exception is caught, it is escaped. This will skip
                 * the jarFile that may have thrown this exception and move on
                 * to the next jarFile (if there are any)
                 */
            }
        }
    }

    /**
     * Gets the name of the main method if specified in the manifest
     *
     * @param location The JAR location
     * @return the main class name, null if there isn't one of if there was an error
     */
    String getMainClassName(URL location) {
        return getManifestAttribute(location, Attributes.Name.MAIN_CLASS);
    }
    
    
    /**
     * Gets the name of the main method if specified in the manifest
     *
     * @param location The JAR location
     * @return the attribute value, null if there isn't one of if there was an error
     */
    public String getManifestAttribute(URL location, Attributes.Name  attribute) {

        String attributeValue = null;
        File f = tracker.getCacheFile(location);

        if( f != null) {
            JarFile mainJar = null;
            try {
                mainJar = new JarFile(f);
                Manifest manifest = mainJar.getManifest();
                if (manifest == null || manifest.getMainAttributes() == null){
                    //yes, jars without manifest exists
                    return null;
                }
                attributeValue = manifest.getMainAttributes().getValue(attribute);
            } catch (IOException ioe) {
                attributeValue = null;
            } finally {
                StreamUtils.closeSilently(mainJar);
            }
        }

        return attributeValue;
    }

    /**
     * Returns true if this loader has the main jar
     */
    public boolean hasMainJar() {
        return this.foundMainJar;
    }

    /**
     * Returns true if extension loaders have the main jar
     */
    private boolean hasMainInExtensions() {
        boolean foundMain = false;

        for (int i = 1; i < loaders.length && !foundMain; i++) {
            foundMain = loaders[i].hasMainJar();
        }

        return foundMain;
    }

    /**
     * Is called by checkForMain() to check if the jar file is signed and if it
     * contains a signed JNLP file.
     * 
     * @param jarDesc JARDesc of jar
     * @param jarFile the jar file
     * @throws LaunchException thrown if the signed JNLP file, within the main jar, fails to be verified or does not match
     */
    private void verifySignedJNLP(JARDesc jarDesc, JarFile jarFile)
            throws LaunchException {

        List<JARDesc> desc = new ArrayList<JARDesc>();
        desc.add(jarDesc);

        // Initialize streams
        InputStream inStream = null;
        InputStreamReader inputReader = null;
        FileReader fr = null;
        InputStreamReader jnlpReader = null;

        try {
            // NOTE: verification should have happened by now. In other words,
            // calling jcv.verifyJars(desc, tracker) here should have no affect.
            if (jcv.isFullySigned()) {

                for (JarEntry je : Collections.list(jarFile.entries())) {
                    String jeName = je.getName().toUpperCase();

                    if (jeName.equals(TEMPLATE) || jeName.equals(APPLICATION)) {

                        OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "Creating Jar InputStream from JarEntry");

                        inStream = jarFile.getInputStream(je);
                        inputReader = new InputStreamReader(inStream);

                        OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "Creating File InputStream from lauching JNLP file");

                        JNLPFile jnlp = this.getJNLPFile();
                        URL url = jnlp.getFileLocation();
                        File jn = null;

                        // If the file is on the local file system, use original path, otherwise find cached file
                        if (url.getProtocol().toLowerCase().equals("file"))
                            jn = new File(url.getPath());
                        else
                            jn = CacheUtil.getCacheFile(url, null);

                        fr = new FileReader(jn);
                        jnlpReader = fr;

                        // Initialize JNLPMatcher class
                        JNLPMatcher matcher;

                        if (jeName.equals(APPLICATION)) { // If signed application was found
                            OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "APPLICATION.JNLP has been located within signed JAR. Starting verfication...");
                           
                            matcher = new JNLPMatcher(inputReader, jnlpReader, false);
                        } else { // Otherwise template was found
                            OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "APPLICATION_TEMPLATE.JNLP has been located within signed JAR. Starting verfication...");
                            
                            matcher = new JNLPMatcher(inputReader, jnlpReader,
                                    true);
                        }

                        // If signed JNLP file does not matches launching JNLP file, throw JNLPMatcherException
                        if (!matcher.isMatch())
                            throw new JNLPMatcherException("Signed Application did not match launching JNLP File");

                        this.isSignedJNLP = true;
                        OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "Signed Application Verification Successful");

                        break; 
                    }
                }
            }
        } catch (JNLPMatcherException e) {

            /*
             * Throws LaunchException if signed JNLP file fails to be verified
             * or fails to match the launching JNLP file
             */

            throw new LaunchException(file, null, R("LSFatal"), R("LCClient"),
                    R("LSignedJNLPFileDidNotMatch"), R(e.getMessage()));

            /*
             * Throwing this exception will fail to initialize the application
             * resulting in the termination of the application
             */

        } catch (Exception e) {
            
            OutputController.getLogger().log(e);

            /*
             * After this exception is caught, it is escaped. If an exception is
             * thrown while handling the jar file, (mainly for
             * JarCertVerifier.add) it assumes the jar file is unsigned and
             * skip the check for a signed JNLP file
             */
            
        } finally {

            //Close all streams
            StreamUtils.closeSilently(inStream);
            StreamUtils.closeSilently(inputReader);
            StreamUtils.closeSilently(fr);
            StreamUtils.closeSilently(jnlpReader);
        }

        OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "Ending check for signed JNLP file...");
    }

    /**
     * Prompt the user for trust on all the signers that require approval.
     * @throws LaunchException if the user does not approve every dialog prompt.
     */
    private void checkTrustWithUser() throws LaunchException {
        if (JNLPRuntime.isTrustNone()) {
            if (!securityDelegate.getRunInSandbox()) {
                setRunInSandbox();
            }
            return;
        }
        if (JNLPRuntime.isTrustAll() || securityDelegate.getRunInSandbox()) {
            return;
        }

        if (getSigningState() == SigningState.FULL && jcv.isFullySigned() && !jcv.getAlreadyTrustPublisher()) {
            jcv.checkTrustWithUser(securityDelegate, file);
        }
    }

    /*
     * Sets whether applets are to be run sandboxed, regardless of JAR
     * signing. This MUST be called before any call to initializeResources,
     * setSecurity, activateJars, or any other method that sets the value
     * of this.security or adds entries into this.jarLocationSecurityMap.
     * @throws LaunchException if security settings have been initialized before
     * this method is called
     */
    public void setRunInSandbox() throws LaunchException {
        securityDelegate.setRunInSandbox();
    }

    public boolean userPromptedForSandbox() {
        return securityDelegate.getRunInSandbox();
    }

    /**
     * Add applet's codebase URL.  This allows compatibility with
     * applets that load resources from their codebase instead of
     * through JARs, but can slow down resource loading.  Resources
     * loaded from the codebase are not cached.
     */
    public void enableCodeBase() {
        addToCodeBaseLoader(file.getCodeBase());
    }

    /**
     * Sets the JNLP app this group is for; can only be called once.
     */
    public void setApplication(ApplicationInstance app) {
        if (this.app != null) {
                OutputController.getLogger().log(new IllegalStateException("Application can only be set once"));
            return;
        }

        this.app = app;
    }

    /**
     * Returns the JNLP app for this classloader
     */
    public ApplicationInstance getApplication() {
        return app;
    }

    /**
     * Returns the JNLP file the classloader was created from.
     */
    public JNLPFile getJNLPFile() {
        return file;
    }

    /**
     * Returns the permissions for the CodeSource.
     */
    protected PermissionCollection getPermissions(CodeSource cs) {
        try {
            Permissions result = new Permissions();

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
                if (cs == null) {
                    throw new NullPointerException("Code source was null");
                }
                if (cs.getCodeSigners() != null) {
                    if (cs.getLocation() == null) {
                        throw new NullPointerException("Code source location was null");
                    }
                    if (getCodeSourceSecurity(cs.getLocation()) == null) {
                        throw new NullPointerException("Code source security was null");
                    }
                    if (getCodeSourceSecurity(cs.getLocation()).getSecurityType() == null) {
                        OutputController.getLogger().log(new NullPointerException("Warning! Code source security type was null"));
                    }
                    Object securityType = getCodeSourceSecurity(cs.getLocation()).getSecurityType();
                    if (SecurityDesc.ALL_PERMISSIONS.equals(securityType)
                            || SecurityDesc.J2EE_PERMISSIONS.equals(securityType)) {

                        permissions = getCodeSourceSecurity(cs.getLocation()).getPermissions(cs);
                    }
                }

                for (Permission perm : Collections.list(permissions.elements())) {
                    result.add(perm);
                }
            }

            // add in permission to read the cached JAR files
            for (Permission perm : resourcePermissions) {
                result.add(perm);
            }

            // add in the permissions that the user granted.
            for (Permission perm : runtimePermissions) {
                result.add(perm);
            }

            // Class from host X should be allowed to connect to host X
            if (cs.getLocation() != null && cs.getLocation().getHost().length() > 0)
                result.add(new SocketPermission(cs.getLocation().getHost(),
                        "connect, accept"));

            return result;
        } catch (RuntimeException ex) {
            OutputController.getLogger().log(ex);
            throw ex;
        }
    }

    protected void addPermission(Permission p) {
        runtimePermissions.add(p);
    }

    /**
     * Adds to the specified list of JARS any other JARs that need
     * to be loaded at the same time as the JARs specified (ie, are
     * in the same part).
     */
    protected void fillInPartJars(List<JARDesc> jars) {
        for (JARDesc desc : jars) {
            String part = desc.getPart();

            // "available" field can be affected by two different threads
            // working in loadClass(String)
            synchronized (available) {
                for (JARDesc jar : available) {
                    if (part != null && part.equals(jar.getPart()))
                        if (!jars.contains(jar))
                            jars.add(jar);
                }
            }
        }
    }

    /**
     * Ensures that the list of jars have all been transferred, and
     * makes them available to the classloader.  If a jar contains
     * native code, the libraries will be extracted and placed in
     * the path.
     *
     * @param jars the list of jars to load
     */
    protected void activateJars(final List<JARDesc> jars) {
        PrivilegedAction<Void> activate = new PrivilegedAction<Void>() {

            @SuppressWarnings("deprecation")
            public Void run() {
                // transfer the Jars
                waitForJars(jars);

                for (JARDesc jar : jars) {
                    available.remove(jar);

                    // add jar
                    File localFile = tracker.getCacheFile(jar.getLocation());
                    try {
                        URL location = jar.getLocation(); // non-cacheable, use source location
                        if (localFile != null) {
                            // TODO: Should be toURI().toURL()
                            location = localFile.toURL(); // cached file

                            // This is really not the best way.. but we need some way for
                            // PluginAppletViewer::getCachedImageRef() to check if the image
                            // is available locally, and it cannot use getResources() because
                            // that prefetches the resource, which confuses MediaTracker.waitForAll()
                            // which does a wait(), waiting for notification (presumably
                            // thrown after a resource is fetched). This bug manifests itself
                            // particularly when using The FileManager applet from Webmin.

                            JarFile jarFile = new JarFile(localFile);
                            for (JarEntry je : Collections.list(jarFile.entries())) {

                                // another jar in my jar? it is more likely than you think
                                if (je.getName().endsWith(".jar")) {
                                    // We need to extract that jar so that it can be loaded
                                    // (inline loading with "jar:..!/..." path will not work
                                    // with standard classloader methods)

                                    String extractedJarLocation = localFile.getParent() + "/" + je.getName();
                                    File parentDir = new File(extractedJarLocation).getParentFile();
                                    if (!parentDir.isDirectory() && !parentDir.mkdirs()) {
                                        throw new RuntimeException(R("RNestedJarExtration"));
                                    }
                                    FileOutputStream extractedJar = new FileOutputStream(extractedJarLocation);
                                    InputStream is = jarFile.getInputStream(je);

                                    byte[] bytes = new byte[1024];
                                    int read = is.read(bytes);
                                    int fileSize = read;
                                    while (read > 0) {
                                        extractedJar.write(bytes, 0, read);
                                        read = is.read(bytes);
                                        fileSize += read;
                                    }

                                    is.close();
                                    extractedJar.close();

                                    // 0 byte file? skip
                                    if (fileSize <= 0) {
                                        continue;
                                    }

                                    tracker.addResource(new File(extractedJarLocation).toURL(), null, null, null);

                                    URL codebase = file.getCodeBase();
                                    if (codebase == null) {
                                        //FIXME: codebase should be the codebase of the Main Jar not
                                        //the location. Although, it still works in the current state.
                                        codebase = file.getResources().getMainJAR().getLocation();
                                    }

                                    final SecurityDesc jarSecurity = securityDelegate.getJarPermissions(codebase.getHost());

                                    try {
                                        URL fileURL = new URL("file://" + extractedJarLocation);
                                        // there is no remote URL for this, so lets fake one
                                        URL fakeRemote = new URL(jar.getLocation().toString() + "!" + je.getName());
                                        CachedJarFileCallback.getInstance().addMapping(fakeRemote, fileURL);
                                        addURL(fakeRemote);

                                        jarLocationSecurityMap.put(fakeRemote, jarSecurity);

                                    } catch (MalformedURLException mfue) {
                                        OutputController.getLogger().log(OutputController.Level.WARNING_DEBUG, "Unable to add extracted nested jar to classpath");
                                        OutputController.getLogger().log(OutputController.Level.ERROR_ALL, mfue);
                                    }
                                }

                                jarEntries.add(je.getName());
                            }

                            jarFile.close();
                        }

                        addURL(jar.getLocation());

                        // there is currently no mechanism to cache files per
                        // instance.. so only index cached files
                        if (localFile != null) {
                            CachedJarFileCallback.getInstance().addMapping(jar.getLocation(), localFile.toURL());

                            JarFile jarFile = new JarFile(localFile.getAbsolutePath());
                            Manifest mf = jarFile.getManifest();

                            // Only check classpath if this is the plugin and there is no jnlp_href usage.
                            // Note that this is different from proprietary plugin behaviour.
                            // If jnlp_href is used, the app should be treated similarly to when
                            // it is run from javaws as a webstart.
                            if (file instanceof PluginBridge && !((PluginBridge) file).useJNLPHref()) {
                                classpaths.addAll(getClassPathsFromManifest(mf, jar.getLocation().getPath()));
                            }

                            JarIndex index = JarIndex.getJarIndex(jarFile, null);
                            if (index != null)
                                jarIndexes.add(index);

                            jarFile.close();
                        } else {
                            CachedJarFileCallback.getInstance().addMapping(jar.getLocation(), jar.getLocation());
                        }

                        OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "Activate jar: " + location);
                    }
                    catch (Exception ex) {
                       OutputController.getLogger().log(ex);
                    }

                    // some programs place a native library in any jar
                    nativeLibraryStorage.addSearchJar(jar.getLocation());
                }

                return null;
            }
        };

        AccessController.doPrivileged(activate, acc);
    }

    /**
     * Return the absolute path to the native library.
     */
    protected String findLibrary(String lib) {
        String syslib = System.mapLibraryName(lib);
        File libFile = nativeLibraryStorage.findLibrary(syslib);

        if (libFile != null) {
            return libFile.toString();
        }

        String result = super.findLibrary(lib);
        if (result != null)
            return result;

        return findLibraryExt(lib);
    }

    /**
     * Try to find the library path from another peer classloader.
     */
    protected String findLibraryExt(String lib) {
        for (JNLPClassLoader loader : loaders) {
            String result = null;

            if (loader != this)
                result = loader.findLibrary(lib);

            if (result != null)
                return result;
        }

        return null;
    }

    /**
     * Wait for a group of JARs, and send download events if there
     * is a download listener or display a progress window otherwise.
     *
     * @param jars the jars
     */
    private void waitForJars(List<JARDesc> jars) {
        URL urls[] = new URL[jars.size()];

        for (int i = 0; i < jars.size(); i++) {
            JARDesc jar = jars.get(i);

            urls[i] = jar.getLocation();
        }

        CacheUtil.waitForResources(app, tracker, urls, file.getTitle());
    }

    /**
     * Find the loaded class in this loader or any of its extension loaders.
     */
    protected Class<?> findLoadedClassAll(String name) {
        for (JNLPClassLoader loader : loaders) {
            Class<?> result = null;

            if (loader == this) {
                result = JNLPClassLoader.super.findLoadedClass(name);
            } else {
                result = loader.findLoadedClassAll(name);
            }

            if (result != null)
                return result;
        }
        
        // Result is still null. Return what the codebaseloader 
        // has (which returns null if it is not loaded there either)
        if (codeBaseLoader != null)
            return codeBaseLoader.findLoadedClassFromParent(name);
        else
            return null;
    }

    /**
     * Find a JAR in the shared 'extension' classloaders, this
     * classloader, or one of the classloaders for the JNLP file's
     * extensions.
     * This method used to be qualified "synchronized." This was done solely for the
     * purpose of ensuring only one thread entered the method at a time. This was not
     * strictly necessary - ensuring that all affected fields are thread-safe is
     * sufficient. Locking on the JNLPClassLoader instance when this method is called
     * can result in deadlock if another thread is dealing with the CodebaseClassLoader
     * at the same time. This solution is very heavy-handed as the instance lock is not
     * truly required, and taking the lock on the classloader instance when not needed is
     * not in general a good idea because it can and will lead to deadlock when multithreaded
     * classloading is in effect. The solution is to keep the fields thread safe on their own.
     * This is accomplished by wrapping them in Collections.synchronized* to provide
     * atomic add/remove operations, and synchronizing on them when iterating or performing
     * multiple mutations.
     * See bug report RH976833. On some systems this bug will manifest itself as deadlock on
     * every webpage with more than one Java applet, potentially also causing the browser
     * process to hang.
     * More information in the mailing list archives:
     * http://mail.openjdk.java.net/pipermail/distro-pkg-dev/2013-September/024536.html
     * 
     * Affected fields: available, classpaths, jarIndexes, jarEntries, jarLocationSecurityMap
     */
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> result = findLoadedClassAll(name);

        // try parent classloader
        if (result == null) {
            try {
                ClassLoader parent = getParent();
                if (parent == null)
                    parent = ClassLoader.getSystemClassLoader();

                return parent.loadClass(name);
            } catch (ClassNotFoundException ex) {
            }
        }

        // filter out 'bad' package names like java, javax
        // validPackage(name);

        // search this and the extension loaders
        if (result == null) {
            try {
                result = loadClassExt(name);
            } catch (ClassNotFoundException cnfe) {
                // Not found in external loader either

                // Look in 'Class-Path' as specified in the manifest file
                try {
                    // This field synchronized before iterating over it since it may
                    // be shared data between threads
                    synchronized (classpaths) {
                        for (String classpath : classpaths) {
                            JARDesc desc;
                            try {
                                URL jarUrl = new URL(file.getCodeBase(), classpath);
                                desc = new JARDesc(jarUrl, null, null, false, true, false, true);
                            } catch (MalformedURLException mfe) {
                                throw new ClassNotFoundException(name, mfe);
                            }
                            addNewJar(desc);
                        }
                    }

                    result = loadClassExt(name);
                    return result;
                } catch (ClassNotFoundException cnfe1) {
                    OutputController.getLogger().log(cnfe1);
                }

                // As a last resort, look in any available indexes

                // Currently this loads jars directly from the site. We cannot cache it because this
                // call is initiated from within the applet, which does not have disk read/write permissions
                // This field synchronized before iterating over it since it may
                // be shared data between threads
                synchronized (jarIndexes) {
                    for (JarIndex index : jarIndexes) {
                        // Non-generic code in sun.misc.JarIndex
                        @SuppressWarnings("unchecked")
                        LinkedList<String> jarList = index.get(name.replace('.', '/'));

                        if (jarList != null) {
                            for (String jarName : jarList) {
                                JARDesc desc;
                                try {
                                    desc = new JARDesc(new URL(file.getCodeBase(), jarName),
                                            null, null, false, true, false, true);
                                } catch (MalformedURLException mfe) {
                                    throw new ClassNotFoundException(name);
                                }
                                try {
                                    addNewJar(desc);
                                } catch (Exception e) {
                                    OutputController.getLogger().log(e);
                                }
                            }

                            // If it still fails, let it error out
                            result = loadClassExt(name);
                        }
                    }
                }
            }
        }

        if (result == null) {
            throw new ClassNotFoundException(name);
        }

        return result;
    }

    /**
     * Adds a new JARDesc into this classloader.
     * <p>
     * This will add the JARDesc into the resourceTracker and block until it
     * is downloaded.
     * </p>
     * @param desc the JARDesc for the new jar
     */
    private void addNewJar(final JARDesc desc) {
        this.addNewJar(desc, JNLPRuntime.getDefaultUpdatePolicy());
    }

    /**
     * Adds a new JARDesc into this classloader.
     * @param desc the JARDesc for the new jar
     * @param updatePolicy the UpdatePolicy for the resource
     */
    private void addNewJar(final JARDesc desc, UpdatePolicy updatePolicy) {

        available.add(desc);

        tracker.addResource(desc.getLocation(),
                desc.getVersion(),
                null,
                updatePolicy
                );

        // Give read permissions to the cached jar file
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                Permission p = CacheUtil.getReadPermission(desc.getLocation(),
                        desc.getVersion());

                resourcePermissions.add(p);

                return null;
            }
        });

        final URL remoteURL = desc.getLocation();
        final URL cachedUrl = tracker.getCacheURL(remoteURL); // blocks till download

        available.remove(desc); // Resource downloaded. Remove from available list.
        
        try {

            // Verify if needed

            final List<JARDesc> jars = new ArrayList<JARDesc>();
            jars.add(desc);

            // Decide what level of security this jar should have
            // The verification and security setting functions rely on 
            // having AllPermissions as those actions normally happen
            // during initialization. We therefore need to do those 
            // actions as privileged.

            AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
                public Void run() throws Exception {
                    jcv.add(jars, tracker);

                    checkTrustWithUser();

                    final SecurityDesc security = securityDelegate.getJarPermissions(file.getCodeBase().getHost());

                    jarLocationSecurityMap.put(remoteURL, security);

                    return null;
                }
            });

            addURL(remoteURL);
            CachedJarFileCallback.getInstance().addMapping(remoteURL, cachedUrl);

        } catch (Exception e) {
            // Do nothing. This code is called by loadClass which cannot 
            // throw additional exceptions. So instead, just ignore it. 
            // Exception => jar will not get added to classpath, which will 
            // result in CNFE from loadClass.
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
        }
    }

    /**
     * Find the class in this loader or any of its extension loaders.
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        for (JNLPClassLoader loader : loaders) {
            try {
                if (loader == this) {
                    final String fName = name;
                    return AccessController.doPrivileged(
                            new PrivilegedExceptionAction<Class<?>>() {
                                public Class<?> run() throws ClassNotFoundException {
                                    return JNLPClassLoader.super.findClass(fName);
                                }
                            }, getAccessControlContextForClassLoading());
                } else {
                    return loader.findClass(name);
                }
            } catch (ClassNotFoundException ex) {
            } catch (ClassFormatError cfe) {
                OutputController.getLogger().log(OutputController.Level.ERROR_ALL, cfe);
            } catch (PrivilegedActionException pae) {
            } catch (NullJnlpFileException ex) {
                throw new ClassNotFoundException(this.mainClass + " in main classloader ", ex);
            }
        }

        // Try codebase loader
        if (codeBaseLoader != null)
            return codeBaseLoader.findClassNonRecursive(name);

        // All else failed. Throw CNFE
        throw new ClassNotFoundException(name);
    }

    /**
     * Search for the class by incrementally adding resources to the
     * classloader and its extension classloaders until the resource
     * is found.
     */
    private Class<?> loadClassExt(String name) throws ClassNotFoundException {
        // make recursive
        addAvailable();

        // find it
        try {
            return findClass(name);
        } catch (ClassNotFoundException ex) {
        }

        // add resources until found
        while (true) {
            JNLPClassLoader addedTo = null;
            
            try {
                addedTo = addNextResource();
            } catch (LaunchException e) {

                /*
                 * This method will never handle any search for the main class
                 * [It is handled in initializeResources()]. Therefore, this
                 * exception will never be thrown here and is escaped
                 */

                throw new IllegalStateException(e);
            }

            if (addedTo == null)
                throw new ClassNotFoundException(name);

            try {
                return addedTo.findClass(name);
            } catch (ClassNotFoundException ex) {
            }
        }
    }

    /**
     * Finds the resource in this, the parent, or the extension
     * class loaders.
     *
     * @return a {@link URL} for the resource, or {@code null}
     * if the resource could not be found.
     */
    @Override
    public URL findResource(String name) {
        URL result = null;

        try {
            Enumeration<URL> e = findResources(name);
            if (e.hasMoreElements()) {
                result = e.nextElement();
            }
        } catch (IOException e) {
            OutputController.getLogger().log(e);
        }
        
        // If result is still null, look in the codebase loader
        if (result == null && codeBaseLoader != null)
            result = codeBaseLoader.findResource(name);

        return result;
    }

    /**
     * Find the resources in this, the parent, or the extension
     * class loaders. Load lazy resources if not found in current resources.
     */
    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        Enumeration<URL> resources = findResourcesBySearching(name);

        try {
            // if not found, load all lazy resources; repeat search
            while (!resources.hasMoreElements() && addNextResource() != null) {
                resources = findResourcesBySearching(name);
            }
        } catch (LaunchException le) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, le);
        }

        return resources;
    }

    /**
     * Find the resources in this, the parent, or the extension
     * class loaders.
     */
    private Enumeration<URL> findResourcesBySearching(String name) throws IOException {
        List<URL> resources = new ArrayList<URL>();
        Enumeration<URL> e = null;

        for (JNLPClassLoader loader : loaders) {
            // TODO check if this will blow up or not
            // if loaders[1].getResource() is called, wont it call getResource() on
            // the original caller? infinite recursion?

            if (loader == this) {
                final String fName = name;
                try {
                    e = AccessController.doPrivileged(
                            new PrivilegedExceptionAction<Enumeration<URL>>() {
                                public Enumeration<URL> run() throws IOException {
                                    return JNLPClassLoader.super.findResources(fName);
                                }
                            }, getAccessControlContextForClassLoading());
                } catch (PrivilegedActionException pae) {
                }
            } else {
                e = loader.findResources(name);
            }

            final Enumeration<URL> fURLEnum = e;
            try {
                resources.addAll(AccessController.doPrivileged(
                    new PrivilegedExceptionAction<Collection<URL>>() {
                        public Collection<URL> run() {
                            List<URL> resources = new ArrayList<URL>();
                            while (fURLEnum != null && fURLEnum.hasMoreElements()) {
                                resources.add(fURLEnum.nextElement());
                            }
                            return resources;
                        }
                    }, getAccessControlContextForClassLoading()));
            } catch (PrivilegedActionException pae) {
            }
        }

        // Add resources from codebase (only if nothing was found above, 
        // otherwise the server will get hammered) 
        if (resources.isEmpty() && codeBaseLoader != null) {
            e = codeBaseLoader.findResources(name);
            while (e.hasMoreElements())
                resources.add(e.nextElement());
        }

        return Collections.enumeration(resources);
    }

    /**
     * Returns if the specified resource is available locally from a cached jar
     *
     * @param s The name of the resource
     * @return Whether or not the resource is available locally
     */
    public boolean resourceAvailableLocally(String s) {
        return jarEntries.contains(s);
    }

    /**
     * Adds whatever resources have already been downloaded in the
     * background.
     */
    protected void addAvailable() {
        // go through available, check tracker for it and all of its
        // part brothers being available immediately, add them.

        for (int i = 1; i < loaders.length; i++) {
            loaders[i].addAvailable();
        }
    }

    /**
     * Adds the next unused resource to the classloader.  That
     * resource and all those in the same part will be downloaded
     * and added to the classloader before returning.  If there are
     * no more resources to add, the method returns immediately.
     *
     * @return the classloader that resources were added to, or null
     * @throws LaunchException Thrown if the signed JNLP file, within the main jar, fails to be verified or does not match
     */
    protected JNLPClassLoader addNextResource() throws LaunchException {
        if (available.size() == 0) {
            for (int i = 1; i < loaders.length; i++) {
                JNLPClassLoader result = loaders[i].addNextResource();

                if (result != null)
                    return result;
            }
            return null;
        }

        // add jar
        List<JARDesc> jars = new ArrayList<JARDesc>();
        jars.add(available.get(0));

        fillInPartJars(jars);
        checkForMain(jars);
        activateJars(jars);

        return this;
    }

    // this part compatibility with previous classloader
    /**
     * @deprecated
     */
    @Deprecated
    public String getExtensionName() {
        String result = file.getInformation().getTitle();

        if (result == null)
            result = file.getInformation().getDescription();
        if (result == null && file.getFileLocation() != null)
            result = file.getFileLocation().toString();
        if (result == null && file.getCodeBase() != null)
            result = file.getCodeBase().toString();

        return result;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public String getExtensionHREF() {
        return file.getFileLocation().toString();
    }

    public boolean getSigning() {
        return signing == SigningState.FULL;
    }

    /**
     * Call this when it's suspected that an applet's permission level may have
     * just changed from Full Signing to Partial Signing.
     * This will display a one-time prompt asking the user to confirm running
     * the partially signed applet.
     * Partially Signed applets always start off as appearing to be Fully
     * Signed, and then during the initialization or loading process, we find
     * that we actually need to demote the applet to Partial, either due to
     * finding that not all of its JARs are actually signed, or because it
     * needs to load something unsigned out of the codebase.
     */
    private void checkPartialSigningWithUser() {
        if (signing == SigningState.FULL && JNLPRuntime.isVerifying()) {
            signing = SigningState.PARTIAL;
            try {
                securityDelegate.promptUserOnPartialSigning();
            } catch (LaunchException e) {
                throw new RuntimeException("The signed applet required loading of unsigned code from the codebase, "
                        + "which the user refused", e);
            }
        }
    }

    public SigningState getSigningState() {
        return signing;
    }

    protected SecurityDesc getSecurity() {
        return security;
    }

    /**
     * Returns the security descriptor for given code source URL
     *
     * @param source the origin (remote) url of the code
     * @return The SecurityDescriptor for that source
     */

    protected SecurityDesc getCodeSourceSecurity(URL source) {
        SecurityDesc sec=jarLocationSecurityMap.get(source);
        synchronized (alreadyTried) {
            if (sec == null && !alreadyTried.contains(source)) {
                alreadyTried.add(source);
                //try to load the jar which is requesting the permissions, but was NOT downloaded by standard way
                OutputController.getLogger().log("Application is trying to get permissions for " + source.toString() + ", which was not added by standard way. Trying to download and verify!");
                try {
                    JARDesc des = new JARDesc(source, null, null, false, false, false, false);
                    addNewJar(des);
                    sec = jarLocationSecurityMap.get(source);
                } catch (Throwable t) {
                    OutputController.getLogger().log(t);
                    sec = null;
                }
            }
        }
        if (sec == null){
            OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, Translator.R("LNoSecInstance",source.toString()));
        }
        return sec;
    }

    /**
     * Merges the code source/security descriptor mapping from another loader
     *
     * @param extLoader The loader form which to merge
     * @throws SecurityException if the code is called from an untrusted source
     */
    private void merge(JNLPClassLoader extLoader) {

        try {
            System.getSecurityManager().checkPermission(new AllPermission());
        } catch (SecurityException se) {
            throw new SecurityException("JNLPClassLoader() may only be called from trusted sources!");
        }

        // jars
        for (URL u : extLoader.getURLs())
            addURL(u);
        
        // Codebase
        addToCodeBaseLoader(extLoader.file.getCodeBase());

        // native search paths
        for (File nativeDirectory : extLoader.nativeLibraryStorage.getSearchDirectories()) {
            nativeLibraryStorage.addSearchDirectory(nativeDirectory);
        }

        // security descriptors
        synchronized (jarLocationSecurityMap) {
            for (URL key : extLoader.jarLocationSecurityMap.keySet()) {
                jarLocationSecurityMap.put(key, extLoader.jarLocationSecurityMap.get(key));
            }
        }
    }

    /**
     * Adds the given path to the path loader
     * 
     * @param u the path to add
     * @throws IllegalArgumentException If the given url is not a path
     */
    private void addToCodeBaseLoader(URL u) {
        if (u == null) {
            return;
        }

        // Only paths may be added
        if (!u.getFile().endsWith("/")) {
            throw new IllegalArgumentException("addToPathLoader only accepts path based URLs");
        }

        // If there is no loader yet, create one, else add it to the 
        // existing one (happens when called from merge())
        if (codeBaseLoader == null) {
            codeBaseLoader = new CodeBaseClassLoader(new URL[] { u }, this);
        } else {
            codeBaseLoader.addURL(u);
        }
    }

    /**
     * Returns a set of paths that indicate the Class-Path entries in the
     * manifest file. The paths are rooted in the same directory as the
     * originalJarPath.
     * @param mf the manifest
     * @param originalJarPath the remote/original path of the jar containing
     * the manifest
     * @return a Set of String where each string is a path to the jar on
     * the original jar's classpath.
     */
    private Set<String> getClassPathsFromManifest(Manifest mf, String originalJarPath) {
        Set<String> result = new HashSet<String>();
        if (mf != null) {
            // extract the Class-Path entries from the manifest and split them
            String classpath = mf.getMainAttributes().getValue("Class-Path");
            if (classpath == null || classpath.trim().length() == 0) {
                return result;
            }
            String[] paths = classpath.split(" +");
            for (String path : paths) {
                if (path.trim().length() == 0) {
                    continue;
                }
                // we want to search for jars in the same subdir on the server
                // as the original jar that contains the manifest file, so find
                // out its subdirectory and use that as the dir
                String dir = "";
                int lastSlash = originalJarPath.lastIndexOf("/");
                if (lastSlash != -1) {
                    dir = originalJarPath.substring(0, lastSlash + 1);
                }
                String fullPath = dir + path;
                result.add(fullPath);
            }
        }
        return result;
    }
    
    /**
     * Increments loader use count by 1
     * 
     * @throws SecurityException if caller is not trusted
     */
    private void incrementLoaderUseCount() {

        // For use by trusted code only
        if (System.getSecurityManager() != null)
            System.getSecurityManager().checkPermission(new AllPermission());

        // NB: There will only ever be one class-loader per unique-key
        synchronized ( getUniqueKeyLock(file.getUniqueKey()) ){
            useCount++;
        }
    }

    /**
     * Returns all loaders that this loader uses, including itself
     */
    JNLPClassLoader[] getLoaders() {
        return loaders;
    }

    /**
     * Remove jars from the file system.
     *
     * @param jars Jars marked for removal.
     */
    void removeJars(JARDesc[] jars) {

        for (JARDesc eachJar : jars) {
            try {
                tracker.removeResource(eachJar.getLocation());
            } catch (Exception e) {
                    OutputController.getLogger().log(e);
                    OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "Failed to remove resource from tracker, continuing..");
            }

            File cachedFile = CacheUtil.getCacheFile(eachJar.getLocation(), null);
            String directoryUrl = CacheUtil.getCacheParentDirectory(cachedFile.getAbsolutePath());

            File directory = new File(directoryUrl);

            OutputController.getLogger().log("Deleting cached file: " + cachedFile.getAbsolutePath());

            cachedFile.delete();

            OutputController.getLogger().log("Deleting cached directory: " + directory.getAbsolutePath());

            directory.delete();
        }
    }

    /**
     * Downloads and initializes jars into this loader.
     *
     * @param ref Path of the launch or extension JNLP File containing the
     * resource. If null, main JNLP's file location will be used instead.
     * @param part The name of the path.
     * @throws LaunchException
     */
    void initializeNewJarDownload(URL ref, String part, Version version) {
        JARDesc[] jars = ManageJnlpResources.findJars(this, ref, part, version);

        for (JARDesc eachJar : jars) {
            OutputController.getLogger().log("Downloading and initializing jar: " + eachJar.getLocation().toString());

            this.addNewJar(eachJar, UpdatePolicy.FORCE);
        }
    }

    /**
     * Manages DownloadService jars which are not mentioned in the JNLP file
     * @param ref Path to the resource.
     * @param version The version of resource. If null, no version is specified.
     * @param action The action to perform with the resource. Either DOWNLOADTOCACHE, REMOVEFROMCACHE, or CHECKCACHE.
     * @return true if CHECKCACHE and the resource is cached.
     */
    boolean manageExternalJars(URL ref, String version, DownloadAction action) {
        boolean approved = false;
        JNLPClassLoader foundLoader = LocateJnlpClassLoader.getLoaderByResourceUrl(this, ref, version);
        Version resourceVersion = (version == null) ? null : new Version(version);

        if (foundLoader != null)
            approved = true;

        else if (ref.toString().startsWith(file.getCodeBase().toString()))
            approved = true;
        else if (SecurityDesc.ALL_PERMISSIONS.equals(security.getSecurityType()))
            approved = true;

        if (approved) {
            if (foundLoader == null)
                foundLoader = this;

            if (action == DownloadAction.DOWNLOAD_TO_CACHE) {
                JARDesc jarToCache = new JARDesc(ref, resourceVersion, null, false, true, false, true);
                OutputController.getLogger().log("Downloading and initializing jar: " + ref.toString());

                foundLoader.addNewJar(jarToCache, UpdatePolicy.FORCE);

            } else if (action == DownloadAction.REMOVE_FROM_CACHE) {
                JARDesc[] jarToRemove = { new JARDesc(ref, resourceVersion, null, false, true, false, true) };
                foundLoader.removeJars(jarToRemove);

            } else if (action == DownloadAction.CHECK_CACHE) {
                return CacheUtil.isCached(ref, resourceVersion);
            }
        }
        return false;
    }

    /**
     * Decrements loader use count by 1
     * 
     * If count reaches 0, loader is removed from list of available loaders
     * 
     * @throws SecurityException if caller is not trusted
     */
    public void decrementLoaderUseCount() {

        // For use by trusted code only
        if (System.getSecurityManager() != null)
            System.getSecurityManager().checkPermission(new AllPermission());

        String uniqueKey = file.getUniqueKey();

        // NB: There will only ever be one class-loader per unique-key
        synchronized ( getUniqueKeyLock(uniqueKey) ) {
            useCount--;

            if (useCount <= 0) {
                uniqueKeyToLoader.remove(uniqueKey);
            }
        }
    }

    /**
     * Returns an appropriate AccessControlContext for loading classes in
     * the running instance.
     *
     * The default context during class-loading only allows connection to
     * codebase. However applets are allowed to load jars from arbitrary
     * locations and the codebase only access falls short if a class from
     * one location needs a class from another.
     *
     * Given protected access since CodeBaseClassloader uses this function too.
     *
     * @return The appropriate AccessControlContext for loading classes for this instance
     */
    public AccessControlContext getAccessControlContextForClassLoading() {
        AccessControlContext context = AccessController.getContext();

        try {
            context.checkPermission(new AllPermission());
            return context; // If context already has all permissions, don't bother
        } catch (AccessControlException ace) {
            // continue below
        }

        // Since this is for class-loading, technically any class from one jar
        // should be able to access a class from another, therefore making the
        // original context code source irrelevant
        PermissionCollection permissions = this.security.getSandBoxPermissions();

        // Local cache access permissions
        for (Permission resourcePermission : resourcePermissions) {
            permissions.add(resourcePermission);
        }

        // Permissions for all remote hosting urls
        synchronized (jarLocationSecurityMap) {
            for (URL u : jarLocationSecurityMap.keySet()) {
                permissions.add(new SocketPermission(u.getHost(),
                        "connect, accept"));
            }
        }

        // Permissions for codebase urls (if there is a loader)
        if (codeBaseLoader != null) {
            for (URL u : codeBaseLoader.getURLs()) {
                permissions.add(new SocketPermission(u.getHost(),
                        "connect, accept"));
            }
        }

        ProtectionDomain pd = new ProtectionDomain(null, permissions);

        return new AccessControlContext(new ProtectionDomain[] { pd });
    }
    
    public String getMainClass() {
        return mainClass;
    }
    


   /**
     * SecurityDelegate, in real usage, relies on having a "parent" JNLPClassLoader instance.
     * However, JNLPClassLoaders are very large, heavyweight, difficult-to-mock objects, which
     * means that unit testing on anything that uses a SecurityDelegate can become very difficult.
     * For example, JarCertVerifier is designed separated from the ClassLoader so it can be tested
     * in isolation. However, JCV needs some sort of access back to JNLPClassLoader instances to
     * be able to invoke setRunInSandbox(). The SecurityDelegate handles this, allowing JCV to be
     * tested without instantiating JNLPClassLoaders, by creating a fake SecurityDelegate that does
     * not require one.
     */
    public static interface SecurityDelegate {
        public boolean isPluginApplet();

        public boolean userPromptedForPartialSigning();

        public boolean userPromptedForSandbox();

        public SecurityDesc getCodebaseSecurityDesc(final JARDesc jarDesc, final String codebaseHost);

        public SecurityDesc getClassLoaderSecurity(final String codebaseHost) throws LaunchException;

        public SecurityDesc getJarPermissions(final String codebaseHost);

        public void promptUserOnPartialSigning() throws LaunchException;

        public void setRunInSandbox() throws LaunchException;

        public boolean getRunInSandbox();

        public void addPermission(final Permission perm);

        public void addPermissions(final PermissionCollection perms);

        public void addPermissions(final Collection<Permission> perms);
    }

    /**
     * Handles security decision logic for the JNLPClassLoader, eg which permission level to assign
     * to JARs.
     */
    public static class SecurityDelegateImpl implements SecurityDelegate {
        private final JNLPClassLoader classLoader;
        private boolean runInSandbox;
        private boolean promptedForPartialSigning;
        private boolean promptedForSandbox;

        public SecurityDelegateImpl(final JNLPClassLoader classLoader) {
            this.classLoader = classLoader;
            runInSandbox = false;
            promptedForSandbox = false;
        }

        public boolean isPluginApplet() {
            return classLoader.file instanceof PluginBridge;
        }

        public SecurityDesc getCodebaseSecurityDesc(final JARDesc jarDesc, final String codebaseHost) {
            if (runInSandbox) {
                return new SecurityDesc(classLoader.file,
                        SecurityDesc.SANDBOX_PERMISSIONS,
                        codebaseHost);
            } else {
                if (isPluginApplet()) {
                    try {
                        if (JarCertVerifier.isJarSigned(jarDesc, new PluginAppVerifier(), classLoader.tracker)) {
                            return new SecurityDesc(classLoader.file,
                                    SecurityDesc.ALL_PERMISSIONS,
                                    codebaseHost);
                        } else {
                            return new SecurityDesc(classLoader.file,
                                    SecurityDesc.SANDBOX_PERMISSIONS,
                                    codebaseHost);
                        }
                    } catch (final Exception e) {
                        OutputController.getLogger().log(e);
                        return new SecurityDesc(classLoader.file,
                                SecurityDesc.SANDBOX_PERMISSIONS,
                                codebaseHost);
                    }
                } else {
                    return classLoader.file.getSecurity();
                }
            }
        }

        public SecurityDesc getClassLoaderSecurity(final String codebaseHost) throws LaunchException {
            if (isPluginApplet()) {
                if (!runInSandbox && classLoader.getSigning()) {
                    return new SecurityDesc(classLoader.file,
                            SecurityDesc.ALL_PERMISSIONS,
                            codebaseHost);
                } else {
                    return new SecurityDesc(classLoader.file,
                            SecurityDesc.SANDBOX_PERMISSIONS,
                            codebaseHost);
                }
            } else {
                /*
                 * Various combinations of the jars being signed and <security> tags being
                 * present are possible. They are treated as follows
                 *
                 * Jars          JNLP File         Result
                 *
                 * Signed        <security>        Appropriate Permissions
                 * Signed        no <security>     Sandbox
                 * Unsigned      <security>        Error
                 * Unsigned      no <security>     Sandbox
                 *
                 */
                if (!runInSandbox && !classLoader.getSigning()
                        && !classLoader.file.getSecurity().getSecurityType().equals(SecurityDesc.SANDBOX_PERMISSIONS)) {
                    if (classLoader.jcv.allJarsSigned()) {
                        throw new LaunchException(classLoader.file, null, R("LSFatal"), R("LCClient"), R("LSignedJNLPAppDifferentCerts"), R("LSignedJNLPAppDifferentCertsInfo"));
                    } else {
                        throw new LaunchException(classLoader.file, null, R("LSFatal"), R("LCClient"), R("LUnsignedJarWithSecurity"), R("LUnsignedJarWithSecurityInfo"));
                    }
                } else if (!runInSandbox && classLoader.getSigning()) {
                    return classLoader.file.getSecurity();
                } else {
                    return new SecurityDesc(classLoader.file,
                            SecurityDesc.SANDBOX_PERMISSIONS,
                            codebaseHost);
                }
            }
        }

        public SecurityDesc getJarPermissions(final String codebaseHost) {
            if (!runInSandbox && classLoader.jcv.isFullySigned()) {
                // Already trust application, nested jar should be given
                return new SecurityDesc(classLoader.file,
                        SecurityDesc.ALL_PERMISSIONS,
                        codebaseHost);
            } else {
                return new SecurityDesc(classLoader.file,
                        SecurityDesc.SANDBOX_PERMISSIONS,
                        codebaseHost);
            }
        }

        public void setRunInSandbox() throws LaunchException {
            if (promptedForSandbox || classLoader.security != null
                    || classLoader.jarLocationSecurityMap.size() != 0) {
                throw new LaunchException(classLoader.file, null, R("LSFatal"), R("LCInit"), R("LRunInSandboxError"), R("LRunInSandboxErrorInfo"));
            }

            JNLPRuntime.reloadPolicy(); // ensure that we have the most up-to-date custom policy loaded
            this.promptedForSandbox = true;
            this.runInSandbox = true;
        }

        public void promptUserOnPartialSigning() throws LaunchException {
            if (promptedForPartialSigning || JNLPRuntime.isTrustAll()) {
                return;
            }
            promptedForPartialSigning = true;
            UnsignedAppletTrustConfirmation.checkPartiallySignedWithUserIfRequired(this, classLoader.file, classLoader.jcv);
        }

        public boolean getRunInSandbox() {
            return this.runInSandbox;
        }

        public boolean userPromptedForPartialSigning() {
            return this.promptedForPartialSigning;
        }

        public boolean userPromptedForSandbox() {
            return this.promptedForSandbox;
        }

        public void addPermission(final Permission perm) {
            classLoader.addPermission(perm);
        }

        public void addPermissions(final PermissionCollection perms) {
            Enumeration<Permission> e = perms.elements();
            while (e.hasMoreElements()) {
                addPermission(e.nextElement());
            }
        }

        public void addPermissions(final Collection<Permission> perms) {
            for (final Permission perm : perms) {
                addPermission(perm);
            }
        }

    }
    

    /*
     * Helper class to expose protected URLClassLoader methods.
     * Classes loaded from the codebase are absolutely NOT signed, by definition!
     * If the CodeBaseClassLoader is used to load any classes in JNLPClassLoader,
     * then you *MUST* check if the JNLPClassLoader is set to FULL signing. If so,
     * then it must be set instead to PARTIAL, and the user prompted if it is okay
     * to proceed. If the JNLPClassLoader is already PARTIAL or NONE signing, then
     * nothing must be done. This is required so that we can support partial signing
     * of applets but also ensure that using codebase loading in conjunction with
     * signed JARs still results in the user having to confirm that this is
     * acceptable.
     */
    public static class CodeBaseClassLoader extends URLClassLoader {

        JNLPClassLoader parentJNLPClassLoader;
        
        /**
         * Classes that are not found, so that findClass can skip them next time
         */
        ConcurrentHashMap<String, URL[]> notFoundResources = new ConcurrentHashMap<String, URL[]>();

        public CodeBaseClassLoader(URL[] urls, JNLPClassLoader cl) {
            super(urls, cl);
            parentJNLPClassLoader = cl;
        }

        @Override
        public void addURL(URL url) { 
            super.addURL(url); 
        }

        /*
         * Use with care! Check the class-level Javadoc before calling this.
         */
        Class<?> findClassNonRecursive(final String name) throws ClassNotFoundException {
            // If we have searched this path before, don't try again
            if (Arrays.equals(super.getURLs(), notFoundResources.get(name)))
                throw new ClassNotFoundException(name);

            try {
                return AccessController.doPrivileged(
                        new PrivilegedExceptionAction<Class<?>>() {
                            public Class<?> run() throws ClassNotFoundException {
                                Class<?> c = CodeBaseClassLoader.super.findClass(name);
                                parentJNLPClassLoader.checkPartialSigningWithUser();
                                return c;
                            }
                        }, parentJNLPClassLoader.getAccessControlContextForClassLoading());
            } catch (PrivilegedActionException pae) {
                notFoundResources.put(name, super.getURLs());
                throw new ClassNotFoundException("Could not find class " + name, pae);
            } catch (NullJnlpFileException njf) {
                notFoundResources.put(name, super.getURLs());
                throw new ClassNotFoundException("Could not find class " + name, njf);
            }
        }

        /*
         * Use with care! Check the class-level Javadoc before calling this.
         */
        @Override
        public Class<?> findClass(String name) throws ClassNotFoundException {
            // Calls JNLPClassLoader#findClass which may call into this.findClassNonRecursive
            Class<?> c = getParentJNLPClassLoader().findClass(name);
            parentJNLPClassLoader.checkPartialSigningWithUser();
            return c;
        }

        /**
         * Returns the output of super.findLoadedClass().
         * 
         * The method is renamed because ClassLoader.findLoadedClass() is final
         * 
         * @param name The name of the class to find
         * @return Output of ClassLoader.findLoadedClass() which is the class if found, null otherwise 
         * @see java.lang.ClassLoader#findLoadedClass(String)
         */
        public Class<?> findLoadedClassFromParent(String name) {
            return findLoadedClass(name);
        }

        /**
         * Returns JNLPClassLoader that encompasses this loader
         * 
         * @return parent JNLPClassLoader
         */
        public JNLPClassLoader getParentJNLPClassLoader() {
            return parentJNLPClassLoader;
        }

        @Override
        public Enumeration<URL> findResources(String name) throws IOException {

            // If we have searched this path before, don't try again
            if (Arrays.equals(super.getURLs(), notFoundResources.get(name)))
                return (new Vector<URL>(0)).elements();

            if (!name.startsWith("META-INF")) {
                Enumeration<URL> urls = super.findResources(name);

                if (!urls.hasMoreElements()) {
                    notFoundResources.put(name, super.getURLs());
                }

                return urls;
            }

            return (new Vector<URL>(0)).elements();
        }

        @Override
        public URL findResource(String name) {

            // If we have searched this path before, don't try again
            if (Arrays.equals(super.getURLs(), notFoundResources.get(name)))
                return null;

            URL url = null;
            if (!name.startsWith("META-INF")) {
                try {
                    final String fName = name;
                    url = AccessController.doPrivileged(
                            new PrivilegedExceptionAction<URL>() {
                                public URL run() {
                                    return CodeBaseClassLoader.super.findResource(fName);
                                }
                            }, parentJNLPClassLoader.getAccessControlContextForClassLoading());
                } catch (PrivilegedActionException pae) {
                } 

                if (url == null) {
                    notFoundResources.put(name, super.getURLs());
                }

                return url;
            }

            return null;
        }
    }
    
    
}
