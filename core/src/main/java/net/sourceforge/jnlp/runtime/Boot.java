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

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.client.certificateviewer.CertificateViewer;
import net.adoptopenjdk.icedteaweb.client.parts.about.AboutDialog;
import net.adoptopenjdk.icedteaweb.client.parts.browser.LinkingBrowser;
import net.adoptopenjdk.icedteaweb.commandline.CommandLineOptions;
import net.adoptopenjdk.icedteaweb.commandline.CommandLineOptionsDefinition;
import net.adoptopenjdk.icedteaweb.commandline.CommandLineOptionsParser;
import net.adoptopenjdk.icedteaweb.commandline.UnevenParameterException;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.PropertyDesc;
import net.adoptopenjdk.icedteaweb.launch.JvmLauncher;
import net.adoptopenjdk.icedteaweb.launch.JvmLauncherHolder;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.ui.swing.SwingUtils;
import net.sourceforge.jnlp.ItwJvmLauncher;
import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.Launcher;
import net.sourceforge.jnlp.ParserSettings;
import net.sourceforge.jnlp.cache.CacheUtil;
import net.sourceforge.jnlp.cache.UpdatePolicy;
import net.sourceforge.jnlp.services.ServiceUtil;
import net.sourceforge.jnlp.util.docprovider.IcedTeaWebTextsProvider;
import net.sourceforge.jnlp.util.docprovider.JavaWsTextsProvider;
import net.sourceforge.jnlp.util.docprovider.TextsProvider;
import net.sourceforge.jnlp.util.docprovider.formatters.formatters.PlainTextFormatter;
import net.sourceforge.jnlp.util.logging.OutputController;
import sun.awt.AppContext;
import sun.awt.SunToolkit;

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
import static java.util.Objects.requireNonNull;
import static net.adoptopenjdk.icedteaweb.IcedTeaWebConstants.JAVAWS;
import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;

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
public final class Boot implements PrivilegedAction<Void> {

    private final static Logger LOG = LoggerFactory.getLogger(Boot.class);

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

    public static CommandLineOptionsParser getOptionParser() {
        return optionParser;
    }

    /**
     * Launch the JNLP file specified by the command-line arguments.
     *
     * @param args launching arguments
     */
    public static void main(String[] args) throws UnevenParameterException {
        main(new ItwJvmLauncher(), args);
    }

