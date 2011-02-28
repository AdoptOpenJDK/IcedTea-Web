/* XFileSaveService.java
   Copyright (C) 2008 Red Hat, Inc.

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

package net.sourceforge.jnlp.services;

import java.io.*;
import javax.jnlp.*;

import net.sourceforge.jnlp.security.SecurityDialogs.AccessType;
import net.sourceforge.jnlp.util.FileUtils;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * The FileSaveService JNLP service.
 *
 * @author <a href="mailto:jsumali@redhat.com">Joshua Sumali</a>
 */
class XFileSaveService implements FileSaveService {

    protected XFileSaveService() {
    }

    /**
     * Prompts the user to save a file.
     */
    public FileContents saveFileDialog(java.lang.String pathHint,
            java.lang.String[] extensions, java.io.InputStream stream,
            java.lang.String name) throws java.io.IOException {

        if (ServiceUtil.checkAccess(AccessType.WRITE_FILE)) {
            JFileChooser chooser = new JFileChooser();
            int chosen = chooser.showSaveDialog(null);

            if (chosen == JFileChooser.APPROVE_OPTION) {
                writeToFile(stream, chooser.getSelectedFile());
                return (FileContents) ServiceUtil.createPrivilegedProxy(
                           FileContents.class,
                           new XFileContents(chooser.getSelectedFile()));
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Prompts the user to save a file, with an optional pre-set filename.
     */
    public FileContents saveAsFileDialog(java.lang.String pathHint,
            java.lang.String[] extensions, FileContents contents) throws java.io.IOException {

        if (ServiceUtil.checkAccess(AccessType.WRITE_FILE)) {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File(contents.getName()));
            int chosen = chooser.showSaveDialog(null);

            if (chosen == JFileChooser.APPROVE_OPTION) {
                writeToFile(contents.getInputStream(),
                            chooser.getSelectedFile());

                return (FileContents) ServiceUtil.createPrivilegedProxy(
                           FileContents.class,
                           new XFileContents(chooser.getSelectedFile()));
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Writes actual file to disk.
     */
    private void writeToFile(InputStream stream, File file) throws IOException {
        if (!file.createNewFile()) { //file exists
            boolean replace = (JOptionPane.showConfirmDialog(null,
                                file.getAbsolutePath() + " already exists.\n"
                                        + "Do you want to replace it?",
                                "Warning - File Exists", JOptionPane.YES_NO_OPTION) == 0);
            if (!replace)
                return;
        } else {
            FileUtils.createRestrictedFile(file, true);
        }

        if (file.canWrite()) {
            FileOutputStream out = new FileOutputStream(file);
            byte[] b = new byte[256];
            int read = 0;
            while ((read = stream.read(b)) > 0)
                out.write(b, 0, read);
            out.flush();
            out.close();
        } else {
            throw new IOException("Unable to open file for writing");
        }
    }
}
