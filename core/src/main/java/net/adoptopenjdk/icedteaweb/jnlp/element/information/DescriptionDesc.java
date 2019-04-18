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
//
package net.adoptopenjdk.icedteaweb.jnlp.element.information;

/**
 * A short statement about the application. Description elements are optional.
 * The kind attribute defines how the description should be used. All descriptions
 * contain plain text. No formatting, such as HTML tags is supported.
 *
 * The kind attribute for the description element indicates the use of a description
 * element. The values are: i) one-line, for a one-line description, ii) short,
 * for a one paragraph description, and iii) tooltip, for a tool-tip description.
 *
 * @see DescriptionKind
 *
 * @implSpec See <b>JSR-56, Section 3.5 Descriptor Information</b>
 * for a detailed specification of this class.
 */
public class DescriptionDesc {
    public static final String DESCRIPTION_ELEMENT = "description";
    public static final String KIND_ATTRIBUTE = "kind";

    // TODO currently used to hold the constants, us this desc to fully represent the description elements
}
