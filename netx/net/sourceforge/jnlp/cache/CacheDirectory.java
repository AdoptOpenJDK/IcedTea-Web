/* CacheDirectory.java -- Traverse the given directory and return the leafs.
   Copyright (C) 2010 Red Hat, Inc.

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
package net.sourceforge.jnlp.cache;

import java.io.File;
import java.util.ArrayList;

import net.sourceforge.jnlp.util.FileUtils;
import net.sourceforge.jnlp.util.logging.OutputController;

public final class CacheDirectory {

    /* Don't allow instantiation of this class */
    private CacheDirectory(){}

    /**
     * Get the structure of directory for keeping track of the protocol and
     * domain.
     * 
     * @param root Location of cache directory.
     */
    public static void getDirStructure(DirectoryNode root) {
        for (File f : root.getFile().listFiles()) {
            DirectoryNode node = new DirectoryNode(f.getName(), f, root);
            if (f.isDirectory() || (!f.isDirectory() && !f.getName().endsWith(".info")))
                root.addChild(node);
            if (f.isDirectory())
                getDirStructure(node);
        }
    }

    /**
     * Get all the leaf nodes.
     * 
     * @param root The point where we want to start getting the leafs.
     * @return An ArrayList of DirectoryNode.
     */
    public static ArrayList<DirectoryNode> getLeafData(DirectoryNode root) {
        ArrayList<DirectoryNode> temp = new ArrayList<DirectoryNode>();
        for (DirectoryNode f : root.getChildren()) {
            if (f.isDir())
                temp.addAll(getLeafData(f));
            else if (!f.getName().endsWith(".info"))
                temp.add(f);
        }
        return temp;
    }

    /**
     * Removes empty folders in the current directory.
     * 
     * @param root File pointing at the beginning of directory.
     * @return True if something was deleted.
     */
    public static boolean cleanDir(File root) {
        boolean delete = true;
        for (File f : root.listFiles()) {
            if (f.isDirectory())
                cleanDir(f);
            else
                delete = false;
        }
        if (delete){
            OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, "Delete -- " + root);
        }
        //            root.delete();
        return true;
    }

    /**
     * This will recursively remove the parent folders if they are empty. 
     * 
     * @param fileNode
     */
    public static void cleanParent(DirectoryNode fileNode) {
        DirectoryNode parent = fileNode.getParent();
        if (parent.getParent() == null)
            return; // Don't delete the root.
        if (parent.getChildren().size() == 0) {
            FileUtils.deleteWithErrMesg(parent.getFile());
            parent.getParent().removeChild(parent);
            cleanParent(parent);
        }
    }
}
