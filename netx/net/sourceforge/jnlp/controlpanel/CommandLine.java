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
 * <p>
 * Sample usage:
 * <pre>
 * CommandLine cli = new CommandLine();
 * // the string array represents input using the command line
 * int retVal = cli.handle(new String[] { "help" });
 * if (retVal == CommandLine.SUCCESS) {
 *    // good!
 * } else {
 *    // bad!
 * }
 * </pre>
 *
 * @author Omair Majid (omajid@redhat.com)
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
            System.out.println(R("RConfigurationFatal"));
        }
    }

    /**
     * Handle the 'help' command
     *
     * @param args optional
     * @return the result of handling the help command. SUCCESS if no errors occurred.
     */
    public int handleHelpCommand(List<String> args) {
        System.out.println(R("Usage"));
        System.out.println("  " + PROGRAM_NAME + " "
                + allCommands.toString().replace(',', '|').replaceAll(" ", "") + " [help]");
        System.out.println(R("CLHelpDescription", PROGRAM_NAME));
        return SUCCESS;
    }

    /**
     * Prints help message for the list command
     */
    public void printListHelp() {
        System.out.println(R("Usage"));
        System.out.println("  " + PROGRAM_NAME + " list [--details]");
        System.out.println(R("CLListDescription"));
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
            System.out.println(key + ": " + value.getValue());
            if (verbose) {
                System.out.println("\t" + R("CLDescription", value.getDescription()));
            }
        }
        return SUCCESS;
    }

    /**
     * Prints help message for the get command
     */
    public void printGetHelp() {
        System.out.println(R("Usage"));
        System.out.println("  " + PROGRAM_NAME + " get property-name");
        System.out.println(R("CLGetDescription"));
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
            System.out.println(value);
            return SUCCESS;
        } else {
            System.out.println(R("CLUnknownProperty", key));
            return ERROR;
        }
    }

    /**
     * Prints the help message for the 'set' command
     */
    public void printSetHelp() {
        System.out.println(R("Usage"));
        System.out.println("  " + PROGRAM_NAME + " set property-name value");
        System.out.println(R("CLSetDescription"));
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
                    System.out.println(R("CLIncorrectValue", old.getName(), value, old.getValidator().getPossibleValues()));
                    return ERROR;
                }
            }
            config.setProperty(key, value);
        } else {
            System.out.println(R("CLWarningUnknownProperty", key));
            config.setProperty(key, value);
        }

        try {
            config.save();
        } catch (IOException e) {
            e.printStackTrace();
            return ERROR;
        }

        return SUCCESS;
    }

    /**
     * Prints a help message for the reset command
     */
    public void printResetHelp() {
        System.out.println(R("Usage"));
        System.out.println("  " + PROGRAM_NAME + " reset [all|property-name]");
        System.out.println(R("CLResetDescription"));
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
            System.out.println(R("CLUnknownProperty", key));
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
            e.printStackTrace();
            return ERROR;
        }

        return SUCCESS;
    }

    /**
     * Print a help message for the 'info' command
     */
    public void printInfoHelp() {
        System.out.println(R("Usage"));
        System.out.println("  " + PROGRAM_NAME + " info property-name");
        System.out.println(R("CLInfoDescription"));
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
            System.out.println(R("CLNoInfo"));
            return ERROR;
        } else {
            System.out.println(R("CLDescription", value.getDescription()));
            System.out.println(R("CLValue", value.getValue()));
            if (value.getValidator() != null) {
                System.out.println("\t" + R("VVPossibleValues", value.getValidator().getPossibleValues()));
            }
            System.out.println(R("CLValueSource", value.getSource()));
            return SUCCESS;
        }
    }

    /**
     * Prints a help message for the 'check' command
     */
    public void printCheckHelp() {
        System.out.println(R("Usage"));
        System.out.println("  " + PROGRAM_NAME + " check");
        System.out.println(R("CLCheckDescription"));
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
            System.out.println(R("CLIncorrectValue", setting.getName(), setting.getValue(), setting.getValidator().getPossibleValues()));
            allValid = false;
        }

        for (Setting<String> setting : validator.getUnrecognizedSetting()) {
            System.out.println(R("CLUnknownProperty", setting.getName()));
            allValid = false;
        }

        if (allValid) {
            System.out.println(R("CLNoIssuesFound"));
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
            System.out.println("INTERNAL ERROR: " + command + " should have been implemented");
            val = ERROR;
        } else {
            System.out.println(R("CLUnknownCommand", command));
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
        if (args.length == 0) {
            ControlPanel.main(new String[] {});
        } else {
            CommandLine cli = new CommandLine();
            int result = cli.handle(args);

            // instead of returning, use System.exit() so we can pass back
            // error codes indicating success or failure. Otherwise using
            // this program for scripting will become much more challenging
            System.exit(result);
        }
    }
}
