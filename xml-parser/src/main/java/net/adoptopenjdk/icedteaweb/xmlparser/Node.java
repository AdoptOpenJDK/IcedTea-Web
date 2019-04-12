/* Node.java
   Copyright (C) 2011 Red Hat, Inc.

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

package net.adoptopenjdk.icedteaweb.xmlparser;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

// this class makes assumptions on how parser calls methods (such
// as getFirstChild->getNextChild only called by a single loop at
// a time, so no need for an iterator).

/**
 * This class converts the NanoXML's XMLElement nodes into the
 * regular XML Node interface (for the methods used by Parser).
 */
/* NANO */
public class Node {
    private final XMLElement xml;

    private Node next;

    private Node[] children;

    private List <String> attributeNames= null;

    public Node(final XMLElement xml) {
        this.xml = Objects.requireNonNull(xml);
    }

    public Node getFirstChild() {
        if (children == null) {
            getChildNodes();
        }

        if (children.length == 0) {
            return null;
        }
        else {
            return children[0];
        }
    }

    public Node getNextSibling() {
        return next;
    }

    public String getNodeValue() {
        return xml.getContent();
    }

    public Node[] getChildNodes() {
        if (children == null) {
            final List<Node> list = new ArrayList<>();

            for (Enumeration<XMLElement> e = xml.enumerateChildren(); e.hasMoreElements();) {
                list.add(new Node(e.nextElement()));
            }

            children = list.toArray(new Node[list.size()]);

            for (int i = 0; i < children.length - 1; i++) {
                children[i].next = children[i + 1];
            }
        }

        return children;
    }

    /**
     * To retrieve all attribute names
     * @return all attribute names of the Node in ArrayList<String>
     */
    @SuppressWarnings("unchecked")
    public List<String> getAttributeNames() {
        if (attributeNames == null) {
            attributeNames= new ArrayList<>();

            for (Enumeration<String> e = xml.enumerateAttributeNames(); e.hasMoreElements();) {
                attributeNames.add(e.nextElement());
            }
        }

        return attributeNames;
    }

    public String getAttribute(String name) {
        return (String) xml.getAttribute(name);
    }

    public ElementName getNodeName() {
        if (xml.getName() == null) {
            return new ElementName("");
        }
        else {
            return new ElementName(xml.getName());
        }
    }

    @Override
    public String toString() {
        return getNodeName().getOriginal();
    }
}