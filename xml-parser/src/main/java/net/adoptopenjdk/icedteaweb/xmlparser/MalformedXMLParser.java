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

import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import org.ccil.cowan.tagsoup.HTMLSchema;
import org.ccil.cowan.tagsoup.Parser;
import org.ccil.cowan.tagsoup.XMLWriter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import static net.adoptopenjdk.icedteaweb.xmlparser.ParserType.MALFORMED;

/**
 * An specialized {@link XMLParser} that uses TagSoup[1] to parse
 * malformed XML
 * <p>
 * Used by net.sourceforge.jnlp.Parser
 * <p>
 * [1] http://home.ccil.org/~cowan/XML/tagsoup/
 */
public class MalformedXMLParser extends XMLParser {

    private final static Logger LOG = LoggerFactory.getLogger(MalformedXMLParser.class);

    /**
     * Reads malformed XML from the InputStream original and returns a new
     * InputStream which can be used to read a well-formed version of the input
     *
     * @param original original input
     * @return an {@link InputStream} which can be used to read a well-formed
     * version of the input XML
     * @throws ParseException if an exception occurs while parsing the input
     */
    public Reader preprocessXml(final Reader original) throws ParseException {
        LOG.info("Using MalformedXMLParser");
        ParseException.setUsed(MALFORMED);
        try {
            final HTMLSchema schema = new HTMLSchema();
            final XMLReader reader = new Parser();

            //TODO walk through the javadoc and tune more settings
            //see tagsoup javadoc for details 
            reader.setProperty(Parser.schemaProperty, schema);
            reader.setFeature(Parser.bogonsEmptyFeature, false);
            reader.setFeature(Parser.ignorableWhitespaceFeature, true);
            reader.setFeature(Parser.ignoreBogonsFeature, false);

            final Writer writer = new StringWriter();
            final XMLWriter xmlWriter = new XMLWriter(writer);
            reader.setContentHandler(xmlWriter);
            final InputSource s = new InputSource(original);
            reader.parse(s);
            return new StringReader(writer.toString());
        } catch (final SAXException | IOException e1) {
            throw new ParseException("Invalid XML document syntax.", e1);
        }
    }

}
