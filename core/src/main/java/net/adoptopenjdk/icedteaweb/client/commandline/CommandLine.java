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

package net.adoptopenjdk.icedteaweb.client.commandline;

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.client.controlpanel.ControlPanel;
import net.adoptopenjdk.icedteaweb.commandline.CommandLineOptions;
import net.adoptopenjdk.icedteaweb.commandline.CommandLineOptionsDefinition;
import net.adoptopenjdk.icedteaweb.commandline.CommandLineOptionsParser;
import net.adoptopenjdk.icedteaweb.config.validators.ConfigurationValidator;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.ui.swing.SwingUtils;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.config.Setting;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.docprovider.ItwebSettingsTextsProvider;
import net.sourceforge.jnlp.util.docprovider.TextsProvider;
import net.sourceforge.jnlp.util.docprovider.formatters.formatters.PlainTextFormatter;
import net.sourceforge.jnlp.util.logging.OutputController;

import javax.naming.ConfigurationException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.ITW_BIN_NAME;
import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;

/**
 * Encapsulates a command line interface to the deployment configuration.
 * <p>
 * The central method is {@link #handle()}, which calls one of the
 * various 'handle' methods. The commands listed in CommandLineOptions.getItwsettingsCommands
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

    private final static Logger LOG = LoggerFactory.getLogger(CommandLine.class);

    public static final int ERROR = 1;
    public static final int SUCCESS = 0;

    private final String programName;

    private final CommandLineOptionsParser optionParser;

    private DeploymentConfiguration config;

    /**
     * Creates a new instance
     * @param optionParser used to parse applications arguments
     */
    public CommandLine(CommandLineOptionsParser optionParser) {
        this.optionParser = optionParser;
        programName = System.getProperty(ITW_BIN_NAME);

        config = new DeploymentConfiguration();
        try {
            config.load(false);
        } catch (ConfigurationException | MalformedURLException e) {
            LOG.error("a fatal error has occurred while loading configuration. Perhaps a global configuration was required but could not be found", e);
        }
    }

    /**
     * Handle the 'help' command
     *
     * @return the result of handling the help command. SUCCESS if no errors occurred.
     */
    private int handleHelpCommand() {
        final TextsProvider helpMessagesProvider = new ItwebSettingsTextsProvider(UTF_8, new PlainTextFormatter(), true, true);
        String helpMessage = PlainTextFormatter.getLineSeparator();

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
    private void printListHelp() {
        OutputController.getLogger().printOutLn(R("Usage"));
        OutputController.getLogger().printOutLn("  " + programName + " list [--details]");
        OutputController.getLogger().printOutLn(R("CLListDescription"));
    }

    /**
     * Handles the 'list' command
     *
     * @return result of handling the command. SUCCESS if no errors occurred.
     */
    private int handleListCommand() {
        if (optionParser.hasOption(CommandLineOptions.HELP2)) {
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
    private void printGetHelp() {
        OutputController.getLogger().printOutLn(R("Usage"));
        OutputController.getLogger().printOutLn("  " + programName + " get property-name");
        OutputController.getLogger().printOutLn(R("CLGetDescription"));
    }

    /**
     * Handles the 'get' command.
     *
     * @return an integer representing success (SUCCESS) or error handling the
     * get command.
     */
    private int handleGetCommand() {
        if (optionParser.hasOption(CommandLineOptions.HELP2)) {
            printGetHelp();
            return SUCCESS;
        }

        List<String> args = optionParser.getParams(CommandLineOptions.GET);
        Map<String, Setting<String>> all = config.getRaw();

        List<String> unknownProperties = new ArrayList<>(args);
        unknownProperties.removeAll(all.keySet());
        if (unknownProperties.size() > 0) {
            for (String property : unknownProperties) {
                LOG.info("Unknown property-name {}", property);
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
    private void printSetHelp() {
        OutputController.getLogger().printOutLn(R("Usage"));
        OutputController.getLogger().printOutLn("  " + programName + " set property-name value");
        OutputController.getLogger().printOutLn(R("CLSetDescription"));
    }

    /**
     * Handles the 'set' command
     *
     * @return an integer indicating success (SUCCESS) or error in handling
     * the command
     */
    public int handleSetCommand() {
        if (optionParser.hasOption(CommandLineOptions.HELP2)) {
            printSetHelp();
            return SUCCESS;
        }

        List<String> args = optionParser.getParams(CommandLineOptions.SET);

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
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE,  e);
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
                LOG.error("Property " + old.getName() + " has incorrect value " + value + ". Possible values: " + old.getValidator().getPossibleValues(), e);
                return ERROR;
            }
        }
        return SUCCESS;
    }

    /**
     * Prints a help message for the reset command
     */
    private void printResetHelp() {
        OutputController.getLogger().printOutLn(R("Usage"));
        OutputController.getLogger().printOutLn("  " + programName + " reset [all|property-name]");
        OutputController.getLogger().printOutLn(R("CLResetDescription"));
    }

    /**
     * Handles the 'reset' command
     *
     * @return an integer indicating success (SUCCESS) or error in handling
     * the command
     */
    private int handleResetCommand() {
        if (optionParser.hasOption(CommandLineOptions.HELP2)) {
            printResetHelp();
            return SUCCESS;
        }

        List<String> args = optionParser.getParams(CommandLineOptions.RESET);

        boolean resetAll = false;
        if (args.contains("all")) {
            resetAll = true;
            if (args.size() > 1) {
                for (String arg : args) {
                    if (!arg.equals("all")) {
                        LOG.info("Unknown command {}", arg);
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
                    LOG.info("Unknown property-name {}", key);
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
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
            return ERROR;
        }

        return SUCCESS;
    }

    /**
     * Print a help message for the 'info' command
     */
    private void printInfoHelp() {
        OutputController.getLogger().printOutLn(R("Usage"));
        OutputController.getLogger().printOutLn("  " + programName + " info property-name");
        OutputController.getLogger().printOutLn(R("CLInfoDescription"));
    }

    /**
     * Handles the 'info' command
     *
     * @return an integer indicating success (SUCCESS) or error in handling
     * the command
     */
    private int handleInfoCommand() {
        if (optionParser.hasOption(CommandLineOptions.HELP2)) {
            printInfoHelp();
            return SUCCESS;
        }

        List<String> args = optionParser.getParams(CommandLineOptions.INFO);

        Map<String, Setting<String>> all = config.getRaw();

        for (String key : args) {
            Setting<String> value = all.get(key);
            if (value == null) {
                LOG.info("No information available (is this a valid option?).");
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
    private void printCheckHelp() {
        OutputController.getLogger().printOutLn(R("Usage"));
        OutputController.getLogger().printOutLn("  " + programName + " check");
        OutputController.getLogger().printOutLn(R("CLCheckDescription"));
    }

    /**
     * Handles the 'check' command
     *
     * @return an integer indicating success (SUCCESS) or error in handling
     * the command
     */
    private int handleCheckCommand() {
        if (optionParser.hasOption(CommandLineOptions.HELP2)) {
            printCheckHelp();
            return SUCCESS;
        }

        List<String> args = optionParser.getParams(CommandLineOptions.CHECK);

        if (!args.isEmpty()) {
            printCheckHelp();
            return ERROR;
        }

        Map<String, Setting<String>> all = config.getRaw();

        ConfigurationValidator validator = new ConfigurationValidator(all);
        validator.validate();

        boolean allValid = true;
        for (Setting<String> setting : validator.getIncorrectSetting()) {
            LOG.info("Property {} has incorrect value {}. Possible values {}.", setting.getName(), setting.getValue(), setting.getValidator().getPossibleValues());
            allValid = false;
        }

        for (Setting<String> setting : validator.getUnrecognizedSetting()) {
            LOG.info("Unknown property-name {}", setting.getName());
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
     * into two pieces: the first element is assumed to be the command, and
     * everything after is taken to be the argument to the command.
     *
     * @return an integer representing an error code or SUCCESS if no problems
     * occurred.
     */
    public int handle() {
        int val;
        if (hasUnrecognizedCommands()) {
            for (String unknown : optionParser.getMainArgs()) {
                LOG.info("Unknown command {}", unknown);
            }
            handleHelpCommand();
            val = ERROR;
        } else if (getNumberOfOptions() > 1) {
            LOG.info("Itweb-settings can only run one command at a time.");
            val = handleHelpCommand();
        } else if (optionParser.hasOption(CommandLineOptions.LIST)) {
            val = handleListCommand();
        } else if (optionParser.hasOption(CommandLineOptions.SET)) {
            val = handleSetCommand();
        } else if (optionParser.hasOption(CommandLineOptions.RESET)) {
            val = handleResetCommand();
        } else if (optionParser.hasOption(CommandLineOptions.GET)) {
            val = handleGetCommand();
        } else if (optionParser.hasOption(CommandLineOptions.INFO)) {
            val = handleInfoCommand();
        } else if (optionParser.hasOption(CommandLineOptions.CHECK)) {
            val = handleCheckCommand();
        } else if (optionParser.hasOption(CommandLineOptions.HELP2)) {
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
                && optionParser.hasOption(CommandLineOptions.LIST) && getNumberOfOptions() == 1);
    }

    private int getNumberOfOptions() {
        int number = optionParser.getNumberOfOptions();
        if (optionParser.hasOption(CommandLineOptions.HELP2)) {
            number--;
        }
        if (optionParser.hasOption(CommandLineOptions.VERBOSE)) {
            number--;
        }
        if (optionParser.hasOption(CommandLineOptions.HEADLESS)) {
            number--;
        }
        return number;
    }

    /**
     * The starting point of the program
     * @param args the command line arguments to this program
     */
    public static void main(String[] args) {
        // setup Swing EDT tracing:
        SwingUtils.setup();

        try {
            CommandLineOptionsParser optionParser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getItwsettingsCommands());
            if (optionParser.hasOption(CommandLineOptions.DETAILS) || optionParser.hasOption(CommandLineOptions.VERBOSE)){
                JNLPRuntime.setDebug(true);
            }
            if (optionParser.hasOption(CommandLineOptions.HEADLESS)) {
                JNLPRuntime.setHeadless(true);
            }
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
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
            JNLPRuntime.exit(1);
        }
    }
}
