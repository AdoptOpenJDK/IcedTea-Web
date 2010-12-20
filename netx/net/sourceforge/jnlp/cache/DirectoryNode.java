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
package net.sourceforge.jnlp.cache;

import java.io.File;
import java.util.ArrayList;

public class DirectoryNode {
    private String name;
    private File path;
    private ArrayList<DirectoryNode> childNodes;
    private DirectoryNode parent = null;
    private File infoFile;

    /**
     * Create a new instance of DirectoryNode.
     * 
     * @param name Name representing this node.
     * @param absPathToNode Absolute path to this node given as a String.
     * @param parent The parent node.
     */
    public DirectoryNode(String name, String absPathToNode, DirectoryNode parent) {
        this(name, new File(absPathToNode), parent);
    }

    /**
     * Create a new instance of DirectoryNode.
     * 
     * @param name Name representing this node.
     * @param absPathToNode Absolute path to this node as a File.
     * @param parent The parent node.
     */
    public DirectoryNode(String name, File absPathToNode, DirectoryNode parent) {
        this(name, absPathToNode, null, parent);
    }

    /**
     * Create a new instance of DirectoryNode.
     * 
     * @param name Name representing this node.
     * @param absPathToNode Absolute path to this node given as a File.
     * @param childNodes List of children nodes.
     * @param parent The parent node.
     */
    public DirectoryNode(String name, File absPathToNode, ArrayList<DirectoryNode> childNodes, DirectoryNode parent) {
        this.name = name;
        this.path = absPathToNode;
        this.childNodes = childNodes;
        if (this.childNodes == null)
            this.childNodes = new ArrayList<DirectoryNode>();
        this.parent = parent;
        if (!isDir())
            this.infoFile = new File(this.getFile().getAbsolutePath().concat(".info"));
    }

    /**
     * Append the given node to the list of child nodes.
     * 
     * @param node Node to be appended.
     */
    public void addChild(DirectoryNode node) {
        try {
            childNodes.add(node);
        } catch (NullPointerException e) {
            this.childNodes = new ArrayList<DirectoryNode>();
            this.childNodes.add(node);
        }
    }

    /**
     * Removes the node specified.
     * 
     * @param node Node to be removed from the list of children
     * @return true if this list of children contained the specified element
     */
    public boolean removeChild(DirectoryNode node) {
        return this.childNodes.remove(node);
    }

    /**
     * Retrieve the name of this node.
     * 
     * @return Name of this node.
     */
    public String getName() {
        return this.name;
    }

    public String toString() {
        return this.name;
    }

    /**
     * Retrieve the file associated with this node.
     * 
     * @return File that is associated with this node.
     */
    public File getFile() {
        return path;
    }

    /**
     * Retrieve the parent node.
     * 
     * @return DirectoryNode representing the parent of the current node.
     */
    public DirectoryNode getParent() {
        return parent;
    }

    /**
     * Retrieves the list of child nodes.
     * 
     * @return ArrayList of type DirectoryNode containing all the child nodes.
     */
    public ArrayList<DirectoryNode> getChildren() {
        return this.childNodes;
    }

    /**
     * Check if this node is a directory.
     * 
     * @return True if node is directory.
     */
    public boolean isDir() {
        return path.isDirectory();
    }

    public File getInfoFile() {
        return this.infoFile;
    }

}
