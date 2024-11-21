package net.adoptopenjdk.icedteaweb.jvm;

import net.adoptopenjdk.icedteaweb.xmlparser.ParseException;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.Character.isWhitespace;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static net.adoptopenjdk.icedteaweb.IcedTeaWebConstants.JAVAWS;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.ITW_BIN_LOCATION;
import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.ITW_BIN_NAME;
import static net.adoptopenjdk.icedteaweb.StringUtils.isBlank;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_JVM_ARGS_WHITELIST;

public class JvmUtils {
    private static final Set<String> VALID_VM_ARGUMENTS = unmodifiableSet(new HashSet<>(asList(getValidVMArguments())));
    private static final List<String> VALID_STARTING_ARGUMENTS = unmodifiableList(asList(getValidStartingVMArguments()));
    private static final List<String> VALID_STARTING_JAVA_MODULES_ARGUMENTS = unmodifiableList(asList(getValidStartingJavaModuleVMArguments()));
    private static final Set<String> VALID_SECURE_PROPERTIES = unmodifiableSet(new HashSet<>(asList(getValidSecureProperties())));
    private static final String WHITESPACE_REGEX = "\\s+";
    private static final String KEY_VALUE_SEPARATOR = "=";
    private static final char QUOTES = '"';

    private static Set<String> configJvmArgsWhitelist = null;

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

