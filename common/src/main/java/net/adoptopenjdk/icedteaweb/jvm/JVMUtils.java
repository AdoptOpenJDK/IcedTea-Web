package net.adoptopenjdk.icedteaweb.jvm;

import java.util.Arrays;
import java.util.List;

public class JVMUtils {
    /**
     * Check that the VM args are valid and safe.
     *
     * Based on
     * http://java.sun.com/javase/6/docs/technotes/guides/javaws/developersguide/syntax.html
     *
     * @param vmArgs a string containing the args
     * @throws IllegalArgumentException if the VM arguments are invalid or dangerous
     */
    public static void checkVMArgs(final String vmArgs) throws IllegalArgumentException {
        if (vmArgs == null) {
            return;
        }

        final List<String> validArguments = Arrays.asList(getValidVMArguments());
        final String[] validStartingArguments = getValidStartingVMArguments();

        final String[] arguments = vmArgs.split(" ");
        boolean argumentIsValid;
        for (String argument : arguments) {
            argumentIsValid = false;

            if (validArguments.contains(argument)) {
                argumentIsValid = true;
            } else {
                for (String validStartingArgument : validStartingArguments) {
                    if (argument.startsWith(validStartingArgument)) {
                        argumentIsValid = true;
                        break;
                    }
                }
            }

            if (!argumentIsValid) {
                throw new IllegalArgumentException(argument);
            }
        }
    }

    /**
     * Returns an array of valid (ie safe and supported) arguments for the JVM
     *
     * Based on
     * http://java.sun.com/javase/6/docs/technotes/guides/javaws/developersguide/syntax.html
     */
    private static String[] getValidVMArguments() {
        return new String[]{
            "-d32", /* use a 32-bit data model if available */
            "-client", /* to select the client VM */
            "-server", /* to select the server VM */
            "-verbose", /* enable verbose output */
            "-version", /* print product version and exit */
            "-showversion", /* print product version and continue */
            "-help", /* print this help message */
            "-X", /* print help on non-standard options */
            "-ea", /* enable assertions */
            "-enableassertions", /* enable assertions */
            "-da", /* disable assertions */
            "-disableassertions", /* disable assertions */
            "-esa", /* enable system assertions */
            "-enablesystemassertions", /* enable system assertions */
            "-dsa", /* disable system assertione */
            "-disablesystemassertions", /* disable system assertione */
            "-Xmixed", /* mixed mode execution (default) */
            "-Xint", /* interpreted mode execution only */
            "-Xnoclassgc", /* disable class garbage collection */
            "-Xincgc", /* enable incremental garbage collection */
            "-Xbatch", /* disable background compilation */
            "-Xprof", /* output cpu profiling data */
            "-Xdebug", /* enable remote debugging */
            "-Xfuture", /* enable strictest checks, anticipating future default */
            "-Xrs", /* reduce use of OS signals by Java/VM (see documentation) */
            "-XX:+ForceTimeHighResolution", /* use high resolution timer */
            "-XX:-ForceTimeHighResolution", /* use low resolution (default) */};
    }

    /**
     * Returns an array containing the starts of valid (ie safe and supported)
     * arguments for the JVM
     *
     * Based on
     * http://java.sun.com/javase/6/docs/technotes/guides/javaws/developersguide/syntax.html
     */
    private static String[] getValidStartingVMArguments() {
        return new String[]{
            "-ea", /* enable assertions for classes */
            "-enableassertions", /* enable assertions for classes */
            "-da", /* disable assertions for classes */
            "-disableassertions", /* disable assertions for classes */
            "-verbose", /* enable verbose output */
            "-Xms", /* set initial Java heap size */
            "-Xmx", /* set maximum Java heap size */
            "-Xss", /* set java thread stack size */
            "-XX:NewRatio", /* set Ratio of new/old gen sizes */
            "-XX:NewSize", /* set initial size of new generation */
            "-XX:MaxNewSize", /* set max size of new generation */
            "-XX:PermSize", /* set initial size of permanent gen */
            "-XX:MaxPermSize", /* set max size of permanent gen */
            "-XX:MaxHeapFreeRatio", /* heap free percentage (default 70) */
            "-XX:MinHeapFreeRatio", /* heap free percentage (default 40) */
            "-XX:UseSerialGC", /* use serial garbage collection */
            "-XX:ThreadStackSize", /* thread stack size (in KB) */
            "-XX:MaxInlineSize", /* set max num of bytecodes to inline */
            "-XX:ReservedCodeCacheSize", /* Reserved code cache size (bytes) */
            "-XX:MaxDirectMemorySize",};
    }
}
