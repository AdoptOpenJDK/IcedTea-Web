package net.sourceforge.jnlp.deploymentrules;

class XmlRule {
    private String location;
    private XmlCertificate certificate;
    private XmlAction action;

    public String getLocation() {
        return location;
    }

    public void setLocation(final String location) {
        this.location = location;
    }

    public XmlCertificate getCertificate() {
        return certificate;
    }

    public void setCertificate(final XmlCertificate certificate) {
        this.certificate = certificate;
    }

    public XmlAction getAction() {
        return action;
    }

    public void setAction(final XmlAction action) {
        this.action = action;
    }
}
