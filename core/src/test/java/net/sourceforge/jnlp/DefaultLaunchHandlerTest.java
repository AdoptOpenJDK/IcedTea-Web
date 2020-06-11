/* DefaultLaunchHandlerTest.java
   Copyright (C) 2012 Red Hat, Inc.

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

package net.sourceforge.jnlp;

import net.adoptopenjdk.icedteaweb.xmlparser.ParseException;
import net.sourceforge.jnlp.util.logging.NoStdOutErrTest;
import net.sourceforge.jnlp.util.logging.OutputController;
import net.sourceforge.jnlp.util.logging.StdInOutErrController;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.sourceforge.jnlp.LaunchException.SEVERE;
import static org.junit.Assert.assertEquals;

public class DefaultLaunchHandlerTest extends NoStdOutErrTest {

    private final OutputController l = OutputController.getLogger();

    private ByteArrayOutputStream out;
    private ByteArrayOutputStream err;

    @Before
    public final void setUp() {
        out = new ByteArrayOutputStream();
        err = new ByteArrayOutputStream();
        l.setInOutErrController(new StdInOutErrController(out, err));
    }

    @Test
    public void testBasicLaunch() throws Exception {
        DefaultLaunchHandler handler = new DefaultLaunchHandler(l);

        // all no-ops with no output
        handler.launchInitialized(null);
        handler.launchStarting(null);
        handler.launchCompleted(null);

        assertEquals("", out.toString(UTF_8.name()));
        assertEquals("", err.toString(UTF_8.name()));
    }

    @Test
    @Ignore //Test must be restructured. Checking Logging output is not the best idea...
    public void testLaunchWarning() throws Exception {
        DefaultLaunchHandler handler = new DefaultLaunchHandler(l);

        LaunchException warning = new LaunchException(null, null,
                SEVERE, "warning type", "test warning", "this is a test of the warning");
        handler.handleLaunchWarning(warning);

        assertEquals("netx: warning type: test warning\n", out.toString(UTF_8.name()));
    }

    @Test
    @Ignore //Test must be restructured. Checking Logging output is not the best idea...
    public void testLaunchError() throws Exception {
        DefaultLaunchHandler handler = new DefaultLaunchHandler(l);

        LaunchException error = new LaunchException(null, null,
                SEVERE, "error type", "test error", "this is a test of the error");
        handler.handleLaunchError(error);

        assertEquals("netx: error type: test error\n", out.toString(UTF_8.name()));
    }

    @Test
    @Ignore //Test must be restructured. Checking Logging output is not the best idea...
    public void testLaunchErrorWithCause() throws Exception {
        DefaultLaunchHandler handler = new DefaultLaunchHandler(l);

        ParseException parse = new ParseException("no information element");
        LaunchException error = new LaunchException(null, parse,
                SEVERE, "error type", "test error", "this is a test of the error");
        handler.handleLaunchError(error);

        assertEquals("netx: error type: test error (no information element)\n", out.toString(UTF_8.name()));
    }

    @Test
    @Ignore //Test must be restructured. Checking Logging output is not the best idea...
    public void testLaunchErrorWithNestedCause() throws Exception {
        DefaultLaunchHandler handler = new DefaultLaunchHandler(l);

        ParseException parse = new ParseException("no information element");
        RuntimeException runtime = new RuntimeException("programmer made a mistake", parse);
        LaunchException error = new LaunchException(null, runtime,
                SEVERE, "error type", "test error", "this is a test of the error");
        handler.handleLaunchError(error);

        assertEquals("netx: error type: test error (programmer made a mistake (no information element))\n", out.toString(UTF_8.name()));
    }

    @Test
    @Ignore //Test must be restructured. Checking Logging output is not the best idea...
    public void testValidationError() throws Exception {
        DefaultLaunchHandler handler = new DefaultLaunchHandler(l);

        LaunchException error = new LaunchException(null, null,
                SEVERE, "validation-error type", "test validation-error", "this is a test of a validation error");
        handler.validationError(error);

        assertEquals("netx: validation-error type: test validation-error\n", out.toString(UTF_8.name()));
    }
}
