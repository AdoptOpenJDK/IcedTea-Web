package net.adoptopenjdk.icedteaweb.resources.cache;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.sourceforge.jnlp.cache.CacheUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The Cache
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
    public static File getCacheFile(final URL resource, final VersionId version) {
        if (!CacheUtil.isCacheable(resource)) {
            throw new IllegalArgumentException(resource + " is not a cacheable resource");
        }
        return LeastRecentlyUsedCache.getInstance().getOrCreateCacheFile(resource, version);
    }

    public static void addToCache(ResourceInfo infoFromRemote, InputStream unpackedStream) throws IOException {
        if (!CacheUtil.isCacheable(infoFromRemote.getResourceHref())) {
            throw new IllegalArgumentException(infoFromRemote.getResourceHref() + " is not a cacheable resource");
        }
        LeastRecentlyUsedCache.getInstance().addToCache(infoFromRemote, unpackedStream);
    }

    /**
     * Invalidate the entry and make it eligible for removal.
     *
     * @param resource the resource {@link URL}
     * @param version  the versions
     * @return the newly created cache file
     * @throws IllegalArgumentException if the resource is not cacheable
     */
    public static File replaceExistingCacheFile(final URL resource, final VersionId version) {
        if (!CacheUtil.isCacheable(resource)) {
            throw new IllegalArgumentException(resource + " is not a cacheable resource");
        }
        return LeastRecentlyUsedCache.getInstance().replaceExistingCacheFile(resource, version);
    }

    public static void deleteFromCache(ResourceInfo info) {
        LeastRecentlyUsedCache.getInstance().deleteFromCache(info.getResourceHref(), info.getVersion());
    }

    public static void deleteFromCache(URL resource, VersionString version) {
        if (!CacheUtil.isCacheable(resource)) {
            throw new IllegalArgumentException(resource + " is not a cacheable resource");
        }
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
        if (!CacheUtil.isCacheable(resource)) {
            throw new IllegalArgumentException(resource + " is not a cacheable resource");
        }

        return LeastRecentlyUsedCache.getInstance().isCached(resource, version);
    }

    public static ResourceInfo getInfo(final URL resource, final VersionId version) {
        if (!CacheUtil.isCacheable(resource)) {
            throw new IllegalArgumentException(resource + " is not a cacheable resource");
        }

        return LeastRecentlyUsedCache.getInstance().getResourceInfo(resource, version).orElse(null);
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
        if (!CacheUtil.isCacheable(resource)) {
            throw new IllegalArgumentException(resource + " is not a cacheable resource");
        }
        return LeastRecentlyUsedCache.getInstance().getBestMatchingEntryInCache(resource, version).isPresent();
    }

    public static VersionId getBestMatchingVersionInCache(final URL resource, final VersionString version) {
        if (!CacheUtil.isCacheable(resource)) {
            throw new IllegalArgumentException(resource + " is not a cacheable resource");
        }
        return LeastRecentlyUsedCache.getInstance().getBestMatchingEntryInCache(resource, version)
                .map(LeastRecentlyUsedCacheEntry::getVersion)
                .orElse(null);
    }

    public static List<VersionId> getAllVersionsInCache(final URL resource) {
        if (!CacheUtil.isCacheable(resource)) {
            throw new IllegalArgumentException(resource + " is not a cacheable resource");
        }
        return LeastRecentlyUsedCache.getInstance().getAllEntriesInCache(resource).stream()
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
        if (!CacheUtil.isCacheable(resource)) {
            throw new IllegalArgumentException(resource + " is not a cacheable resource");
        }

        return LeastRecentlyUsedCache.getInstance().isUpToDate(resource, version, lastModified);
    }

    /* ***************
     * CACHE IDs
     * ***************/

    /**
     * This method load all known IDs of applications
     */
    public static List<CacheId> getJnlpCacheIds() {
        return LeastRecentlyUsedCache.getInstance().getCacheIds(".*", true, false);
    }

    /**
     * This method load all known IDs of applications
     */
    public static List<CacheId> getDomainCacheIds() {
        return LeastRecentlyUsedCache.getInstance().getCacheIds(".*", false, true);
    }

    /**
     * This method load all known IDs of applications and  will gather all members, which share the id
     *
     * @param filter - regex to filter keys
     */
    public static List<CacheId> getCacheIds(final String filter) {
        return LeastRecentlyUsedCache.getInstance().getCacheIds(filter, true, true);
    }

    public static void deleteFromCache(final String cacheId) {
        LeastRecentlyUsedCache.getInstance().deleteFromCache(cacheId);
    }
}
