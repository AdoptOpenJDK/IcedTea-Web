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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;

import static java.util.Objects.requireNonNull;

public class StdInOutErrController {
    private static final StdInOutErrController INSTANCE = new StdInOutErrController(System.out, System.err);

    private final PrintStreamLogger outLog;
    private final PrintStreamLogger errLog;
    /*stdin reader for headless dialogues*/
    private BufferedReader br;

    public static StdInOutErrController getInstance() {
        return INSTANCE;
    }

    /**
     * for testing purposes the logger with custom streams can be created
     * otherwise only getLogger()'s singleton can be called.
     */
    public StdInOutErrController(OutputStream out, OutputStream err) {
        this(new PrintStream(requireNonNull(out, "out")), new PrintStream(requireNonNull(err, "err")));
    }

    private StdInOutErrController(PrintStream out, PrintStream err) {
        outLog = new PrintStreamLogger(requireNonNull(out, "out"));
        errLog = new PrintStreamLogger(requireNonNull(err, "err"));
    }

    public PrintStreamLogger getOut() {
        return outLog;
    }

    /**
     * @return current stream for std.out reprint
     */
    public PrintStream getOutStream() {
        return outLog.getStream();
    }

    public PrintStreamLogger getErr() {
        return errLog;
    }

    public synchronized String readLine() throws IOException {
        if (br == null) {
            br = new BufferedReader(new InputStreamReader(System.in));
        }
        return br.readLine();
    }
}
