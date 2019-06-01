// Copyright (C) 2001-2003 Jon A. Maxwell (JAM)
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

package net.adoptopenjdk.icedteaweb.jnlp.element.resource;

import net.adoptopenjdk.icedteaweb.jnlp.version.Version;

import java.net.URL;

/**
 * The JAR element.
 *
 * This class is immutable and thread safe
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.6 $
 */
public class JARDesc {
    public static final String VERSION_ATTRIBUTE = "version";
    public static final String MAIN_ATTRIBUTE = "main";
    public static final String DOWNLOAD_ATTRIBUTE = "download";
    public static final String PART_ATTRIBUTE = "part";

    // TODO: missing the jar element attribute "size" (defined since spec version 1.0)

    /** the location of the JAR file */
    private final URL location;

    /** the required JAR versions, or null */
    private final Version version;

    /** the part name */
    private final String part;

    /** whether to load the JAR on demand */
    private final boolean lazy;

    /** whether the JAR contains the main class */
    private final boolean main;

    /** whether the JAR contains native libraries */
    private final boolean nativeJar;

    /** whether the JAR can be cached */
    private final boolean cacheable;

    /**
     * Create a JAR descriptor.
     *
     * @param location the location of the JAR file
     * @param version the required JAR versions, or null
     * @param part the part name, or null
     * @param lazy whether to load the JAR on demand
     * @param main whether the JAR contains the main class
     * @param nativeJar whether the JAR contains native libraries
     * @param cacheable whether the JAR can be cached or not
     */
    public JARDesc(final URL location, final Version version, final String part, final boolean lazy, final boolean main, final boolean nativeJar, final boolean cacheable) {
        this.location = location;
        this.version = version;
        this.part = part;
        this.lazy = lazy;
        this.main = main;
        this.nativeJar = nativeJar;
        this.cacheable = cacheable;
    }

    /**
     * @return the URL of the JAR file.
     */
    public URL getLocation() {
        return location;
    }

    /**
     * @return the required version of the JAR file.
     */
    public Version getVersion() {
        return version;
    }

    /**
     * @return the part name, or null if not specified in the JNLP
     * file.
     */
    public String getPart() {
        return part;
    }

    /**
     * @return true if the JAR file contains native code
     * libraries.
     */
    public boolean isNative() {
        return nativeJar;
    }

    // these both are included in case the spec adds a new value,
    // where !lazy would no longer imply eager.

    /**
     * @return true if the JAR file should be downloaded before
     * starting the application.
     */
    public boolean isEager() {
        return !lazy;
    }

    /**
     * @return true if the JAR file should be downloaded on demand.
     */
    public boolean isLazy() {
        return lazy;
    }

    /**
     * @return true if the JNLP file defined this JAR as containing
     * the main class.  If no JARs were defined as the main JAR then
     * the first JAR should be used to locate the main class.
     *
     * @see ResourcesDesc#getMainJAR
     */
    public boolean isMain() {
        return main;
    }

    /**
     * Returns if this jar is cacheable
     *
     * @return Whether or not this jar is cacheable
     */
    public boolean isCacheable() {
        return cacheable;
    }

}
