/*Copyright (C) 2013 Red Hat, Inc.

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
statement from your version.
*/

package net.sourceforge.jnlp.util.logging;

import net.adoptopenjdk.icedteaweb.ProcessUtils;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.util.docprovider.TextsProvider;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import static net.adoptopenjdk.icedteaweb.resources.DaemonThreadPoolProvider.createSingletonDaemonThreadPool;


public class UnixSystemLog implements SingleStreamLogger {

    private static final Logger LOG = LoggerFactory.getLogger(UnixSystemLog.class);

    private static final String PREAMBLE = "IcedTea-Web java error - for more info see itweb-settings debug options or console." +
            " See " + TextsProvider.ITW_BUGS + " for help.\nIcedTea-Web java error manual log:\n";
    private final ExecutorService singleExecutor = createSingletonDaemonThreadPool();

    @Override
    public void log(final String message) {
        singleExecutor.submit(() -> logToSyslog(message));
    }

    private void logToSyslog(final String message) {
        final String s = PREAMBLE + message;
        final String[] messages = s.split("\\R"); // split string into lines
        for (String msg : messages) {
            try {
                final String m = msg.replaceAll("\t", "    ");
                final ProcessBuilder pb = new ProcessBuilder("logger", "-p", "user.err", "--", m);
                final Process p = pb.start();
                ProcessUtils.waitForSafely(p);
                LOG.debug("System logger called with result of {}", p.exitValue());
            } catch (IOException e) {
                LOG.error("Error while sending message to Unix system log", e);
            }
        }
    }

    @Override
    public void close() {
        singleExecutor.shutdown();
    }
}
