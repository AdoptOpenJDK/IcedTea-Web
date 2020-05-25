package net.adoptopenjdk.icedteaweb.xmlparser;

import java.util.List;

public interface XmlNode {

    XmlNode getFirstChild();

    XmlNode getNextSibling();

    String getNodeValue();

    XmlNode[] getChildNodes();

    List<String> getAttributeNames();

    String getAttribute(String name);

    List<XmlNode> getChildren(final String name);

    String getNodeName();

}
