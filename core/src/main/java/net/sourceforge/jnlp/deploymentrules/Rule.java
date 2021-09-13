package net.sourceforge.jnlp.deploymentrules;

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
        // TODO: implement according to https://docs.oracle.com/javase/8/docs/technotes/guides/deploy/deployment_rules.html#CIHDCEDE
        // Maybe take some inspiration from ParsedWhitelistEntry.matches(URL)
        return false;
    }
}
