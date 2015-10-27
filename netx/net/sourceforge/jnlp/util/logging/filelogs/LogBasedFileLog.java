/*
 Copyright (C) 2011, 2013 Red Hat, Inc.

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
package net.sourceforge.jnlp.util.logging.filelogs;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import net.sourceforge.jnlp.util.FileUtils;
import net.sourceforge.jnlp.util.logging.FileLog;
import net.sourceforge.jnlp.util.logging.SingleStreamLogger;

/**
 * This class writes log information to file.
 */
public final class LogBasedFileLog implements SingleStreamLogger {

    //really instance bounded
    private final Logger impl;
    private final FileHandler fh;

    // testing constructor 
    public LogBasedFileLog(String fileName, boolean append) {
        this(fileName, fileName, append);
    }

    public LogBasedFileLog(String loggerName, String fileName, boolean append) {
        try {
            File futureFile = new File(fileName);
            if (!futureFile.exists()) {
                FileUtils.createRestrictedFile(futureFile, true);
            }
            fh = new FileHandler(fileName, append);
            fh.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord record) {
                    return record.getMessage() + "\n";
                }
            });
            impl = Logger.getLogger(loggerName);
            impl.setLevel(Level.ALL);
            impl.addHandler(fh);
            log(FileLog.getHeadlineHeader().toString() + " log-based impl.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Log the String to file.
     *
     * @param s {@link Exception} that was thrown.
     */
    @Override
    public synchronized void log(String s) {
        impl.log(Level.FINE, s);
    }

    @Override
    public void close() {
        fh.close();
    }

}
