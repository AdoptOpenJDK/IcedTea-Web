package net.sourceforge.jnlp.deploymentrules;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.StringUtils;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.xmlparser.ParseException;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.IpUtil;
import net.sourceforge.jnlp.deploymentrules.DeploymentRule;
import net.sourceforge.jnlp.deploymentrules.DeploymentRulesSet;
//import net.sourceforge.jnlp.util.whitelist.UrlWhiteListUtils;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
/*
 * Added DeploymentRuleSet white listing checks 
 * Added method call :isUrlInDeploymentRuleSetUrl
 * This method will do the checks of the DeploymentRuleSet.jar file of ruleset.xml
 * DJ- 3/02/2021 
 * 
 */
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_DEPLOYMENT_RULE_SET;
/*
 * @author DJ
 * @date 03/02/2021
 * This class is implementing the DeplyomentRuleSet checks for the jar
 * Added DeploymentRuleSet white listing checks 
 * Added method call :isUrlInDeploymentRuleSetUrl
 *  * 
 */
public class UrlDeploymentRulesSetUtils  {

    private static List<Rule> applicationUrlDeploymentRuleSetList;
    private static List<String> applicationDeploymentRuleSetList;
    private final static DeploymentRulesSet rulesSet= new DeploymentRulesSet();
    private static  boolean isRuleSetInitialized=false;
    private static final Logger LOG = LoggerFactory.getLogger(UrlDeploymentRulesSetUtils.class);


    public static List<Rule> getApplicationUrlDeploymentRuleSetList() {
        if (applicationUrlDeploymentRuleSetList == null) {
        	applicationUrlDeploymentRuleSetList = loadDeploymentRuleSetFromConfiguration(KEY_DEPLOYMENT_RULE_SET);
        }
        return applicationUrlDeploymentRuleSetList;
    }
    
    public static List<String> getApplicationLinkDeploymentRuleSetList() {
        if (applicationUrlDeploymentRuleSetList == null) {
        	applicationDeploymentRuleSetList = loadDeploymentRuleSetLinksFromConfiguration(KEY_DEPLOYMENT_RULE_SET);
        }
        return applicationDeploymentRuleSetList;
    }

    /**
     * @author-DJ
     * @date 03/02/21
     * Added for deploymentRuleSet checks
     * @param deploymentRuleSetJarPath
     * @return
     */
    public static List<String> loadDeploymentRuleSetLinksFromConfiguration(final String deploymentRuleSetPropertyName) {
    	initRulesSet(deploymentRuleSetPropertyName);
    	return rulesSet.getVettedUrls();
    }
    
    private static void initRulesSet(final String deploymentRuleSetPropertyName) {
    	try {
			rulesSet.parseDeploymentRuleSet(deploymentRuleSetPropertyName);
			isRuleSetInitialized=true;
		} catch (ParseException e) {
			LOG.error("Please Check property name . This should point to a valid DeploymentRuleSet jar file"+deploymentRuleSetPropertyName);
			//absorb the Error and send error message for trouble shooting.
			e.printStackTrace();
		}
    	
    }
  
    public static  List<Rule> loadRuleSetFromConfiguration(final String deploymentRuleSetJarPath) {
    	List<Rule> rulesSetList=null;
    	if (!isRuleSetInitialized) {
    		initRulesSet(deploymentRuleSetJarPath);
    	}else {
    		rulesSetList=rulesSet.getRuleSet();
    	}
    	return rulesSetList;
     }
    
    /**
     * @author-DJ
     * @date 03/02/21
     * Added for deploymentRuleSet checks
     * @param deploymentRuleSetJarPath
     * @return
     */
    public static List<Rule> loadDeploymentRuleSetFromConfiguration(final String deploymentRuleSetJarPath) {
    	//Implement the DeplymentRuleSet parser here. DJ and create the DeploymentRuleSet.
        return loadRuleSetFromConfiguration(deploymentRuleSetJarPath);
    }
    /**
     * 
     * Adding by DJ 3/2/2021 to add DeploymentRuleSet functionality
     * @param url
     * @return
     */
    public static boolean isUrlInDeploymentRuleSetlist(final URL url) {
        return isUrlInDeploymentRuleSetUrl(url, getApplicationLinkDeploymentRuleSetList());
    }

 
     /**
     * isUrlInDeploymentRuleSetUrl
     * Adding by DJ 3/2/2021 to add DeploymentRuleSet functionality
     * @param url
     * @param whiteList
     * @return
     */
    public static boolean isUrlInDeploymentRuleSetUrl(final URL url, final List<String> deploymentRuleSetList) {
        Assert.requireNonNull(url, "url");
        Assert.requireNonNull(deploymentRuleSetList, "whiteList");

        if (deploymentRuleSetList.isEmpty()) {
            return false; // empty deploymentRuleSetList == allow none. Nothing is whitelisted
        }

       
        
        return deploymentRuleSetList.stream().anyMatch(wlEntry -> wlEntry.matches(url.getHost()));
    }
    

}
