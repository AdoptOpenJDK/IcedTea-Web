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

import java.util.Arrays;
import net.adoptopenjdk.icedteaweb.commandline.CommandLineOptions;
import org.junit.Test;

import static net.adoptopenjdk.icedteaweb.client.commandline.CommandLine.SUCCESS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;

/**
 * Test the Iced-tea web settings commands.
 */
public class HelpCommandTest extends AbstractCommandTest {

    /**
     * Test whether the {@code -help}, command executes and terminates with {@link CommandLine#SUCCESS}.
     */
    @Test
    public void testHelpCommand() {
        // GIVEN -----------
        final String[] args = {"-help"}; // use literals for readability

        // test if literals still backed by constants
        assertThat(Arrays.asList(args), contains(CommandLineOptions.HELP2.getOption()));

        // WHEN ------------
        final int status = getCommandLine(args).handle();

        // THEN ------------
        assertEquals(SUCCESS, status);
        assertEquals("\n" +
                "SYNOPSIS\n" +
                "    itweb-settings\n" +
                "    itweb-settings command arguments\n" +
                "\n" +
                "DESCRIPTION\n" +
                "    itweb-settings is a command line and a GUI program to modify and edit settings used by the IcedTea-Web implementation of javaws and the browser plugin.\n" +
                "    \n" +
                "    If executed without any arguments, it starts up a GUI. Otherwise, it tries to do what is specified in the argument.\n" +
                "    \n" +
                "    The command-line allows quickly searching, making a copy of and modifying specific settings without having to hunt through a UI.\n" +
                "\n" +
                "DESCRIPTION\n" +
                "    -check          - Checks that all the current settings have valid values.(No argument expected)\n" +
                "    -get name       - Shows the value of the specified settings.(Expected one or more arguments)\n" +
                "    -headless       - Disables download window, other UIs.(No argument expected)\n" +
                "    -help           - Prints out information about supported command and basic usage. Can also take an parameter, and then it prints detailed help for this command.(No argument expected)\n" +
                "    -info name      - Shows additional information about the named settings. Includes a description, the current value, the possible values, and the source of the setting.(Expected one or more arguments)\n" +
                "    -list           - Shows a list of all the IcedTea-Web settings and their current values.(No argument expected)\n" +
                "    -reset name     - Resets the specified settings to their original value.(Expected one or more arguments)\n" +
                "    -reset all      - Resets all settings to their original values.(No argument expected)\n" +
                "    -set name value - Sets the settings to the new value specified, if it is an appropriate value.(Expected even number of arguments with param=value as valid argument)\n" +
                "    -verbose        - Enable verbose output.(No argument expected)\n" +
                "\n", getOutContent());
    }
}