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
    private final File[] jarFiles;

    public DummyJNLPFileWithJar(File... jarFiles) throws MalformedURLException {
        this(-1, jarFiles);
    }
    public DummyJNLPFileWithJar(int main, File... jarFiles) throws MalformedURLException {
        codeBase = jarFiles[0].getParentFile().toURI().toURL();
        this.jarFiles = jarFiles;
        jarDescs = new JARDesc[jarFiles.length];

        for (int i = 0; i < jarFiles.length; i++) {
            jarDescs[i] = makeJarDesc(jarFiles[i].toURI().toURL(), i==main);

        }
        info = new ArrayList<InformationDesc>();
    }

    public URL getJarLocation() {
        try {
            return jarFiles[0].toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public URL getJarLocation(int i) {
        try {
            return jarFiles[i].toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
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

    @Override
    public SecurityDesc getSecurity() {
        return new SecurityDesc(this, SecurityDesc.SANDBOX_PERMISSIONS, null);
    }

    public void setInfo(List<InformationDesc> info) {
        this.info = info;
    }
    
    
}