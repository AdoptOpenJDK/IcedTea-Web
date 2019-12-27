package net.sourceforge.jnlp.runtime.classloader2;

import java.net.URL;
import java.util.Objects;

public class Extension {

    private final URL extensionLocation;

    private final String version;

    public Extension(final URL extensionLocation, final String version) {
        this.extensionLocation = extensionLocation;
        this.version = version;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Extension extension = (Extension) o;
        return Objects.equals(extensionLocation, extension.extensionLocation) &&
                Objects.equals(version, extension.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(extensionLocation, version);
    }

    public URL getExtensionLocation() {
        return extensionLocation;
    }

    public String getVersion() {
        return version;
    }
}
