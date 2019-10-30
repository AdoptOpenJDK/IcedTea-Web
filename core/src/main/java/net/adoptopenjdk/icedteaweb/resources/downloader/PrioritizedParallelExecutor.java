package net.adoptopenjdk.icedteaweb.resources.downloader;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * Executor which will execute multiple {@link Callable Callables} in parallel.
 * The callables must be sorted and have the highest priority callable as the first in list.
 * The all executions are canceled as soon as the result is determined.
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

        private final AtomicBoolean complete = new AtomicBoolean(false);
        private final AtomicReference<Future<V>> lowerPriority = new AtomicReference<>();

        private final Callable<V> delegate;

        private CancelingCallable(Callable<V> delegate) {
            this.delegate = delegate;
        }

        void setLowerPriority(Future<V> lowerPriority) {
            this.lowerPriority.set(lowerPriority);
            if (complete.get()) {
                lowerPriority.cancel(true);
            }
        }

        @Override
        public V call() throws Exception {
            final V result = delegate.call();
            cancelLowerPriority();
            return result;
        }

        private void cancelLowerPriority() {
            complete.set(true);
            final Future<V> lowerPriority = this.lowerPriority.get();
            if (lowerPriority != null) {
                lowerPriority.cancel(true);
            }
        }
    }

    private static class ChainableFuture<V> implements Future<V> {

        private final AtomicBoolean terminatedExceptionally = new AtomicBoolean(false);
        private final CompletableFuture<V> lowerPriorityResult = new CompletableFuture<>();
        private final AtomicReference<Future<V>> lowerPriority = new AtomicReference<>();

        private final Future<V> delegate;

        private ChainableFuture(Future<V> delegate) {
            this.delegate = delegate;
        }

        void setLowerPriority(Future<V> lowerPriority) {
            this.lowerPriority.set(lowerPriority);

            if (terminatedExceptionally.get()) {
                // TODO: should the body of this if be executed in a separate thread to not block the current thread
                //  which is about to plug together the future chain...
                try {
                    lowerPriorityResult.complete(lowerPriority.get());
                } catch (Exception e) {
                    lowerPriorityResult.completeExceptionally(e);
                }
            }
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            final boolean result = delegate.cancel(mayInterruptIfRunning);
            cancelLowerPriority();
            return result;
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
                return delegate.get();
            } catch (ExecutionException e) {
                return returnLowerPriority();
            }
        }

        @Override
        public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            final long start = System.nanoTime();
            try {
                return delegate.get(timeout, unit);
            } catch (ExecutionException e) {
                final long elapsed = System.nanoTime() - start;
                final long remaining = unit.toNanos(timeout) - elapsed;
                return returnLowerPriority(remaining, NANOSECONDS);
            } catch (TimeoutException e) {
                return returnLowerPriority(1, MILLISECONDS);
            }
        }

        private void cancelLowerPriority() {
            terminatedExceptionally.set(true);
            final Future<V> lowerPriority = this.lowerPriority.get();
            if (lowerPriority != null) {
                lowerPriority.cancel(true);
            }
        }

        private V returnLowerPriority() throws InterruptedException, ExecutionException {
            terminatedExceptionally.set(true);
            final Future<V> lowerPriority = this.lowerPriority.get();
            if (lowerPriority != null) {
                return lowerPriority.get();
            } else {
                return lowerPriorityResult.get();
            }
        }

        private V returnLowerPriority(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            terminatedExceptionally.set(true);
            final Future<V> lowerPriority = this.lowerPriority.get();
            if (lowerPriority != null) {
                return lowerPriority.get(timeout, unit);
            } else {
                return lowerPriorityResult.get(timeout, unit);
            }
        }
    }
}
