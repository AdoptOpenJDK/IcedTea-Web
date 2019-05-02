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

import net.adoptopenjdk.icedteaweb.commandline.CommandLineOptions;
import org.junit.Test;

import java.util.Arrays;

import static net.adoptopenjdk.icedteaweb.client.commandline.CommandLine.ERROR;
import static net.adoptopenjdk.icedteaweb.client.commandline.CommandLine.SUCCESS;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_SECURITY_LEVEL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;

/**
 * Test whether the {@code -get} command of the {@link CommandLine} executes and terminates as expected.
 */
public class GetCommandTest extends AbstractCommandTest {
    /**
     * Test whether the {@code -set}, command executes and terminates with {@link CommandLine#SUCCESS}.
     */
    @Test
    public void testGetCommand() {
        // GIVEN -----------
        final String[] args = {"-get", "deployment.security.level"}; // use literals for readability

        // test if literals still backed by constants
        assertThat(Arrays.asList(args), contains(CommandLineOptions.GET.getOption(), KEY_SECURITY_LEVEL));

        // WHEN ------------
        final int status = getCommandLine(args).handle();

        // THEN ------------
        assertEquals(SUCCESS, status);
        assertThat(getOutContent(), containsString(KEY_SECURITY_LEVEL + ": " + "null"));
    }


    /**
     * Test whether the {@code -set}, command executes and terminates with {@link CommandLine#ERROR} for incorrect value.
     */
    @Test
    public void testGetCommandWithIncorrectValue() {
        // GIVEN -----------
        final String[] args = {"-get", "deployment.security.level", "INTENTIONALLY_INCORRECT"}; // use literals for readability

        // test if literals still backed by constants
        assertThat(Arrays.asList(args), contains(CommandLineOptions.GET.getOption(), KEY_SECURITY_LEVEL, "INTENTIONALLY_INCORRECT"));

        // WHEN ------------
        final int status = getCommandLine(args).handle();

        // THEN ------------
        assertEquals(ERROR, status);
    }
}