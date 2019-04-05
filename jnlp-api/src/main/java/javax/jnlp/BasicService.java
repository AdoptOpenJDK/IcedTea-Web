package javax.jnlp;

public interface BasicService {

    public java.net.URL getCodeBase();

    public boolean isOffline();

    public boolean showDocument(java.net.URL url);

    public boolean isWebBrowserSupported();

}
