// Copyright (C) 2009 Red Hat, Inc.
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

package net.adoptopenjdk.icedteaweb.jnlp.element.information;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.xmlparser.ParseException;

/**
 * The optional association element is a hint to the JNLP client that it wishes to be registered with the
 * operating system as the primary handler of certain extensions and a certain mime-type. The association
 * element must have the extensions and mime-type attributes, and may contain the two optional sub elements
 * description, and icon.
 *
 * @implSpec See <b>JSR-56, Section 3.5 Descriptor Information</b>
 * for a detailed specification of this class.
 */
public final class AssociationDesc {
    public static final String ASSOCIATION_ELEMENT = "association";
    public static final String EXTENSIONS_ATTRIBUTE = "extensions";
    public static final String MIME_TYPE_ATTRIBUTE = "mime-type";

    /** the extensions this application wants to register with */
    private final String[] extensions;

    /** the mime type for the association */
    private final String mimeType;

    // TODO: optional description element according to JSR
    // TODO: optional icon element according to JSR

    public AssociationDesc(final String mimeType, final String[] extensions) throws ParseException {
        Assert.requireNonNull(mimeType, "mimeType");
        Assert.requireNonNull(extensions, "extensions");

        checkMimeType(mimeType);
        this.mimeType = mimeType;
        this.extensions = extensions;
    }

    /**
     * @return the extensions for this association
     */
    public String[] getExtensions() {
        return extensions;
    }

    /**
     * @return the MIME type for this association
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Check for valid mimeType
     * @param mimeType a mime type
     * @throws ParseException if mimeType is an invalid MIME type
     */
    private void checkMimeType(String mimeType) throws ParseException {
        // TODO check that mime type is valid
    }

}
