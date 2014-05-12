/* FileUtilsTest.java
Copyright (C) 2014 Red Hat, Inc.

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

import java.io.File;

import org.junit.Test;

import static org.junit.Assert.*;

public class FileUtilsTest {

    private static final char[] INVALID = {
            '\\',
            '/',
            ':',
            '*',
            '?',
            '"',
            '<',
            '>',
            '|', };
    private static final char SANITIZED = '_';

    @Test
    public void testSanitizePath() throws Exception {
        for (char ch : INVALID) {
            String str = File.separator + "tmp" + File.separator + "test" + ch + "path";
            String sanitized = FileUtils.sanitizePath(str);
            assertFalse(ch + " should be sanitized from " + sanitized, ch != File.separatorChar && sanitized.contains(Character.toString(ch)));
            assertEquals(str.replace(ch, ch == File.separatorChar ? ch : SANITIZED), sanitized);
        }
    }

    @Test
    public void testSanitizeFilename() throws Exception {
        for (char ch : INVALID) {
            String str = "file" + ch + "name";
            String sanitized = FileUtils.sanitizeFileName(str);
            assertFalse(ch + " should be sanitized from " + sanitized, sanitized.contains(Character.toString(ch)));
            assertEquals(str.replace(ch, SANITIZED), sanitized);
        }
    }

    @Test
    public void testCreateParentDir() throws Exception {
        final File tmpdir = new File(System.getProperty("java.io.tmpdir")), testParent = new File(tmpdir, "itw_test_create_parent_dir"), testChild = new File(testParent, "test_child_dir");
        testChild.deleteOnExit();
        testParent.deleteOnExit();
        FileUtils.createParentDir(testChild);
        assertTrue(tmpdir.isDirectory());
        assertTrue(testParent.isDirectory());
        assertFalse(testChild.exists());
    }

}
