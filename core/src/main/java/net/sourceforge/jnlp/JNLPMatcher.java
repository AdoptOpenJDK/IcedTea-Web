/* JNLPMatcher.java
   Copyright (C) 2011 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify it under the
terms of the GNU General Public License as published by the Free Software
Foundation, version 2.

IcedTea is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
IcedTea; see the file COPYING. If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is making a
combined work based on this library. Thus, the terms and conditions of the GNU
General Public License cover the whole combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent modules, and
to copy and distribute the resulting executable under terms of your choice,
provided that you also meet, for each linked independent module, the terms and
conditions of the license of that module. An independent module is a module
which is not derived from or based on this library. If you modify this library,
you may extend this exception to your version of the library, but you are not
obligated to do so. If you do not wish to do so, delete this exception
statement from your version.
*/

package net.sourceforge.jnlp;

import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.xmlparser.XMLParser;
import net.adoptopenjdk.icedteaweb.xmlparser.XmlNode;
import net.adoptopenjdk.icedteaweb.xmlparser.XmlParserFactory;
import net.sourceforge.jnlp.util.JarFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;

/**
 * To compare launching JNLP file with signed APPLICATION.JNLP or
 * APPLICATION_TEMPLATE.jnlp.
 * <p>
 * Used by net.sourceforge.jnlp.runtime.JNLPCLassLoader
 */

public final class JNLPMatcher {

    private static final Logger LOG = LoggerFactory.getLogger(JNLPMatcher.class);

    static final String TEMPLATE = "JNLP-INF/APPLICATION_TEMPLATE.JNLP";
    static final String APPLICATION = "JNLP-INF/APPLICATION.JNLP";

    private final File mainJarFile;
    private final File jnlpFile;
    private final ParserSettings p;

    /**
     * Public constructor
     *
     * @param mainJarFile the reader stream of the signed APPLICATION.jnlp or
     *                    APPLICATION_TEMPLATE.jnlp
     * @param jnlpFile    the reader stream of the launching JNLP file
     * @param p           the parser settings for the JNLP parsing
     */
    public JNLPMatcher(File mainJarFile, File jnlpFile, ParserSettings p) {
        this.mainJarFile = mainJarFile;
        this.jnlpFile = jnlpFile;
        this.p = p;
    }

    /**
     * Compares both JNLP files
     *
     * @return true if both JNLP files are 'matched', otherwise false
     */
    public boolean isMatch() {
        try (final JarFile jarFile = new JarFile(mainJarFile)) {
            for (JarEntry entry : Collections.list(jarFile.entries())) {
                final String entryName = entry.getName().toUpperCase();

                if (entryName.equals(APPLICATION)) {
                    LOG.debug("APPLICATION.JNLP has been located within signed JAR.");
                    return isMatch(jarFile, entry, false);
                } else if (entryName.equals(TEMPLATE)) {
                    LOG.debug("APPLICATION_TEMPLATE.JNLP has been located within signed JAR. Starting verification...");
                    return isMatch(jarFile, entry, true);
                }
            }
        } catch (IOException e) {
            LOG.error("Could not read local main jar file: {}", e.getMessage());
        }
        return false;
    }

    private boolean isMatch(JarFile jarFile, JarEntry entry, boolean isTemplate) {
        try (final InputStream jnlpStream = jarFile.getInputStream(entry)) {
            return isMatch(jnlpStream, isTemplate);
        } catch (IOException e) {
            LOG.error("Could not read JNLP jar entry: {}", e.getMessage());
            return false;
        }
    }

    private boolean isMatch(final InputStream appTemplateStream, final boolean isTemplate) {
        try (final InputStream launchJNLPStream = new FileInputStream(jnlpFile)) {
            return isMatch(appTemplateStream, launchJNLPStream, isTemplate);
        } catch (IOException e) {
            LOG.error("Could not read local JNLP file: {}", e.getMessage());
            return false;
        }
    }

