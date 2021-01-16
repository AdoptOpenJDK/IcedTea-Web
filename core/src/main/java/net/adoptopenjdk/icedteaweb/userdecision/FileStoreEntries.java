package net.adoptopenjdk.icedteaweb.userdecision;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

class FileStoreEntries {

    @SuppressWarnings("unused") // this is used for identifying the serialization version in the JSON file.
    private final String version = "1";
    private final List<FileStoreEntry> userDecisions;

    FileStoreEntries() {
        this.userDecisions = new ArrayList<>();
    }

    <T extends Enum<T>> Optional<T> getDecision(final URL domain, final Set<String> jarNames, final UserDecision.Key key, final Class<T> resultType) {
        final T[] enumConstants = resultType.getEnumConstants();
        return userDecisions.stream()
                .filter(entry -> entry.getDomain().equals(domain))
                .filter(entry -> entry.getJarNames().isEmpty() || entry.getJarNames().equals(jarNames))
                .max(Comparator.comparingInt(entry -> entry.getJarNames().size()))
                .flatMap(entry -> entry.getUserDecisionValue(key))
                .filter(value -> Arrays.stream(enumConstants).anyMatch((t) -> t.name().equals(value)))
                .map(value -> Enum.valueOf(resultType, value));
    }

    <T extends Enum<T>> void addDecision(final URL domain, final Set<String> jarNames, final UserDecision<T> userDecision) {
        final FileStoreEntry foundEntry = userDecisions.stream()
                .filter(entry -> entry.getDomain().equals(domain))
                .filter(entry -> entry.getJarNames().equals(jarNames))
                .findFirst()
                .orElseGet(() -> {
                    final FileStoreEntry newEntry = new FileStoreEntry(domain, jarNames);
                    userDecisions.add(newEntry);
                    return newEntry;
                });

        foundEntry.setUserDecision(userDecision);
    }
}
