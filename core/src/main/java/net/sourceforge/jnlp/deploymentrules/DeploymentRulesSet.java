package net.sourceforge.jnlp.deploymentrules;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

//import javax.xml.bind.JAXBContext;
//import javax.xml.bind.JAXBException;
//import javax.xml.bind.Marshaller;
//import javax.xml.bind.Unmarshaller;
//import javax.xml.bind.annotation.*;

import net.adoptopenjdk.icedteaweb.xmlparser.ParseException;
import net.adoptopenjdk.icedteaweb.xmlparser.XMLParser;
import net.adoptopenjdk.icedteaweb.xmlparser.XmlNode;
import net.adoptopenjdk.icedteaweb.xmlparser.XmlParserFactory;
public class DeploymentRulesSet {
    public static final String DEPLOYMENTRULE_SET_ROOT_ELEMENT = "deploymentRulesSet";
    public static final String RULE_SET_ELEMENT="ruleset";
    //From rule starts the actual list of rule and locations stored.
    public static final String RULE_ELEMENT="rule";
    public static final String ID_ELEMENT="id";
    public static final String CERTIFICATE_ELEMENT="certificate";
    public static final String ACTION_ELEMENT="action";
    public static final String MESSAGE_ELEMENT="message";
    //id element
    public static final String LOCATION_ATTRIBUTE = "location";
    //certificate element
    public static final String HASH_ATTRIBUTE = "hash";
    //action element
    public static final String VERSION_ATTRIBUTE = "version";
    public static final String PERMISSION_ATTRIBUTE = "permission";


	private List<Rule> list;
	private List<String> vettedUrls =  new ArrayList<String>();;
	private ParserSettings parserSettings;
	
    public List<String> getVettedUrls() {
		return vettedUrls;
	}

    private ArrayList<Rule> ruleSet;
    public List<Rule> getRuleSet() {
		return ruleSet;
	}
	public void setRuleSet(List<Rule> rules) {
		this.ruleSet = (ArrayList<Rule>) rules;
	}

	private String version;
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	private static final String RULESET_XML = "./ruleset-jaxb.xml";
	
public static void main(String[] args) {
	//For testing
	DeploymentRulesSet ruleSet= new DeploymentRulesSet();
	try {
		ruleSet.parseDeploymentRuleSet("C:\\\\softwares\\\\icedtea-web\\\\DeploymentRuleSet.jar");
	} catch (ParseException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
 
}

/**
 * @param jarFilePath
 * @throws ParseException
 */
public void parseDeploymentRuleSet(String jarFilePath) throws ParseException{
	JarFile file;
	JarEntry entry = null;
	InputStream in=null;
	String appendedXML=null;
	if (new File(jarFilePath).exists()) {
	try {
		
			file = new JarFile(new File(jarFilePath));
			entry = file.getJarEntry("ruleset.xml");
			if (entry!=null) {
			 in= file.getInputStream(entry);
	
			 StringBuilder textBuilder = new StringBuilder();
			    try (Reader reader = new BufferedReader(new InputStreamReader
			      (in, Charset.forName(StandardCharsets.UTF_8.name())))) {
			        int c = 0;
			        while ((c = reader.read()) != -1) {
			            textBuilder.append((char) c);
			        }
			    }
			 String content= textBuilder.toString();
			    int insertCount=content.indexOf("<ruleset");
			 String fullXml=content.substring(insertCount, content.length());
			 String sub=content.substring(0, insertCount);
			  appendedXML=sub+"<deploymentRulesSet>"+fullXml+"</deploymentRulesSet>";
			}
			System.out.println(appendedXML);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			new ParseException("file IO exception accessing the ruleset or some network issues" + e1.getMessage());
		}
		

	parserSettings = new ParserSettings(true, false,true);
    final XMLParser xmlParser = XmlParserFactory.getParser(parserSettings.getParserType());
    InputStream is = new ByteArrayInputStream(appendedXML.getBytes(StandardCharsets.UTF_8));
    XmlNode root=null;
	try {
		root = xmlParser.getRootNode(is);
	} catch (ParseException e) {
		new ParseException("Could not parser the root Node" +e.getMessage());
	}
    
    DeploymentRulesSet ruleSetDescriptor = new DeploymentRulesSet();
    try {
		DeploymentRuleSetParser parser= new DeploymentRuleSetParser(ruleSetDescriptor, root, parserSettings);
	} catch (ParseException e) {
		new ParseException("Could not intialize the DeploymentRuleSetParser" +e.getMessage());

	}
      list = ruleSetDescriptor.getRuleSet();
     parseDeploymentRuleSet();
	}
}

private void parseDeploymentRuleSet() {
	for (Rule rules: list) {
		//Questions.. Do we also accept Urls to be vetted if DEFAULT permissions 
		if (rules.getAction().getPermission().matches(PermissionsConstant.RUN)) {
			vettedUrls.add(rules.getLocation());
		}
	}
}

}
