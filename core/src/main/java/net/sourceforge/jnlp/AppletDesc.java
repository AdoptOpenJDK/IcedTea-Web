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

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The applet-desc element.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.8 $
 */
public class AppletDesc implements LaunchDesc {

    private final static Logger LOG = LoggerFactory.getLogger(AppletDesc.class);

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
    public AppletDesc(final String name, final String mainClass, final URL documentBase, final int width, final int height,
                      final Map<String, String> parameters) {
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

    private Integer getConfigurationPropertyAsInt(final String name) {
        return Integer.valueOf(JNLPRuntime.getConfiguration().getProperty(name));
    }

    /**
     * @return the width
     */
    public int getWidth() {
        if (width < getConfigurationPropertyAsInt(DeploymentConfiguration.KEY_SMALL_SIZE_OVERRIDE_TRESHOLD)) {
            final Integer nww = fixWidth();
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
        if (height < getConfigurationPropertyAsInt(DeploymentConfiguration.KEY_SMALL_SIZE_OVERRIDE_TRESHOLD)) {
            final Integer nwh = fixHeight();
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
        return Collections.unmodifiableMap(parameters);
    }

    /**
     * Adds a parameter to the applet.  If the parameter already
     * exists then it is overwritten with the new value.  Adding a
     * parameter will have no effect on already-running applets
     * launched from this JNLP file.
     * @param name key of value
     * @param value value to be added
     */
    public void addParameter(final String name, final String value) {
        parameters.put(name, value);
    }

    private Integer fixHeight() {
        return fixSize(DeploymentConfiguration.KEY_SMALL_SIZE_OVERRIDE_HEIGHT, "Height", "height", "HEIGHT");
    }
    private Integer fixWidth() {
        return fixSize(DeploymentConfiguration.KEY_SMALL_SIZE_OVERRIDE_WIDTH, "Width", "width", "WIDTH");
    }

    private Integer fixSize(final String depKey, final String... keys) {
        LOG.info("Found to small applet!");
        try {
            final Integer depVal = getConfigurationPropertyAsInt(depKey);
            if (depVal == 0) {
                LOG.info("using its size");
                return null;
            }
            if (depVal < 0) {
                LOG.info("enforcing {}", depVal);
                return Math.abs(depVal);
            }
            for (final String key : keys) {
                final String sizeFromParam = parameters.get(key);
                if (sizeFromParam != null) {
                    try {
                        LOG.info("using its {}={}", key, sizeFromParam);
                        return Integer.valueOf(sizeFromParam);
                    } catch (NumberFormatException ex) {
                        LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
                    }
                }
            }
            LOG.info("defaulting to {}", depVal);
            return depVal;
        } catch (final NumberFormatException | NullPointerException ex) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
            return null;
        }
    }

}
