/* CommandLine.java -- command line interface to icedtea-web's deployment settings.
Copyright (C) 2010 Red Hat
Copyright (C) 2019 Karakun AG

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

package net.adoptopenjdk.icedteaweb.client.commandline;

import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity.AppletSecurityLevel;
import net.adoptopenjdk.icedteaweb.commandline.CommandLineOptions;
import net.adoptopenjdk.icedteaweb.commandline.UnevenParameterException;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_SECURITY_LEVEL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test whether the {@code -set} command of the {@link CommandLine} executes and terminates as expected.
 */
public class SetCommandTest extends AbstractCommandTest {

    @Test
    public void testSetCommand() throws IOException {
        // GIVEN -----------
        final String[] args = { "-set", "deployment.security.level", "ALLOW_UNSIGNED" }; // use literals for readability

        // test if literals still backed by constants
        assertThat(Arrays.asList(args), contains(CommandLineOptions.SET.getOption(), KEY_SECURITY_LEVEL, AppletSecurityLevel.ALLOW_UNSIGNED.name()));

        // WHEN ------------
        final int status = getCommandLine(args).handle();

        // THEN ------------
        assertEquals(CommandLine.SUCCESS, status);
        assertTrue(getOutContent().isEmpty());
        assertThat(getUserDeploymentPropertiesFileContent(), containsString(AppletSecurityLevel.ALLOW_UNSIGNED.name()));
    }

    @Test
    public void testSetCommandDisplaysWarningOnUnknownProperty() {
        // GIVEN -----------
        final String[] args = { "-set", "unknown.setting.name", "does_not_matter" }; // use literals for readability

        // test if literals still backed by constants
        assertThat(Arrays.asList(args), hasItem(CommandLineOptions.SET.getOption()));

        // WHEN ------------
        final int status = getCommandLine(args).handle();

        // THEN ------------
        assertEquals(CommandLine.SUCCESS, status);
        assertThat(getOutContent(), containsString(R("CLWarningUnknownProperty", "unknown.setting.name") ));
    }

    @Test
    public void testSetCommandWithIdenticalKeyAndValue() throws IOException {
        // GIVEN -----------
        final String[] args = { "-set", "same", "same"}; // use literals for readability

        // test if literals still backed by constants
        assertThat(Arrays.asList(args), hasItem(CommandLineOptions.SET.getOption()));

        // WHEN ------------
        final int status = getCommandLine(args).handle();

        // THEN ------------
        assertEquals(CommandLine.SUCCESS, status);
        assertThat(getOutContent(), containsString(R("CLWarningUnknownProperty", "same")));
        assertThat(getUserDeploymentPropertiesFileContent(), containsString("same=same"));
    }

    @Test
    public void testSetCommandWithDuplicateKeyValue() throws IOException {
        // GIVEN -----------
        final String[] args = { "-set", "name", "same", "same", "value" }; // use literals for readability

        // test if literals still backed by constants
        assertThat(Arrays.asList(args), hasItem(CommandLineOptions.SET.getOption()));

        // WHEN ------------
        final int status = getCommandLine(args).handle();

        // THEN ------------
        assertEquals(CommandLine.SUCCESS, status);
        assertThat(getOutContent(), containsString(R("CLWarningUnknownProperty", "name")));
        assertThat(getOutContent(), containsString(R("CLWarningUnknownProperty", "same")));
        assertThat(getUserDeploymentPropertiesFileContent(), containsString("name=same"));
        assertThat(getUserDeploymentPropertiesFileContent(), containsString("same=value"));
    }

    @Test
    public void testSetCommandWithPropertyWithIncorrectValue() {
        // GIVEN -----------
        final String[] args = { "-set", "deployment.security.level", "INTENTIONALLY_INCORRECT" }; // use literals for readability

        // test if literals still backed by constants
        assertThat(Arrays.asList(args), hasItems(CommandLineOptions.SET.getOption(), KEY_SECURITY_LEVEL));

        // WHEN ------------
        final int status = getCommandLine(args).handle();

        // THEN ------------
        assertEquals(CommandLine.ERROR, status);
    }

    @Test(expected = UnevenParameterException.class)
    public void testSetCommandWithOddNumberOfParams() {
        // GIVEN -----------
        final String[] args = { "-set", "name", "value", "another"};

        // test if literals still backed by constants
        assertThat(Arrays.asList(args), hasItem(CommandLineOptions.SET.getOption()));

        // WHEN ------------
        getCommandLine(args).handleSetCommand();
    }

    @Test
    public void testSetCommandWithValueHavingSpace() throws IOException {
        // GIVEN -----------
        final String[] args = { "-set", "name", "hello world" };

        // WHEN ------------
        final int status = getCommandLine(args).handle();

        // THEN ------------
        assertEquals(CommandLine.SUCCESS, status);
        assertThat(getOutContent(), containsString(R("CLWarningUnknownProperty", "name")));
        assertThat(getUserDeploymentPropertiesFileContent(), containsString("name=hello world"));
    }

    @Test
    public void testSetCommandWithKeyHavingSpace() throws IOException {
        // GIVEN -----------
        final String[] args = { "-set", "some name", "value" };

        // WHEN ------------
        final int status = getCommandLine(args).handle();

        // THEN ------------
        assertEquals(CommandLine.SUCCESS, status);
        assertThat(getOutContent(), containsString(R("CLWarningUnknownProperty", "some name")));
        assertThat(getUserDeploymentPropertiesFileContent(), containsString("some\\ name=value"));
    }
}
