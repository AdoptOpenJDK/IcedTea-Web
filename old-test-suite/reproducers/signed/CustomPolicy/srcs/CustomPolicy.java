/* CustomPolicy.java
Copyright (C) 2012 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 2.

IcedTea is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to
the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version.
 */

import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.cert.Certificate;
import java.security.AccessControlException;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;

public class CustomPolicy {
    public static AccessControlContext strictAccessControlContext() throws MalformedURLException {
        CodeSource code = new CodeSource(new URL("http://localhost"), (Certificate[]) null);
        ProtectionDomain pd = new ProtectionDomain(code, new Permissions(),
                null, null);
        return new AccessControlContext(new ProtectionDomain[] { pd });
    }

    public static void main(String[] args) throws PrivilegedActionException, MalformedURLException {
        final Policy defaultPolicy = Policy.getPolicy();
        Policy.setPolicy(new Policy() {
            public PermissionCollection getPermissions(CodeSource codesource) {
                System.out.println("Loading System here may cause problems.");
                return defaultPolicy.getPermissions(codesource);
            }

            public void refresh() {
                defaultPolicy.refresh();
            }
        });

        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
                public Void run() {
                    Thread.currentThread().setContextClassLoader(null);
                    return null;
                }
            }, strictAccessControlContext());
        } catch (AccessControlException ace) {
            System.out.println("AccessControlException: Cannot set context class loader");
        }
        System.out.println("Program Executed Correctly");
    }
}
