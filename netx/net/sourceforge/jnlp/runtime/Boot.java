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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import net.sourceforge.jnlp.AppletDesc;
import net.sourceforge.jnlp.ApplicationDesc;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.Launcher;
import net.sourceforge.jnlp.ParseException;
import net.sourceforge.jnlp.PropertyDesc;
import net.sourceforge.jnlp.ResourcesDesc;
import net.sourceforge.jnlp.cache.CacheUtil;
import net.sourceforge.jnlp.cache.UpdatePolicy;
import net.sourceforge.jnlp.security.VariableX509TrustManager;
import net.sourceforge.jnlp.security.viewer.CertificateViewer;
import net.sourceforge.jnlp.services.ServiceUtil;

/**
 * This is the main entry point for the JNLP client.  The main
 * method parses the command line parameters and loads a JNLP
 * file into the secure runtime environment.  This class is meant
 * to be called from the command line or file association; to
 * initialize the netx engine from other code invoke the
 * <code>JNLPRuntime.initialize</code> method after configuring
 * the runtime.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.21 $
 */
public final class Boot implements PrivilegedAction {

    // todo: decide whether a spawned netx (external launch)
    // should inherit the same options as this instance (store argv?)

    private static String R(String key) { return JNLPRuntime.getMessage(key); }
    private static String R(String key, Object param) { return JNLPRuntime.getMessage(key, new Object[] {param}); }

    private static final String version = "0.5";

    /** the text to display before launching the about link */
    private static final String aboutMessage = ""
        + "netx v"+version+" - (C)2001-2003 Jon A. Maxwell (jmaxwell@users.sourceforge.net)\n"
        + "\n"
        + R("BLaunchAbout");

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

    private static final String helpMessage = "\n"
        + "Usage:   " + R("BOUsage")+"\n"
        + "         " + R("BOUsage2")+"\n"
        + "\n"
        + "control-options:"+"\n"
        + "  -about                "+R("BOAbout")+"\n"
        + "  -viewer               "+R("BOViewer")+"\n"
        + "\n"
        + "run-options:"+"\n"
        + "  -basedir dir          "+R("BOBasedir")+"\n"
        + "  -arg arg              "+R("BOArg")+"\n"
        + "  -param name=value     "+R("BOParam")+"\n"
        + "  -property name=value  "+R("BOProperty")+"\n"
        + "  -update seconds       "+R("BOUpdate")+"\n"
        + "  -license              "+R("BOLicense")+"\n"
        + "  -verbose              "+R("BOVerbose")+"\n"
        + "  -nosecurity           "+R("BONosecurity")+"\n"
        + "  -noupdate             "+R("BONoupdate")+"\n"
        + "  -headless             "+R("BOHeadless")+"\n"
        + "  -strict               "+R("BOStrict")+"\n"
        + "  -umask=value          "+R("BOUmask")+"\n"
        + "  -Xnofork              "+R("BXnofork")+"\n"
        + "  -Xclearcache          "+R("BXclearcache")+"\n"
        + "  -help                 "+R("BOHelp")+"\n";

    private static final String doubleArgs = "-basedir -jnlp -arg -param -property -update";

    private static String args[]; // avoid the hot potato


