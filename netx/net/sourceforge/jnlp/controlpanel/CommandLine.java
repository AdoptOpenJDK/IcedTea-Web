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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.naming.ConfigurationException;

import net.sourceforge.jnlp.config.ConfiguratonValidator;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.config.Setting;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.logging.OutputController;

/**
 * Encapsulates a command line interface to the deployment configuration.
 * <p>
 * The central method is {@link #handle(String[])}, which calls one of the
 * various 'handle' methods. The commands listed in {@link #allCommands} are
 * supported. For each supported command, a method handleCOMMANDCommand exists.
 * This method actually takes action based on the command. Generally, a
 * printCOMMANDHelp method also exists, and prints out the help message for
 * that specific command. For example, see {@link #handleListCommand(List)}
 * and {@link #printListHelp()}.
 * </p>
 * Sample usage:
 * <pre><code>
 * CommandLine cli = new CommandLine();
 * // the string array represents input using the command line
 * int retVal = cli.handle(new String[] { "help" });
 * if (retVal == CommandLine.SUCCESS) {
 *    // good!
 * } else {
 *    // bad!
 * }
 * </code></pre>
 *
 * @author <a href="mailto:Omair%20Majid%20&lt;omajid@redhat.com&gt;">Omair Majid</a>
 */
public class CommandLine {

    public static final int ERROR = 1;
    public static final int SUCCESS = 0;

    public final String PROGRAM_NAME;

    private static final List<String> allCommands = Arrays.asList(new String[] {
            "list", "get", "set", "reset", "info", "check"
    });

    DeploymentConfiguration config = null;

