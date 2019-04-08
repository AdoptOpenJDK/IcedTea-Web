package net.adoptopenjdk.icedteaweb.impl;

import net.adoptopenjdk.icedteaweb.Application;
import net.adoptopenjdk.icedteaweb.Process;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.Launcher;

import java.util.concurrent.CompletableFuture;

public class ApplicationImpl implements Application {

    @Deprecated
    private final JNLPFile file;

    private final Launcher launcher;

    public ApplicationImpl(final JNLPFile file, final Launcher launcher) {
        this.file = file;
        this.launcher = launcher;
    }


    @Override
    public CompletableFuture<Process> start() {

        try {
            launcher.launch(file);
        } catch (LaunchException e) {
            e.printStackTrace();
        }

        return null;
    }
}
