package javax.jnlp;

public interface DownloadServiceListener {

    public void progress(java.net.URL url, java.lang.String version, long readSoFar, long total, int overallPercent);

    public void validating(java.net.URL url, java.lang.String version, long entry, long total, int overallPercent);

    public void upgradingArchive(java.net.URL url, java.lang.String version, int patchPercent, int overallPercent);

    public void downloadFailed(java.net.URL url, java.lang.String version);

}
