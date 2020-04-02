// Copyright (C) 2001-2003 Jon A. Maxwell (JAM)
// Copyright (C) 2019 Karakun AG
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
package net.sourceforge.jnlp.runtime;

import net.adoptopenjdk.icedteaweb.client.certificateviewer.CertificateViewer;
import net.adoptopenjdk.icedteaweb.client.parts.about.AboutDialog;
import net.adoptopenjdk.icedteaweb.client.parts.browser.LinkingBrowser;
import net.adoptopenjdk.icedteaweb.commandline.CommandLineOptions;
import net.adoptopenjdk.icedteaweb.commandline.CommandLineOptionsDefinition;
import net.adoptopenjdk.icedteaweb.commandline.CommandLineOptionsParser;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.PropertyDesc;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.resources.UpdatePolicy;
import net.adoptopenjdk.icedteaweb.resources.cache.Cache;
import net.adoptopenjdk.icedteaweb.ui.swing.SwingUtils;
import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.Launcher;
import net.sourceforge.jnlp.ParserSettings;
import net.sourceforge.jnlp.cache.CacheUtil;
import net.sourceforge.jnlp.services.ServiceUtil;
import net.sourceforge.jnlp.util.docprovider.IcedTeaWebTextsProvider;
import net.sourceforge.jnlp.util.docprovider.JavaWsTextsProvider;
import net.sourceforge.jnlp.util.docprovider.TextsProvider;
import net.sourceforge.jnlp.util.docprovider.formatters.formatters.PlainTextFormatter;
import net.sourceforge.jnlp.util.logging.OutputController;
import sun.awt.AppContext;
import sun.awt.SunToolkit;

import javax.jnlp.BasicService;
import javax.swing.UIManager;
import java.io.File;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.isNull;
import static net.adoptopenjdk.icedteaweb.IcedTeaWebConstants.JAVAWS;
import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;
import static net.sourceforge.jnlp.runtime.ForkingStrategy.NEVER;

/**
 * This is the main entry point for the JNLP client. The main method parses the
 * command line parameters and loads a JNLP file into the secure runtime
 * environment. This class is meant to be called from the command line or file
 * association; to initialize the netx engine from other code invoke the
 * {@link JNLPRuntime#initialize} method after configuring the runtime.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell
 * (JAM)</a> - initial author
 * @version $Revision: 1.21 $
 */
public final class Boot implements PrivilegedAction<Integer> {

    private static final Logger LOG = LoggerFactory.getLogger(Boot.class);

    // todo: decide whether a spawned netx (external launch)
    // should inherit the same options as this instance (store argv?)
    private static final String name = Boot.class.getPackage().getImplementationTitle();
    public static final String version = Boot.class.getPackage().getImplementationVersion();

    private static final String nameAndVersion = name + " " + version;

    private static final String miniLicense = "\n"
            + "   netx - an open-source JNLP client.\n"
            + "   Copyright (C) 2001-2003 Jon A. Maxwell (JAM)\n"
            + "\n"
            + "   // This library is free software; you can redistribute it and/or\n"
            + "   modify it under the terms of the GNU Lesser General Public\n"
            + "   License as published by the Free Software Foundation; either\n"
            + "   version 2.1 of the License, or (at your option) any later version.\n"
            + "\n"
            + "   This library is distributed in the hope that it will be useful,\n"
            + "   but WITHOUT ANY WARRANTY; without even the implied warranty of\n"
            + "   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU\n"
            + "   Lesser General Public License for more details.\n"
            + "\n"
            + "   You should have received a copy of the GNU Lesser General Public\n"
            + "   License along with this library; if not, write to the Free Software\n"
            + "   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.\n"
            + "\n";

    private static CommandLineOptionsParser optionParser;

    /**
     * Launch the JNLP file specified by the command-line arguments.
     *
     * @param args launching arguments
     */
    public static void main(String[] args) {
        int status = -42;
        try {
            EnvironmentPrinter.logEnvironment(args);
            status = mainWithReturnCode(args);
        } finally {
            if (status != 0) {
                System.exit(status);
            }
        }
    }

    /**
     * Launch the JNLP file specified by the command-line arguments.
     *
     * This method is only public for integration testing. You should not call this method !!
     *
     * @param args launching arguments
     * @return the return code. 0 = SUCCESS anything else is an error
     */
    public static int mainWithReturnCode(String[] args) {
        try {
            final Integer result = runMain(args);
            LOG.debug("Exiting Boot.mainWithReturnCode() with {}", result);
            return result;
        } catch (RuntimeException | Error e) {
            LOG.debug("Exiting Boot.mainWithReturnCode() exceptionally", e);
            throw e;
        } finally {
            JNLPRuntime.closeLoggerAndWaitForExceptionDialogsToBeClosed();
        }
    }

