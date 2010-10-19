
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeSet;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
import net.sourceforge.jnlp.security.SecurityWarningDialog;
import net.sourceforge.jnlp.tools.JarSigner;
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

    /** shortcut for resources */
    private static String R(String key) { return JNLPRuntime.getMessage(key); }

    /** map from JNLPFile url to shared classloader */
    private static Map urlToLoader = new HashMap(); // never garbage collected!

    /** the directory for native code */
    private File nativeDir = null; // if set, some native code exists

    /** a list of directories that contain native libraries */
    private List<File> nativeDirectories = Collections.synchronizedList(new LinkedList<File>());

    /** security context */
    private AccessControlContext acc = AccessController.getContext();

    /** the permissions for the cached jar files */
    private List resourcePermissions;

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
    private List available = new ArrayList();

        /** all of the jar files that were verified */
        private ArrayList<String> verifiedJars = null;

        /** all of the jar files that were not verified */
        private ArrayList<String> unverifiedJars = null;

        /** the jarsigner tool to verify our jars */
        private JarSigner js = null;

        private boolean signing = false;

        /** ArrayList containing jar indexes for various jars available to this classloader */
        private ArrayList<JarIndex> jarIndexes = new ArrayList<JarIndex>();

        /** File entries in the jar files available to this classloader */
        private TreeSet jarEntries = new TreeSet();

        /** Map of specific codesources to securitydesc */
        private HashMap<URL, SecurityDesc> jarLocationSecurityMap = new HashMap<URL, SecurityDesc>();

    /**
     * Create a new JNLPClassLoader from the specified file.
     *
     * @param file the JNLP file
     */
    protected JNLPClassLoader(JNLPFile file, UpdatePolicy policy) throws LaunchException {
        super(new URL[0], JNLPClassLoader.class.getClassLoader());

        if (JNLPRuntime.isDebug())
            System.out.println("New classloader: "+file.getFileLocation());

        this.file = file;
        this.updatePolicy = policy;
        this.resources = file.getResources();

        // initialize extensions
        initializeExtensions();

        // initialize permissions
        initializePermissions();

        initializeResources();

        setSecurity();

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
            }
            else if (signing == true) {
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
            baseLoader = (JNLPClassLoader) urlToLoader.get(uniqueKey);

                try {

                    // If base loader is null, or the baseloader's file and this
                    // file is different, initialize a new loader
                    if (baseLoader == null ||
                        !baseLoader.getJNLPFile().getFileLocation().equals(file.getFileLocation())) {

                        loader = new JNLPClassLoader(file, policy);

                        // New loader init may have caused extentions to create a
                        // loader for this unique key. Check.
                        JNLPClassLoader extLoader = (JNLPClassLoader) urlToLoader.get(uniqueKey);

                        if (extLoader != null && extLoader != loader) {
                            if (loader.signing && !extLoader.signing)
                                if (!SecurityWarningDialog.showNotAllSignedWarningDialog(file))
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
        JNLPClassLoader loader = (JNLPClassLoader) urlToLoader.get(uniqueKey);

        if (loader == null || !location.equals(loader.getJNLPFile().getFileLocation()))
            loader = getInstance(new JNLPFile(location, uniqueKey, version, false, policy), policy);

        return loader;
    }

    /**
     * Load the extensions specified in the JNLP file.
     */
    void initializeExtensions() {
        ExtensionDesc[] ext = resources.getExtensions();

        List loaderList = new ArrayList();

        loaderList.add(this);

                //if (ext != null) {
                for (int i=0; i < ext.length; i++) {
                try {
                    String uniqueKey = this.getJNLPFile().getUniqueKey();
                    JNLPClassLoader loader = getInstance(ext[i].getLocation(), uniqueKey, ext[i].getVersion(), updatePolicy);
                    loaderList.add(loader);
                }
                catch (Exception ex) {
                        ex.printStackTrace();
                }
                }
                //}

        loaders = (JNLPClassLoader[]) loaderList.toArray(new JNLPClassLoader[ loaderList.size()]);
    }

    /**
     * Make permission objects for the classpath.
     */
    void initializePermissions() {
        resourcePermissions = new ArrayList();

        JARDesc jars[] = resources.getJARs();
        for (int i=0; i < jars.length; i++) {
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
        List initialJars = new ArrayList();

        for (int i=0; i < jars.length; i++) {

            available.add(jars[i]);

            if (jars[i].isEager())
                initialJars.add(jars[i]); // regardless of part

            tracker.addResource(jars[i].getLocation(),
                                jars[i].getVersion(),
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
                        if (js.anyJarsSigned()){
                                signing = true;

                                if (!js.allJarsSigned() &&
                                    !SecurityWarningDialog.showNotAllSignedWarningDialog(file))
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

                for (JARDesc jarDesc: file.getResources().getJARs()) {
                        try {
                                URL location = tracker.getCacheFile(jarDesc.getLocation()).toURI().toURL();
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

                                jarLocationSecurityMap.put(location, jarSecurity);
                        } catch (MalformedURLException mfe) {
                                System.err.println(mfe.getMessage());
                        }
                }

        activateJars(initialJars);
    }

    private void checkTrustWithUser(JarSigner js) throws LaunchException {
        if (!js.getRootInCacerts()) { //root cert is not in cacerts
            boolean b = SecurityWarningDialog.showCertWarningDialog(
                SecurityWarningDialog.AccessType.UNVERIFIED, file, js);
            if (!b)
                throw new LaunchException(null, null, R("LSFatal"),
                    R("LCLaunching"), R("LNotVerified"), "");
        } else if (js.getRootInCacerts()) { //root cert is in cacerts
            boolean b = false;
            if (js.noSigningIssues())
                b = SecurityWarningDialog.showCertWarningDialog(
                        SecurityWarningDialog.AccessType.VERIFIED, file, js);
            else if (!js.noSigningIssues())
                b = SecurityWarningDialog.showCertWarningDialog(
                        SecurityWarningDialog.AccessType.SIGNING_ERROR, file, js);
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
        addURL( file.getCodeBase() ); // nothing happens if called more that once?
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
                     getCodeSourceSecurity(cs.getLocation()).getSecurityType().equals(SecurityDesc.J2EE_PERMISSIONS))
                    ) {

                permissions = getCodeSourceSecurity(cs.getLocation()).getPermissions();
            }

            Enumeration<Permission> e = permissions.elements();
            while (e.hasMoreElements())
                result.add(e.nextElement());
        }

        // add in permission to read the cached JAR files
        for (int i=0; i < resourcePermissions.size(); i++)
            result.add((Permission) resourcePermissions.get(i));

        // add in the permissions that the user granted.
        for (int i=0; i < runtimePermissions.size(); i++)
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
    protected void fillInPartJars(List jars) {
        for (int i=0; i < jars.size(); i++) {
            String part = ((JARDesc) jars.get(i)).getPart();

            for (int a=0; a < available.size(); a++) {
                JARDesc jar = (JARDesc) available.get(a);

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
    protected void activateJars(final List jars) {
        PrivilegedAction activate = new PrivilegedAction() {

            public Object run() {
                // transfer the Jars
                waitForJars(jars);

                for (int i=0; i < jars.size(); i++) {
                    JARDesc jar = (JARDesc) jars.get(i);

                    available.remove(jar);

                    // add jar
                    File localFile = tracker.getCacheFile(jar.getLocation());
                    try {
                        URL location = jar.getLocation(); // non-cacheable, use source location
                        if (localFile != null) {
                            location = localFile.toURL(); // cached file

                            // This is really not the best way.. but we need some way for
                            // PluginAppletViewer::getCachedImageRef() to check if the image
                            // is available locally, and it cannot use getResources() because
                            // that prefetches the resource, which confuses MediaTracker.waitForAll()
                            // which does a wait(), waiting for notification (presumably
                            // thrown after a resource is fetched). This bug manifests itself
                            // particularly when using The FileManager applet from Webmin.

                            JarFile jarFile = new JarFile(localFile);
                            Enumeration e = jarFile.entries();
                            while (e.hasMoreElements()) {

                                JarEntry je = (JarEntry) e.nextElement();

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
                                    signer.verifyJar(extractedJarLocation);

                                    if (signer.anyJarsSigned() && !signer.getAlreadyTrustPublisher()) {
                                        checkTrustWithUser(signer);
                                    }

                                    try {
                                        URL fileURL = new URL("file://" + extractedJarLocation);
                                        addURL(fileURL);

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

                                        jarLocationSecurityMap.put(fileURL, jarSecurity);

                                     } catch (MalformedURLException mfue) {
                                        if (JNLPRuntime.isDebug())
                                            System.err.println("Unable to add extracted nested jar to classpath");

                                        mfue.printStackTrace();
                                    }
                                }

                                jarEntries.add(je.getName());
                            }

                        }

                        addURL(location);

                        // there is currently no mechanism to cache files per
                        // instance.. so only index cached files
                        if (localFile != null) {
                            JarIndex index = JarIndex.getJarIndex(new JarFile(localFile.getAbsolutePath()), null);

                            if (index != null)
                                jarIndexes.add(index);
                        }

                        if (JNLPRuntime.isDebug())
                            System.err.println("Activate jar: "+location);
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
            System.out.println("Activate native: "+jar.getLocation());

        File localFile = tracker.getCacheFile(jar.getLocation());
        if (localFile == null)
            return;

        if (nativeDir == null)
            nativeDir = getNativeDir();

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

                for (String suffix: librarySuffixes) {
                    if (name.endsWith(suffix)) {
                       isLibrary = true;
                       break;
                    }
                }
                if (!isLibrary) {
                    continue;
                }

                File outFile = new File(nativeDir, name);

                CacheUtil.streamCopy(jarFile.getInputStream(e),
                                     new FileOutputStream(outFile));
            }
        }
        catch (IOException ex) {
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
        nativeDir = new File(System.getProperty("java.io.tmpdir")
                             + File.separator + "netx-native-"
                             + (new Random().nextInt() & 0xFFFF));

        if (!nativeDir.mkdirs())
            return null;
        else {
            // add this new native directory to the search path
            addNativeDirectory(nativeDir);
            return nativeDir;
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

        for (File dir: getNativeDirectories()) {
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
        for (int i=0; i < loaders.length; i++) {
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

        for (int i=0; i < jars.size(); i++) {
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
        for (int i=0; i < loaders.length; i++) {
            Class result = null;

            if (loaders[i] == this)
                result = super.findLoadedClass(name);
            else
                result = loaders[i].findLoadedClassAll(name);

            if (result != null)
                return result;
        }

        return null;
    }

    /**
     * Find a JAR in the shared 'extension' classloaders, this
     * classloader, or one of the classloaders for the JNLP file's
     * extensions.
     */
    public Class loadClass(String name) throws ClassNotFoundException {

        Class result = findLoadedClassAll(name);

        // try parent classloader
        if (result == null) {
            try {
                ClassLoader parent = getParent();
                if (parent == null)
                    parent = ClassLoader.getSystemClassLoader();

                return parent.loadClass(name);
            }
            catch (ClassNotFoundException ex) { }
        }

        // filter out 'bad' package names like java, javax
        // validPackage(name);

        // search this and the extension loaders
        if (result == null)
            try {
                result = loadClassExt(name);
            } catch (ClassNotFoundException cnfe) {

                // Not found in external loader either. As a last resort, look in any available indexes

                // Currently this loads jars directly from the site. We cannot cache it because this
                // call is initiated from within the applet, which does not have disk read/write permissions
                for (JarIndex index: jarIndexes) {
                    LinkedList<String> jarList = index.get(name.replace('.', '/'));

                    if (jarList != null) {
                        for (String jarName: jarList) {
                            JARDesc desc;
                            try {
                                desc = new JARDesc(new URL(file.getCodeBase(), jarName),
                                        null, null, false, true, false, true);
                            } catch (MalformedURLException mfe) {
                                throw new ClassNotFoundException(name);
                            }

                            available.add(desc);

                            tracker.addResource(desc.getLocation(),
                                    desc.getVersion(),
                                    JNLPRuntime.getDefaultUpdatePolicy()
                            );

                            URL remoteURL;
                            try {
                                remoteURL = new URL(file.getCodeBase() + jarName);
                            } catch (MalformedURLException mfe) {
                                throw new ClassNotFoundException(name);
                            }

                            URL u;

                            try {
                                u = tracker.getCacheURL(remoteURL);
                            } catch (Exception e) {
                                throw new ClassNotFoundException(name);
                            }

                            if (u != null)
                                addURL(u);

                        }

                        // If it still fails, let it error out
                        result = loadClassExt(name);
                    }
                }
            }

        return result;
    }

    /**
     * Find the class in this loader or any of its extension loaders.
     */
    protected Class findClass(String name) throws ClassNotFoundException {
        for (int i=0; i < loaders.length; i++) {
            try {
                if (loaders[i] == this)
                    return super.findClass(name);
                else
                    return loaders[i].findClass(name);
            }
            catch(ClassNotFoundException ex) { }
            catch(ClassFormatError cfe) {}
        }

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
        }
        catch(ClassNotFoundException ex) {
        }

        // add resources until found
        while (true) {
            JNLPClassLoader addedTo = addNextResource();

            if (addedTo == null)
                throw new ClassNotFoundException(name);

            try {
                return addedTo.findClass(name);
            }
            catch(ClassNotFoundException ex) {
            }
        }
    }

    /**
     * Finds the resource in this, the parent, or the extension
     * class loaders.
     */
    public URL getResource(String name) {
        URL result = super.getResource(name);

        for (int i=1; i < loaders.length; i++)
            if (result == null)
                result = loaders[i].getResource(name);

        return result;
    }

    /**
     * Finds the resource in this, the parent, or the extension
     * class loaders.
     */
    public Enumeration findResources(String name) throws IOException {
        Vector resources = new Vector();

        for (int i=0; i < loaders.length; i++) {
            Enumeration e;

            if (loaders[i] == this)
                e = super.findResources(name);
            else
                e = loaders[i].findResources(name);

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

        for (int i=1; i < loaders.length; i++) {
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
            for (int i=1; i < loaders.length; i++) {
                JNLPClassLoader result = loaders[i].addNextResource();

                if (result != null)
                    return result;
            }
            return null;
        }

        // add jar
        List jars = new ArrayList();
        jars.add(available.get(0));

        fillInPartJars(jars);


                activateJars(jars);

        return this;
    }

    // this part compatibility with previous classloader
    /**
     * @deprecated
     */
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
         * @param source The code source
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

                // native search paths
        for (File nativeDirectory: extLoader.getNativeDirectories())
            addNativeDirectory(nativeDirectory);

        // security descriptors
                for (URL key: extLoader.jarLocationSecurityMap.keySet()) {
                        jarLocationSecurityMap.put(key, extLoader.jarLocationSecurityMap.get(key));
                }
        }
}
