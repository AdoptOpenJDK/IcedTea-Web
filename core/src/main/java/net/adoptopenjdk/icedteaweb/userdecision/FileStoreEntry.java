package net.adoptopenjdk.icedteaweb.userdecision;

import net.adoptopenjdk.icedteaweb.Assert;

import java.net.URL;
import java.util.List;
import java.util.Objects;

class FileStoreEntry {
    private UserDecision userDecision;
    private URL domain;
    private List<String> jarNames;

    public FileStoreEntry(final UserDecision userDecision, final URL domain, final List<String> jarNames) {
        this.userDecision = Assert.requireNonNull(userDecision, "userDecision");
        this.domain = Assert.requireNonNull(domain, "domain");
        this.jarNames = Assert.requireNonNull(jarNames, "jarNames");
    }

    public UserDecision getUserDecision() {
        return userDecision;
    }

    public URL getDomain() {
        return domain;
    }

    public List<String> getJarNames() {
        return jarNames;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof FileStoreEntry)) return false;
        final FileStoreEntry that = (FileStoreEntry) o;
        return userDecision.equals(that.userDecision) &&
                domain.equals(that.domain) &&
                jarNames.equals(that.jarNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userDecision, domain, jarNames);
    }
}
