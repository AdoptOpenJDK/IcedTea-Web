/* FileLog.java
 Copyright (C) 2011, 2013 Red Hat, Inc.

 This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify it under the
terms of the GNU General Public License as published by the Free Software
Foundation, version 2.

IcedTea is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
IcedTea; see the file COPYING. If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is making a
combined work based on this library. Thus, the terms and conditions of the GNU
General Public License cover the whole combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent modules, and
to copy and distribute the resulting executable under terms of your choice,
provided that you also meet, for each linked independent module, the terms and
conditions of the license of that module. An independent module is a module
which is not derived from or based on this library. If you modify this library,
you may extend this exception to your version of the library, but you are not
obligated to do so. If you do not wish to do so, delete this exception
statement from your version. */
package net.sourceforge.jnlp.util.logging;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.StringUtils;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.os.OsUtil;
import net.sourceforge.jnlp.util.docprovider.TextsProvider;
import net.sourceforge.jnlp.util.logging.filelogs.LogBasedFileLog;
import net.sourceforge.jnlp.util.logging.filelogs.WriterBasedFileLog;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class is utility and factory around file logs.
 */
public final class FileLog {

    private static final Logger LOG = LoggerFactory.getLogger(FileLog.class);

    public static String LOG_PREFIX_ENV = "itwLogFilePrefix";
    public static String LOG_POSTFIX_ENV = "itwLogFilePostfix";

    private static final String DEFAULT_LOGGER_NAME = TextsProvider.ITW + " file-logger";
    private static final String TIME_SEPARATOR = OsUtil.isWindows() ? "_" : ":";
    private static final SimpleDateFormat FILE_LOG_NAME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd_HH" + TIME_SEPARATOR + "mm" + TIME_SEPARATOR + "ss.S");

    private static final String logFileNamePrefix;
    private static String logFileNamePostfix;

    static {
        final String envPrefix = System.getenv(LOG_PREFIX_ENV);
        if (!StringUtils.isBlank(envPrefix)) {
            logFileNamePrefix = envPrefix;
        } else {
            logFileNamePrefix = FILE_LOG_NAME_FORMATTER.format(new Date());
        }

        final String envPostfix = System.getenv(LOG_POSTFIX_ENV);
        if (!StringUtils.isBlank(envPostfix)) {
            logFileNamePostfix = envPostfix;
        } else {
            logFileNamePostfix = "itw";
        }
    }

    public static String getLogFileNamePrefix() {
        return logFileNamePrefix;
    }

    public static String getLogFileNamePostfix() {
        return logFileNamePostfix;
    }

    public static void setLogFileNamePostfix(String logFileNamePostfix) {
        FileLog.logFileNamePostfix = Assert.requireNonBlank(logFileNamePostfix, "logFileNamePostfix");
    }

    public static SingleStreamLogger createFileLog() {
        SingleStreamLogger s;
        try {
            final String fileName = getFileName();
            if (LogConfig.getLogConfig().isLegacyLogBasedFileLog()) {
                s = new LogBasedFileLog(DEFAULT_LOGGER_NAME, fileName, false);
            } else {
                s = new WriterBasedFileLog(fileName, false);
            }
            LOG.debug("Start logging into: {}", s);
        } catch (Exception ex) {
            LOG.error("Exception while creating FileLog", ex);
            // we do not wont to block whole logging just because initialization error in "new FileLog()"
            s = new DummyLogger();
        }
        return s;
    }

    private static String getFileName() {
        final String logDir = LogConfig.getLogConfig().getIcedteaLogDir();
        return logDir + (logFileNamePrefix + "-" + logFileNamePostfix + ".log");
    }
}
