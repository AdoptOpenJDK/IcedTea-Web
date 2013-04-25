package net.sourceforge.jnlp;

import java.util.concurrent.Callable;

/**
 * A call that runs on a separate thread, with an optional timeout. It takes a runnable and allows
 * joining. 
 * 
 * On join, throws any exceptions that occurred within the call, or a TimeOutException if
 * it did not finish. Returns the value from the call.
 */
public class AsyncCall<T> {
    static public class TimeOutException extends RuntimeException  {
        public TimeOutException() {
            super("Call did not finish within the allocated time.");
        }
    }

    private Thread handler;
    private Callable<T> callable;
    private long timeout;
    private T callResult;

    /* Captures exception from async call */
    private Exception asyncException = null;

    /* Create an AsyncCall with a given time-out */
    public AsyncCall(Callable<T> callable, long timeout) {
        this.callable = callable;
        this.timeout = timeout;
        this.handler = new HandlerThread();
    }

    /* Create an AsyncCall with (effectively) no time-out */
    public AsyncCall(Callable<T> call) {
        this(call, Long.MAX_VALUE);
    }

    /* Chains construction + start for convenience */
    public static <T> AsyncCall<T> startWithTimeOut(Callable<T> callable, long timeout) {
        AsyncCall<T> asyncCall = new AsyncCall<T>(callable, timeout);
        asyncCall.start();
        return asyncCall;
    }

    /* Chains construction + start for convenience */
    public static <T> AsyncCall<T> startWithTimeOut(Callable<T> callable) {
        return startWithTimeOut(callable, 1000); // Default timeout of 1 second
    }

    public void start() {
        this.handler.start();
    }

    // Rethrows exceptions from handler thread, and throws TimeOutException in case of time-out.
    public T join() throws Exception {
        handler.join();
        if (asyncException != null) {
            throw asyncException;
        }
        return callResult;
    }

    /* The handler thread is responsible for timing-out the Callable thread.
     * The resulting thread */
    private class HandlerThread extends Thread {
        @Override
        public void run() {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        /* Capture result of the call */
                        callResult = callable.call();
                    } catch (Exception e) {
                        /* In case of exception, capture for re-throw */
                        asyncException = e;
                    }
                    handler.interrupt(); // Finish early
                }
            };

            thread.start();

            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                // Finish early
                return;
            }

            if (thread.isAlive()) {
                asyncException = new TimeOutException();
            }

            // Make sure the thread is finished
            while (thread.isAlive()) {
                thread.interrupt();
            }
        }
    }
}
