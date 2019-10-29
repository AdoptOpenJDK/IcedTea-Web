package net.adoptopenjdk.icedteaweb.resources.downloader;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * ...
 */
public class PrioritizedParallelExecutor {

    private final ExecutorService executor;

    public PrioritizedParallelExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    public <V> Future<V> getSuccessfulResultWithHighestPriority(List<Callable<V>> callables) {
        if (callables.isEmpty()) {
            final CompletableFuture<V> result = new CompletableFuture<>();
            result.completeExceptionally(new RuntimeException("No callable left to try"));
            return result;
        }

        final CancelingCallable<V> head = new CancelingCallable<>(callables.get(0));
        final List<Callable<V>> tail = callables.subList(1, callables.size());

        final ChainableFuture<V> result = new ChainableFuture<>(executor.submit(head));
        final Future<V> lowerPriority = getSuccessfulResultWithHighestPriority(tail);

        head.setLowerPriority(lowerPriority);
        result.setLowerPriority(lowerPriority);

        return result;
    }

    private static class CancelingCallable<V> implements Callable<V> {

        private enum STATE {RUNNING, SUCCESS, ERROR}

        private final AtomicReference<STATE> state = new AtomicReference<>(STATE.RUNNING);
        private final AtomicReference<Future<V>> lowerPriority = new AtomicReference<>();
        private final CompletableFuture<V> lowerPriorityResult = new CompletableFuture<>();

        private final Callable<V> delegate;

        private CancelingCallable(Callable<V> delegate) {
            this.delegate = delegate;
        }

        void setLowerPriority(Future<V> lowerPriority) {
            this.lowerPriority.set(lowerPriority);
            final STATE currentState = state.get();
            if (currentState == STATE.SUCCESS) {
                lowerPriority.cancel(true);
            }
            if (currentState == STATE.ERROR) {
                try {
                    lowerPriorityResult.complete(lowerPriority.get());
                } catch (Exception e) {
                    lowerPriorityResult.completeExceptionally(e);
                }
            }
        }

        @Override
        public V call() throws Exception {
            try {
                final V result = delegate.call();
                state.set(STATE.SUCCESS);
                final Future<V> lowerPriority = this.lowerPriority.get();
                if (lowerPriority != null) {
                    lowerPriority.cancel(true);
                }
                return result;
            } catch (Exception e) {
                state.set(STATE.ERROR);
                final Future<V> lowerPriority = this.lowerPriority.get();
                if (lowerPriority != null) {
                    return lowerPriority.get();
                } else {
                    return lowerPriorityResult.get();
                }
            }
        }
    }

    private static class ChainableFuture<V> implements Future<V> {

        private enum STATE {RUNNING, SUCCESS, ERROR}

        private final AtomicReference<STATE> state = new AtomicReference<>(STATE.RUNNING);
        private final AtomicReference<Future<V>> lowerPriority = new AtomicReference<>();
        private final CompletableFuture<V> lowerPriorityResult = new CompletableFuture<>();

        private final Future<V> delegate;


        private ChainableFuture(Future<V> delegate) {
            this.delegate = delegate;
        }

        void setLowerPriority(Future<V> lowerPriority) {
            this.lowerPriority.set(lowerPriority);
            final STATE currentState = this.state.get();
            if (currentState == STATE.SUCCESS) {
                lowerPriority.cancel(true);
            }
            if (currentState == STATE.ERROR) {
                try {
                    lowerPriorityResult.complete(lowerPriority.get());
                } catch (Exception e) {
                    lowerPriorityResult.completeExceptionally(e);
                }
            }
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            state.set(STATE.ERROR);
            final Future<V> lowerPriority = this.lowerPriority.get();
            if (lowerPriority != null) {
                lowerPriority.cancel(mayInterruptIfRunning);
            }
            return delegate.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
            return delegate.isCancelled();
        }

        @Override
        public boolean isDone() {
            return delegate.isDone();
        }

        @Override
        public V get() throws InterruptedException, ExecutionException {
            try {
                final V result = delegate.get();
                cancelLowerPriority();
                return result;
            } catch (InterruptedException | ExecutionException e) {
                return returnLowerPriority();
            }
        }

        @Override
        public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            try {
                final V result = delegate.get(timeout, unit);
                cancelLowerPriority();
                return result;
            } catch (InterruptedException | ExecutionException e) {
                return returnLowerPriority();
            } catch (TimeoutException e) {
                return returnLowerPriorityImmediatelyOrThrow(e);
            }
        }

        private void cancelLowerPriority() {
            state.set(STATE.SUCCESS);
            final Future<V> lowerPriority = this.lowerPriority.get();
            if (lowerPriority != null) {
                lowerPriority.cancel(true);
            }
        }

        private V returnLowerPriority() throws InterruptedException, ExecutionException {
            state.set(STATE.ERROR);
            final Future<V> lowerPriority = this.lowerPriority.get();
            if (lowerPriority != null) {
                return lowerPriority.get();
            } else {
                return lowerPriorityResult.get();
            }
        }

        private V returnLowerPriorityImmediatelyOrThrow(TimeoutException e) throws InterruptedException, ExecutionException, TimeoutException {
            state.set(STATE.ERROR);
            final Future<V> lowerPriority = this.lowerPriority.get();
            if (lowerPriority != null) {
                return lowerPriority.get(1, MILLISECONDS);
            } else {
                throw e;
            }
        }
    }
}
