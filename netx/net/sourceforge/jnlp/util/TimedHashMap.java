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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;
import net.sourceforge.jnlp.util.logging.OutputController;

/**
 * Simple utility class that extends HashMap by adding an expiry to the entries.
 *
 * This map stores entries, and returns them only if the entries were last accessed within a specified timeout period.
 * Otherwise, null is returned.
 * 
 * This map does not allow null keys but does allow null values.
 *
 * @param K The key type
 * @param V The Object type
 */
public class TimedHashMap<K, V> implements Map<K, V> {

    private static class TimedEntry<T> {
        private final T value;
        private long timestamp;

        public TimedEntry(final T value) {
            this.value = value;
            updateTimestamp();
        }

        public void updateTimestamp() {
            timestamp = System.nanoTime();
        }
    }

    private static final long DEFAULT_TIMEOUT = TimeUnit.SECONDS.toNanos(10);

    private final HashMap<K, TimedEntry<V>> actualMap = new HashMap<>();
    private long timeout = DEFAULT_TIMEOUT;

    public TimedHashMap() {
        this(DEFAULT_TIMEOUT, TimeUnit.NANOSECONDS);
    }

    /**
     * Create a new map with a non-default entry timeout period
     * @param unit the units of the timeout
     * @param timeout the length of the timeout
     */
    public TimedHashMap(final long timeout, final TimeUnit unit) {
        setTimeout(timeout, unit);
    }

    /**
     * Specify how long (in nanoseconds) entries are valid for
     * @param unit the units of the timeout
     * @param timeout the length of the timeout
     */
    public void setTimeout(final long timeout, final TimeUnit unit) {
        this.timeout = unit.toNanos(timeout);
    }

    /**
     * Store the item in the map and associate a timestamp with it. null is not accepted as a key.
     *
     * @param key The key
     * @param value The value to store
     */
    @Override
    public V put(final K key, final V value) {
        requireNonNull(key);
        final TimedEntry<V> oldEntry = actualMap.get(key);
        final V oldValue;
        if (oldEntry != null) {
            oldValue = oldEntry.value;
        } else {
            oldValue = null;
        }
        actualMap.put(key, new TimedEntry<>(value));
        return oldValue;
    }

    /**
     * Return cached item if it has not already expired.
     *
     * Before returning, this method also resets the "last accessed"
     * time for this entry, so it is good for another 10 seconds
     *
     * @param key The key
     */
    @Override
    public V get(final Object key) {
        final long now = System.nanoTime();

        if (actualMap.containsKey(key)) {
            final TimedEntry<V> timedEntry = actualMap.get(key);
            final long age = now - timedEntry.timestamp;

            // Item exists. If it has not expired, renew its access time and return it
            if (age <= timeout) {
                OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "Returning entry " + actualMap.get(key) + " from cache for " + key);
                timedEntry.updateTimestamp();
                return timedEntry.value;
            } else {
                OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "Cached entry for " + key + " has expired (age=" + (age * 1e-9) + " seconds)");
            }
        }

        return null;
    }

    @Override
    public boolean containsKey(final Object key) {
        return actualMap.containsKey(key);
    }

    @Override
    public int size() {
        return actualMap.size();
    }

    @Override
    public boolean isEmpty() {
        return actualMap.isEmpty();
    }

    @Override
    public boolean containsValue(final Object value) {
        for (final TimedEntry<V> entry : actualMap.values()) {
            if (Objects.equals(entry.value, value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public V remove(final Object key) {
        if (actualMap.containsKey(key)) {
            return actualMap.remove(key).value;
        }
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (final Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
            actualMap.put(entry.getKey(), new TimedEntry<V>(entry.getValue()));
        }
    }

    @Override
    public void clear() {
        actualMap.clear();
    }

    @Override
    public Set<K> keySet() {
        return new HashSet<>(actualMap.keySet());
    }

    @Override
    public Collection<V> values() {
        final Collection<V> values = new ArrayList<>(actualMap.size());
        for (final TimedEntry<V> value : actualMap.values()) {
            values.add(value.value);
        }
        return values;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        final Map<K, V> strippedMap = new HashMap<>(actualMap.size());
        for (final Map.Entry<K, TimedEntry<V>> entry : actualMap.entrySet()) {
            strippedMap.put(entry.getKey(), entry.getValue().value);
        }
        return strippedMap.entrySet();
    }

}

