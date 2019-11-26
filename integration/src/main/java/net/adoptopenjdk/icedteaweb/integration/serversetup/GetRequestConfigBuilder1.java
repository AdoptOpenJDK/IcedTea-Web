package net.adoptopenjdk.icedteaweb.integration.serversetup;

import java.time.ZonedDateTime;

/**
 * ...
 */
public interface GetRequestConfigBuilder1 {
    GetRequestConfigBuilder2 returnsNotFound();
    GetRequestConfigBuilder2 returnsServerError();
    GetRequestConfigBuilder2 lastModifiedAt(ZonedDateTime lastModified);
    GetRequestConfigBuilder2 withoutLastModificationDate();
}
