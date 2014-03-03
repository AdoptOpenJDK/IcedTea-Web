/*   Copyright (C) 2013 Red Hat, Inc.

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
package net.sourceforge.jnlp.security.appletextendedsecurity;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UnsignedAppletActionEntry {

    private ExecuteAppletAction unsignedAppletAction;
    private Date timeStamp;
    private UrlRegEx documentBase;
    private UrlRegEx codeBase;
    private List<String> archives;

    public static UnsignedAppletActionEntry createFromString(String s) {
        String[] split = s.split("\\s+");
        UnsignedAppletActionEntry nw = new UnsignedAppletActionEntry(
                ExecuteAppletAction.fromString(split[0]),
                new Date(new Long(split[1])),
                new UrlRegEx(split[2]),
                null,
                null);
        if (split.length > 3) {
            nw.setCodeBase(new UrlRegEx(split[3]));
        }
        if (split.length > 4) {
            nw.setArchives(createArchivesList(s.substring(s.lastIndexOf(split[3]) + split[3].length()).trim()));
        }
        return nw;
    }

    public UnsignedAppletActionEntry(ExecuteAppletAction unsignedAppletAction, Date timeStamp, UrlRegEx documentBase, UrlRegEx codeBase, List<String> archives) {
        this.unsignedAppletAction = unsignedAppletAction;
        this.timeStamp = timeStamp;
        this.documentBase = documentBase;
        this.codeBase = codeBase;
        this.archives = archives;

    }

    @Override
    public String toString() {
        return this.serializeToReadableAndParseableString();

    }

    public void write(Writer bw) throws IOException {
        bw.write(this.serializeToReadableAndParseableString());
    }

    private String serializeToReadableAndParseableString() {
        return unsignedAppletAction.toChar()
                + " " + ((timeStamp == null) ? "1" : timeStamp.getTime())
                + " " + ((documentBase == null) ? "" : documentBase.getRegEx())
                + " " + ((codeBase == null) ? "" : codeBase.getRegEx())
                + " " + createArchivesString(archives);
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public UrlRegEx getDocumentBase() {
        return documentBase;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setDocumentBase(UrlRegEx documentBase) {
        this.documentBase = documentBase;
    }

    public ExecuteAppletAction getUnsignedAppletAction() {
        return unsignedAppletAction;
    }

    public void setUnsignedAppletAction(ExecuteAppletAction unsignedAppletAction) {
        this.unsignedAppletAction = unsignedAppletAction;
    }

    public UrlRegEx getCodeBase() {
        return codeBase;
    }

    public void setCodeBase(UrlRegEx codeBase) {
        this.codeBase = codeBase;
    }

    public List<String> getArchives() {
        return archives;
    }

    public void setArchives(List<String> archives) {
        this.archives = archives;
    }

    public static String createArchivesString(List<String> listOfArchives) {
        if (listOfArchives == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < listOfArchives.size(); i++) {
            String string = listOfArchives.get(i);
            if (string.trim().isEmpty()) {
                continue;
            }
            sb.append(string);
            if (i != listOfArchives.size() - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    public static List<String> createArchivesList(String commedArchives) {
        if (commedArchives == null) {
            return null;
        }
        if (commedArchives.trim().isEmpty()) {
            return null;
        }
        String[] items = commedArchives.trim().split(",");
        List<String> r = new ArrayList<String>(items.length);
        for (int i = 0; i < items.length; i++) {
            String string = items[i];
            if (string.trim().isEmpty()) {
                continue;
            }
            r.add(string);

        }
        return r;

    }
}