    /**
     * Creates a new instance
     */
    public CommandLine() {
        PROGRAM_NAME = System.getProperty("icedtea-web.bin.name");

        config = new DeploymentConfiguration();
        try {
            config.load(false);
        } catch (ConfigurationException e) {
            OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, R("RConfigurationFatal"));
            OutputController.getLogger().log(e);
        }
    }

    /**
     * Handle the 'help' command
     *
     * @param args optional
     * @return the result of handling the help command. SUCCESS if no errors occurred.
     */
    public int handleHelpCommand(List<String> args) {
        OutputController.getLogger().printOutLn(R("Usage"));
        OutputController.getLogger().printOutLn("  " + PROGRAM_NAME + " "
                + allCommands.toString().replace(',', '|').replaceAll(" ", "") + " [help]");
        OutputController.getLogger().printOutLn(R("CLHelpDescription", PROGRAM_NAME));
        return SUCCESS;
    }

    /**
     * Prints help message for the list command
     */
    public void printListHelp() {
        OutputController.getLogger().printOutLn(R("Usage"));
        OutputController.getLogger().printOutLn("  " + PROGRAM_NAME + " list [--details]");
        OutputController.getLogger().printOutLn(R("CLListDescription"));
    }

    /**
     * Handles the 'list' command
     *
     * @param args the arguments to the list command
     * @return result of handling the command. SUCCESS if no errors occurred.
     */
    public int handleListCommand(List<String> args) {
        if (args.contains("help")) {
            printListHelp();
            return SUCCESS;
        }

        boolean verbose = false;

        if (args.contains("--details")) {
            verbose = true;
            args.remove("--details");
        }

        if (args.size() != 0) {
            printListHelp();
            return ERROR;
        }

        Map<String, Setting<String>> all = config.getRaw();
        for (String key : all.keySet()) {
            Setting<String> value = all.get(key);
            OutputController.getLogger().printOutLn(key + ": " + value.getValue());
            if (verbose) {
                OutputController.getLogger().printOutLn("\t" + R("CLDescription", value.getDescription()));
            }
        }
        return SUCCESS;
    }

    /**
     * Prints help message for the get command
     */
    public void printGetHelp() {
        OutputController.getLogger().printOutLn(R("Usage"));
        OutputController.getLogger().printOutLn("  " + PROGRAM_NAME + " get property-name");
        OutputController.getLogger().printOutLn(R("CLGetDescription"));
    }

    /**
     * Handles the 'get' command.
     *
     * @param args the arguments to the get command
     * @return an integer representing success (SUCCESS) or error handling the
     * get command.
     */
    public int handleGetCommand(List<String> args) {
        if (args.contains("help")) {
            printGetHelp();
            return SUCCESS;
        }

        if (args.size() != 1) {
            printGetHelp();
            return ERROR;
        }

        Map<String, Setting<String>> all = config.getRaw();

        String key = args.get(0);
        String value = null;
        if (all.containsKey(key)) {
            value = all.get(key).getValue();
            OutputController.getLogger().printOutLn(value);
            return SUCCESS;
        } else {
            OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, R("CLUnknownProperty", key));
            return ERROR;
        }
    }

    /**
     * Prints the help message for the 'set' command
     */
    public void printSetHelp() {
        OutputController.getLogger().printOutLn(R("Usage"));
        OutputController.getLogger().printOutLn("  " + PROGRAM_NAME + " set property-name value");
        OutputController.getLogger().printOutLn(R("CLSetDescription"));
    }

    /**
     * Handles the 'set' command
     *
     * @param args the arguments to the set command
     * @return an integer indicating success (SUCCESS) or error in handling
     * the command
     */
    public int handleSetCommand(List<String> args) {
        if (args.contains("help")) {
            printSetHelp();
            return SUCCESS;
        }

        if (args.size() != 2) {
            printSetHelp();
            return ERROR;
        }

        String key = args.get(0);
        String value = args.get(1);

        if (config.getRaw().containsKey(key)) {
            Setting<String> old = config.getRaw().get(key);
            if (old.getValidator() != null) {
                try {
                    old.getValidator().validate(value);
                } catch (IllegalArgumentException e) {
                    OutputController.getLogger().log(OutputController.Level.WARNING_ALL, R("CLIncorrectValue", old.getName(), value, old.getValidator().getPossibleValues()));
                    OutputController.getLogger().log(e);
                    return ERROR;
                }
            }
            config.setProperty(key, value);
        } else {
            OutputController.getLogger().printOutLn(R("CLWarningUnknownProperty", key));
            config.setProperty(key, value);
        }

        try {
            config.save();
        } catch (IOException e) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
            return ERROR;
        }

        return SUCCESS;
    }

    /**
     * Prints a help message for the reset command
     */
    public void printResetHelp() {
        OutputController.getLogger().printOutLn(R("Usage"));
        OutputController.getLogger().printOutLn("  " + PROGRAM_NAME + " reset [all|property-name]");
        OutputController.getLogger().printOutLn(R("CLResetDescription"));
    }

    /**
     * Handles the 'reset' command
     *
     * @param args the arguments to the reset command
     * @return an integer indicating success (SUCCESS) or error in handling
     * the command
     */
    public int handleResetCommand(List<String> args) {
        if (args.contains("help")) {
            printResetHelp();
            return SUCCESS;
        }

        if (args.size() != 1) {
            printResetHelp();
            return ERROR;
        }

        String key = args.get(0);

        boolean resetAll = false;
        if (key.equals("all")) {
            resetAll = true;
        }

        Map<String, Setting<String>> all = config.getRaw();
        if (!resetAll && !all.containsKey(key)) {
            OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, R("CLUnknownProperty", key));
            return ERROR;
        }

        if (resetAll) {
            for (String aKey: all.keySet()) {
                Setting<String> setting = all.get(aKey);
                setting.setValue(setting.getDefaultValue());
            }
        } else {
            Setting<String> setting = all.get(key);
            setting.setValue(setting.getDefaultValue());
        }

        try {
            config.save();
        } catch (IOException e) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
            return ERROR;
        }

        return SUCCESS;
    }

    /**
     * Print a help message for the 'info' command
     */
    public void printInfoHelp() {
        OutputController.getLogger().printOutLn(R("Usage"));
        OutputController.getLogger().printOutLn("  " + PROGRAM_NAME + " info property-name");
        OutputController.getLogger().printOutLn(R("CLInfoDescription"));
    }

    /**
     * Handles the 'info' command
     *
     * @param args the arguments to the info command
     * @return an integer indicating success (SUCCESS) or error in handling
     * the command
     */
    public int handleInfoCommand(List<String> args) {
        if (args.contains("help")) {
            printInfoHelp();
            return SUCCESS;
        }

        if (args.size() != 1) {
            printInfoHelp();
            return ERROR;
        }

        Map<String, Setting<String>> all = config.getRaw();

        String key = args.get(0);
        Setting<String> value = all.get(key);
        if (value == null) {
            OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, R("CLNoInfo"));
            return ERROR;
        } else {
            OutputController.getLogger().printOutLn(R("CLDescription", value.getDescription()));
            OutputController.getLogger().printOutLn(R("CLValue", value.getValue()));
            if (value.getValidator() != null) {
                OutputController.getLogger().printOutLn("\t" + R("VVPossibleValues", value.getValidator().getPossibleValues()));
            }
            OutputController.getLogger().printOutLn(R("CLValueSource", value.getSource()));
            return SUCCESS;
        }
    }

    /**
     * Prints a help message for the 'check' command
     */
    public void printCheckHelp() {
        OutputController.getLogger().printOutLn(R("Usage"));
        OutputController.getLogger().printOutLn("  " + PROGRAM_NAME + " check");
        OutputController.getLogger().printOutLn(R("CLCheckDescription"));
    }

    /**
     * Handles the 'check' command
     *
     * @param args the arguments to the check command.
     * @return an integer indicating success (SUCCESS) or error in handling
     * the command
     */
    public int handleCheckCommand(List<String> args) {
        if (args.contains("help")) {
            printCheckHelp();
            return SUCCESS;
        }

        if (args.size() != 0) {
            printCheckHelp();
            return ERROR;
        }

        Map<String, Setting<String>> all = config.getRaw();

        ConfiguratonValidator validator = new ConfiguratonValidator(all);
        validator.validate();

        boolean allValid = true;
        for (Setting<String> setting : validator.getIncorrectSetting()) {
            OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, R("CLIncorrectValue", setting.getName(), setting.getValue(), setting.getValidator().getPossibleValues()));
            allValid = false;
        }

        for (Setting<String> setting : validator.getUnrecognizedSetting()) {
            OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, R("CLUnknownProperty", setting.getName()));
            allValid = false;
        }

        if (allValid) {
            OutputController.getLogger().printOutLn(R("CLNoIssuesFound"));
            return SUCCESS;
        } else {
            return ERROR;
        }
    }

    /**
     * Handles overall command line arguments. The argument array is split
     * into two pieces: the first element is assumend to be the command, and
     * everything after is taken to be the argument to the command.
     *
     * @param commandAndArgs A string array representing the command and
     * arguments to take action on
     * @return an integer representing an error code or SUCCESS if no problems
     * occurred.
     */
    public int handle(String[] commandAndArgs) {

        if (commandAndArgs == null) {
            throw new NullPointerException("command is null");
        }

        if (commandAndArgs.length == 0) {
            handleHelpCommand(new ArrayList<String>());
            return ERROR;
        }

        String command = commandAndArgs[0];
        String[] argsArray = new String[commandAndArgs.length - 1];
        System.arraycopy(commandAndArgs, 1, argsArray, 0, commandAndArgs.length - 1);
        List<String> arguments = new ArrayList<String>(Arrays.asList(argsArray));

        int val;
        if (command.equals("help")) {
            val = handleHelpCommand(arguments);
        } else if (command.equals("list")) {
            val = handleListCommand(arguments);
        } else if (command.equals("set")) {
            val = handleSetCommand(arguments);
        } else if (command.equals("reset")) {
            val = handleResetCommand(arguments);
        } else if (command.equals("get")) {
            val = handleGetCommand(arguments);
        } else if (command.equals("info")) {
            val = handleInfoCommand(arguments);
        } else if (command.equals("check")) {
            val = handleCheckCommand(arguments);
        } else if (allCommands.contains(command)) {
            OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, "INTERNAL ERROR: " + command + " should have been implemented");
            val = ERROR;
        } else {
            OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, R("CLUnknownCommand", command));
            handleHelpCommand(new ArrayList<String>());
            val = ERROR;
        }

        return val;
    }

    /**
     * The starting point of the program
     * @param args the command line arguments to this program
     */
    public static void main(String[] args) throws Exception {
        DeploymentConfiguration.move14AndOlderFilesTo15StructureCatched();
        if (args.length == 0) {
            ControlPanel.main(new String[] {});
        } else {
            CommandLine cli = new CommandLine();
            int result = cli.handle(args);

            // instead of returning, use JNLPRuntime.exit() so we can pass back
            // error codes indicating success or failure. Otherwise using
            // this program for scripting will become much more challenging
            JNLPRuntime.exit(result);
        }
    }
}
