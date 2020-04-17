package net.adoptopenjdk.icedteaweb.userdecision;

import java.util.Collections;
import java.util.List;

public class FileStoreEntries {
    private final List<FileStoreEntry> userDecisions;

    public FileStoreEntries(final List<FileStoreEntry> userDecisions) {
        this.userDecisions = Collections.unmodifiableList(userDecisions);
    }

    public List<FileStoreEntry> getUserDecisions() {
        return userDecisions;
    }
}
