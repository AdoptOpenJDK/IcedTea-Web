package net.sourceforge.jnlp.deploymentrules;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.StringUtils;
//import net.adoptopenjdk.icedteaweb.deploymentruleset.util.UrlDeploymentRuleSetUtil;
//import net.adoptopenjdk.icedteaweb.deploymentruleset.DeploymentRulesSet;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.IpUtil;
import net.sourceforge.jnlp.util.whitelist.UrlWhiteListUtils;

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

    private static List<DeploymentRule> applicationUrlDeploymentRuleSetList;
    private static List<String> applicationDeploymentRuleSetList;
    private final static DeploymentRulesSet rulesSet= new DeploymentRulesSet();
    private static  boolean isRuleSetInitialized=false;

    public static List<DeploymentRule> getApplicationUrlDeploymentRuleSetList() {
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
    	rulesSet.parseDeploymentRuleSet(deploymentRuleSetPropertyName);
    	isRuleSetInitialized=true;
    }
  
    public static  List<DeploymentRule> loadRuleSetFromConfiguration(final String deploymentRuleSetJarPath) {
    	List<DeploymentRule> rulesSetList=null;
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
    public static List<DeploymentRule> loadDeploymentRuleSetFromConfiguration(final String deploymentRuleSetJarPath) {
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
    
    /*
    static WhitelistEntry parseEntry(final String wlUrlStr) {
        Assert.requireNonNull(wlUrlStr, "wlUrlStr");
        return WhitelistEntry.parse(wlUrlStr);
    }
    /**
 * Validate the security certificates (signers) for the class data.
 *
private Certificate[] getSigners(String className, JarEntry je) throws IOException {

    try {
        Certificate[] list = je.getCertificates();
        if ((list == null) || (list.length == 0)) {
            return null;
        }

        for (Certificate aList : list) {
            if (!(aList instanceof X509Certificate)) {
                String msg = MessageService.getTextMessage(
                        MessageId.CM_UNKNOWN_CERTIFICATE, className,
                        getJarName());

                throw new SecurityException(msg);
            }

            X509Certificate cert = (X509Certificate) aList;

            cert.checkValidity();
        }

        return list;

    } catch (GeneralSecurityException gse) {
        // convert this into an unchecked security
        // exception. Unchecked as eventually it has
        // to pass through a method that's only throwing
        // ClassNotFoundException
        throw handleException(gse, className);
    }
    
}
 
*/
}
