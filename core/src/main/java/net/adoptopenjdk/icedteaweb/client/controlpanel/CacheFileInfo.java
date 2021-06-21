package net.adoptopenjdk.icedteaweb.client.controlpanel;

import java.io.File;
import java.util.Date;

public interface CacheFileInfo {

    String getInfoFile();

    File getParentFile();

    String getProtocol();

    String getDomain();

    long getSize();

    Date getLastModified();

    String getJnlpPath();
}
