// Copyright (C) 2001-2003 Jon A. Maxwell (JAM)
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

import static net.sourceforge.jnlp.runtime.Translator.R;

import java.io.File;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.UIManager;

import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.Launcher;
import net.sourceforge.jnlp.OptionsDefinitions;
import net.sourceforge.jnlp.ParserSettings;
import net.sourceforge.jnlp.about.AboutDialog;
import net.sourceforge.jnlp.cache.CacheUtil;
import net.sourceforge.jnlp.cache.UpdatePolicy;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.security.viewer.CertificateViewer;
import net.sourceforge.jnlp.services.ServiceUtil;
import net.sourceforge.jnlp.util.docprovider.IcedTeaWebTextsProvider;
import net.sourceforge.jnlp.util.docprovider.JavaWsTextsProvider;
import net.sourceforge.jnlp.util.docprovider.TextsProvider;
import net.sourceforge.jnlp.util.docprovider.formatters.formatters.PlainTextFormatter;
import net.sourceforge.jnlp.util.logging.OutputController;
import net.sourceforge.jnlp.util.optionparser.InvalidArgumentException;
import net.sourceforge.jnlp.util.optionparser.OptionParser;
import sun.awt.AppContext;
import sun.awt.SunToolkit;

/**
 * This is the main entry point for the JNLP client. The main
 * method parses the command line parameters and loads a JNLP
 * file into the secure runtime environment. This class is meant
 * to be called from the command line or file association; to
 * initialize the netx engine from other code invoke the
 * {@link JNLPRuntime#initialize} method after configuring
 * the runtime.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.21 $
 */
public final class Boot implements PrivilegedAction<Void> {

    // todo: decide whether a spawned netx (external launch)
    // should inherit the same options as this instance (store argv?)

    public static final String name = Boot.class.getPackage().getImplementationTitle();
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

    private static OptionParser optionParser;

    /**
     * Launch the JNLP file specified by the command-line arguments.
     */
    public static void main(String[] argsIn) {
        optionParser = new OptionParser(argsIn, OptionsDefinitions.getJavaWsOptions());

        if (!optionParser.hasOption(OptionsDefinitions.OPTIONS.JNLP)) {
            optionParser.findMainArg();
        }

        if (optionParser.hasOption(OptionsDefinitions.OPTIONS.VERBOSE)) {
            JNLPRuntime.setDebug(true);
        }

        if (AppContext.getAppContext() == null) {
            SunToolkit.createNewAppContext();
        }
        if (optionParser.hasOption(OptionsDefinitions.OPTIONS.HEADLESS)) {
            JNLPRuntime.setHeadless(true);
        }
        
        DeploymentConfiguration.move14AndOlderFilesTo15StructureCatched();

        if (optionParser.hasOption(OptionsDefinitions.OPTIONS.VIEWER)) {
            try {
                CertificateViewer.main(null);
                JNLPRuntime.exit(0);
            } catch (Exception e) {
                OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
            }
        }
        
        if (optionParser.hasOption(OptionsDefinitions.OPTIONS.VERSION)) {
            OutputController.getLogger().printOutLn(nameAndVersion);
            JNLPRuntime.exit(0);
        }

        if (optionParser.hasOption(OptionsDefinitions.OPTIONS.LICENSE)) {
            OutputController.getLogger().printOutLn(miniLicense);
            JNLPRuntime.exit(0);
        }

        if (optionParser.hasOption(OptionsDefinitions.OPTIONS.HELP)) {
            handleMessage();
            JNLPRuntime.exit(0);
        }

        if (optionParser.hasOption(OptionsDefinitions.OPTIONS.ABOUT)) {
                handleAbout();
            if (optionParser.hasOption(OptionsDefinitions.OPTIONS.HEADLESS)) {
                JNLPRuntime.exit(0);
            } else {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    OutputController.getLogger().log("Unable to set system look and feel");
                }
                OutputController.getLogger().printOutLn(R("BLaunchAbout"));
                AboutDialog.display(TextsProvider.JAVAWS);
                return;
            }
        }


        if (optionParser.hasOption(OptionsDefinitions.OPTIONS.UPDATE)) {
            int value = Integer.parseInt(optionParser.getValue(OptionsDefinitions.OPTIONS.UPDATE));
            JNLPRuntime.setDefaultUpdatePolicy(new UpdatePolicy(value * 1000l));
        }

        if (optionParser.hasOption(OptionsDefinitions.OPTIONS.NOUPDATE))
            JNLPRuntime.setDefaultUpdatePolicy(UpdatePolicy.NEVER);

