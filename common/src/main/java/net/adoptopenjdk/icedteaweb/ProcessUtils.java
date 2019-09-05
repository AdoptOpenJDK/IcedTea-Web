/* 
Copyright (C) 2013 Red Hat, Inc.

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

package net.adoptopenjdk.icedteaweb;

import net.adoptopenjdk.icedteaweb.io.IOUtils;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class ProcessUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessUtils.class);

    /**
     * This should be workaround for https://en.wikipedia.org/wiki/Spurious_wakeup which real can happen in case of processes.
     * See http://mail.openjdk.java.net/pipermail/distro-pkg-dev/2015-June/032350.html thread
     *
     * @param process process to be waited for
     */
    public static void waitForSafely(final Process process) {
        Objects.requireNonNull(process);
        boolean pTerminated = false;
        while (!pTerminated) {
            try {
                process.waitFor();
            } catch (final InterruptedException e) {
                LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
            }
            try {
                process.exitValue();
                pTerminated = true;
            } catch (final IllegalThreadStateException e) {
                LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
            }
        }
    }

    /**
     * Actively ignore the content of stdout and stderr.
     * This happens in a separate thread as on windows the process will not terminate if a buffer is full.
     * Reading in a separate thread ensures that the buffer is constantly drained.
     *
     * @param process the process to drain stdout and stderr
     */
    public static void ignoreStdOutAndStdErr(final Process process) {
        ignoreStream(process.getInputStream());
        ignoreStream(process.getErrorStream());
    }

    private static void ignoreStream(final InputStream in) {
        new Thread(() -> {
            try {
                final byte[] buffer = new byte[1024];
                while (in.read(buffer) > 0) {
                    // do nothing;
                }
            } catch (IOException e) {
                LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
                throw new RuntimeException(e);
            }
        }).start();
    }

    /**
     * Reads the content of the stdout into a future string.
     * This happens in a separate thread as on windows the process will not terminate if the out buffer is full.
     * Reading in a separate thread ensures that the buffer is constantly drained.
     * The future will complete when the process has exited.
     *
     * @param process the process to read the stdout from
     * @return a future holding the content of the stdout
     */
    public static Future<String> readStdOutAsUtf8(final Process process) {
        return readStreamAsUtf8(process.getInputStream());
    }

    /**
     * Reads the content of the stderr into a future string.
     * This happens in a separate thread as on windows the process will not terminate if the err buffer is full.
     * Reading in a separate thread ensures that the buffer is constantly drained.
     * The future will complete when the process has exited.
     *
     * @param process the process to read the stderr from
     * @return a future holding the content of the stderr
     */
    public static Future<String> readStdErrAsUtf8(final Process process) {
        return readStreamAsUtf8(process.getErrorStream());
    }

    private static Future<String> readStreamAsUtf8(final InputStream in) {
        final CompletableFuture<String> result = new CompletableFuture<>();
        new Thread(() -> {
            try {
                final String content = IOUtils.readContentAsUtf8String(in);
                result.complete(content);
            } catch (IOException e) {
                LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
                throw new RuntimeException(e);
            }
        }).start();
        return result;
    }
}
