/* TimedHashMapTest.java
   Copyright (C) 2014 Red Hat, Inc.

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

public class TimedHashMapTest {

    private TimedHashMap<Object, Object> testMap;
    private Object o1, o2, o3, o4;

    @Before
    public void resetTestMap() {
        testMap = new TimedHashMap<>();
        o1 = new Object();
        o2 = new Object();
        o3 = new Object();
        o4 = new Object();
    }

    @Test
    public void testPutAndGet() {
        testMap.put(o1, o2);
        testMap.put(o2, o4);
        testMap.put(o3, o4);
        assertEquals("map[o1] != o2", o2, testMap.get(o1));
        assertEquals("map[o2] != o4", o4, testMap.get(o2));
        assertEquals("map[o3] != o4", o4, testMap.get(o3));
        testMap.put(o1, o3);
        assertEquals("map[o1] != o3", o3, testMap.get(o1));
    }

    @Test
    public void testEntryExpiry() throws Exception {
        testMap.setTimeout(0, TimeUnit.NANOSECONDS); // immediate expiry
        testMap.put(o1, o2);
        Thread.sleep(5); // so we don't manage to put and get in the same nanosecond
        assertNull("map[o1] should have expired", testMap.get(o1));
    }

    @Test(expected = NullPointerException.class)
    public void testPutNullKey() {
        testMap.put(null, o1);
    }

    @Test
    public void testPutNullValue() {
        testMap.put(o1, null);
        assertNull("map[o1] != null", testMap.get(o1));
        assertTrue("testMap should contain the key o1", testMap.containsKey(o1));
    }

    @Test
    public void testContainsKey() {
        testMap.put(o1, o2);
        assertTrue("testMap should contain the key o1", testMap.containsKey(o1));
    }

    @Test
    public void testSize() {
        assertEquals(0, testMap.size());
        testMap.put(o1, o2);
        assertEquals(1, testMap.size());
    }

    @Test
    public void testIsEmpty() {
        assertTrue("map should be empty", testMap.isEmpty());
        testMap.put(o1, o2);
        assertFalse("map should not be empty", testMap.isEmpty());
    }

    @Test
    public void testContainsValue() {
        assertFalse("map should not contain o2", testMap.containsValue(o2));
        testMap.put(o1, o2);
        assertTrue("map does not contain o2", testMap.containsValue(o2));
    }

    @Test
    public void testContainsValueNull() {
        assertFalse("map should not contain null value", testMap.containsValue(null));
        testMap.put(o1, null);
        assertTrue("map does not contain null value", testMap.containsValue(null));
    }

    @Test
    public void testRemove() {
        testMap.put(o1, o2);
        o3 = testMap.remove(o1);
        assertEquals("o2 != o3", o2, o3);
        assertFalse("map should not contain o1", testMap.containsKey(o1));
    }

    @Test
    public void testRemoveFromEmpty() {
        o2 = testMap.remove(o1);
        assertNull("o2 should be null", o2);
    }

    @Test
    public void testPutAll() {
        final Map<Object, Object> newMap = new HashMap<>();
        newMap.put(o1, o2);
        newMap.put(o3, o4);
        testMap.putAll(newMap);
        assertTrue("map should contain key o1", testMap.containsKey(o1));
        assertTrue("map should contain value o2", testMap.containsValue(o2));
        assertTrue("map should contain key o3", testMap.containsKey(o3));
        assertTrue("map should contain value o4", testMap.containsValue(o4));
        assertEquals("map[o1] != o2", o2, testMap.get(o1));
        assertEquals("map[o3] != o4", o4, testMap.get(o3));
        assertEquals(2, testMap.size());
    }

    @Test
    public void testClear() {
        testMap.put(o1, o2);
        testMap.clear();
        assertEquals(0, testMap.size());
        assertFalse("map should not contain key o1", testMap.containsKey(o1));
        assertFalse("map should not contain value o2", testMap.containsValue(o2));
    }

    @Test
    public void testKeySet() {
        testMap.put(o1, o2);
        Set<Object> keys = testMap.keySet();
        assertNotNull("keyset should not be null", keys);
        assertTrue("keyset should contain o1", keys.contains(o1));
        assertEquals(1, keys.size());
    }

    @Test
    public void testValues() {
        testMap.put(o1, o2);
        Collection<Object> values = testMap.values();
        assertNotNull("values collection should not be null", values);
        assertTrue("values collection should contain o2", values.contains(o2));
        assertEquals(1, values.size());
    }

    @Test
    public void testEntrySet() {
        testMap.put(o1, o2);
        testMap.put(o3, o4);
        Set<Map.Entry<Object, Object>> entrySet = testMap.entrySet();
        assertNotNull("entryset should not be null", entrySet);
        assertEquals(2, entrySet.size());
        for (final Map.Entry<Object, Object> entry : entrySet) {
            final Object key = entry.getKey();
            final Object value = entry.getValue();
            if (key.equals(o1)) {
                assertEquals("entry with key o1 should have value o2", o2, value);
            }
            if (key.equals(o3)) {
                assertEquals("entry with key o3 should have value o4", o4, value);
            }
        }
    }

}
