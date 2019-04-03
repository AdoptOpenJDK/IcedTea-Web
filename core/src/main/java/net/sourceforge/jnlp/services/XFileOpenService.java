/* XFileOpenService.java
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

import javax.swing.JFileChooser;

/**
 * The FileOpenService JNLP service.
 *
 * @author <a href="mailto:jsumali@redhat.com">Joshua Sumali</a>
 */
class XFileOpenService implements FileOpenService {

    protected XFileOpenService() {
    }

    /**
     * Prompts the user to select a single file.
     */
    public FileContents openFileDialog(java.lang.String pathHint,
            java.lang.String[] extensions) throws java.io.IOException {

        if (ServiceUtil.checkAccess(AccessType.READ_FILE)) {

            //open a file dialog here, let the user choose the file.
            JFileChooser chooser = new JFileChooser();
            int chosen = chooser.showOpenDialog(null);
            if (chosen == JFileChooser.APPROVE_OPTION) {
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
     * Prompts the user to select one or more files.
     */
    public FileContents[] openMultiFileDialog(java.lang.String pathHint,
            java.lang.String[] extensions) throws java.io.IOException {

        if (ServiceUtil.checkAccess(AccessType.WRITE_FILE)) {
            JFileChooser chooser = new JFileChooser();
            chooser.setMultiSelectionEnabled(true);
            int chosen = chooser.showOpenDialog(null);

            if (chosen == JFileChooser.APPROVE_OPTION) {
                File[] files = chooser.getSelectedFiles();
                int length = files.length;
                FileContents[] result = new FileContents[length];
                for (int i = 0; i < length; i++) {
                    XFileContents xfile = new XFileContents(files[i]);
                    result[i] = (FileContents) ServiceUtil.createPrivilegedProxy(FileContents.class, xfile);
                }
                return result;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
