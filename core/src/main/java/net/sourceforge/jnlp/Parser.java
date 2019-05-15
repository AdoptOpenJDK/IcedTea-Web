// Copyright (C) 2001-2003 Jon A. Maxwell (JAM)
// Copyright (C) 2009-2013 Red Hat, Inc.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
package net.sourceforge.jnlp;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.jnlp.element.EntryPoint;
import net.adoptopenjdk.icedteaweb.jnlp.element.application.AppletDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.application.ApplicationDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.application.ApplicationType;
import net.adoptopenjdk.icedteaweb.jnlp.element.extension.ComponentDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.extension.InstallerDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.AssociationDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.DescriptionDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.DescriptionKind;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.IconDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.IconKind;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.InformationDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.MenuDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.RelatedContentDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.ShortcutDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.ExtensionDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JREDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.PackageDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.PropertyDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.ResourcesDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.security.ApplicationPermissionLevel;
import net.adoptopenjdk.icedteaweb.jnlp.element.security.SecurityDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.update.UpdateCheck;
import net.adoptopenjdk.icedteaweb.jnlp.element.update.UpdateDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.update.UpdatePolicy;
import net.adoptopenjdk.icedteaweb.jnlp.version.JreVersion;
import net.adoptopenjdk.icedteaweb.jnlp.version.Version;
import net.adoptopenjdk.icedteaweb.jvm.JvmUtils;
import net.adoptopenjdk.icedteaweb.xmlparser.Node;
import net.adoptopenjdk.icedteaweb.xmlparser.ParseException;
import net.adoptopenjdk.icedteaweb.xmlparser.XMLParser;
import net.sourceforge.jnlp.util.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;
import static net.adoptopenjdk.icedteaweb.jnlp.element.application.AppletDesc.APPLET_DESC_ELEMENT;
import static net.adoptopenjdk.icedteaweb.jnlp.element.application.ApplicationDesc.APPLICATION_DESC_ELEMENT;
import static net.adoptopenjdk.icedteaweb.jnlp.element.application.ApplicationDesc.JAVAFX_DESC_ELEMENT;
import static net.adoptopenjdk.icedteaweb.jnlp.element.extension.InstallerDesc.INSTALLER_DESC_ELEMENT;
import static net.adoptopenjdk.icedteaweb.jnlp.element.information.AssociationDesc.EXTENSIONS_ATTRIBUTE;
import static net.adoptopenjdk.icedteaweb.jnlp.element.information.AssociationDesc.MIME_TYPE_ATTRIBUTE;
import static net.adoptopenjdk.icedteaweb.jnlp.element.information.HomepageDesc.HOMEPAGE_ELEMENT;
import static net.adoptopenjdk.icedteaweb.jnlp.element.information.HomepageDesc.HREF_ATTRIBUTE;
import static net.adoptopenjdk.icedteaweb.jnlp.element.information.InformationDesc.INFORMATION_ELEMENT;
import static net.adoptopenjdk.icedteaweb.jnlp.element.information.InformationDesc.LOCALE_ATTRIBUTE;
import static net.adoptopenjdk.icedteaweb.jnlp.element.information.RelatedContentDesc.RELATED_CONTENT_ELEMENT;
import static net.adoptopenjdk.icedteaweb.jnlp.element.security.SecurityDesc.SECURITY_ELEMENT;
import static net.adoptopenjdk.icedteaweb.jnlp.element.update.UpdateDesc.UPDATE_ELEMENT;
import static net.adoptopenjdk.icedteaweb.xmlparser.NodeUtils.getAttribute;
import static net.adoptopenjdk.icedteaweb.xmlparser.NodeUtils.getChildNode;
import static net.adoptopenjdk.icedteaweb.xmlparser.NodeUtils.getChildNodes;
import static net.adoptopenjdk.icedteaweb.xmlparser.NodeUtils.getRequiredAttribute;
import static net.adoptopenjdk.icedteaweb.xmlparser.NodeUtils.getRequiredURL;
import static net.adoptopenjdk.icedteaweb.xmlparser.NodeUtils.getSpanText;
import static net.adoptopenjdk.icedteaweb.xmlparser.NodeUtils.getURL;

/**
 * Contains methods to parse an XML document into a JNLPFile. Implements JNLP
 * specification version 1.0.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell
 * (JAM)</a> - initial author
 * @version $Revision: 1.13 $
 */
public final class Parser {

    private final static Logger LOG = LoggerFactory.getLogger(Parser.class);

    private static String MAINCLASS = "main-class";
    private static final Pattern anyWhiteSpace = Pattern.compile("\\s");

