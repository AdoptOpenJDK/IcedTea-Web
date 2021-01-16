package javax.jnlp;

import java.net.URL;

/**
 * The ExtensionInstallerService is used by an extension installer to communicate with the JNLP Client.
 * It provides the following type of functionality:
 * <ul>
 *     <li>Access to prefered installation location, and other information about the JNLP Client</li>
 *     <li>Manipulation of the JNLP Client's download screen</li>
 *     <li>Methods for updating the JNLP Client with the installed code</li>
 * </ul>
 * <p>
 * The normal sequence of events for an installer is:
 * <ol>
 *     <li>Get service using ServiceManager.lookup("javax.jnlp.ExtensionInstallerService").</li>
 *     <li>Update status, heading, and progress as install progresses (setStatus, setHeading and updateProgress).</li>
 *     <li>Invoke either setJREInfo or setNativeLibraryInfo depending on if a JRE or a library is installed</li>
 *     <li>If successful invoke installSucceeded, otherwise invoke installFailed.</li>
 * </ol>
 *
 * @since 1.4.2
 */
public interface ExtensionInstallerService {

    /**
     * Returns the directory where the installer is recommended to install the extension in.
     * It is not required that the installer install in this directory, this is merely a suggested path.
     *
     * @return the directory where the installer is recommended to install the extension in.
     */
    String getInstallPath();

    /**
     * Returns the version of the extension being installed.
     *
     * @return the version of the extension being installed
     */
    String getExtensionVersion();

    /**
     * Returns the location of the extension being installed.
     *
     * @return the location of the extension being installed
     */
    URL getExtensionLocation();

    /**
     * Hides the progress bar.
     * Any subsequent calls to updateProgress will force it to be visible.
     */
    void hideProgressBar();

    /**
     * Hides the status window.
     * You should only invoke this if you are going to provide your own feedback
     * to the user as to the progress of the install.
     */
    void hideStatusWindow();

    /**
     * Updates the heading text of the progress window.
     *
     * @param heading the heading text
     */
    void setHeading(String heading);

    /**
     * Updates the status text of the progress window.
     *
     * @param status the status text
     */
    void setStatus(String status);

    /**
     * Updates the progress bar.
     *
     * @param value progress bar value - should be between 0 and 100.
     */
    void updateProgress(int value);

    /**
     * Installers should invoke this upon a successful installation of the extension.
     * This will cause the JNLP Client to regain control and continue its normal operation.
     *
     * @param needsReboot If true, a reboot is needed
     */
    void installSucceeded(boolean needsReboot);

    /**
     * This should be invoked if the install fails.
     * The JNLP Client will continue its operation, and inform the user that the install has failed.
     */
    void installFailed();

    /**
     * Informs the JNLP Client of the path to the executable for the JRE,
     * if this is an installer for a JRE, and about platform-version this JRE implements.
     *
     * @param platformVersion the platform-version this JRE implements
     * @param jrePath         the path to the executable for the JRE
     */
    void setJREInfo(String platformVersion, String jrePath);

    /**
     * Informs the JNLP Client of a directory where it should search for native libraries.
     *
     * @param path the search path for native libraries
     */
    void setNativeLibraryInfo(String path);

    /**
     * Returns the path to the executable for the given JRE.
     * This method can be used by extensions that needs to find information in a given JRE, or enhance a given JRE.
     *
     * @param url     product location of the JRE
     * @param version product version of the JRE
     * @return The path to the executable for the given JRE, or null if the JRE is not installed.
     */
    String getInstalledJRE(URL url, String version);
}
