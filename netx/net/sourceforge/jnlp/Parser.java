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

import static net.sourceforge.jnlp.runtime.Translator.R;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.jnlp.SecurityDesc.RequestedPermissionLevel;
import net.sourceforge.jnlp.UpdateDesc.Check;
import net.sourceforge.jnlp.UpdateDesc.Policy;
import net.sourceforge.jnlp.util.logging.OutputController;

/**
 * Contains methods to parse an XML document into a JNLPFile.
 * Implements JNLP specification version 1.0.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.13 $
 */
public final class Parser {
    
    private static String CODEBASE = "codebase";
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
    /** the file reference */
    private final JNLPFile file; // do not use (uninitialized)

    /** the root node */
    private final Node root;

    /** the specification version */
    private final Version spec;

    /** the base URL that all hrefs are relative to */
    private final URL base;

    /** the codebase URL */
    private URL codebase;

    /** the file URL */
    private final URL fileLocation;

    /** whether to throw errors on non-fatal errors. */
    private final boolean strict; // if strict==true parses a file with no error then strict==false should also

    /** whether to allow extensions to the JNLP specification */
    private final boolean allowExtensions; // true if extensions to JNLP spec are ok
    
    /**
     * Create a parser for the JNLP file. If the location
     * parameters is not null it is used as the default codebase
     * (does not override value of jnlp element's href
     * attribute).
     * <p>
     * The root node may be normalized as a side effect of this
     * constructor.
     * </p>
     * @param file the (uninitialized) file reference
     * @param base if codebase is not specified, a default base for relative URLs
     * @param root the root node
     * @param settings the parser settings to use when parsing the JNLP file
     * @throws ParseException if the JNLP file is invalid
     */
    Parser(JNLPFile file, URL base, Node root, ParserSettings settings) throws ParseException {
	this(file, base, root, settings, null);
    }

    /**
     * Create a parser for the JNLP file. If the location
     * parameters is not null it is used as the default codebase
     * (does not override value of jnlp element's href
     * attribute).
     * <p>
     * The root node may be normalized as a side effect of this
     * constructor.
     * </p>
     * @param file the (uninitialized) file reference
     * @param base if codebase is not specified, a default base for relative URLs
     * @param root the root node
     * @param settings the parser settings to use when parsing the JNLP file
     * @param codebase codebase to use if we did not parse one from JNLP file.
     * @throws ParseException if the JNLP file is invalid
     */
    Parser(JNLPFile file, URL base, Node root, ParserSettings settings, URL codebase) throws ParseException {
        this.file = file;
        this.root = root;
        this.strict = settings.isStrict();
        this.allowExtensions = settings.isExtensionAllowed();

        // ensure it's a JNLP node
        if (root == null || !root.getNodeName().equals("jnlp"))
            throw new ParseException(R("PInvalidRoot"));

        // JNLP tag information
        this.spec = getVersion(root, "spec", "1.0+");

        try {
            this.codebase = addSlash(getURL(root, CODEBASE, base));
        } catch (ParseException e) {
            //If parsing fails, continue by overriding the codebase with the one passed in
        }

        if (this.codebase == null) // Codebase is overwritten if codebase was not specified in file or if parsing of it failed
            this.codebase = codebase;

        this.base = (this.codebase != null) ? this.codebase : base; // if codebase not specified use default codebase
        fileLocation = getURL(root, "href", this.base);

        // normalize the text nodes
        root.normalize();
    }

    /**
     * Returns the file version.
     * @return version of file
     */
    public Version getFileVersion() {
        return getVersion(root, "version", null);
    }