    /**
     * Launch the JNLP file specified by the command-line arguments with the given JVM launch.
     *
     * @param launcher the JVM launcher
     * @param args launching arguments
     */
    public static void main(final JvmLauncher launcher, final String[] args) {
        JvmLauncherHolder.setLauncher(requireNonNull(launcher));

        // setup Swing EDT tracing:
        SwingUtils.setup();

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
            } catch (Exception e) {
                LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
            } finally {
                //no matter what happens, terminate
                return;
            }
        }

        if (optionParser.hasOption(CommandLineOptions.VERSION)) {
            OutputController.getLogger().printOutLn(nameAndVersion);
            JNLPRuntime.exit(0);
        }

        if (optionParser.hasOption(CommandLineOptions.LICENSE)) {
            OutputController.getLogger().printOutLn(miniLicense);
            JNLPRuntime.exit(0);
        }

        if (optionParser.hasOption(CommandLineOptions.HELP1)) {
            handleMessage();
            JNLPRuntime.exit(0);
        }
        List<String> properties = optionParser.getParams(CommandLineOptions.PROPERTY);
        if (properties != null) {
            for (String prop : properties) {
                try {
                    PropertyDesc propDesc = PropertyDesc.fromString(prop);
                    JNLPRuntime.getConfiguration().setProperty(propDesc.getKey(), propDesc.getValue());
                } catch (LaunchException ex) {
                    LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
                }
            }
        }

        if (optionParser.hasOption(CommandLineOptions.ABOUT)) {
            handleAbout();
            if (JNLPRuntime.isHeadless()) {
                JNLPRuntime.exit(0);
            } else {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    LOG.error("Unable to set system look and feel", e);
                }
                OutputController.getLogger().printOutLn(R("BLaunchAbout"));
                AboutDialog.display(JAVAWS);
                return;
            }
        }

        if (optionParser.hasOption(CommandLineOptions.UPDATE)) {
            int value = Integer.parseInt(optionParser.getParam(CommandLineOptions.UPDATE));
            JNLPRuntime.setDefaultUpdatePolicy(new UpdatePolicy(value * 1000L));
        }
        if (optionParser.hasOption(CommandLineOptions.NOUPDATE)) {
            JNLPRuntime.setDefaultUpdatePolicy(UpdatePolicy.NEVER);
        }
        if (optionParser.hasOption(CommandLineOptions.NOFORK)) {
            JNLPRuntime.setForksAllowed(false);
        }
        if (optionParser.hasOption(CommandLineOptions.TRUSTALL)) {
            JNLPRuntime.setTrustAll(true);
        }
        if (optionParser.hasOption(CommandLineOptions.HTML)) {
            JNLPRuntime.setHtml(true);
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

        //if it is browser go by ots own, otherwise proceed with normal ITW logic
        if (optionParser.hasOption(CommandLineOptions.BROWSER)) {
            String url = optionParser.getParam(CommandLineOptions.BROWSER);
            LinkingBrowser.showStandAloneWindow(url, false);
        } else {

            JNLPRuntime.setInitialArguments(Arrays.asList(args));

            AccessController.doPrivileged(new Boot());
        }
    }

    private static void handleMessage() {
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
    public Void run() {

        Map<String, List<String>> extra = new HashMap<>();

        if (optionParser.hasOption(CommandLineOptions.HTML)) {
            new HtmlBoot(optionParser).run(extra);
        } else {
            final ParserSettings settings = init(extra);
            if (settings != null) {
                try {
                    LOG.info("Proceeding with jnlp");
                    Launcher launcher = new Launcher(true);
                    launcher.setParserSettings(settings);
                    launcher.setInformationToMerge(extra);
                    launcher.launch(Boot.getFileLocation());
                } catch (LaunchException ex) {
                    // default handler prints this
                    JNLPRuntime.exit(1);
                } catch (Exception ex) {
                    LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
                    Boot.fatalError("Unexpected" + ex.toString() + " at " + ex.getStackTrace()[0]);
                }
            }
        }
        return null;
    }

    static void fatalError(String message) {
        LOG.error("netx: " + message);
        JNLPRuntime.exit(1);
    }

    /**
     * Returns the url of file to open; does not return if no file was
     * specified, or if the file location was invalid.
     */
    static URL getFileLocation() {

        final String location = getJnlpFileLocationFromCommandLineArguments(optionParser);

        if (location == null) {
            handleMessage();
            JNLPRuntime.exit(1);
        }

        LOG.info("JNLP file location: {}", location);

        URL url = null;

        try {
            if (new File(location).exists()) // TODO: Should be toURI().toURL()
            {
                url = new File(location).toURL(); // Why use file.getCanonicalFile?
            } else if (ServiceUtil.getBasicService() != null) {
                LOG.warn("Warning, null basicService");
                url = new URL(ServiceUtil.getBasicService().getCodeBase(), location);
            } else {
                url = new URL(location);
            }
        } catch (Exception e) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
            fatalError("Invalid jnlp file " + location);
        }

        return url;
    }

    /**
     * Fetch the JNLP file location from command line arguments. If the {@link CommandLineOptionsParser} cannot parse
     * a {@link CommandLineOptions#JNLP} or a a {@link CommandLineOptions#HTML} it returns the first command argument.
     *
     * @return the file location or null if no file location can be found in the command line arguments.
     */
    static String getJnlpFileLocationFromCommandLineArguments(final CommandLineOptionsParser commandLineOptionsParser) {
        if (commandLineOptionsParser.hasOption(CommandLineOptions.JNLP)) {
            return fixJnlpProtocol(commandLineOptionsParser.getParam(CommandLineOptions.JNLP));
        }
        else if (commandLineOptionsParser.hasOption(CommandLineOptions.HTML)) {
            return commandLineOptionsParser.getParam(CommandLineOptions.HTML);
        }
        else if (commandLineOptionsParser.mainArgExists()) {
            // so file location must be in the list of arguments, take the first one as best effort, ignore the others
            return fixJnlpProtocol(commandLineOptionsParser.getMainArg());
        }
        // no file location available as argument
        return null;
    }

    static ParserSettings init(Map<String, List<String>> extra) {
        JNLPRuntime.setSecurityEnabled(!optionParser.hasOption(CommandLineOptions.NOSEC));
        JNLPRuntime.setOfflineForced(optionParser.hasOption(CommandLineOptions.OFFLINE));
        JNLPRuntime.initialize(true);

        if (optionParser.hasOption(CommandLineOptions.LISTCACHEIDS)) {
            List<String> optionArgs = optionParser.getMainArgs();
            if (optionArgs.size() > 0) {
                //clear one app
                CacheUtil.listCacheIds(optionArgs.get(0), true, true);
            } else {
                // clear all cache
                CacheUtil.listCacheIds(".*", true, true);
            }
            return null;
        }

        /*
         * FIXME
         * This should have been done with the rest of the argument parsing
         * code. But we need to know what the cache and base directories are,
         * and baseDir is initialized here
         */
        if (optionParser.hasOption(CommandLineOptions.CLEARCACHE)) {
            List<String> optionArgs = optionParser.getMainArgs();
            if (optionArgs.size() > 0) {
                //clear one app
                CacheUtil.clearCache(optionArgs.get(0), true, true);
            } else {
                // clear all cache
                CacheUtil.clearCache();
            }
            return null;
        }

        extra.put("arguments", optionParser.getParams(CommandLineOptions.ARG));
        extra.put("parameters", optionParser.getParams(CommandLineOptions.PARAM));
        extra.put("properties", optionParser.getParams(CommandLineOptions.PROPERTY));

        return ParserSettings.setGlobalParserSettingsFromOptionParser(optionParser);
    }

}
