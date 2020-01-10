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
package net.sourceforge.jnlp.runtime.classloader;

import net.adoptopenjdk.icedteaweb.http.CloseableConnection;
import net.adoptopenjdk.icedteaweb.http.ConnectionFactory;
import net.adoptopenjdk.icedteaweb.jdk89access.JarIndexAccess;
import net.adoptopenjdk.icedteaweb.jnlp.element.EntryPoint;
import net.adoptopenjdk.icedteaweb.jnlp.element.application.AppletDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.application.ApplicationDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.ExtensionDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.ResourcesDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.security.SecurityDesc;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.manifest.ManifestAttributesChecker;
import net.adoptopenjdk.icedteaweb.manifest.ManifestAttributesReader;
import net.adoptopenjdk.icedteaweb.resources.IllegalResourceDescriptorException;
import net.adoptopenjdk.icedteaweb.resources.ResourceTracker;
import net.adoptopenjdk.icedteaweb.resources.UpdatePolicy;
import net.adoptopenjdk.icedteaweb.resources.cache.Cache;
import net.adoptopenjdk.icedteaweb.xmlparser.ParseException;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.JNLPFileFactory;
import net.sourceforge.jnlp.JNLPMatcher;
import net.sourceforge.jnlp.JNLPMatcherException;
import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.NullJnlpFileException;
import net.sourceforge.jnlp.ParserSettings;
import net.sourceforge.jnlp.cache.CacheUtil;
import net.sourceforge.jnlp.cache.NativeLibraryStorage;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.runtime.ApplicationInstance;
import net.sourceforge.jnlp.runtime.CachedJarFileCallback;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.security.AppVerifier;
import net.sourceforge.jnlp.security.JNLPAppVerifier;
import net.sourceforge.jnlp.tools.JarCertVerifier;
import net.sourceforge.jnlp.util.JarFile;
import net.sourceforge.jnlp.util.UrlUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.stream.Stream;

import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;
import static net.sourceforge.jnlp.LaunchException.FATAL;
import static net.sourceforge.jnlp.util.UrlUtils.FILE_PROTOCOL;
import static sun.security.util.SecurityConstants.FILE_READ_ACTION;

/**
 * Classloader that takes it's resources from a JNLP file. If the JNLP file
 * defines extensions, separate classloaders for these will be created
 * automatically. Classes are loaded with the security context when the
 * classloader was created.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell
 * (JAM)</a> - initial author
 * @version $Revision: 1.20 $
 */
public class JNLPClassLoader extends URLClassLoader {

    private static final Logger LOG = LoggerFactory.getLogger(JNLPClassLoader.class);

    // todo: initializePermissions should get the permissions from
    // extension classes too so that main file classes can load
    // resources in an extension.
    /**
     * Signed JNLP File and Template
     */
    private static final String TEMPLATE = "JNLP-INF/APPLICATION_TEMPLATE.JNLP";
    private static final String APPLICATION = "JNLP-INF/APPLICATION.JNLP";

    /**
     * True if the application has a signed JNLP File
     */
    private boolean isSignedJNLP = false;

    /**
     * map from JNLPFile unique key to shared classloader
     */
    private static final Map<String, JNLPClassLoader> uniqueKeyToLoader = new ConcurrentHashMap<>();

    /**
     * map from JNLPFile unique key to lock, the lock is needed to enforce
     * correct initialization of applets that share a unique key
     */
    private static final Map<String, ReentrantLock> uniqueKeyToLock = new HashMap<>();

    /**
     * Provides a search path & temporary storage for native code
     */
    private final NativeLibraryStorage nativeLibraryStorage;

    /**
     * security context
     */
    private final AccessControlContext acc = AccessController.getContext();

    /**
     * the permissions for the cached jar files
     */
    private final List<Permission> resourcePermissions;

    /**
     * the app
     */
    private ApplicationInstance app = null; // here for faster lookup in security manager

    /**
     * list of this, local and global loaders this loader uses
     */
    private JNLPClassLoader[] loaders = null; // ..[0]==this

    /**
     * whether to strictly adhere to the spec or not
     */
    private final boolean strict;

    /**
     * loads the resources
     */
    private final ResourceTracker tracker;

    /**
     * the update policy for resources
     */
    private final UpdatePolicy updatePolicy;

    /**
     * the JNLP file
     */
    private final JNLPFile file;

    /**
     * the resources section
     */
    private final ResourcesDesc resources;

    /**
     * the security section
     */
    private SecurityDesc security;

    /**
     * Permissions granted by the user during runtime.
     */
    private final ArrayList<Permission> runtimePermissions = new ArrayList<>();

    /**
     * all jars not yet part of classloader or active Synchronized since this
     * field may become shared data between multiple classloading threads. See
     * loadClass(String) and CodebaseClassLoader.findClassNonRecursive(String).
     */
    private final List<JARDesc> available = Collections.synchronizedList(new ArrayList<>());

    /**
     * the jar cert verifier tool to verify our jars
     */
    final JarCertVerifier jcv;

    private SigningState signing = SigningState.NONE;

    /**
     * ArrayList containing jar indexes for various jars available to this
     * classloader Synchronized since this field may become shared data between
     * multiple classloading threads/ See loadClass(String) and
     * CodebaseClassLoader.findClassNonRecursive(String).
     */
    private final List<JarIndexAccess> jarIndexes = Collections.synchronizedList(new ArrayList<>());

    /**
     * Set of classpath strings declared in the manifest.mf files Synchronized
     * since this field may become shared data between multiple classloading
     * threads. See loadClass(String) and
     * CodebaseClassLoader.findClassNonRecursive(String).
     */
    private final Set<String> classpaths = Collections.synchronizedSet(new HashSet<>());

    /**
     * File entries in the jar files available to this classloader Synchronized
     * sinc this field may become shared data between multiple classloading
     * threads. See loadClass(String) and
     * CodebaseClassLoader.findClassNonRecursive(String).
     */
    private final Set<String> jarEntries = Collections.synchronizedSet(new TreeSet<>());

    /**
     * Map of specific original (remote) CodeSource Urls to securitydesc
     * Synchronized since this field may become shared data between multiple
     * classloading threads. See loadClass(String) and
     * CodebaseClassLoader.findClassNonRecursive(String).
     */
    final Map<URL, SecurityDesc> jarLocationSecurityMap = Collections.synchronizedMap(new HashMap<>());

    /*Set to prevent once tried-to-get resources to be tried again*/
    private final Set<URL> alreadyTried = Collections.synchronizedSet(new HashSet<>());

    /**
     * Loader for codebase (which is a path, rather than a file)
     */
    private CodeBaseClassLoader codeBaseLoader;

    /**
     * True if the jar with the main class has been found
     */
    private boolean foundMainJar = false;

    /**
     * Name of the application's main class
     */
    private String mainClass;

    /**
     * Variable to track how many times this loader is in use
     */
    private int useCount = 0;

