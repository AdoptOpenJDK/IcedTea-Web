package net.sourceforge.jnlp.deploymentrules;


/*
 * This class copies all the deployment Rule set to a class.
 * <ruleset version="1.0+">
  <rule> <!-- allow anything signed with company's public cert --> 
    <id>
      <certificate hash="84B843BE942CAB4309F258FD946D62A6C4CCEAB8E1DB2C6" />
    </id>
    <action permission="run" version="SECURE" />
  </rule>

  <rule>
    <id location="*.example.com" />
    <action permission="default" />
  </rule>

  <rule>
    <id />
    <action permission="block">
      <message>Blocked by corporate. Contact J. Smith, smith@host.example.com, if you need to run this app.</message>
    </action>
  </rule>
  <rule>
    <id location="https://host.example.com" />
    <action permission="run" version="SECURE-1.7" />
  </rule>
</ruleset> 
 * */


public class DeploymentRule {

private Rule rule;

public Rule getRule() {
	return rule;
}

public String getTitle() {
	return title;
}
public void setTitle(String title) {
	this.title = title;
}

	
private Action action;

public Action getAction() {
	return action;
}
public void setAction(Action action) {
	this.action = action;
}

private String version;


public String getVersion() {
	return version;
}
public void setVersion(String version) {
	this.version = version;
}

private String title;


}
