package sun.applet;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.Callable;

import net.sourceforge.jnlp.AsyncCall;
import net.sourceforge.jnlp.ServerAccess;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sun.applet.mock.PluginPipeMock;

public class PluginAppletViewerTest {

    /**************************************************************************
     *                          Test setup                                    *
     **************************************************************************/

    ThreadGroup spawnedForTestThreadGroup; // Set up before each test
    PluginPipeMock pipeMock; // Set up before each test

    /* By providing custom implementations of the input stream & output stream used by PluginStreamHandler,
     * we are able to mock the C++-side of the plugin. We do this by sending the messages the Java-side expects
     * to receive. Additionally, we able to test that the Java-side sends the correct requests.
     * See PluginPipeMock for more details.
     */
    private void installPipeMock() {
        AppletSecurityContextManager.addContext(0, new PluginAppletSecurityContext(0, false /* no security */));

        pipeMock = new PluginPipeMock();

        PluginStreamHandler streamHandler = new PluginStreamHandler(pipeMock.getResponseInputStream(), pipeMock.getRequestOutputStream());
        PluginAppletViewer.setStreamhandler(streamHandler);
        PluginAppletViewer.setPluginCallRequestFactory(new PluginCallRequestFactory());

        streamHandler.startProcessing();
    }

    /* Call installPipeMock, wrapping the threads it creates in a ThreadGroup.
     * This allows us to stop the message handling threads we spawn, while normally
     * this would be difficult as they are meant to be alive at all times.
     */
    @Before
    public void setupMockedMessageHandling() throws Exception {
        spawnedForTestThreadGroup = new ThreadGroup("PluginAppletViewerTestThreadGroup") {
            public void uncaughtException(Thread t, Throwable e) {
                // Silent death for plugin message handler threads
            }
        };
        // Do set-up in a thread so we can pass along our thread-group, used for clean-up.
        Thread initThread = new Thread(spawnedForTestThreadGroup, "InstallPipeMockThread") {
            @Override
            public void run() {
                installPipeMock();
            }
        };
        initThread.start();
        initThread.join();
    }

    @After
    @SuppressWarnings("deprecation") // 'stop' must be used, 'interrupt' is too gentle.
    public void cleanUpMessageHandlingThreads() throws Exception {
        spawnedForTestThreadGroup.stop();
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
                + " JavaScriptCall " + storeObject(expectedReturn));

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
                + " JavaScriptEval " + storeObject(expectedReturn));

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
                + " JavaScriptToString " + storeObject(expectedReturn));

        assertEquals(expectedReturn, call.join());
    }

   /**************************************************************************
    *                          Test utilities                                *
    **************************************************************************/

    /*
     * Helpers for manipulating the object mapping using to refer to objects in
     * the plugin
     */
    private static Object getStoredObject(int id) {
        return PluginObjectStore.getInstance().getObject(id);
    }

    private static int storeObject(Object obj) {
        PluginObjectStore.getInstance().reference(obj);
        return PluginObjectStore.getInstance().getIdentifier(obj);
    }

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
        assertEquals(stringArg, getStoredObject(Integer.parseInt(parts[6])));

        for (int i = 0; i < arguments.length; i++) {
            int objectID = Integer.parseInt(parts[7+i]);
            assertEquals(arguments[i], getStoredObject(objectID));
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