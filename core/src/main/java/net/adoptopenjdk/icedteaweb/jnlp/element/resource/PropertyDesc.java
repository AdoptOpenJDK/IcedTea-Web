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

import net.sourceforge.jnlp.LaunchException;

import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;

/**
 * The property element describes a name/value pair that is available to the launched application
 * as a system property.
 *
 * @implSpec See <b>JSR-56, Section 4.2 Setting System Properties</b>
 * for a detailed specification of this class.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.6 $
 */
public class PropertyDesc {

    public static final String NAME_ATTRIBUTE = "name";
    public static final String VALUE_ATTRIBUTE = "value";

    /**
     *
     * @param prop - the property to be parsed from format key=value
     * @return new PropertyDesc based on parsed key=value, though composed from key and value
     * @throws net.sourceforge.jnlp.LaunchException if creations fails
     */
    public static PropertyDesc fromString(final String prop) throws LaunchException {
        // allows empty property, not sure about validity of that.
        int equals = prop.indexOf("=");
        if (equals == -1) {
            throw new LaunchException("Incorrect property format" + prop + " (should be key=value): ");
        }
        final String key = prop.substring(0, equals);
        final String value = prop.substring(equals + 1, prop.length());

        return new PropertyDesc(key, value);

    }

    /** the key name */
    final private String key;

    /** the value */
    final private String value;

    /**
     * Creates a property descriptor.
     *
     * @param key the key name
     * @param value the value
     */
    public PropertyDesc(final String key, final String value) {
        this.key = key;
        this.value = value;
    }

    /**
     * @return the property's key
     */
    public String getKey() {
        return key;
    }

    /**
     * @return the property's value
     */
    public String getValue() {
        return value;
    }

}