    // defines netx.jnlp.Node class if using Tiny XML or Nano XML
    // Currently uses the Nano XML parse.  Search for "SAX" or
    // "TINY" or "NANO" and uncomment those blocks and comment the
    // active ones (if any) to switch XML parsers.  Also
    // (un)comment appropriate Node class at end of this file and
    // do a clean build.
    /**
     * Ensure consistent error handling.
     */
    /* SAX
    static ErrorHandler errorHandler = new ErrorHandler() {
        public void error(SAXParseException exception) throws SAXParseException {
            //throw exception;
        }
        public void fatalError(SAXParseException exception) throws SAXParseException {
            //throw exception;
        }
        public void warning(SAXParseException exception) {
            OutputController.getLogger().log(OutputController.Level.WARNING_ALL, "XML parse warning:");
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, exception);
        }
    };
     */
    // fix: some descriptors need to use the jnlp file at a later
    // date and having file ref lets us pass it to their
    // constructors
    //
    /**
     * the file reference
     */
    private final JNLPFile file; // do not use (uninitialized)

    /**
     * the root node
     */
    private final Node root;

    /**
     * the specification version
     */
    private final Version spec;

    /**
     * the base URL that all hrefs are relative to
     */
    private final URL base;

    /**
     * the codebase URL
     */
    private URL codebase;

    /**
     * the file URL
     */
    private final URL fileLocation;

    /**
     * whether to throw errors on non-fatal errors.
     */
    private final boolean strict; // if strict==true parses a file with no error then strict==false should also

    /**
     * whether to allow extensions to the JNLP specification
     */
    private final boolean allowExtensions; // true if extensions to JNLP spec are ok

    /**
     * Create a parser for the JNLP file. If the location parameters is not null
     * it is used as the default codebase (does not override value of jnlp
     * element's href attribute).
     * <p>
     * The root node may be normalized as a side effect of this constructor.
     * </p>
     *
     * @param file the (uninitialized) file reference
     * @param base if codebase is not specified, a default base for relative
     * URLs
     * @param root the root node
     * @param settings the parser settings to use when parsing the JNLP file
     * @throws ParseException if the JNLP file is invalid
     */
    public Parser(final JNLPFile file, final URL base, final Node root, final ParserSettings settings) throws ParseException {
        this(file, base, root, settings, null);
    }

    /**
     * Create a parser for the JNLP file. If the location parameters is not null
     * it is used as the default codebase (does not override value of jnlp
     * element's href attribute).
     * <p>
     * The root node may be normalized as a side effect of this constructor.
     * </p>
     *
     * @param file the (uninitialized) file reference
     * @param base if codebase is not specified, a default base for relative
     * URLs
     * @param root the root node
     * @param settings the parser settings to use when parsing the JNLP file
     * @param codebase codebase to use if we did not parse one from JNLP file.
     * @throws ParseException if the JNLP file is invalid
     */
    public Parser(final JNLPFile file, final URL base, final Node root, final ParserSettings settings, final URL codebase) throws ParseException {
        this.file = file;
        this.root = root;
        this.strict = settings.isStrict();
        this.allowExtensions = settings.isExtensionAllowed();

        // ensure it's a JNLP node
        if (root == null || !root.getNodeName().getName().equals(JNLPFile.JNLP_ROOT_ELEMENT)) {
            throw new ParseException(R("PInvalidRoot"));
        }

        // JNLP tag information
        this.spec = getVersion(root, JNLPFile.SPEC_ATTRIBUTE, "1.0+");

        try {
            this.codebase = addSlash(getURL(root, XMLParser.CODEBASE, base, strict));
        } catch (ParseException e) {
            //If parsing fails, continue by overriding the codebase with the one passed in
        }

        if (this.codebase == null) // Codebase is overwritten if codebase was not specified in file or if parsing of it failed
        {
            this.codebase = codebase;
        }

        this.base = (this.codebase != null) ? this.codebase : base; // if codebase not specified use default codebase
        fileLocation = getURL(root, "href", this.base, strict);
    }

    /**
     * Returns a URL with a trailing / appended to it if there is no trailing
     * slash on the specified URL.
     */
    private URL addSlash(final URL source) {
        if (source == null) {
            return null;
        }

        final String urlString = source.toExternalForm();
        if (!urlString.endsWith("/")) {
            try {
                return new URL(urlString + "/");
            } catch (MalformedURLException ex) {
                throw new IllegalArgumentException("Could not add slash to malformed URL: " + urlString, ex);
            }
        }

        return source;
    }

    /**
     * Returns the file version.
     *
     * @return version of file
     */
    public Version getFileVersion() {
        return getVersion(root, JNLPFile.VERSION_ATTRIBUTE, null);
    }

    /**
     * Returns the file location.
     *
     * @return url of source file
     */
    public URL getFileLocation() {
        return fileLocation;
    }

    /**
     * @return the codebase.
     */
    public URL getCodeBase() {
        return codebase;
    }

