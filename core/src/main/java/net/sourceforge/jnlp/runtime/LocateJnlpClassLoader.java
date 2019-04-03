/* LocateJNLPClassLoader.java
Copyright (C) 2012, Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 2.

IcedTea is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to
the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version.
*/

package net.sourceforge.jnlp.runtime;

import java.net.URL;
import net.sourceforge.jnlp.JARDesc;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.ResourcesDesc;
import net.sourceforge.jnlp.Version;

class LocateJnlpClassLoader {

    /**
     * Locates the JNLPClassLoader of the JNLP file.
     * @param rootClassLoader Root JNLPClassLoader of the application.
     * @param urlToJnlpFile Path of the JNLP file. If {@code null}, main JNLP file's location
     * be used instead
     * @return the JNLPClassLoader of the JNLP file.
     */
    static JNLPClassLoader getLoaderByJnlpFile(final JNLPClassLoader rootClassLoader, URL urlToJnlpFile) {

        if (rootClassLoader == null)
            return null;

        JNLPFile file = rootClassLoader.getJNLPFile();

        if (urlToJnlpFile == null)
            urlToJnlpFile = rootClassLoader.getJNLPFile().getFileLocation();

        if (file.getFileLocation().equals(urlToJnlpFile))
            return rootClassLoader;

        for (JNLPClassLoader loader : rootClassLoader.getLoaders()) {
            if (rootClassLoader != loader) {
                JNLPClassLoader foundLoader = LocateJnlpClassLoader.getLoaderByJnlpFile(loader, urlToJnlpFile);
                if (foundLoader != null)
                    return foundLoader;
            }
        }

        return null;
    }

    /**
     * Locates the JNLPClassLoader of the JNLP file's resource.
     * @param rootClassLoader Root JNLPClassLoader of the application.
     * @param ref Path of the launch or extension JNLP File. If {@code null},
     * main JNLP file's location will be used instead.
     * @param version The version of resource. Is null if no version is specified
     * @return the JNLPClassLoader of the JNLP file's resource.
     */
    static JNLPClassLoader getLoaderByResourceUrl(final JNLPClassLoader rootClassLoader, final URL ref, final String version) {
        Version resourceVersion = (version == null) ? null : new Version(version);

        for (JNLPClassLoader loader : rootClassLoader.getLoaders()) {
            ResourcesDesc resources = loader.getJNLPFile().getResources();

            for (JARDesc eachJar : resources.getJARs()) {
                if (ref.equals(eachJar.getLocation()) &&
                        (resourceVersion == null || resourceVersion.equals(eachJar.getVersion())))
                    return loader;
            }
        }

        for (JNLPClassLoader loader : rootClassLoader.getLoaders()) {
            if (rootClassLoader != loader) {
                JNLPClassLoader foundLoader = LocateJnlpClassLoader.getLoaderByResourceUrl(loader, ref, version);

                if (foundLoader != null)
                    return foundLoader;
            }
        }

        return null;
    }
}
