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
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import net.sourceforge.jnlp.DownloadOptions;
import net.sourceforge.jnlp.ExtensionDesc;
import net.sourceforge.jnlp.JARDesc;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.ParseException;
import net.sourceforge.jnlp.PluginBridge;
import net.sourceforge.jnlp.ResourcesDesc;
import net.sourceforge.jnlp.SecurityDesc;
import net.sourceforge.jnlp.Version;
import net.sourceforge.jnlp.cache.CacheUtil;
import net.sourceforge.jnlp.cache.ResourceTracker;
import net.sourceforge.jnlp.cache.UpdatePolicy;
import net.sourceforge.jnlp.security.SecurityDialogs;
import net.sourceforge.jnlp.security.SecurityDialogs.AccessType;
import net.sourceforge.jnlp.tools.JarSigner;
import net.sourceforge.jnlp.util.FileUtils;
import sun.misc.JarIndex;

/**
 * Classloader that takes it's resources from a JNLP file.  If the
 * JNLP file defines extensions, separate classloaders for these
 * will be created automatically.  Classes are loaded with the
 * security context when the classloader was created.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.20 $
 */
public class JNLPClassLoader extends URLClassLoader {

    // todo: initializePermissions should get the permissions from
    // extension classes too so that main file classes can load
    // resources in an extension.

    /** map from JNLPFile url to shared classloader */
    private static Map<String, JNLPClassLoader> urlToLoader =
            new HashMap<String, JNLPClassLoader>(); // never garbage collected!

    /** the directory for native code */
    private File nativeDir = null; // if set, some native code exists

    /** a list of directories that contain native libraries */
    private List<File> nativeDirectories = Collections.synchronizedList(new LinkedList<File>());

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

    /** all jars not yet part of classloader or active */
    private List<JARDesc> available = new ArrayList<JARDesc>();

    /** all of the jar files that were verified */
    private ArrayList<String> verifiedJars = null;

    /** all of the jar files that were not verified */
    private ArrayList<String> unverifiedJars = null;

    /** the jarsigner tool to verify our jars */
    private JarSigner js = null;

    private boolean signing = false;

    /** ArrayList containing jar indexes for various jars available to this classloader */
    private ArrayList<JarIndex> jarIndexes = new ArrayList<JarIndex>();

    /** Set of classpath strings declared in the manifest.mf files */
    private Set<String> classpaths = new HashSet<String>();

    /** File entries in the jar files available to this classloader */
    private TreeSet<String> jarEntries = new TreeSet<String>();

    /** Map of specific original (remote) CodeSource Urls  to securitydesc */
    private HashMap<URL, SecurityDesc> jarLocationSecurityMap =
            new HashMap<URL, SecurityDesc>();
    
    /** Loader for codebase (which is a path, rather than a file) */
    private CodeBaseClassLoader codeBaseLoader;

