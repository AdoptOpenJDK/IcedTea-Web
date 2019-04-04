/*Copyright (C) 2014 Red Hat, Inc.

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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MD5SumWatcherTest {

    private File file;
    private MD5SumWatcher watcher;

    @Before
    public void createNewFile() throws Exception {
        file = File.createTempFile("md5sumwatchertest", "tmp");
        file.deleteOnExit();
        watcher = new MD5SumWatcher(file);
    }

    @After
    public void deleteTempFile() throws Exception {
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    public void testNonExistentFile() {
        file.delete();
        file.mkdirs();
        watcher = new MD5SumWatcher(file);
        boolean gotException = false;
        try {
            watcher.update();
        } catch (final Exception e) {
            gotException = true;
            assertTrue("Should have received FileNotFoundException", e instanceof FileNotFoundException);
        }
        assertTrue("Should have received FileNotFoundException", gotException);
    }

    @Test
    public void testNoFileChangeGivesSameMd5() throws Exception {
        byte[] sum = watcher.getSum();
        byte[] sum2 = watcher.getSum();
        assertTrue("MD5 sums should be the same. first: " + Arrays.toString(sum) + ", second: " + Arrays.toString(sum2),
                Arrays.equals(sum, sum2));
    }

    @Test
    public void testSavingToFileChangesMd5() throws Exception {
        byte[] original = watcher.getSum();
        FileUtils.saveFile("some test content\n", file);
        byte[] changed = watcher.getSum();
        assertFalse("MD5 sum should have changed, but was constant as " + Arrays.toString(original),
                Arrays.equals(original, changed));
    }

    @Test
    public void testUnchangedContentUpdate() throws Exception {
        assertFalse("update() should return false", watcher.update());
    }

    @Test
    public void testChangedContentUpdate() throws Exception {
        FileUtils.saveFile("some test content\n", file);
        final boolean changed = watcher.update();
        assertTrue("update() should return true", changed);
    }

}
