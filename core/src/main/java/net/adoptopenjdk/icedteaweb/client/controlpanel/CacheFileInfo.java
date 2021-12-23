package net.adoptopenjdk.icedteaweb.client.controlpanel;

import net.adoptopenjdk.icedteaweb.resources.cache.ResourceInfo;

import java.io.File;
import java.util.Date;

public interface CacheFileInfo {

    ResourceInfo getInfoFile();

    File getParentFile();

    String getProtocol();

    String getDomain();

    long getSize();

    Date getLastModified();

    String getJnlpPath();

    long getDownloadedAt();
}
