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

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.util.logging.OutputControllerLevel;

import java.util.Date;

import static net.sourceforge.jnlp.util.logging.OutputControllerLevel.WARNING_ALL;

public class PluginMessage implements MessageWithHeader {

    private static final Logger LOG = LoggerFactory.getLogger(PluginMessage.class);

    public final PluginHeader header;
    public final String restOfMessage;
    public final boolean wasError;

    public PluginMessage(final String orig) {
        boolean wasError;
        String restOfMessage;
        PluginHeader header;
        try {
            final String s = orig.trim();
            final boolean preInit = s.startsWith("preinit_plugin");
            final String[] init = PluginHeader.whiteSpaces.split(s);
            final String[] main = PluginHeader.bracketsPattern.split(s);

            final Date timestamp = new Date(Long.parseLong(init[1]) / 1000);
            final String user = main[1];
            final String date = main[4];
            final String caller = main[5];
            final String[] threads = PluginHeader.threadsPattern.split(main[6]);
            final String thread1 = threads[2];
            final String thread2 = threads[4];
            final OutputControllerLevel level = getLevel(s);

            wasError = false;
            restOfMessage = orig.substring(orig.indexOf(thread2) + thread2.length() + 2); //+": "
            header = new PluginHeader(level, timestamp, date, user, caller, thread1, thread2, preInit);
        } catch (final Exception ex) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
            wasError = true;
            restOfMessage = orig;
            header = new PluginHeader();
        }

        this.wasError = wasError;
        this.restOfMessage = restOfMessage;
        this.header = header;
    }

    private static OutputControllerLevel getLevel(final String s) {
        if (s.startsWith(PluginHeader.PLUGIN_DEBUG) || s.startsWith(PluginHeader.PLUGIN_DEBUG_PREINIT)) {
            return OutputControllerLevel.MESSAGE_DEBUG;
        } else if (s.startsWith(PluginHeader.PLUGIN_ERROR) || s.startsWith(PluginHeader.PLUGIN_ERROR_PREINIT)) {
            return OutputControllerLevel.ERROR_ALL;
        } else {
            return WARNING_ALL;
        }
    }

    @Override
    public Header getHeader() {
        return header;
    }

    @Override
    public String getMessage() {
        return restOfMessage;
    }

    @Override
    public boolean hasStackTrace() {
        return false;
    }

    @Override
    public String getStackTrace() {
        return null;
    }
}
