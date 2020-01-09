package net.adoptopenjdk.icedteaweb.classloader;

import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;

import java.net.URL;

public interface LocalCacheAccess {

    URL getLocalUrl(JARDesc jar);
}
