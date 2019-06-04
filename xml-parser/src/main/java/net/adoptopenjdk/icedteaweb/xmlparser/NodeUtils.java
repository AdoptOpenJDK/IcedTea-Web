package net.adoptopenjdk.icedteaweb.xmlparser;

import net.adoptopenjdk.icedteaweb.Assert;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;

/**
 * Utilities method for {@link Node}.
 */
public class NodeUtils {
    /**
     * Returns the first child node with the specified name.
     */
    public static Node getChildNode(final Node node, final String name) {
        final Node[] result = getChildNodes(node, name);
        if (result.length == 0) {
            return null;
        } else {
            return result[0];
        }
    }

    /**
     * Returns all child nodes with the specified name.
     */
    public static Node[] getChildNodes(final Node node, final String name) {
        final List<Node> result = new ArrayList<>();

        Node child = node.getFirstChild();
        while (child != null) {
            if (child.getNodeName().getName().equals(name)) {
                result.add(child);
            }
            child = child.getNextSibling();
        }

        return result.toArray(new Node[0]);
    }

    /**
     * Returns the implied text under a node, for example "text" in
     * "&lt;description&gt;text&lt;/description&gt;".
     *
     * @param node the node with text under it
     */
    public static String getSpanText(final Node node) {
        return getSpanText(node, true);
    }

    /**
     * Returns the implied text under a node, for example "text" in
     * "&lt;description&gt;text&lt;/description&gt;". If preserveSpacing is
     * false, sequences of whitespace characters are turned into a single space
     * character.
     *
     * @param node the node with text under it
     * @param preserveSpacing if true, preserve whitespace
     */
    public static String getSpanText(final Node node, final boolean preserveSpacing) {
        if (node == null) {
            return null;
        }

        String val = node.getNodeValue();
        if (preserveSpacing) {
            return val;
        } else if (val == null) {
            return null;
        } else {
            return val.replaceAll("\\s+", " ");
        }
    }

    /**
     * @return an attribute or the specified defaultValue if there is no such
     * attribute.
     *
     * @param node the node
     * @param name the attribute
     * @param defaultValue default if no such attribute
     */
    public static String getAttribute(final Node node, final String name, final String defaultValue) {
        Assert.requireNonNull(node, "node");

        final String result = node.getAttribute(name);

        if (result == null || result.length() == 0) {
            return defaultValue;
        }

        return result;
    }

    /**
     * @return the same result as getAttribute except that if strict mode is
     * enabled or the default value is null a parse exception is thrown instead
     * of returning the default value.
     *
     * @param node the node
     * @param name the attribute
     * @param defaultValue default value
     * @throws ParseException if the attribute does not exist or is empty
     */
    public static String getRequiredAttribute(final Node node, final String name, final String defaultValue, final boolean strict) throws ParseException {
        final String result = getAttribute(node, name, null);

        if (result == null || result.length() == 0) {
            if (strict || defaultValue == null) {
                throw new ParseException("The "+node.getNodeName().getName()+" element must specify a "+name+" attribute.");
            }
        }

        if (result == null) {
            return defaultValue;
        } else {
            return result;
        }
    }

    /**
     * @return the same result as getURL except that a ParseException is thrown
     * if the attribute is null or empty.
     *
     * @param node the node
     * @param name the attribute containing an href
     * @param base the base URL
     * @throws ParseException if the JNLP file is invalid
     */
    public static URL getRequiredURL(final Node node, final String name, final URL base, final boolean strict) throws ParseException {
        // probably should change "" to null so that url is always
        // required even if !strict
        getRequiredAttribute(node, name, "", strict);

        return getURL(node, name, base, strict);
    }

    /**
     * @return a URL object from a href string relative to the code base. If the
     * href denotes a relative URL, it must reference a location that is a
     * subdirectory of the codebase.
     *
     * @param node the node
     * @param name the attribute containing an href
     * @param base the base URL
     * @throws ParseException if the JNLP file is invalid
     */
    public static URL getURL(final Node node, final String name, final URL base, final boolean strict) throws ParseException {
        Assert.requireNonNull(node, "node");

        String href;
        if (XMLParser.CODEBASE.equals(name)) {
            href = node.getAttribute(name);
            //in case of null code can throw an exception later
            //some bogus jnlps have codebase as "" and expect it behaving as "."
            if (href != null && href.trim().isEmpty()) {
                href = ".";
            }
        } else {
            href = getAttribute(node, name, null);
        }
        return getURL(href, node.getNodeName().getName(), base, strict);
    }

    public static URL getURL(final String href, final String nodeName, final URL base, final boolean strict) throws ParseException {
        if (href == null) {
            return null; // so that code can throw an exception if attribute was required
        }
        try {
            if (base == null) {
                return new URL(href);
            } else {
                try {
                    return new URL(href);
                } catch (MalformedURLException ex) {
                    // is relative
                }

                final URL result = new URL(base, href);

                // check for going above the codebase
                if (!result.toString().startsWith(base.toString()) && !base.toString().startsWith(result.toString())) {
                    if (strict) {
                        throw new ParseException("Relative URL does not specify a subdirectory of the codebase. (node="+nodeName+", href="+href+", base="+base+")");
                    }
                }
                return result;
            }

        } catch (MalformedURLException ex) {
            if (base == null) {
                throw new ParseException("Invalid non-relative URL (node="+nodeName+", href="+href+")");
            } else {
                throw new ParseException("Invalid relative URL (node="+nodeName+", href="+href+", base="+base+")");
            }
        }
    }
}
