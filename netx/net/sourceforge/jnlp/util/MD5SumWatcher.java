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

import static net.sourceforge.jnlp.runtime.Translator.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import net.sourceforge.jnlp.util.logging.OutputController;

public class MD5SumWatcher {

    private final File watchedFile;
    private byte[] md5sum;

    /**
     * Create a new MD5SumWatcher instance
     * @param watchedFile the file to watch
     */
    public MD5SumWatcher(final File watchedFile) {
        this.watchedFile = watchedFile;
        try {
            this.md5sum = getSum();
        } catch (final IOException ioe) {
            OutputController.getLogger().log(ioe);
            this.md5sum = null;
        }
    }

    /**
     * Get the current MD5 sum of the watched file
     * @return a byte array of the MD5 sum
     * @throws FileNotFoundException if the watched file does not exist
     * @throws IOException if the file cannot be read
     */
    public byte[] getSum() throws FileNotFoundException, IOException {
        update();
        return md5sum;
    }

    /**
     * Detect if the file's MD5 has changed and track its new sum if so
     * @return if the file's MD5 has changed since the last update
     * @throws FileNotFoundException if the watched file does not exist
     * @throws IOException if the file cannot be read
     */
    public boolean update() throws FileNotFoundException, IOException {
        byte[] newSum;
        try {
            newSum = FileUtils.getFileMD5Sum(watchedFile, "MD5");
        } catch (final NoSuchAlgorithmException e) {
            // There definitely should be an MD5 algorithm, but if not, all we can do is fail.
            // This really, really is not expected to happen, so rethrow as RuntimeException
            // to avoid having to check for NoSuchAlgorithmExceptions all the time
            OutputController.getLogger().log(e);
            throw new RuntimeException(e);
        }
        final boolean changed = !Arrays.equals(newSum, md5sum);
        md5sum = newSum;
        return changed;
    }

}
