/* DefaultLaunchHandlerTest.java
   Copyright (C) 2012 Red Hat, Inc.

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

package net.sourceforge.jnlp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;

public class DefaultLaunchHandlerTest {

    @Test
    public void testBasicLaunch() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DefaultLaunchHandler handler = new DefaultLaunchHandler(new PrintStream(baos));

        // all no-ops with no output
        handler.launchInitialized(null);
        handler.launchStarting(null);
        handler.launchCompleted(null);

        String output = baos.toString();
        assertEquals("", output);
    }

    @Test
    public void testLaunchWarning() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DefaultLaunchHandler handler = new DefaultLaunchHandler(new PrintStream(baos));

        LaunchException warning = new LaunchException(null, null,
                "severe", "warning type", "test warning", "this is a test of the warning");
        boolean continueLaunch = handler.launchWarning(warning);

        assertTrue(continueLaunch);
        String output = baos.toString();
        assertEquals("netx: warning type: test warning\n", output);
    }

    @Test
    public void testLaunchError() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DefaultLaunchHandler handler = new DefaultLaunchHandler(new PrintStream(baos));

        LaunchException error = new LaunchException(null, null,
                "severe", "error type", "test error", "this is a test of the error");
        handler.launchError(error);

        String output = baos.toString();
        assertEquals("netx: error type: test error\n", output);
    }

    @Test
    public void testLaunchErrorWithCause() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DefaultLaunchHandler handler = new DefaultLaunchHandler(new PrintStream(baos));

        ParseException parse = new ParseException("no information element");
        LaunchException error = new LaunchException(null, parse,
                "severe", "error type", "test error", "this is a test of the error");
        handler.launchError(error);

        String output = baos.toString();
        assertEquals("netx: error type: test error (no information element)\n", output);
    }

    @Test
    public void testLaunchErrorWithNestedCause() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DefaultLaunchHandler handler = new DefaultLaunchHandler(new PrintStream(baos));

        ParseException parse = new ParseException("no information element");
        RuntimeException runtime = new RuntimeException("programmer made a mistake", parse);
        LaunchException error = new LaunchException(null, runtime,
                "severe", "error type", "test error", "this is a test of the error");
        handler.launchError(error);

        String output = baos.toString();
        assertEquals("netx: error type: test error (programmer made a mistake (no information element))\n", output);
    }


    @Test
    public void testValidationError() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DefaultLaunchHandler handler = new DefaultLaunchHandler(new PrintStream(baos));

        LaunchException error = new LaunchException(null, null,
                "severe", "validation-error type", "test validation-error", "this is a test of a validation error");
        handler.validationError(error);

        String output = baos.toString();
        assertEquals("netx: validation-error type: test validation-error\n", output);
    }
}