    /**
     * @return the specification version.
     *
     */
    public Version getSpecVersion() {
        return spec;
    }

    UpdateDesc getUpdate(final Node parent) throws ParseException {
        UpdateDesc updateDesc = null;
        Node child = parent.getFirstChild();
        while (child != null) {
            if (child.getNodeName().getName().equals(UPDATE_ELEMENT)) {
                if (strict && updateDesc != null) {
                    throw new ParseException(R("PTwoUpdates"));
                }

                final Node node = child;

                final UpdateCheck check;
                final String checkValue = getAttribute(node, UpdateDesc.CHECK_ATTRIBUTE, UpdateCheck.TIMEOUT.getValue());
                switch (checkValue) {
                    case "always":
                        check = UpdateCheck.ALWAYS;
                        break;
                    case "timeout":
                        check = UpdateCheck.TIMEOUT;
                        break;
                    case "background":
                        check = UpdateCheck.BACKGROUND;
                        break;
                    default:
                        check = UpdateCheck.TIMEOUT;
                        break;
                }

                final  String policyString = getAttribute(node, UpdateDesc.POLICY_ATTRIBUTE, UpdatePolicy.ALWAYS.getValue());
                final UpdatePolicy policy;
                switch (policyString) {
                    case "always":
                        policy = UpdatePolicy.ALWAYS;
                        break;
                    case "prompt-update":
                        policy = UpdatePolicy.PROMPT_UPDATE;
                        break;
                    case "prompt-run":
                        policy = UpdatePolicy.PROMPT_RUN;
                        break;
                    default:
                        policy = UpdatePolicy.ALWAYS;
                        break;
                }

                updateDesc = new UpdateDesc(check, policy);
            }

            child = child.getNextSibling();
        }

        if (updateDesc == null) {
            updateDesc = new UpdateDesc(UpdateCheck.TIMEOUT, UpdatePolicy.ALWAYS);
        }
        return updateDesc;
    }

    //
    // This section loads the resources elements
    //
    /**
     * @return all of the ResourcesDesc elements under the specified node (jnlp
     * or j2se).
     *
     * @param parent the parent node (either jnlp or j2se)
     * @param j2se true if the resources are located under a j2se or java node
     * @throws ParseException if the JNLP file is invalid
     */
    public List<ResourcesDesc> getResources(final Node parent, final boolean j2se)
            throws ParseException {
        final List<ResourcesDesc> result = new ArrayList<>();
        final Node resources[] = getChildNodes(parent, ResourcesDesc.RESOURCES_ELEMENT);

        // ensure that there are at least one information section present
        if (resources.length == 0 && !j2se) {
            throw new ParseException(R("PNoResources"));
        }
        for (final Node resource : resources) {
            result.add(getResourcesDesc(resource, j2se));
        }
        return result;
    }

    /**
     * @return the ResourcesDesc element at the specified node.
     *
     * @param node the resources node
     * @param j2se true if the resources are located under a j2se or java node
     * @throws ParseException if the JNLP file is invalid
     */
    private ResourcesDesc getResourcesDesc(final Node node, final boolean j2se) throws ParseException {
        boolean mainFlag = false; // if found a main tag

        // create resources
        final ResourcesDesc resources
                = new ResourcesDesc(file,
                        getLocales(node),
                        splitString(getAttribute(node, ResourcesDesc.OS_ATTRIBUTE, null)),
                        splitString(getAttribute(node, ResourcesDesc.ARCH_ATTRIBUTE, null)));

        // step through the elements
        Node child = node.getFirstChild();
        while (child != null) {
            final String name = child.getNodeName().getName();

            // check for nativelib but no trusted environment
            if ("nativelib".equals(name)) {
                if (!isTrustedEnvironment()) {
                    throw new ParseException(R("PUntrustedNative"));
                }
            }

            if ("j2se".equals(name) || "java".equals(name)) {
                if (getChildNode(root, ComponentDesc.COMPONENT_DESC_ELEMENT) != null) {
                    if (strict) {
                        throw new ParseException(R("PExtensionHasJ2SE"));
                    }
                }
                if (!j2se) {
                    resources.addResource(getJRE(child));
                } else {
                    throw new ParseException(R("PInnerJ2SE"));
                }
            }

            if ("jar".equals(name) || "nativelib".equals(name)) {
                JARDesc jar = getJAR(child);

                // check for duplicate main entries
                if (jar.isMain()) {
                    if (mainFlag == true) {
                        if (strict) {
                            throw new ParseException(R("PTwoMains"));
                        }
                    }
                    mainFlag = true;
                }

                resources.addResource(jar);
            }

            if ("extension".equals(name)) {
                resources.addResource(getExtension(child));
            }

            if ("property".equals(name)) {
                resources.addResource(getProperty(child));
            }

            if ("package".equals(name)) {
                resources.addResource(getPackage(child));
            }

            child = child.getNextSibling();
        }

        return resources;
    }

