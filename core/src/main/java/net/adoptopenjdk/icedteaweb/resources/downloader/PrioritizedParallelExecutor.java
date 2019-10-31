package net.adoptopenjdk.icedteaweb.resources.downloader;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * Executor which will execute multiple {@link Callable Callables} in parallel.
 * The callables must be sorted and have the highest priority callable as the first in list.
 * Executions of lower priority are canceled as soon as a higher priority result has been successfully been returned.
 * The final {@link Future} will complete as soon as the first callable completed successfully and all other callables
 * with higher priority (if any) have completed exceptionally
 */
public class PrioritizedParallelExecutor {

    private final ExecutorService executor;

    /**
     * Constructor
     *
     * @param executor the executor service which is used for executing the callables.
     */
    public PrioritizedParallelExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    /**
     * Executes the passed callables in parallel
     * The callables must be sorted by priority with the highest priority as the first in list.
     * Executions of lower priority are canceled as soon as a higher priority result has successfully been calculated.
     * The returned {@link Future} will complete as soon as the first callable completed successfully and all other
     * callables with higher priority (if any) have completed exceptionally.
     *
     * @param callables the callables to execute.
     * @return a future holding the best result from all callables.
     */
    public <V> Future<V> getSuccessfulResultWithHighestPriority(List<Callable<V>> callables) {
        if (callables == null || callables.isEmpty()) {
            throw new IllegalArgumentException("No callables");
        }

        if (callables.size() == 1) {
            return executor.submit(callables.get(0));
        }

        final CancelingCallable<V> head = new CancelingCallable<>(callables.get(0));
        final List<Callable<V>> tail = callables.subList(1, callables.size());

        final ChainableFuture<V> result = new ChainableFuture<>(executor.submit(head));
        final Future<V> lowerPriority = getSuccessfulResultWithHighestPriority(tail);

        head.setLowerPriority(lowerPriority);
        result.setLowerPriority(lowerPriority);

        return result;
    }

    /**
     * Wrapper around {@link Callable} which will cancel any lower priority after completing the call successfully.
     */
    private static class CancelingCallable<V> implements Callable<V> {

        private final AtomicBoolean complete = new AtomicBoolean(false);
        private final AtomicReference<Future<V>> lowerPriority = new AtomicReference<>();

        private final Callable<V> delegate;

        private CancelingCallable(Callable<V> delegate) {
            this.delegate = delegate;
        }

        /**
         * Set a lower priority to be canceled.
         * This is not passed in the constructor as the value is not known then.
         * <p>
         * If this callable has already been completed successfully then the passed future will be canceled immediately.
         *
         * @param lowerPriority the lower priority future to be canceled after completing this callable successfully.
         */
        void setLowerPriority(Future<V> lowerPriority) {
            this.lowerPriority.set(lowerPriority);
            if (complete.get()) {
                executeInBackground(this::cancelLowerPriority);
            }
        }

        @Override
        public V call() throws Exception {
            final V result = delegate.call();
            complete.set(true);
            cancelLowerPriority();
            return result;
        }

        private void cancelLowerPriority() {
            final Future<V> lowerPriorityFuture = this.lowerPriority.get();
            if (lowerPriorityFuture != null) {
                lowerPriorityFuture.cancel(true);
            }
        }
    }

    /**
     * Wrapper around {@link Future} which allows to construct a chain of futures.
     * Lower priority futures are canceled if this future is canceled.
     * If this future fails the result of the lower priority is returned.
     */
    private static class ChainableFuture<V> implements Future<V> {

        private final AtomicBoolean terminatedExceptionally = new AtomicBoolean(false);
        private final CompletableFuture<V> lowerPriorityResult = new CompletableFuture<>();
        private final AtomicReference<Future<V>> lowerPriority = new AtomicReference<>();

        private final Future<V> delegate;

        private ChainableFuture(Future<V> delegate) {
            this.delegate = delegate;
        }

        /**
         * Set a lower priority to be canceled.
         * This is not passed in the constructor as the value is not known then.
         *
         * @param lowerPriorityFuture the lower priority future.
         */
        void setLowerPriority(Future<V> lowerPriorityFuture) {
            this.lowerPriority.set(lowerPriorityFuture);

            if (terminatedExceptionally.get()) {
                executeInBackground(() -> completeLowerPriorityResult(lowerPriorityFuture));
            }
        }

        private void completeLowerPriorityResult(Future<V> lowerPriorityFuture) {
            try {
                lowerPriorityResult.complete(lowerPriorityFuture.get());
            } catch (Exception e) {
                lowerPriorityResult.completeExceptionally(e);
            }
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            terminatedExceptionally.set(true);
            final boolean result = delegate.cancel(mayInterruptIfRunning);
            executeInBackground(this::cancelLowerPriority);
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
                terminatedExceptionally.set(true);
                return returnLowerPriority();
            }
        }

        @Override
        public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            final long start = System.nanoTime();
            try {
                return delegate.get(timeout, unit);
            } catch (ExecutionException e) {
                terminatedExceptionally.set(true);
                final long elapsed = System.nanoTime() - start;
                final long remaining = unit.toNanos(timeout) - elapsed;
                return returnLowerPriority(remaining, NANOSECONDS);
            } catch (TimeoutException e) {
                terminatedExceptionally.set(true);
                return returnLowerPriorityImmediately(e);
            }
        }

        private void cancelLowerPriority() {
            final Future<V> lowerPriorityFuture = this.lowerPriority.get();
            if (lowerPriorityFuture != null) {
                lowerPriorityFuture.cancel(true);
            }
        }

        private V returnLowerPriority() throws InterruptedException, ExecutionException {
            final Future<V> lowerPriorityFuture = this.lowerPriority.get();
            if (lowerPriorityFuture != null) {
                return lowerPriorityFuture.get();
            } else {
                return lowerPriorityResult.get();
            }
        }

        private V returnLowerPriority(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            final Future<V> lowerPriorityFuture = this.lowerPriority.get();
            if (lowerPriorityFuture != null) {
                return lowerPriorityFuture.get(timeout, unit);
            } else {
                return lowerPriorityResult.get(timeout, unit);
            }
        }

        private V returnLowerPriorityImmediately(TimeoutException e) throws TimeoutException {
            final Future<V> lowerPriorityFuture = this.lowerPriority.get();
            if (lowerPriorityFuture != null && lowerPriorityFuture.isDone() && !lowerPriorityFuture.isCancelled()) {
                try {
                    return lowerPriorityFuture.get();
                } catch (ExecutionException | InterruptedException ignored) {
                    throw e;
                }
            } else {
                throw e;
            }
        }
    }

    private static void executeInBackground(Runnable runnable) {
        Executors.newSingleThreadExecutor().execute(runnable);
    }
}
