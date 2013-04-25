package sun.applet.mock;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Helper for getting an input & output stream for use with PluginStreamHandler.
 * Provides a convenient way of reading the Java requests and sending mocked
 * plugin responses.
 * 
 * The handling of these requests should be done on a different thread from the
 * tested method, as icedtea-web will block waiting for a reply after sending a
 * request.
 */
public class PluginPipeMock {
    private ResponseInputPipeMock responseInputStream = new ResponseInputPipeMock();
    private RequestOutputPipeMock requestOutputStream = new RequestOutputPipeMock();

    /*
     * A queue of mocked responses that are sent as replies to icedtea-web
     * Java-side requests.
     */
    private BlockingQueue<String> mockedResponseQueue = new LinkedBlockingQueue<String>();

    /*
     * A queue of actual (ie, not mocked) requests that come from methods
     * under test.
     */
    private BlockingQueue<String> requestQueue = new LinkedBlockingQueue<String>();

    public InputStream getResponseInputStream() {
        return responseInputStream;
    }

    public OutputStream getRequestOutputStream() {
        return requestOutputStream;
    }

    public String getNextRequest() {
        try {
            return requestQueue.take();
        } catch (InterruptedException e) {
            // Nothing to do
            return null;
        }
    }

    public void sendResponse(String response) {
        try {
            mockedResponseQueue.put(response);
        } catch (InterruptedException e) {
            // Nothing to do
        }
    }

    /**
     * Queues mocked responses and sends them as replies to icedtea-web. A
     * synchronized message queue is read from. Blocks until it gets the next
     * message.
     */
    private class ResponseInputPipeMock extends InputStream {
        private StringReader reader = null;

        @Override
        public int read() throws IOException {
            try {
                while (true) {
                    if (reader == null) {
                        reader = new StringReader(mockedResponseQueue.take() + '\n');
                    }
                    int chr = reader.read();
                    if (chr == -1) {
                        reader = null;
                        continue;
                    }
                    return chr;
                }
            } catch (InterruptedException e) {
                // Nothing to do
                return -1;
            }
        }

        /* Necessary for correct behaviour with BufferedReader! */
        @Override
        public int read(byte b[], int off, int len) throws IOException {
            if (len == 0) {
                return 0;
            }
            b[off] = (byte) read();
            return 1;
        }
    }

    /**
     * Outputs requests from icedtea-web as a stream of lines. A synchronized
     * message queue is written to.
     */
    private class RequestOutputPipeMock extends OutputStream {
        private StringBuilder lineBuffer = new StringBuilder();

        @Override
        public synchronized void write(int b) throws IOException {
            try {
                char chr = (char) b;
                if (chr == '\0') {
                    requestQueue.put(lineBuffer.toString());
                    lineBuffer.setLength(0);
                } else {
                    lineBuffer.append((char) b);
                }
            } catch (InterruptedException e) {
                // Nothing to do
            }
        }
    }
}
