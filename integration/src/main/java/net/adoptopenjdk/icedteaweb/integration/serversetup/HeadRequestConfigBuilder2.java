package net.adoptopenjdk.icedteaweb.integration.serversetup;

import java.time.ZonedDateTime;

/**
 * ...
 */
public interface HeadRequestConfigBuilder2 {
    HeadRequestConfigBuilder3 returnsNotFound();

    HeadRequestConfigBuilder3 returnsServerError();

    HeadRequestConfigBuilder3 lastModifiedAt(ZonedDateTime lastModified);

    HeadRequestConfigBuilder3 withoutLastModificationDate();
}
