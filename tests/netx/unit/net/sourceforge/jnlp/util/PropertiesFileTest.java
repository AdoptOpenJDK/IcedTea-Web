/*  PropertiesFileTest.java
   Copyright (C) 2012 Thomas Meyer

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

package net.sourceforge.jnlp.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.Before;
import org.junit.Test;

public class PropertiesFileTest {

    private PropertiesFile propertiesFile;

    @Before
    public void setup() throws IOException {
        File lru = Files.createTempFile("properties_file", ".tmp").toFile();
        lru.createNewFile();
        lru.deleteOnExit();
        propertiesFile = new PropertiesFile(lru);
    }

    @Test
    public void testSetProperty() {
        propertiesFile.setProperty("key", "value");
        assertTrue(propertiesFile.containsKey("key") && propertiesFile.containsValue("value"));
    }

    @Test
    public void testGetProperty() {
        propertiesFile.setProperty("key", "value");
        String v = propertiesFile.getProperty("key");
        assertEquals("value", v);
    }

    @Test
    public void testGetDefaultProperty() {
        String v = propertiesFile.getProperty("key", "default");
        assertEquals("default", v);
    }

    @Test
    public void testStore() throws IOException {
        String key = "key";
        String value = "value";
        propertiesFile.setProperty(key, value);
        try {
            propertiesFile.lock();
            propertiesFile.store();
        } finally {
            propertiesFile.unlock();
        }

        File f = propertiesFile.getStoreFile();
        String output = new String(Files.readAllBytes(f.toPath()));
        assertTrue(output.contains(key + "=" + value));
    }

    @Test
    public void testReloadAfterStore() {
        try {
            boolean reloaded;
            propertiesFile.lock();

            // 1. clear entries + store
            clearPropertiesFile();

            // 2. load from file
            reloaded = propertiesFile.load();
            assertTrue("File was not reloaded!", reloaded);

            // 3. add some entries and store
            fillProperties(10);

            propertiesFile.store();
            reloaded = propertiesFile.load();

            assertTrue("File was not reloaded!", reloaded);
        } finally {
            propertiesFile.unlock();
        }
    }

    private void fillProperties(int noEntries) {
        for(int i = 0; i < noEntries; i++) {
            propertiesFile.setProperty(String.valueOf(i), String.valueOf(i));
        }
    }

    private void clearPropertiesFile() {
        try {
            propertiesFile.lock();

            // clear cache + store file
            propertiesFile.clear();
            propertiesFile.store();
        } finally {
            propertiesFile.unlock();
        }
    }

    @Test
    public void testLoad() throws InterruptedException {
        try {
            propertiesFile.lock();

            propertiesFile.setProperty("key", "value");
            propertiesFile.store();

            propertiesFile.setProperty("shouldNotRemainAfterLoad", "def");
            propertiesFile.load();

            assertFalse(propertiesFile.contains("shouldNotRemainAfterLoad"));
        } finally {
            propertiesFile.unlock();

        }
    }

    @Test
    public void testLoadWithNoChanges() throws InterruptedException {
        try {
            propertiesFile.lock();

            propertiesFile.setProperty("key", "value");
            propertiesFile.store();

            Thread.sleep(1000l);

            assertFalse(propertiesFile.load());
        } finally {
            propertiesFile.unlock();
        }
    }

    @Test
    public void testLock() throws IOException {
        try {
            propertiesFile.lock();
            assertTrue(propertiesFile.isHeldByCurrentThread());
        } finally {
            propertiesFile.unlock();
        }
    }

    @Test
    public void testUnlock() throws IOException {
        try {
            propertiesFile.lock();
        } finally {
            propertiesFile.unlock();
        }
        assertTrue(!propertiesFile.isHeldByCurrentThread());
    }
}