    private boolean enableCodeBase;

    private final SecurityDelegate securityDelegate;

    private ManifestAttributesChecker mac;

    /**
     * Create a new JNLPClassLoader from the specified file.
     *
     * @param file   the JNLP file
     * @param policy update policy of loader
     * @throws net.sourceforge.jnlp.LaunchException if app can not be loaded
     */
    public JNLPClassLoader(JNLPFile file, UpdatePolicy policy) throws LaunchException {
        this(file, policy, null, false);
    }

    /**
     * Create a new JNLPClassLoader from the specified file.
     *
     * @param file           the JNLP file
     * @param policy         the UpdatePolicy for this class loader
     * @param mainName       name of the application's main class
     * @param enableCodeBase switch whether this classloader can search in
     *                       codebase or not
     * @throws net.sourceforge.jnlp.LaunchException when need to kill an app
     *                                              comes.
     */
    private JNLPClassLoader(JNLPFile file, UpdatePolicy policy, String mainName, boolean enableCodeBase) throws LaunchException {
        super(new URL[0], JNLPClassLoader.class.getClassLoader());

        LOG.info("New classloader: {}", file.getFileLocation());
        strict = Boolean.parseBoolean(JNLPRuntime.getConfiguration().getProperty(ConfigurationConstants.KEY_STRICT_JNLP_CLASSLOADER));

        this.file = file;
        this.tracker = new ResourceTracker(true, file.getDownloadOptions(), JNLPRuntime.getDefaultUpdatePolicy());
        this.updatePolicy = policy;
        this.resources = file.getResources();

        this.nativeLibraryStorage = new NativeLibraryStorage(tracker);

        this.mainClass = mainName;

        this.enableCodeBase = enableCodeBase;

        final AppVerifier verifier = new JNLPAppVerifier();

        jcv = new JarCertVerifier(verifier);

        if (this.enableCodeBase) {
            addToCodeBaseLoader(this.file.getCodeBase());
        }

        this.securityDelegate = new SecurityDelegateImpl(this);

        if (mainClass == null) {
            final EntryPoint entryPoint = file.getEntryPointDesc();
            if (entryPoint instanceof ApplicationDesc || entryPoint instanceof AppletDesc) {
                mainClass = entryPoint.getMainClass();
            }
        }
        resourcePermissions = new ArrayList<>();

        // initialize extensions
        initializeExtensions();

        initializeResources();

        // initialize permissions
        initializeReadJarPermissions();

        installShutdownHooks();

    }

    /**
     * Install JVM shutdown hooks to clean up resources allocated by this
     * ClassLoader.
     */
    private void installShutdownHooks() {
        /*
         * Delete only the native dir created by this classloader (if
         * there is one). Other classloaders (parent, peers) will all
         * cleanup things they created
         */
        Runtime.getRuntime().addShutdownHook(new Thread(nativeLibraryStorage::cleanupTemporaryFolder));
    }

    private void setSecurity() throws LaunchException {
        URL codebase = UrlUtils.guessCodeBase(file);
        this.security = securityDelegate.getClassLoaderSecurity(codebase);
    }

    /**
     * Gets the lock for a given unique key, creating one if it does not yet
     * exist. This operation is atomic & thread-safe.
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
     * to be used as an applet/application's classloader. In contrast, JNLP
     * classloaders can also be constructed simply to merge its resources into
     * another classloader.
     *
     * @param file     the file to load classes for
     * @param policy   the update policy to use when downloading resources
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
        }

        // New loader init may have caused extensions to create a
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
     * @param file           the file to load classes for
     * @param policy         the update policy to use when downloading resources
     * @param enableCodeBase true if codebase can be searched (ok for
     *                       applets,false for apps)
     * @return existing classloader. creates new if none reliable exists
     * @throws net.sourceforge.jnlp.LaunchException when launch is doomed
     */
    public static JNLPClassLoader getInstance(JNLPFile file, UpdatePolicy policy, boolean enableCodeBase) throws LaunchException {
        return getInstance(file, policy, null, enableCodeBase);
    }

