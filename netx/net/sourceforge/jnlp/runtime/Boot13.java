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

import java.lang.reflect.*;
import java.net.*;
import java.security.*;

/**
 * Allows a Policy and SecurityManager to be set in JRE1.3 without
 * running the code with only applet permissions; this class is
 * for backward compatibility only and is totally unnecessary if
 * running in jdk 1.4 or later (can call Boot directly).
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.5 $
 */
public class Boot13 extends URLClassLoader {

    // The problem with setting a Policy in jdk1.3 is that the
    // system and application classes seem to be loaded in such a
    // way that only their protection domain determines the
    // permissions; the policy object is never asked for permissions
    // after the class is loaded.  This hack creates a classloader
    // that loads duplicate versions of the classes in such a
    // manner where they ask with the policy object.  The jdk1.4
    // correctly honors the Policy object making this unneccessary
    // post-1.3.

    private Boot13(URL source[]) {
        super(source);
    }

    protected PermissionCollection getPermissions(CodeSource source) {
        Permissions result = new Permissions();
        result.add(new AllPermission());

        return result;
    }

    public Class loadClass(String name) throws ClassNotFoundException {
        Class c = findLoadedClass(name);
        if (c != null)
            return c;

        // reverse the search order so that classes from this
        // classloader, which sets the right permissions, are found
        // before the parent classloader which has the same classes
        // but the wrong permissions.
        try {
            return findClass(name);
        } catch (ClassNotFoundException ex) {
        }

        return getParent().loadClass(name);
    }

    public static void main(final String args[]) throws Exception {
        URL cs = Boot13.class.getProtectionDomain().getCodeSource().getLocation();
        //  instead of using a custom loadClass search order, we could
        //  put the classes in a boot/ subdir of the JAR and load
        //  them from there.  This would be an improvement by not
        //  allowing applications to get a duplicate jnlp engine (one
        //  with applet access permissions) by using the system
        //  classloader but a drawback by not allowing Boot to be
        //  called directly.
        //cs = new URL("jar:"+cs+"!/boot/");

        if (cs == null) {
            System.err.println("fatal: cannot determine code source.");
            System.exit(1);
        }

        Boot13 b = new Boot13(new URL[] { cs });

        Thread.currentThread().setContextClassLoader(b); // try to prevent getting the non-policy version of classes

        Class<?> c = b.loadClass("net.sourceforge.jnlp.runtime.Boot");
        Method main = c.getDeclaredMethod("main", new Class<?>[] { String[].class });

        main.invoke(null, new Object[] { args });
    }

}
