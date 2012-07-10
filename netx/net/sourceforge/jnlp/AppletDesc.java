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

package net.sourceforge.jnlp;

import java.net.*;
import java.util.*;

/**
 * The applet-desc element.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.8 $
 */
public class AppletDesc implements LaunchDesc {

    /** the applet name */
    private String name;

    /** the main class name and package */
    private String mainClass;

    /** the document base */
    private URL documentBase;

    /** the width */
    private int width;

    /** the height */
    private int height;

    /** the parameters */
    private Map<String, String> parameters;

    /**
     * Create an Applet descriptor.
     *
     * @param name the applet name
     * @param mainClass the main class name and package
     * @param documentBase the document base
     * @param width the width
     * @param height the height
     * @param parameters the parameters
     */
    public AppletDesc(String name, String mainClass, URL documentBase, int width, int height,
                      Map<String, String> parameters) {
        this.name = name;
        this.mainClass = mainClass;
        this.documentBase = documentBase;
        this.width = width;
        this.height = height;
        this.parameters = new HashMap<String, String>(parameters);
    }

    /**
     * Returns the applet name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the main class name in the dot-separated form (eg: foo.bar.Baz)
     */
    @Override
    public String getMainClass() {
        return mainClass;
    }

    /**
     * Returns the document base
     */
    public URL getDocumentBase() {
        return documentBase;
    }

    /**
     * Returns the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns the applet parameters
     */
    public Map<String, String> getParameters() {
        return new HashMap<String, String>(parameters);
    }

    /**
     * Adds a parameter to the applet.  If the parameter already
     * exists then it is overwritten with the new value.  Adding a
     * parameter will have no effect on already-running applets
     * launched from this JNLP file.
     */
    public void addParameter(String name, String value) {
        parameters.put(name, value);
    }

}