    /**
     * @return the JRE element at the specified node.
     *
     * @param node the j2se/java node
     * @throws ParseException if the JNLP file is invalid
     */
    private JREDesc getJRE(Node node) throws ParseException {
        Version version = getVersion(node, "version", null);
        URL location = getURL(node, "href", base, strict);
        String vmArgs = getAttribute(node, "java-vm-args", null);
        try {
            JvmUtils.checkVMArgs(vmArgs);
        } catch (IllegalArgumentException argumentException) {
            vmArgs = null;
        }
        String initialHeap = getAttribute(node, "initial-heap-size", null);
        String maxHeap = getAttribute(node, "max-heap-size", null);
        List<ResourcesDesc> resources = getResources(node, true);

        // require version attribute
        getRequiredAttribute(node, "version", null, strict);

        return new JREDesc(new JreVersion(version.toString(), strict), location, vmArgs, initialHeap, maxHeap, resources);
    }

    /**
     * Returns the JAR element at the specified node.
     *
     * @param node the jar or nativelib node
     * @throws ParseException if the JNLP file is invalid
     */
    private JARDesc getJAR(Node node) throws ParseException {
        boolean nativeJar = "nativelib".equals(node.getNodeName().getName());
        URL location = getRequiredURL(node, "href", base, strict);
        Version version = getVersion(node, "version", null);
        String part = getAttribute(node, "part", null);
        boolean main = "true".equals(getAttribute(node, "main", "false"));
        boolean lazy = "lazy".equals(getAttribute(node, "download", "eager"));

        if (nativeJar && main) {
            if (strict) {
                throw new ParseException(R("PNativeHasMain"));
            }
        }

        return new JARDesc(location, version, part, lazy, main, nativeJar, true);

    }

    /**
     * @return the Extension element at the specified node.
     *
     * @param node the extension node
     * @throws ParseException if the JNLP file is invalid
     */
    private ExtensionDesc getExtension(Node node) throws ParseException {
        String name = getAttribute(node, "name", null);
        Version version = getVersion(node, "version", null);
        URL location = getRequiredURL(node, "href", base, strict);

        ExtensionDesc ext = new ExtensionDesc(name, version, location);

        Node dload[] = getChildNodes(node, "ext-download");
        for (Node dload1 : dload) {
            boolean lazy = "lazy".equals(getAttribute(dload1, "download", "eager"));
            ext.addPart(getRequiredAttribute(dload1, "ext-part", null, strict), getAttribute(dload1, "part", null), lazy);
        }

        return ext;
    }

    /**
     * @return the Property element at the specified node.
     *
     * @param node the property node
     * @throws ParseException if the JNLP file is invalid
     */
    private PropertyDesc getProperty(Node node) throws ParseException {
        String name = getRequiredAttribute(node, "name", null, strict);
        String value = getRequiredAttribute(node, "value", "", strict);

        return new PropertyDesc(name, value);
    }

    /**
     * @return the Package element at the specified node.
     *
     * @param node the package node
     * @throws ParseException if the JNLP file is invalid
     */
    private PackageDesc getPackage(Node node) throws ParseException {
        String name = getRequiredAttribute(node, "name", null, strict);
        String part = getRequiredAttribute(node, "part", "", strict);
        boolean recursive = getAttribute(node, "recursive", "false").equals("true");

        return new PackageDesc(name, part, recursive);
    }

    //
    // This section loads the information elements
    //
    /**
     * Make sure a title and vendor are present and nonempty and localized as
     * best matching as possible for the JVM's current locale. Fallback to a
     * generalized title and vendor otherwise. If none is found, throw an
     * exception.
     *
     * Additionally prints homepage, description, title and vendor to stdout if
     * in Debug mode.
     *
     * @throws RequiredElementException
     */
    void checkForInformation() throws RequiredElementException {
        LOG.info("Homepage: {}", file.getInformation().getHomepage());
       LOG.info("Description: {}", file.getInformation().getDescription());
        file.getTitle(strict);
        file.getVendor(strict);
    }

    /**
     * Search through the information elements in the order specified in the JNLP file (resp. the specified node).
     * <p/>
     * For each information element, it checks if the value specified in the locale attribute matches the current
     * locale. If a match is found, the values specified in that information element will be used, possibly
     * overriding values found in previous information elements. Thus, the locale-independent information
     * needs only to be specified once, in the information element without the locale attribute.
     *
     * @param parent the parent node containing the information elements in the order specified in the JNLP file
     * @return all of the information elements under the specified node.
     * @throws ParseException if the JNLP file is invalid
     *
     * @implSpec See <b>JSR-56, Section 3.5 Descriptor Information</b>
     * for a detailed specification of this functionality.
     */
    public List<InformationDesc> getInformationDescs(final Node parent) throws ParseException {
        final List<InformationDesc> result = new ArrayList<>();
        final Node informationElements[] = getChildNodes(parent, INFORMATION_ELEMENT);

        // ensure that there is at least one information element present in the JNLP file
        if (informationElements.length == 0) {
            throw new MissingInformationException();
        }

        // create an information descriptor for each information element
        for (final Node informationElement : informationElements) {
            result.add(getInformationDesc(informationElement));
        }

        return result;
    }

