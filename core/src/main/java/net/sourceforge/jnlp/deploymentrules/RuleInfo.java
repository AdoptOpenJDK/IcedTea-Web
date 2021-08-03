package net.sourceforge.jnlp.deploymentrules;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.eclipse.persistence.oxm.annotations.XmlPath;

/*
 *   <id location="*.example1.com">
      <certificate hash="794F53C746E2AA77D84B843BE942CAB4309F258FD946D62A6C4CCEAB8E1DB2C6" />
    </id>
  <id location="*.example.com" />
 */

@XmlRootElement(name = "id")
@XmlAccessorType(XmlAccessType.FIELD)
public class RuleInfo {
	@XmlElement(name="certificate")
	private Certificate certificate;
	public Certificate getCertificate() {
		return certificate;
	}
	public void setcertificate(Certificate certificate) {
		this.certificate = certificate;
	}
	@XmlAttribute(name="location")
	private String location;
	@XmlElement(name="action")
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
