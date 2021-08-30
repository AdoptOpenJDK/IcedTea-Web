package net.sourceforge.jnlp.deploymentrules;

import net.adoptopenjdk.icedteaweb.io.IOUtils;
import net.adoptopenjdk.icedteaweb.xmlparser.ParseException;
import net.adoptopenjdk.icedteaweb.xmlparser.XMLParser;
import net.adoptopenjdk.icedteaweb.xmlparser.XmlNode;
import net.adoptopenjdk.icedteaweb.xmlparser.XmlParserFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import static net.adoptopenjdk.icedteaweb.xmlparser.ParserType.MALFORMED;

class DeploymentRulesSetFile {

    private static final String RULESET_XML = "ruleset.xml";
    private static final String RUN = "run";

    private final String rulesetPath;

    public DeploymentRulesSetFile(String rulesetPath) {
        this.rulesetPath = rulesetPath;
    }

    public List<String> parseDeploymentRuleSet() throws ParseException {
        final File rulesetJarFile = new File(rulesetPath);
        if (rulesetJarFile.exists()) {
            final String rawContent = getRulesetXmlContent(rulesetJarFile);
            final String content = wrapInArtificialRoot(rawContent);
            final XmlNode root = parseXml(content);
            final List<XmlRule> rules = extractRules(root);

            return rules.stream()
                    .filter(rule -> Objects.equals(rule.getAction().getPermission(), RUN))
                    .map(XmlRule::getLocation)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private String getRulesetXmlContent(File rulesetJarFile) throws ParseException {
        try {
            final JarFile file = new JarFile(rulesetJarFile);
            final JarEntry entry = file.getJarEntry(RULESET_XML);
            if (entry == null) {
                throw new ParseException("could not find a " + RULESET_XML + " in the jar " + rulesetJarFile);
            }

            try (final InputStream in = file.getInputStream(entry)) {
                return IOUtils.readContentAsUtf8String(in);
            }
        } catch (IOException e) {
            throw new ParseException("file IO exception accessing the ruleset or some network issues", e);
        }
    }

    private String wrapInArtificialRoot(String content) throws ParseException {
        final int idx = content.indexOf("<ruleset");
        if (idx < 0) {
            throw new ParseException("Could not find any <ruleset>");
        }
        final String prefix = content.substring(0, idx);
        final String fullXml = content.substring(idx);
        return prefix + "<deploymentRulesSet>" + fullXml + "</deploymentRulesSet>";
    }

    private XmlNode parseXml(String content) throws ParseException {
        try {
            final InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            final XMLParser xmlParser = XmlParserFactory.getParser(MALFORMED);
            return xmlParser.getRootNode(is);
        } catch (ParseException e) {
            throw new ParseException("Could not parser the root Node" + e.getMessage());
        }
    }

    private List<XmlRule> extractRules(XmlNode root) throws ParseException {
        try {
            return new DeploymentRuleSetParser().getRules(root);
        } catch (ParseException e) {
            throw new ParseException("Could not initialize the DeploymentRuleSetParser" + e.getMessage());
        }
    }

}