    /**
     * Returns the information element at the specified node.
     *
     * @param node the node containing the information element as specified in the JNLP file
     * @return the information element at the specified node.
     * @throws ParseException if the JNLP file is invalid
     *
     * @implSpec See <b>JSR-56, Section 3.5 Descriptor Information</b>
     * for a detailed specification of this functionality.
     */
    private InformationDesc getInformationDesc(final Node node) throws ParseException {
        final List<String> descriptionsUsed = new ArrayList<>();

        // create information
        InformationDesc informationDesc = new InformationDesc(getLocales(node), strict);

        // step through the elements
        Node child = node.getFirstChild();
        while (child != null) {
            String name = child.getNodeName().getName();

            if (InformationDesc.TITLE_ELEMENT.equals(name)) {
                addInfo(informationDesc, child, null, getSpanText(child, false));
            }
            if (InformationDesc.VENDOR_ELEMENT.equals(name)) {
                addInfo(informationDesc, child, null, getSpanText(child, false));
            }
            if (DescriptionDesc.DESCRIPTION_ELEMENT.equals(name)) {
                String kind = getAttribute(child, DescriptionDesc.KIND_ATTRIBUTE, DescriptionKind.DEFAULT.getValue());
                if (descriptionsUsed.contains(kind)) {
                    if (strict) {
                        throw new ParseException(R("PTwoDescriptions", kind));
                    }
                }
                descriptionsUsed.add(kind);
                addInfo(informationDesc, child, kind, getSpanText(child, false));
            }
            if (HOMEPAGE_ELEMENT.equals(name)) {
                addInfo(informationDesc, child, null, getRequiredURL(child, HREF_ATTRIBUTE, base, strict));
            }
            if (IconDesc.ICON_ELEMENT.equals(name)) {
                addInfo(informationDesc, child, getAttribute(child, IconDesc.KIND_ATTRIBUTE, IconKind.DEFAULT.getValue()), getIcon(child));
            }
            if (InformationDesc.OFFLINE_ALLOWED_ELEMENT.equals(name)) {
                addInfo(informationDesc, child, null, Boolean.TRUE);
            }
            if ("sharing-allowed".equals(name)) {
                if (strict && !allowExtensions) {
                    throw new ParseException(R("PSharing"));
                }
                addInfo(informationDesc, child, null, Boolean.TRUE);
            }
            if (AssociationDesc.ASSOCIATION_ELEMENT.equals(name)) {
                addInfo(informationDesc, child, null, getAssociation(child));
            }
            if (ShortcutDesc.SHORTCUT_ELEMENT.equals(name)) {
                addInfo(informationDesc, child, null, getShortcut(child));
            }
            if (RELATED_CONTENT_ELEMENT.equals(name)) {
                addInfo(informationDesc, child, null, getRelatedContent(child));
            }

            child = child.getNextSibling();
        }

        return informationDesc;
    }

    /**
     * Adds a key,value pair to the information object.
     *
     * @param info the information object
     * @param node node name to be used as the key
     * @param mod key name appended with "-"+mod if not null
     * @param value the info object to add (icon or string)
     */
    protected void addInfo(InformationDesc info, Node node, String mod, Object value) {
        String modStr = (mod == null) ? "" : "-" + mod;

        if (node == null) {
            return;
        }

        info.addItem(node.getNodeName().getName() + modStr, value);
    }

    /**
     * @return the icon element at the specified node.
     *
     * @param node the icon node
     * @throws ParseException if the JNLP file is invalid
     */
    private IconDesc getIcon(Node node) throws ParseException {
        int width = Integer.parseInt(getAttribute(node, IconDesc.WIDTH_ATTRIBUTE, "-1"));
        int height = Integer.parseInt(getAttribute(node, IconDesc.HEIGHT_ATTRIBUTE, "-1"));
        int size = Integer.parseInt(getAttribute(node, IconDesc.SIZE_ATTRIBUTE, "-1"));
        int depth = Integer.parseInt(getAttribute(node, IconDesc.DEPTH_ATTRIBUTE, "-1"));
        URL location = getRequiredURL(node, IconDesc.HREF_ATTRIBUTE, base, strict);

        return new IconDesc(location, getIconKind(node), width, height, depth, size);
    }

