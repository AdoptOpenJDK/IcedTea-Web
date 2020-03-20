package net.sourceforge.jnlp.services;

import net.adoptopenjdk.icedteaweb.classloader.Extension;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;

import java.net.URL;

public interface PartsCache {

    void downloadPart(String partName);

    void downloadPart(String partName, Extension extension);

    boolean isPartDownloaded(String partName);

    boolean isPartDownloaded(String partName, Extension extension);

    boolean isInAnyPart(URL ref, VersionString version);
}
