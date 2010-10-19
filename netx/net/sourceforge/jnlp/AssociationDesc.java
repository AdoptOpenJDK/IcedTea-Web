// Copyright (C) 2009 Red Hat, Inc.
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

package net.sourceforge.jnlp;

public final class AssociationDesc {

    /** the extensions this application wants to register with */
    private String[] extensions;

    /** the mime type for the association */
    private String mimeType;

    public AssociationDesc(String mimeType, String[] extensions) throws ParseException {
        checkMimeType(mimeType);
        this.mimeType = mimeType;
        this.extensions = extensions;
    }

    /**
     * Return the extensions for this association
     */
    public String[] getExtensions() {
        return extensions;
    }

    /**
     * Return the mimetype for this association
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
