package net.adoptopenjdk.icedteaweb.resources.downloader;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Demo for {@link PrioritizedParallelExecutor}
 */
public class PrioritizedParallelExecutorDemo {

    private static final int DELAY = 1000;

    private static long startTimeMillis;

    public static void main(String... args) throws Exception {

        final Callable<Character> first = callable("FIRST", 4, new RuntimeException());
//        final Callable<Character> first = callable("FIRST ", 3, 'a');
//        final Callable<Character> second = callable("SECOND", 1, new RuntimeException());
        final Callable<Character> second = callable("SECOND", 1, 'b');
        final Callable<Character> third = callable("THIRD", 3, new RuntimeException());
//        final Callable<Character> third = callable("THIRD ", 2, 'c');
        final Callable<Character> fourth = callable("FOURTH", 2, 'd');
        final List<Callable<Character>> callables = Arrays.asList(first, second, third, fourth);

        final ExecutorService executor = Executors.newFixedThreadPool(6);
        final PrioritizedParallelExecutor prioritizedExecutor = new PrioritizedParallelExecutor(executor);
        final ParallelExecutor parallelExecutor = new ParallelExecutor(executor);

        startTimeMillis = System.currentTimeMillis();
        System.out.println("START");
        final char resultOne = prioritizedExecutor.getSuccessfulResultWithHighestPriority(callables).get();
        System.out.println("END");
        System.out.println("-->  " + resultOne);

        startTimeMillis = System.currentTimeMillis();
        System.out.println("START");
        final char resultTwo = parallelExecutor.getSuccessfulResultWithHighestPriority(callables).get();
        System.out.println("END");
        System.out.println("-->  " + resultTwo);

        executor.shutdown();
    }

    private static Callable<Character> callable(String name, long sleep, char result) {
        return () -> {
            try {
                Thread.sleep(sleep * DELAY);
            } catch (InterruptedException e) {
                System.out.println(time() + "Cancel  " + name);
                throw e;
            }
            System.out.println(time() + "Success " + name);
            return result;
        };
    }

    private static Callable<Character> callable(String name, long sleep, Exception exception) {
        return () -> {
            try {
                Thread.sleep(sleep * DELAY);
            } catch (InterruptedException e) {
                System.out.println(time() + "Cancel  " + name);
                throw e;
            }
            System.out.println(time() + "Failed  " + name);
            throw exception;
        };
    }

    private static String time() {
        final long diff = System.currentTimeMillis() - startTimeMillis;
        return String.format("  %.2f s  ", diff / 1000d);
    }

    private static class ParallelExecutor {

        private final ExecutorService executor;

        ParallelExecutor(ExecutorService executor) {
            this.executor = executor;
        }

        <V> Future<V> getSuccessfulResultWithHighestPriority(List<Callable<V>> callables) {
            CompletableFuture<V> higherPrio = create(callables.get(0));
            for (int i = 1; i < callables.size(); i++) {
                higherPrio = create(higherPrio, callables.get(i));
            }

            return higherPrio;
        }

        private <V> CompletableFuture<V> create(final Callable<V> supplier) {
            final CompletableFuture<V> result = new CompletableFuture<>();
            executor.execute(() -> {
                try {
                    result.complete(supplier.call());
                } catch (Exception e) {
                    result.completeExceptionally(e);
                }
            });
            return result;
        }

        private <V> CompletableFuture<V> create(final CompletableFuture<V> higherPrio, final Callable<V> supplier) {
            final Future<V> myFuture = executor.submit(supplier);
            return higherPrio.handle((i, e) -> {
                if (e != null) {
                    try {
                        return myFuture.get();
                    } catch (Exception ex) {
                        throw new RuntimeException("Can not get value", ex);
                    }
                } else {
                    myFuture.cancel(true);
                    return i;
                }
            });
        }
    }
}
