/*
   PrivilegeManager.java
   Copyright (C) 2011  Red Hat

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

IcedTea is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
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

/**
 *
 * This class does not implement any functionality and exists for backward 
 * compatibility only.
 *
 * At one point Netscape required applets to request specific permissions to
 * do things. This is not longer the case with IcedTea-Web (and other modern
 * plug-ins). However because some old applets may still have code calling 
 * this class, an empty stub is needed to prevent a ClassNotFoundException.
 *
 */

package netscape.security;

import sun.applet.PluginDebug;

public class PrivilegeManager {

    /**
     * Stub for enablePrivilege. Not used by IcedTea-Web, kept for compatibility
     * 
     * @param privilege
     */
    public static void  enablePrivilege(String privilege) {
        PluginDebug.debug("netscape.security.enablePrivilege stub called");
    }
    
    /**
     * Stub for disablePrivilege. Not used by IcedTea-Web, kept for compatibility
     * 
     * @param privilege
     */
    public static void  disablePrivilege(String privilege) {
        PluginDebug.debug("netscape.security.disablePrivilege stub called");
    }
}
