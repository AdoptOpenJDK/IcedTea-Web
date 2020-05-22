package net.adoptopenjdk.icedteaweb.xmlparser;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.text.Collator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class XmlNodeImpl implements XmlNode, Comparable<XmlNode> {

    private final Element internalElement;

    private final XmlNode nextNode;

    private final List<XmlNode> children;

    public XmlNodeImpl(final Element internalElement) {
        this(internalElement, null);
    }

    public XmlNodeImpl(final Element internalElement, final XmlNode nextNode) {
        this.internalElement = internalElement;
        this.nextNode = nextNode;

        final NodeList childNodes = internalElement.getChildNodes();
        final List<Element> childrenElements = IntStream.range(0, childNodes.getLength())
                .mapToObj(i -> childNodes.item(i))
                .filter(n -> n instanceof Element)
                .map(n -> (Element) n)
                .sorted((e1, e2) -> Collator.getInstance().compare(e1.getTagName(), e2.getTagName()))
                .collect(Collectors.toList());
        final Map<Element, XmlNodeImpl> mappedChildren = new HashMap<>();
        for (int i = childrenElements.size() - 1; i >= 0; i--) {
            final Element element = childrenElements.get(i);
            if(i != childrenElements.size() - 1) {
                final Element nextElement = childrenElements.get(i+1);
                final XmlNodeImpl nextChildNode = mappedChildren.get(nextElement);
                if(nextChildNode == null) {
                    throw new RuntimeException("Error in XML parsing!");
                }
                mappedChildren.put(element, new XmlNodeImpl(element, nextChildNode));
            } else {
                mappedChildren.put(element, new XmlNodeImpl(element));
            }
        }
        this.children = childrenElements.stream().map(e -> mappedChildren.get(e))
                .collect(Collectors.toList());
    }

    @Override
    public XmlNode getFirstChild() {
        if (children.isEmpty()) {
            return null;
        }
        return children.get(0);
    }

    @Override
    public XmlNode getNextSibling() {
        return nextNode;
    }

    @Override
    public String getNodeValue() {
        if(children.size() == 0) {
            return internalElement.getTextContent();
        } else {
            return children.get(0).getNodeValue();
        }
    }

    @Override
    public XmlNode[] getChildNodes() {
        return children.toArray(new XmlNode[children.size()]);
    }

    @Override
    public List<String> getAttributeNames() {
        final NamedNodeMap attributes = internalElement.getAttributes();
        final int lenght = attributes.getLength();
        return IntStream.range(0, lenght).mapToObj(i -> attributes.item(i).getNodeName())
                .collect(Collectors.toList());
    }

    @Override
    public String getAttribute(final String name) {
        return internalElement.getAttribute(name);
    }

    @Override
    public String getNodeName() {
        return internalElement.getNodeName();
    }

    @Override
    public List<XmlNode> getChildren(final String name) {
        return children.stream()
                .filter(c -> Objects.equals(c.getNodeName(), name))
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "XmlNode" + " - " + internalElement;
    }

    @Override
    public int compareTo(final XmlNode o) {
        final String nameA = getNodeName();
        final String nameB = Optional.ofNullable(o)
                .map(n -> n.getNodeName())
                .orElse("");
        return nameA.compareTo(nameB);
    }
}
