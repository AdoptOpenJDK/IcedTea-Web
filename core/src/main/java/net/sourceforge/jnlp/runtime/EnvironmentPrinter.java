package net.sourceforge.jnlp.runtime;

import net.adoptopenjdk.icedteaweb.JavaSystemProperties;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.util.Arrays;

public class EnvironmentPrinter {

    private static final Logger LOG = LoggerFactory.getLogger(EnvironmentPrinter.class);

    public static void logEnvironment(final String[] args) {
        LOG.info("OpenWebStartLauncher called with args: {}.", Arrays.toString(args));
        LOG.info("OS: {}", JavaSystemProperties.getOsName());
        LOG.info("Java Runtime {}-{}", JavaSystemProperties.getJavaVendor(), JavaSystemProperties.getJavaVersion());
    }
}
