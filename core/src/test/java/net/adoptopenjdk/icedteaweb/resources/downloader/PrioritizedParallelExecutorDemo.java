package net.adoptopenjdk.icedteaweb.resources.downloader;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ...
 */
public class PrioritizedParallelExecutorDemo {

    public static void main(String... args) throws Exception {
        System.out.println("START");

        final Callable<Character> first = callable("FIRST", 6, new RuntimeException());
//        final Callable<Character> first = callable("FIRST ", 6, 'a');
        final Callable<Character> second = callable("SECOND", 2, 'b');
        final Callable<Character> third = callable("THIRD ", 3, new RuntimeException());
        final Callable<Character> fourth = callable("FOURTH", 8, 'c');
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
                Thread.sleep(sleep * 1000);
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
                Thread.sleep(sleep * 1000);
            } catch (InterruptedException e) {
                System.out.println("  Cancel  " + name);
                throw e;
            }
            System.out.println("  Failed  " + name);
            throw exception;
        };
    }

}
