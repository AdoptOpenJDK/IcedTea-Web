package net.sourceforge.jnlp.util;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

import java.util.Map;

public class AppletUtils {
    private final static Logger LOG = LoggerFactory.getLogger(AppletUtils.class);

    /**
     * @return the width of the Applet in pixels
     */
    public static int getFixedWidth(final int width, final Map<String, String> parameters) {
        Assert.requireNonNull(parameters, "parameters");

        if (width < getConfigurationPropertyAsInt(ConfigurationConstants.KEY_SMALL_SIZE_OVERRIDE_THRESHOLD)) {
            final Integer nww = fixWidth(parameters);
            if (nww != null) {
                return nww;
            }
        }
        return width;
    }

    /**
     * @return the height of the Applet in pixels
     */
    public static int getFixedHeight(final int height, final Map<String, String> parameters) {
        Assert.requireNonNull(parameters, "parameters");

        if (height < getConfigurationPropertyAsInt(ConfigurationConstants.KEY_SMALL_SIZE_OVERRIDE_THRESHOLD)) {
            final Integer nwh = fixHeight(parameters);
            if (nwh != null) {
                return nwh;
            }
        }
        return height;
    }

    private static Integer fixHeight(final Map<String, String> parameters) {
        return fixSize(ConfigurationConstants.KEY_SMALL_SIZE_OVERRIDE_HEIGHT, parameters, "Height", "height", "HEIGHT");
    }
    private static Integer fixWidth(final Map<String, String> parameters) {
        return fixSize(ConfigurationConstants.KEY_SMALL_SIZE_OVERRIDE_WIDTH, parameters, "Width", "width", "WIDTH");
    }

    private static Integer getConfigurationPropertyAsInt(final String name) {
        return Integer.valueOf(JNLPRuntime.getConfiguration().getProperty(name));
    }

    private static Integer fixSize(final String depKey, final Map<String, String> parameters, final String... keys) {
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
