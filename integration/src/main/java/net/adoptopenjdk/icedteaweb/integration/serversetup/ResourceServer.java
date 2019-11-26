package net.adoptopenjdk.icedteaweb.integration.serversetup;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;

/**
 * ...
 */
public interface ResourceServer {
    HeadRequestConfigBuilder1 withoutVersion();
    HeadRequestConfigBuilder1 withVersion(VersionId versionId);
    HeadRequestConfigBuilder1 withVersion(VersionString requestedVersion, VersionId versionId);
}
