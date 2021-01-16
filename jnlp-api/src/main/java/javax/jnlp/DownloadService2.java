package javax.jnlp;

import java.io.IOException;

/**
 * Provides cache query services to JNLP applications.
 * Together with methods in DownloadService, this allows for advanced programmatic cache management.
 *
 * @since 6.0.18
 */
public interface DownloadService2 {

    /**
     * Specifies patterns for resource queries as arguments and holds results in
     * {@link #getCachedResources(javax.jnlp.DownloadService2.ResourceSpec)} and
     * {@link #getUpdateAvailableResources(javax.jnlp.DownloadService2.ResourceSpec)}.
     * For the url and version properties, standard regular expressions as documented in
     * {code java.util.regex} are supported.
     */
    class ResourceSpec {

        public static final long UNKNOWN = Long.MIN_VALUE;

        protected String url;
        protected String version;
        protected int type;

        /**
         * Creates a new ResourceSpec instance.
         *
         * @param url     the URL pattern
         * @param version the version pattern
         * @param type    the resource type. This should be one of the following constants defined in DownloadService2:
         *                {@link #ALL}, {@link #APPLICATION}, {@link #APPLET}, {@link #EXTENSION},
         *                {@link #JAR}, {@link #IMAGE}, or {@link #CLASS}.
         */
        public ResourceSpec(java.lang.String url, java.lang.String version, int type) {
            this.url = url;
            this.version = version;
            this.type = type;
        }

        /**
         * Returns the time of expiration of the resource.
         * The returned value has the same semantics as the return value of System.currentTimeMillis().
         * A value of 0 means unknown.
         *
         * @return the time of expiration of the resource
         */
        public long getExpirationDate() {
            return UNKNOWN;
        }

        /**
         * Returns the time of last modification of the resource.
         * The returned value has the same semantics as the return value of System.currentTimeMillis().
         * A value of 0 means unknown.
         *
         * @return the time of last modification of the resource
         */
        public long getLastModified() {
            return UNKNOWN;
        }

        /**
         * Returns the size of a resource.
         * This is only useful for ResourceSpecs that have been returned as a result of
         * {@link #getCachedResources(javax.jnlp.DownloadService2.ResourceSpec)} or
         * {@link #getUpdateAvailableResources(javax.jnlp.DownloadService2.ResourceSpec)}.
         *
         * @return the size of a resource
         */
        public long getSize() {
            return UNKNOWN;
        }

        /**
         * Returns the type of this resource.
         *
         * @return the type of this resource
         */
        public int getType() {
            return type;
        }

        /**
         * Returns the URL of this resource.
         *
         * @return the URL of this resource
         */
        public java.lang.String getUrl() {
            return url;
        }

        /**
         * Returns the version of this resource.
         *
         * @return the version of this resource
         */
        public java.lang.String getVersion() {
            return version;
        }
    }

    /**
     * Matches all resources in {@link #getCachedResources(javax.jnlp.DownloadService2.ResourceSpec)} and
     * {@link #getUpdateAvailableResources(javax.jnlp.DownloadService2.ResourceSpec)}.
     */
    int ALL = 0;
    /**
     * Matches applets in {@link #getCachedResources(javax.jnlp.DownloadService2.ResourceSpec)} and
     * {@link #getUpdateAvailableResources(javax.jnlp.DownloadService2.ResourceSpec)}.
     */
    int APPLET = 2;
    /**
     * Matches applications in {@link #getCachedResources(javax.jnlp.DownloadService2.ResourceSpec)} and
     * {@link #getUpdateAvailableResources(javax.jnlp.DownloadService2.ResourceSpec)}.
     */
    int APPLICATION = 1;
    /**
     * Matches class files in {@link #getCachedResources(javax.jnlp.DownloadService2.ResourceSpec)} and
     * {@link #getUpdateAvailableResources(javax.jnlp.DownloadService2.ResourceSpec)}.
     */
    int CLASS = 6;
    /**
     * Matches extensions in {@link #getCachedResources(javax.jnlp.DownloadService2.ResourceSpec)} and
     * {@link #getUpdateAvailableResources(javax.jnlp.DownloadService2.ResourceSpec)}.
     */
    int EXTENSION = 3;
    /**
     * Matches images in {@link #getCachedResources(javax.jnlp.DownloadService2.ResourceSpec)} and
     * {@link #getUpdateAvailableResources(javax.jnlp.DownloadService2.ResourceSpec)}.
     */
    int IMAGE = 5;
    /**
     * Matches JARs in {@link #getCachedResources(javax.jnlp.DownloadService2.ResourceSpec)} and
     * {@link #getUpdateAvailableResources(javax.jnlp.DownloadService2.ResourceSpec)}.
     */
    int JAR = 4;

    /**
     * Returns all resources in the cache that match one of the specified resource specs.
     * For supported patterns in the query arguments, see DownloadService2.ResourceSpec.
     * The returned ResourceSpec objects have specific URL and version properties (i.e. no patterns).
     *
     * @param resourceSpec the spec to match resources against
     * @return all resources that match one of the specs
     * @throws IllegalArgumentException if the ResourceSpec is null, or
     *                                  if the ResourceSpec contains a null or empty URL string, or
     *                                  if the ResourceSpec contains invalid regular expressions.
     *                                  if the ResourceSpec contains a type that is not one of:
     *                                  {@link #ALL}, {@link #APPLICATION}, {@link #APPLET}, {@link #EXTENSION},
     *                                  {@link #JAR}, {@link #IMAGE}, or {@link #CLASS}.
     */
    DownloadService2.ResourceSpec[] getCachedResources(ResourceSpec resourceSpec);

    /**
     * Returns all resources in the cache that match one of the specified resource specs AND
     * have an update available from their server.
     * For supported patterns in the query arguments, see DownloadService2.ResourceSpec.
     * The returned ResourceSpec objects have specific URL and version properties (i.e. no patterns).
     * NOTE: This call may attempt HTTP GET request to check for update.
     *
     * @param resourceSpec the spec to match resources against
     * @return all resources for which an update is available that match one of the specs
     * @throws IOException              if something went wrong during update checks
     * @throws IllegalArgumentException if the ResourceSpec is null, or
     *                                  if the ResourceSpec contains a null or empty URL string, or
     *                                  if the ResourceSpec contains invalid regular expressions.
     *                                  if the ResourceSpec contains a type that is not one of:
     *                                  {@link #ALL}, {@link #APPLICATION}, {@link #APPLET}, {@link #EXTENSION},
     *                                  {@link #JAR}, {@link #IMAGE}, or {@link #CLASS}.
     */
    DownloadService2.ResourceSpec[] getUpdateAvailableResources(ResourceSpec resourceSpec) throws IOException;
}
