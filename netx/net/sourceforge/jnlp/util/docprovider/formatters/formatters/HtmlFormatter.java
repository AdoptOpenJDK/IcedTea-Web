/* 
   Copyright (C) 2014 Red Hat, Inc.

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

package net.sourceforge.jnlp.util.docprovider.formatters.formatters;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import net.sourceforge.jnlp.runtime.JNLPRuntime;


public class HtmlFormatter extends ReplacingTextFormatter {

    private final Map<String, String> content = new TreeMap<>();
    private final boolean allowContext;
    private final boolean allowLogo;
    private final boolean includeXmlHeader;
    public  static final String SUFFIX = "html";

    @Override
    public String process(String s) {
        //the texts in properties are already using html markup
        return s;
    }

    
    public HtmlFormatter(boolean allowContext, boolean allowLogo, boolean includeXmlHeader) {
        this.allowContext = allowContext;
        this.allowLogo = allowLogo;
        this.includeXmlHeader = includeXmlHeader;
    }

    public HtmlFormatter() {
        this(true, true, false);
    }

    @Override
    public String getNewLine() {
        return "<BR/>" + PlainTextFormatter.getLineSeparator();
    }

    @Override
    public String getBoldOpening() {
        return "<B>";
    }

    @Override
    public String getBoldClosing() {
        return "</B>";
    }

    @Override
    public String getTitle(String s) {
        return " <a name=\"" + s + "\"/><H3>" + knownIdToString(s) + "</H3>\n";
    }

    @Override
    public String getHeaders(String id, String encoding) {
        //jeditorpane dont like <? declaration
        String xml = "";
        if (includeXmlHeader) {
            xml = "<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>\n";
        }
        return xml + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n"
                + "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\">\n"
                + "  <head>\n"
                + "    <title>" + id + ": " + getVersion() + "</title>"
                + "    <meta http-equiv=\"content-type\" content=\"application/xhtml+xml; charset=" + encoding + "\" />\n"
                + "  </head>\n"
                + "  <body>\n"
                + "    <H5  align=\"right\">" + id + ": " + getVersion() + ", " + JNLPRuntime.getLocalisedTimeStamp(new Date()) + "</H5>";
    }

    @Override
    public String getTail() {
        return "  </body>\n</html>";
    }

    @Override
    public String getUrl(String url, String visible) {
        return "<a href=\"" + url + "\">" + visible + "</a>";
    }

    private String knownIdToString(String s) {
        String value = localizeTitle(s);
        String key = s;
        content.put(key, value);
        return value;
    }

    @Override
    public String getSeeAlso(String s) {
        return "<li>" + getUrl(s + getFileSuffix(), s) + "</li>\n";
    }

    public StringBuilder generateIndex() {
        if (!allowContext) {
            return new StringBuilder();
        }
        Set<Map.Entry<String, String>> set = content.entrySet();
        if (set.isEmpty()) {
            return new StringBuilder();
        }
        StringBuilder sb = new StringBuilder("<H4>" + "Context" + "</H4>");
        for (Map.Entry<String, String> entry : set) {
            sb.append("<li><a href=\"#" + entry.getKey() + "\">").append(entry.getValue()).append("</a></li>");
        }
        return sb;

    }

    @Override
    public String getFileSuffix() {
        return "."+SUFFIX;
    }

    @Override
    public String wrapParagraph(String s) {
        return "<p><blockquote>" + s + "</blockquote></p>";
    }

    @Override
    public String getBreakAndBold() {
        return getNewLine() + getBoldOpening();
    }

    @Override
    public String getCloseBoldAndBreak() {
        return getBoldClosing() + getNewLine();
    }

    @Override
    public String getBoldCloseNwlineBoldOpen() {
        return getBoldClosing() + getNewLine() + getBoldOpening();
    }

    public String generateLogo() {
        if (allowLogo) {
            return "<center><img src=\"itw_logo.png\" alt=\"IcedTea-Web Logo\" width=\"413\" height=\"240\"/></center>" + getNewLine();
        } else {
            return "";
        }
    }

    @Override
    public String getOption(String key, String value) {
        return "<li><b>" + key + " </b> - " + process(value) + "</li>";
    }

}
