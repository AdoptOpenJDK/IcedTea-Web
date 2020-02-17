package net.adoptopenjdk.icedteaweb.userdecision;

import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember.RememberResult;
import net.sourceforge.jnlp.JNLPFile;

public interface UserDecisions {
    <T extends Enum<T>> T getUserDecisions(UserDecision.Key key, JNLPFile file, Class<T> resultType);

    default <T extends Enum<T>> void saveForDomain(JNLPFile file, UserDecision<T> userDecision) {
        save(RememberResult.REMEMBER_BY_DOMAIN, file, userDecision);
    }

    default <T extends Enum<T>> void saveForApplication(JNLPFile file, UserDecision<T> userDecision) {
        save(RememberResult.REMEMBER_BY_APPLICATION, file, userDecision);
    }

    <T extends Enum<T>> void save(RememberResult result, JNLPFile file, UserDecision<T> userDecision);
}
