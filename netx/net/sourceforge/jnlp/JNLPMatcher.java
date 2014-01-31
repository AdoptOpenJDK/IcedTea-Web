/* JNLPMatcher.java
   Copyright (C) 2011 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 2.

IcedTea is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to
the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version.
 */

package net.sourceforge.jnlp;

import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import net.sourceforge.jnlp.util.logging.OutputController;
import net.sourceforge.nanoxml.XMLElement;

/**
 * To compare launching JNLP file with signed APPLICATION.JNLP or
 * APPLICATION_TEMPLATE.jnlp.
 * 
 * Used by net.sourceforge.jnlp.runtime.JNLPCLassLoader
 */

public final class JNLPMatcher {

    private final Node appTemplateNode;
    private final Node launchJNLPNode;
    private final boolean isTemplate;
    
    /**
     * Public constructor
     * 
     * @param appTemplate
     *            the reader stream of the signed APPLICATION.jnlp or
     *            APPLICATION_TEMPLATE.jnlp
     * @param launchJNLP
     *            the reader stream of the launching JNLP file
     * @param isTemplate
     *            a boolean that specifies if appTemplateFile is a template
     * @throws JNLPMatcherException
     *             if IOException, XMLParseException is thrown during parsing;
     *             Or launchJNLP/appTemplate is null
     */
    public JNLPMatcher(Reader appTemplate, Reader launchJNLP,
            boolean isTemplate) throws JNLPMatcherException {

        if (appTemplate == null && launchJNLP == null)
            throw new JNLPMatcherException(
                    "Template JNLP file and Launching JNLP file are both null.");
        else if (appTemplate == null)
            throw new JNLPMatcherException("Template JNLP file is null.");
        else if (launchJNLP == null)
            throw new JNLPMatcherException("Launching JNLP file is null.");
        
        //Declare variables for signed JNLP file
        ByteArrayOutputStream poutTemplate= null;
      
        //Declare variables for launching JNLP file 
        ByteArrayOutputStream poutJNLPFile = null;
        
        try {
            XMLElement appTemplateXML = new XMLElement();
            XMLElement launchJNLPXML = new XMLElement();

            // Remove the comments and CDATA from the JNLP file
            poutTemplate = new ByteArrayOutputStream();
            appTemplateXML.sanitizeInput(appTemplate, poutTemplate);

            poutJNLPFile = new ByteArrayOutputStream();
            launchJNLPXML.sanitizeInput(launchJNLP, poutJNLPFile);

            // Parse both files
            appTemplateXML.parseFromReader(new StringReader(poutTemplate.toString()));
            launchJNLPXML.parseFromReader(new StringReader(poutJNLPFile.toString()));

            // Initialize parent nodes
            this.appTemplateNode = new Node(appTemplateXML);
            this.launchJNLPNode = new Node(launchJNLPXML);
            this.isTemplate = isTemplate;

        } catch (Exception e) {
            throw new JNLPMatcherException(
                    "Failed to create an instance of JNLPVerify with specified InputStreamReader",
                    e);
        } finally {
            // Close all stream
            closeOutputStream(poutTemplate);
            
            closeOutputStream(poutJNLPFile);

        }
    }

    /**
     * Compares both JNLP files
     * 
     * @return true if both JNLP files are 'matched', otherwise false
     */
    public boolean isMatch() {
    
        return matchNodes(appTemplateNode, launchJNLPNode);
    
    }

    /**
     * Compares two Nodes regardless of the order of their children/attributes
     * 
     * @param appTemplate
     *            signed application or template's Node
     * @param launchJNLP
     *            launching JNLP file's Node
     * 
     * @return true if both Nodes are 'matched', otherwise false
     */
    private boolean matchNodes(Node appTemplate, Node launchJNLP) {

        if (appTemplate != null && launchJNLP != null) {

            Node templateNode = appTemplate;
            Node launchNode = launchJNLP;
            // Store children of Node
            List<Node> appTemplateChild = new LinkedList<Node>(Arrays.asList(templateNode
                    .getChildNodes()));
            List<Node> launchJNLPChild = new LinkedList<Node>(Arrays.asList(launchNode
                    .getChildNodes()));

            // Compare only if both Nodes have the same name, else return false
            if (templateNode.getNodeName().equals(launchNode.getNodeName())) {

                if (appTemplateChild.size() == launchJNLPChild.size()) { // Compare
                                                                         // children

                    int childLength = appTemplateChild.size();

                    for (int i = 0; i < childLength;) {
                        for (int j = 0; j < childLength; j++) {
                            boolean isSame = matchNodes(appTemplateChild.get(i),
                                    launchJNLPChild.get(j));

                            if (!isSame && j == childLength - 1)
                                return false;
                            else if (isSame) { // If both child matches, remove them from the list of children
                                appTemplateChild.remove(i);
                                launchJNLPChild.remove(j);
                                --childLength;
                                break;
                            }
                        }
                    }

                    if (!templateNode.getNodeValue().equals(launchNode.getNodeValue())) {

                        // If it's a template and the template's value is NOT '*'
                        if (isTemplate && !templateNode.getNodeValue().equals("*"))
                            return false;
                        // Else if it's not a template, then return false
                        else if (!isTemplate)
                            return false;
                    }
                    // Compare attributes of both Nodes
                    return matchAttributes(templateNode, launchNode);
                }

            }
        }
        return false;
    }

    /**
     * Compares attributes of two {@link Node Nodes} regardless of order
     * 
     * @param templateNode signed application or template's {@link Node} with attributes
     * @param launchNode launching JNLP file's {@link Node} with attributes
     * 
     * @return {@code true} if both {@link Node Nodes} have 'matched' attributes, otherwise {@code false}
     */
    private boolean matchAttributes(Node templateNode, Node launchNode) {

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

                        if (!isTemplate && !isSame)
                            return false;
                        else if (isTemplate && !isSame
                                && !templateNode.getAttribute(attribute).equals("*"))
                            return false;

                    } else
                        // If attributes names do not match, return false
                        return false;
                }
                return true;
            }
        }
        return false;
    }
    
    /***
     * Closes an input stream
     * 
     * @param stream
     *            The input stream that will be closed
     */
    private void closeInputStream(InputStream stream) {
        if (stream != null)
            try {
                stream.close();
            } catch (Exception e) {
                OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
            }
    }

    /***
     * Closes an output stream
     * 
     * @param stream
     *            The output stream that will be closed
     */
    private void closeOutputStream(OutputStream stream) {
        if (stream != null)
            try {
                stream.close();
            } catch (Exception e) {
                OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
            }
    }
}
