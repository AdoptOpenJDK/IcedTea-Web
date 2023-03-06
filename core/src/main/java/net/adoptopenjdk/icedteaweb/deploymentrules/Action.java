package net.adoptopenjdk.icedteaweb.deploymentrules;

/**
 * Action object of Rule from the ruleset file
 * Stores the attributes value from id tag permission and version.
 * If permission is run, then location which is the url whitelisted is permitted to be accessible.
 */
class Action {

    private String permission;
    private String version;
    private String message;

    public String getPermission() {
        return permission;
    }

    public void setPermission(final String permission) {
        this.permission = permission;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }
}
