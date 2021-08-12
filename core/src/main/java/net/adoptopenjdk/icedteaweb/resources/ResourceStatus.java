package net.adoptopenjdk.icedteaweb.resources;

public enum ResourceStatus {
    INCOMPLETE,
    DOWNLOADED,
    ERROR;

    private final String shortName;

    ResourceStatus() {
        shortName = name().substring(0,1);
    }

    public String getShortName() {
        return shortName;
    }
}
