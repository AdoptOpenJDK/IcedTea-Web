package net.adoptopenjdk.icedteaweb.client.controlpanel;

import net.adoptopenjdk.icedteaweb.resources.cache.CachedFile;

import java.io.File;
import java.util.Date;

public interface CacheFileInfo {

    CachedFile getInfoFile();

    File getParentFile();

    String getProtocol();

    String getDomain();

    long getSize();

    Date getLastModified();

    String getJnlpPath();

    long getDownloadedAt();
}
