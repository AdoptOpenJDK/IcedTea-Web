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
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.logging.OutputController;

/**
 * The applet-desc element.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.8 $
 */
public class AppletDesc implements LaunchDesc {

    /** the applet name */
    private final String name;

    /** the main class name and package */
    private final String mainClass;

    /** the document base */
    private final URL documentBase;

    /** the width */
    private final int width;

    /** the height */
    private final int height;

    /** the parameters */
    private final Map<String, String> parameters;

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
        this.parameters = new HashMap<>(parameters);
    }

    /**
     * @return the applet name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the main class name in the dot-separated form (eg: foo.bar.Baz)
     */
    @Override
    public String getMainClass() {
        return mainClass;
    }

    /**
     * @return the document base
     */
    public URL getDocumentBase() {
        return documentBase;
    }

    /**
     * @return the width
     */
    public int getWidth() {
        if (width < Integer.valueOf(JNLPRuntime.getConfiguration().getProperty(DeploymentConfiguration.KEY_SMALL_SIZE_OVERRIDE_TRESHOLD))) {
            Integer nww = fixWidth();
            if (nww != null) {
                return nww;
            }
        }
        return width;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        if (height < Integer.valueOf(JNLPRuntime.getConfiguration().getProperty(DeploymentConfiguration.KEY_SMALL_SIZE_OVERRIDE_TRESHOLD))) {
            Integer nwh = fixHeight();
            if (nwh != null) {
                return nwh;
            }
        }
        return height;
    }

    /**
     * @return  the applet parameters
     */
    public Map<String, String> getParameters() {
        return new HashMap<>(parameters);
    }

    /**
     * Adds a parameter to the applet.  If the parameter already
     * exists then it is overwritten with the new value.  Adding a
     * parameter will have no effect on already-running applets
     * launched from this JNLP file.
     * @param name key of value
     * @param value value to be added
     */
    public void addParameter(String name, String value) {
        parameters.put(name, value);
    }

    private Integer fixHeight() {
        return fixSize(DeploymentConfiguration.KEY_SMALL_SIZE_OVERRIDE_HEIGHT, "Height", "height", "HEIGHT");
    }
    private Integer fixWidth() {
        return fixSize(DeploymentConfiguration.KEY_SMALL_SIZE_OVERRIDE_WIDTH, "Width", "width", "WIDTH");
    }

    private Integer fixSize(String depKey, String... keys) {
        OutputController.getLogger().log("Found to small applet!");
        try {
            Integer depVal = Integer.valueOf(JNLPRuntime.getConfiguration().getProperty(depKey));
            if (depVal == 0) {
                OutputController.getLogger().log("using its size");
                return null;
            }
            if (depVal < 0) {
                OutputController.getLogger().log("enforcing " + depVal);
                return Math.abs(depVal);
            }
            for (String key : keys) {
                String sizeFromParam = parameters.get(key);
                if (sizeFromParam != null) {
                    try {
                        OutputController.getLogger().log("using its "+key+"=" + sizeFromParam);
                        return Integer.valueOf(sizeFromParam);
                    } catch (NumberFormatException ex) {
                        OutputController.getLogger().log(ex);
                    }
                }
            }
            OutputController.getLogger().log("defaulting to " + depVal);
            return depVal;
        } catch (NumberFormatException | NullPointerException ex) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, ex);
            return null;
        }
    }

}