        final String[] arguments = vmArgs.trim().split(WHITESPACE_REGEX);
        for (String argument : arguments) {
            if (!isValidArgument(argument)) {
                throw new IllegalArgumentException(argument);
            }
        }
    }

    private static boolean isValidArgument(final String argument) {
        return VALID_VM_ARGUMENTS.contains(argument)
                || isValidStartingArgument(argument)
                || isSecurePropertyAValidJVMArg(argument)
                || isValidStartingJavaModulesArgument(argument)
                || isValidConfigArgument(argument);
    }

    /**
     * Properties set in the JNLP file will normally be set by Web Start after the VM is started but before the
     * application is invoked. Some properties are considered "secure" properties and can be passed
     * as -Dkey=value arguments on the java invocation command line.
     */
    static boolean isSecurePropertyAValidJVMArg(String argument) {
        if (argument.startsWith("-D") && argument.length() > 2) {
            final int indexOfEqual = argument.indexOf(KEY_VALUE_SEPARATOR.charAt(0));
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

    public static boolean isValidStartingJavaModulesArgument(final String argument) {
        for (String validStartingArgument : VALID_STARTING_JAVA_MODULES_ARGUMENTS) {
            if (argument.startsWith(validStartingArgument)) {
                return true;
            }
        }
        return false;
    }

    /**
     * A secure property is valid if it is in the whitelist or it begins with "jnlp." or "javaws."
     *
     * @see <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/javaws/developersguide/syntax.html#secure-property">Java 8 Spec</a>
     */
    public static boolean isValidSecureProperty(final String argument) {
        if (argument.startsWith("jnlp.") || argument.startsWith("javaws.")) {
            return true;
        }
        return VALID_SECURE_PROPERTIES.contains(argument);
    }

    private static Set<String> getConfigJvmArgsWhitelist() {
        if (configJvmArgsWhitelist == null) {
            final List<String> whitelist = JNLPRuntime.getConfiguration().getPropertyAsList(KEY_JVM_ARGS_WHITELIST);
            configJvmArgsWhitelist = unmodifiableSet(new HashSet<>(whitelist));
        }
        return configJvmArgsWhitelist;
    }

    // Arguments that are specified in deployment.jvm.arguments.whitelist in deployment.config
    private static boolean isValidConfigArgument(final String argument) {
        final String[] argParts = argument.split(KEY_VALUE_SEPARATOR);
        return getConfigJvmArgsWhitelist().contains(argParts[0]);
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
                "-XX:GCHeapFreeLimit",
                "-XX:+UseParNewGC",
                "-XX:+CMSParallelRemarkEnabled",
                "-XX:InitialRAMPercentage", /* The initial heap size as percentage of total memory, conflicts with Xms */
                "-XX:MinRAMPercentage", /* Sets the max heap size of RAM as a percentage before looking at other heuristics like MaxRAMPercentage. This is primarily useful for low memory environments (<100m) */
                "-XX:MaxRAMPercentage", /* Sets the max heap size of RAM as a percentage */
        };
    }

    /**
     * https://docs.oracle.com/javase/8/docs/technotes/guides/javaws/developersguide/syntax.html#secure-property
     * <p>
     * Properties set in the JNLP file will normally be set by Web Start after the VM is started but before
     * the application is invoked. These properties are considered "secure" properties and can be passed as
     * -Dkey=value arguments on the java invocation command line. The following properties are predefined secure
     * properties and will be passed to the VM in this way.
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
                "http.auth.digest.validateServer",
                // https://news.kynosarges.org/2019/03/24/swing-high-dpi-properties/
                "sun.java2d.uiScale.enabled",
                "sun.java2d.win.uiScaleX",
                "sun.java2d.win.uiScaleY",
                "sun.java2d.uiScale",
                "prism.allowhidpi", // for JavaFX
                "sun.net.client.defaultConnectTimeout",
                "sun.net.client.defaultReadTimeout"
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

    /**
     * https://docs.oracle.com/javase/9/tools/java.htm#JSWOR624
     * Java Module VM arguments specified by app developer in jnlp file.
     * ITW adds its own set of jvm args either through {@link #getPredefinedJavaModulesVMArgumentsMap()}
     * or the itw-modularjdk.args file. These are required for the execution of ITW.
     * <p>
     * Note:
     * --module-path is not accepted by ITW as it would circumvent the classloader and security.
     */
    public static String[] getValidStartingJavaModuleVMArguments() {
        return new String[]{
                "--add-reads", /* Updates module to read the target-module, regardless of the module declaration. */
                "--add-exports", /* Updates module to export package to target-module, regardless of module declaration.  */
                "--add-opens", /* Updates module to open package to target-module, regardless of module declaration.*/
                "--add-modules", /* Specifies the root modules to resolve in addition to the initial module. */
                "--patch-module" /* Overrides or augments a module with classes and resources in JAR files or directories. */
        };
    }

    /**
     * Defined in itw-modularjdk.args. Required for running ITW with jdk 9 or higher
     */
    static Map<String, Set<String>> getPredefinedJavaModulesVMArgumentsMap() {
        return new LinkedHashMap<String, Set<String>>() {{
            put("--add-reads=java.base", moduleArgs("ALL-UNNAMED", "java.desktop"));
            put("--add-reads=java.desktop", moduleArgs("ALL-UNNAMED", "java.naming"));
            put("--add-reads=java.naming", moduleArgs("ALL-UNNAMED", "java.desktop"));

            put("--add-exports=java.desktop/sun.awt", moduleArgs("ALL-UNNAMED", "java.desktop"));
            put("--add-exports=java.desktop/javax.jnlp", moduleArgs("ALL-UNNAMED", "java.desktop"));

            put("--add-exports=java.base/com.sun.net.ssl.internal.ssl", moduleArgs("ALL-UNNAMED", "java.desktop"));
            put("--add-exports=java.base/sun.net.www.protocol.jar", moduleArgs("ALL-UNNAMED", "java.desktop"));
            put("--add-exports=java.base/sun.security.action", moduleArgs("ALL-UNNAMED", "java.desktop"));
            put("--add-exports=java.base/sun.security.provider", moduleArgs("ALL-UNNAMED", "java.desktop"));
            put("--add-exports=java.base/sun.security.util", moduleArgs("ALL-UNNAMED", "java.desktop"));
            put("--add-exports=java.base/sun.security.validator", moduleArgs("ALL-UNNAMED", "java.desktop"));
            put("--add-exports=java.base/sun.security.x509", moduleArgs("ALL-UNNAMED", "java.desktop"));
            put("--add-exports=java.base/jdk.internal.util.jar", moduleArgs("ALL-UNNAMED", "java.desktop"));
            put("--add-exports=java.base/sun.net.www.protocol.http", moduleArgs("ALL-UNNAMED", "java.desktop"));

            put("--add-exports=java.desktop/sun.awt.X11", moduleArgs("ALL-UNNAMED", "java.desktop"));
            put("--add-exports=java.desktop/sun.applet", moduleArgs("ALL-UNNAMED", "java.desktop", "jdk.jsobject"));
            put("--add-exports=java.naming/com.sun.jndi.toolkit.url", moduleArgs("ALL-UNNAMED", "java.desktop"));
        }};
    }

    private static Set<String> moduleArgs(final String... args) {
        return new LinkedHashSet<>(asList(args));
    }

    /**
     * Combines PREDEFINED_JAVA_MODULES_VM_ARGUMENTS with the Java Module VM Args in the input vmargs.
     * Does not check legality of args, this is done by {@link #checkVMArgs(String)}check
     */
    public static List<String> mergeJavaModulesVMArgs(final List<String> vmArgs) {

        final List<String> mergedVMArgs = new ArrayList<>();
        final Map<String, Set<String>> moduleArgMap = getPredefinedJavaModulesVMArgumentsMap();

        vmArgs.forEach(arg -> {
            if (isValidStartingJavaModulesArgument(arg)) { // it is a Java Module VM arg
                final int vmArgEndIndex = arg.lastIndexOf("=");
                final String argKey = arg.substring(0, vmArgEndIndex);
                final String argValue = arg.substring(vmArgEndIndex + 1);

                moduleArgMap.computeIfAbsent(argKey, k -> new LinkedHashSet<>())
                        .addAll(asList(argValue.split(",")));
            } else { // non module arg
                mergedVMArgs.add(arg);
            }
        });
        // Now add merged predefined args
        moduleArgMap.forEach((key, value) -> mergedVMArgs.add(key + "=" + String.join(",", value)));
        return mergedVMArgs;
    }

    public static List<String> parseArguments(final String args) throws ParseException {
        if (isBlank(args)) {
            return emptyList();
        }

        final List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        boolean wasInQuotes = false;
        StringBuilder next = new StringBuilder();
        for (char c : args.toCharArray()) {
            if (c == QUOTES) {
                wasInQuotes = inQuotes;
                inQuotes = !inQuotes;
            } else if (inQuotes) {
                next.append(c);
            } else if (isWhitespace(c)) {
                wasInQuotes = false;
                if (next.length() > 0) {
                    result.add(next.toString());
                    next = new StringBuilder();
                }
            } else if (wasInQuotes) {
                // after a quote must follow a whitespace
                throw new ParseException("failed to parse vmArgs " + args);
            } else {
                next.append(c);
            }
        }

        if (inQuotes) {
            throw new ParseException("failed to parse vmArgs " + args);
        }

        if (next.length() > 0) {
            result.add(next.toString());
        }

        return result;
    }
}
