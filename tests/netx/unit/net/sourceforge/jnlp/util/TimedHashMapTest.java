/*
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

import org.junit.Test;

import static org.junit.Assert.*;

public class TimedHashMapTest {

    @Test
    public void testPutAndGet() {
        final TimedHashMap<Object, Object> map = new TimedHashMap<>();
        final Object o1 = new Object(), o2 = new Object(), o3 = new Object(), o4 = new Object();
        map.put(o1, o2);
        map.put(o2, o4);
        map.put(o3, o4);
        assertEquals(o2, map.get(o1));
        assertEquals(o4, map.get(o2));
        assertEquals(o4, map.get(o3));
        map.put(o1, o3);
        assertEquals(o3, map.get(o1));
    }

    @Test
    public void testEntryExpiry() throws Exception {
        final TimedHashMap<Object, Object> map = new TimedHashMap<>();
        final Object o1 = new Object(), o2 = new Object();
        map.setExpiry(0l); // immediate expiry
        map.put(o1, o2);
        Thread.sleep(5); // so we don't manage to put and get in the same nanosecond
        assertNull(map.get(o1));
    }

}
