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

package sun.applet;

import static org.junit.Assert.assertEquals;

import static sun.applet.PluginPipeMockUtil.getPluginStoreId;
import static sun.applet.PluginPipeMockUtil.getPluginStoreObject;

import java.util.concurrent.Callable;

import net.sourceforge.jnlp.AsyncCall;
import net.sourceforge.jnlp.ServerAccess;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sun.applet.mock.PluginPipeMock;
import sun.applet.PluginPipeMockUtil;

public class PluginAppletViewerTest {

    /**************************************************************************
     *                          Test setup                                    *
     **************************************************************************/

    PluginPipeMock pipeMock; // Set up before each test
    @Before
    public void setupMockedMessageHandling() throws Exception {
        pipeMock = PluginPipeMockUtil.setupMockedMessageHandling();
    }

    @After
    public void cleanUpMessageHandlingThreads() throws Exception {
        PluginPipeMockUtil.cleanUpMockedMessageHandling(pipeMock);
    }

    /**************************************************************************
     *                          Test cases                                    *
     *   A PluginStreamHandler is installed for each, see 'installPipeMock'.  *
     **************************************************************************/

    @Test
    public void testJavascriptCall() throws Exception {
        /* JS call parameters */
        final int jsObjectID = 0;
        final String callName = "testfunction";
        final Object[] arguments = { "testargument", 1 }; // Arbitrary objects

        AsyncCall<Object> call = AsyncCall.startWithTimeOut(new Callable<Object>() {
            public Object call() {
                return PluginAppletViewer.call(jsObjectID, callName, arguments);
            }
        });

        String message = pipeMock.getNextRequest();
        Object expectedReturn = new Object();
        pipeMock.sendResponse("context 0 reference " 
                + parseAndCheckJSCall(message, jsObjectID, callName, arguments)
                + " JavaScriptCall " + getPluginStoreId(expectedReturn));

        assertEquals(expectedReturn, call.join());
    }

    @Test
    public void testJavascriptEval() throws Exception {
        /* JS eval parameters */
        final int jsObjectID = 0;
        final String callName = "testfunction";

        AsyncCall<Object> call = AsyncCall.startWithTimeOut(new Callable<Object>() {
            public Object call() {
                return PluginAppletViewer.eval(jsObjectID, callName);
            }
        });

        String message = pipeMock.getNextRequest();
        Object expectedReturn = new Object();
        pipeMock.sendResponse("context 0 reference " 
                + parseAndCheckJSEval(message, jsObjectID, callName)
                + " JavaScriptEval " + getPluginStoreId(expectedReturn));

        assertEquals(expectedReturn, call.join());
    }

    @Test
    public void testJavascriptFinalize() throws Exception {
        final int jsObjectID = 0;
        AsyncCall<Void> call = AsyncCall.startWithTimeOut(new Callable<Void>() {
            public Void call() {
                PluginAppletViewer.JavaScriptFinalize(jsObjectID);
                return null;
            }
        });

        String message = pipeMock.getNextRequest();
        pipeMock.sendResponse("context 0 reference "
                + parseAndCheckJSFinalize(message, jsObjectID) 
                + " JavaScriptFinalize ");

        call.join();
    }

    @Test
    public void testJavascriptToString() throws Exception {
        final int jsObjectID = 0;
        AsyncCall<String> call = AsyncCall.startWithTimeOut(new Callable<String>() {
            public String call() {
                return PluginAppletViewer.javascriptToString(jsObjectID);
            }
        });

        String message = pipeMock.getNextRequest();

        String expectedReturn = "testreturn";
        pipeMock.sendResponse("context 0 reference " 
                + parseAndCheckJSToString(message, jsObjectID)
                + " JavaScriptToString " + getPluginStoreId(expectedReturn));

        assertEquals(expectedReturn, call.join());
    }

   /**************************************************************************
    *                          Test utilities                                *
    **************************************************************************/

    /*
     * Asserts that the message is a valid javascript request and returns the
     * reference number
     */
    private static int parseAndCheckJSMessage(String message, int messageLength,
        String messageType, int contextObjectID) {
        ServerAccess.logOutputReprint(message);
        String[] parts = message.split(" ");
        assertEquals(messageLength, parts.length);

        assertEquals("instance", parts[0]);
        assertEquals("0", parts[1]); // JSCall's are prefixed with a dummy '0' instance
        assertEquals("reference", parts[2]);
        int reference = Integer.parseInt(parts[3]);
        assertEquals(messageType, parts[4]);

        assertEquals(contextObjectID, Integer.parseInt(parts[5]));
        return reference;
    }

    /*
     * Asserts that the message is a valid javascript request and returns the
     * reference number
     */
    private static int parseAndCheckJSMessage(String message,
            String messageType, int contextObjectID, String stringArg,
            Object[] arguments) {
        int expectedLength = 7 + arguments.length;
        int reference = parseAndCheckJSMessage(message, expectedLength, messageType, contextObjectID);

        String[] parts = message.split(" ");
        assertEquals(stringArg, getPluginStoreObject(Integer.parseInt(parts[6])));

        for (int i = 0; i < arguments.length; i++) {
            int objectID = Integer.parseInt(parts[7+i]);
            assertEquals(arguments[i], getPluginStoreObject(objectID));
        }

        return reference;
    }

    /*
     * Asserts that the message is a valid javascript method call request, and
     * returns the reference number
     */
    public static int parseAndCheckJSCall(String message, int contextObjectID,
            String callName, Object[] arguments) {
        return parseAndCheckJSMessage(message, "Call", contextObjectID,
                callName, arguments);
    }

    /*
     * Asserts that the message is a valid javascript Eval request, and returns
     * the reference number
     */
    public static int parseAndCheckJSEval(String message, int contextObjectID,
            String evalString) {
        return parseAndCheckJSMessage(message, "Eval", contextObjectID,
                evalString, new Object[] {});
    }

    /*
     * Asserts that the message is a valid javascript Finalize request, and returns
     * the reference number
     */
    public static int parseAndCheckJSFinalize(String message, int contextObjectID) {
        int expectedLength = 6;
        return parseAndCheckJSMessage(message, expectedLength, "Finalize", contextObjectID);
    }

    /*
     * Asserts that the message is a valid javascript ToString request, and returns
     * the reference number
     */
    public static int parseAndCheckJSToString(String message, int contextObjectID) {
        int expectedLength = 6;
        return parseAndCheckJSMessage(message, expectedLength, "ToString", contextObjectID);
    }
}
