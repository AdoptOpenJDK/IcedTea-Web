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
package net.sourceforge.jnlp.runtime.html;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.OptionsDefinitions;
import net.sourceforge.jnlp.ParseException;
import net.sourceforge.jnlp.Parser;
import net.sourceforge.jnlp.ParserSettings;
import net.sourceforge.jnlp.cache.UpdatePolicy;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.runtime.Translator;
import net.sourceforge.jnlp.util.logging.OutputController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class is taking HTML document url as input, try to sanitize with
 * tagsoup, if availale - and then finds and applet declarations here. Then it
 * returns first, selected or last one as PluginBridge instance. See parse
 * method.
 */
public class AppletExtractor {

    private final URL html;
    private static final String[] APPLETS = new String[]{
        "applet", "APPLET", "Applet",
        "object", "OBJECT", "Object",
        "embed", "EMBED", "Embed"};
    private final ParserSettings ps;

    public AppletExtractor(URL html) {
        this(html, null);
    }
    public AppletExtractor(URL html, ParserSettings ps) {
        JNLPRuntime.saveHistory(html.toExternalForm());
        this.html = html;
        this.ps = ps;
    }

    public URL getHtml() {
        return html;
    }

    private InputStream cleanStreamIfPossible(InputStream is) {
        try {
            if (ps != null && ps.isMalformedXmlAllowed()){
                Object parser = Parser.getParserInstance(ps);
                Method m = parser.getClass().getMethod("xmlizeInputStream", InputStream.class);
                return (InputStream) m.invoke(null, is);
            } else {
                OutputController.getLogger().log(OutputController.Level.WARNING_ALL, Translator.R("TAGSOUPhtmlNotUsed", OptionsDefinitions.OPTIONS.XML.option));    
            }
        } catch (Exception ex) {
            OutputController.getLogger().log(OutputController.Level.WARNING_ALL, Translator.R("TAGSOUPhtmlBroken"));
            OutputController.getLogger().log(ex);
        }
        return is;
    }   

    public List<Element> findAppletsOnPage() {
        try{
        return findAppletsOnPageImpl(openDocument(cleanStreamIfPossible(JNLPFile.openURL(html, null, UpdatePolicy.ALWAYS))));
        } catch (SAXException sex) {
            throw new RuntimeException(new ParseException(sex));
        } catch (IOException | ParserConfigurationException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private List<Element> findAppletsOnPageImpl(Document doc) throws ParserConfigurationException, SAXException, IOException {
        OutputController.getLogger().log("Root element :" + doc.getDocumentElement().getNodeName());
        //search for applets
        //search for embed/object
        //<embed type="application/x-java-applet" 
        //<object type="application/x-java-applet" 
        //warning all searches are case sensitive
        return findElements(APPLETS, doc.getDocumentElement(), new ElementValidator() {

            @Override
            public boolean isElementValid(Element e) {
                return isApplet(e);
            }
        });
    }

    private Document openDocument(InputStream is) throws SAXException, ParserConfigurationException, IOException {
        OutputController.getLogger().log("Reading " + html.toExternalForm());
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
        doc.getDocumentElement().normalize();
        return doc;
    }

    //warning, even application/x-java-applet;jpi-version=1.5.0_07 and more blah blah  can be in type, so of embed/object start with
    //search for 
    //OBJECT classid="clsid:8AD9C840-044E-11D1-B3E9-00805F499D93"
    //<object codetype="application/x-java-applet" height="120" width="81"
    private static boolean isApplet(Element eElement) {
        if (eElement.getNodeName().toLowerCase().equals("applet")) {
            return true;
        } else {
            String type = eElement.getAttribute("type");
            String codeType = eElement.getAttribute("codetype");
            String classid = eElement.getAttribute("classid");
            if ((type != null && type.toLowerCase().contains("application/x-java-applet"))
                    || (codeType != null && codeType.toLowerCase().contains("application/x-java-applet"))
                    || (classid != null && classid.equalsIgnoreCase("clsid:8AD9C840-044E-11D1-B3E9-00805F499D93"))) {
                return true;
            }
        }
        return false;
    }

    static List<Element> findElements(String[] elements, Element doc, ElementValidator elementValidator) {
        List<Element> found = new LinkedList();
        for (String key : elements) {
            NodeList nList = doc.getElementsByTagName(key);
            for (int temp = 0; temp < nList.getLength(); temp++) {

                Node nNode = nList.item(temp);
                OutputController.getLogger().log("Found in html: " + nNode.getNodeName());
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    if (elementValidator.isElementValid(eElement)) {
                        found.add(eElement);
                    }
                }
            }
        }
        return found;
    }

}
