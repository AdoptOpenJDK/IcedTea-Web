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
package net.sourceforge.jnlp.cache.cache;

import net.sourceforge.jnlp.config.PathsAndFiles;
import net.sourceforge.jnlp.util.PropertiesFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

public final class CacheDirectory {

    /* Don't allow instantiation of this class */
    private CacheDirectory(){}

    /**
     * This creates the data for the table.
     *
     * @return ArrayList containing an Object array of data for each row in the table.
     */
    public static ArrayList<Object[]> generateData() {
        final DirectoryNode root = createDirStructure(PathsAndFiles.CACHE_DIR.getFile());
        final ArrayList<Object[]> data = new ArrayList<>();

        for (DirectoryNode identifier : root.getChildren()) {
            for (DirectoryNode type : identifier.getChildren()) {
                for (DirectoryNode domain : type.getChildren()) {
                    //after domain, there is optional port dir. It is skipped here (as is skipped path on domain)
                    for (DirectoryNode leaf : getLeafData(domain)) {
                        final File f = leaf.getFile();
                        final PropertiesFile pf = new PropertiesFile(new File(f.toString() + CacheEntry.INFO_SUFFIX));
                        // if jnlp-path in .info equals path of app to delete mark to delete
                        final String jnlpPath = pf.getProperty(CacheEntry.KEY_JNLP_PATH);
                        final Object[] o = {
                                /* 0 */ leaf,
                                /* 1 */ f.getParentFile(),
                                /* 2 */ type,
                                /* 3 */ domain,
                                /* 4 */ f.length(),
                                /* 5 */ new Date(f.lastModified()),
                                /* 6 */ jnlpPath
                        };
                        data.add(o);
                    }
                }
            }
        }

        return data;
    }

    /**
     * Create the entire DirectoryNode tree starting from the given root directory
     *
     * @param rootPath the root directory
     * @return DirectoryNode tree
     */
    private static DirectoryNode createDirStructure(File rootPath) {
        final DirectoryNode root = new DirectoryNode("Root", rootPath, null);
        initDirStructure(root);
        return root;
    }

    /**
     * Initialize the structure of directory for keeping track of the protocol and domain.
     *
     * @param root Location of cache directory.
     */
    private static void initDirStructure(DirectoryNode root) {
        final File[] files = root.getFile().listFiles();
        for (File f : files != null ? files : new File[0]) {
            if (f.isDirectory()) {
                final DirectoryNode dirNode = new DirectoryNode(f.getName(), f, root);
                root.addChild(dirNode);
                initDirStructure(dirNode);
            } else if (f.isFile() && !f.getName().endsWith(CacheEntry.INFO_SUFFIX)) {
                final File infoFile = new File(f.getAbsolutePath() + CacheEntry.INFO_SUFFIX);
                final DirectoryNode resourceNode = new DirectoryNode(f.getName(), f, root, infoFile);
                root.addChild(resourceNode);
            }
        }
    }

    /**
     * Get all the leaf nodes.
     * 
     * @param root The point where we want to start getting the leafs.
     * @return An ArrayList of DirectoryNode.
     */
    private static ArrayList<DirectoryNode> getLeafData(DirectoryNode root) {
        ArrayList<DirectoryNode> temp = new ArrayList<>();
        for (DirectoryNode f : root.getChildren()) {
            if (f.isDir()) {
                temp.addAll(getLeafData(f));
            } else {
                temp.add(f);
            }
        }
        return temp;
    }
}
