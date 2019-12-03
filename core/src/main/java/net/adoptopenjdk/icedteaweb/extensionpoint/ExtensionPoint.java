package net.adoptopenjdk.icedteaweb.extensionpoint;

import net.adoptopenjdk.icedteaweb.client.controlpanel.ControlPanelStyle;
import net.adoptopenjdk.icedteaweb.client.controlpanel.DefaultControlPanelStyle;
import net.adoptopenjdk.icedteaweb.launch.JvmLauncher;
import net.sourceforge.jnlp.ItwJvmLauncher;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.proxy.browser.BrowserAwareProxySelector;
import net.sourceforge.jnlp.runtime.ItwMenuAndDesktopIntegration;
import net.sourceforge.jnlp.runtime.MenuAndDesktopIntegration;

import java.net.ProxySelector;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * Extension point which allows to set a different implementation for an interface at runtime.
 *
 * Upon start of ITW extension points are loaded using the ServiceLoader mechanism.
 * If no implementation is found the {@link #DEFAULT} is used.
 * If exactly one implementation is found this one is used.
 * If more are found an {@link IllegalStateException} is thrown.
 */
public interface ExtensionPoint {

    ExtensionPoint DEFAULT = new ExtensionPoint() {};

    default JvmLauncher createJvmLauncher(final DeploymentConfiguration configuration) {
        return new ItwJvmLauncher();
    }

    default MenuAndDesktopIntegration createMenuAndDesktopIntegration(final DeploymentConfiguration configuration) {
        return new ItwMenuAndDesktopIntegration();
    }

    default ProxySelector createProxySelector(final DeploymentConfiguration configuration) {
        return new BrowserAwareProxySelector(configuration);
    }

    default ControlPanelStyle createControlPanelStyle(final DeploymentConfiguration configuration) {
        return new DefaultControlPanelStyle();
    }

    default List<String> getTranslationResources() {
        return emptyList();
    }
}
