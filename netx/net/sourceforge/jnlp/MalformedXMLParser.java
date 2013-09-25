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
package net.sourceforge.jnlp;

import static net.sourceforge.jnlp.runtime.Translator.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.logging.OutputController;

import org.ccil.cowan.tagsoup.HTMLSchema;
import org.ccil.cowan.tagsoup.Parser;
import org.ccil.cowan.tagsoup.XMLWriter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * An specialized {@link XMLParser} that uses TagSoup[1] to parse
 * malformed XML
 *
 * Used by net.sourceforge.jnlp.Parser
 *
 * [1] http://home.ccil.org/~cowan/XML/tagsoup/
 */
public class MalformedXMLParser extends XMLParser {

    /**
     * Parses the data from an {@link InputStream} to create a XML tree.
     * Returns a {@link Node} representing the root of the tree.
     *
     * @param input the {@link InputStream} to read data from
     * @throws ParseException if an exception occurs while parsing the input
     */
    @Override
    public Node getRootNode(InputStream input) throws ParseException {
        OutputController.getLogger().log("Using MalformedXMLParser");
        InputStream xmlInput = xmlizeInputStream(input);
        return super.getRootNode(xmlInput);
    }

    /**
     * Reads malformed XML from the InputStream original and returns a new
     * InputStream which can be used to read a well-formed version of the input
     *
     * @param original
     * @return an {@link InputStream} which can be used to read a well-formed
     * version of the input XML
     * @throws ParseException
     */
    private InputStream xmlizeInputStream(InputStream original) throws ParseException {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            HTMLSchema schema = new HTMLSchema();
            XMLReader reader = new Parser();

            //TODO walk through the javadoc and tune more settings
            //see tagsoup javadoc for details 
            reader.setProperty(Parser.schemaProperty, schema);
            reader.setFeature(Parser.bogonsEmptyFeature, false);
            reader.setFeature(Parser.ignorableWhitespaceFeature, true);
            reader.setFeature(Parser.ignoreBogonsFeature, false);

            Writer writeger = new OutputStreamWriter(out);
            XMLWriter x = new XMLWriter(writeger);

            reader.setContentHandler(x);

            InputSource s = new InputSource(original);

            reader.parse(s);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (SAXException e) {
            throw new ParseException(R("PBadXML"), e);
        } catch (IOException e) {
            throw new ParseException(R("PBadXML"), e);
        }

    }

}