    private static Integer runMain(String[] args) {
        // setup Swing EDT tracing:
        SwingUtils.setup();

        JNLPRuntime.getExtensionPoint().getTranslationResources().forEach(Translator::addBundle);

        optionParser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getJavaWsOptions());

        if (optionParser.hasOption(CommandLineOptions.VERBOSE)) {
            JNLPRuntime.setDebug(true);
        }

        if (AppContext.getAppContext() == null) {
            SunToolkit.createNewAppContext();
        }

        if (optionParser.hasOption(CommandLineOptions.HEADLESS)) {
            JNLPRuntime.setHeadless(true);
        }

        if (optionParser.hasOption(CommandLineOptions.VIEWER)) {
            try {
                CertificateViewer.main(null);
                return 0;
            } catch (Exception e) {
                LOG.error("Exception while displaying the Certificate Viewer", e);
                return 1;
            }
        }

        if (optionParser.hasOption(CommandLineOptions.VERSION)) {
            OutputController.getLogger().printOutLn(nameAndVersion);
            return 0;
        }

        if (optionParser.hasOption(CommandLineOptions.LICENSE)) {
            OutputController.getLogger().printOutLn(miniLicense);
            return 0;
        }

        if (optionParser.hasOption(CommandLineOptions.HELP1)) {
            printHelpMessage();
            return 0;
        }

        final List<String> properties = optionParser.getParams(CommandLineOptions.PROPERTY);
        if (properties != null) {
            for (String prop : properties) {
                try {
                    PropertyDesc propDesc = PropertyDesc.fromString(prop);
                    JNLPRuntime.getConfiguration().setProperty(propDesc.getKey(), propDesc.getValue());
                } catch (LaunchException ex) {
                    LOG.error(ex.getMessage());
                }
            }
        }

