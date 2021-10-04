package net.adoptopenjdk.icedteaweb.deploymentrules;

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
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static net.adoptopenjdk.icedteaweb.xmlparser.ParserType.MALFORMED;

class RulesetJarFile {

    private static final String RULESET_XML = "ruleset.xml";

    private final File jarFile;

    public RulesetJarFile(String rulesetPath) {
        jarFile = new File(rulesetPath);
    }

    public XmlNode getRulesetXml() throws ParseException {
        final File rulesetJarFile = jarFile;

        if (!rulesetJarFile.exists()) {
            throw new RuntimeException("Ruleset jar file is missing");
        }

        final String content = getRulesetXmlContent(rulesetJarFile);
        return parseXml(content);
    }

    public String getRulesetXmlContent(File rulesetJarFile) throws ParseException {
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

    private XmlNode parseXml(String content) throws ParseException {
        try {
            final InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            final XMLParser xmlParser = XmlParserFactory.getParser(MALFORMED);
            return xmlParser.getRootNode(is);
        } catch (ParseException e) {
            throw new ParseException("Could not parser the root Node" + e.getMessage());
        }
    }

    public boolean isNotPresent() {
        return !jarFile.exists();
    }
}
