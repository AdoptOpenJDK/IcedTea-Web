package net.sourceforge.jnlp.deploymentrules;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.JavaSystemProperties;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.jnlp.element.EntryPoint;

import net.adoptopenjdk.icedteaweb.jvm.JvmUtils;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.ui.swing.ScreenFinder;
import net.adoptopenjdk.icedteaweb.xmlparser.ParseException;
import net.adoptopenjdk.icedteaweb.xmlparser.XMLParser;
import net.adoptopenjdk.icedteaweb.xmlparser.XmlNode;


import javax.swing.JOptionPane;
import java.awt.Rectangle;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Boolean.*;
import static java.util.Arrays.asList;

import static net.adoptopenjdk.icedteaweb.xmlparser.NodeUtils.getAttribute;
import static net.adoptopenjdk.icedteaweb.xmlparser.NodeUtils.getChildNode;
import static net.adoptopenjdk.icedteaweb.xmlparser.NodeUtils.getChildNodes;
import static net.adoptopenjdk.icedteaweb.xmlparser.NodeUtils.getRequiredAttribute;
import static net.adoptopenjdk.icedteaweb.xmlparser.NodeUtils.getRequiredURL;
import static net.adoptopenjdk.icedteaweb.xmlparser.NodeUtils.getSpanText;
import static net.adoptopenjdk.icedteaweb.xmlparser.NodeUtils.getURL;


/**
* Contains methods to parse an XML document into a DeploymentRuleSetFile. Implements JNLP
* specification version 1.0.
*
* @author <a href="mailto:dhirenjoshi@gmail.com">Dhiren Joshi
* (JAM)</a> - initial author
* @version $Revision: 1.13 $
*/
public final class  DeploymentRuleSetParser  {

 private static final Logger LOG = LoggerFactory.getLogger(DeploymentRuleSetParser.class);


 
 /**
  * the file reference
  */
 private  DeploymentRulesSet file=null; // do not use (uninitialized)

 /**
  * the root node
  */
 private final XmlNode root;
 /**
  * whether to throw errors on non-fatal errors.
  */
 private final boolean strict; // if strict==true parses a file with no error then strict==false should also

 /**
  * whether to allow extensions to the JNLP specification
  */
 private final boolean allowExtensions;  

 

 /**
  * Create a parser for the Deployment rule set file
  * Reads the jar and ruleset.xml file is read and parsed. Adds a deploymentRuleSet tag to cover the legalities
  * If any with using a Oracle ruleset.xml.
  *
  *  * @throws ParseException if the  DeploymentRuleSet string is invalid
  */
 /**
 * @param ruleSet the object created from the parsed ruleset.xml
 * @param root , the root XmlNode
 * @param settings , the parser settings
 * @throws ParseException
 */
public DeploymentRuleSetParser(final DeploymentRulesSet ruleSet,  final XmlNode root, final ParserSettings settings) throws ParseException {
     this.file = file;
     this.root = root;
     this.strict = settings.isStrict();
     this.allowExtensions = settings.isExtensionAllowed();

     // ensure it's a DeploymentRuleSet node
     if (root == null || !root.getNodeName().equals(DeploymentRulesSet.DEPLOYMENTRULE_SET_ROOT_ELEMENT)) {
         throw new ParseException("Root element is not a DeploymentRuleset element.");
     }
     processXmlParsingOfRuleSet(ruleSet,root);
   
  }

 

/**
 * Returns the rule attributes populated
 * @param rule
 * @param root
 * @return rule object populated with attributes values
 */
public Rule getRuleIdAttributeValues(Rule rule, XmlNode root) {
	 //certificate element
     final String hash = getAttribute(root, DeploymentRulesSet.HASH_ATTRIBUTE, null);
     //id element
     Certificate certs= new Certificate();
     certs.setHash(hash);
     final String location = getAttribute(root, DeploymentRulesSet.LOCATION_ATTRIBUTE, null);
     rule.setcertificate(certs);
     rule.setLocation(location);
     return rule;
 }

 /**
  *
 * @param action
 * @param root
 * @return Action attributes sets
 */
public Action getActionAttributes(Action action, XmlNode root) {
     //action element
     final String permission = getAttribute(root, DeploymentRulesSet.PERMISSION_ATTRIBUTE, null);
     String version = getAttribute(root, DeploymentRulesSet.VERSION_ATTRIBUTE, null);
     action.setPermission(permission);
     action.setVersion(version);
     return action;
 }

/**
 * @param ruleSet
 * @param parent
 * @throws ParseException
 */
public void processXmlParsingOfRuleSet(DeploymentRulesSet ruleSet, final XmlNode parent) throws ParseException {
     Rule rule = null;
     List<Rule> rules= new ArrayList<Rule>();
     XmlNode child = parent.getFirstChild();
     XmlNode childRuleSet =null;
         if (child.getNodeName().equals(DeploymentRulesSet.RULE_SET_ELEMENT)) {
              final XmlNode node = child;
               if (!child.getNodeName().equals(DeploymentRulesSet.RULE_SET_ELEMENT)) {
            	  throw new ParseException("Invalid Deployment rule set <ruleset> tag is missing");
              }else {
            	  
            	 rules = getRules(child);
              }
              
         }
     ruleSet.setRuleSet(rules);
     
 }


 /**
 * @param parent
 * @return List<Rule> list of Rules from deplyoment rule set
 * @throws ParseException
 */
public List<Rule> getRules(final XmlNode parent)
         throws ParseException {
     final List<Rule> result = new ArrayList<Rule>();
     final XmlNode rules[] = getChildNodes(parent, DeploymentRulesSet.RULE_ELEMENT);

     // ensure that there are at least one information section present
     if (rules.length == 0 ) {
         throw new ParseException("No  rule <rule> element specified.");
     }
     for (final XmlNode rule : rules) {
         result.add(getRule(rule));
     }
     return result;
 }

/**
 * @return the Rule element at the specified node.
 * @param node
 * @return
 * @throws ParseException if the Rule eement does not exist
 */
private Rule getRule(final XmlNode node) throws ParseException {

     // create rules
     Rule rule=new Rule();

     // step through the elements
     //first populate the id tag attribute
     XmlNode child = node.getFirstChild();
         final String name = child.getNodeName();
              if (name.equals(DeploymentRulesSet.ID_ELEMENT)) {
                getRuleIdAttributeValues(rule, child);
             }
         //next populate the action tag attribute.     
         child = child.getNextSibling();
         if (child.getNodeName().equals(DeploymentRulesSet.ACTION_ELEMENT)) {
             Action action= new Action();
             rule.setAction(action);
             getActionAttributes(action, child);
         }
       return rule;
 }
 
}

