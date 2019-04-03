package net.sourceforge.jnlp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.Callable;

import org.junit.Test;

public class AsyncCallTest {

    @Test
    public void timeOutTest() {
        final boolean[] wasInterrupted = { false };

        AsyncCall<Void> call = AsyncCall.startWithTimeOut(new Callable<Void>() {
            @Override
            public synchronized Void call() {
                try {
                    wait();
                } catch (InterruptedException ie) {
                    // Received on time-out
                    wasInterrupted[0] = true;
                }
                return null;
            }
        }, 100 /* 100 millisecond time-out */);

        boolean completedNormally = false;

        try {
            call.join();
            completedNormally = true;
        } catch (Exception e) {
            ServerAccess.logErrorReprint(e.toString());
            assertTrue(e instanceof AsyncCall.TimeOutException);
        }

        assertFalse(completedNormally);
        assertTrue(wasInterrupted[0]);
    }

    @Test
    public void normalReturnTest() {
        AsyncCall<Integer> call = AsyncCall.startWithTimeOut(new Callable<Integer>() {
            @Override
            public Integer call() {
                return 1;
            }
        });

        Integer result = null;
        boolean completedNormally = false;

        try {
            result = call.join();
            completedNormally = true;
        } catch (Exception e) {
            ServerAccess.logErrorReprint(e.toString());
        }

        assertTrue(completedNormally);
        assertEquals(Integer.valueOf(1), result);
    }

    @Test
    public void thrownExceptionTest() {

        @SuppressWarnings("serial")
        class TestException extends RuntimeException {
        }

        AsyncCall<Void> call = AsyncCall.startWithTimeOut(new Callable<Void>() {
            @Override
            public Void call() {
                throw new TestException();
            }
        });

        boolean completedNormally = false;

        try {
            call.join();
            completedNormally = true;
        } catch (Exception e) {
            ServerAccess.logErrorReprint(e.toString());
            assertTrue(e instanceof TestException);
        }

        assertFalse(completedNormally);
    }
}