    private boolean isMatch(final InputStream appTemplateStream, final InputStream launchJNLPStream, final boolean isTemplate) {
        try {
            final XMLParser xmlParser = XmlParserFactory.getParser(p.getParserType());
            final XmlNode appTemplateNode = xmlParser.getRootNode(appTemplateStream);
            final XmlNode launchJNLPNode = xmlParser.getRootNode(launchJNLPStream);
            final boolean result = matchNodes(appTemplateNode, launchJNLPNode, isTemplate);

            if (result) {
                LOG.debug("JNLP file verification successful");
            } else {
                LOG.warn("Signed JNLP file in main jar does not match launching JNLP file");
            }

            return result;
        } catch (Exception e) {
            LOG.error("Failed to create an instance of JNLPVerify with specified InputStreamReader: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Compares two Nodes regardless of the order of their children/attributes
     *
     * @param appTemplate signed application or template's Node
     * @param launchJNLP  launching JNLP file's Node
     * @return true if both Nodes are 'matched', otherwise false
     */
    private boolean matchNodes(XmlNode appTemplate, XmlNode launchJNLP, final boolean isTemplate) {

        if (appTemplate != null && launchJNLP != null) {

            // Store children of Node
            List<XmlNode> appTemplateChild = new LinkedList<>(Arrays.asList(appTemplate.getChildNodes()));
            List<XmlNode> launchJNLPChild = new LinkedList<>(Arrays.asList(launchJNLP.getChildNodes()));

            // Compare only if both Nodes have the same name, else return false
            if (appTemplate.getNodeName().equals(launchJNLP.getNodeName())) {

                if (appTemplateChild.size() == launchJNLPChild.size()) { // Compare
                    // children

                    int childLength = appTemplateChild.size();

                    for (int i = 0; i < childLength; ) {
                        for (int j = 0; j < childLength; j++) {
                            boolean isSame = matchNodes(appTemplateChild.get(i), launchJNLPChild.get(j), isTemplate);
                            if (!isSame && j == childLength - 1) {
                                return false;
                            } else if (isSame) { // If both child matches, remove them from the list of children
                                appTemplateChild.remove(i);
                                launchJNLPChild.remove(j);
                                --childLength;
                                break;
                            }
                        }
                    }

                    if (!appTemplate.getNodeValue().equals(launchJNLP.getNodeValue())) {

                        // If it's a template and the template's value is NOT '*'
                        if (isTemplate && !appTemplate.getNodeValue().equals("*")) {
                            return false;
                        }
                        // Else if it's not a template, then return false
                        else if (!isTemplate) {
                            return false;
                        }
                    }
                    // Compare attributes of both Nodes
                    return matchAttributes(appTemplate, launchJNLP, isTemplate);
                }

            }
        }
        return false;
    }

    /**
     * Compares attributes of two {@link XmlNode Nodes} regardless of order
     *
     * @param templateNode signed application or template's {@link XmlNode} with attributes
     * @param launchNode   launching JNLP file's {@link XmlNode} with attributes
     * @return {@code true} if both {@link XmlNode Nodes} have 'matched' attributes, otherwise {@code false}
     */
    private boolean matchAttributes(XmlNode templateNode, XmlNode launchNode, boolean isTemplate) {

        if (templateNode != null && launchNode != null) {

            List<String> appTemplateAttributes = templateNode.getAttributeNames();
            List<String> launchJNLPAttributes = launchNode.getAttributeNames();

            Collections.sort(appTemplateAttributes);
            Collections.sort(launchJNLPAttributes);

            if (appTemplateAttributes.size() == launchJNLPAttributes.size()) {

                int size = appTemplateAttributes.size(); // Number of attributes

                for (int i = 0; i < size; i++) {

                    if (launchJNLPAttributes.get(i).equals(appTemplateAttributes.get(i))) { // If both Node's attribute name are the
                        // same then compare the values

                        String attribute = launchJNLPAttributes.get(i);
                        boolean isSame = templateNode.getAttribute(attribute).equals( // Check if the Attribute values match
                                launchNode.getAttribute(attribute));

                        if (!isTemplate && !isSame) {
                            return false;
                        } else if (isTemplate && !isSame && !templateNode.getAttribute(attribute).equals("*")) {
                            return false;
                        }
                    } else {
                        // If attributes names do not match, return false
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
}
