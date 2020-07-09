/* TeeOutputStream.java
   Copyright (C) 2010 Red Hat

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


import net.sourceforge.jnlp.util.docprovider.formatters.formatters.PlainTextFormatter;
import net.sourceforge.jnlp.util.logging.headers.Header;
import net.sourceforge.jnlp.util.logging.headers.JavaMessage;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static net.sourceforge.jnlp.util.logging.OutputControllerLevel.ERROR_ALL;
import static net.sourceforge.jnlp.util.logging.OutputControllerLevel.MESSAGE_ALL;

/**
 * Behaves like the 'tee' command, sends output to both actual std stream and a
 * log
 */
public final class TeeOutputStream extends PrintStream {

    private static final String LINE_SEPARATOR = PlainTextFormatter.getLineSeparator();

    // Everything written to TeeOutputStream is written to our log too
    private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private final OutputControllerLevel level;
    private final BasicOutputController outputController;

    public TeeOutputStream(PrintStream stdStream, boolean isError) {
        this(stdStream, isError, OutputController.getLogger());
    }

    TeeOutputStream(PrintStream stdStream, boolean isError, BasicOutputController outputController) {
        super(stdStream);
        this.level = isError ? ERROR_ALL : MESSAGE_ALL;
        this.outputController = outputController;
    }

    @Override
    public void close() {
        flushLog();
        super.close();
    }

    @Override
    public void flush() {
        flushLog();
        super.flush();
    }

    /*
     * The big ones: these do the actual writing
     */

    @Override
    public synchronized void write(byte[] b, int off, int len) {
        if (len == 0) {
            return;
        }

        byteArrayOutputStream.write(b, off, len);
        flushLogAtEndOfLine();

        super.write(b, off, len);
    }

    @Override
    public synchronized void write(int b) {
        byteArrayOutputStream.write(b);
        flushLogAtEndOfLine();

        super.write(b);
    }

    private void flushLog() {
        flushLog(true);
    }

    private void flushLogAtEndOfLine() {
        flushLog(false);
    }

    private void flushLog(boolean always) {
        final String s = byteArrayOutputStream.toString();
        if (s.length() > 0 && (always || s.endsWith(LINE_SEPARATOR))) {
            final JavaMessage jm = new JavaMessage(new Header(level, true), s);
            outputController.log(jm);
            byteArrayOutputStream.reset();
        }
    }
}
