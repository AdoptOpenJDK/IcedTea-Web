package net.sourceforge.jnlp.deploymentrules;
/**
 * Action object of Rule from the rulset file
 * Stores the attributes value from id tag
 * permission and version.
 * If permission is run, then location which is the url whitelisted is permitted to be accessible.
 */
public class Action {

private	String permission;
private String version;
public String getVersion() {
	return version;
}
public void setVersion(String version) {
	this.version = version;
}
public String getPermission() {
	return permission;
}
public void setPermission(String permission) {
	this.permission = permission;
}

private 	String message;
public String getMessage() {
	return message;
}
public void setMessage(String message) {
	this.message = message;
}

}
