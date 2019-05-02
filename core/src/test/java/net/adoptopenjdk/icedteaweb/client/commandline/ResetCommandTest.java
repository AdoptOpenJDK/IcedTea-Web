// Copyright (C) 2019 Karakun AG
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

package net.adoptopenjdk.icedteaweb.client.commandline;

import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity.AppletSecurityLevel;
import net.adoptopenjdk.icedteaweb.commandline.CommandLineOptions;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static net.adoptopenjdk.icedteaweb.client.commandline.CommandLine.ERROR;
import static net.adoptopenjdk.icedteaweb.client.commandline.CommandLine.SUCCESS;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_ENABLE_LOGGING;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_SECURITY_LEVEL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;

/**
 * Test whether the {@code -reset} command of the {@link CommandLine} executes and terminates as expected.
 */
public class ResetCommandTest extends AbstractCommandTest {

    @Before
    public void beforeEach() {
        super.beforeEach();

        // prepare some custom settings so that we have something to reset
        getCommandLine(new String[]{"-set", "deployment.security.level", "ALLOW_UNSIGNED"}).handle();
        getCommandLine(new String[]{"-set", "deployment.log", "true"}).handle();
    }

    @Test
    public void testResetCommand() throws IOException {
        // GIVEN -----------
        assertThat(getUserDeploymentPropertiesFileContent(), containsString(KEY_SECURITY_LEVEL + "=" + AppletSecurityLevel.ALLOW_UNSIGNED.name()));

        final String[] args = {"-reset", "deployment.security.level"}; // use literals for readability

        // test if literals still backed by constants
        assertThat(Arrays.asList(args), contains(CommandLineOptions.RESET.getOption(), KEY_SECURITY_LEVEL));

        // WHEN ------------
        final int status = getCommandLine(args).handle();

        // THEN ------------
        assertEquals(SUCCESS, status);
        assertThat(getUserDeploymentPropertiesFileContent(), not(containsString(AppletSecurityLevel.ALLOW_UNSIGNED.name())));
    }

    @Test
    public void testResetCommandWithUnknownProperty() throws IOException {
        // GIVEN -----------
        assertThat(getUserDeploymentPropertiesFileContent(), containsString(KEY_SECURITY_LEVEL + "=" + AppletSecurityLevel.ALLOW_UNSIGNED.name()));

        final String[] args = {"-reset", "unknown.setting.name"}; // use literals for readability

        // test if literals still backed by constants
        assertThat(Arrays.asList(args), hasItem(CommandLineOptions.RESET.getOption()));

        // WHEN ------------
        final int status = getCommandLine(args).handle();

        // THEN ------------
        assertEquals(ERROR, status);
    }

    @Test
    public void testResetAllCommand() throws IOException {
        // GIVEN -----------
        assertThat(getUserDeploymentPropertiesFileContent(), containsString(KEY_SECURITY_LEVEL + "=" + AppletSecurityLevel.ALLOW_UNSIGNED.name()));
        assertThat(getUserDeploymentPropertiesFileContent(), containsString(KEY_ENABLE_LOGGING + "=" + "true"));

        final String[] args = {"-reset", "all"}; // use literals for readability

        // test if literals still backed by constants
        assertThat(Arrays.asList(args), hasItem(CommandLineOptions.RESETALL.getOption()));

        // WHEN ------------
        final int status = getCommandLine(args).handle();

        // THEN ------------
        assertEquals(SUCCESS, status);
        assertThat(getUserDeploymentPropertiesFileContent(), not(containsString(AppletSecurityLevel.ALLOW_UNSIGNED.name())));
        assertThat(getUserDeploymentPropertiesFileContent(), not(containsString("true")));
    }
}