package net.sourceforge.jnlp.runtime;

import java.net.URL;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Collection;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.security.SecurityDesc;
import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.runtime.JNLPClassLoader.SecurityDelegate;

public class DummySecurityDelegate implements SecurityDelegate {

    public boolean isPluginApplet() {
        return false;
    }

    public boolean userPromptedForPartialSigning() {
        return false;
    }

    public boolean userPromptedForSandbox() {
        return false;
    }

    public SecurityDesc getCodebaseSecurityDesc(JARDesc jarDesc, URL codebaseHost) {
        return null;
    }

    public SecurityDesc getClassLoaderSecurity(URL codebaseHost) throws LaunchException {
        return null;
    }

    public SecurityDesc getJarPermissions(URL codebaseHost) {
        return null;
    }

    public void promptUserOnPartialSigning() throws LaunchException {
    }

    public void setRunInSandbox() throws LaunchException {
    }

    public boolean getRunInSandbox() {
        return false;
    }

    public void addPermission(Permission perm) {
    }

    public void addPermissions(PermissionCollection perms) {
    }

    public void addPermissions(Collection<Permission> perms) {
    }
}
