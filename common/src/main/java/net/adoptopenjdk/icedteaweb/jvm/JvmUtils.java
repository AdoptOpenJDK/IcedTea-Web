package net.adoptopenjdk.icedteaweb.jvm;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static net.adoptopenjdk.icedteaweb.IcedTeaWebConstants.JAVAWS;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.ITW_BIN_LOCATION;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.ITW_BIN_NAME;
import static net.adoptopenjdk.icedteaweb.StringUtils.isBlank;

public class JvmUtils {

    private static final Set<String> VALID_VM_ARGUMENTS = unmodifiableSet(new HashSet<>(asList(getValidVMArguments())));
    private static final List<String> VALID_STARTING_ARGUMENTS = unmodifiableList(asList(getValidStartingVMArguments()));
    private static final Set<String> VALID_SECURE_PROPERTIES = unmodifiableSet(new HashSet<>(asList(getValidSecureProperties())));

    /**
     * Check that the VM args are valid and safe.
     * <p>
     * Based on
     * http://java.sun.com/javase/6/docs/technotes/guides/javaws/developersguide/syntax.html
     *
     * @param vmArgs a string containing the args
     * @throws IllegalArgumentException if the VM arguments are invalid or dangerous
     */
    public static void checkVMArgs(final String vmArgs) throws IllegalArgumentException {
        if (isBlank(vmArgs)) {
            return;
        }

        final String[] arguments = vmArgs.split(" ");
        for (String argument : arguments) {
            if (isInvalidValidArgument(argument)) {
                throw new IllegalArgumentException(argument);
            }
        }
    }

    private static boolean isInvalidValidArgument(final String argument) {
        return !VALID_VM_ARGUMENTS.contains(argument) && !isValidStartingArgument(argument) && !isSecurePropertyAValidJVMArg(argument);
    }

    /**
     * Properties set in the JNLP file will normally be set by Web Start after the VM is started but before the
     * application is invoked. Some properties are considered "secure" properties and can be passed
     * as -Dkey=value arguments on the java invocation command line.
     *
     * @param argument
     * @return
     */
    static boolean isSecurePropertyAValidJVMArg(String argument) {
        if (argument.startsWith("-D") && argument.length() > 2) {
            final int indexOfEqual = argument.indexOf('=');
            final int lastIndex = indexOfEqual == -1 ? argument.length() : indexOfEqual;
            return isValidSecureProperty(argument.substring(2, lastIndex));
        }
        return false;
    }

    private static boolean isValidStartingArgument(final String argument) {
        for (String validStartingArgument : VALID_STARTING_ARGUMENTS) {
            if (argument.startsWith(validStartingArgument)) {
                return true;
            }
        }
        return false;
    }

    /**
     * A secure property is valid if it is in the whitelist or it begins with "jnlp." or "javaws."
     *
     * @param argument
     * @return
     * @see <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/javaws/developersguide/syntax.html#secure-property">Java 8 Spec</a>
     */
    static boolean isValidSecureProperty(final String argument) {
        if (argument.startsWith("jnlp.") || argument.startsWith("javaws.")) {
            return true;
        }
        return VALID_SECURE_PROPERTIES.contains(argument);
    }

