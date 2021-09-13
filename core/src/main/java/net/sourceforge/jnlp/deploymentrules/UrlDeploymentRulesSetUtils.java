package net.sourceforge.jnlp.deploymentrules;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.xmlparser.ParseException;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

import java.net.URL;
import java.util.Collections;
import java.util.List;

import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_DEPLOYMENT_RULE_SET;

public class UrlDeploymentRulesSetUtils {

    private static final Logger LOG = LoggerFactory.getLogger(UrlDeploymentRulesSetUtils.class);

    private static List<String> applicationDeploymentRuleSetList;

    private static List<String> getApplicationLinkDeploymentRuleSetList() {
        if (applicationDeploymentRuleSetList == null) {
            applicationDeploymentRuleSetList = loadDeploymentRuleSetLinksFromConfiguration();
        }
        return applicationDeploymentRuleSetList;
    }

    private static List<String> loadDeploymentRuleSetLinksFromConfiguration() {
        try {
            final String rulesetPath = JNLPRuntime.getConfiguration().getProperty(KEY_DEPLOYMENT_RULE_SET);
            final RulesetJarFile rulesSetFile = new RulesetJarFile(rulesetPath);
            return rulesSetFile.parseDeploymentRuleSet();
        } catch (ParseException e) {
            LOG.error("Please Check config property " + KEY_DEPLOYMENT_RULE_SET + ". This should point to a valid DeploymentRuleSet jar file: ", e);
            return Collections.emptyList();
        }
    }

    public static boolean isUrlInDeploymentRuleSet(final URL url) {
        Assert.requireNonNull(url, "url");
        return isUrlInDeploymentRuleSetUrl(url, getApplicationLinkDeploymentRuleSetList());
    }

    private static boolean isUrlInDeploymentRuleSetUrl(final URL url, final List<String> deploymentRuleSetList) {
        if (deploymentRuleSetList.isEmpty()) {
            return false; // empty deploymentRuleSetList == allow none. Nothing is whitelisted
        }
        return deploymentRuleSetList.stream().anyMatch(wlEntry -> wlEntry.matches(url.getHost()));
    }
}
