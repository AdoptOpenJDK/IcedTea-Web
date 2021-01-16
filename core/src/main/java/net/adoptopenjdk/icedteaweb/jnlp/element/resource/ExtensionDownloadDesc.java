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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

package net.adoptopenjdk.icedteaweb.jnlp.element.resource;

import net.adoptopenjdk.icedteaweb.StringUtils;

/**
 * The extension download element.
 */
public class ExtensionDownloadDesc {
    public static final String PART_ATTRIBUTE = "part";
    public static final String EXT_PART_ATTRIBUTE = "ext-part";
    public static final String DOWNLOAD_ATTRIBUTE = "download";

    private final String extPart;
    private final String part;
    private final boolean lazy;


    /**
     * Create an extension download descriptor.
     *
     * @param extPart the name of the part in the extension JNLP
     * @param part    the name of the part in the current JNLP
     * @param lazy    download the extension part lazy
     */
    public ExtensionDownloadDesc(String extPart, String part, boolean lazy) {
        this.extPart = extPart;
        this.part = part;
        this.lazy = lazy && !StringUtils.isBlank(part);
    }

    public String getExtPart() {
        return extPart;
    }

    public String getPart() {
        return part;
    }

    public boolean isLazy() {
        return lazy;
    }
}