    private static IconKind getIconKind(final Node node) {
        Assert.requireNonNull(node, "node");
        return IconKind.fromString(getAttribute(node, IconDesc.KIND_ATTRIBUTE, IconKind.DEFAULT.getValue()));
    }

    //
    // This section loads the security descriptor element
    //
    /**
     * @return the security descriptor element. If no security element was
     * specified in the JNLP file then a SecurityDesc with applet permissions is
     * returned.
     *
     * @param parent the parent node
     * @throws ParseException if the JNLP file is invalid
     */
    public SecurityDesc getSecurity(final Node parent) throws ParseException {
        final Node nodes[] = getChildNodes(parent, SECURITY_ELEMENT);

        // test for too many security elements
        if (nodes.length > 1) {
            if (strict) {
                throw new ParseException(R("PTwoSecurity"));
            }
        }

        Object type = SecurityDesc.SANDBOX_PERMISSIONS;
        ApplicationPermissionLevel applicationPermissionLevel = ApplicationPermissionLevel.NONE;

        if (nodes.length == 0) {
            type = SecurityDesc.SANDBOX_PERMISSIONS;
            applicationPermissionLevel = ApplicationPermissionLevel.NONE;
        } else if (null != getChildNode(nodes[0], ApplicationPermissionLevel.ALL.getValue())) {
            type = SecurityDesc.ALL_PERMISSIONS;
            applicationPermissionLevel = ApplicationPermissionLevel.ALL;
        } else if (null != getChildNode(nodes[0], ApplicationPermissionLevel.J2EE.getValue())) {
            type = SecurityDesc.J2EE_PERMISSIONS;
            applicationPermissionLevel = ApplicationPermissionLevel.J2EE;
        } else if (strict) {
            throw new ParseException(R("PEmptySecurity"));
        }

        if (base != null) {
            return new SecurityDesc(file, applicationPermissionLevel, type, base);
        } else {
            return new SecurityDesc(file, applicationPermissionLevel, type, null);
        }
    }

    /**
     * Returns whether the JNLP file requests a trusted execution environment.
     */
    private boolean isTrustedEnvironment() {
        final Node security = getChildNode(root, SECURITY_ELEMENT);

        if (security != null) {
            if (getChildNode(security, ApplicationPermissionLevel.ALL.getValue()) != null
                    || getChildNode(security, ApplicationPermissionLevel.J2EE.getValue()) != null) {
                return true;
            }
        }

        return false;
    }

    /**
     * Load the entry point descriptor element.
     *
     * @return the entry point descriptor element
     * @param parent the parent node
     * @throws ParseException if the JNLP file is invalid
     *
     * @see EntryPoint
     *
     */
    public EntryPoint getEntryPointDesc(final Node parent) throws ParseException {
        // check for other than one application type
        if (1 < getChildNodes(parent, APPLET_DESC_ELEMENT).length
                + getChildNodes(parent, APPLICATION_DESC_ELEMENT).length
                + getChildNodes(parent, JAVAFX_DESC_ELEMENT).length
                + getChildNodes(parent, INSTALLER_DESC_ELEMENT).length) {
            throw new ParseException(R("PTwoDescriptors"));
        }

        Node child = parent.getFirstChild();
        while (child != null) {
            final String name = child.getNodeName().getName();

            if (APPLET_DESC_ELEMENT.equals(name)) {
                return getApplet(child);
            }
            if (APPLICATION_DESC_ELEMENT.equals(name)) {
                return getApplication(ApplicationType.JAVA, child);
            }
            if (INSTALLER_DESC_ELEMENT.equals(name)) {
                return getInstaller(child);
            }
            if (JAVAFX_DESC_ELEMENT.equals(name)) {
                return getApplication(ApplicationType.JAVAFX, child);
            }

            child = child.getNextSibling();
        }

        // not reached
        return null;
    }

    /**
     * @param node
     * @return the applet descriptor.
     *
     * @throws ParseException if the JNLP file is invalid
     *
     * TODO: parse and set {@link AppletDesc#getProgressClass()}
     */
    private AppletDesc getApplet(final Node node) throws ParseException {
        final String name = getRequiredAttribute(node, "name", R("PUnknownApplet"), strict);
        final String main = getMainClass(node, true);
        final URL docbase = getURL(node, "documentbase", base, strict);
        final Map<String, String> paramMap = new HashMap<>();
        int width = 0;
        int height = 0;

        try {
            width = Integer.parseInt(getRequiredAttribute(node, "width", "100", strict));
            height = Integer.parseInt(getRequiredAttribute(node, "height", "100", strict));
        } catch (NumberFormatException nfe) {
            if (width <= 0) {
                throw new ParseException(R("PBadWidth"));
            }
            throw new ParseException(R("PBadWidth"));
        }

        // read params
        final Node params[] = getChildNodes(node, "param");
        for (final Node param : params) {
            paramMap.put(getRequiredAttribute(param, "name", null, strict), getRequiredAttribute(param, "value", "", strict));
        }

        return new AppletDesc(name, main, docbase, width, height, paramMap);
    }

