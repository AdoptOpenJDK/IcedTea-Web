// Copyright (C) 2001 Jon A. Maxwell (JAM)
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

package net.sourceforge.jnlp.services;

import static net.sourceforge.jnlp.runtime.Translator.R;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.jnlp.BasicService;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.sourceforge.jnlp.InformationDesc;
import net.sourceforge.jnlp.JARDesc;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.Launcher;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.ApplicationInstance;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

/**
 * The BasicService JNLP service.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.10 $
 */
class XBasicService implements BasicService {

    /** command used to exec the native browser */
    private String command = null;

    /** whether the command was loaded / prompted for */
    private boolean initialized = false;

    protected XBasicService() {
    }

    /**
     * Returns the codebase of the application, applet, or
     * installer.  If the codebase was not specified in the JNLP
     * element then the main JAR's location is returned.  If no main
     * JAR was specified then the location of the JAR containing the
     * main class is returned.
     */
    public URL getCodeBase() {
        ApplicationInstance app = JNLPRuntime.getApplication();

        if (app != null) {
            JNLPFile file = app.getJNLPFile();

            // return the codebase.
            if (file.getCodeBase() != null)
                return file.getCodeBase();

            // else return the main JAR's URL.
            JARDesc mainJar = file.getResources().getMainJAR();
            if (mainJar != null)
                return mainJar.getLocation();

            // else find JAR where main class was defined.
            //
            // JNLPFile file = app.getJNLPFile();
            // String mainClass = file.getLaunchInfo().getMainClass()+".class";
            // URL jarUrl = app.getClassLoader().getResource(mainClass);
            // go through list of JARDesc to find one matching jarUrl
        }

        return null;
    }

    /**
     * Return true if the Environment is Offline
     */
    public boolean isOffline() {

        URL url = findFirstURLFromJNLPFile();

        try {
            url.openConnection().getInputStream().close();
            return false;
        } catch (IOException exception) {
            return true;
        }
    }

    /**
     * Return the first URL from the jnlp file
     * Or a default URL if no url found in JNLP file
     */
    private URL findFirstURLFromJNLPFile() {

        ApplicationInstance app = JNLPRuntime.getApplication();

        if (app != null) {
            JNLPFile jnlpFile = app.getJNLPFile();

            URL sourceURL = jnlpFile.getSourceLocation();
            if (sourceURL != null) {
                return sourceURL;
            }

            URL codeBaseURL = jnlpFile.getCodeBase();
            if (codeBaseURL != null) {
                return codeBaseURL;
            }

            InformationDesc informationDesc = jnlpFile.getInformation();
            URL homePage = informationDesc.getHomepage();
            if (homePage != null) {
                return homePage;
            }

            JARDesc[] jarDescs = jnlpFile.getResources().getJARs();
            for (JARDesc jarDesc : jarDescs) {
                return jarDesc.getLocation();
            }
        }

        // this section is only reached if the jnlp file has no jars.
        // that doesnt seem very likely.
        URL arbitraryURL;
        try {
            arbitraryURL = new URL("http://icedtea.classpath.org");
        } catch (MalformedURLException malformedURL) {
            throw new RuntimeException(malformedURL);
        }

        return arbitraryURL;
    }

    /**
     * Return true if a Web Browser is Supported
     */
    public boolean isWebBrowserSupported() {
        initialize();

        return command != null;
    }

    /**
     * Show a document.
     *
     * @return whether the document was opened
     */
    public boolean showDocument(URL url) {
        initialize();

        if (url.toString().endsWith(".jnlp")) {
            try {
                new Launcher().launchExternal(url);
                return true;
            } catch (Exception ex) {
                return false;
            }
        }

        if (command != null) {
            try {
                // this is bogus because the command may require options;
                // should use a StreamTokenizer or similar to get tokens
                // outside of quotes.
                Runtime.getRuntime().exec(command + " " + url.toString());
                //Runtime.getRuntime().exec(new String[]{command,url.toString()});

                return true;
            } catch (IOException ex) {
                if (JNLPRuntime.isDebug())
                    ex.printStackTrace();
            }
        }

        return false;
    }

    private void initialize() {
        if (initialized)
            return;
        initialized = true;
        initializeBrowserCommand();
        if (JNLPRuntime.isDebug()) {
            System.out.println("browser is " + command);
        }
    }

    /**
     * Initializes {@link #command} to launch a browser
     */
    private void initializeBrowserCommand() {
        if (JNLPRuntime.isWindows()) {
            command = "rundll32 url.dll,FileProtocolHandler ";
        } else if (JNLPRuntime.isUnix()) {
            DeploymentConfiguration config = JNLPRuntime.getConfiguration();
            command = config.getProperty(DeploymentConfiguration.KEY_BROWSER_PATH);
            if (command != null) {
                return;
            }

            if (posixCommandExists("xdg-open")) {
                command = "xdg-open";
                return;
            }

            if (posixCommandExists(System.getenv("BROWSER"))) {
                command = System.getenv("BROWSER");
                return;
            }

            while (true) {
                command = promptForCommand(command);
                if (command != null && posixCommandExists(command)) {
                    config.setProperty(DeploymentConfiguration.KEY_BROWSER_PATH, command);
                    try {
                        config.save();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        } else {
            DeploymentConfiguration config = JNLPRuntime.getConfiguration();
            command = config.getProperty(DeploymentConfiguration.KEY_BROWSER_PATH);

            if (command == null) { // prompt & store
                command = promptForCommand(null);

                if (command != null) {
                    config.setProperty(DeploymentConfiguration.KEY_BROWSER_PATH, command);
                    try {
                        config.save();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Check that a command exists on a posix-like system
     * @param command the command to check
     * @return true if the command exists
     */
    private boolean posixCommandExists(String command) {
        if (command == null || command.trim().length() == 0) {
            return false;
        }

        command = command.trim();
        if (command.contains("\n") || command.contains("\r")) {
            return false;
        }

        try {
            Process p = Runtime.getRuntime().exec(new String[] { "bash", "-c", "type " + command });
            p.waitFor();
            return (p.exitValue() == 0);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String promptForCommand(String previousCommand) {
        String message = null;
        if (previousCommand == null) {
            message = R("RBrowserLocationPromptMessage");
        } else {
            message = R("RBrowserLocationPromptMessageWithReason", previousCommand);
        }
        return JOptionPane.showInputDialog(new JPanel(),
                                           R("RBrowserLocationPromptTitle"),
                                           message,
                                           JOptionPane.PLAIN_MESSAGE
                                          );
    }

}
