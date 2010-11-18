package javax.jnlp;

public interface DownloadService2 {

    public static class ResourceSpec {

        public static final long UNKNOWN = Long.MIN_VALUE;

        protected String url;
        protected String version;
        protected int type;

        public ResourceSpec(java.lang.String url, java.lang.String version, int type) {
            this.url = url;
            this.version = version;
            this.type = type;
        }

        public long getExpirationDate() {
            return UNKNOWN;
        }

        public long getLastModified() {
            return UNKNOWN;
        }

        public long getSize() {
            return UNKNOWN;
        }

        public int getType() {
            return type;
        }

        public java.lang.String getUrl() {
            return url;
        }

        public java.lang.String getVersion() {
            return version;
        }
    }

    public static final int ALL = 0;
    public static final int APPLET = 2;
    public static final int APPLICATION = 1;
    public static final int CLASS = 6;
    public static final int EXTENSION = 3;
    public static final int IMAGE = 5;
    public static final int JAR = 4;

    public DownloadService2.ResourceSpec[] getCachedResources(
            javax.jnlp.DownloadService2.ResourceSpec resourceSpec);

    public DownloadService2.ResourceSpec[] getUpdateAvaiableReosurces(
            javax.jnlp.DownloadService2.ResourceSpec resourceSpec);
}
