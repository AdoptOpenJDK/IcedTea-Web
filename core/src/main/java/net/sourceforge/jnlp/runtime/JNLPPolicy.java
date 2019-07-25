// Copyright (C) 2001-2003 Jon A. Maxwell (JAM)
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

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.config.PathsAndFiles;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.NoSuchAlgorithmException;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.security.URIParameter;
import java.util.Enumeration;

import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.JAVA_HOME;

/**
 * Policy for JNLP environment.  This class delegates to the
 * system policy but always grants permissions to the JNLP code
 * and system CodeSources (no separate policy file needed).  This
 * class may also grant permissions to applications at runtime if
 * approved by the user.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.7 $
 */
public class JNLPPolicy extends Policy {

    private static final Logger LOG = LoggerFactory.getLogger(JNLPPolicy.class);

    /**
     * classes from this source have all permissions
     */
    private static CodeSource shellSource;

    /**
     * classes from this source have all permissions
     */
    private static CodeSource systemSource;

    /**
     * the previous policy
     */
    private static Policy systemPolicy;

    private final URI jreExtDir;

    /**
     * the system level policy for jnlps
     */
    private Policy systemJnlpPolicy = null;

    /**
     * the user-level policy for jnlps
     */
    private Policy userJnlpPolicy = null;

    protected JNLPPolicy() {
        shellSource = JNLPPolicy.class.getProtectionDomain().getCodeSource();
        systemSource = Policy.class.getProtectionDomain().getCodeSource();
        systemPolicy = Policy.getPolicy();

        systemJnlpPolicy = getPolicyFromConfig(ConfigurationConstants.KEY_SYSTEM_SECURITY_POLICY);
        userJnlpPolicy = getPolicyFromUrl(PathsAndFiles.JAVA_POLICY.getFullPath());

        String jre = System.getProperty(JAVA_HOME);
        jreExtDir = (new File(jre + File.separator + "lib" + File.separator + "ext")).toURI();
    }

    /**
     * Return a mutable, heterogeneous-capable permission collection
     * for the source.
     */
    public PermissionCollection getPermissions(CodeSource source) {
        if (source.equals(systemSource) || source.equals(shellSource))
            return getAllPermissions();

        if (isSystemJar(source)) {
            return getAllPermissions();
        }

        // if we check the SecurityDesc here then keep in mind that
        // code can add properties at runtime to the ResourcesDesc!
        if (JNLPRuntime.getApplication() != null) {
            if (JNLPRuntime.getApplication().getClassLoader() instanceof JNLPClassLoader) {
                JNLPClassLoader cl = (JNLPClassLoader) JNLPRuntime.getApplication().getClassLoader();

                PermissionCollection clPermissions = cl.getPermissions(source);

                Enumeration<Permission> e;
                CodeSource appletCS = new CodeSource(JNLPRuntime.getApplication().getJNLPFile().getSourceLocation(), (java.security.cert.Certificate[]) null);

                // systempolicy permissions need to be accounted for as well
                e = systemPolicy.getPermissions(appletCS).elements();
                while (e.hasMoreElements()) {
                    clPermissions.add(e.nextElement());
                }

                // and so do permissions from the jnlp-specific system policy
                if (systemJnlpPolicy != null) {
                    e = systemJnlpPolicy.getPermissions(appletCS).elements();
                    while (e.hasMoreElements()) {
                        clPermissions.add(e.nextElement());
                    }
                }

                // and permissions from jnlp-specific user policy too
                if (userJnlpPolicy != null) {
                    e = userJnlpPolicy.getPermissions(appletCS).elements();
                    while (e.hasMoreElements()) {
                        clPermissions.add(e.nextElement());
                    }

                    CodeSource appletCodebaseSource = new CodeSource(JNLPRuntime.getApplication().getJNLPFile().getCodeBase(), (java.security.cert.Certificate[]) null);
                    e = userJnlpPolicy.getPermissions(appletCodebaseSource).elements();
                    while (e.hasMoreElements()) {
                        clPermissions.add(e.nextElement());
                    }
                }

                return clPermissions;
            }
        }

        // delegate to original Policy object; required to run under WebStart
        return systemPolicy.getPermissions(source);
    }

    /**
     * Refresh.
     */
    public void refresh() {
        if (userJnlpPolicy != null) {
            userJnlpPolicy.refresh();
        }
    }

    /**
     * Return an all-permissions collection.
     */
    private Permissions getAllPermissions() {
        Permissions result = new Permissions();

        result.add(new AllPermission());
        return result;
    }

    /**
     * Returns true if the CodeSource corresponds to a system jar. That is,
     * it's part of the JRE.
     */
    private boolean isSystemJar(final CodeSource source) {
        if (source == null || source.getLocation() == null) {
            return false;
        }

        // anything in JRE/lib/ext is a system jar and has full permissions
        final String sourceProtocol = source.getLocation().getProtocol();
        final String sourcePath = source.getLocation().getPath();
        final String jreExtDirRawPath = jreExtDir.getRawPath();

        if (sourceProtocol.toUpperCase().equals("FILE") &&
                sourcePath.toLowerCase().startsWith(jreExtDirRawPath.toLowerCase())) {
            return true;
        }

        // check to see if source protocol is a Java System Library protocol
        if (sourceProtocol.equalsIgnoreCase("jrt")) {
            // jrt protocols are only for system code return true 
            return true;
        }

        return false;
    }

    /**
     * Constructs a delegate policy based on a config setting
     *
     * @param key a KEY_* in DeploymentConfiguration
     * @return a policy based on the configuration set by the user
     */
    private Policy getPolicyFromConfig(String key) {
        DeploymentConfiguration config = JNLPRuntime.getConfiguration();
        String policyLocation = config.getProperty(key);
        return getPolicyFromUrl(policyLocation);
    }

    /**
     * Loads a policy from a URI
     *
     * @param policyLocation the URI of the policy
     * @return a policy based on the configuration set by the user
     */
    private Policy getPolicyFromUrl(String policyLocation) {
        Policy policy = null;
        if (policyLocation != null) {
            try {
                final URI policyUri;
                if (policyLocation.startsWith("file://")) {
                    policyUri = new File(policyLocation).toURI();
                } else {
                    policyUri = new URI(policyLocation.replace("\\", "/"));
                }
                policy = getInstance("JavaPolicy", new URIParameter(policyUri));
            } catch (IllegalArgumentException | NoSuchAlgorithmException | URISyntaxException e) {
                LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
            }
        }
        return policy;
    }

    public boolean implies(ProtectionDomain domain, Permission permission) {
        //Include the permissions that may be added during runtime.
        PermissionCollection pc = getPermissions(domain.getCodeSource());
        return super.implies(domain, permission) || pc.implies(permission);
    }
}
