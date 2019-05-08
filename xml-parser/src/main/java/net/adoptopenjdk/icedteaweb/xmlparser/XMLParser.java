/*
   Copyright (C) 2013 Red Hat, Inc.

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

import net.adoptopenjdk.icedteaweb.Assert;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_16;
import static java.nio.charset.StandardCharsets.UTF_16BE;
import static java.nio.charset.StandardCharsets.UTF_16LE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;
import static net.adoptopenjdk.icedteaweb.xmlparser.XMLSanitizer.sanitizeXml;

/**
 * A gateway to the actual implementation of the parsers.
 *
 * Used by net.sourceforge.jnlp.Parser
 */
public class XMLParser {
    private static final String UNICODE_LITTLE = "UnicodeLittle";
    private static final String UTF_32_BE = "UTF-32BE";
    private static final String UTF_32_LE = "UTF-32LE";
    private static final String X_UTF_32_BE_BOM = "X-UTF-32BE-BOM";
    private static final String X_UTF_32_LE_BOM = "X-UTF-32LE-BOM";

    public static final String CODEBASE = "codebase";

    /**
     * @return the root node from the XML document in the specified input
     * stream.
     *
     * @throws ParseException if the JNLP file is invalid
     */
    public static Node getRootNode(InputStream input, boolean malformedXmlAllowed) throws ParseException {
        try {
            XMLParser parser = getParserInstance(malformedXmlAllowed);
            return parser.getRootNode(input);
        } catch (Exception e) {
            throw new ParseException(R("PBadXML"), e);
        }
    }

    public static XMLParser getParserInstance(boolean malformedXmlAllowed) {
        if (malformedXmlAllowed) {
            return new MalformedXMLParser();
        } else {
            return new XMLParser();
        }
    }

    /**
     * Parses input from an InputStream and returns a Node representing the
     * root of the parse tree.
     *
     * @param input the {@link InputStream} containing the XML
     * @return a {@link Node} representing the root of the parsed XML
     * @throws ParseException if parsing fails
     */
    public Node getRootNode(final InputStream input) throws ParseException {
        try {
            // A BufferedInputStream is used to allow marking and resetting of a stream.
            final BufferedInputStream bs = new BufferedInputStream(input);
            final Charset encoding = getEncoding(bs);
            final InputStreamReader isr = new InputStreamReader(bs, encoding);

            // Clean the jnlp xml file of all comments before passing it to the parser.
            final XMLElement xml = new XMLElement();
            xml.parseFromReader(sanitizeXml(isr));
            return new Node(xml);
        } catch (Exception ex) {
            throw new ParseException(R("PBadXML"), ex);
        }
    }

    /**
     * Returns the name of the encoding used in this InputStream.
     *
     * @param input the InputStream
     * @return a String representation of encoding
     */
    private static Charset getEncoding(final InputStream input) throws IOException {
        //Fixme:
        // This only recognizes UTF-8, UTF-16, and UTF-32,
        // which is enough to parse the prolog portion of xml to
        // find out the exact encoding (if it exists). The reason being
        // there could be other encodings, such as ISO 8859 which is 8-bits
        // but it supports latin characters.
        // So what needs to be done is to parse the prolog and retrieve
        // the exact encoding from it.

        final int[] s = new int[4];

        //Determine what the first four bytes are and store them into an int array.
        input.mark(4);
        for (int i = 0; i < 4; i++) {
            s[i] = input.read();
        }
        input.reset();

        //Set the encoding base on what the first four bytes of the
        //inputstream turn out to be (following the information from
        //www.w3.org/TR/REC-xml/#sec-guessing).
        if (s[0] == 255) {
            if (s[1] == 254) {
                if (s[2] != 0 || s[3] != 0) {
                    return Charset.forName(UNICODE_LITTLE);
                } else {
                    return Charset.forName(X_UTF_32_LE_BOM);
                }
            }
        } else if (s[0] == 254 && s[1] == 255 && (s[2] != 0 || s[3] != 0)) {
            return UTF_16;
        } else if (s[0] == 0 && s[1] == 0 && s[2] == 254 && s[3] == 255) {
            return Charset.forName(X_UTF_32_BE_BOM);
        } else if (s[0] == 0 && s[1] == 0 && s[2] == 0 && s[3] == 60) {
            return Charset.forName(UTF_32_BE);
        } else if (s[0] == 60 && s[1] == 0 && s[2] == 0 && s[3] == 0) {
            return Charset.forName(UTF_32_LE);
        } else if (s[0] == 0 && s[1] == 60 && s[2] == 0 && s[3] == 63) {
            return UTF_16BE;
        } else if (s[0] == 60 && s[1] == 0 && s[2] == 63 && s[3] == 0) {
            return UTF_16LE;
        }
        return UTF_8;
    }

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

        return result.toArray(new Node[result.size()]);
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
     * Returns a URL with a trailing / appended to it if there is no trailing
     * slash on the specified URL.
     */
    public static URL addSlash(final URL source) {
        if (source == null) {
            return null;
        }

        if (!source.toString().endsWith("/")) {
            try {
                return new URL(source.toString() + "/");
            } catch (MalformedURLException ex) {
                throw new IllegalArgumentException("Could not add slash to malformed URL: "+ source.toString(), ex);
            }
        }

        return source;
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
                throw new ParseException(R("PNeedsAttribute", node.getNodeName().getName(), name));
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
        if (CODEBASE.equals(name)) {
            href = node.getAttribute(name);
            //in case of null code can throw an exception later
            //some bogus jnlps have codebase as "" and expect it behaving as "."
            if (href != null && href.trim().isEmpty()) {
                href = ".";
            }
        } else {
            href = getAttribute(node, name, null);
        }
        return XMLParser.getURL(href, node.getNodeName().getName(), base, strict);
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
                        throw new ParseException(R("PUrlNotInCodebase", nodeName, href, base));
                    }
                }
                return result;
            }

        } catch (MalformedURLException ex) {
            if (base == null) {
                throw new ParseException(R("PBadNonrelativeUrl", nodeName, href));
            } else {
                throw new ParseException(R("PBadRelativeUrl", nodeName, href, base));
            }
        }
    }
}
