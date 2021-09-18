package net.sourceforge.jnlp.deploymentrules;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.xmlparser.ParseException;
import net.adoptopenjdk.icedteaweb.xmlparser.XmlNode;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.IpUtil;

import java.net.URL;
import java.util.List;

import static java.util.Collections.emptyList;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_DEPLOYMENT_RULE_SET;

public class UrlDeploymentRulesSetUtils {

    private static final Logger LOG = LoggerFactory.getLogger(UrlDeploymentRulesSetUtils.class);

    private static List<Rule> deploymentRules;

    public static boolean isUrlInDeploymentRuleSet(final URL url) {
        Assert.requireNonNull(url, "url");
        return isUrlInDeploymentRuleSetUrl(url, getApplicationLinkDeploymentRuleSetList());
    }

    private static boolean isUrlInDeploymentRuleSetUrl(final URL url, final List<Rule> deploymentRuleSetList) {
        if (deploymentRuleSetList.isEmpty()) {
            return true; // empty deploymentRuleset == allow all connection
        }

        if (IpUtil.isLocalhostOrLoopback(url)) {
            return true; // localhost need not be in whitelist
        }

        return deploymentRuleSetList.stream().anyMatch(wlEntry -> wlEntry.matches(url));
    }

    private static List<Rule> getApplicationLinkDeploymentRuleSetList() {
        if (deploymentRules == null) {
            deploymentRules = loadDeploymentRuleSetLinksFromConfiguration();
        }
        return deploymentRules;
    }

    private static List<Rule> loadDeploymentRuleSetLinksFromConfiguration() {
        try {
            final String rulesetPath = JNLPRuntime.getConfiguration().getProperty(KEY_DEPLOYMENT_RULE_SET);
            return parseDeploymentRuleSet(rulesetPath);
        } catch (ParseException e) {
            LOG.error("Please Check config property " + KEY_DEPLOYMENT_RULE_SET + ". This should point to a valid DeploymentRuleSet jar file: ", e);
            return emptyList();
        }
    }

    private static List<Rule> parseDeploymentRuleSet(String rulesetPath) throws ParseException {
        final RulesetJarFile rulesetJarFile = new RulesetJarFile(rulesetPath);
        if (rulesetJarFile.isNotPresent()) {
            return emptyList();
        }

        final XmlNode root = rulesetJarFile.getRulesetXml();
        final RulesetParser parser = new RulesetParser();
        return parser.getRules(root);
    }
}
