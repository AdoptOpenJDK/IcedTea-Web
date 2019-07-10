package net.adoptopenjdk.icedteaweb.impl;

import net.adoptopenjdk.icedteaweb.Application;
import net.adoptopenjdk.icedteaweb.Process;
import net.adoptopenjdk.icedteaweb.launch.ApplicationLauncher;
import net.sourceforge.jnlp.LaunchException;

import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class ApplicationImpl implements Application {

    private final URL jnlpFileLocation;
    private final ApplicationLauncher launcher;


    public ApplicationImpl(final URL jnlpFileLocation, final ApplicationLauncher launcher) {
        this.jnlpFileLocation = jnlpFileLocation;
        this.launcher = launcher;
    }

    @Override
    public CompletableFuture<Process> start() {
        try {
            launcher.launch(jnlpFileLocation);
        } catch (LaunchException e) {
            e.printStackTrace();
        }

        return null;
    }
}
