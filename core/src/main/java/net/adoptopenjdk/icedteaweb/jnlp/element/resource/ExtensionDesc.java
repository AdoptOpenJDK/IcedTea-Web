// Copyright (C) 2001-2003 Jon A. Maxwell (JAM)
// Copyright (C) 2019 Karakun AG
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

package net.adoptopenjdk.icedteaweb.jnlp.element.resource;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The extension element.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.8 $
 */
public class ExtensionDesc {
    public static final String EXT_DOWNLOAD_ELEMENT = "ext-download";
    public static final String HREF_ATTRIBUTE = "href";
    public static final String DOWNLOAD_ATTRIBUTE = "download";
    public static final String EXT_PART_ATTRIBUTE = "ext-part";

    public static final String NAME_ATTRIBUTE = "name";
    public static final String VERSION_ATTRIBUTE = "version";
    public static final String PART_ATTRIBUTE = "part";

    /** the extension name */
    private final String name;

    /**
     * The required version. The version attribute can specify an exact version or
     * a list of versions (version string). See JSR-56, section 6.4 for details. */
    private final VersionString version;

    /** the location of the extension JNLP file */
    private final URL location;

    /** map from ext-part to local part */
    private final Map<String, String> extToPart = new HashMap<>();

    /** eager ext parts */
    private final List<String> eagerExtParts = new ArrayList<>();

    /**
     * Create an extension descriptor.
     *
     * @param name the extension name
     * @param version the required version of the JNLP file extension
     * @param location the location of the JNLP file extension
     */
    public ExtensionDesc(String name, VersionString version, URL location) {
        this.name = name;
        this.version = version;
        this.location = location;
    }

    /**
     * Adds an extension part to be downloaded when the specified
     * part of the main JNLP file is loaded.  The extension part
     * will be downloaded before the application is launched if the
     * lazy value is false or the part is empty or null.
     *
     * @param extPart the part name in the extension file
     * @param part the part name in the main file
     * @param lazy whether to load the part before launching
     */
    public void addPart(String extPart, String part, boolean lazy) {
        extToPart.put(extPart, part);

        if (!lazy || part == null || part.length() == 0)
            eagerExtParts.add(extPart);
    }

    /**
     * @return the name of the extension.
     */
    public String getName() {
        return name;
    }

    /**
     * @return the required version of the extension JNLP file.
     */
    public VersionString getVersion() {
        return version;
    }

    /**
     * @return the location of the extension JNLP file.
     */
    public URL getLocation() {
        return location;
    }
}
