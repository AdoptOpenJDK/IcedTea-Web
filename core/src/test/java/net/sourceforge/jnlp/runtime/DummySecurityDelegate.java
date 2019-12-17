package net.sourceforge.jnlp.runtime;

import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.security.SecurityDesc;
import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.runtime.classloader.JNLPClassLoader.SecurityDelegate;

import java.net.URL;
import java.security.Permission;
import java.util.Collection;

public class DummySecurityDelegate implements SecurityDelegate {

    @Override
    public SecurityDesc getCodebaseSecurityDesc(JARDesc jarDesc, URL codebaseHost) {
        return null;
    }

    @Override
    public SecurityDesc getClassLoaderSecurity(URL codebaseHost) throws LaunchException {
        return null;
    }

    @Override
    public SecurityDesc getJarPermissions(URL codebaseHost) {
        return null;
    }

    @Override
    public void promptUserOnPartialSigning() throws LaunchException {
    }

    @Override
    public void setRunInSandbox() throws LaunchException {
    }

    @Override
    public boolean getRunInSandbox() {
        return false;
    }

    @Override
    public void addPermissions(Collection<Permission> perms) {
    }
}
