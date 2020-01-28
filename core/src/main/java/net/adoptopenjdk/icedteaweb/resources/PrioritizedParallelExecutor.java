package net.adoptopenjdk.icedteaweb.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Executor which will execute multiple {@link Callable Callables} in parallel.
 * The callables must be sorted and have the highest priority callable as the first in list.
 * Executions of lower priority are canceled as soon as a higher priority result has been successfully been returned.
 * The final {@link Future} will complete as soon as the first callable completed successfully and all other callables
 * with higher priority (if any) have completed exceptionally
 */
public class PrioritizedParallelExecutor {

    private static final String LIST_PREFIX = "\n   - ";

    private final ExecutorService executor;

    /**
     * Constructor
     *
     * @param executor the executor service which is used for executing the callables.
     */
    public PrioritizedParallelExecutor(final ExecutorService executor) {
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
    public <V> Future<V> getSuccessfulResultWithHighestPriority(final List<Callable<V>> callables) {
        if (callables == null || callables.isEmpty()) {
            throw new IllegalArgumentException("No callables");
        }

        final List<Exception> exceptions = new ArrayList<>();
        for (Callable<V> next : callables) {
            try {
                return CompletableFuture.completedFuture(executor.submit(next).get());
            } catch (Exception e) {
                exceptions.add(e);
                // continue with next callable
            }
        }
        final CompletableFuture<V> futureResult = new CompletableFuture<>();
        futureResult.completeExceptionally(getFailureReason(exceptions));
        return futureResult;
    }

    private Exception getFailureReason(List<Exception> exceptions) {
        if (exceptions.size() == 1) {
            return exceptions.get(0);
        } else {
            return new RuntimeException("All callables completed exceptionally:" + LIST_PREFIX +
                    exceptions.stream().map(Exception::getMessage).collect(Collectors.joining(LIST_PREFIX)));
        }
    }

}
