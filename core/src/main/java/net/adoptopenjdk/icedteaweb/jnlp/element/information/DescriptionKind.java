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

import java.util.Arrays;
import net.adoptopenjdk.icedteaweb.Assert;
import net.sourceforge.jnlp.Parser;

/**
 * The kind attribute defines how a description should be used.
 *
 * Only one description element of each kind can be specified. A description element without a kind {@link #DEFAULT}
 * is used as a default value.
 *
 * @implSpec See <b>JSR-56, Section 3.5 Descriptor Information</b>
 * for a detailed specification of this class.
 */
public enum DescriptionKind {
    /**
     * Use this DescriptionKind if the description of the application is going to appear in one row in a
     * list or a table.
     */
    ONE_LINE("one-line"),
    /**
     * Use this DescriptionKind if the description of the application is going to be displayed in a situation
     * where there is room for a paragraph.
     */
    SHORT("short"),
    /**
     * Use this DescriptionKind if the description of the application is intended to be used as a tooltip.
     */
    TOOLTIP("tooltip"),
    /**
     * Use this DescriptionKind if the description of the application does not specify a specific usage (Default).
     */
    DEFAULT("default");


    private final String value;

    DescriptionKind(final String value) {
        this.value = value;
    }

    /**
     * The attribute value name as used in the JSR-56 specification or the {@link Parser}.
     *
     * @return the attribute value name
     */
    public String getValue() {
        return value;
    }

    /**
     * Creates an enum value from the given string
     * @param name the string name
     * @return an enum value for the given string
     * @throws IllegalArgumentException of there is no enum value for the given name
     */
    public static DescriptionKind fromString(String name) throws IllegalArgumentException {
        Assert.requireNonNull(name, "name");

        return Arrays.stream(DescriptionKind.values()).filter(kind -> kind.value.equals(name)).findAny().orElseThrow(
                () -> new IllegalArgumentException("No enum constant " + DescriptionKind.class.getCanonicalName() + "." + name)
        );
    }
}
