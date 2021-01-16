package javax.jnlp;

import java.net.URL;

/**
 * The BasicService interface provides access to the codebase of the application, if an application is run
 * in offline mode, and simple interaction with the native browser on the given platform.
 * <p>
 * This interface mimics loosely the AppletContext functionality.
 */
public interface BasicService {

    /**
     * Returns the codebase for the application. The codebase is either specified directly in the JNLP file,
     * or it is the location of the JAR file containing the main class of the application.
     *
     * @return a URL with the codebase of the application, or null if the application is running from
     * local file system.
     */
    URL getCodeBase();

    /**
     * Determines if the system is offline. The return value represents the JNLP client's "best guess" at the
     * online / offline state of the client system. The return value is does not have to be guaranteed to
     * be reliable, as it is sometimes difficult to ascertain the true online / offline state of a client system.
     *
     * @return true if the system is offline, otherwise false
     */
    boolean isOffline();

    /**
     * Directs a browser on the client to show the given URL. This will typically replace the page currently
     * being viewed in a browser with the given URL, or cause a browser to be launched that will show the given URL.
     *
     * @param url an URL giving the location of the document. A relative URL will be relative to the codebase.
     * @return true if the request succeeded false if the url is null or the request failed.
     */
    boolean showDocument(URL url);

    /**
     * Checks if a Web browser is supported on the current platform and by the given JNLP Client.
     * If this is not the case, then showDocument(java.net.URL) will always return false.
     *
     * @return true if a Web browser is supported, otherwise false
     */
    boolean isWebBrowserSupported();
}