        if (optionParser.hasOption(OptionsDefinitions.OPTIONS.NOFORK)) {
            JNLPRuntime.setForksAllowed(false);
        }
        if (optionParser.hasOption(OptionsDefinitions.OPTIONS.TRUSTALL)) {
            JNLPRuntime.setTrustAll(true);
        }
        if (optionParser.hasOption(OptionsDefinitions.OPTIONS.TRUSTNONE)) {
            JNLPRuntime.setTrustNone(true);
        }
        if (optionParser.hasOption(OptionsDefinitions.OPTIONS.NOHEADERS)) {
            JNLPRuntime.setIgnoreHeaders(true);
        }
        if (optionParser.hasOption(OptionsDefinitions.OPTIONS.REDIRECT)) {
            JNLPRuntime.setAllowRedirect(true);
        }

        JNLPRuntime.setInitialArgments(Arrays.asList(argsIn));

        AccessController.doPrivileged(new Boot());

    }

    private static void handleMessage() {
        final TextsProvider helpMessagesProvider = new JavaWsTextsProvider("utf-8", new PlainTextFormatter(), true, true);

        String helpMessage = "\n";
        if (JNLPRuntime.isDebug()){
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
        final TextsProvider aboutMessagesProvider = new IcedTeaWebTextsProvider("utf-8", new PlainTextFormatter(), false, true);
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

    /**
     * The privileged part (jdk1.3 compatibility).
     */
    public Void run() {
        JNLPRuntime.setSecurityEnabled(!optionParser.hasOption(OptionsDefinitions.OPTIONS.NOSEC));
        JNLPRuntime.setOfflineForced(optionParser.hasOption(OptionsDefinitions.OPTIONS.OFFLINE));
        JNLPRuntime.initialize(true);

        /*
         * FIXME
         * This should have been done with the rest of the argument parsing
         * code. But we need to know what the cache and base directories are,
         * and baseDir is initialized here
         */
        if (optionParser.hasOption(OptionsDefinitions.OPTIONS.CLEARCACHE)) {
            CacheUtil.clearCache();
            return null;
        }

        Map<String, List<String>> extra = new HashMap<String, List<String>>();
        extra.put("arguments", optionParser.getValues(OptionsDefinitions.OPTIONS.ARG));
        extra.put("parameters", optionParser.getValues(OptionsDefinitions.OPTIONS.PARAM));
        extra.put("properties", optionParser.getValues(OptionsDefinitions.OPTIONS.PROPERTY));

        ParserSettings settings = ParserSettings.setGlobalParserSettingsFromOptionParser(optionParser);

        try {
            Launcher launcher = new Launcher(false);
            launcher.setParserSettings(settings);
            launcher.setInformationToMerge(extra);
            launcher.launch(getFileLocation());
        } catch (LaunchException ex) {
            // default handler prints this
        } catch (Exception ex) {
            OutputController.getLogger().log(ex);
            fatalError(R("RUnexpected", ex.toString(), ex.getStackTrace()[0]));
        }

        return null;
    }

    private static void fatalError(String message) {
        OutputController.getLogger().log(OutputController.Level.ERROR_ALL, "netx: " + message);
        JNLPRuntime.exit(1);
    }

    /**
     * Returns the url of file to open; does not return if no file was
     * specified, or if the file location was invalid.
     */
    private static URL getFileLocation() {

        String location = null;
        try {
            location = getJNLPFile();
        } catch (InvalidArgumentException e) {
            OutputController.getLogger().log(e);
            fatalError("Invalid argument: "+e);
        }

        if (location == null) {
            handleMessage();
            JNLPRuntime.exit(1);
        }

        OutputController.getLogger().log(R("BFileLoc") + ": " + location);

        URL url = null;

        try {
            if (new File(location).exists())
                // TODO: Should be toURI().toURL()
                url = new File(location).toURL(); // Why use file.getCanonicalFile?
            else
                url = new URL(ServiceUtil.getBasicService().getCodeBase(), location);
        } catch (Exception e) {
            OutputController.getLogger().log(e);
            fatalError("Invalid jnlp file " + location);
        }

        return url;
    }

    /**
     * Gets the JNLP file from the command line arguments, or exits upon error.
     */
    private static String getJNLPFile() throws InvalidArgumentException {
        if (optionParser.getMainArgs().size() > 1 || (optionParser.mainArgExists()
                && optionParser.hasOption(OptionsDefinitions.OPTIONS.JNLP))) {
              throw new InvalidArgumentException(optionParser.getMainArg());
        } else if (optionParser.hasOption(OptionsDefinitions.OPTIONS.JNLP)) {
            return optionParser.getValue(OptionsDefinitions.OPTIONS.JNLP);
        } else if (optionParser.mainArgExists()) {
            return optionParser.getMainArg();
        }

        handleMessage();
        JNLPRuntime.exit(0);
        return null;
    }

}
