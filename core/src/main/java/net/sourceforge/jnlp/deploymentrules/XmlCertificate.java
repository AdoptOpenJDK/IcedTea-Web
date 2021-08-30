package net.sourceforge.jnlp.deploymentrules;

/**
 * Certificate object of Rule from the ruleset file
 * Stores the attributes value from action tag hash.
 * This is class is rarely used yet and can be extended when a
 * UI component to display the entire ruleset.xml file and edit it will be enhanced
 */
class XmlCertificate {

    private String hash;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