    /**
     * Create a new JNLPClassLoader from the specified file.
     *
     * @param file the JNLP file
     */
    protected JNLPClassLoader(JNLPFile file, UpdatePolicy policy) throws LaunchException {
        super(new URL[0], JNLPClassLoader.class.getClassLoader());

        if (JNLPRuntime.isDebug())
            System.out.println("New classloader: " + file.getFileLocation());

        this.file = file;
        this.updatePolicy = policy;
        this.resources = file.getResources();

        // initialize extensions
        initializeExtensions();

        initializeResources();

        // initialize permissions
        initializePermissions();

        setSecurity();

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
                if (nativeDir != null) {
                    if (JNLPRuntime.isDebug()) {
                        System.out.println("Cleaning up native directory" + nativeDir.getAbsolutePath());
                    }
                    try {
                        FileUtils.recursiveDelete(nativeDir,
                                new File(System.getProperty("java.io.tmpdir")));
                    } catch (IOException e) {
                        /*
                         * failed to delete a file in tmpdir, no big deal (not
                         * to mention that the VM is shutting down at this
                         * point so no much we can do)
                         */
                    }
                }
            }
        });
    }

    private void setSecurity() throws LaunchException {

        URL codebase = null;

        if (file.getCodeBase() != null) {
            codebase = file.getCodeBase();
        } else {
            //Fixme: codebase should be the codebase of the Main Jar not
            //the location. Although, it still works in the current state.
            codebase = file.getResources().getMainJAR().getLocation();
        }

        /**
         * When we're trying to load an applet, file.getSecurity() will return
         * null since there is no jnlp file to specify permissions. We
         * determine security settings here, after trying to verify jars.
         */
        if (file instanceof PluginBridge) {
            if (signing == true) {
                this.security = new SecurityDesc(file,
                        SecurityDesc.ALL_PERMISSIONS,
                        codebase.getHost());
            } else {
                this.security = new SecurityDesc(file,
                        SecurityDesc.SANDBOX_PERMISSIONS,
                        codebase.getHost());
            }
        } else { //regular jnlp file

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
            if (!file.getSecurity().getSecurityType().equals(SecurityDesc.SANDBOX_PERMISSIONS) && !signing) {
                throw new LaunchException(file, null, R("LSFatal"), R("LCClient"), R("LUnsignedJarWithSecurity"), R("LUnsignedJarWithSecurityInfo"));
            } else if (signing == true) {
                this.security = file.getSecurity();
            } else {
                this.security = new SecurityDesc(file,
                        SecurityDesc.SANDBOX_PERMISSIONS,
                        codebase.getHost());
            }
        }
    }

    /**
     * Returns a JNLP classloader for the specified JNLP file.
     *
     * @param file the file to load classes for
     * @param policy the update policy to use when downloading resources
     */
    public static JNLPClassLoader getInstance(JNLPFile file, UpdatePolicy policy) throws LaunchException {
        JNLPClassLoader baseLoader = null;
        JNLPClassLoader loader = null;
        String uniqueKey = file.getUniqueKey();

        if (uniqueKey != null)
            baseLoader = urlToLoader.get(uniqueKey);

        try {


            // A null baseloader implies that no loader has been created 
            // for this codebase/jnlp yet. Create one.
            if (baseLoader == null ||
                    (file.isApplication() && 
                     !baseLoader.getJNLPFile().getFileLocation().equals(file.getFileLocation()))) {

                loader = new JNLPClassLoader(file, policy);

                // New loader init may have caused extentions to create a
                // loader for this unique key. Check.
                JNLPClassLoader extLoader = urlToLoader.get(uniqueKey);

                if (extLoader != null && extLoader != loader) {
                    if (loader.signing && !extLoader.signing)
                        if (!SecurityDialogs.showNotAllSignedWarningDialog(file))
                            throw new LaunchException(file, null, R("LSFatal"), R("LCClient"), R("LSignedAppJarUsingUnsignedJar"), R("LSignedAppJarUsingUnsignedJarInfo"));

                    loader.merge(extLoader);
                }

                // loader is now current + ext. But we also need to think of
                // the baseLoader
                if (baseLoader != null && baseLoader != loader) {
                    loader.merge(baseLoader);
                }

            } else {
                // if key is same and locations match, this is the loader we want
                if (!file.isApplication()) {
                    // If this is an applet, we do need to consider its loader
                    loader = new JNLPClassLoader(file, policy);

                    if (baseLoader != null)
                        baseLoader.merge(loader);
                }
                loader = baseLoader;
            }

        } catch (LaunchException e) {
            throw e;
        }

        // loaders are mapped to a unique key. Only extensions and parent
        // share a key, so it is safe to always share based on it
        urlToLoader.put(uniqueKey, loader);

        return loader;
    }

    /**
     * Returns a JNLP classloader for the JNLP file at the specified
     * location.
     *
     * @param location the file's location
     * @param version the file's version
     * @param policy the update policy to use when downloading resources
     */
    public static JNLPClassLoader getInstance(URL location, String uniqueKey, Version version, UpdatePolicy policy)
            throws IOException, ParseException, LaunchException {
        JNLPClassLoader loader = urlToLoader.get(uniqueKey);

        if (loader == null || !location.equals(loader.getJNLPFile().getFileLocation()))
            loader = getInstance(new JNLPFile(location, uniqueKey, version, false, policy), policy);

        return loader;
    }

    /**
     * Load the extensions specified in the JNLP file.
     */
    void initializeExtensions() {
        ExtensionDesc[] ext = resources.getExtensions();

        List<JNLPClassLoader> loaderList = new ArrayList<JNLPClassLoader>();

        loaderList.add(this);

        //if (ext != null) {
        for (int i = 0; i < ext.length; i++) {
            try {
                String uniqueKey = this.getJNLPFile().getUniqueKey();
                JNLPClassLoader loader = getInstance(ext[i].getLocation(), uniqueKey, ext[i].getVersion(), updatePolicy);
                loaderList.add(loader);
            } catch (Exception ex) {
                ex.printStackTrace();
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
        for (int i = 0; i < jars.length; i++) {
            Permission p = CacheUtil.getReadPermission(jars[i].getLocation(),
                                                       jars[i].getVersion());

            if (JNLPRuntime.isDebug()) {
                if (p == null)
                    System.out.println("Unable to add permission for " + jars[i].getLocation());
                else
                    System.out.println("Permission added: " + p.toString());
            }
            if (p != null)
                resourcePermissions.add(p);
        }
    }

    /**
     * Load all of the JARs used in this JNLP file into the
     * ResourceTracker for downloading.
     */
    void initializeResources() throws LaunchException {
        JARDesc jars[] = resources.getJARs();
        if (jars == null || jars.length == 0)
            return;
        /*
        if (jars == null || jars.length == 0) {
                throw new LaunchException(null, null, R("LSFatal"),
                                    R("LCInit"), R("LFatalVerification"), "No jars!");
        }
        */
        List<JARDesc> initialJars = new ArrayList<JARDesc>();

        for (int i = 0; i < jars.length; i++) {

            available.add(jars[i]);

            if (jars[i].isEager())
                initialJars.add(jars[i]); // regardless of part

            tracker.addResource(jars[i].getLocation(),
                                jars[i].getVersion(),
                                getDownloadOptionsForJar(jars[i]),
                                jars[i].isCacheable() ? JNLPRuntime.getDefaultUpdatePolicy() : UpdatePolicy.FORCE
                               );
        }

        if (strict)
            fillInPartJars(initialJars); // add in each initial part's lazy jars

        if (JNLPRuntime.isVerifying()) {

            JarSigner js;
            waitForJars(initialJars); //download the jars first.

            try {
                js = verifyJars(initialJars);
            } catch (Exception e) {
                //we caught an Exception from the JarSigner class.
                //Note: one of these exceptions could be from not being able
                //to read the cacerts or trusted.certs files.
                e.printStackTrace();
                throw new LaunchException(null, null, R("LSFatal"),
                                        R("LCInit"), R("LFatalVerification"), R("LFatalVerificationInfo"));
            }

            //Case when at least one jar has some signing
            if (js.anyJarsSigned() && js.isFullySignedByASingleCert()) {
                signing = true;

                if (!js.allJarsSigned() &&
                                    !SecurityDialogs.showNotAllSignedWarningDialog(file))
                    throw new LaunchException(file, null, R("LSFatal"), R("LCClient"), R("LSignedAppJarUsingUnsignedJar"), R("LSignedAppJarUsingUnsignedJarInfo"));

                //user does not trust this publisher
                if (!js.getAlreadyTrustPublisher()) {
                    checkTrustWithUser(js);
                } else {
                    /**
                     * If the user trusts this publisher (i.e. the publisher's certificate
                     * is in the user's trusted.certs file), we do not show any dialogs.
                     */
                }
            } else {

                signing = false;
                //otherwise this jar is simply unsigned -- make sure to ask
                //for permission on certain actions
            }
        }

        for (JARDesc jarDesc : file.getResources().getJARs()) {
            try {
                File cachedFile = tracker.getCacheFile(jarDesc.getLocation());

                if (cachedFile == null) {
                    System.err.println("JAR " + jarDesc.getLocation() + " not found. Continuing.");
                    continue; // JAR not found. Keep going.
                }

                // TODO: Should be toURI().toURL()
                URL location = cachedFile.toURL();
                SecurityDesc jarSecurity = file.getSecurity();

                if (file instanceof PluginBridge) {

                    URL codebase = null;

                    if (file.getCodeBase() != null) {
                        codebase = file.getCodeBase();
                    } else {
                        //Fixme: codebase should be the codebase of the Main Jar not
                        //the location. Although, it still works in the current state.
                        codebase = file.getResources().getMainJAR().getLocation();
                    }

                    if (signing) {
                        jarSecurity = new SecurityDesc(file,
                                                        SecurityDesc.ALL_PERMISSIONS,
                                                        codebase.getHost());
                    } else {
                        jarSecurity = new SecurityDesc(file,
                                                        SecurityDesc.SANDBOX_PERMISSIONS,
                                                        codebase.getHost());
                    }
                }

                jarLocationSecurityMap.put(jarDesc.getLocation(), jarSecurity);
            } catch (MalformedURLException mfe) {
                System.err.println(mfe.getMessage());
            }
        }

        activateJars(initialJars);
    }

    private void checkTrustWithUser(JarSigner js) throws LaunchException {
        if (!js.getRootInCacerts()) { //root cert is not in cacerts
            boolean b = SecurityDialogs.showCertWarningDialog(
                    AccessType.UNVERIFIED, file, js);
            if (!b)
                throw new LaunchException(null, null, R("LSFatal"),
                        R("LCLaunching"), R("LNotVerified"), "");
        } else if (js.getRootInCacerts()) { //root cert is in cacerts
            boolean b = false;
            if (js.noSigningIssues())
                b = SecurityDialogs.showCertWarningDialog(
                        AccessType.VERIFIED, file, js);
            else if (!js.noSigningIssues())
                b = SecurityDialogs.showCertWarningDialog(
                        AccessType.SIGNING_ERROR, file, js);
            if (!b)
                throw new LaunchException(null, null, R("LSFatal"),
                        R("LCLaunching"), R("LCancelOnUserRequest"), "");
        }
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
            if (JNLPRuntime.isDebug()) {
                Exception ex = new IllegalStateException("Application can only be set once");
                ex.printStackTrace();
            }
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
            if (cs.getCodeSigners() != null &&
                    (getCodeSourceSecurity(cs.getLocation()).getSecurityType().equals(SecurityDesc.ALL_PERMISSIONS) ||
                     getCodeSourceSecurity(cs.getLocation()).getSecurityType().equals(SecurityDesc.J2EE_PERMISSIONS))) {

                permissions = getCodeSourceSecurity(cs.getLocation()).getPermissions(cs);
            }

            Enumeration<Permission> e = permissions.elements();
            while (e.hasMoreElements())
                result.add(e.nextElement());
        }

        // add in permission to read the cached JAR files
        for (int i = 0; i < resourcePermissions.size(); i++)
            result.add(resourcePermissions.get(i));

        // add in the permissions that the user granted.
        for (int i = 0; i < runtimePermissions.size(); i++)
            result.add(runtimePermissions.get(i));

        return result;
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
        for (int i = 0; i < jars.size(); i++) {
            String part = jars.get(i).getPart();

            for (int a = 0; a < available.size(); a++) {
                JARDesc jar = available.get(a);

                if (part != null && part.equals(jar.getPart()))
                    if (!jars.contains(jar))
                        jars.add(jar);
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

                for (int i = 0; i < jars.size(); i++) {
                    JARDesc jar = jars.get(i);

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
                            Enumeration<JarEntry> e = jarFile.entries();
                            while (e.hasMoreElements()) {
                                JarEntry je = e.nextElement();

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

                                    JarSigner signer = new JarSigner();
                                    List<JARDesc> jars = new ArrayList<JARDesc>();
                                    JARDesc jarDesc = new JARDesc(new File(extractedJarLocation).toURL(), null, null, false, false, false, false);
                                    jars.add(jarDesc);
                                    tracker.addResource(new File(extractedJarLocation).toURL(), null, null, null);
                                    signer.verifyJars(jars, tracker);

                                    if (signer.anyJarsSigned() && !signer.getAlreadyTrustPublisher()) {
                                        checkTrustWithUser(signer);
                                    }

                                    try {
                                        URL fileURL = new URL("file://" + extractedJarLocation);
                                        // there is no remote URL for this, so lets fake one
                                        URL fakeRemote = new URL(jar.getLocation().toString() + "!" + je.getName());
                                        CachedJarFileCallback.getInstance().addMapping(fakeRemote, fileURL);
                                        addURL(fakeRemote);

                                        SecurityDesc jarSecurity = file.getSecurity();

                                        if (file instanceof PluginBridge) {

                                            URL codebase = null;

                                            if (file.getCodeBase() != null) {
                                                codebase = file.getCodeBase();
                                            } else {
                                                //Fixme: codebase should be the codebase of the Main Jar not
                                                //the location. Although, it still works in the current state.
                                                codebase = file.getResources().getMainJAR().getLocation();
                                            }

                                            jarSecurity = new SecurityDesc(file,
                                                    SecurityDesc.ALL_PERMISSIONS,
                                                    codebase.getHost());
                                        }

                                        jarLocationSecurityMap.put(fakeRemote, jarSecurity);

                                    } catch (MalformedURLException mfue) {
                                        if (JNLPRuntime.isDebug())
                                            System.err.println("Unable to add extracted nested jar to classpath");

                                        mfue.printStackTrace();
                                    }
                                }

                                jarEntries.add(je.getName());
                            }

                        }

                        addURL(jar.getLocation());

                        // there is currently no mechanism to cache files per
                        // instance.. so only index cached files
                        if (localFile != null) {
                            CachedJarFileCallback.getInstance().addMapping(jar.getLocation(), localFile.toURL());

                            JarFile jarFile = new JarFile(localFile.getAbsolutePath());
                            Manifest mf = jarFile.getManifest();
                            classpaths.addAll(getClassPathsFromManifest(mf, jar.getLocation().getPath()));
                            JarIndex index = JarIndex.getJarIndex(jarFile, null);
                            if (index != null)
                                jarIndexes.add(index);
                        } else {
                            CachedJarFileCallback.getInstance().addMapping(jar.getLocation(), jar.getLocation());
                        }

                        if (JNLPRuntime.isDebug())
                            System.err.println("Activate jar: " + location);
                    }
                    catch (Exception ex) {
                        if (JNLPRuntime.isDebug())
                            ex.printStackTrace();
                    }

                    // some programs place a native library in any jar
                    activateNative(jar);
                }

                return null;
            }
        };

        AccessController.doPrivileged(activate, acc);
    }

    /**
     * Search for and enable any native code contained in a JAR by copying the
     * native files into the filesystem. Called in the security context of the
     * classloader.
     */
    protected void activateNative(JARDesc jar) {
        if (JNLPRuntime.isDebug())
            System.out.println("Activate native: " + jar.getLocation());

        File localFile = tracker.getCacheFile(jar.getLocation());
        if (localFile == null)
            return;

        String[] librarySuffixes = { ".so", ".dylib", ".jnilib", ".framework", ".dll" };

        try {
            JarFile jarFile = new JarFile(localFile, false);
            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry e = entries.nextElement();

                if (e.isDirectory()) {
                    continue;
                }

                String name = new File(e.getName()).getName();
                boolean isLibrary = false;

                for (String suffix : librarySuffixes) {
                    if (name.endsWith(suffix)) {
                        isLibrary = true;
                        break;
                    }
                }
                if (!isLibrary) {
                    continue;
                }

                if (nativeDir == null)
                    nativeDir = getNativeDir();

                File outFile = new File(nativeDir, name);
                if (!outFile.isFile()) {
                    FileUtils.createRestrictedFile(outFile, true);
                }
                CacheUtil.streamCopy(jarFile.getInputStream(e),
                                     new FileOutputStream(outFile));

            }
        } catch (IOException ex) {
            if (JNLPRuntime.isDebug())
                ex.printStackTrace();
        }
    }

    /**
     * Return the base directory to store native code files in.
     * This method does not need to return the same directory across
     * calls.
     */
    protected File getNativeDir() {
        final int rand = (int)((Math.random()*2 - 1) * Integer.MAX_VALUE);
        nativeDir = new File(System.getProperty("java.io.tmpdir")
                             + File.separator + "netx-native-"
                             + (rand & 0xFFFF));
        File parent = nativeDir.getParentFile();
        if (!parent.isDirectory() && !parent.mkdirs()) {
            return null;
        }

        try {
            FileUtils.createRestrictedDirectory(nativeDir);
            // add this new native directory to the search path
            addNativeDirectory(nativeDir);
            return nativeDir;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Adds the {@link File} to the search path of this {@link JNLPClassLoader}
     * when trying to find a native library
     */
    protected void addNativeDirectory(File nativeDirectory) {
        nativeDirectories.add(nativeDirectory);
    }

    /**
     * Returns a list of all directories in the search path of the current classloader
     * when it tires to find a native library.
     * @return a list of directories in the search path for native libraries
     */
    protected List<File> getNativeDirectories() {
        return nativeDirectories;
    }

    /**
     * Return the absolute path to the native library.
     */
    protected String findLibrary(String lib) {
        String syslib = System.mapLibraryName(lib);

        for (File dir : getNativeDirectories()) {
            File target = new File(dir, syslib);
            if (target.exists())
                return target.toString();
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
        for (int i = 0; i < loaders.length; i++) {
            String result = null;

            if (loaders[i] != this)
                result = loaders[i].findLibrary(lib);

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
    private void waitForJars(List jars) {
        URL urls[] = new URL[jars.size()];

        for (int i = 0; i < jars.size(); i++) {
            JARDesc jar = (JARDesc) jars.get(i);

            urls[i] = jar.getLocation();
        }

        CacheUtil.waitForResources(app, tracker, urls, file.getTitle());
    }

    /**
         * Verifies code signing of jars to be used.
         *
         * @param jars the jars to be verified.
         */
    private JarSigner verifyJars(List<JARDesc> jars) throws Exception {

        js = new JarSigner();
        js.verifyJars(jars, tracker);
        return js;
    }

    /**
     * Find the loaded class in this loader or any of its extension loaders.
     */
    protected Class findLoadedClassAll(String name) {
        for (int i = 0; i < loaders.length; i++) {
            Class result = null;

            if (loaders[i] == this)
                result = super.findLoadedClass(name);
            else
                result = loaders[i].findLoadedClassAll(name);

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
     */
    public synchronized Class<?> loadClass(String name) throws ClassNotFoundException {

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
                    for (String classpath: classpaths) {
                        JARDesc desc;
                        try {
                            URL jarUrl = new URL(file.getCodeBase(), classpath);
                            desc = new JARDesc(jarUrl, null, null, false, true, false, true);
                        } catch (MalformedURLException mfe) {
                            throw new ClassNotFoundException(name, mfe);
                        }
                        addNewJar(desc);
                    }

                    result = loadClassExt(name);
                    return result;
                } catch (ClassNotFoundException cnfe1) {
                    if (JNLPRuntime.isDebug()) {
                        cnfe1.printStackTrace();
                    }
                }

                // As a last resort, look in any available indexes

                // Currently this loads jars directly from the site. We cannot cache it because this
                // call is initiated from within the applet, which does not have disk read/write permissions
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
                                if (JNLPRuntime.isDebug()) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        // If it still fails, let it error out
                        result = loadClassExt(name);
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
     * @param desc the JARDesc for the new jar
     */
    private void addNewJar(JARDesc desc) {

        available.add(desc);

        tracker.addResource(desc.getLocation(),
                desc.getVersion(),
                null,
                JNLPRuntime.getDefaultUpdatePolicy()
                );

        URL remoteURL = desc.getLocation();
        URL cachedUrl = tracker.getCacheURL(remoteURL);
        addURL(remoteURL);
        CachedJarFileCallback.getInstance().addMapping(remoteURL, cachedUrl);
    }

    /**
     * Find the class in this loader or any of its extension loaders.
     */
    protected Class findClass(String name) throws ClassNotFoundException {
        for (int i = 0; i < loaders.length; i++) {
            try {
                if (loaders[i] == this)
                    return super.findClass(name);
                else
                    return loaders[i].findClass(name);
            } catch (ClassNotFoundException ex) {
            } catch (ClassFormatError cfe) {
            }
        }

        // Try codebase loader
        if (codeBaseLoader != null)
            return codeBaseLoader.findClass(name);
        
        // All else failed. Throw CNFE
        throw new ClassNotFoundException(name);
    }

    /**
     * Search for the class by incrementally adding resources to the
     * classloader and its extension classloaders until the resource
     * is found.
     */
    private Class loadClassExt(String name) throws ClassNotFoundException {
        // make recursive
        addAvailable();

        // find it
        try {
            return findClass(name);
        } catch (ClassNotFoundException ex) {
        }

        // add resources until found
        while (true) {
            JNLPClassLoader addedTo = addNextResource();

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
     */
    public URL getResource(String name) {
        URL result = super.getResource(name);

        for (int i = 1; i < loaders.length; i++)
            if (result == null)
                result = loaders[i].getResource(name);
        
        // If result is still null, look in the codebase loader
        if (result == null && codeBaseLoader != null)
            result = codeBaseLoader.getResource(name);

        return result;
    }

    /**
     * Finds the resource in this, the parent, or the extension
     * class loaders.
     */
    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        Vector<URL> resources = new Vector<URL>();
        Enumeration<URL> e;

        for (int i = 0; i < loaders.length; i++) {

            if (loaders[i] == this)
                e = super.findResources(name);
            else
                e = loaders[i].findResources(name);

            while (e.hasMoreElements())
                resources.add(e.nextElement());
        }

        // Add resources from codebase (only if nothing was found above, 
        // otherwise the server will get hammered) 
        if (resources.isEmpty() && codeBaseLoader != null) {
            e = codeBaseLoader.findResources(name);
            while (e.hasMoreElements())
                resources.add(e.nextElement());
        }

        return resources.elements();
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
     */
    protected JNLPClassLoader addNextResource() {
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
        return jarLocationSecurityMap.get(source);
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
        for (File nativeDirectory : extLoader.getNativeDirectories())
            addNativeDirectory(nativeDirectory);

        // security descriptors
        for (URL key : extLoader.jarLocationSecurityMap.keySet()) {
            jarLocationSecurityMap.put(key, extLoader.jarLocationSecurityMap.get(key));
        }
    }

    /**
     * Adds the given path to the path loader
     * 
     * @param URL the path to add
     * @throws IllegalArgumentException If the given url is not a path
     */
    private void addToCodeBaseLoader(URL u) {

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

    private DownloadOptions getDownloadOptionsForJar(JARDesc jar) {
        return file.getDownloadOptionsForJar(jar);
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

    /*
     * Helper class to expose protected URLClassLoader methods.
     */

    public static class CodeBaseClassLoader extends URLClassLoader {

        JNLPClassLoader parentJNLPClassLoader;
        
        public CodeBaseClassLoader(URL[] urls, JNLPClassLoader cl) {
            super(urls);
            parentJNLPClassLoader = cl;
        }

        @Override
        public void addURL(URL url) { 
            super.addURL(url); 
        }

        @Override
        public Class<?> findClass(String name) throws ClassNotFoundException { 
            return super.findClass(name); 
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
            if (!name.startsWith("META-INF")) {
                return super.findResources(name);
            }
            return (new Vector<URL>(0)).elements();
        }

        @Override
        public URL findResource(String name) {
            if (!name.startsWith("META-INF")) {
                return super.findResource(name);
            }
            return null;
        }
    }
}
