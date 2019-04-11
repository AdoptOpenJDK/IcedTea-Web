package net.adoptopenjdk.icedteaweb.testing.tools;


import net.adoptopenjdk.icedteaweb.testing.ContentReaderListener;
import net.adoptopenjdk.icedteaweb.testing.ServerAccess;
import org.junit.Assert;

import java.util.List;

/**
 * You can see ClipboardContext reproducers as examples
 * 
 */

public class WaitingForStringProcess implements ContentReaderListener, Runnable {
    private final boolean headless;
    private final String url;
    private final StringBuilder output = new StringBuilder();
    private final StringBuilder err = new StringBuilder();
    private AsyncJavaws aj;
    final ContentReaderListener errListener = new ContentReaderListener() {
        @Override
        public void charReaded(char ch) {
            err.append(ch);
        }

        @Override
        public void lineReaded(String s) {
        }
    };
    private final String waitingFor;
    private boolean canRun = true;
    private final ServerAccess server;
    private final List<String> otherArgs;

    public WaitingForStringProcess(ServerAccess server, String url, List<String> otherArgs, boolean headless, String waitingFor) {
        this.url = url;
        this.headless = headless;
        this.waitingFor = waitingFor;
        Assert.assertNotNull(waitingFor);
        Assert.assertNotNull(url);
        this.server = server;
        this.otherArgs = otherArgs;
        Assert.assertNotNull(server);
    }

    @Override
    public void charReaded(char ch) {
        output.append(ch);
    }

    @Override
    public void lineReaded(String s) {
        if (s.contains(waitingFor)) {
            canRun = false;
        }
    }

    @Override
    public void run() {
        aj = new AsyncJavaws(server, url, otherArgs, headless, this, errListener);
        ServerAccess.logOutputReprint("Starting thread with " + url + " and waiting for result or string " + waitingFor);
        new Thread(aj).start();
        while (canRun && aj.getResult() == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                ServerAccess.logErrorReprint("iteration interrupted");
                throw new RuntimeException(ex);
            }
        }
        if (aj.getResult() != null) {
            ServerAccess.logOutputReprint("Waiting done. Result have been delivered");
        }
        if (!canRun) {
            ServerAccess.logOutputReprint("Waiting done. String " + waitingFor + " delivered");
        }
    }


    public String getOutput() {
        return output.toString();
    }
}
