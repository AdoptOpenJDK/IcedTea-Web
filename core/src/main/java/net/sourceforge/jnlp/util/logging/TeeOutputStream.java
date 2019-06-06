/* TeeOutputStream.java
   Copyright (C) 2010 Red Hat

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

package net.sourceforge.jnlp.util.logging;


import net.sourceforge.jnlp.util.docprovider.formatters.formatters.PlainTextFormatter;
import net.sourceforge.jnlp.util.logging.headers.Header;
import net.sourceforge.jnlp.util.logging.headers.JavaMessage;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Behaves like the 'tee' command, sends output to both actual std stream and a
 * log
 */
public final class TeeOutputStream extends PrintStream implements SingleStreamLogger{

    // Everything written to TeeOutputStream is written to our log too
    private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private final boolean isError;
    private final BasicOutputController outputController;

    public TeeOutputStream(PrintStream stdStream, boolean isError) {
        this(stdStream, isError, OutputController.getLogger());
    }

    TeeOutputStream(PrintStream stdStream, boolean isError, BasicOutputController outputController) {
        super(stdStream);
        this.isError = isError;
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
        appendByteArray(b, off, len);
        super.write(b, off, len);
    }

    @Override
    public synchronized void write(int b) {
        appendByte(b);
        super.write(b);
    }

    private void flushLog() {
        String s = byteArrayOutputStream.toString();
        if (s.length() > 0) {
            log(s);
            byteArrayOutputStream.reset();
        }
    }

    @Override
    public void log(String s) {
        JavaMessage  jm = new JavaMessage(new Header(getLevel(), true), s);
        outputController.log(jm);
    }

    private boolean isError() {
        return isError;
    }

    private void appendByte(int b) {
        byteArrayOutputStream.write(b);
        String s = byteArrayOutputStream.toString();
        if (s.endsWith(PlainTextFormatter.getLineSeparator())) {
            flushLog();
        }
    }

    private void appendByteArray(byte[] b, int off, int len) {
        byteArrayOutputStream.write(b, off, len);
        String s = new String(b, off, len);
        if (s.endsWith(PlainTextFormatter.getLineSeparator())) {
            flushLog();
        }
    }

    private OutputControllerLevel getLevel() {
        if (isError()) {
            return OutputControllerLevel.ERROR_ALL;
        } else {
            return OutputControllerLevel.MESSAGE_ALL;
        }
    }
}
