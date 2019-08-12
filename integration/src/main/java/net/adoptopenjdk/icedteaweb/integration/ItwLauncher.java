package net.adoptopenjdk.icedteaweb.integration;

import net.adoptopenjdk.icedteaweb.JvmPropertyConstants;
import net.adoptopenjdk.icedteaweb.StreamUtils;
import net.adoptopenjdk.icedteaweb.commandline.CommandLineOptions;
import net.sourceforge.jnlp.runtime.Boot;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static net.adoptopenjdk.icedteaweb.commandline.CommandLineOptions.HEADLESS;
import static net.adoptopenjdk.icedteaweb.commandline.CommandLineOptions.NOFORK;
import static net.adoptopenjdk.icedteaweb.commandline.CommandLineOptions.VERBOSE;

/**
 * Helper to launch ITW in a new JVM with only the fat jar (with all dependencies included) on the classpath.
 */
public class ItwLauncher {

    public static int launchItw(String jnlpUrl, String... arguments) throws Exception {
        return launchItw(jnlpUrl, Arrays.asList(NOFORK, VERBOSE), arguments);
    }

    public static int launchItwHeadless(String jnlpUrl, String... arguments) throws Exception {
        return launchItw(jnlpUrl, Arrays.asList(NOFORK, VERBOSE, HEADLESS), arguments);
    }

    private static int launchItw(String jnlpUrl, List<CommandLineOptions> additionalArgs, String... arguments) throws Exception {
        final String javaBinary = "/bin/java".replace('/', File.separatorChar);
        final String pathToJavaBinary = System.getProperty(JvmPropertyConstants.JAVA_HOME) + javaBinary;

        final String pathToJavawsJar = Boot.class.getProtectionDomain().getCodeSource().getLocation().getFile();

        final List<String> args = new ArrayList<>();
        args.add("-jnlp");
        args.add(jnlpUrl);
        additionalArgs.stream().map(CommandLineOptions::getOption).forEach(args::add);
        Collections.addAll(args, arguments);


        return launchExternal(pathToJavaBinary, pathToJavawsJar, Collections.emptyList(), args);
    }

    /**
     * @param pathToJavaBinary path to the java binary of the JRE in which to start OWS
     * @param pathToJavawsJar  path to the javaws.jar included in OWS
     * @param vmArgs           the arguments to pass to the jvm
     * @param javawsArgs       the arguments to pass to javaws (aka IcedTea-Web)
     * @return the exit value of the external JVM process
     */
    private static int launchExternal(String pathToJavaBinary, String pathToJavawsJar, List<String> vmArgs, List<String> javawsArgs) throws Exception {
        final List<String> commands = new LinkedList<>();

        commands.add(addQuotesIfRequired(pathToJavaBinary));
        commands.add("-cp");
        commands.add(addQuotesIfRequired(pathToJavawsJar));

        commands.addAll(vmArgs);
        commands.add(Boot.class.getName());
        commands.addAll(javawsArgs);

        final ProcessBuilder builder = new ProcessBuilder()
                .command(commands)
                .inheritIO();

        builder.environment();

        final Process p = builder
                .start();

        StreamUtils.waitForSafely(p);

        return p.exitValue();
    }

    /**
     * Checks whether a given path string contains spaces and if so, is correctly double quoted.
     *
     * @param original the path string to check
     * @return the correctly quoted path string if needed
     */
    private static String addQuotesIfRequired(final String original) {
        final String WHITESPACE = " ";
        final String DOUBLE_QUOTE = "\"";


        final String osName = System.getProperty("os.name");
        if (osName.toLowerCase().contains("mac")) {
            return original;
        }

        if (original != null && original.contains(WHITESPACE) && !original.startsWith(DOUBLE_QUOTE)) {
            return DOUBLE_QUOTE + original + DOUBLE_QUOTE;
        }
        return original;
    }


}
