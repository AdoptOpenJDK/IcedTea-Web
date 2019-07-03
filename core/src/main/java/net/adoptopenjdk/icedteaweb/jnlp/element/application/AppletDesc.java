// Copyright (C) 2001-2003 Jon A. Maxwell (JAM)
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

package net.adoptopenjdk.icedteaweb.jnlp.element.application;

import net.adoptopenjdk.icedteaweb.jnlp.element.EntryPoint;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The applet-desc element contains all information needed to launch an Applet, given the resources described
 * by the resources elements. A JNLP file is an application descriptor for an Applet if the applet-desc
 * element is specified.
 *
 * @implSpec See <b>JSR-56, Section 3.7.2 Application Descriptor for an Applet</b>
 * for a detailed specification of this class.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 */
public class AppletDesc implements EntryPoint {

    private final static Logger LOG = LoggerFactory.getLogger(AppletDesc.class);
    public static final String APPLET_DESC_ELEMENT = "applet-desc";
    public static final String DOCUMENTBASE_ATTRIBUTE = "documentbase";
    public static final String PARAM_ELEMENT = "param";
    public static final String NAME_ATTRIBUTE = "name";
    public static final String WIDTH_ATTRIBUTE = "width";
    public static final String HEIGHT_ATTRIBUTE = "height";
    public static final String PROGRESS_CLASS_ATTRIBUTE = "progress-class";

    /** Name of the Applet. This is available to the Applet through the AppletContext. */
    private final String name;

    /**
     * This is the fully-qualified name of the main Applet class (e.g., com.mysite.MyApplet),
     * as opposed to the HTML <applet> tag's code attribute as a filename (e.g., MyApplet.class).
     */
    private final String mainClass;

    /**
     * The name of a class containing an implementation of the {@link javax.jnlp.DownloadServiceListener}
     * interface of applications. May be used to indicate download progress.
     */
    private final String progressClass;

    /**
     * The document base for the Applet as a URL. This is available to the Applet through the AppletContext.
     * The documentbase can be provided explicitly since an applet launched with a JNLP Client may not be
     * embedded in a Web page.
     */
    private final URL documentBase;

    /** Width of the Applet in pixels. */
    private final int width;

    /** Height of the Applet in pixels. */
    private final int height;

    /** Contains parameters to the Applet. The parameters can be retrieved with the Applet.getParameter method. */
    private final Map<String, String> parameters;

    /**
     * Create an Applet descriptor.
     *
     * @param name the applet name that is available to the Applet through the AppletContext
     * @param mainClass the fully-qualified name of the main Applet class
     * @param documentBase the document base for the Applet
     * @param width the width of the Applet in pixels
     * @param height the height of the Applet in pixels
     * @param parameters the parameters
     */
    public AppletDesc(final String name, final String mainClass, final URL documentBase, final int width,
                      final int height, final Map<String, String> parameters) {
        this(name, mainClass, null, documentBase, width, height, parameters);
    }

    /**
     * Create an Applet descriptor.
     *
     * @param name the applet name that is available to the Applet through the AppletContext
     * @param mainClass the fully-qualified name of the main Applet class
     * @param progressClass the fully qualified name of the class containing an implementation of the
     * {@link javax.jnlp.DownloadServiceListener} interface
     * @param documentBase the document base for the Applet
     * @param width the width of the Applet in pixels
     * @param height the height of the Applet in pixels
     * @param parameters the parameters
     */
    public AppletDesc(final String name, final String mainClass, final String progressClass, final URL documentBase, final int width,
                       final int height, final Map<String, String> parameters) {
        this.name = name;
        this.mainClass = mainClass;
        this.progressClass = progressClass;
        this.documentBase = documentBase;
        this.width = width;
        this.height = height;
        this.parameters = new HashMap<>(parameters);
    }

    /**
     * @return the applet name that is available to the Applet through the AppletContext
     */
    public String getName() {
        return name;
    }

    /**
     * @return the fully-qualified name of the main Applet class
     */
    @Override
    public String getMainClass() {
        return mainClass;
    }

    /**
     * The name of a class containing an implementation of the {@link javax.jnlp.DownloadServiceListener}
     * interface.
     *
     * @return the fully qualified name of the class containing an implementation of the
     * {@link javax.jnlp.DownloadServiceListener} interface
     */
    public String getProgressClass() {
        return progressClass;
    }

    /**
     * @return the document base for the Applet as a URL
     */
    public URL getDocumentBase() {
        return documentBase;
    }

    /**
     * @return the width of the Applet in pixels
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return the height of the Applet in pixels
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return the Applet parameters
     */
    public Map<String, String> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    /**
     * Adds a parameter to the Applet. If the parameter already exists then it is
     * overwritten with the new value. Adding a parameter will have no effect on
     * already-running Applets launched from this JNLP file.
     *
     * @param name key of value
     * @param value value to be added
     */
    public void addParameter(final String name, final String value) {
        parameters.put(name, value);
    }
}
