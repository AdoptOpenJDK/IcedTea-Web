/* ManageJnlpResources.java
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

package net.sourceforge.jnlp.runtime.classloader;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.ResourcesDesc;
import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.sourceforge.jnlp.runtime.classloader.JNLPClassLoader.DownloadAction;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ManageJnlpResources {

    /**
     * Returns jars from the JNLP file with the part name provided.
     * @param rootClassLoader Root JNLPClassLoader of the application.
     * @param ref Path of the launch or extension JNLP File containing the
     * resource. If null, main JNLP's file location will be used instead.
     * @param part The name of the part.
     * @param version version of jar
     * @return jars found.
     */
    public static JARDesc[] findJars(final ClassLoader rootClassLoader, final URL ref, final String part, final VersionString version) {
        final JNLPClassLoader jnlpClassLoader = assertJnlpClassLoader(rootClassLoader);

        final JNLPClassLoader foundLoader = LocateJnlpClassLoader.getLoaderByJnlpFile(jnlpClassLoader, ref);

        if (foundLoader != null) {
            final List<JARDesc> foundJars = new ArrayList<>();
            final ResourcesDesc resources = foundLoader.getJNLPFile().getResources();

            for (final JARDesc aJar : resources.getJARs(part)) {
                if (Objects.equals(version, aJar.getVersion()))
                    foundJars.add(aJar);
            }

            return foundJars.toArray(new JARDesc[foundJars.size()]);
        }

        return new JARDesc[] {};
    }

    /**
     * Removes jars from cache.
     * @param classLoader JNLPClassLoader of the application that is associated to the resource.
     * @param ref Path of the launch or extension JNLP File containing the
     * resource. If null, main JNLP's file location will be used instead.
     * @param jars Jars marked for removal.
     */
    public static void removeCachedJars(final ClassLoader classLoader, final URL ref, final JARDesc[] jars) {
        final JNLPClassLoader jnlpClassLoader = assertJnlpClassLoader(classLoader);
        JNLPClassLoader foundLoader = LocateJnlpClassLoader.getLoaderByJnlpFile(jnlpClassLoader, ref);

        if (foundLoader != null)
            foundLoader.removeJars(jars);
    }

    /**
     * Downloads jars identified by part name.
     * @param classLoader JNLPClassLoader of the application that is associated to the resource.
     * @param ref Path of the launch or extension JNLP File containing the
     * resource. If null, main JNLP's file location will be used instead.
     * @param part The name of the path.
     * @param version version of jar to be downloaded
     */
    public static void downloadJars(final ClassLoader classLoader, final URL ref, final String part, final VersionString version) {
        final JNLPClassLoader jnlpClassLoader = assertJnlpClassLoader(classLoader);
        final JNLPClassLoader foundLoader = LocateJnlpClassLoader.getLoaderByJnlpFile(jnlpClassLoader, ref);

        if (foundLoader != null) {
            foundLoader.initializeNewJarDownload(ref, part, version);
        }
    }

    /**
     * Downloads and initializes resources which are not mentioned in the jnlp file.
     * Used by DownloadService.
     * @param rootClassLoader Root JNLPClassLoader of the application.
     * @param ref Path to the resource.
     * @param version The version of resource. If null, no version is specified.
     */

    public static void loadExternalResourceToCache(final ClassLoader rootClassLoader, final URL ref, final String version) {
        assertJnlpClassLoader(rootClassLoader).manageExternalJars(ref, version, DownloadAction.DOWNLOAD_TO_CACHE);
    }

    /**
     * Removes resource which are not mentioned in the jnlp file.
     * Used by DownloadService.
     * @param rootClassLoader Root JNLPClassLoader of the application.
     * @param ref Path to the resource.
     * @param version The version of resource. If null, no version is specified.
     */
    public static void removeExternalCachedResource(final ClassLoader rootClassLoader, final URL ref, final String version) {
        assertJnlpClassLoader(rootClassLoader).manageExternalJars(ref, version, DownloadAction.REMOVE_FROM_CACHE);
    }

    /**
     * Returns {@code true} if the resource (not mentioned in the jnlp file) is cached, otherwise {@code false}
     * Used by DownloadService.
     * @param rootClassLoader Root {@link JNLPClassLoader} of the application.
     * @param ref Path to the resource.
     * @param version The version of resource. If {@code null}, no version is specified.
     * @return {@code true} if the external resource is cached, otherwise {@code false}
     */
    public static boolean isExternalResourceCached(final ClassLoader rootClassLoader, final URL ref, final String version) {
        return assertJnlpClassLoader(rootClassLoader).manageExternalJars(ref, version, DownloadAction.CHECK_CACHE);
    }

    private static JNLPClassLoader assertJnlpClassLoader(final ClassLoader classLoader) {
        Assert.requireNonNull(classLoader, "classLoader");
        if(classLoader instanceof JNLPClassLoader) {
            return (JNLPClassLoader) classLoader;
        }
        throw new IllegalArgumentException("The given classloader of type " + classLoader.getClass() + " is not a " + JNLPClassLoader.class);
    }
}
