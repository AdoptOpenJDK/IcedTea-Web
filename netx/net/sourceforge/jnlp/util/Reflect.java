// Copyright (C) 2003 Jon A. Maxwell (JAM)
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

package net.sourceforge.jnlp.util;

import java.lang.reflect.Method;
import net.sourceforge.jnlp.util.logging.OutputController;


/**
 * Provides simply, convenient methods to invoke methods by
 * name. This class is used to consolidate reflection needed to
 * access methods specific to Sun's JVM or to remain backward
 * compatible while supporting method in newer JVMs.
 * <p>
 * Most methods of this class invoke the first method on the
 * specified object that matches the name and number of
 * parameters. The type of the parameters are not considered, so
 * do not attempt to use this class to invoke overloaded
 * methods.
 * </p>
 * <p>
 * Instances of this class are not synchronized.
 * </p>
 *
 * @author <a href="mailto:jon.maxwell@acm.org">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.1 $
 */
public class Reflect {

    // todo: check non-null parameter types, try to send to proper
    // method if overloaded ones exist on the target object

    // todo: optimize slightly using hashtable of Methods

    private boolean accessible;

    private static Object zero[] = new Object[0];

    /**
     * Create a new Reflect instance.
     */
    public Reflect() {
        //
    }

    /**
     * Create a new Reflect instance.
     *
     * @param accessible whether to bypass access permissions
     */
    public Reflect(boolean accessible) {
        this.accessible = accessible;
    }

    /**
     * Invoke a zero-parameter static method by name.
     */
    public Object invokeStatic(String className, String method) {
        return invokeStatic(className, method, zero);
    }

    /**
     * Invoke the static method using the specified parameters.
     */
    public Object invokeStatic(String className, String method, Object args[]) {
        try {
            Class<?> c = Class.forName(className, true, Reflect.class.getClassLoader());

            Method m = getMethod(c, method, args);
            if (m.isAccessible() != accessible) {
                m.setAccessible(accessible);
            }

            return m.invoke(null, args);
        } catch (Exception ex) { // eat
            return null;
        }
    }

    /**
     * Invoke a zero-parameter method by name on the specified
     * object.
     */
    public Object invoke(Object object, String method) {
        return invoke(object, method, zero);
    }

    /**
     * Invoke a method by name with the specified parameters.
     *
     * @return the result of the method, or null on exception.
     */
    public Object invoke(Object object, String method, Object args[]) {
        try {
            Method m = getMethod(object.getClass(), method, args);
            if (m.isAccessible() != accessible) {
                m.setAccessible(accessible);
            }

            return m.invoke(object, args);
        } catch (Exception ex) { // eat
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, ex);
            return null;
        }
    }

    /**
     * Return the Method matching the specified name and number of
     * arguments.
     */
    public Method getMethod(Class<?> type, String method, Object args[]) {
        try {
            for (Class<?> c = type; c != null; c = c.getSuperclass()) {
                Method methods[] = c.getMethods();

                for (int i = 0; i < methods.length; i++) {
                    if (methods[i].getName().equals(method)) {
                        Class parameters[] = methods[i].getParameterTypes();

                        if (parameters.length == args.length) {
                            return methods[i];
                        }
                    }
                }
            }
        } catch (Exception ex) { // eat
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, ex);
        }

        return null;
    }

}
