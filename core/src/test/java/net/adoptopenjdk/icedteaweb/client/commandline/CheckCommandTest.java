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

import static net.adoptopenjdk.icedteaweb.client.commandline.CommandLine.SUCCESS;
import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;

/**
 * Test whether the {@code -check} command of the {@link CommandLine} executes and terminates as expected.
 */
public class CheckCommandTest extends AbstractCommandTest {
    @Test
    public void testCheckCommand() {
        // GIVEN -----------
        final String[] args = {"-check"}; // use literals for readability

        // test if literals still backed by constants
        assertThat(Arrays.asList(args), contains(CommandLineOptions.CHECK.getOption()));

        // WHEN ------------
        final int status = getCommandLine(args).handle();

        // THEN ------------
        assertEquals(SUCCESS, status);
        assertThat(getOutContent(), containsString(R("CLNoIssuesFound")));
    }
}