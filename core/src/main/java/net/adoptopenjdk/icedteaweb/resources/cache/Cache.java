package net.adoptopenjdk.icedteaweb.resources.cache;

import net.adoptopenjdk.icedteaweb.client.controlpanel.CacheIdInfo;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import static net.sourceforge.jnlp.cache.CacheUtil.isNonCacheable;

/**
 * The Cache.
 *
 * - clear() -> void
 * - cleanup() -> void
 * - contains(cacheKey) -> boolean
 * - isNewerThan(cacheKey, date) -> boolean
 * - get(cacheKey) -> file
 * - add(inputStream, downloadInfo) -> void
 * - bestMatchingVersion(url, versionRange) -> versionId
 */
public class Cache {
    /**
     * Clears the cache by deleting all content
     * <p>
     * Note: Because of how our caching system works, deleting jars of another javaws
     * process is using them can be quite disastrous. Hence why Launcher creates lock files
     * and we check for those.
     *
     * @return true if the cache could and was cleared
     */
    public static boolean clearCache() {
        return LeastRecentlyUsedCache.getInstance().clearCache();
    }

    /**
     * This will remove all old cache items.
     */
    public static void cleanCache() {
        LeastRecentlyUsedCache.getInstance().cleanCache();
    }

    /**
     * Returns the file for the locally cached contents of the
     * resource.  This method returns the file location only and does
     * not download the resource.  The latest version of the
     * resource that matches the specified version will be returned.
     *
     * @param resource the resource {@link URL}
     * @param version  the version of the local file
     * @return the file location in the cache, or {@code null} if no versions cached
     * @throws IllegalArgumentException if the resource is not cacheable
     */
    public static File getOrCreateCacheFile(final URL resource, final VersionId version) {
        return getOrCreateCacheFile(new CacheKey(resource, version));
    }
    private static File getOrCreateCacheFile(final CacheKey key) {
        assertLocationIsCacheable(key.getLocation());
        return LeastRecentlyUsedCache.getInstance().getOrCreateCacheFile(key);
    }

    public static File addToCache(DownloadInfo infoFromRemote, InputStream unpackedStream) throws IOException {
        assertLocationIsCacheable(infoFromRemote.getCacheKey().getLocation());
        return LeastRecentlyUsedCache.getInstance().addToCache(infoFromRemote, unpackedStream);
    }

    /**
     * Invalidate the entry and make it eligible for removal.
     *
     * @param resource the resource {@link URL}
     * @param version  the versions
     * @throws IllegalArgumentException if the resource is not cacheable
     */
    public static void invalidateExistingCacheFile(final URL resource, final VersionId version) {
        invalidateExistingCacheFile(new CacheKey(resource, version));
    }
    private static void invalidateExistingCacheFile(final CacheKey key) {
        assertLocationIsCacheable(key.getLocation());
        LeastRecentlyUsedCache.getInstance().invalidateExistingCacheFile(key);
    }

    public static void deleteFromCache(ResourceInfo info) {
        LeastRecentlyUsedCache.getInstance().deleteFromCache(info.getCacheKey());
    }

    public static void deleteFromCache(URL resource, VersionString version) {
        assertLocationIsCacheable(resource);
        LeastRecentlyUsedCache.getInstance().deleteFromCache(resource, version);
    }

    /**
     * Returns whether there is a version of the URL contents in the
     * cache.
     *
     * @param resource the resource {@link URL}
     * @param version  the versions to check for
     * @return whether the cache contains the version
     * @throws IllegalArgumentException if the resource is not cacheable
     */
    public static boolean isCached(final URL resource, final VersionId version) {
        return isCached(new CacheKey(resource, version));
    }
    private static boolean isCached(final CacheKey key) {
        assertLocationIsCacheable(key.getLocation());
        return LeastRecentlyUsedCache.getInstance().isCached(key);
    }

    public static ResourceInfo getInfo(final URL resource, final VersionId version) {
        return getInfo(new CacheKey(resource, version));
    }
    public static ResourceInfo getInfo(final CacheKey key) {
        assertLocationIsCacheable(key.getLocation());
        return LeastRecentlyUsedCache.getInstance().getResourceInfo(key).orElse(null);
    }

    /**
     * Returns true if the cache has a local copy of the contents of
     * the URL matching the specified version.
     *
     * @param resource the resource URL
     * @param version  the version to check for
     * @return true if the source is in the cache
     * @throws IllegalArgumentException if the source is not cacheable
     */
    public static boolean isAnyCached(final URL resource, final VersionString version) {
        assertLocationIsCacheable(resource);
        return LeastRecentlyUsedCache.getInstance().getBestMatchingEntryInCache(resource, version).isPresent();
    }

    public static VersionId getBestMatchingVersionInCache(final URL resource, final VersionString version) {
        assertLocationIsCacheable(resource);
        return LeastRecentlyUsedCache.getInstance().getBestMatchingEntryInCache(resource, version)
                .map(LeastRecentlyUsedCacheEntry::getVersion)
                .orElse(null);
    }

    public static List<VersionId> getAllVersionsInCache(final URL resourceHref) {
        assertLocationIsCacheable(resourceHref);
        return LeastRecentlyUsedCache.getInstance().getAllEntriesInCache(resourceHref).stream()
                .map(LeastRecentlyUsedCacheEntry::getVersion)
                .collect(Collectors.toList());
    }

    /**
     * Returns whether there is a version of the URL contents in the
     * cache and it is up to date.  This method may not return
     * immediately.
     *
     * @param resource     the resource {@link URL}
     * @param version      the versions to check for
     * @param lastModified time in millis since epoch of last modification
     * @return whether the cache contains the version
     * @throws IllegalArgumentException if the resource is not cacheable
     */
    public static boolean isUpToDate(final URL resource, final VersionId version, long lastModified) {
        return isUpToDate(new CacheKey(resource, version), lastModified);
    }
    private static boolean isUpToDate(final CacheKey key, long lastModified) {
        assertLocationIsCacheable(key.getLocation());
        return LeastRecentlyUsedCache.getInstance().isUpToDate(key, lastModified);
    }

    private static void assertLocationIsCacheable(URL location) {
        if (isNonCacheable(location)) {
            throw new IllegalArgumentException(location + " is not a cacheable resource");
        }
    }

    /* ***************
     * CACHE IDs
     * ***************/

    /**
     * This method load all known IDs of applications
     */
    public static List<CacheIdInfo> getJnlpCacheIds() {
        return LeastRecentlyUsedCache.getInstance().getCacheIds(".*", true, false);
    }

    /**
     * This method load all known IDs of applications
     */
    public static List<CacheIdInfo> getDomainCacheIds() {
        return LeastRecentlyUsedCache.getInstance().getCacheIds(".*", false, true);
    }

    /**
     * This method load all known IDs of applications and  will gather all members, which share the id
     *
     * @param filter - regex to filter keys
     */
    public static List<CacheIdInfo> getCacheIds(final String filter) {
        return LeastRecentlyUsedCache.getInstance().getCacheIds(filter, true, true);
    }

    public static void deleteFromCache(final String cacheId) {
        LeastRecentlyUsedCache.getInstance().deleteFromCache(CacheIdInfoImpl.domainId(cacheId));
        LeastRecentlyUsedCache.getInstance().deleteFromCache(CacheIdInfoImpl.jnlpPathId(cacheId));
    }

    public static void deleteFromCache(final CacheIdInfo cacheId) {
        LeastRecentlyUsedCache.getInstance().deleteFromCache(cacheId);
    }
}
