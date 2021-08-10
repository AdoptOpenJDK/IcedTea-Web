package net.sourceforge.jnlp.deploymentrules;


/*
 *   <id location="*.example1.com">
      <certificate hash="84B843BE942CAB4309F258FD946D62A6C4CCEAB8E1DB2C6" />
    </id>
  <id location="*.example.com" />
 */
public class Rule {
	private String location;
	
	private Certificate certificate;
	public Certificate getCertificate() {
		return certificate;
	}
	public void setcertificate(Certificate certificate) {
		this.certificate = certificate;
	}
	
	private Action action;

	 
	
	public Action getAction() {
		return action;
	}
	public void setAction(Action action) {
		this.action = action;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}


}
