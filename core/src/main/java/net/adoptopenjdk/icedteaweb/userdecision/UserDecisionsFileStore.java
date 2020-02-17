package net.adoptopenjdk.icedteaweb.userdecision;

import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.config.PathsAndFiles;

import java.io.File;
import java.util.Optional;

public class UserDecisionsFileStore implements UserDecisions {
    private final static File store = PathsAndFiles.APPLET_TRUST_SETTINGS_USER.getFile();

    @Override
    public <T extends Enum<T>> Optional<T> getUserDecisions(final UserDecision.Key key, final JNLPFile file, final Class<T> resultType) {
        // TODO implementation missing
        // lock file
        return Optional.empty();
    }

    @Override
    public <T extends Enum<T>> void saveForDomain(final JNLPFile file, final UserDecision<T> userDecision) {
        // TODO implementation missing
        // lock file, clean domain, see old impl for UrlUtils/UrlRegEx
    }

    @Override
    public <T extends Enum<T>> void saveForApplication(final JNLPFile file, final UserDecision<T> userDecision) {
        // TODO implementation missing
        // lock file, clean domain, see old impl for UrlUtils/UrlRegEx
    }
}