        if (optionParser.hasOption(CommandLineOptions.ABOUT)) {
            handleAbout();
            if (!JNLPRuntime.isHeadless()) {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    LOG.error("Unable to set system look and feel", e);
                }
                OutputController.getLogger().printOutLn(R("BLaunchAbout"));
                AboutDialog.display(JAVAWS);
            }
            return 0;
        }

        if (optionParser.hasOption(CommandLineOptions.UPDATE)) {
            int value = Integer.parseInt(optionParser.getParam(CommandLineOptions.UPDATE));
            JNLPRuntime.setDefaultUpdatePolicy(new UpdatePolicy(value * 1000L));
        }
        if (optionParser.hasOption(CommandLineOptions.NOUPDATE)) {
            JNLPRuntime.setDefaultUpdatePolicy(UpdatePolicy.NEVER);
        }
        if (optionParser.hasOption(CommandLineOptions.NOFORK)) {
            JNLPRuntime.setForkingStrategy(NEVER);
        }
        if (optionParser.hasOption(CommandLineOptions.TRUSTALL)) {
            JNLPRuntime.setTrustAll(true);
        }
        if (optionParser.hasOption(CommandLineOptions.TRUSTNONE)) {
            JNLPRuntime.setTrustNone(true);
        }
        if (optionParser.hasOption(CommandLineOptions.NOHEADERS)) {
            JNLPRuntime.setIgnoreHeaders(true);
        }
        if (optionParser.hasOption(CommandLineOptions.REDIRECT)) {
            JNLPRuntime.setAllowRedirect(true);
        }

        if (optionParser.hasOption(CommandLineOptions.NOSPLASH)) {
            JNLPRuntime.setShowWebSplash(false);
        }

        //if it is browser go by ots own, otherwise proceed with normal ITW logic
        if (optionParser.hasOption(CommandLineOptions.BROWSER)) {
            String url = optionParser.getParam(CommandLineOptions.BROWSER);
            LinkingBrowser.showStandAloneWindow(url, false);
            return 0;
        }

        JNLPRuntime.setInitialArguments(Arrays.asList(args));
        JNLPRuntime.setJnlpPath(getJnlpFileLocationFromCommandLineArguments(optionParser));

        if (optionParser.hasOption(CommandLineOptions.NOSEC)) {
            JNLPRuntime.setSecurityEnabled(false);
        }
        if (optionParser.hasOption(CommandLineOptions.OFFLINE)) {
            JNLPRuntime.setOfflineForced(true);
        }

        JNLPRuntime.initialize(true);

        if (optionParser.hasOption(CommandLineOptions.LISTCACHEIDS)) {
            List<String> optionArgs = optionParser.getMainArgs();
            final String arg = optionArgs.size() > 0 ? optionArgs.get(0) : ".*";
            CacheUtil.logCacheIds(arg);
            return 0;
        }

        if (optionParser.hasOption(CommandLineOptions.CLEARCACHE)) {
            List<String> optionArgs = optionParser.getMainArgs();
            if (optionArgs.size() > 0) {
                //clear one app
                Cache.deleteFromCache(optionArgs.get(0));
            } else {
                // clear all cache
                Cache.clearCache();
            }
            return 0;
        }

        return AccessController.doPrivileged(new Boot());
    }

    private static void printHelpMessage() {
        final TextsProvider helpMessagesProvider = new JavaWsTextsProvider(UTF_8, new PlainTextFormatter(), true, true);

        String helpMessage = "\n";
        if (JNLPRuntime.isDebug()) {
            helpMessage += helpMessagesProvider.writeToString();
        } else {
            helpMessage = helpMessage
                    + helpMessagesProvider.prepare().getSynopsis()
                    + helpMessagesProvider.getFormatter().getNewLine()
                    + helpMessagesProvider.prepare().getOptions()
                    + helpMessagesProvider.getFormatter().getNewLine();
        }

        OutputController.getLogger().printOut(helpMessage);
    }

    private static void handleAbout() {
        final TextsProvider aboutMessagesProvider = new IcedTeaWebTextsProvider(UTF_8, new PlainTextFormatter(), false, true);
        String itwInfoMessage = ""
                + nameAndVersion
                + "\n\n";

        if (JNLPRuntime.isDebug()) {
            itwInfoMessage += aboutMessagesProvider.writeToString();
        } else {
            itwInfoMessage = itwInfoMessage
                    + aboutMessagesProvider.prepare().getIntroduction();
        }
        OutputController.getLogger().printOut(itwInfoMessage);
    }

    static String fixJnlpProtocol(String param) {
        if (isNull(param) || param.isEmpty()) {
            return null;
        }
        //remove jnlp: for case like jnlp:https://some.app/file.jnlp
        if (param.matches("^jnlp[s]?:.*://.*")) {
            param = param.replaceFirst("^jnlp[s]?:", "");
        }
        //translate jnlp://some.app/file.jnlp to http/https
        return param.replaceFirst("^jnlp:", "http:").replaceFirst("^jnlps:", "https:");
    }


    /**
     * The privileged part (jdk1.3 compatibility).
     */
    @Override
    public Integer run() {
        try {
            final String location = getJnlpFileLocationFromCommandLineArguments(optionParser);
            if (location != null) {
                launch(location);
                return 0;
            } else {
                printHelpMessage();
                return 1;
            }
        } catch (LaunchException e) {
            LOG.error("failed to launch", e);
            return 1;
        } catch (Exception e) {
            LOG.error("Unexpected exception", e);
            return 1;
        }
    }

    private void launch(String location) throws LaunchException {
        LOG.info("Proceeding with jnlp");
        Launcher launcher = new Launcher();
        launcher.setParserSettings(getParserSettings());
        launcher.setInformationToMerge(getExtras());
        launcher.launch(locationToUrl(location));
    }

    private Map<String, List<String>> getExtras() {
        final Map<String, List<String>> extra = new HashMap<>();
        extra.put("arguments", optionParser.getParams(CommandLineOptions.ARG));
        extra.put("parameters", optionParser.getParams(CommandLineOptions.PARAM));
        extra.put("properties", optionParser.getParams(CommandLineOptions.PROPERTY));
        return extra;
    }

    private ParserSettings getParserSettings() {
        final boolean strict = optionParser.hasOption(CommandLineOptions.STRICT);
        final boolean strictXml = optionParser.hasOption(CommandLineOptions.XML);
        return new ParserSettings(strict, true, !strictXml);
    }

    private URL locationToUrl(String location) throws LaunchException {
        LOG.info("JNLP file location: {}", location);
        try {
            if (new File(location).exists()) {
                return new File(location).toURI().toURL(); // Why use file.getCanonicalFile?
            } else {
                final BasicService basicService = ServiceUtil.getBasicService();
                if (basicService != null) {
                    return new URL(basicService.getCodeBase(), location);
                } else {
                    LOG.warn("Warning, null basicService");
                    return new URL(location);
                }
            }
        } catch (Exception e) {
            throw new LaunchException("Invalid jnlp file " + location, e);
        }
    }

    /**
     * Fetch the JNLP file location from command line arguments. If the {@link CommandLineOptionsParser} cannot parse
     * a {@link CommandLineOptions#JNLP} it returns the first command argument.
     *
     * @return the file location or null if no file location can be found in the command line arguments.
     */
    static String getJnlpFileLocationFromCommandLineArguments(final CommandLineOptionsParser commandLineOptionsParser) {
        if (commandLineOptionsParser.hasOption(CommandLineOptions.JNLP)) {
            return fixJnlpProtocol(commandLineOptionsParser.getParam(CommandLineOptions.JNLP));
        } else if (commandLineOptionsParser.mainArgExists()) {
            // so file location must be in the list of arguments, take the first one as best effort, ignore the others
            return fixJnlpProtocol(commandLineOptionsParser.getMainArg());
        }
        // no file location available as argument
        return null;
    }
}
