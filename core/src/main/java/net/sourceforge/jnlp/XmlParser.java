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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import net.sourceforge.jnlp.util.logging.OutputController;

import net.sourceforge.nanoxml.XMLElement;

//import javax.xml.parsers.*; // commented to use right Node
//import org.w3c.dom.*;       // class for using Tiny XML | NanoXML
//import org.xml.sax.*;
//import gd.xml.tiny.*;

/**
 * A gateway to the actual implementation of the parsers.
 *
 * Used by net.sourceforge.jnlp.Parser
 */
class XMLParser {

    /**
     * Parses input from an InputStream and returns a Node representing the
     * root of the parse tree.
     *
     * @param input the {@link InputStream} containing the XML
     * @return a {@link Node} representing the root of the parsed XML
     * @throws ParseException if parsing fails
     */
    public Node getRootNode(InputStream input) throws ParseException {

        try {
            /* SAX
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(errorHandler);

            Document doc = builder.parse(input);
            return doc.getDocumentElement();
            */

            /* TINY
            Node document = new Node(TinyParser.parseXML(input));
            Node jnlpNode = getChildNode(document, "jnlp"); // skip comments
            */

            //A BufferedInputStream is used to allow marking and reseting
            //of a stream.
            BufferedInputStream bs = new BufferedInputStream(input);

            /* NANO */
            final XMLElement xml = new XMLElement();
            final PipedInputStream pin = new PipedInputStream();
            final PipedOutputStream pout = new PipedOutputStream(pin);
            final InputStreamReader isr = new InputStreamReader(bs, getEncoding(bs));
            // Clean the jnlp xml file of all comments before passing
            // it to the parser.
            new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            (new XMLElement()).sanitizeInput(isr, pout);
                            try {
                                pout.close();
                            } catch (IOException ioe) {
                                OutputController.getLogger().log(OutputController.Level.ERROR_ALL, ioe);
                            }
                        }
                    }).start();
            xml.parseFromReader(new InputStreamReader(pin));
            Node jnlpNode = new Node(xml);
            return jnlpNode;
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
    private static String getEncoding(InputStream input) throws IOException {
        //Fixme: This only recognizes UTF-8, UTF-16, and
        //UTF-32, which is enough to parse the prolog portion of xml to
        //find out the exact encoding (if it exists). The reason being
        //there could be other encodings, such as ISO 8859 which is 8-bits
        //but it supports latin characters.
        //So what needs to be done is to parse the prolog and retrieve
        //the exact encoding from it.

        int[] s = new int[4];
        String encoding = "UTF-8";

        //Determine what the first four bytes are and store
        //them into an int array.
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
                    encoding = "UnicodeLittle";
                } else {
                    encoding = "X-UTF-32LE-BOM";
                }
            }
        } else if (s[0] == 254 && s[1] == 255 && (s[2] != 0 ||
                s[3] != 0)) {
            encoding = "UTF-16";

        } else if (s[0] == 0 && s[1] == 0 && s[2] == 254 &&
                s[3] == 255) {
            encoding = "X-UTF-32BE-BOM";

        } else if (s[0] == 0 && s[1] == 0 && s[2] == 0 &&
                s[3] == 60) {
            encoding = "UTF-32BE";

        } else if (s[0] == 60 && s[1] == 0 && s[2] == 0 &&
                s[3] == 0) {
            encoding = "UTF-32LE";

        } else if (s[0] == 0 && s[1] == 60 && s[2] == 0 &&
                s[3] == 63) {
            encoding = "UTF-16BE";
        } else if (s[0] == 60 && s[1] == 0 && s[2] == 63 &&
                s[3] == 0) {
            encoding = "UTF-16LE";
        }

        return encoding;
    }
}
