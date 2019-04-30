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

import net.sourceforge.jnlp.runtime.JNLPRuntime;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class HtmlFormatter extends ReplacingTextFormatter {

    private final Map<String, String> content = new TreeMap<>();
    private final boolean allowContext;
    private final boolean allowLogo;
    private final boolean includeXmlHeader;
    public static final String SUFFIX = "html";

    @Override
    public String process(final String s) {
        //the texts in properties are already using html markup
        return s;
    }

    public HtmlFormatter(final boolean allowContext, final boolean allowLogo, final boolean includeXmlHeader) {
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
    public String getTitle(final String s) {
        return " <a name=\"" + s + "\"/><H3>" + knownIdToString(s) + "</H3>\n";
    }

    @Override
    public String getHeaders(final String id, final Charset encoding) {
        //jeditorpane doesn't like <? declaration
        String xml = "";
        if (includeXmlHeader) {
            xml = "<?xml version=\"1.0\" encoding=\"" + encoding.name() + "\"?>\n";
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
    public String getUrl(final String url, final String visible) {
        return "<a href=\"" + url + "\">" + visible + "</a>";
    }

    private String knownIdToString(final String s) {
        final String value = localizeTitle(s);
        content.put(s, value);
        return value;
    }

    @Override
    public String getSeeAlso(final String s) {
        return "<li>" + getUrl(s + getFileSuffix(), s) + "</li>\n";
    }

    public StringBuilder generateIndex() {
        if (!allowContext) {
            return new StringBuilder();
        }
        final Set<Map.Entry<String, String>> set = content.entrySet();
        if (set.isEmpty()) {
            return new StringBuilder();
        }
        final StringBuilder sb = new StringBuilder("<H4>" + "Context" + "</H4>");
        for (final Map.Entry<String, String> entry : set) {
            sb.append("<li><a href=\"#").append(entry.getKey()).append("\">").append(entry.getValue()).append("</a></li>");
        }
        return sb;

    }

    @Override
    public String getFileSuffix() {
        return "." + SUFFIX;
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
    public String getOption(final String key, final String value) {
        return "<li><b>" + key + " </b> - " + process(value) + "</li>";
    }

    @Override
    public String getAddressLink(final String s) {
        final String emailDelBracket = s.replaceAll(".*<", "");
        final String address = emailDelBracket.replaceAll(">.*", "");
        if (s.contains("@")) {
            final String name = s.replaceAll("<.*", "").trim();
            return "<a href=\"mailto:" + antiSpam(address) + "\" target=\"_top\">" + name + "</a>";
        } else {
            return s.replaceAll("<.*>", "<a href=\"" + address + "\">" + address + "</a>");
        }
    }

    @Override
    public String replaceLtGtCharacters(final String s) {
        final String lT = "&#60";
        final String replaceLt = s.replaceAll("<", lT);
        final String gT = "&#62";
        return replaceLt.replaceAll(">", gT);
    }

    private static String antiSpam(final String address) {
        final StringBuilder sb = new StringBuilder();
        for (int x = 0; x < address.length(); x++) {
            sb.append(address.charAt(x)).append(" ");
        }
        return sb.toString().trim();
    }
}
