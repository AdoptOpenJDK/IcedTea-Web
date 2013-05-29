package net.sourceforge.jnlp.mock;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
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
    static JARDesc makeJarDesc(URL jarLocation) {
        return new JARDesc(jarLocation, new Version("1"), null, false,false, false,false);
    }

    public URL codeBase, jarLocation;
    public JARDesc jarDesc;

    public DummyJNLPFileWithJar(File jarFile) throws MalformedURLException {
        codeBase = jarFile.getParentFile().toURI().toURL();
        jarLocation = jarFile.toURI().toURL();
        jarDesc = makeJarDesc(jarLocation); 
        info = new ArrayList<InformationDesc>();
    }

    @Override
    public ResourcesDesc getResources() {
        ResourcesDesc resources = new ResourcesDesc(null, new Locale[0], new String[0], new String[0]);
        resources.addResource(jarDesc);
        return resources;
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
}