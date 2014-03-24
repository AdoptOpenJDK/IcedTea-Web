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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.UIManager;

import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.Launcher;
import net.sourceforge.jnlp.ParserSettings;
import net.sourceforge.jnlp.about.AboutDialog;
import net.sourceforge.jnlp.cache.CacheUtil;
import net.sourceforge.jnlp.cache.UpdatePolicy;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.security.viewer.CertificateViewer;
import net.sourceforge.jnlp.services.ServiceUtil;
import net.sourceforge.jnlp.util.logging.OutputController;
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

    private static final String itwInfoMessage = ""
            + nameAndVersion
            + "\n\n*  "
            + R("BAboutITW")
            + "\n*  "
            + R("BFileInfoAuthors")
            + "\n*  "
            + R("BFileInfoNews")
            + "\n*  "
            + R("BFileInfoCopying");

    private static final String helpMessage = "\n"
            + "Usage:   " + R("BOUsage") + "\n"
            + "         " + R("BOUsage2") + "\n"
            + "\n"
            + "control-options:" + "\n"
            + "  -about                " + R("BOAbout") + "\n"
            + "  -viewer               " + R("BOViewer") + "\n"
            + "\n"
            + "run-options:" + "\n"
            + "  -version              " + R("BOVersion") + "\n"
            + "  -arg arg              " + R("BOArg") + "\n"
            + "  -param name=value     " + R("BOParam") + "\n"
            + "  -property name=value  " + R("BOProperty") + "\n"
            + "  -update seconds       " + R("BOUpdate") + "\n"
            + "  -license              " + R("BOLicense") + "\n"
            + "  -verbose              " + R("BOVerbose") + "\n"
            + "  -nosecurity           " + R("BONosecurity") + "\n"
            + "  -noupdate             " + R("BONoupdate") + "\n"
            + "  -headless             " + R("BOHeadless") + "\n"
            + "  -strict               " + R("BOStrict") + "\n"
            + "  -xml                  " + R("BOXml") + "\n"
            + "  -allowredirect        " + R("BOredirect") + "\n"
            + "  -Xnofork              " + R("BXnofork") + "\n"
            + "  -Xclearcache          " + R("BXclearcache") + "\n"
            + "  -Xignoreheaders       " + R("BXignoreheaders") + "\n"
            + "  -help                 " + R("BOHelp") + "\n";

    private static final String doubleArgs = "-basedir -jnlp -arg -param -property -update";

    private static String args[]; // avoid the hot potato

    /**
     * Launch the JNLP file specified by the command-line arguments.
     */
    public static void main(String[] argsIn) {
        args = argsIn;

        if (AppContext.getAppContext() == null) {
            SunToolkit.createNewAppContext();
        }
        if (null != getOption("-headless")) {
            JNLPRuntime.setHeadless(true);
        }

        DeploymentConfiguration.move14AndOlderFilesTo15StructureCatched();

        if (null != getOption("-viewer")) {
            try {
                CertificateViewer.main(null);
                JNLPRuntime.exit(0);
            } catch (Exception e) {
                OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
            }
        }

        if (null != getOption("-version")) {
            OutputController.getLogger().printOutLn(nameAndVersion);
            JNLPRuntime.exit(0);
        }

        if (null != getOption("-license")) {
            OutputController.getLogger().printOutLn(miniLicense);
            JNLPRuntime.exit(0);
        }

        if (null != getOption("-help")) {
            OutputController.getLogger().printOutLn(helpMessage);
            JNLPRuntime.exit(0);
        }

        if (null != getOption("-about")) {
                OutputController.getLogger().printOutLn(itwInfoMessage);
            if (null != getOption("-headless")) {
                JNLPRuntime.exit(0);
            } else {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    OutputController.getLogger().log("Unable to set system look and feel");
                }
                OutputController.getLogger().printOutLn(R("BLaunchAbout"));
                AboutDialog.display();
                return;
            }
        }

        if (null != getOption("-verbose"))
            JNLPRuntime.setDebug(true);

        if (null != getOption("-update")) {
            int value = Integer.parseInt(getOption("-update"));
            JNLPRuntime.setDefaultUpdatePolicy(new UpdatePolicy(value * 1000l));
        }

        if (null != getOption("-noupdate"))
            JNLPRuntime.setDefaultUpdatePolicy(UpdatePolicy.NEVER);

        if (null != getOption("-Xnofork")) {
            JNLPRuntime.setForksAllowed(false);
        }
        if (null != getOption("-Xtrustall")) {
            JNLPRuntime.setTrustAll(true);
        }
        if (null != getOption("-Xtrustnone")) {
            JNLPRuntime.setTrustNone(true);
        }
        if (null != getOption("-Xignoreheaders")) {
            JNLPRuntime.setIgnoreHeaders(true);
        }
        if (null != getOption("-allowredirect")) {
            JNLPRuntime.setAllowRedirect(true);
        }

        JNLPRuntime.setInitialArgments(Arrays.asList(argsIn));

        AccessController.doPrivileged(new Boot());

    }

    /**
     * The privileged part (jdk1.3 compatibility).
     */
    public Void run() {
        JNLPRuntime.setSecurityEnabled(null == getOption("-nosecurity"));
        JNLPRuntime.initialize(true);

        /*
         * FIXME
         * This should have been done with the rest of the argument parsing
         * code. But we need to know what the cache and base directories are,
         * and baseDir is initialized here
         */
        if (null != getOption("-Xclearcache")) {
            CacheUtil.clearCache();
            return null;
        }

        Map<String, String[]> extra = new HashMap<String, String[]>();
        extra.put("arguments", getOptions("-arg"));
        extra.put("parameters", getOptions("-param"));
        extra.put("properties", getOptions("-property"));

        ParserSettings settings = ParserSettings.setGlobalParserSettingsFromArgs(args);

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

        String location = getJNLPFile();

        if (location == null) {
            OutputController.getLogger().printOutLn(helpMessage);
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
    private static String getJNLPFile() {

        if (args.length == 0) {
            OutputController.getLogger().printOutLn(helpMessage);
            JNLPRuntime.exit(0);
        } else if (args.length == 1) {
            return args[args.length - 1];
        } else {
            String lastArg = args[args.length - 1];
            String secondLastArg = args[args.length - 2];

            if (doubleArgs.indexOf(secondLastArg) == -1) {
                return lastArg;
            } else {
                OutputController.getLogger().printOutLn(helpMessage);
                JNLPRuntime.exit(0);
            }
        }
        return null;
    }

    /**
     * Return value of the first occurence of the specified
     * option, or null if the option is not present.  If the
     * option is a flag (0-parameter) and is present then the
     * option name is returned.
     */
    private static String getOption(String option) {
        String result[] = getOptions(option);

        if (result.length == 0)
            return null;
        else
            return result[0];
    }

    /**
     * Return all the values of the specified option, or an empty
     * array if the option is not present.  If the option is a
     * flag (0-parameter) and is present then the option name is
     * returned once for each occurrence.
     */
    private static String[] getOptions(String option) {
        List<String> result = new ArrayList<String>();

        for (int i = 0; i < args.length; i++) {
            if (option.equals(args[i])) {
                if (-1 == doubleArgs.indexOf(option))
                    result.add(option);
                else if (i + 1 < args.length)
                    result.add(args[i + 1]);
            }

            if (args[i].startsWith("-") && -1 != doubleArgs.indexOf(args[i]))
                i++;
        }

        return result.toArray(new String[result.size()]);
    }

}
