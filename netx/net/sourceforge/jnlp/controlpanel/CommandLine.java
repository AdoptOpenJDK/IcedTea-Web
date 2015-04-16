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
import java.util.List;
import java.util.Map;

import javax.naming.ConfigurationException;

import net.sourceforge.jnlp.config.ConfiguratonValidator;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.OptionsDefinitions;
import net.sourceforge.jnlp.config.Setting;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.docprovider.ItwebSettingsTextsProvider;
import net.sourceforge.jnlp.util.docprovider.TextsProvider;
import net.sourceforge.jnlp.util.docprovider.formatters.formatters.PlainTextFormatter;
import net.sourceforge.jnlp.util.logging.OutputController;
import net.sourceforge.jnlp.util.optionparser.OptionParser;

/**
 * Encapsulates a command line interface to the deployment configuration.
 * <p>
 * The central method is {@link #handle()}, which calls one of the
 * various 'handle' methods. The commands listed in OptionsDefinitions.getItwsettingsCommands
 * are supported. For each supported command, a method handleCOMMANDCommand exists.
 * This method actually takes action based on the command. Generally, a
 * printCOMMANDHelp method also exists, and prints out the help message for
 * that specific command. For example, see {@link #handleListCommand()}
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

    private OptionParser optionParser;

    DeploymentConfiguration config = null;

    /**
     * Creates a new instance
     * @param optionParser used to parse applications arguments
     */
    public CommandLine(OptionParser optionParser) {
        this.optionParser = optionParser;
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
     * @return the result of handling the help command. SUCCESS if no errors occurred.
     */
    public int handleHelpCommand() {
        final TextsProvider helpMessagesProvider = new ItwebSettingsTextsProvider("utf-8", new PlainTextFormatter(), true, true);
        String helpMessage = "\n";

        if (JNLPRuntime.isDebug()) {
            helpMessage = helpMessage
                    + helpMessagesProvider.writeToString();
        } else {
            helpMessage = helpMessage
                    + helpMessagesProvider.prepare().getSynopsis()
                    + helpMessagesProvider.getFormatter().getNewLine()
                    + helpMessagesProvider.prepare().getDescription()
                    + helpMessagesProvider.getFormatter().getNewLine()
                    + helpMessagesProvider.prepare().getCommands()
                    + helpMessagesProvider.getFormatter().getNewLine();
        }
        OutputController.getLogger().printOut(helpMessage);
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
     * @return result of handling the command. SUCCESS if no errors occurred.
     */
    public int handleListCommand() {
        if (optionParser.hasOption(OptionsDefinitions.OPTIONS.HELP2)) {
            printListHelp();
            return SUCCESS;
        }


        if (optionParser.getMainArgs().contains("details") || optionParser.getMainArgs().contains("verbose")) {
            JNLPRuntime.setDebug(true);
        }

        Map<String, Setting<String>> all = config.getRaw();
        for (String key : all.keySet()) {
            Setting<String> value = all.get(key);
            OutputController.getLogger().printOutLn(key + ": " + value.getValue());
            if (JNLPRuntime.isDebug()) {
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
     * @return an integer representing success (SUCCESS) or error handling the
     * get command.
     */
    public int handleGetCommand() {
        if (optionParser.hasOption(OptionsDefinitions.OPTIONS.HELP2)) {
            printGetHelp();
            return SUCCESS;
        }

        List<String> args = optionParser.getParams(OptionsDefinitions.OPTIONS.GET);
        Map<String, Setting<String>> all = config.getRaw();

        List<String> unknownProperties = new ArrayList<>(args);
        unknownProperties.removeAll(all.keySet());
        if (unknownProperties.size() > 0) {
            for (String property : unknownProperties) {
                OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, R("CLUnknownProperty", property));
            }
            return ERROR;
        }
        for (String key : args) {
            String value = all.get(key).getValue();
            OutputController.getLogger().printOutLn(key+": "+value);
        }
        return SUCCESS;
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
     * @return an integer indicating success (SUCCESS) or error in handling
     * the command
     */
    public int handleSetCommand() {
        if (optionParser.hasOption(OptionsDefinitions.OPTIONS.HELP2)) {
            printSetHelp();
            return SUCCESS;
        }

        List<String> args = optionParser.getParams(OptionsDefinitions.OPTIONS.SET);

        String key = null;
        String value;
        boolean isArgKey = false;

        for (String arg : args) {
            isArgKey = !isArgKey;

            if (isArgKey) {
                key = arg;
                continue;
            }

            value = arg;

            if (configContains(key)) {
                if (validateValue(key, value) == ERROR) {
                    return ERROR;
                }
                config.setProperty(key, value);
            } else {
                OutputController.getLogger().printOutLn(R("CLWarningUnknownProperty", key));
                config.setProperty(key, value);
            }
        }

        try {
            config.save();
        } catch (IOException e) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
            return ERROR;
        }

        return SUCCESS;
    }

    private boolean configContains(final String arg) {
        return config.getRaw().containsKey(arg);
    }

    private int validateValue(final String key, final String value) {
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
     * @return an integer indicating success (SUCCESS) or error in handling
     * the command
     */
    public int handleResetCommand() {
        if (optionParser.hasOption(OptionsDefinitions.OPTIONS.HELP2)) {
            printResetHelp();
            return SUCCESS;
        }

        List<String> args = optionParser.getParams(OptionsDefinitions.OPTIONS.RESET);

        boolean resetAll = false;
        if (args.contains("all")) {
            resetAll = true;
            if (args.size() > 1) {
                for (String arg : args) {
                    if (!arg.equals("all")) {
                        OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, R("CLUnknownCommand", arg));
                    }
                }
            }
        }

        Map<String, Setting<String>> all = config.getRaw();

        if (resetAll) {
            for (String aKey: all.keySet()) {
                Setting<String> setting = all.get(aKey);
                setting.setValue(setting.getDefaultValue());
            }
        } else {
            for (String key : args) {
                if (!all.containsKey(key)) {
                    OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, R("CLUnknownProperty", key));
                    return ERROR;
                } else {
                    Setting<String> setting = all.get(key);
                    setting.setValue(setting.getDefaultValue());
                }
            }
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
     * @return an integer indicating success (SUCCESS) or error in handling
     * the command
     */
    public int handleInfoCommand() {
        if (optionParser.hasOption(OptionsDefinitions.OPTIONS.HELP2)) {
            printInfoHelp();
            return SUCCESS;
        }

        List<String> args = optionParser.getParams(OptionsDefinitions.OPTIONS.INFO);

        Map<String, Setting<String>> all = config.getRaw();

        for (String key : args) {
            Setting<String> value = all.get(key);
            if (value == null) {
                OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, R("CLNoInfo"));
            } else {
                OutputController.getLogger().printOutLn(R("CLDescription", value.getDescription()));
                OutputController.getLogger().printOutLn(R("CLValue", value.getValue()));
                if (value.getValidator() != null) {
                    OutputController.getLogger().printOutLn("\t" + R("VVPossibleValues", value.getValidator().getPossibleValues()));
                }
                OutputController.getLogger().printOutLn(R("CLValueSource", value.getSource()));
            }
        }
        return SUCCESS;
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
     * @return an integer indicating success (SUCCESS) or error in handling
     * the command
     */
    public int handleCheckCommand() {
        if (optionParser.hasOption(OptionsDefinitions.OPTIONS.HELP2)) {
            printCheckHelp();
            return SUCCESS;
        }

        List<String> args = optionParser.getParams(OptionsDefinitions.OPTIONS.CHECK);

        if (!args.isEmpty()) {
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
     * @return an integer representing an error code or SUCCESS if no problems
     * occurred.
     */
    public int handle() {
        int val;
        if (hasUnrecognizedCommands()) {
            for (String unknown : optionParser.getMainArgs()) {
                OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, R("CLUnknownCommand", unknown));
            }
            handleHelpCommand();
            val = ERROR;
        } else if (getNumberOfOptions() > 1) {
            OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, R("CLUnexpectedNumberOfCommands"));
            val = handleHelpCommand();
        } else if (optionParser.hasOption(OptionsDefinitions.OPTIONS.LIST)) {
            val = handleListCommand();
        } else if (optionParser.hasOption(OptionsDefinitions.OPTIONS.SET)) {
            val = handleSetCommand();
        } else if (optionParser.hasOption(OptionsDefinitions.OPTIONS.RESET)) {
            val = handleResetCommand();
        } else if (optionParser.hasOption(OptionsDefinitions.OPTIONS.GET)) {
            val = handleGetCommand();
        } else if (optionParser.hasOption(OptionsDefinitions.OPTIONS.INFO)) {
            val = handleInfoCommand();
        } else if (optionParser.hasOption(OptionsDefinitions.OPTIONS.CHECK)) {
            val = handleCheckCommand();
        } else if (optionParser.hasOption(OptionsDefinitions.OPTIONS.HELP2)) {
            val = handleHelpCommand();
        } else {
            handleHelpCommand();
            val = ERROR;
        }
        return val;
    }

    private boolean hasUnrecognizedCommands() {
        int size = optionParser.getMainArgs().size();
        return !(isDetailsValid()) && size > 0;
    }

    private boolean isDetailsValid() {
        int size = optionParser.getMainArgs().size();
        return (optionParser.getMainArgs().contains("details") && size == 1
                && optionParser.hasOption(OptionsDefinitions.OPTIONS.LIST) && getNumberOfOptions() == 1);
    }

    private int getNumberOfOptions() {
        int number = optionParser.getNumberOfOptions();
        if (optionParser.hasOption(OptionsDefinitions.OPTIONS.HELP2)) {
            number--;
        }
        if (optionParser.hasOption(OptionsDefinitions.OPTIONS.VERBOSE)) {
            number--;
        }
        if (optionParser.hasOption(OptionsDefinitions.OPTIONS.HEADLESS)) {
            number--;
        }
        return number;
    }

    /**
     * The starting point of the program
     * @param args the command line arguments to this program
     * @throws java.lang.Exception when it goes wrong
     */
    public static void main(String[] args) throws Exception {
        try {
            OptionParser optionParser = new OptionParser(args, OptionsDefinitions.getItwsettingsCommands());
            if (optionParser.hasOption(OptionsDefinitions.OPTIONS.DETAILS) || optionParser.hasOption(OptionsDefinitions.OPTIONS.VERBOSE)){
                JNLPRuntime.setDebug(true);
            }
            if (optionParser.hasOption(OptionsDefinitions.OPTIONS.HEADLESS)) {
                JNLPRuntime.setHeadless(true);
            }
            DeploymentConfiguration.move14AndOlderFilesTo15StructureCatched();
            if (args.length == 0) {
                ControlPanel.main(new String[] {});
            } else {
                CommandLine cli = new CommandLine(optionParser);
                int result = cli.handle();

                // instead of returning, use JNLPRuntime.exit() so we can pass back
                // error codes indicating success or failure. Otherwise using
                // this program for scripting will become much more challenging
                JNLPRuntime.exit(result);
            }
        } catch (Exception e) {
            OutputController.getLogger().log(OutputController.Level.WARNING_ALL, e);
            JNLPRuntime.exit(1);
        }
    }
}