    /**
     * Returns an array of valid (ie safe and supported) arguments for the JVM
     * <p>
     * Based on
     * https://docs.oracle.com/javase/8/docs/technotes/guides/javaws/developersguide/syntax.html#secure-property
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
                "-XX:-ForceTimeHighResolution", /* use low resolution (default) */
                "-XX:+PrintGCDetails", /* Gives some details about the GCs */
                "-XX:+PrintGCTimeStamps", /* Prints GCs times happen to the start of the application */
                "-XX:+PrintHeapAtGC", /* Prints detailed GC info including heap occupancy */
                "-XX:+PrintTenuringDistribution", /* Gives the aging distribution of the allocated objects */
                "-XX:+TraceClassUnloading", /* Display classes as they are unloaded */
                "-XX:+CMSClassUnloadingEnabled", /* It needs to be combined with -XX:+CMSPermGenSweepingEnabled */
                "-XX:+CMSIncrementalPacing", /* Automatic adjustment of the incremental mode duty cycle */
                "-XX:+UseConcMarkSweepGC", /* Turns on concurrent garbage collection */
                "-XX:-ParallelRefProcEnabled",
                "-XX:+DisableExplicitGC", /* Disable calls to System.gc() */
                "-XX:+UseG1GC",
                "-XX:+HeapDumpOnOutOfMemoryError",
                "-XX:-TransmitErrorReport",
                "-XstartOnFirstThread",
                "-XX:+UseStringDeduplication",
                "-XX:+PrintStringDeduplicationStatistics",
                "-XX:+UseParallelOldGC",
                "-XX:-UseParallelOldGC",
                "-XX:+UseParallelOldGCCompacting",
                "-XX:-UseParallelOldGCCompacting",
                "-XX:+UseParallelGC",
                "-XX:-UseParallelGC",
                "-XX:+UseGCTimeLimit",
                "-XX:-UseGCTimeLimit",
                "-XX:+UseGCOverheadLimit",
                "-XX:-UseGCOverheadLimit",
                "-XX:+ScavengeBeforeFullGC",
                "-XX:-ScavengeBeforeFullGC",
                "-XX:+UseParallelScavenge",
                "-XX:-UseParallelScavenge"
        };
    }

    /**
     * Returns an array containing the starts of valid (ie safe and supported)
     * arguments for the JVM
     * <p>
     * Based on
     * https://docs.oracle.com/javase/8/docs/technotes/guides/javaws/developersguide/syntax.html#secure-property
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
                "-XX:-UseSerialGC",
                "-XX:ThreadStackSize", /* thread stack size (in KB) */
                "-XX:MaxInlineSize", /* set max num of bytecodes to inline */
                "-XX:ReservedCodeCacheSize", /* Reserved code cache size (bytes) */
                "-XX:MaxDirectMemorySize", /* set maximum direct memory size */
                "-XX:PrintCMSStatistics", /* If > 0, Print statistics about the concurrent collections */
                "-XX:SurvivorRatio",                    /* Sets the ratio of the survivor spaces */
                "-XX:MaxTenuringThreshold",             /* Determines how much the objects may age */
                "-XX:CMSMarkStackSize",
                "-XX:CMSMarkStackSizeMax",
                "-XX:CMSIncrementalDutyCycleMin",       /* The percentage which is the lower bound on the duty cycle */
                "-XX:ParallelCMSThreads",
                "-XX:ParallelGCThreads",                /* Sets the number of parallel GC threads */
                "-XX:CMSInitiatingOccupancyFraction",   /* Sets the threshold percentage of the used heap */
                "-XX:+UseCompressedOops",               /* Enables compressed references in 64-bit JVMs */
                "-XX:GCPauseIntervalMillis",
                "-XX:MaxGCPauseMillis",                 /* A hint to the virtual machine to pause times */
                "-XX:+CMSIncrementalMode",              /* Enables the incremental mode */
                "-XX:MaxMetaspaceSize",                 /* Sets an upper limit on memory used for class metadata */
                "-XX:StringDeduplicationAgeThreshold",
                "-XX:GCTimeLimit",
                "-XX:GCHeapFreeLimit"
        };
    }

    /**
     * https://docs.oracle.com/javase/8/docs/technotes/guides/javaws/developersguide/syntax.html#secure-property
     * <p>
     * Properties set in the JNLP file will normally be set by Web Start after the VM is started but before
     * the application is invoked. These properties are considered "secure" properties and can be passed as
     * -Dkey=value arguments on the java invocation command line. The following properties are predefined secure
     * properties and will be passed to the VM in this way.
     *
     * @return
     */
    private static String[] getValidSecureProperties() {
        return new String[]{
                "sun.java2d.noddraw",
                "javaws.cfg.jauthenticator",
                "swing.useSystemFontSettings",
                "swing.metalTheme",
                "http.agent",
                "http.keepAlive",
                "sun.awt.noerasebackground",
                "sun.java2d.opengl",
                "sun.java2d.d3d",
                "java.awt.syncLWRequests",
                "java.awt.Window.locationByPlatform",
                "sun.awt.erasebackgroundonresize",
                "sun.awt.keepWorkingSetOnMinimize",
                "swing.noxp",
                "swing.boldMetal",
                "awt.useSystemAAFontSettings",
                "sun.java2d.dpiaware",
                "sun.awt.disableMixing",
                "sun.lang.ClassLoader.allowArraySyntax",
                "java.awt.smartInvalidate",
                "apple.laf.useScreenMenuBar",
                "java.net.preferIPv4Stack",
                "java.util.Arrays.useLegacyMergeSort",
                "sun.locale.formatasdefault",
                "sun.awt.enableExtraMouseButtons",
                "com.sun.management.jmxremote.local.only",
                "sun.nio.ch.bugLevel",
                "sun.nio.ch.disableSystemWideOverlappingFileLockCheck",
                "jdk.map.althashing.threshold",
                "javaplugin.lifecycle.cachesize",
                "http.maxRedirects",
                "http.auth.digest.validateProxy",
                "http.auth.digest.validateServer"
        };
    }

    /**
     * @return the javaws binary.
     */
    public static String getJavaWsBin() {
        //Shortcut executes the jnlp as it was with system preferred java. It should work fine offline
        //absolute - works in case of self built
        final String exec = System.getProperty(ITW_BIN_LOCATION);
        if (exec != null) {
            return exec;
        }
        final String pathResult = findOnPath(new String[]{JAVAWS, System.getProperty(ITW_BIN_NAME)});
        if (pathResult != null) {
            return pathResult;
        }

        return JAVAWS;
    }

    /**
     * Find a binary of the given binaries on PATH.
     *
     * @param bins list of binaries to look for
     * @return the absolute path to the first binary found
     */
    public static String findOnPath(final String[] bins) {
        String exec = null;
        //find if one of binaries is on path
        String path = System.getenv().get("PATH");
        if (path == null || path.trim().isEmpty()) {
            path = System.getenv().get("path");
        }
        if (path == null || path.trim().isEmpty()) {
            path = System.getenv().get("Path");
        }
        if (path != null && !path.trim().isEmpty()) {
            //relative - works with alternatives
            final String[] paths = path.split(File.pathSeparator);
            outerloop:
            for (String bin : bins) {
                //when property is not set
                if (bin == null) {
                    continue;
                }
                for (String p : paths) {
                    final File file = new File(p, bin);
                    if (file.exists()) {
                        exec = file.getAbsolutePath();
                        break outerloop;
                    }
                }

            }
        }
        return exec;
    }
}
