/* CommandLine.java -- command line interface to icedtea-web's deployment settings.
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

package net.sourceforge.jnlp.controlpanel;

import static net.sourceforge.jnlp.runtime.Translator.R;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;

import net.sourceforge.jnlp.OptionsDefinitions;
import net.sourceforge.jnlp.config.PathsAndFiles;
import net.sourceforge.jnlp.util.logging.NoStdOutErrTest;
import net.sourceforge.jnlp.util.logging.OutputController;
import net.sourceforge.jnlp.util.optionparser.OptionParser;
import net.sourceforge.jnlp.util.optionparser.UnevenParameterException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class CommandLineTest extends NoStdOutErrTest{

    public static final int ERROR = 1;
    public static final int SUCCESS = 0;
    private static File userDeployFile;
    private static String userDeployContents;

    @BeforeClass
    public static void setup() throws IOException {
        userDeployFile = PathsAndFiles.USER_DEPLOYMENT_FILE.getFile();
        userDeployContents = new String(Files.readAllBytes(userDeployFile.toPath()));
        clearDeployFile();
    }

    @AfterClass
    public static void afterClass() throws IOException {
        Files.write(userDeployFile.toPath(), userDeployContents.getBytes());
    }

    private static void clearDeployFile() throws IOException {
        String clear = "";
        Files.write(userDeployFile.toPath(), clear.getBytes());
    }

    private ByteArrayOutputStream getOutputControllerStream() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        PrintStream outPrintStream = new PrintStream(outStream);
        OutputController.getLogger().setOut(outPrintStream);

        return outStream;
    }

    @Test
    public void testHandleSetCommand() throws IOException {
        ByteArrayOutputStream outStream = getOutputControllerStream();

        String[] args = {
                "set", "deployment.security.level", "ALLOW_UNSIGNED"
        };
        OptionParser optionParser = new OptionParser(args, OptionsDefinitions.getItwsettingsCommands());
        CommandLine commandLine = new CommandLine(optionParser);
        int status = commandLine.handleSetCommand();

        assertTrue(outStream.toString().isEmpty());
        assertEquals(SUCCESS, status);

        clearDeployFile();
    }

    @Test
    public void testHandleSetCommandDisplaysWarningOnUknownProperty() throws IOException {
        ByteArrayOutputStream outStream = getOutputControllerStream();

        String[] args = {
                "set", "unknown", "does_not_matter"
        };
        OptionParser optionParser = new OptionParser(args, OptionsDefinitions.getItwsettingsCommands());
        CommandLine commandLine = new CommandLine(optionParser);
        int status = commandLine.handleSetCommand();

        String output = outStream.toString();
        assertEquals(output, R("CLWarningUnknownProperty", "unknown") + "\n");
        assertEquals(SUCCESS, status);

        clearDeployFile();
    }

    @Test
    public void testSetWithDuplicateKeyValuePair() throws IOException {
        ByteArrayOutputStream outStream = getOutputControllerStream();

        String[] args = {
                "set", "blah", "blah"
        };
        OptionParser optionParser = new OptionParser(args, OptionsDefinitions.getItwsettingsCommands());
        CommandLine commandLine = new CommandLine(optionParser);
        int status = commandLine.handleSetCommand();
        String contents = new String(Files.readAllBytes(userDeployFile.toPath()));

        String output = outStream.toString();
        assertEquals(output, R("CLWarningUnknownProperty", "blah") + "\n");
        assertEquals(SUCCESS, status);
        assertTrue(contents.contains("blah=blah"));

        clearDeployFile();
    }

    @Test
    public void testSetWithDuplicateKeyValue() throws IOException {
        ByteArrayOutputStream outStream = getOutputControllerStream();

        String[] args = {
                "set", "blue", "blah", "blah", "green"
        };

        OptionParser optionParser = new OptionParser(args, OptionsDefinitions.getItwsettingsCommands());
        CommandLine commandLine = new CommandLine(optionParser);
        int status = commandLine.handleSetCommand();
        String contents = new String(Files.readAllBytes(userDeployFile.toPath()));

        String output = outStream.toString();
        assertTrue(output.contains(R("CLWarningUnknownProperty", "blue") + "\n"));
        assertTrue(output.contains(R("CLWarningUnknownProperty", "blah") + "\n"));
        assertTrue(contents.contains("blue=blah"));
        assertTrue(contents.contains("blah=green"));
        assertEquals(SUCCESS, status);

        clearDeployFile();
    }

    @Test
    public void testSetPropertyWithIncorrectValue() throws IOException {
        String[] args = {
                "set", "deployment.security.level", "INTENTIONALLY_INCORRECT"
        };

        OptionParser optionParser = new OptionParser(args, OptionsDefinitions.getItwsettingsCommands());
        CommandLine commandLine = new CommandLine(optionParser);
        int status = commandLine.handleSetCommand();

        assertEquals(ERROR, status);

        clearDeployFile();
    }

    @Test(expected = UnevenParameterException.class)
    public void testSetOddNumberOfParams() throws IOException {
        ByteArrayOutputStream outStream = getOutputControllerStream();

        String[] args = {
                "set", "blue", "blah", "purple"
        };

        OptionParser optionParser = new OptionParser(args, OptionsDefinitions.getItwsettingsCommands());
        CommandLine commandLine = new CommandLine(optionParser);
        int status = commandLine.handleSetCommand();

        clearDeployFile();
    }

    @Test
    public void testSetWithValueHavingSpace() throws IOException {
        ByteArrayOutputStream outStream = getOutputControllerStream();

        String[] args = {
                "set", "blue", "blah red"
        };

        OptionParser optionParser = new OptionParser(args, OptionsDefinitions.getItwsettingsCommands());
        CommandLine commandLine = new CommandLine(optionParser);
        int status = commandLine.handleSetCommand();
        String contents = new String(Files.readAllBytes(userDeployFile.toPath()));

        String output = outStream.toString();
        assertTrue(output.contains(R("CLWarningUnknownProperty", "blue") + "\n"));
        assertTrue(contents.contains("blue=blah red"));
        assertEquals(SUCCESS, status);

        clearDeployFile();
    }

    @Test
    public void testSetWithKeyHavingSpace() throws IOException {
        ByteArrayOutputStream outStream = getOutputControllerStream();

        String[] args = {
                "set", "blue green", "blah"
        };

        OptionParser optionParser = new OptionParser(args, OptionsDefinitions.getItwsettingsCommands());
        CommandLine commandLine = new CommandLine(optionParser);
        int status = commandLine.handleSetCommand();
        String contents = new String(Files.readAllBytes(userDeployFile.toPath()));

        String output = outStream.toString();
        assertTrue(output.contains(R("CLWarningUnknownProperty", "blue green") + "\n"));
        assertTrue(contents.contains("blue\\ green=blah"));
        assertEquals(SUCCESS, status);

        clearDeployFile();
    }
}
