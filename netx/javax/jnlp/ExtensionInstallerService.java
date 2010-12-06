package javax.jnlp;

public interface ExtensionInstallerService {

    public java.lang.String getInstallPath();

    public java.lang.String getExtensionVersion();

    public java.net.URL getExtensionLocation();

    public void hideProgressBar();

    public void hideStatusWindow();

    public void setHeading(java.lang.String heading);

    public void setStatus(java.lang.String status);

    public void updateProgress(int value);

    public void installSucceeded(boolean needsReboot);

    public void installFailed();

    public void setJREInfo(java.lang.String platformVersion, java.lang.String jrePath);

    public void setNativeLibraryInfo(java.lang.String path);

    public java.lang.String getInstalledJRE(java.net.URL url, java.lang.String version);

}
