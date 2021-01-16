package net.sourceforge.jnlp.runtime;

import net.sourceforge.jnlp.LaunchException;

import java.security.Permission;
import java.util.Collection;

public class DummySecurityDelegate implements SecurityDelegate {
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
