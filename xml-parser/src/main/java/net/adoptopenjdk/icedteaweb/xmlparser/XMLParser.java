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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import static java.nio.charset.StandardCharsets.UTF_16;
import static java.nio.charset.StandardCharsets.UTF_16BE;
import static java.nio.charset.StandardCharsets.UTF_16LE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;
import static net.adoptopenjdk.icedteaweb.xmlparser.ParserType.MALFORMED;
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
    public static Node getRootNode(InputStream input, ParserType parserToUse) throws ParseException {
        try {
            ParseException.setUsed(parserToUse);
            final XMLParser parser = parserToUse == MALFORMED ? new MalformedXMLParser() : new XMLParser();
            return parser.getRootNode(input);
        } catch (Exception e) {
            throw new ParseException(R("PBadXML"), e);
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

}
