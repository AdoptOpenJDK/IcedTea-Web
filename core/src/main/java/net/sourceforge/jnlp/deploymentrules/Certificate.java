package net.sourceforge.jnlp.deploymentrules;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

//import org.eclipse.persistence.oxm.annotations.XmlPath;

@XmlRootElement(name = "certificate")
//If you want you can define the order in which the fields are written
//Optional
//@XmlType(propOrder = { "id", "action","message"})

@XmlAccessorType(XmlAccessType.FIELD)

public class Certificate {
@XmlAttribute(name="hash")
private String hash;


public String getHash() {
	return hash;
}

public void setHash(String hash) {
	this.hash = hash;
}
}
