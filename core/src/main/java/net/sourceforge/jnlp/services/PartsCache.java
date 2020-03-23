package net.sourceforge.jnlp.services;

import net.adoptopenjdk.icedteaweb.classloader.Extension;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;

import java.net.URL;

public interface PartsCache {

    void downloadPart(String partName);

    void downloadPart(String partName, Extension extension);

    void downloadPartContainingJar(URL ref, VersionString version);

    boolean isPartDownloaded(String partName);

    boolean isPartDownloaded(String partName, Extension extension);

    boolean isPartContainingJar(URL ref, VersionString version);

    void removePart(final String partName);

    void removePart(final String partName, Extension extension);

    void removePartContainingJar(URL ref, VersionString version);
}
