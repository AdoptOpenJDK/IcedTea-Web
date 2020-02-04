package net.adoptopenjdk.icedteaweb.classloader;

import net.adoptopenjdk.icedteaweb.classloader.JnlpApplicationClassLoader.LoadableJar;

import java.util.List;

/**
 * Responsible for validating the trust we have in the application.
 * <ul>
 *     <li>Jar Signatures</li>
 *     <li>JNLP Signature</li>
 *     <li>Certificates used for signing</li>
 *     <li>Content of manifest</li>
 * </ul>
 */
public interface ApplicationTrustValidator {
    void validateJars(List<LoadableJar> jars);
}
