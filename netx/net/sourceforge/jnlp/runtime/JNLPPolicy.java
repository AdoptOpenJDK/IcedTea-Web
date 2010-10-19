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

import java.security.*;
import java.util.Enumeration;

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

    /** classes from this source have all permissions */
    private static CodeSource shellSource;

    /** classes from this source have all permissions */
    private static CodeSource systemSource;

    /** the previous policy */
    private static Policy systemPolicy;


    protected JNLPPolicy() {
        shellSource = JNLPPolicy.class.getProtectionDomain().getCodeSource();
        systemSource = Policy.class.getProtectionDomain().getCodeSource();
        systemPolicy = Policy.getPolicy();
    }

    /**
     * Return a mutable, heterogeneous-capable permission collection
     * for the source.
     */
    public PermissionCollection getPermissions(CodeSource source) {
        if (source.equals(systemSource) || source.equals(shellSource))
            return getAllPermissions();

        // if we check the SecurityDesc here then keep in mind that
        // code can add properties at runtime to the ResourcesDesc!
        if (JNLPRuntime.getApplication() != null) {
                if (JNLPRuntime.getApplication().getClassLoader() instanceof JNLPClassLoader) {
                        JNLPClassLoader cl = (JNLPClassLoader) JNLPRuntime.getApplication().getClassLoader();

                        PermissionCollection clPermissions = cl.getPermissions(source);

                        // systempolicy permissions need to be accounted for as well
                        CodeSource appletCS = new CodeSource(JNLPRuntime.getApplication().getJNLPFile().getSourceLocation(), (java.security.cert.Certificate[]) null);
                        Enumeration e = systemPolicy.getPermissions(appletCS).elements();
                while (e.hasMoreElements())
                    clPermissions.add((Permission) e.nextElement());

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
        // no op
    }

    /**
     * Return an all-permissions collection.
     */
    private Permissions getAllPermissions() {
        Permissions result = new Permissions();

        result.add( new AllPermission() );
        return result;
    }

        public boolean implies(ProtectionDomain domain, Permission permission) {
                //Include the permissions that may be added during runtime.
                PermissionCollection pc = getPermissions(domain.getCodeSource());
                return super.implies(domain, permission) || pc.implies(permission);
        }
}
