/* TestsLogs.java
Copyright (C) 2011,2012 Red Hat, Inc.

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

package net.adoptopenjdk.icedteaweb.testing;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

class TestsLogs {

    private final List<LogItem> outs = new LinkedList<>();
    private final List<LogItem> errs = new LinkedList<>();
    private final List<LogItem> all = new LinkedList<>();
    private static final String LOG_ELEMENT = "log";
    private static final String LOG_ID_ATTRIBUTE = "id";

    synchronized void add(final boolean err, final boolean out, final String text) {
        LogItem li = new LogItem(Optional.ofNullable(text).orElse("null"));
        if (out) {
            outs.add(li);
        }
        if (err) {
            errs.add(li);
        }
        all.add(li);

    }

    @Override
    public String toString() {
        final StringBuilder sb = listToStringBuilder(outs, "out");
        sb.append(listToStringBuilder(errs, "err"));
        sb.append(listToStringBuilder(all, "all"));
        return sb.toString();
    }

    private StringBuilder listToStringBuilder(final List<LogItem> l, final String id) {
        final StringBuilder sb = new StringBuilder();
        sb.append("<" + LOG_ELEMENT + " " + LOG_ID_ATTRIBUTE + "=\"").append(id).append("\">\n");
        int i = 0;
        for (final LogItem logItem : l) {
            i++;
            sb.append(logItem.toStringBuilder(i));
        }
        sb.append("</" + LOG_ELEMENT + ">\n");
        return sb;
    }
}