    /**
     * Returns the file location.
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

    UpdateDesc getUpdate(Node parent) throws ParseException {
        UpdateDesc updateDesc = null;
        Node child = parent.getFirstChild();
        while (child != null) {
            if (child.getNodeName().equals("update")) {
                if (strict && updateDesc != null) {
                    throw new ParseException(R("PTwoUpdates"));
                }

                Node node = child;

                Check check;
                String checkValue = getAttribute(node, "check", "timeout");
                switch (checkValue) {
                    case "always":
                        check = Check.ALWAYS;
                        break;
                    case "timeout":
                        check = Check.TIMEOUT;
                        break;
                    case "background":
                        check = Check.BACKGROUND;
                        break;
                    default:
                        check = Check.TIMEOUT;
                        break;
                }

                String policyString = getAttribute(node, "policy", "always");
                Policy policy;
                switch (policyString) {
                    case "always":
                        policy = Policy.ALWAYS;
                        break;
                    case "prompt-update":
                        policy = Policy.PROMPT_UPDATE;
                        break;
                    case "prompt-run":
                        policy = Policy.PROMPT_RUN;
                        break;
                    default:
                        policy = Policy.ALWAYS;
                        break;
                }

                updateDesc = new UpdateDesc(check, policy);
            }

            child = child.getNextSibling();
        }

        if (updateDesc == null) {
            updateDesc = new UpdateDesc(Check.TIMEOUT, Policy.ALWAYS);
        }
        return updateDesc;
    }

    //
    // This section loads the resources elements
    //

    /**
     * @return all of the ResourcesDesc elements under the specified
     * node (jnlp or j2se).
     *
     * @param parent the parent node (either jnlp or j2se)
     * @param j2se true if the resources are located under a j2se or java node
     * @throws ParseException if the JNLP file is invalid
     */
    List<ResourcesDesc> getResources(Node parent, boolean j2se)
            throws ParseException {
        List<ResourcesDesc> result = new ArrayList<>();
        Node resources[] = getChildNodes(parent, "resources");

        // ensure that there are at least one information section present
        if (resources.length == 0 && !j2se) {
            throw new ParseException(R("PNoResources"));
        }
        for (Node resource : resources) {
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
    private  ResourcesDesc getResourcesDesc(Node node, boolean j2se) throws ParseException {
        boolean mainFlag = false; // if found a main tag

        // create resources
        ResourcesDesc resources =
                new ResourcesDesc(file,
                              getLocales(node),
                              splitString(getAttribute(node, "os", null)),
                              splitString(getAttribute(node, "arch", null)));

        // step through the elements
        Node child = node.getFirstChild();
        while (child != null) {
            String name = child.getNodeName();

            // check for nativelib but no trusted environment
            if ("nativelib".equals(name))
                if (!isTrustedEnvironment())
                    throw new ParseException(R("PUntrustedNative"));

            if ("j2se".equals(name) || "java".equals(name)) {
                if (getChildNode(root, "component-desc") != null)
                    if (strict)
                        throw new ParseException(R("PExtensionHasJ2SE"));
                if (!j2se)
                    resources.addResource(getJRE(child));
                else
                    throw new ParseException(R("PInnerJ2SE"));
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

            if ("extension".equals(name))
                resources.addResource(getExtension(child));

            if ("property".equals(name))
                resources.addResource(getProperty(child));

            if ("package".equals(name))
                resources.addResource(getPackage(child));

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
        URL location = getURL(node, "href", base);
        String vmArgs = getAttribute(node, "java-vm-args", null);
        try {
            checkVMArgs(vmArgs);
        } catch (IllegalArgumentException argumentException) {
            vmArgs = null;
        }
        String initialHeap = getAttribute(node, "initial-heap-size", null);
        String maxHeap = getAttribute(node, "max-heap-size", null);
        List<ResourcesDesc> resources = getResources(node, true);

        // require version attribute
        getRequiredAttribute(node, "version", null);

        return new JREDesc(new Version.JreVersion(version.toString(), strict), location, vmArgs, initialHeap, maxHeap, resources);
    }

    /**
     * Returns the JAR element at the specified node.
     *
     * @param node the jar or nativelib node
     * @throws ParseException if the JNLP file is invalid
     */
    private JARDesc getJAR(Node node) throws ParseException {
        boolean nativeJar = "nativelib".equals(node.getNodeName());
        URL location = getRequiredURL(node, "href", base);
        Version version = getVersion(node, "version", null);
        String part = getAttribute(node, "part", null);
        boolean main = "true".equals(getAttribute(node, "main", "false"));
        boolean lazy = "lazy".equals(getAttribute(node, "download", "eager"));

        if (nativeJar && main)
            if (strict)
                throw new ParseException(R("PNativeHasMain"));

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
        URL location = getRequiredURL(node, "href", base);

        ExtensionDesc ext = new ExtensionDesc(name, version, location);

        Node dload[] = getChildNodes(node, "ext-download");
        for (Node dload1 : dload) {
            boolean lazy = "lazy".equals(getAttribute(dload1, "download", "eager"));
            ext.addPart(getRequiredAttribute(dload1, "ext-part", null), getAttribute(dload1, "part", null), lazy);
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
        String name = getRequiredAttribute(node, "name", null);
        String value = getRequiredAttribute(node, "value", "");

        return new PropertyDesc(name, value);
    }

    /**
     * @return the Package element at the specified node.
     *
     * @param node the package node
     * @throws ParseException if the JNLP file is invalid
     */
    private PackageDesc getPackage(Node node) throws ParseException {
        String name = getRequiredAttribute(node, "name", null);
        String part = getRequiredAttribute(node, "part", "");
        boolean recursive = getAttribute(node, "recursive", "false").equals("true");

        return new PackageDesc(name, part, recursive);
    }

    //
    // This section loads the information elements
    //

    /**
     * Make sure a title and vendor are present and nonempty and localized as
     * best matching as possible for the JVM's current locale. Fallback to a
     * generalized title and vendor otherwise. If none is found, throw an exception.
     *
     * Additionally prints homepage, description, title and vendor to stdout
     * if in Debug mode.
     * @throws RequiredElementException
     */
    void checkForInformation() throws RequiredElementException {
        OutputController.getLogger().log("Homepage: " + file.getInformation().getHomepage());
        OutputController.getLogger().log("Description: " + file.getInformation().getDescription());

        String title = file.getTitle();
        String vendor = file.getVendor();

        if (title == null || title.trim().isEmpty())
            throw new MissingTitleException();
        else OutputController.getLogger().log("Acceptable title tag found, contains: " + title);

        if (vendor == null || vendor.trim().isEmpty())
            throw new MissingVendorException();
        else OutputController.getLogger().log("Acceptable vendor tag found, contains: " + vendor);
    }

    /**
     * @return all of the information elements under the specified
     * node.
     *
     * @param parent the parent node (jnlp)
     * @throws ParseException if the JNLP file is invalid
     */
    List<InformationDesc> getInfo(Node parent)
            throws ParseException {
        List<InformationDesc> result = new ArrayList<>();
        Node info[] = getChildNodes(parent, "information");

        // ensure that there are at least one information section present
        if (info.length == 0)
            throw new MissingInformationException();

        // create objects from the info sections
        for (Node infoNode : info) {
            result.add(getInformationDesc(infoNode));
        }

        return result;
    }

    /**
     * @return the information element at the specified node.
     *
     * @param node the information node
     * @throws ParseException if the JNLP file is invalid
     */
     InformationDesc getInformationDesc(Node node) throws ParseException {
        List<String> descriptionsUsed = new ArrayList<>();

        // locale
        Locale locales[] = getLocales(node);

        // create information
        InformationDesc info = new InformationDesc(locales, strict);

        // step through the elements
        Node child = node.getFirstChild();
        while (child != null) {
            String name = child.getNodeName();

            if ("title".equals(name))
                addInfo(info, child, null, getSpanText(child, false));
            if ("vendor".equals(name))
                addInfo(info, child, null, getSpanText(child, false));
            if ("description".equals(name)) {
                String kind = getAttribute(child, "kind", "default");
                if (descriptionsUsed.contains(kind))
                    if (strict)
                        throw new ParseException(R("PTwoDescriptions", kind));

                descriptionsUsed.add(kind);
                addInfo(info, child, kind, getSpanText(child, false));
            }
            if ("homepage".equals(name))
                addInfo(info, child, null, getRequiredURL(child, "href", base));
            if ("icon".equals(name))
                addInfo(info, child, getAttribute(child, "kind", "default"), getIcon(child));
            if ("offline-allowed".equals(name))
                addInfo(info, child, null, Boolean.TRUE);
            if ("sharing-allowed".equals(name)) {
                if (strict && !allowExtensions)
                    throw new ParseException(R("PSharing"));
                addInfo(info, child, null, Boolean.TRUE);
            }
            if ("association".equals(name)) {
                addInfo(info, child, null, getAssociation(child));
            }
            if ("shortcut".equals(name)) {
                addInfo(info, child, null, getShortcut(child));
            }
            if ("related-content".equals(name)) {
                addInfo(info, child, null, getRelatedContent(child));
            }

            child = child.getNextSibling();
        }

        return info;
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

        if (node == null)
            return;

        info.addItem(node.getNodeName() + modStr, value);
    }

    /**
     * @return the icon element at the specified node.
     *
     * @param node the icon node
     * @throws ParseException if the JNLP file is invalid
     */
    private IconDesc getIcon(Node node) throws ParseException {
        int width = Integer.parseInt(getAttribute(node, "width", "-1"));
        int height = Integer.parseInt(getAttribute(node, "height", "-1"));
        int size = Integer.parseInt(getAttribute(node, "size", "-1"));
        int depth = Integer.parseInt(getAttribute(node, "depth", "-1"));
        URL location = getRequiredURL(node, "href", base);
        Object kind = getAttribute(node, "kind", "default");

        return new IconDesc(location, kind, width, height, depth, size);
    }

    //
    // This section loads the security descriptor element
    //

    /**
     * @return the security descriptor element.  If no security
     * element was specified in the JNLP file then a SecurityDesc
     * with applet permissions is returned.
     *
     * @param parent the parent node
     * @throws ParseException if the JNLP file is invalid
     */
    SecurityDesc getSecurity(Node parent) throws ParseException {
        Node nodes[] = getChildNodes(parent, "security");

        // test for too many security elements
        if (nodes.length > 1)
            if (strict)
                throw new ParseException(R("PTwoSecurity"));

        Object type = SecurityDesc.SANDBOX_PERMISSIONS;
        RequestedPermissionLevel requestedPermissionLevel = RequestedPermissionLevel.NONE;

        if (nodes.length == 0) {
            type = SecurityDesc.SANDBOX_PERMISSIONS;
            requestedPermissionLevel = RequestedPermissionLevel.NONE;
        } else if (null != getChildNode(nodes[0], "all-permissions")) {
            type = SecurityDesc.ALL_PERMISSIONS;
            requestedPermissionLevel = RequestedPermissionLevel.ALL;
        } else if (null != getChildNode(nodes[0], "j2ee-application-client-permissions")) {
            type = SecurityDesc.J2EE_PERMISSIONS;
            requestedPermissionLevel = RequestedPermissionLevel.J2EE;
        } else if (strict) {
            throw new ParseException(R("PEmptySecurity"));
        }

        if (base != null) {
            return new SecurityDesc(file, requestedPermissionLevel, type, base);
        } else {
            return new SecurityDesc(file, requestedPermissionLevel, type, null);
        }
    }

    /**
     * Returns whether the JNLP file requests a trusted execution
     * environment.
     */
    private boolean isTrustedEnvironment() {
        Node security = getChildNode(root, "security");

        if (security != null)
            if (getChildNode(security, "all-permissions") != null
                    || getChildNode(security, "j2ee-application-client-permissions") != null)
                return true;

        return false;
    }

    //
    // This section loads the launch descriptor element
    //

    /**
     * @return the launch descriptor element, either AppletDesc,
     * ApplicationDesc, or InstallerDesc.
     *
     * @param parent the parent node
     * @throws ParseException if the JNLP file is invalid
     */
    LaunchDesc getLauncher(Node parent) throws ParseException {
        // check for other than one application type
        if (1 < getChildNodes(parent, "applet-desc").length
                + getChildNodes(parent, "application-desc").length
                + getChildNodes(parent, "installer-desc").length)
            throw new ParseException(R("PTwoDescriptors"));

        Node child = parent.getFirstChild();
        while (child != null) {
            String name = child.getNodeName();

            if ("applet-desc".equals(name))
                return getApplet(child);
            if ("application-desc".equals(name))
                return getApplication(child);
            if ("installer-desc".equals(name))
                return getInstaller(child);

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
     */
    private AppletDesc getApplet(Node node) throws ParseException {
        String name = getRequiredAttribute(node, "name", R("PUnknownApplet"));
        String main = getMainClass(node, true);
        URL docbase = getURL(node, "documentbase", base);
        Map<String, String> paramMap = new HashMap<>();
        int width = 0;
        int height = 0;

        try {
            width = Integer.parseInt(getRequiredAttribute(node, "width", "100"));
            height = Integer.parseInt(getRequiredAttribute(node, "height", "100"));
        } catch (NumberFormatException nfe) {
            if (width <= 0)
                throw new ParseException(R("PBadWidth"));
            throw new ParseException(R("PBadWidth"));
        }

        // read params
        Node params[] = getChildNodes(node, "param");
        for (Node param : params) {
            paramMap.put(getRequiredAttribute(param, "name", null), getRequiredAttribute(param, "value", ""));
        }

        return new AppletDesc(name, main, docbase, width, height, paramMap);
    }

    /**
     * @return the application descriptor.
     *
     * @param node
     * @throws ParseException if the JNLP file is invalid
     */
    private ApplicationDesc getApplication(Node node) throws ParseException {
        String main = getMainClass(node, false);
        List<String> argsList = new ArrayList<>();

        // if (main == null)
        //   only ok if can be found in main jar file (can't check here but make a note)

        // read parameters
        Node args[] = getChildNodes(node, "argument");
        for (Node arg : args) {
            //argsList.add( args[i].getNodeValue() );
            //This approach was not finding the argument text
            argsList.add(getSpanText(arg));
        }

        String argStrings[] = argsList.toArray(new String[argsList.size()]);

        return new ApplicationDesc(main, argStrings);
    }

    /**
     * @param parent
     * @return the component descriptor.
     * @throws net.sourceforge.jnlp.ParseException
     */
    ComponentDesc getComponent(Node parent) throws ParseException {

        if (1 < getChildNodes(parent, "component-desc").length) {
            throw new ParseException(R("PTwoDescriptors"));
        }

        Node child = parent.getFirstChild();
        while (child != null) {
            String name = child.getNodeName();

            if ("component-desc".equals(name))
                return new ComponentDesc();

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
     * @throws net.sourceforge.jnlp.ParseException
     */
    private AssociationDesc getAssociation(Node node) throws ParseException {
        String[] extensions = getRequiredAttribute(node, "extensions", null).split(" ");
        String mimeType = getRequiredAttribute(node, "mime-type", null);

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
            String name = child.getNodeName();

            if (null != name) switch (name) {
                case "desktop":
                    if (showOnDesktop && strict) {
                        throw new ParseException(R("PTwoDesktops"));
                    }   showOnDesktop = true;
                    break;
                case "menu":
                    if (menu != null && strict) {
                        throw new ParseException(R("PTwoMenus"));
                    }   menu = getMenu(child);
                    break;
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
     * @return the menu descriptor.
     */
    private MenuDesc getMenu(Node node) {
        String subMenu = getAttribute(node, "submenu", null);

        return new MenuDesc(subMenu);
    }

    /**
     * @return the related-content descriptor.
     */
    private RelatedContentDesc getRelatedContent(Node node) throws ParseException {

        getRequiredAttribute(node, "href", null);
        URL location = getURL(node, "href", base);

        String title = null;
        String description = null;
        IconDesc icon = null;

        // step through the elements
        Node child = node.getFirstChild();
        while (child != null) {
            String name = child.getNodeName();

            if (null != name) switch (name) {
                case "title":
                    if (title != null && strict) {
                        throw new ParseException(R("PTwoTitles"));
                    }   title = getSpanText(child, false);
                    break;
                case "description":
                    if (description != null && strict) {
                        throw new ParseException(R("PTwoDescriptions"));
                    }   description = getSpanText(child, false);
                    break;
                case "icon":
                    if (icon != null && strict) {
                        throw new ParseException(R("PTwoIcons"));
                    }   icon = getIcon(child);
                    break;
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
     * @return an array of substrings seperated by spaces (spaces
     * escaped with backslash do not separate strings).  This method
     * splits strings as per the spec except that it does replace
     * escaped other characters with their own value.
     */
    private String[] splitString(String source) {
        if (source == null)
            return new String[0];

        List<String> result = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(source, " ");
        StringBuilder part = new StringBuilder();
        while (st.hasMoreTokens()) {
            part.setLength(0);

            // tack together tokens joined by backslash
            while (true) {
                part.append(st.nextToken());

                if (st.hasMoreTokens() && part.charAt(part.length() - 1) == '\\')
                    part.setCharAt(part.length() - 1, ' '); // join with the space
                else
                    break; // bizarre while format gets \ at end of string right (no extra space added at end)
            }

            // delete \ quote chars
            for (int i = part.length(); i-- > 0;)
                // sweet syntax for reverse loop
                if (part.charAt(i) == '\\')
                    part.deleteCharAt(i--); // and skip previous char so \\ becomes \

            result.add(part.toString());
        }

        return result.toArray(new String[result.size()]);
    }

    /**
     * @return the Locale object(s) from a node's locale attribute.
     *
     * @param node the node with a locale attribute
     */
    private Locale[] getLocales(Node node) {
        List<Locale> locales = new ArrayList<>();
        String localeParts[] =
                splitString(getAttribute(node, "locale", ""));

        for (String localePart : localeParts) {
            Locale l = getLocale(localePart);
            if (l != null)
                locales.add(l);
        }

        return locales.toArray(new Locale[locales.size()]);
    }

    /**
     * Returns a {@link Locale} from a single locale.
     *
     * @param localeStr the locale string
     * @return locale of document
     */
    public Locale getLocale(String localeStr) {
        if (localeStr.length() < 2)
            return null;

        String language = localeStr.substring(0, 2);
        String country = (localeStr.length() < 5) ? "" : localeStr.substring(3, 5);
        String variant = (localeStr.length() > 7) ? localeStr.substring(6) : "";

        // null is not allowed n locale but "" is
        return new Locale(language, country, variant);
    }

    // XML junk
    /**
     * Returns the implied text under a node, for example "text" in
     * "&lt;description&gt;text&lt;/description&gt;".
     *
     * @param node the node with text under it
     * @return 
     * @throws ParseException if the JNLP file is invalid
     */
    private String getSpanText(Node node) throws ParseException {
        return getSpanText(node, true);
    }

    /**
     * Returns the implied text under a node, for example "text" in
     * "&lt;description&gt;text&lt;/description&gt;". If preserveSpacing is false,
     * sequences of whitespace characters are turned into a single
     * space character.
     *
     * @param node the node with text under it
     * @param preserveSpacing if true, preserve whitespace
     * @throws ParseException if the JNLP file is invalid
     */
    private String getSpanText(Node node, boolean preserveSpacing)
            throws ParseException {
        if (node == null)
            return null;

        // NANO
        String val = node.getNodeValue();
        if (preserveSpacing) {
            return val;
        } else {
            if (val == null) {
                return null;
            } else {
                return val.replaceAll("\\s+", " ");
            }
        }

        /* TINY
        Node child = node.getFirstChild();

        if (child == null) {
            if (strict)
                // not sure if this is an error or whether "" is proper
                throw new ParseException("No text specified (node="+node.getNodeName()+")");
            else
                return "";
        }

        return child.getNodeValue();
        */
    }

    /**
     * Returns the first child node with the specified name.
     */
    private static Node getChildNode(Node node, String name) {
        Node[] result = getChildNodes(node, name);
        if (result.length == 0)
            return null;
        else
            return result[0];
    }

    /**
     * Returns all child nodes with the specified name.
     */
    private static Node[] getChildNodes(Node node, String name) {
        List<Node> result = new ArrayList<>();

        Node child = node.getFirstChild();
        while (child != null) {
            if (child.getNodeName().equals(name))
                result.add(child);
            child = child.getNextSibling();
        }

        return result.toArray(new Node[result.size()]);
    }

    /**
     * Returns a URL with a trailing / appended to it if there is no
     * trailing slash on the specifed URL.
     */
    private URL addSlash(URL source) {
        if (source == null)
            return null;

        if (!source.toString().endsWith("/")) {
            try {
                source = new URL(source.toString() + "/");
            } catch (MalformedURLException ex) {
            }
        }

        return source;
    }

    /**
     * @return the same result as getURL except that a
     * ParseException is thrown if the attribute is null or empty.
     *
     * @param node the node
     * @param name the attribute containing an href
     * @param base the base URL
     * @throws ParseException if the JNLP file is invalid
     */
    private URL getRequiredURL(Node node, String name, URL base) throws ParseException {
        // probably should change "" to null so that url is always
        // required even if !strict
        getRequiredAttribute(node, name, "");

        return getURL(node, name, base);
    }

    /**
     * @return a URL object from a href string relative to the
     * code base. If the href denotes a relative URL, it must
     * reference a location that is a subdirectory of the
     * codebase.
     *
     * @param node the node
     * @param name the attribute containing an href
     * @param base the base URL
     * @throws ParseException if the JNLP file is invalid
     */
    URL getURL(Node node, String name, URL base) throws ParseException {
        String href;
        if (CODEBASE.equals(name)) {
            href = getCleanAttribute(node, name);
            //in case of null code can throw an exception later
            //some bogus jnlps have codebase as "" and expect it behaving as "."
            if (href != null && href.trim().isEmpty()) {
                href = ".";
            }
        } else {
            href = getAttribute(node, name, null);
        }
        return getURL(href, node.getNodeName(), base, strict);
    }
    
    public static URL getURL(String href, String nodeName, URL base, boolean strict) throws ParseException {
         if (href == null) {
            return null; // so that code can throw an exception if attribute was required
        }
        try {
            if (base == null)
                return new URL(href);
            else {
                try {
                    return new URL(href);
                } catch (MalformedURLException ex) {
                    // is relative
                }

                URL result = new URL(base, href);

                // check for going above the codebase
                if (!result.toString().startsWith(base.toString()) &&  !base.toString().startsWith(result.toString())){
                    if (strict) {
                        throw new ParseException(R("PUrlNotInCodebase", nodeName, href, base));
                    }
                }
                return result;
            }

        } catch (MalformedURLException ex) {
            if (base == null)
                throw new ParseException(R("PBadNonrelativeUrl", nodeName, href));
            else
                throw new ParseException(R("PBadRelativeUrl", nodeName, href, base));
        }
    }

    /**
     * @return a Version from the specified attribute and default
     * value.
     *
     * @param node the node
     * @param name the attribute
     * @param defaultValue default if no such attribute
     * @return a Version, or null if no such attribute and default is null
     */
    private Version getVersion(Node node, String name, String defaultValue) {
        String version = getAttribute(node, name, defaultValue);
        if (version == null)
            return null;
        else
            return new Version(version);
    }

    /**
     * Check that the VM args are valid and safe
     * @param vmArgs a string containing the args
     * @throws ParseException if the VM arguments are invalid or dangerous
     */
    private void checkVMArgs(String vmArgs) throws IllegalArgumentException {
        if (vmArgs == null) {
            return;
        }

        List<String> validArguments = Arrays.asList(getValidVMArguments());
        List<String> validStartingArguments = Arrays.asList(getValidStartingVMArguments());

        String[] arguments = vmArgs.split(" ");
        boolean argumentIsValid;
        for (String argument : arguments) {
            argumentIsValid = false;

            if (validArguments.contains(argument)) {
                argumentIsValid = true;
            } else {
                for (String validStartingArgument : validStartingArguments) {
                    if (argument.startsWith(validStartingArgument)) {
                        argumentIsValid = true;
                        break;
                    }
                }
            }

            if (!argumentIsValid) {
                throw new IllegalArgumentException(argument);
            }
        }

    }

    /**
     * Returns an array of valid (ie safe and supported) arguments for the JVM
     *
     * Based on http://java.sun.com/javase/6/docs/technotes/guides/javaws/developersguide/syntax.html
     */
    private String[] getValidVMArguments() {
        return new String[] {
                "-d32", /* use a 32-bit data model if available */
                "-client", /* to select the client VM */
                "-server", /* to select the server VM */
                "-verbose", /* enable verbose output */
                "-version", /* print product version and exit */
                "-showversion", /* print product version and continue */
                "-help", /* print this help message */
                "-X", /* print help on non-standard options */
                "-ea", /* enable assertions */
                "-enableassertions", /* enable assertions */
                "-da", /* disable assertions */
                "-disableassertions", /* disable assertions */
                "-esa", /* enable system assertions */
                "-enablesystemassertions", /* enable system assertions */
                "-dsa", /* disable system assertione */
                "-disablesystemassertions", /* disable system assertione */
                "-Xmixed", /* mixed mode execution (default) */
                "-Xint", /* interpreted mode execution only */
                "-Xnoclassgc", /* disable class garbage collection */
                "-Xincgc", /* enable incremental garbage collection */
                "-Xbatch", /* disable background compilation */
                "-Xprof", /* output cpu profiling data */
                "-Xdebug", /* enable remote debugging */
                "-Xfuture", /* enable strictest checks, anticipating future default */
                "-Xrs", /* reduce use of OS signals by Java/VM (see documentation) */
                "-XX:+ForceTimeHighResolution", /* use high resolution timer */
                "-XX:-ForceTimeHighResolution", /* use low resolution (default) */
        };
    }

    /**
     * Returns an array containing the starts of valid (ie safe and supported)
     * arguments for the JVM
     *
     * Based on http://java.sun.com/javase/6/docs/technotes/guides/javaws/developersguide/syntax.html
     */
    private String[] getValidStartingVMArguments() {
        return new String[] {
                "-ea", /* enable assertions for classes */
                "-enableassertions", /* enable assertions for classes */
                "-da", /* disable assertions for classes */
                "-disableassertions", /* disable assertions for classes */
                "-verbose", /* enable verbose output */
                "-Xms", /* set initial Java heap size */
                "-Xmx", /* set maximum Java heap size */
                "-Xss", /* set java thread stack size */
                "-XX:NewRatio", /* set Ratio of new/old gen sizes */
                "-XX:NewSize", /* set initial size of new generation */
                "-XX:MaxNewSize", /* set max size of new generation */
                "-XX:PermSize", /* set initial size of permanent gen */
                "-XX:MaxPermSize", /* set max size of permanent gen */
                "-XX:MaxHeapFreeRatio", /* heap free percentage (default 70) */
                "-XX:MinHeapFreeRatio", /* heap free percentage (default 40) */
                "-XX:UseSerialGC", /* use serial garbage collection */
                "-XX:ThreadStackSize", /* thread stack size (in KB) */
                "-XX:MaxInlineSize", /* set max num of bytecodes to inline */
                "-XX:ReservedCodeCacheSize", /* Reserved code cache size (bytes) */
                "-XX:MaxDirectMemorySize",

        };
    }

    /**
     * @return the same result as getAttribute except that if strict
     * mode is enabled or the default value is null a parse
     * exception is thrown instead of returning the default value.
     *
     * @param node the node
     * @param name the attribute
     * @param defaultValue default value
     * @throws ParseException if the attribute does not exist or is empty
     */
    private String getRequiredAttribute(Node node, String name, String defaultValue) throws ParseException {
        String result = getAttribute(node, name, null);

        if (result == null || result.length() == 0)
            if (strict || defaultValue == null)
                throw new ParseException(R("PNeedsAttribute", node.getNodeName(), name));

        if (result == null)
            return defaultValue;
        else
            return result;
    }

    /**
     * @return an attribute or the specified defaultValue if there is
     * no such attribute.
     *
     * @param node the node
     * @param name the attribute
     * @param defaultValue default if no such attribute
     */
    private String getAttribute(Node node, String name, String defaultValue) {
        // SAX
        // String result = ((Element) node).getAttribute(name);
        String result = getCleanAttribute(node, name);

        if (result == null || result.length() == 0) {
            return defaultValue;
        }

        return result;
    }

    private String getCleanAttribute(Node node, String name) {
        String result = node.getAttribute(name);
        return result;
    }

    
    public static final String MALFORMED_PARSER_CLASS = "net.sourceforge.jnlp.MalformedXMLParser";
    public static final String NORMAL_PARSER_CLASS = "net.sourceforge.jnlp.XMLParser";
    /**
     * @return the root node from the XML document in the specified
     * input stream.
     *
     * @throws ParseException if the JNLP file is invalid
     */
    static Node getRootNode(InputStream input, ParserSettings settings) throws ParseException {
        try {
            Object parser = getParserInstance(settings);
            Method m = parser.getClass().getMethod("getRootNode", InputStream.class);
            return (Node) m.invoke(parser, input);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof ParseException) {
                throw (ParseException)(e.getCause());
            }
            throw new ParseException(R("PBadXML"), e);
        } catch (Exception e) {
            throw new ParseException(R("PBadXML"), e);
        }
    }
    

     public static Object getParserInstance(ParserSettings settings) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        String className;
        if (settings.isMalformedXmlAllowed()) {
            className = MALFORMED_PARSER_CLASS;
            ParseException.setExpected(ParseException.UsedParsers.MALFORMED);
        } else {
            className = NORMAL_PARSER_CLASS;
            ParseException.setExpected(ParseException.UsedParsers.NORMAL);
        }

        Class<?> klass;
        Object instance;

        try {
            klass = Class.forName(className);
            instance = klass.newInstance();
            //catch both, for case that tagsoup was removed after build
        } catch (ClassNotFoundException | NoClassDefFoundError | InstantiationException e) {
            OutputController.getLogger().log(e);
            klass = Class.forName(NORMAL_PARSER_CLASS);
            instance = klass.newInstance();
        }

        switch (instance.getClass().getName()) {
            case MALFORMED_PARSER_CLASS:
                ParseException.setUsed(ParseException.UsedParsers.MALFORMED);
                break;
            case NORMAL_PARSER_CLASS:
                ParseException.setUsed(ParseException.UsedParsers.NORMAL);
                break;
        }
        return instance;
    }

  private String getOptionalMainClass(Node node) {
        try {
            return getMainClass(node, false);
        } catch (ParseException ex) {
            //only getRequiredAttribute can throw this
            //and as there is call to getMainClass  with required false
            //it is not going to be thrown
            OutputController.getLogger().log(ex);
            return null;
        }
    }

    private String getMainClass(Node node, boolean required) throws ParseException {
        String main;
        if (required) {
            main = getRequiredAttribute(node, MAINCLASS, null);
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
                OutputController.getLogger().log(OutputController.Level.WARNING_ALL, "Warning! main-class contains whitespace - '" + main + "'");
                main = main.trim();
                OutputController.getLogger().log(OutputController.Level.WARNING_ALL, "Trimmed - '" + main + "'");
            }
            boolean valid = true;
            if (!Character.isJavaIdentifierStart(main.charAt(0))) {
                valid = false;
                OutputController.getLogger().log(OutputController.Level.MESSAGE_DEBUG, "Invlaid char in main-class: '" + main.charAt(0) + "'");
            }
            for (int i = 1; i < main.length(); i++) {
                if (main.charAt(i)=='.'){
                    //dot connects identifiers
                    continue;
                }
                if (!Character.isJavaIdentifierPart(main.charAt(i))) {
                    valid = false;
                    OutputController.getLogger().log(OutputController.Level.MESSAGE_DEBUG, "Invlaid char in main-class: '" + main.charAt(i) + "'");
                }
            }
            if (!valid) {
                OutputController.getLogger().log(OutputController.Level.WARNING_ALL, "main-class contains invalid characters - '" + main + "'. Check with vendor.");
                if (strict) {
                    throw new ParseException("main-class contains invalid characters - '" + main + "'. Check with vendor. You are in strict mode. This is fatal.");
                }
            }
        }
        return main;
    }
}
