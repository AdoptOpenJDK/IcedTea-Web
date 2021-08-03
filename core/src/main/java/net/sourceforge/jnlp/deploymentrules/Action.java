package net.sourceforge.jnlp.deploymentrules;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "action")
@XmlAccessorType(XmlAccessType.FIELD)
public class Action {
	@XmlAttribute
private	String permission;
public String getPermission() {
	return permission;
}
public void setPermission(String permission) {
	this.permission = permission;
}
@XmlElement
private 	String message;
public String getMessage() {
	return message;
}
public void setMessage(String message) {
	this.message = message;
}

}
