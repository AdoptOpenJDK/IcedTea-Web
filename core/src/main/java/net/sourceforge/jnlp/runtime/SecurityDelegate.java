package net.sourceforge.jnlp.runtime;

import net.sourceforge.jnlp.LaunchException;

import java.security.Permission;
import java.util.Collection;

/**
 * SecurityDelegate, in real usage, relies on having a "parent"
 * JNLPClassLoader instance. However, JNLPClassLoaders are very large,
 * heavyweight, difficult-to-mock objects, which means that unit testing on
 * anything that uses a SecurityDelegate can become very difficult. For
 * example, JarCertVerifier is designed separated from the ClassLoader so it
 * can be tested in isolation. However, JCV needs some sort of access back
 * to JNLPClassLoader instances to be able to invoke setRunInSandbox(). The
 * SecurityDelegate handles this, allowing JCV to be tested without
 * instantiating JNLPClassLoaders, by creating a fake SecurityDelegate that
 * does not require one.
 */
public interface SecurityDelegate {

    void promptUserOnPartialSigning() throws LaunchException;

    void setRunInSandbox() throws LaunchException;

    boolean getRunInSandbox();

    void addPermissions(final Collection<Permission> perms);
}