    /**
     * @return the application descriptor.
     *
     * @param node
     * @throws ParseException if the JNLP file is invalid
     *
     * TODO: parse and set {@link ApplicationDesc#getProgressClass()}
     */
    private ApplicationDesc getApplication(final ApplicationType applicationType, final Node node) throws ParseException {
        String main = getMainClass(node, false);
        List<String> argsList = new ArrayList<>();

        // if (main == null)
        //   only ok if can be found in main jar file (can't check here but make a note)
        // read parameters
        final Node args[] = getChildNodes(node, "argument");
        for (Node arg : args) {
            //argsList.add( args[i].getNodeValue() );
            //This approach was not finding the argument text
            argsList.add(getSpanText(arg));
        }

        final String argStrings[] = argsList.toArray(new String[argsList.size()]);

        return new ApplicationDesc(applicationType, main, argStrings);
    }

    /**
     * @param parent
     * @return the component descriptor.
     * @throws ParseException
     */
    ComponentDesc getComponent(final Node parent) throws ParseException {

        if (1 < getChildNodes(parent, ComponentDesc.COMPONENT_DESC_ELEMENT).length) {
            throw new ParseException(R("PTwoDescriptors"));
        }

        Node child = parent.getFirstChild();
        while (child != null) {
            final String name = child.getNodeName().getName();

            if (ComponentDesc.COMPONENT_DESC_ELEMENT.equals(name)) {
                return new ComponentDesc();
            }

            child = child.getNextSibling();
        }

        return null;
    }

    /**
     * @param node
     * @return the installer descriptor.
     */
    private InstallerDesc getInstaller(Node node) {
        String main = getOptionalMainClass(node);

        return new InstallerDesc(main);
    }

    /**
     * @return the association descriptor.
     * @param node
     * @throws ParseException
     */
    private AssociationDesc getAssociation(final Node node) throws ParseException {
        Assert.requireNonNull(node, "node");

        final String[] extensions = getRequiredAttribute(node, EXTENSIONS_ATTRIBUTE, null, strict).split(" ");
        final String mimeType = getRequiredAttribute(node, MIME_TYPE_ATTRIBUTE, null, strict);

        // TODO: optional description element according to JSR
        // TODO: optional icon element according to JSR

        return new AssociationDesc(mimeType, extensions);
    }

    /**
     * @return the shortcut descriptor.
     */
    private ShortcutDesc getShortcut(Node node) throws ParseException {

        String online = getAttribute(node, "online", "true");
        boolean shortcutIsOnline = Boolean.valueOf(online);

        boolean showOnDesktop = false;
        MenuDesc menu = null;

        // step through the elements
        Node child = node.getFirstChild();
        while (child != null) {
            String name = child.getNodeName().getName();

            if (null != name) {
                switch (name) {
                    case "desktop":
                        if (showOnDesktop && strict) {
                            throw new ParseException(R("PTwoDesktops"));
                        }
                        showOnDesktop = true;
                        break;
                    case "menu":
                        if (menu != null && strict) {
                            throw new ParseException(R("PTwoMenus"));
                        }
                        menu = getMenu(child);
                        break;
                }
            }

            child = child.getNextSibling();
        }

        ShortcutDesc shortcut = new ShortcutDesc(shortcutIsOnline, showOnDesktop);
        if (menu != null) {
            shortcut.setMenu(menu);
        }
        return shortcut;
    }

    /**
     * Returns the menu element at the specified node.
     *
     * @return the menu element at the specified node
     */
    private MenuDesc getMenu(final Node node) {
        return new MenuDesc(getAttribute(node, MenuDesc.SUBMENU_ATTRIBUTE, null));
    }

    /**
     * @return the related-content descriptor.
     */
    private RelatedContentDesc getRelatedContent(final Node node) throws ParseException {

        getRequiredAttribute(node, RelatedContentDesc.HREF_ATTRIBUTE, null, strict);
        URL location = getURL(node, RelatedContentDesc.HREF_ATTRIBUTE, base, strict);

        String title = null;
        String description = null;
        IconDesc icon = null;

        // step through the elements
        Node child = node.getFirstChild();
        while (child != null) {
            String name = child.getNodeName().getName();

            if (null != name) {
                switch (name) {
                    case RelatedContentDesc.TITLE_ELEMENT:
                        if (title != null && strict) {
                            throw new ParseException(R("PTwoTitles"));
                        }
                        title = getSpanText(child, false);
                        break;
                    case RelatedContentDesc.DESCRIPTION_ELEMENT:
                        if (description != null && strict) {
                            throw new ParseException(R("PTwoDescriptions"));
                        }
                        description = getSpanText(child, false);
                        break;
                    case RelatedContentDesc.ICON_ELEMENT:
                        if (icon != null && strict) {
                            throw new ParseException(R("PTwoIcons"));
                        }
                        icon = getIcon(child);
                        break;
                }
            }

            child = child.getNextSibling();
        }

        RelatedContentDesc relatedContent = new RelatedContentDesc(location);
        relatedContent.setDescription(description);
        relatedContent.setIconDesc(icon);
        relatedContent.setTitle(title);

        return relatedContent;
    }

