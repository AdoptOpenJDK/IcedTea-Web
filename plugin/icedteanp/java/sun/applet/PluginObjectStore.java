/* PluginObjectStore -- manage identifier-to-object mapping
   Copyright (C) 2008  Red Hat

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
exception statement from your version. */

package sun.applet;

import java.util.*;
import java.lang.reflect.*;
import java.io.*;

public class PluginObjectStore
{
    private static HashMap<Integer, Object> objects = new HashMap();
    private static HashMap<Integer, Integer> counts = new HashMap();
    private static HashMap<Object, Integer> identifiers = new HashMap();
    // FIXME:
    //
    // IF uniqueID == MAX_LONG, uniqueID =
    // 0 && wrapped = true
    //
    // if (wrapped), check if
    // objects.get(uniqueID) returns null
    //
    // if yes, use uniqueID, if no,
    // uniqueID++ and keep checking
    // or:
    // stack of available ids:
    // derefed id -> derefed id -> nextUniqueIdentifier
    private static int nextUniqueIdentifier = 1;

    public Object getObject(Integer identifier) {
        return objects.get(identifier);
    }

    public Integer getIdentifier(Object object) {
        if (object == null)
            return 0;
        return identifiers.get(object);
    }
    
    public boolean contains(Object object) {
    	if (object == null)
    		return identifiers.containsKey(object);

    	return false;
    }
    
    public boolean contains(int identifier) {
   		return objects.containsKey(identifier);
    }

    public void reference(Object object) {
        Integer identifier = identifiers.get(object);
        if (identifier == null) {
            objects.put(nextUniqueIdentifier, object);
            counts.put(nextUniqueIdentifier, 1);
            identifiers.put(object, nextUniqueIdentifier);
            //System.out.println("JAVA ADDED: " + nextUniqueIdentifier);
            //System.out.println("JAVA REFERENCED: " + nextUniqueIdentifier
            //                   + " to: 1");
            nextUniqueIdentifier++;
        } else {
            counts.put(identifier, counts.get(identifier) + 1);
            //System.out.println("JAVA REFERENCED: " + identifier +
            //                   " to: " + counts.get(identifier));
        }
    }

    public void unreference(int identifier) {
        Integer currentCount = counts.get(identifier);
        if (currentCount == null) {
            //System.out.println("ERROR UNREFERENCING: " + identifier);
            return;
        }
        if (currentCount == 1) {
            //System.out.println("JAVA DEREFERENCED: " + identifier
            //                   + " to: 0");
            Object object = objects.get(identifier);
            objects.remove(identifier);
            counts.remove(identifier);
            identifiers.remove(object);
            //System.out.println("JAVA REMOVED: " + identifier);
        } else {
            counts.put(identifier, currentCount - 1);
            //System.out.println("JAVA DEREFERENCED: " +
            //                   identifier + " to: " +
            //                   counts.get(identifier));
        }
    }

    public void dump() {
   		Iterator i = objects.keySet().iterator();
   		while (i.hasNext()) {
   			Object key = i.next();
   			PluginDebug.debug(key + "::" +  objects.get(key));
   		}
    }
}

