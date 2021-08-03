package net.sourceforge.jnlp.deploymentrules;
import javax.xml.bind.annotation.*;
import org.eclipse.persistence.oxm.annotations.XmlPath;
/*
 * This class copies all the deployment Rule set to a class.
 * <ruleset version="1.0+">
  <rule> <!-- allow anything signed with company's public cert --> 
    <id>
      <certificate hash="794F53C746E2AA77D84B843BE942CAB4309F258FD946D62A6C4CCEAB8E1DB2C6" />
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
@XmlRootElement(name = "rule")
//If you want you can define the order in which the fields are written
//Optional
//@XmlType(propOrder = { "id", "action","message"})

@XmlAccessorType(XmlAccessType.FIELD)
public class DeploymentRule {

@XmlElement(name="id")	
private RuleInfo ruleInfo;

public RuleInfo getRuleInfo() {
	return ruleInfo;
}
public void setRuleInfo(RuleInfo ruleInfo) {
	this.ruleInfo = ruleInfo;
}


private String name;
 public String getName() {
	return name;
}
public void setName(String name) {
	this.name = name;
}
public String getTitle() {
	return title;
}
public void setTitle(String title) {
	this.title = title;
}
@XmlElement(name="action")	
private Action ruleAction;

public Action getRuleAction() {
	return ruleAction;
}
public void setRuleAction(Action ruleAction) {
	this.ruleAction = ruleAction;
}
@XmlAttribute
private String version;


public String getVersion() {
	return version;
}
public void setVersion(String version) {
	this.version = version;
}

@XmlElement(name="title")
private String title;


}
