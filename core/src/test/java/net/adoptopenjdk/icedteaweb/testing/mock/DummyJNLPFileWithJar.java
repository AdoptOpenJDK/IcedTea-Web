package net.adoptopenjdk.icedteaweb.testing.mock;

import net.adoptopenjdk.icedteaweb.jnlp.element.information.InformationDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.ResourcesDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.security.AppletPermissionLevel;
import net.adoptopenjdk.icedteaweb.jnlp.element.security.SecurityDesc;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.sourceforge.jnlp.JNLPFile;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/* A mocked dummy JNLP file with a single JAR. */
public class DummyJNLPFileWithJar extends JNLPFile {

    /* Create a JARDesc for the given URL location */
    private static JARDesc makeJarDesc(final URL jarLocation, final boolean main) {
        return new JARDesc(jarLocation, VersionString.fromString("1"), null, false,main, false,false);
    }

    private final JARDesc[] jarDescs;
    private final URL[] jarFiles;

    public DummyJNLPFileWithJar(final File... jarFiles) throws MalformedURLException {
        this(-1, jarFiles);
    }
    
    public DummyJNLPFileWithJar(final URL codebaseRewritter, final URL... jarFiles) {
        this(-1, codebaseRewritter, jarFiles);
    }
    public DummyJNLPFileWithJar(final int main, final File... jarFiles) throws MalformedURLException {
        this(main, jarFiles[0].getParentFile().toURI().toURL(), filesToUrls(jarFiles));

    }
    
    private static URL[] filesToUrls(final File[] f) throws MalformedURLException{
        final URL[] r = new URL[f.length];
        for (int i = 0; i < f.length; i++) {
            r[i]=f[i].toURI().toURL();
        }
        return r;
    }
    
    private DummyJNLPFileWithJar(final int main, final URL codebaseRewritter, final URL... jarFiles) {
        codeBase = codebaseRewritter;
        this.jarFiles = jarFiles;
        jarDescs = new JARDesc[jarFiles.length];

        for (int i = 0; i < jarFiles.length; i++) {
            jarDescs[i] = makeJarDesc(jarFiles[i], i==main);

        }
        infos = new ArrayList<>();
        this.security = new SecurityDesc(this, AppletPermissionLevel.NONE, SecurityDesc.SANDBOX_PERMISSIONS, null);
    }

    public URL getJarLocation() {
            return jarFiles[0];
    }

    public JARDesc[] getJarDescs() {
        return jarDescs;
    }
    
    public JARDesc getJarDesc() {
        return jarDescs[0];
    }


    @Override
    public ResourcesDesc getResources() {
        final ResourcesDesc localResources = new ResourcesDesc(null, new Locale[0], new String[0], new String[0]);
        for (final JARDesc j : jarDescs) {
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

    public void setInfo(final List<InformationDesc> infos) {
        this.infos = infos;
    }
    
    
}