    // other methods
    /**
     * @return an array of substrings separated by spaces (spaces escaped with
     * backslash do not separate strings). This method splits strings as per the
     * spec except that it does replace escaped other characters with their own
     * value.
     */
    private String[] splitString(String source) {
        if (source == null) {
            return new String[0];
        }

        List<String> result = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(source, " ");
        StringBuilder part = new StringBuilder();
        while (st.hasMoreTokens()) {
            part.setLength(0);

            // tack together tokens joined by backslash
            while (true) {
                part.append(st.nextToken());

                if (st.hasMoreTokens() && part.charAt(part.length() - 1) == '\\') {
                    part.setCharAt(part.length() - 1, ' '); // join with the space
                } else {
                    break; // bizarre while format gets \ at end of string right (no extra space added at end)
                }
            }

            // delete \ quote chars
            for (int i = part.length(); i-- > 0;) // sweet syntax for reverse loop
            {
                if (part.charAt(i) == '\\') {
                    part.deleteCharAt(i--); // and skip previous char so \\ becomes \
                }
            }
            result.add(part.toString());
        }

        return result.toArray(new String[result.size()]);
    }

    /**
     * Returns the locales for which the information element should be used. Several locales can be specified,
     * separated with spaces.
     *
     * @param node the node with a locale attribute
     * @return the Locale object(s) from a node's locale attribute.
     *
     * @implSpec See <b>JSR-56, Section 3.5 Descriptor Information</b>
     * for a detailed specification of this functionality.
     */
    private Locale[] getLocales(final Node node) throws ParseException {
        final List<Locale> locales = new ArrayList<>();
        final String localeParts[] = splitString(getAttribute(node, LOCALE_ATTRIBUTE, ""));

        for (final String localePart : localeParts) {
            final Locale l = LocaleUtils.getLocale(localePart);
            if (l != null) {
                locales.add(l);
            }
        }

        return locales.toArray(new Locale[locales.size()]);
    }


    /**
     * @return a Version from the specified attribute and default value.
     *
     * @param node the node
     * @param name the attribute
     * @param defaultValue default if no such attribute
     * @return a Version, or null if no such attribute and default is null
     */
    private Version getVersion(Node node, String name, String defaultValue) {
        String version = getAttribute(node, name, defaultValue);
        if (version == null) {
            return null;
        } else {
            return new Version(version);
        }
    }

    private String getOptionalMainClass(Node node) {
        try {
            return getMainClass(node, false);
        } catch (ParseException ex) {
            //only getRequiredAttribute can throw this
            //and as there is call to getMainClass  with required false
            //it is not going to be thrown
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
            return null;
        }
    }

    private String getMainClass(Node node, boolean required) throws ParseException {
        String main;
        if (required) {
            main = getRequiredAttribute(node, MAINCLASS, null, strict);
        } else {
            main = getAttribute(node, MAINCLASS, null);
        }
        return cleanMainClassAttribute(main);
    }

    private String cleanMainClassAttribute(String main) throws ParseException {
        if (main != null) {
            Matcher matcher = anyWhiteSpace.matcher(main);
            boolean found = matcher.find();
            if (found && !strict) {
                LOG.warn("Warning! main-class contains whitespace - '{}'", main);
                main = main.trim();
                LOG.warn("Trimmed - '{}'", main);
            }
            boolean valid = true;
            if (!Character.isJavaIdentifierStart(main.charAt(0))) {
                valid = false;
                LOG.debug("Invalid char in main-class: '{}'", main.charAt(0));
            }
            for (int i = 1; i < main.length(); i++) {
                if (main.charAt(i) == '.') {
                    //dot connects identifiers
                    continue;
                }
                if (!Character.isJavaIdentifierPart(main.charAt(i))) {
                    valid = false;
                    LOG.debug("Invalid char in main-class: '{}'", main.charAt(i));
                }
            }
            if (!valid) {
                LOG.warn("main-class contains invalid characters - '{}'. Check with vendor.", main);
                if (strict) {
                    throw new ParseException("main-class contains invalid characters - '" + main + "'. Check with vendor. You are in strict mode. This is fatal.");
                }
            }
        }
        return main;
    }
}
