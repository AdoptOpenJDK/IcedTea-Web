package net.adoptopenjdk.icedteaweb.deploymentrules;

import java.net.URL;

/**
 * See https://docs.oracle.com/javase/8/docs/technotes/guides/deploy/deployment_rules.html#CIHDCEDE
 */
class Rule {
    private String location;
    private Certificate certificate;
    private Action action;

    public String getLocation() {
        return location;
    }

    public void setLocation(final String location) {
        this.location = location;
    }

    public Certificate getCertificate() {
        return certificate;
    }

    public void setCertificate(final Certificate certificate) {
        this.certificate = certificate;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(final Action action) {
        this.action = action;
    }

    public boolean matches(URL url) {
        // TODO: implement according to https://docs.oracle.com/javase/10/deploy/deployment-rule-set.htm#GUID-413F29CF-81B5-4154-9C52-22D993819C2B
        // Maybe take some inspiration from ParsedWhitelistEntry.matches(URL)
        return false;
    }

    public boolean isAllowedToRun() {
        // TODO: return true if this is allowed to run according to the action
        return false;
    }
}
