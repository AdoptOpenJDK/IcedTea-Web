package net.sourceforge.jnlp;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import net.sourceforge.nanoxml.XMLElement;

// this class makes assumptions on how parser calls methods (such
// as getFirstChild->getNextChild only called by a single loop at
// a time, so no need for an iterator).

/**
 * This class converts the NanoXML's XMLElement nodes into the
 * regular XML Node interface (for the methods used by Parser).
 */
/* NANO */
class Node {
    private XMLElement xml;
    private Node next;
    private Node children[];
    private List <String> attributeNames= null;

    Node(XMLElement xml) {
        this.xml = xml;
    }

    Node getFirstChild() {
        if (children == null)
            getChildNodes();

        if (children.length == 0)
            return null;
        else
            return children[0];
    }

    Node getNextSibling() {
        return next;
    }

    void normalize() {
    }

    String getNodeValue() {
        return xml.getContent();
    }

    Node[] getChildNodes() {
        if (children == null) {
            List<Node> list = new ArrayList<Node>();

            for (Enumeration e = xml.enumerateChildren(); e.hasMoreElements();)
                list.add(new Node((XMLElement) e.nextElement()));

            children = list.toArray(new Node[list.size()]);

            for (int i = 0; i < children.length - 1; i++)
                children[i].next = children[i + 1];
        }

        return children;
    }
    
    /**
     * To retrieve all attribute names
     * @return all attribute names of the Node in ArrayList<String> 
     */
    List<String> getAttributeNames() {
        if (attributeNames == null) {
            attributeNames= new ArrayList<String>();

            for (Enumeration e = xml.enumerateAttributeNames(); e.hasMoreElements();)
                attributeNames.add(new String((String) e.nextElement()));
        }

        return attributeNames;
    }

    String getAttribute(String name) {
        return (String) xml.getAttribute(name);
    }

    String getNodeName() {
        if (xml.getName() == null)
            return "";
        else
            return xml.getName();
    }

    public String toString() {
        return getNodeName();
    }
}

/**
 * This class converts the TinyXML's ParsedXML nodes into the
 * regular XML Node interface (for the methods used by Parser).
 */
/* TINY
class Node {
    private ParsedXML tinyNode;
    private Node next;
    private Node children[];
    private String attributeNames[];

    Node(ParsedXML tinyNode) {
        this.tinyNode = tinyNode;
    }

    Node getFirstChild() {
        if (children == null)
            getChildNodes();

        if (children.length == 0)
            return null;
        else
            return children[0];
    }

    Node getNextSibling() {
        return next;
    }

    void normalize() {
    }

    String getNodeValue() {
        return tinyNode.getContent();
    }

    Node[] getChildNodes() {
        if (children == null) {
            List list = new ArrayList();

            for (Enumeration e = tinyNode.elements(); e.hasMoreElements();) {
                list.add( new Node((ParsedXML)e.nextElement()) );
            }
            children = (Node[]) list.toArray( new Node[list.size()] );

            for (int i=0; i < children.length-1; i++)
                children[i].next = children[i+1];
        }

        return children;
    }
    
    String getAttribute(String name) {
        return tinyNode.getAttribute(name);
    }

    String getNodeName() {
        if (tinyNode.getName() == null)
            return "";
        else
            return tinyNode.getName();
    }

    public String toString() {
        return getNodeName();
    }
}
*/
