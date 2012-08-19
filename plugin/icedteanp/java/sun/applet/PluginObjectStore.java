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

import java.util.HashMap;
import java.util.Map;

// Enums are the best way to implement singletons:
// Bloch, Joshua. Effective Java, 2nd Edition. Item 3, Chapter 2. ISBN: 0-321-35668-3.
enum PluginObjectStore {
    INSTANCE;

    private final Map<Integer, Object> objects = new HashMap<Integer, Object>();
    private final Map<Integer, Integer> counts = new HashMap<Integer, Integer>();
    private final Map<Object, Integer> identifiers = new HashMap<Object, Integer>();
    private final Object lock = new Object();

    private boolean wrapped = false;
    private int nextUniqueIdentifier = 1;

    public static PluginObjectStore getInstance() {
        return INSTANCE;
    }

    public Object getObject(Integer identifier) {
        synchronized(lock) {
            return objects.get(identifier);
        }
    }

    public Integer getIdentifier(Object object) {
        if (object == null)
            return 0;

        synchronized(lock) {
            return identifiers.get(object);
        }
    }

    public boolean contains(Object object) {
        if (object != null) {
            synchronized(lock) {
                return identifiers.containsKey(object);
            }
        }
        return false;

    }

    public boolean contains(int identifier) {
        synchronized(lock) {
            return objects.containsKey(identifier);
        }
    }

    private boolean checkNeg() {
        if (nextUniqueIdentifier < 1) {
            wrapped = true;
            nextUniqueIdentifier = 1;
        }
        return wrapped;
    }

    private int getNextID() {
        while (checkNeg() && objects.containsKey(nextUniqueIdentifier))
            nextUniqueIdentifier++;
        return nextUniqueIdentifier++;
    }

    public void reference(Object object) {
        synchronized(lock) {
            Integer identifier = identifiers.get(object);
            if (identifier == null) {
                int next = getNextID();
                objects.put(next, object);
                counts.put(next, 1);
                identifiers.put(object, next);
            } else {
                counts.put(identifier, counts.get(identifier) + 1);
            }
        }
    }

    public void unreference(int identifier) {
        synchronized(lock) {
            Integer currentCount = counts.get(identifier);
            if (currentCount == null) {
                return;
            }
            if (currentCount == 1) {
                Object object = objects.get(identifier);
                objects.remove(identifier);
                counts.remove(identifier);
                identifiers.remove(object);
            } else {
                counts.put(identifier, currentCount - 1);
            }
        }
    }

    public void dump() {
        synchronized(lock) {
            if (PluginDebug.DEBUG) {
                for (Map.Entry<Integer, Object> e : objects.entrySet()) {
                    PluginDebug.debug(e.getKey(), "::", e.getValue());
                }
            }
        }
    }
}
