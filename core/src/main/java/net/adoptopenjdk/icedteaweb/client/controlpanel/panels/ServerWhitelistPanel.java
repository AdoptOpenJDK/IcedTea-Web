/* SecuritySettingsPanel.java -- Display possible security settings.
Copyright (C) 2010 Red Hat

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
*/
package net.adoptopenjdk.icedteaweb.client.controlpanel.panels;

import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.util.whitelist.UrlWhiteListUtils;
import net.sourceforge.jnlp.util.whitelist.WhitelistEntry;

import java.util.List;

/**
 * This provides a way for the user to display the server white list defined in <code>deployment.properties</code>.
 */
@SuppressWarnings("serial")
public class ServerWhitelistPanel extends AbstractUrlWhitelistPanel {

    /**
     * This creates a new instance of the server white list panel.
     *
     * @param config Loaded DeploymentConfiguration file.
     */
    public ServerWhitelistPanel(DeploymentConfiguration config) {
        super(config, Translator.R("CPServerWhitelist"));
    }

    @Override
    protected List<WhitelistEntry> getUrlWhitelist() {
        return UrlWhiteListUtils.getApplicationUrlWhiteList();
    }
}
