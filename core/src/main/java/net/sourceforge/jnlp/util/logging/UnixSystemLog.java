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
import net.adoptopenjdk.icedteaweb.resources.CachedDaemonThreadPoolProvider;
import net.sourceforge.jnlp.util.docprovider.TextsProvider;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;


public class UnixSystemLog implements SingleStreamLogger {

    private static final Logger LOG = LoggerFactory.getLogger(UnixSystemLog.class);

    private static final String PREAMBLE = "IcedTea-Web java error - for more info see itweb-settings debug options or console." +
            " See " + TextsProvider.ITW_BUGS + " for help.\nIcedTea-Web java error manual log:\n";

    private static final ExecutorService executor = CachedDaemonThreadPoolProvider.createDaemonThreadPool();

    @Override
    public void log(String message) {
        final String s = PREAMBLE + message;
        final String[] ss = s.split("\\R"); // split string into lines
        try {
            CompletableFuture<Void> f = CompletableFuture.completedFuture(null);
            for (String m : ss) {
                f = f.thenRunAsync(() -> logToSyslog(m), executor);
            }
        } catch (Exception ex) {
            LOG.error("Error while sending message to Unix system log", ex);
        }
    }

    private void logToSyslog(final String msg) {
        try {
            final String message = msg.replaceAll("\t", "    ");
            ProcessBuilder pb = new ProcessBuilder("logger", "-p", "user.err", "--", message);
            Process p = pb.start();
            ProcessUtils.waitForSafely(p);
            LOG.debug("System logger called with result of {}", p.exitValue());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        // nothing to close
    }
}
