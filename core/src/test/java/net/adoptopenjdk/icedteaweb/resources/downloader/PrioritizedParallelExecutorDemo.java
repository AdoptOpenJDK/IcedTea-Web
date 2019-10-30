package net.adoptopenjdk.icedteaweb.resources.downloader;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Demo for {@link PrioritizedParallelExecutor}
 */
public class PrioritizedParallelExecutorDemo {

    public static void main(String... args) throws Exception {
        System.out.println("START");

        final Callable<Character> first = callable("FIRST", 3, new RuntimeException());
//        final Callable<Character> first = callable("FIRST ", 3, 'a');
//        final Callable<Character> second = callable("SECOND", 1, new RuntimeException());
        final Callable<Character> second = callable("SECOND", 1, 'b');
        final Callable<Character> third = callable("THIRD ", 2, new RuntimeException());
//        final Callable<Character> third = callable("THIRD ", 2, 'c');
        final Callable<Character> fourth = callable("FOURTH", 4, 'd');
        final List<Callable<Character>> callables = Arrays.asList(first, second, third, fourth);

        final ExecutorService executor = Executors.newFixedThreadPool(6);
        final PrioritizedParallelExecutor prioritizedExecutor = new PrioritizedParallelExecutor(executor);

        final char result = prioritizedExecutor.getSuccessfulResultWithHighestPriority(callables).get();

        System.out.println("END");
        System.out.println("-->  " +  result);

        executor.shutdown();
    }

    private static Callable<Character> callable(String name, long sleep, char result) {
        return () -> {
            try {
                Thread.sleep(sleep * 200);
            } catch (InterruptedException e) {
                System.out.println("  Cancel  " + name);
                throw e;
            }
            System.out.println("  Success " + name);
            return result;
        };
    }

    private static Callable<Character> callable(String name, long sleep, Exception exception) {
        return () -> {
            try {
                Thread.sleep(sleep * 200);
            } catch (InterruptedException e) {
                System.out.println("  Cancel  " + name);
                throw e;
            }
            System.out.println("  Failed  " + name);
            throw exception;
        };
    }

}