    /**
     * Launch the JNLP file specified by the command-line arguments.
     */
    public static void main(String[] argsIn) {
        args = argsIn;

        if (null != getOption("-viewer")) {

            try {
                CertificateViewer.main(null);
                System.exit(0);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        if (null != getOption("-license")) {
            System.out.println(miniLicense);
            System.exit(0);
        }

        if (null != getOption("-help")) {
            System.out.println(helpMessage);
            System.exit(0);
        }

        if (null != getOption("-about"))
            System.out.println(aboutMessage);

        if (null != getOption("-verbose"))
            JNLPRuntime.setDebug(true);

        if (null != getOption("-update")) {
            int value = Integer.parseInt(getOption("-update"));
            JNLPRuntime.setDefaultUpdatePolicy(new UpdatePolicy(value*1000l));
        }

        if (null != getOption("-headless"))
            JNLPRuntime.setHeadless(true);


        if (null != getOption("-noupdate"))
            JNLPRuntime.setDefaultUpdatePolicy(UpdatePolicy.NEVER);

        if (null != getOption("-Xnofork")) {
            JNLPRuntime.setForksAllowed(false);
        }

        // wire in custom authenticator
        try {
            SSLSocketFactory sslSocketFactory;
            SSLContext context = SSLContext.getInstance("SSL");
            TrustManager[] trust = new TrustManager[] { VariableX509TrustManager.getInstance() };
            context.init(null, trust, null);
            sslSocketFactory = context.getSocketFactory();

            HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory);
        } catch (Exception e) {
            System.err.println("Unable to set SSLSocketfactory (may _prevent_ access to sites that should be trusted)! Continuing anyway...");
            e.printStackTrace();
        }

        JNLPRuntime.setInitialArgments(Arrays.asList(argsIn));

        // do in a privileged action to clear the security context of
        // the Boot13 class, which doesn't have any privileges in
        // JRE1.3; JRE1.4 works without Boot13 or this PrivilegedAction.
        AccessController.doPrivileged(new Boot());

    }

    /**
     * The privileged part (jdk1.3 compatibility).
     */
    public Object run() {
        JNLPRuntime.setBaseDir(getBaseDir());
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

        try {
            new Launcher().launch(getFile());
        }
        catch (LaunchException ex) {
            // default handler prints this
        }
        catch (Exception ex) {
            if (JNLPRuntime.isDebug())
                ex.printStackTrace();

            fatalError(JNLPRuntime.getMessage("RUnexpected",
                        new Object[] {ex.toString(), ex.getStackTrace()[0]} ));
        }

        return null;
    }

    private static void fatalError(String message) {
        System.err.println("netx: "+message);
        System.exit(1);
    }

    /**
     * Returns the about.jnlp file in {java.home}/lib or null if this file
     * does not exist.
     */
    private static String getAboutFile() {

        if (new File(JNLPRuntime.NETX_ABOUT_FILE).exists())
            return JNLPRuntime.NETX_ABOUT_FILE;
        else
            return null;
    }

    /**
     * Returns the file to open; does not return if no file was
     * specified.
     */
    private static JNLPFile getFile() throws ParseException, MalformedURLException, IOException {

        String location = getJNLPFile();

        // override -jnlp with aboutFile
        if (getOption("-about") != null) {
            location = getAboutFile();
            if (location == null)
                fatalError("Unable to find about.jnlp in {java.home}/lib/");
        } else {
            location = getJNLPFile();
        }

        if (location == null) {
            System.out.println(helpMessage);
            System.exit(1);
        }

        if (JNLPRuntime.isDebug())
            System.out.println(R("BFileLoc")+": "+location);

        URL url = null;

        try {
            if (new File(location).exists())
                url = new File(location).toURL(); // Why use file.getCanonicalFile?
            else
                url = new URL(ServiceUtil.getBasicService().getCodeBase(), location);
        } catch (Exception e) {
            fatalError("Invalid jnlp file " + location);
            if (JNLPRuntime.isDebug())
                e.printStackTrace();
        }

        boolean strict = (null != getOption("-strict"));

        JNLPFile file = new JNLPFile(url, strict);

        // Launches the jnlp file where this file originated.
        if (file.getSourceLocation() != null) {
            file = new JNLPFile(file.getSourceLocation(), strict);
        }

        // add in extra params from command line
        addProperties(file);

        if (file.isApplet())
            addParameters(file);

        if (file.isApplication())
            addArguments(file);

        if (JNLPRuntime.isDebug()) {
            if (getOption("-arg") != null)
                if (file.isInstaller() || file.isApplet())
                    System.out.println(R("BArgsNA"));

            if (getOption("-param") != null)
                if (file.isApplication())
                    System.out.println(R("BParamNA"));
        }

        return file;
    }

    /**
     * Add the properties to the JNLP file.
     */
    private static void addProperties(JNLPFile file) {
        String props[] = getOptions("-property");
        ResourcesDesc resources = file.getResources();

        for (int i=0; i < props.length; i++) {
            // allows empty property, not sure about validity of that.
            int equals = props[i].indexOf("=");
            if (equals == -1)
                fatalError(R("BBadProp", props[i]));

            String key = props[i].substring(0, equals);
            String value = props[i].substring(equals+1, props[i].length());

            resources.addResource(new PropertyDesc(key, value));
        }
    }

    /**
     * Add the params to the JNLP file; only call if file is
     * actually an applet file.
     */
    private static void addParameters(JNLPFile file) {
        String params[] = getOptions("-param");
        AppletDesc applet = file.getApplet();

        for (int i=0; i < params.length; i++) {
            // allows empty param, not sure about validity of that.
            int equals = params[i].indexOf("=");
            if (equals == -1)
                fatalError(R("BBadParam", params[i]));

            String name = params[i].substring(0, equals);
            String value = params[i].substring(equals+1, params[i].length());

            applet.addParameter(name, value);
        }
    }

    /**
     * Add the arguments to the JNLP file; only call if file is
     * actually an application (not installer).
     */
    private static void addArguments(JNLPFile file) {
        String args[] = getOptions("-arg");  // FYI args also global variable
        ApplicationDesc app = file.getApplication();

        for (int i=0; i < args.length; i++) {
            app.addArgument(args[i]);
        }
    }

    /**
     * Gets the JNLP file from the command line arguments, or exits upon error.
     */
    private static String getJNLPFile() {

        if (args.length == 0) {
            System.out.println(helpMessage);
            System.exit(0);
        } else if (args.length == 1) {
            return args[args.length - 1];
        } else {
            String lastArg = args[args.length - 1];
            String secondLastArg = args[args.length - 2];

            if (doubleArgs.indexOf(secondLastArg) == -1) {
                return lastArg;
            } else {
                System.out.println(helpMessage);
                System.exit(0);
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
        List result = new ArrayList();

        for (int i=0; i < args.length; i++) {
            if (option.equals(args[i])) {
                if (-1 == doubleArgs.indexOf(option))
                    result.add(option);
                else
                    if (i+1 < args.length)
                        result.add(args[i+1]);
            }

            if (args[i].startsWith("-") && -1 != doubleArgs.indexOf(args[i]))
                i++;
        }

        return (String[]) result.toArray( new String[result.size()] );
    }

    /**
     * Return the base dir.  If the base dir parameter is not set
     * the value is read from JNLPRuntime.NETX_ABOUT_FILE file.
     * If that file does not exist, an install dialog is displayed
     * to select the base directory.
     */
    private static File getBaseDir() {
        if (getOption("-basedir") != null) {
            File basedir = new File(getOption("-basedir"));

            if (!basedir.exists() || !basedir.isDirectory())
                fatalError(R("BNoDir", basedir));

            return basedir;
        }

        // check .netxrc
        File basedir = JNLPRuntime.getDefaultBaseDir();
        if (basedir == null)
            fatalError(R("BNoBase"));

        return basedir;
    }

}