    /**
     * Returns a JNLP classloader for the specified JNLP file.
     *
     * @param file           the file to load classes for
     * @param policy         the update policy to use when downloading resources
     * @param mainName       Overrides the main class name of the application
     * @param enableCodeBase ue if codebase can be searched (ok for
     *                       applets,false for apps)
     * @return existing classloader. creates new if none reliable exists
     * @throws net.sourceforge.jnlp.LaunchException when launch is doomed
     */
    private static JNLPClassLoader getInstance(JNLPFile file, UpdatePolicy policy, String mainName, boolean enableCodeBase) throws LaunchException {
        JNLPClassLoader loader;
        String uniqueKey = file.getUniqueKey();

        synchronized (getUniqueKeyLock(uniqueKey)) {
            JNLPClassLoader baseLoader = uniqueKeyToLoader.get(uniqueKey);

            // A null baseloader implies that no loader has been created
            // for this codebase/jnlp yet. Create one.
            if (baseLoader == null
                    || (file.isApplication()
                    && !baseLoader.getJNLPFile().getFileLocation().equals(file.getFileLocation()))) {

                loader = createInstance(file, policy, mainName, enableCodeBase);
            } else {
                // if key is same and locations match, this is the loader we want
                if (!file.isApplication()) {
                    // If this is an applet, we do need to consider its loader
                    loader = new JNLPClassLoader(file, policy, mainName, enableCodeBase);
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
     * Returns a JNLP classloader for the JNLP file at the specified location.
     *
     * @param location       the file's location
     * @param uniqueKey      key to manage applets/applications in shared vm
     * @param version        the file's version
     * @param settings       settings of parser
     * @param policy         the update policy to use when downloading resources
     * @param mainName       Overrides the main class name of the application
     * @param enableCodeBase whether to enable codebase search or not
     * @return classloader of this app
     * @throws java.io.IOException                  when IO fails
     * @throws ParseException                       when parsing fails
     * @throws net.sourceforge.jnlp.LaunchException when launch is doomed
     */
    private static JNLPClassLoader getInstance(final URL location, final String uniqueKey, final VersionString version, final ParserSettings settings, final UpdatePolicy policy, final String mainName, boolean enableCodeBase)
            throws IOException, ParseException, LaunchException {

        JNLPClassLoader loader;

        synchronized (getUniqueKeyLock(uniqueKey)) {
            loader = uniqueKeyToLoader.get(uniqueKey);

            if (loader == null || !location.equals(loader.getJNLPFile().getFileLocation())) {
                final JNLPFile jnlpFile = new JNLPFileFactory().create(location, uniqueKey, version, settings, policy);

                loader = getInstance(jnlpFile, policy, mainName, enableCodeBase);
            }
        }

        return loader;
    }

    /**
     * Load the extensions specified in the JNLP file.
     */
    private void initializeExtensions() {
        final List<Exception> exceptions = new ArrayList<>();
        final List<JNLPClassLoader> loaderList = new ArrayList<>();
        loaderList.add(this);

        final ExtensionDesc[] extDescs = resources.getExtensions();
        if (extDescs != null) {
            final String uniqueKey = file.getUniqueKey();
            for (ExtensionDesc ext : extDescs) {
                try {
                    final JNLPClassLoader loader = getInstance(ext.getLocation(), uniqueKey, ext.getVersion(), file.getParserSettings(), updatePolicy, mainClass, enableCodeBase);
                    loaderList.add(loader);
                } catch (Exception ex) {
                    exceptions.add(new Exception("Exception while initializing extension '" + ext.getLocation() + "'", ex));
                }
            }
        }

        if (exceptions.size() > 0) {
            exceptions.forEach(e -> LOG.error(e.getMessage(), e.getCause()));
            throw new RuntimeException(exceptions.get(0));
        }

        loaders = loaderList.toArray(new JNLPClassLoader[0]);
    }

    /**
     * Make permission objects for the classpath.
     */
    private void initializeReadJarPermissions() {

        JARDesc[] jars = resources.getJARs();
        for (JARDesc jar : jars) {
            Permission p = getReadPermission(jar);

            if (p == null) {
                LOG.info("Unable to add permission for {}", jar.getLocation());
            } else {
                resourcePermissions.add(p);
                LOG.info("Permission added: {}", p.toString());
            }
        }
    }

    private Permission getReadPermission(JARDesc jar) {
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

    /**
     * Load all of the JARs used in this JNLP file into the ResourceTracker for
     * downloading.
     */
    private void initializeResources() throws LaunchException {

        final JARDesc[] jars = resources.getJARs();

        if (jars.length == 0) {
            LOG.debug("no jars defined in jnlp file '{}'", file.getSourceLocation());

            if (loaders.length > 1) {
                LOG.debug("Checking extensions of jnlp file '{}'", file.getSourceLocation());
                final boolean containsUnsigned = Stream.of(loaders).anyMatch(l -> !l.getSigning());
                if (containsUnsigned) {
                    LOG.debug("At least one extension for jnlp file '{}' contains unsigned content", file.getSourceLocation());
                    //TODO: is NONE really right? We do not kn ow if it is NONE or PARTIAL....
                    signing = SigningState.NONE;
                } else {
                    LOG.debug("All extensions of jnlp file '{}' are fully signed", file.getSourceLocation());
                    signing = SigningState.FULL;
                }
            } else {
                LOG.debug("JNLP file {} does not contain any jars or extensions and therefore is marked as fully signed", file.getSourceLocation());
                signing = SigningState.FULL;
            }

            //Check if main jar is found within extensions
            foundMainJar = foundMainJar || hasMainInExtensions();

            setSecurity();
            initializeManifestAttributesChecker();
            mac.checkAll();
            return;
        }

        final List<JARDesc> initialJars = new ArrayList<>();

        for (JARDesc jar : jars) {

            available.add(jar);

            if (jar.isEager() || jar.isMain()) {
                initialJars.add(jar); // regardless of part
            }
            // FIXME: this will trigger an eager download as the tracker is created with prefetch == true
            tracker.addResource(jar.getLocation(), jar.getVersion(),
                    jar.isCacheable() ? JNLPRuntime.getDefaultUpdatePolicy() : UpdatePolicy.FORCE);
        }

        //If there are no eager jars, initialize the first jar
        if (initialJars.isEmpty()) {
            initialJars.add(jars[0]);
        }

        if (strict) {
            fillInPartJars(initialJars); // add in each initial part's lazy jars
        }

        waitForJars(initialJars); //download the jars first.

        if (JNLPRuntime.isVerifying()) {
            try {
                jcv.add(initialJars, tracker);
            } catch (Exception e) {
                //we caught an Exception from the JarCertVerifier class.
                //Note: one of these exceptions could be from not being able
                //to read the cacerts or trusted.certs files.
                LOG.error("Exception while verifying jars", e);
                LaunchException ex = new LaunchException(null, null, FATAL,
                        "Initialization Error", "A fatal error occurred while trying to verify jars.", "An exception has been thrown in class JarCertVerifier. Being unable to read the cacerts or trusted.certs files could be a possible cause for this exception.: " + e.getMessage());
                SecurityDelegateImpl.consultCertificateSecurityException(ex);
            }

            //Case when at least one jar has some signing
            if (jcv.isFullySigned()) {
                signing = SigningState.FULL;

                // Check for main class in the downloaded jars, and check/verify signed JNLP fill
                checkForMain(initialJars);

                // If jar with main class was not found, check available resources
                while (!foundMainJar && !available.isEmpty()) {
                    addNextResource();
                }

                // If the jar with main class was not found, check extension
                // jnlp's resources
                foundMainJar = foundMainJar || hasMainInExtensions();

                boolean externalAppletMainClass = file.getEntryPointDesc() != null && !foundMainJar && available.isEmpty();

                // We do this check here simply to ensure that if there are no JARs at all,
                // and also no main-class in the codebase (ie the applet doesn't really exist), we
                // fail ASAP rather than continuing (and showing the NotAllSigned dialog for no applet)
                if (externalAppletMainClass) {
                    if (codeBaseLoader != null) {
                        try {
                            codeBaseLoader.findClass(mainClass);
                        } catch (ClassNotFoundException extCnfe) {
                            LOG.error("Could not determine the main class for this application.", extCnfe);
                            throw new LaunchException(file, extCnfe, FATAL, "Initialization Error", "Unknown Main-Class.", "Could not determine the main class for this application.");
                        }
                    } else {
                        throw new LaunchException(file, null, FATAL, "Initialization Error", "Unknown Main-Class.", "Could not determine the main class for this application.");
                    }
                }

                // If externalAppletMainClass is true and a LaunchException was not thrown above,
                // then the main-class can be loaded from the applet codebase, but is obviously not signed
                if (externalAppletMainClass) {
                    checkPartialSigningWithUser();
                }

                // If main jar was found, but a signed JNLP file was not located
                if (!isSignedJNLP && foundMainJar) {
                    file.setSignedJNLPAsMissing();
                }

                //user does not trust this publisher
                if (!jcv.isTriviallySigned()) {
                    checkTrustWithUser();
                }
            } else {

                // Otherwise this jar is simply unsigned -- make sure to ask
                // for permission on certain actions
                signing = SigningState.NONE;
            }
        }
        setSecurity();

        final Set<JARDesc> validJars = new HashSet<>();
        boolean containsSignedJar = false, containsUnsignedJar = false;
        for (JARDesc jarDesc : file.getResources().getJARs()) {
            File cachedFile;

            try {
                cachedFile = tracker.getCacheFile(jarDesc.getLocation());
            } catch (IllegalResourceDescriptorException irde) {
                //Caused by ignored resource being removed due to not being valid
                LOG.error("JAR " + jarDesc.getLocation() + " is not a valid jar file. Continuing.", irde);
                continue;
            }

            if (cachedFile == null) {
                LOG.warn("initializeResource JAR {} not found. Continuing.", jarDesc.getLocation());
                continue; // JAR not found. Keep going.
            }

            validJars.add(jarDesc);
            final URL codebase = getJnlpFileCodebase();

            final SecurityDesc jarSecurity = securityDelegate.getCodebaseSecurityDesc(jarDesc, codebase);
            if (jarSecurity.getSecurityType().equals(SecurityDesc.SANDBOX_PERMISSIONS)) {
                containsUnsignedJar = true;
            } else {
                containsSignedJar = true;
            }

            if (containsUnsignedJar && containsSignedJar) {
                signing = SigningState.PARTIAL;
                break;
            }
        }

        if (containsSignedJar && containsUnsignedJar) {
            checkPartialSigningWithUser();
        }

        setSecurity();

        initializeManifestAttributesChecker();
        mac.checkAll();

        for (JARDesc jarDesc : validJars) {
            final URL codebase = getJnlpFileCodebase();
            final SecurityDesc jarSecurity = securityDelegate.getCodebaseSecurityDesc(jarDesc, codebase);
            jarLocationSecurityMap.put(jarDesc.getLocation(), jarSecurity);
        }

        activateJars(initialJars);
    }

    private void initializeManifestAttributesChecker() {
        if (mac == null) {
            file.getManifestAttributesReader().setLoader(this);
            mac = new ManifestAttributesChecker(security, file, signing, securityDelegate);
        }
    }

    private URL getJnlpFileCodebase() {
        final URL codebase;
        if (file.getCodeBase() != null) {
            codebase = file.getCodeBase();
        } else {
            // FIXME: codebase should be the codebase of the Main Jar not
            // the location. Although, it still works in the current state.
            codebase = file.getResources().getMainJAR().getLocation();
        }
        return codebase;
    }

    /**
     * *
     * Checks for the jar that contains the main class. If the main class was
     * found, it checks to see if the jar is signed and whether it contains a
     * signed JNLP file
     *
     * @param jars Jars that are checked to see if they contain the main class
     * @throws LaunchException Thrown if the signed JNLP file, within the main
     *                         jar, fails to be verified or does not match
     */
    private void checkForMain(List<JARDesc> jars) throws LaunchException {

        // Check launch info
        if (mainClass == null) {
            final EntryPoint entryPoint = file.getEntryPointDesc();
            if (entryPoint != null) {
                mainClass = entryPoint.getMainClass();
            }
        }

        // The main class may be specified in the manifest
        if (mainClass == null) {
            mainClass = ManifestAttributesReader.getAttributeFromJars(Attributes.Name.MAIN_CLASS, jars, tracker);
        }

        final String desiredJarEntryName = mainClass + ".class";

        for (JARDesc jar : jars) {

            try {
                final File localFile = tracker.getCacheFile(jar.getLocation());

                if (localFile == null) {
                    LOG.warn("checkForMain JAR {} not found. Continuing.", jar.getLocation());
                    continue; // JAR not found. Keep going.
                }

                try (final JarFile jarFile = new JarFile(localFile)) {
                    for (JarEntry entry : Collections.list(jarFile.entries())) {
                        String jeName = entry.getName().replaceAll("/", ".");
                        if (jeName.equals(desiredJarEntryName)) {
                            foundMainJar = true;
                            verifySignedJNLP(jarFile);
                            break;
                        }
                    }
                }
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
     * @return true if this loader has the main jar
     */
    private boolean hasMainJar() {
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
     * @param jarFile the jar file
     * @throws LaunchException thrown if the signed JNLP file, within the main
     *                         jar, fails to be verified or does not match
     */
    private void verifySignedJNLP(JarFile jarFile) throws LaunchException {
        try {
            // NOTE: verification should have happened by now. In other words,
            // calling jcv.verifyJars(desc, tracker) here should have no affect.
            if (jcv.isFullySigned()) {

                for (JarEntry je : Collections.list(jarFile.entries())) {
                    String jeName = je.getName().toUpperCase();

                    if (jeName.equals(TEMPLATE) || jeName.equals(APPLICATION)) {
                        LOG.debug("Creating Jar InputStream from JarEntry");
                        InputStream inStream = jarFile.getInputStream(je);
                        LOG.debug("Creating File InputStream from launching JNLP file");
                        File jn;
                        // If the file is on the local file system, use original path, otherwise find cached file
                        if (file.getFileLocation().getProtocol().toLowerCase().equals(FILE_PROTOCOL)) {
                            jn = new File(file.getFileLocation().getPath());
                        } else {
                            jn = Cache.getCacheFile(file.getFileLocation(), file.getFileVersion());
                        }

                        InputStream jnlpStream = new FileInputStream(jn);
                        JNLPMatcher matcher;
                        if (jeName.equals(APPLICATION)) { // If signed application was found
                            LOG.debug("APPLICATION.JNLP has been located within signed JAR. Starting verification...");
                            matcher = new JNLPMatcher(inStream, jnlpStream, false, file.getParserSettings());
                        } else { // Otherwise template was found
                            LOG.debug("APPLICATION_TEMPLATE.JNLP has been located within signed JAR. Starting verification...");
                            matcher = new JNLPMatcher(inStream, jnlpStream, true, file.getParserSettings());
                        }
                        // If signed JNLP file does not matches launching JNLP file, throw JNLPMatcherException
                        if (!matcher.isMatch()) {
                            throw new JNLPMatcherException("Signed Application did not match launching JNLP File");
                        }

                        this.isSignedJNLP = true;
                        LOG.debug("Signed Application Verification Successful");

                        break;
                    }
                }
            }
        } catch (JNLPMatcherException e) {

            /*
             * Throws LaunchException if signed JNLP file fails to be verified
             * or fails to match the launching JNLP file
             */
            LaunchException ex = new LaunchException(file, null, FATAL, "Application Error",
                    "The signed JNLP file did not match the launching JNLP file.", R(e.getMessage()));
            SecurityDelegateImpl.consultCertificateSecurityException(ex);
            /*
             * Throwing this exception will fail to initialize the application
             * resulting in the termination of the application
             */

        } catch (Exception e) {
            LOG.error("failed to validate the JNLP file itself", e);

            /*
             * After this exception is caught, it is escaped. If an exception is
             * thrown while handling the jar file, (mainly for
             * JarCertVerifier.add) it assumes the jar file is unsigned and
             * skip the check for a signed JNLP file
             */
        }
        LOG.debug("Ending check for signed JNLP file...");
    }

    /**
     * Prompt the user for trust on all the signers that require approval.
     *
     * @throws LaunchException if the user does not approve every dialog prompt.
     */
    private void checkTrustWithUser() throws LaunchException {

        if (securityDelegate.getRunInSandbox()) {
            return;
        }

        if (getSigningState() == SigningState.FULL && jcv.isFullySigned() && !jcv.getAlreadyTrustPublisher()) {
            jcv.checkTrustWithUser(securityDelegate, file);
        }
    }

    /**
     * Add applet's codebase URL. This allows compatibility with applets that
     * load resources from their codebase instead of through JARs, but can slow
     * down resource loading. Resources loaded from the codebase are not cached.
     */
    public void enableCodeBase() {
        addToCodeBaseLoader(file.getCodeBase());
    }

    /**
     * Sets the JNLP app this group is for; can only be called once.
     *
     * @param app application to be ser to this group
     */
    public void setApplication(ApplicationInstance app) {
        if (this.app != null) {
            LOG.error("Application can only be set once");
            return;
        }

        this.app = app;
    }

    /**
     * @return the JNLP app for this classloader
     */
    public ApplicationInstance getApplication() {
        return app;
    }

    /**
     * @return the JNLP file the classloader was created from.
     */
    public JNLPFile getJNLPFile() {
        return file;
    }

    /**
     * Returns the permissions for the CodeSource.
     */
    @SuppressWarnings("ConstantConditions")
    @Override
    public PermissionCollection getPermissions(CodeSource cs) {
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
                        LOG.error("Warning! Code source security type was null");
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
            if (cs.getLocation() != null && cs.getLocation().getHost().length() > 0) {
                result.add(new SocketPermission(UrlUtils.getHostAndPort(cs.getLocation()),
                        "connect, accept"));
            }

            return result;
        } catch (RuntimeException ex) {
            LOG.error("Failed to get permissions", ex);
            throw ex;
        }
    }

    public void addPermission(Permission p) {
        runtimePermissions.add(p);
    }

    /**
     * Adds to the specified list of JARS any other JARs that need to be loaded
     * at the same time as the JARs specified (ie, are in the same part).
     *
     * @param jars jar archives to be added
     */
    private void fillInPartJars(List<JARDesc> jars) {
        final LinkedHashSet<JARDesc> result = new LinkedHashSet<>();
        for (JARDesc jar : jars) {
            result.addAll(getAllAvailableJarsInPart(jar.getPart()));
            result.remove(jar);
        }

        jars.addAll(result);
    }

    private LinkedHashSet<JARDesc> getAllAvailableJarsInPart(String part) {
        final LinkedHashSet<JARDesc> jars = new LinkedHashSet<>();

        // "available" field can be affected by two different threads
        // working in loadClass(String)
        if (part != null) {
            synchronized (available) {
                for (JARDesc jar : available) {
                    if (part.equals(jar.getPart())) {
                        jars.add(jar);
                    }
                }
            }
        }

        return jars;
    }

    /**
     * Ensures that the list of jars have all been transferred, and makes them
     * available to the classloader. If a jar contains native code, the
     * libraries will be extracted and placed in the path.
     *
     * @param jars the list of jars to load
     */
    void activateJars(final List<JARDesc> jars) {
        PrivilegedAction<Void> activate = () -> doActivateJars(jars);
        AccessController.doPrivileged(activate, acc);
    }

    private Void doActivateJars(List<JARDesc> jars) {
        // transfer the Jars
        waitForJars(jars);

        for (JARDesc jar : jars) {
            available.remove(jar);

            // add jar
            File localFile = tracker.getCacheFile(jar.getLocation());
            try {
                URL location = jar.getLocation(); // non-cacheable, use source location
                if (localFile != null) {
                    location = localFile.toURI().toURL(); // cached file
                    // This is really not the best way.. but we need some way for
                    // PluginAppletViewer::getCachedImageRef() to check if the image
                    // is available locally, and it cannot use getResources() because
                    // that prefetches the resource, which confuses MediaTracker.waitForAll()
                    // which does a wait(), waiting for notification (presumably
                    // thrown after a resource is fetched). This bug manifests itself
                    // particularly when using The FileManager applet from Webmin.
                    try (JarFile jarFile = new JarFile(localFile)) {
                        for (JarEntry je : Collections.list(jarFile.entries())) {

                            // another jar in my jar? it is more likely than you think
                            if (je.getName().endsWith(".jar")) {
                                // We need to extract that jar so that it can be loaded
                                // (inline loading with "jar:..!/..." path will not work
                                // with standard classloader methods)

                                String name = je.getName();
                                if (name.contains("..")) {
                                    name = CacheUtil.hex(name, name);
                                }
                                String extractedJarLocation = localFile + ".nested/" + name;
                                File parentDir = new File(extractedJarLocation).getParentFile();
                                if (!parentDir.isDirectory() && !parentDir.mkdirs()) {
                                    throw new RuntimeException("Unable to extract nested jar.");
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

                                tracker.addResource(new File(extractedJarLocation).toURI().toURL(), (VersionString) null);

                                URL codebase = file.getCodeBase();
                                if (codebase == null) {
                                    //FIXME: codebase should be the codebase of the Main Jar not
                                    //the location. Although, it still works in the current state.
                                    codebase = file.getResources().getMainJAR().getLocation();
                                }

                                final SecurityDesc jarSecurity = securityDelegate.getJarPermissions(codebase);

                                try {
                                    URL fileURL = new URL("file://" + extractedJarLocation);
                                    // there is no remote URL for this, so lets fake one
                                    URL fakeRemote = new URL(jar.getLocation().toString() + "!" + je.getName());
                                    CachedJarFileCallback.getInstance().addMapping(fakeRemote, fileURL);
                                    addURL(fakeRemote);

                                    jarLocationSecurityMap.put(fakeRemote, jarSecurity);

                                } catch (MalformedURLException mfue) {
                                    LOG.error("Unable to add extracted nested jar to classpath", mfue);
                                }
                            }

                            jarEntries.add(je.getName());
                        }
                    }
                }

                addURL(jar.getLocation());

                // there is currently no mechanism to cache files per
                // instance.. so only index cached files
                if (localFile != null) {
                    CachedJarFileCallback.getInstance().addMapping(jar.getLocation(), localFile.toURI().toURL());

                    try (JarFile jarFile = new JarFile(localFile.getAbsolutePath())) {
                        JarIndexAccess index = JarIndexAccess.getJarIndex(jarFile);
                        if (index != null) {
                            jarIndexes.add(index);
                        }
                    }
                } else {
                    CachedJarFileCallback.getInstance().addMapping(jar.getLocation(), jar.getLocation());
                }

                LOG.debug("Activate jar: {}", location);
            } catch (Exception ex) {
                LOG.error("Error while activating jars", ex);
            }

            // some programs place a native library in any jar
            nativeLibraryStorage.addSearchJar(jar.getLocation());
        }

        return null;
    }

    /**
     * Return the absolute path to the native library.
     */
    @Override
    protected String findLibrary(String lib) {
        String syslib = System.mapLibraryName(lib);
        File libFile = nativeLibraryStorage.findLibrary(syslib);

        if (libFile != null) {
            return libFile.toString();
        }

        String result = super.findLibrary(lib);
        if (result != null) {
            return result;
        }

        return findLibraryExt(lib);
    }

    /**
     * Try to find the library path from another peer classloader.
     *
     * @param lib library to be found
     * @return location of library
     */
    private String findLibraryExt(String lib) {
        for (JNLPClassLoader loader : loaders) {
            String result = null;

            if (loader != this) {
                result = loader.findLibrary(lib);
            }

            if (result != null) {
                return result;
            }
        }

        return null;
    }

    /**
     * Wait for a group of JARs, and send download events if there is a download
     * listener or display a progress window otherwise.
     *
     * @param jars the jars
     */
    private void waitForJars(List<JARDesc> jars) {
        URL[] urls = new URL[jars.size()];

        for (int i = 0; i < jars.size(); i++) {
            JARDesc jar = jars.get(i);

            urls[i] = jar.getLocation();
        }

        CacheUtil.waitForResources(this, tracker, urls, file.getTitle());
    }

    /**
     * Find the loaded class in this loader or any of its extension loaders.
     *
     * @param name name of class
     * @return the class found by name
     */
    private Class<?> findLoadedClassAll(String name) {
        for (JNLPClassLoader loader : loaders) {
            Class<?> result;

            if (loader == this) {
                result = JNLPClassLoader.super.findLoadedClass(name);
            } else {
                result = loader.findLoadedClassAll(name);
            }

            if (result != null) {
                return result;
            }
        }

        // Result is still null. Return what the codebase loader
        // has (which returns null if it is not loaded there either)
        if (codeBaseLoader != null) {
            return codeBaseLoader.findLoadedClassFromParent(name);
        } else {
            return null;
        }
    }

    @FunctionalInterface
    public interface ExceptionalSupplier<T, E extends Exception> {

        T call() throws E;

        default T getResultOfCallOrNull() {
            try {
                return call();
            } catch (Exception e) {
                return null;
            }
        }
    }

    /**
     * Find a JAR in the shared 'extension' classloaders, this classloader, or
     * one of the classloaders for the JNLP file's extensions. This method used
     * to be qualified "synchronized." This was done solely for the purpose of
     * ensuring only one thread entered the method at a time. This was not
     * strictly necessary - ensuring that all affected fields are thread-safe is
     * sufficient. Locking on the JNLPClassLoader instance when this method is
     * called can result in deadlock if another thread is dealing with the
     * CodebaseClassLoader at the same time. This solution is very heavy-handed
     * as the instance lock is not truly required, and taking the lock on the
     * classloader instance when not needed is not in general a good idea
     * because it can and will lead to deadlock when multithreaded classloading
     * is in effect. The solution is to keep the fields thread safe on their
     * own. This is accomplished by wrapping them in Collections.synchronized*
     * to provide atomic add/remove operations, and synchronizing on them when
     * iterating or performing multiple mutations. See bug report RH976833. On
     * some systems this bug will manifest itself as deadlock on every webpage
     * with more than one Java applet, potentially also causing the browser
     * process to hang. More information in the mailing list archives:
     * http://mail.openjdk.java.net/pipermail/distro-pkg-dev/2013-September/024536.html
     * <p>
     * Affected fields: available, classpaths, jarIndexes, jarEntries,
     * jarLocationSecurityMap
     */
    @Override
    public Class<?> loadClass(final String name) throws ClassNotFoundException {
        final List<ExceptionalSupplier<Class<?>, ClassNotFoundException>> list = new ArrayList<>();
        list.add(() -> findLoadedClassAll(name));
        list.add(() -> loadClassFromParentClassloader(name));
        list.add(() -> loadClassExt(name));
        list.add(() -> loadClassFromInternalManifestClasspath(name));
        list.add(() -> loadFromJarIndexes(name));

        return list.stream()
                .map(ExceptionalSupplier::getResultOfCallOrNull)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new ClassNotFoundException(name));
    }

    private Class<?> loadClassFromParentClassloader(final String name) throws ClassNotFoundException {
        // try parent classloader
        ClassLoader parent = getParent();
        if (parent == null) {
            parent = ClassLoader.getSystemClassLoader();
        }
        return parent.loadClass(name);
    }

    private Class<?> loadClassFromInternalManifestClasspath(final String name) throws ClassNotFoundException {
        // Look in 'Class-Path' as specified in the manifest file

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

        return loadClassExt(name);
    }

    private Class<?> loadFromJarIndexes(final String name) throws ClassNotFoundException {
        // As a last resort, look in any available indexes
        // Currently this loads jars directly from the site. We cannot cache it because this
        // call is initiated from within the applet, which does not have disk read/write permissions
        // This field synchronized before iterating over it since it may
        // be shared data between threads
        synchronized (jarIndexes) {
            for (JarIndexAccess index : jarIndexes) {
                // Non-generic code in sun.misc.JarIndex
                LinkedList<String> jarList = index.get(name.replace('.', '/'));

                if (jarList != null) {
                    for (String jarName : jarList) {
                        try {
                            final JARDesc desc = new JARDesc(new URL(file.getCodeBase(), jarName),
                                    null, null, false, true, false, true);
                            addNewJar(desc);
                        } catch (MalformedURLException mfe) {
                            LOG.debug("encountered invalid URL for {} - {}", file.getCodeBase(), jarName);
                        }
                    }

                    // If it still fails, let it error out
                    return loadClassExt(name);
                }
            }
        }
        throw new ClassNotFoundException(name);
    }

    /**
     * Adds a new JARDesc into this classloader.
     * <p>
     * This will add the JARDesc into the resourceTracker and block until it is
     * downloaded.
     * </p>
     *
     * @param desc the JARDesc for the new jar
     */
    private void addNewJar(final JARDesc desc) {
        this.addNewJar(desc, JNLPRuntime.getDefaultUpdatePolicy());
    }

    /**
     * Adds a new JARDesc into this classloader.
     *
     * @param desc         the JARDesc for the new jar
     * @param updatePolicy the UpdatePolicy for the resource
     */
    private void addNewJar(final JARDesc desc, UpdatePolicy updatePolicy) {

        available.add(desc);

        tracker.addResource(desc.getLocation(),
                desc.getVersion(),
                updatePolicy
        );

        // Give read permissions to the cached jar file
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            Permission p = getReadPermission(desc);

            resourcePermissions.add(p);

            return null;
        });

        final URL remoteURL = desc.getLocation();
        final URL cachedUrl = tracker.getCacheURL(remoteURL); // blocks till download

        available.remove(desc); // Resource downloaded. Remove from available list.

        try {
            // Decide what level of security this jar should have
            // The verification and security setting functions rely on
            // having AllPermissions as those actions normally happen
            // during initialization. We therefore need to do those
            // actions as privileged.
            AccessController.doPrivileged((PrivilegedExceptionAction<Void>) () -> {
                jcv.add(desc, tracker);

                checkTrustWithUser();

                final SecurityDesc security = securityDelegate.getJarPermissions(file.getCodeBase());

                jarLocationSecurityMap.put(remoteURL, security);

                return null;
            });

            addURL(remoteURL);
            CachedJarFileCallback.getInstance().addMapping(remoteURL, cachedUrl);

        } catch (Exception e) {
            // Do nothing. This code is called by loadClass which cannot
            // throw additional exceptions. So instead, just ignore it.
            // Exception => jar will not get added to classpath, which will
            // result in CNFE from loadClass.
            LOG.error("Failed to add jar " + desc.getLocation(), e);
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
                            (PrivilegedExceptionAction<Class<?>>) () -> JNLPClassLoader.super.findClass(fName), getAccessControlContextForClassLoading());
                } else {
                    return loader.findClass(name);
                }
            } catch (ClassNotFoundException | PrivilegedActionException ignored) {
            } catch (ClassFormatError cfe) {
                LOG.error("Error while trying to find class", cfe);
            } catch (NullJnlpFileException ex) {
                throw new ClassNotFoundException(this.mainClass + " in main classloader ", ex);
            }
        }

        // Try codebase loader
        if (codeBaseLoader != null) {
            return codeBaseLoader.findClassNonRecursive(name);
        }

        // All else failed. Throw CNFE
        throw new ClassNotFoundException(name);
    }

    /**
     * Search for the class by incrementally adding resources to the classloader
     * and its extension classloaders until the resource is found.
     */
    private Class<?> loadClassExt(String name) throws ClassNotFoundException {
        // make recursive
        addAvailable();

        // find it
        try {
            return findClass(name);
        } catch (ClassNotFoundException ignored) {
        }

        // add resources until found
        while (true) {
            JNLPClassLoader addedTo;

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

            if (addedTo == null) {
                throw new ClassNotFoundException(name);
            }

            try {
                return addedTo.findClass(name);
            } catch (ClassNotFoundException ignored) {
            }
        }
    }

    /**
     * Finds the resource in this, the parent, or the extension class loaders.
     *
     * @return a {@link URL} for the resource, or {@code null} if the resource
     * could not be found.
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
            LOG.error("Failed to find resource", e);
        }

        // If result is still null, look in the codebase loader
        if (result == null && codeBaseLoader != null) {
            result = codeBaseLoader.findResource(name);
        }

        return result;
    }

    /**
     * Find the resources in this, the parent, or the extension class loaders.
     * Load lazy resources if not found in current resources.
     */
    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        Enumeration<URL> lresources = findResourcesBySearching(name);

        try {
            // if not found, load all lazy resources; repeat search
            while (!lresources.hasMoreElements() && addNextResource() != null) {
                lresources = findResourcesBySearching(name);
            }
        } catch (LaunchException le) {
            LOG.error("Failed to load resources", le);
        }

        return lresources;
    }

    /**
     * Find the resources in this, the parent, or the extension class loaders.
     */
    private Enumeration<URL> findResourcesBySearching(String name) throws IOException {
        List<URL> lresources = new ArrayList<>();
        Enumeration<URL> e = null;

        for (JNLPClassLoader loader : loaders) {
            // TODO check if this will blow up or not
            // if loaders[1].getResource() is called, wont it call getResource() on
            // the original caller? infinite recursion?

            if (loader == this) {
                final String fName = name;
                try {
                    e = AccessController.doPrivileged((PrivilegedExceptionAction<Enumeration<URL>>) () -> JNLPClassLoader.super.findResources(fName), getAccessControlContextForClassLoading());
                } catch (PrivilegedActionException ignored) {
                }
            } else {
                e = loader.findResources(name);
            }

            final Enumeration<URL> fURLEnum = e;
            try {
                lresources.addAll(AccessController.doPrivileged(
                        new PrivilegedExceptionAction<Collection<URL>>() {
                            @Override
                            public Collection<URL> run() {
                                List<URL> resources = new ArrayList<>();
                                while (fURLEnum != null && fURLEnum.hasMoreElements()) {
                                    resources.add(fURLEnum.nextElement());
                                }
                                return resources;
                            }
                        }, getAccessControlContextForClassLoading()));
            } catch (PrivilegedActionException ignored) {
            }
        }

        // Add resources from codebase (only if nothing was found above,
        // otherwise the server will get hammered)
        if (lresources.isEmpty() && codeBaseLoader != null) {
            e = codeBaseLoader.findResources(name);
            while (e.hasMoreElements()) {
                lresources.add(e.nextElement());
            }
        }

        return Collections.enumeration(lresources);
    }

    /**
     * Adds whatever resources have already been downloaded in the background.
     */
    private void addAvailable() {
        // go through available, check tracker for it and all of its
        // part brothers being available immediately, add them.

        for (int i = 1; i < loaders.length; i++) {
            loaders[i].addAvailable();
        }
    }

    /**
     * Adds the next unused resource to the classloader. That resource and all
     * those in the same part will be downloaded and added to the classloader
     * before returning. If there are no more resources to add, the method
     * returns immediately.
     *
     * @return the classloader that resources were added to, or null
     * @throws LaunchException Thrown if the signed JNLP file, within the main
     *                         jar, fails to be verified or does not match
     */
    private JNLPClassLoader addNextResource() throws LaunchException {
        if (available.isEmpty()) {
            for (int i = 1; i < loaders.length; i++) {
                JNLPClassLoader result = loaders[i].addNextResource();

                if (result != null) {
                    return result;
                }
            }
            return null;
        }

        final List<JARDesc> jars = getNextJarsToLoad();

        checkForMain(jars);
        activateJars(jars);

        return this;
    }

    private List<JARDesc> getNextJarsToLoad() {
        final JARDesc nextJar = available.get(0);

        final LinkedHashSet<JARDesc> result = new LinkedHashSet<>();
        result.add(nextJar);
        result.addAll(getAllAvailableJarsInPart(nextJar.getPart()));

        return new ArrayList<>(result);
    }

    public boolean getSigning() {
        return signing == SigningState.FULL;
    }

    /**
     * Call this when it's suspected that an applet's permission level may have
     * just changed from Full Signing to Partial Signing. This will display a
     * one-time prompt asking the user to confirm running the partially signed
     * applet. Partially Signed applets always start off as appearing to be
     * Fully Signed, and then during the initialization or loading process, we
     * find that we actually need to demote the applet to Partial, either due to
     * finding that not all of its JARs are actually signed, or because it needs
     * to load something unsigned out of the codebase.
     */
    void checkPartialSigningWithUser() {
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

    private SigningState getSigningState() {
        return signing;
    }

    public SecurityDesc getSecurity() {
        return security;
    }

    /**
     * Returns the security descriptor for given code source URL
     *
     * @param source the origin (remote) url of the code
     * @return The SecurityDescriptor for that source
     */
    private SecurityDesc getCodeSourceSecurity(URL source) {
        SecurityDesc sec = jarLocationSecurityMap.get(source);
        synchronized (alreadyTried) {
            if (sec == null && !alreadyTried.contains(source)) {
                alreadyTried.add(source);
                //try to load the jar which is requesting the permissions, but was NOT downloaded by standard way
                LOG.info("Application is trying to get permissions for {}, which was not added by standard way. Trying to download and verify!", source.toString());
                try {
                    JARDesc des = new JARDesc(source, null, null, false, false, false, false);
                    addNewJar(des);
                    sec = jarLocationSecurityMap.get(source);
                } catch (Throwable t) {
                    LOG.error("Error while getting security", t);
                    sec = null;
                }
            }
        }
        if (sec == null) {
            LOG.info("Error: No security instance for {}. The application may have trouble continuing", source.toString());
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
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkPermission(new AllPermission());
            }
        } catch (SecurityException se) {
            throw new SecurityException("JNLPClassLoader() may only be called from trusted sources!");
        }

        // jars
        for (URL u : extLoader.getURLs()) {
            addURL(u);
        }

        // Codebase
        if (this.enableCodeBase) {
            addToCodeBaseLoader(extLoader.file.getCodeBase());
        }

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
            codeBaseLoader = new CodeBaseClassLoader(new URL[]{u}, this);
        } else {
            codeBaseLoader.addURL(u);
        }
    }


    /**
     * Increments loader use count by 1
     *
     * @throws SecurityException if caller is not trusted
     */
    private void incrementLoaderUseCount() {

        // For use by trusted code only
        if (System.getSecurityManager() != null) {
            System.getSecurityManager().checkPermission(new AllPermission());
        }

        // NB: There will only ever be one class-loader per unique-key
        synchronized (getUniqueKeyLock(file.getUniqueKey())) {
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
            final URL location = eachJar.getLocation();
            final VersionString version = eachJar.getVersion();

            try {
                tracker.removeResource(location);
            } catch (Exception e) {
                LOG.error("Failed to remove resource from tracker, continuing..", e);
            }

            Cache.deleteFromCache(location, version);
        }
    }

    /**
     * Downloads and initializes jars into this loader.
     *
     * @param ref     Path of the launch or extension JNLP File containing the
     *                resource. If null, main JNLP's file location will be used instead.
     * @param part    The name of the path.
     * @param version of jar to be downloaded
     */
    void initializeNewJarDownload(final URL ref, final String part, final VersionString version) {
        final JARDesc[] jars = ManageJnlpResources.findJars(this, ref, part, version);

        for (JARDesc eachJar : jars) {
            LOG.info("Downloading and initializing jar: {}", eachJar.getLocation().toString());

            this.addNewJar(eachJar, UpdatePolicy.FORCE);
        }
    }

    /**
     * Manages DownloadService jars which are not mentioned in the JNLP file
     *
     * @param ref     Path to the resource.
     * @param version The version of resource. If null, no version is specified.
     * @param action  The action to perform with the resource. Either
     *                DOWNLOADTOCACHE, REMOVEFROMCACHE, or CHECKCACHE.
     * @return true if CHECKCACHE and the resource is cached.
     */
    boolean manageExternalJars(final URL ref, final String version, final DownloadAction action) {
        boolean approved = false;
        JNLPClassLoader foundLoader = LocateJnlpClassLoader.getLoaderByResourceUrl(this, ref, version);
        final VersionString resourceVersion = (version == null) ? null : VersionString.fromString(version);

        if (foundLoader != null) {
            approved = true;
        } else if (ref.toString().startsWith(file.getNotNullProbableCodeBase().toString())) {
            approved = true;
        } else if (SecurityDesc.ALL_PERMISSIONS.equals(security.getSecurityType())) {
            approved = true;
        }

        if (approved) {
            if (foundLoader == null) {
                foundLoader = this;
            }

            if (action == DownloadAction.DOWNLOAD_TO_CACHE) {
                JARDesc jarToCache = new JARDesc(ref, resourceVersion, null, false, true, false, true);
                LOG.info("Downloading and initializing jar: {}", ref.toString());

                foundLoader.addNewJar(jarToCache, UpdatePolicy.FORCE);

            } else if (action == DownloadAction.REMOVE_FROM_CACHE) {
                JARDesc[] jarToRemove = {new JARDesc(ref, resourceVersion, null, false, true, false, true)};
                foundLoader.removeJars(jarToRemove);

            } else if (action == DownloadAction.CHECK_CACHE) {
                return Cache.isAnyCached(ref, resourceVersion);
            }
        }
        return false;
    }

    /**
     * Decrements loader use count by 1
     * <p>
     * If count reaches 0, loader is removed from list of available loaders
     *
     * @throws SecurityException if caller is not trusted
     */
    private void decrementLoaderUseCount() {

        // For use by trusted code only
        if (System.getSecurityManager() != null) {
            System.getSecurityManager().checkPermission(new AllPermission());
        }

        String uniqueKey = file.getUniqueKey();

        // NB: There will only ever be one class-loader per unique-key
        synchronized (getUniqueKeyLock(uniqueKey)) {
            useCount--;

            if (useCount <= 0) {
                uniqueKeyToLoader.remove(uniqueKey);
            }
        }
    }

    /**
     * Returns an appropriate AccessControlContext for loading classes in the
     * running instance.
     * <p>
     * The default context during class-loading only allows connection to
     * codebase. However applets are allowed to load jars from arbitrary
     * locations and the codebase only access falls short if a class from one
     * location needs a class from another.
     * <p>
     * Given protected access since CodeBaseClassloader uses this function too.
     *
     * @return The appropriate AccessControlContext for loading classes for this
     * instance
     */
    AccessControlContext getAccessControlContextForClassLoading() {
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
                permissions.add(new SocketPermission(UrlUtils.getHostAndPort(u),
                        "connect, accept"));
            }
        }

        // Permissions for codebase urls (if there is a loader)
        if (codeBaseLoader != null) {
            for (URL u : codeBaseLoader.getURLs()) {
                permissions.add(new SocketPermission(UrlUtils.getHostAndPort(u),
                        "connect, accept"));
            }
        }

        ProtectionDomain pd = new ProtectionDomain(null, permissions);

        return new AccessControlContext(new ProtectionDomain[]{pd});
    }

    public String getMainClass() {
        return mainClass;
    }


    public ResourceTracker getTracker() {
        return tracker;
    }

    public String getMainClassNameFromManifest(JARDesc mainJarDesc) throws IOException {
        final File f = tracker.getCacheFile(mainJarDesc.getLocation());
        if (f != null) {
            try (final JarFile mainJar = new JarFile(f)) {
                return mainJar.getManifest().getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
            }
        }
        return null;
    }
}
