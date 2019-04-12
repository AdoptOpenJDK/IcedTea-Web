package net.adoptopenjdk.icedteaweb.xmlparser;

import java.util.Objects;

public class ElementName {

    private final String base;

    public ElementName(final String base) {
        this.base = Objects.requireNonNull(base);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof ElementName) {
            return Objects.equals(((ElementName) obj).base, base);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return base.hashCode();
    }

    public String getName() {
        if (base.contains(":")) {
            return base.split(":")[1];
        } else {
            return base;
        }
    }
    private String getPrefix() {
        if (base.contains(":")) {
            return base.split(":")[0];
        } else {
            return "";
        }
    }

    public String getOriginal() {
        return base + "(" + getPrefix() + ":" + getName() + ")";
    }

}
