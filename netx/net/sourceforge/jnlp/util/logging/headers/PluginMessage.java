/*
Copyright (C) 2009, 2013  Red Hat

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

IcedTea is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
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
exception statement from your version. */

package net.sourceforge.jnlp.util.logging.headers;

import java.util.Date;
import net.sourceforge.jnlp.util.logging.FileLog;
import net.sourceforge.jnlp.util.logging.OutputController;

public class PluginMessage  implements MessageWithHeader{

    public PluginHeader header;
    public String restOfMessage;
    public boolean wasError = false;

    public PluginMessage(String orig) {
        restOfMessage = orig;
        header = new PluginHeader();
        String s = orig.trim();
        PluginHeader p = this.header;
        try {
            p.isC = true;
            p.application = false;
            if (s.startsWith("preinit_plugin")) {
                p.preinit = true;
            }
            if (s.startsWith(PluginHeader.PLUGIN_DEBUG) || s.startsWith(PluginHeader.PLUGIN_DEBUG_PREINIT)) {
                p.level = OutputController.Level.MESSAGE_DEBUG;
            } else if (s.startsWith(PluginHeader.PLUGIN_ERROR) || s.startsWith(PluginHeader.PLUGIN_ERROR_PREINIT)) {
                p.level = OutputController.Level.ERROR_ALL;
            } else {
                p.level = OutputController.Level.WARNING_ALL;
            }
            String[] init = PluginHeader.whiteSpaces.split(s);
            p.originalTimeStamp = new Date(Long.parseLong(init[1]) / 1000);
            String[] main = PluginHeader.bracketsPattern.split(s);
            p.user = main[1];
            p.caller = main[5];
            p.date = FileLog.getPluginSharedFormatter().parse(main[4]);
            String[] threads = PluginHeader.threadsPattern.split(main[6]);
            p.thread1 = threads[2];
            p.thread2 = threads[4];
            int i = orig.indexOf(p.thread2);
            restOfMessage = orig.substring(i + p.thread2.length() + 2); //+": "
        } catch (Exception ex) {
            OutputController.getLogger().log(ex);
            this.wasError = true;
        }
    }

    @Override
    public String getMessage() {
        return restOfMessage;
    }

    @Override
    public Header getHeader() {
        return header;
    }
}
