/* TimedHashMap.java
   Copyright (C) 2011 Red Hat, Inc.

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

package net.sourceforge.jnlp.util;

import net.sourceforge.jnlp.util.logging.OutputController;
import java.util.HashMap;

import net.sourceforge.jnlp.runtime.JNLPRuntime;

/**
 * Simple utility class that extends HashMap by adding an expiry to the entries.
 *
 * This map stores entries, and returns them only if the entries were last accessed within time t=10 seconds
 *
 * @param K The key type
 * @param V The Object type
 */
public class TimedHashMap<K, V> {

    HashMap<K,V> actualMap = new HashMap<K,V>();
    HashMap<K, Long> timeStamps = new HashMap<K, Long>();
    Long expiry = 10000000000L;

    /**
     * Store the item in the map and associate a timestamp with it
     *
     * @param key The key
     * @param value The value to store
     */
    public V put(K key, V value) {
        timeStamps.put(key, System.nanoTime());
        return actualMap.put(key, value);
    }

    /**
     * Return cached item if it has not already expired.
     *
     * Before returning, this method also resets the "last accessed"
     * time for this entry, so it is good for another 10 seconds
     *
     * @param key The key
     */
    public V get(K key) {
        Long now = System.nanoTime();

        if (actualMap.containsKey(key)) {
            Long age = now - timeStamps.get(key);

            // Item exists. If it has not expired, renew its access time and return it
            if (age <= expiry) {
                OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "Returning proxy " + actualMap.get(key) + " from cache for " + key);
                timeStamps.put(key, System.nanoTime());
                return actualMap.get(key);
            } else {
                OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "Proxy cache for " + key + " has expired (age=" + (age * 1e-9) + " seconds)");
            }
        }

        return null;
    }
}

