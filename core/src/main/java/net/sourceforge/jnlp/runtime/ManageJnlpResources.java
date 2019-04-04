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

package net.sourceforge.jnlp.runtime;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.jnlp.JARDesc;
import net.sourceforge.jnlp.ResourcesDesc;
import net.sourceforge.jnlp.Version;
import net.sourceforge.jnlp.runtime.JNLPClassLoader.DownloadAction;

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
    public static JARDesc[] findJars(final JNLPClassLoader rootClassLoader, final URL ref, final String part, final Version version) {
        JNLPClassLoader foundLoader = LocateJnlpClassLoader.getLoaderByJnlpFile(rootClassLoader, ref);

        if (foundLoader != null) {
            List<JARDesc> foundJars = new ArrayList<>();
            ResourcesDesc resources = foundLoader.getJNLPFile().getResources();

            for (JARDesc eachJar : resources.getJARs(part)) {
                if (version == null || version.equals(eachJar.getVersion()))
                    foundJars.add(eachJar);
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
    public static void removeCachedJars(final JNLPClassLoader classLoader, final URL ref, final JARDesc[] jars) {
        JNLPClassLoader foundLoader = LocateJnlpClassLoader.getLoaderByJnlpFile(classLoader, ref);

        if (foundLoader != null)
            foundLoader.removeJars(jars);
    }

    /**
     * Downloads jars identified by part name.
     * @param classLoader JNLPClassLoader of the application that is associated to the resource.
     * @param ref Path of the launch or extension JNLP File containing the
     * resource. If null, main JNLP's file location will be used instead.
     * @param part The name of the path.
     * @param version version of jar to be downlaoded
     */
    public static void downloadJars(final JNLPClassLoader classLoader, final URL ref, final String part, final Version version) {
        JNLPClassLoader foundLoader = LocateJnlpClassLoader.getLoaderByJnlpFile(classLoader, ref);

        if (foundLoader != null)
            foundLoader.initializeNewJarDownload(ref, part, version);
    }

    /**
     * Downloads and initializes resources which are not mentioned in the jnlp file.
     * Used by DownloadService.
     * @param rootClassLoader Root JNLPClassLoader of the application.
     * @param ref Path to the resource.
     * @param version The version of resource. If null, no version is specified.
     */

    public static void loadExternalResouceToCache(final JNLPClassLoader rootClassLoader, final URL ref, final String version) {
        rootClassLoader.manageExternalJars(ref, version, DownloadAction.DOWNLOAD_TO_CACHE);
    }

    /**
     * Removes resource which are not mentioned in the jnlp file.
     * Used by DownloadService.
     * @param rootClassLoader Root JNLPClassLoader of the application.
     * @param ref Path to the resource.
     * @param version The version of resource. If null, no version is specified.
     */
    public static void removeExternalCachedResource(final JNLPClassLoader rootClassLoader, final URL ref, final String version) {
        rootClassLoader.manageExternalJars(ref, version, DownloadAction.REMOVE_FROM_CACHE);
    }

    /**
     * Returns {@code true} if the resource (not mentioned in the jnlp file) is cached, otherwise {@code false}
     * Used by DownloadService.
     * @param rootClassLoader Root {@link JNLPClassLoader} of the application.
     * @param ref Path to the resource.
     * @param version The version of resource. If {@code null}, no version is specified.
     * @return {@code true} if the external resource is cached, otherwise {@code false}
     */
    public static boolean isExternalResourceCached(final JNLPClassLoader rootClassLoader, final URL ref, final String version) {
        return rootClassLoader.manageExternalJars(ref, version, DownloadAction.CHECK_CACHE);
    }

}
