package net.sourceforge.jnlp.runtime.classloader2;

import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;

import java.net.URL;

public interface LocalCacheAccess {

    URL getLocalUrl(JARDesc jar);
}
