package net.sourceforge.jnlp.mock;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.sourceforge.jnlp.InformationDesc;
import net.sourceforge.jnlp.JARDesc;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.ResourcesDesc;
import net.sourceforge.jnlp.SecurityDesc;
import net.sourceforge.jnlp.Version;

/* A mocked dummy JNLP file with a single JAR. */
public class DummyJNLPFileWithJar extends JNLPFile {

    /* Create a JARDesc for the given URL location */
    static JARDesc makeJarDesc(URL jarLocation, boolean main) {
        return new JARDesc(jarLocation, new Version("1"), null, false,main, false,false);
    }

    private final JARDesc[] jarDescs;
    private final URL[] jarFiles;

    public DummyJNLPFileWithJar(File... jarFiles) throws MalformedURLException {
        this(-1, jarFiles);
    }
    
    public DummyJNLPFileWithJar(URL codebaseRewritter, URL... jarFiles) throws MalformedURLException {
        this(-1, codebaseRewritter, jarFiles);
    }
    public DummyJNLPFileWithJar(int main, File... jarFiles) throws MalformedURLException {
        this(main, jarFiles[0].getParentFile().toURI().toURL(), filesToUrls(jarFiles));

    }
    
    private static URL[] filesToUrls(File[] f) throws MalformedURLException{
        URL[] r = new URL[f.length];
        for (int i = 0; i < f.length; i++) {
            r[i]=f[i].toURI().toURL();
        }
        return r;
    }
    
    public DummyJNLPFileWithJar(int main, URL codebaseRewritter, URL... jarFiles) throws MalformedURLException {
        codeBase = codebaseRewritter;
        this.jarFiles = jarFiles;
        jarDescs = new JARDesc[jarFiles.length];

        for (int i = 0; i < jarFiles.length; i++) {
            jarDescs[i] = makeJarDesc(jarFiles[i], i==main);

        }
        info = new ArrayList<>();
        this.security = new SecurityDesc(this, SecurityDesc.SANDBOX_PERMISSIONS, null);
    }

    public URL getJarLocation() {
            return jarFiles[0];
    }
    
    public URL getJarLocation(int i) {
            return jarFiles[i];
    }

    public JARDesc[] getJarDescs() {
        return jarDescs;
    }
    
    public JARDesc getJarDesc() {
        return jarDescs[0];
    }

    public JARDesc getJarDesc(int i) {
        return jarDescs[i];
    }
    
        
    @Override
    public ResourcesDesc getResources() {
        ResourcesDesc localResources = new ResourcesDesc(null, new Locale[0], new String[0], new String[0]);
        for (JARDesc j : jarDescs) {
            localResources.addResource(j);            
        }
        return localResources;
    }
    @Override
    public ResourcesDesc[] getResourcesDescs(final Locale locale, final String os, final String arch) {
        return new ResourcesDesc[] { getResources() };
    }

    @Override
    public URL getCodeBase() {
        return codeBase;
    }

    public void setInfo(List<InformationDesc> info) {
        this.info = info;
    }
    
    
}