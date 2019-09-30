/* DirectoryNode.java -- Structure for maintaining the cache directory tree.
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

import java.io.File;
import java.util.ArrayList;

public class DirectoryNode {
    private final String name;
    private final File path;
    private final ArrayList<DirectoryNode> childNodes;
    private final DirectoryNode parent;
    private final File infoFile;

    /**
     * Create a new instance of DirectoryNode.
     * 
     * @param name Name representing this node.
     * @param absPathToNode Absolute path to this node as a File.
     * @param parent The parent node.
     */
    DirectoryNode(String name, File absPathToNode, DirectoryNode parent) {
        this(name, absPathToNode, parent, null);
    }
    /**
     * Create a new instance of DirectoryNode.
     *
     * @param name Name representing this node.
     * @param absPathToNode Absolute path to this node as a File.
     * @param parent The parent node.
     * @param infoFile the info file used by the LRU Cache
     */
    DirectoryNode(String name, File absPathToNode, DirectoryNode parent, File infoFile) {
        this.name = name;
        this.path = absPathToNode;
        this.childNodes = new ArrayList<>();
        this.parent = parent;
        this.infoFile = infoFile;
    }

    public boolean delete() {
        if (Cache.deleteFromCache(path)) {
            removeThisAndCleanParent();
            return true;
        }
        return false;
    }


    /**
     * This will recursively remove the parent folders if they are empty.
     */
    private void removeThisAndCleanParent() {
        parent.childNodes.remove(this);
        if (this.parent.parent == null) {
            return; // parent is root.
        }
        if (this.parent.childNodes.isEmpty()) {
            this.parent.removeThisAndCleanParent();
        }
    }

    /**
     * @deprecated should return {@link ResourceInfo} to not reveal internal information of the cache
     */
    @Deprecated
    public File getInfoFile() {
        return this.infoFile;
    }

    /**
     * Check if this node is a directory.
     *
     * @return True if node is directory.
     */
    boolean isDir() {
        return path.isDirectory();
    }

    /**
     * Append the given node to the list of child nodes.
     *
     * @param node Node to be appended.
     */
    void addChild(DirectoryNode node) {
        childNodes.add(node);
    }

    /**
     * Retrieve the file associated with this node.
     *
     * @return File that is associated with this node.
     */
    File getFile() {
        return path;
    }

    /**
     * Retrieves the list of child nodes.
     *
     * @return ArrayList of type DirectoryNode containing all the child nodes.
     */
    ArrayList<DirectoryNode> getChildren() {
        return this.childNodes;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
