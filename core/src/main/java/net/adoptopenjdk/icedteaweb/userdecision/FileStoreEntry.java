package net.adoptopenjdk.icedteaweb.userdecision;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

class FileStoreEntry {
    private final URL domain;
    private final Set<String> jarNames;
    private final Map<UserDecision.Key, String> decisions = new HashMap<>();

    FileStoreEntry(final URL domain, final Set<String> jarNames) {
        this.domain = domain;
        this.jarNames = jarNames;
    }

    Optional<String> getUserDecisionValue(UserDecision.Key key) {
        return Optional.ofNullable(decisions.get(key));
    }

    <T extends Enum<T>> void setUserDecision(UserDecision<T> userDecision) {
        decisions.put(userDecision.getKey(), userDecision.getValue().name());
    }

    URL getDomain() {
        return domain;
    }

    Set<String> getJarNames() {
        return jarNames;
    }
}
