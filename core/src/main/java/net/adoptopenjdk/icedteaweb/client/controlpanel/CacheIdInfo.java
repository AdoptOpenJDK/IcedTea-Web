package net.adoptopenjdk.icedteaweb.client.controlpanel;

import java.util.List;

/**
 * ID for locating resources in the cache either by domain or jnlp-path.
 */
public interface CacheIdInfo {

    enum CacheIdType {
        DOMAIN, JNLP_PATH
    }

    String getId();
    CacheIdType getType();
    List<CacheFileInfo> getFileInfos();
}
