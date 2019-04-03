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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sourceforge.jnlp.ParseException;
import net.sourceforge.jnlp.Parser;
import net.sourceforge.jnlp.PluginBridge;
import net.sourceforge.jnlp.PluginParameters;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public class AppletParser {

    private static final String[] PARAMS = new String[]{
        "param", "PARAM", "Param"
    };

    private final Element source;
    private final URL docBase;

    public AppletParser(Element applet, URL docbase) {
        this.source = applet;
        this.docBase = docbase;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("width: ").append(source.getAttribute("width")).append("\n");
        sb.append("height: ").append(source.getAttribute("height")).append("\n");
        sb.append("codebase: ").append(source.getAttribute("codebase")).append("\n");
        sb.append("code: ").append(source.getAttribute("code")).append("\n");
        sb.append("archive: ").append(source.getAttribute("archive")).append("\n");
        sb.append("data: ").append(source.getAttribute("data")).append("\n");
        List<Element> found = findParams();
        sb.append("params: ").append(found.size()).append("\n");
        for (Element element : found) {
            sb.append("param: ").append(element.getAttribute("name")).append("=").append(element.getAttribute("value")).append("\n");
        }
        return sb.toString();
    }

    public PluginBridge toPluginBridge() throws Exception {
        return new PluginBridge(
                createCodebase(), 
                docBase,
                getArchives(), 
                getMain(), 
                //removing all chars from number - like whitespace, px and so on...
                new Integer(sanitizeSize(source.getAttribute("width"))),
                new Integer(sanitizeSize(source.getAttribute("height"))),
                createParams());
    }

    private URL createCodebase() throws ParseException, MalformedURLException {
        String inHtmlCodebase = source.getAttribute("codebase");
        if (inHtmlCodebase != null && inHtmlCodebase.trim().isEmpty()) {
            inHtmlCodebase = ".";
        }
        URL u = Parser.getURL(inHtmlCodebase, "In html " + source.getNodeName() + "'s codebase", docBase, false);
        if (!u.toExternalForm().endsWith("/")) {
            u = new URL(u.toExternalForm() + "/");
        }
        return u;
    }

    private PluginParameters createParams() {
        Map<String, String> data = new HashMap<>();
        List<Element> found = findParams();
        for (Element element : found) {
            data.put(element.getAttribute("name"), element.getAttribute("value"));
        }
        //strange compatibility issue
        if (data.get("jnlp_href") == null) {
            if (data.get("code") == null && data.get("object") == null) {
                data.put("code", source.getAttribute("code"));
            }
        }
        //push all attributes to map
        NamedNodeMap atts = source.getAttributes();
        for (int i = 0; i < atts.getLength(); i++) {
            String name = atts.item(i).getNodeName();
            String value = atts.item(i).getTextContent();
            if (name.trim().equalsIgnoreCase("width")
                    || name.trim().equalsIgnoreCase("height")) {
                value = sanitizeSize(value);
            }
            data.put(name, value);
        }
        return new PluginParameters(data);
    }

    private List<Element> findParams() {
        return AppletExtractor.findElements(PARAMS, source, new ElementValidator() {
            @Override
            public boolean isElementValid(Element e) {
                return true;
            }
        });
    }

    private String getMain() {
        String s = source.getAttribute("code");
        if (s == null || s.trim().length() == 0) {
            List<Element> found = findParams();
            for (Element element : found) {
                if (element.getAttribute("name").equalsIgnoreCase("code")) {
                    s = element.getAttribute("value");
                    break;
                }
            }
        }
        if (s == null || s.trim().length() == 0) {
            s = source.getAttribute("classid");
            if (s != null && s.contains(":")) {
                s = s.split(":")[1];
            }
        }
        return s;
    }

    private String getArchives() {
        String s = source.getAttribute("archive");
        if (s == null || s.trim().length() == 0) {
            List<Element> found = findParams();
            for (Element element : found) {
                if (element.getAttribute("name").equalsIgnoreCase("archive")) {
                    s = element.getAttribute("value");
                    break;
                }
            }
        }
        return s;
    }

    static String sanitizeSize(String attribute) {
        if (attribute == null) {
            return "1";
        }
        //remove all nondigits
        return attribute.replaceAll("[^0-9]+", "");
    }

}
