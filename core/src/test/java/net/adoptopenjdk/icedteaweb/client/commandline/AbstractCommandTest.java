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
import net.adoptopenjdk.icedteaweb.commandline.CommandLineOptionsDefinition;
import net.adoptopenjdk.icedteaweb.commandline.CommandLineOptionsParser;
import net.sourceforge.jnlp.config.PathsAndFiles;
import net.sourceforge.jnlp.util.logging.NoStdOutErrTest;
import net.sourceforge.jnlp.util.logging.OutputController;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;

/**
 * Base test class to test various Iced-tea web {@link CommandLine} commands.
 */
public abstract class AbstractCommandTest extends NoStdOutErrTest {
    private static List<CommandLineOptions> commandLineOptions;

    private static File userDeploymentPropertiesFile;
    private static String originalUserDeploymentPropertiesFileContent;

    private ByteArrayOutputStream outContent;
    private ByteArrayOutputStream errContent;


    @BeforeClass
    public static void beforeAll() throws IOException {
        Locale.setDefault(Locale.ENGLISH);
        commandLineOptions = CommandLineOptionsDefinition.getItwsettingsCommands();

        userDeploymentPropertiesFile = PathsAndFiles.USER_DEPLOYMENT_FILE.getFile();
        originalUserDeploymentPropertiesFileContent =
                userDeploymentPropertiesFile.toPath().toFile().exists() ? getUserDeploymentPropertiesFileContent() : "";

        clearDeployFile();
    }

    @AfterClass
    public static void afterClass() throws IOException {
        Files.write(userDeploymentPropertiesFile.toPath(), originalUserDeploymentPropertiesFileContent.getBytes());
    }

    @Before
    public void beforeEach() {
        outContent = new ByteArrayOutputStream();
        OutputController.getLogger().setOut(new PrintStream(outContent));
        errContent = new ByteArrayOutputStream();
        OutputController.getLogger().setErr(new PrintStream(errContent));
    }

    @After
    public void afterEach() throws IOException {
        clearDeployFile();
    }

    String getOutContent() {
        return outContent.toString();
    }

    String getErrContent() {
        return errContent.toString();
    }

    CommandLine getCommandLine(final String[] args) {
        return new CommandLine(new CommandLineOptionsParser(args, commandLineOptions));
    }

    static String getUserDeploymentPropertiesFileContent() throws IOException {
        return new String(Files.readAllBytes(userDeploymentPropertiesFile.toPath()));
    }

    private static void clearDeployFile() throws IOException {
        Files.write(userDeploymentPropertiesFile.toPath(), new byte[0]);
    }
}