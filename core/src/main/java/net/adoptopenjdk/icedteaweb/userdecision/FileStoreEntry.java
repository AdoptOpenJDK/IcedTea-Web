package net.adoptopenjdk.icedteaweb.userdecision;

import net.adoptopenjdk.icedteaweb.Assert;

import java.net.URL;
import java.util.List;
import java.util.Objects;

public class FileStoreEntry {
    private UserDecision.Key userDecisionKey;
    private String userDecisionValue;

    private URL domain;
    private List<String> jarNames;

    public <T extends Enum<T>> FileStoreEntry(UserDecision.Key userDecisionKey, String userDecisionValue, final URL domain, final List<String> jarNames) {
        this.userDecisionKey = Assert.requireNonNull(userDecisionKey, "userDecisionKey");
        this.userDecisionValue = Assert.requireNonNull(userDecisionValue, "userDecisionValue");
        this.domain = Assert.requireNonNull(domain, "domain");
        this.jarNames = Assert.requireNonNull(jarNames, "jarNames");
    }

    public UserDecision.Key getUserDecisionKey() {
        return userDecisionKey;
    }

    public String getUserDecisionValue() {
        return userDecisionValue;
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
        return userDecisionKey.equals(that.userDecisionKey) &&
                userDecisionValue.equals(that.userDecisionValue) &&
                domain.equals(that.domain) &&
                jarNames.equals(that.jarNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userDecisionKey, userDecisionValue, domain, jarNames);
    }
}
