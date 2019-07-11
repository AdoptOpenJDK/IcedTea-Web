package net.adoptopenjdk.icedteaweb.impl;

import net.adoptopenjdk.icedteaweb.Application;
import net.adoptopenjdk.icedteaweb.Process;
import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.Launcher;

import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class ApplicationImpl implements Application {

    private final URL jnlpFileLocation;
    private final Launcher launcher;


    public ApplicationImpl(final URL jnlpFileLocation, final Launcher launcher) {
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
