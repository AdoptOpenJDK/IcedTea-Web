package net.sourceforge.jnlp.deploymentrules;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import org.apache.commons.io.IOUtils;


@XmlRootElement//(namespace = "net.adoptopenjdk.icedtea.web")
@XmlAccessorType(XmlAccessType.FIELD)
public class DeploymentRulesSet {
	private List<DeploymentRule> list;
	private List<String> vettedUrls;
	
    public List<String> getVettedUrls() {
		return vettedUrls;
	}
	@XmlElementWrapper(name = "ruleset")
    // XmlElement sets the name of the entities
    @XmlElement(name = "rule")
    private ArrayList<DeploymentRule> ruleSet;
    public ArrayList<DeploymentRule> getRuleSet() {
		return ruleSet;
	}
	public void setRuleSet(ArrayList<DeploymentRule> ruleSet) {
		this.ruleSet = ruleSet;
	}
	@XmlAttribute(name = "version")
	private String version;
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	private static final String RULESET_XML = "./ruleset-jaxb.xml";
public static void main(String[] args) {
	DeploymentRulesSet ruleSet= new DeploymentRulesSet();
	ruleSet.parseDeploymentRuleSet("C:\\\\softwares\\\\icedtea-web\\\\DeploymentRuleSet.jar");
	//InputStream in = ruleSet.getResourceAsStream("C:\\softwares\\icedtea-web\\DeploymentRuleSetTest.jar\\ruleset.xml"); 
	//BufferedReader reader = new BufferedReader(new InputStreamReader(in));
 
}

public void parseDeploymentRuleSet(String jarFilePath) {
	JarFile file;
	JarEntry entry = null;
	InputStream in=null;
	String appendedXML=null;
	if (new File(jarFilePath).exists()) {
	try {
		
			file = new JarFile(new File(jarFilePath));
			//file = new JarFile(new File("C:\\\\softwares\\\\icedtea-web\\\\DeploymentRuleSetTest.jar"));
			//entry = file.getJarEntry("ruleset-jaxb.xml");
			
			entry = file.getJarEntry("ruleset.xml");
			if (entry!=null) {
			 in= file.getInputStream(entry);
			 String content = IOUtils.toString(file.getInputStream(entry));
			 int insertCount=content.indexOf("<ruleset");
			 String fullXml=content.substring(insertCount, content.length());
			 String sub=content.substring(0, insertCount);
			  appendedXML=sub+"<deploymentRulesSet>"+fullXml+"</deploymentRulesSet>";
			}
			System.out.println(appendedXML);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		//URL url = ruleSet.getResourceAsStream("C:\\softwares\\icedtea-web\\ruleset.xml");
		//ServicesLoader jsl = new ServicesLoader( url.toString() );
	    JAXBContext context;
	    if (appendedXML !=null) {
			try {
				context = JAXBContext.newInstance(DeploymentRulesSet.class);
				   Marshaller m = context.createMarshaller();
				    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		
				    Unmarshaller um = context.createUnmarshaller();
		//		    DeploymentRulesSet bookstore2 = (DeploymentRulesSet) um.unmarshal(new FileReader(
		//		    		RULESET_XML));
				    StringReader reader = new StringReader(appendedXML);
				    DeploymentRulesSet ruleSetDescriptor = (DeploymentRulesSet) um.unmarshal(reader);
		
				     list = ruleSetDescriptor.getRuleSet();
				     parseDeploymentRuleSet();
				    System.out.println("Done"+list.toString());
		
			} catch (JAXBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	}
}

private void parseDeploymentRuleSet() {
	for (DeploymentRule rules: list) {
		//Questions.. Do we also accept Urls to be vetted if DEFAULT permissions 
		if (rules.getRuleAction().getPermission().matches(PermissionsConstant.RUN)) {
			vettedUrls.add(rules.getRuleInfo().getLocation());
		}
	}
}

}
