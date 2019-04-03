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

package net.sourceforge.jnlp;

import static net.sourceforge.jnlp.runtime.Translator.R;

/**
 * The property element.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.6 $
 */
public class PropertyDesc {

    /**
     * 
     * @param prop - the property to be parsed from format key=value
     * @return new PropertyDesc based on parsed key=value, though composed from key and value
     * @throws net.sourceforge.jnlp.LaunchException if creations fails
     */
    public static PropertyDesc fromString(String prop) throws LaunchException {
        // allows empty property, not sure about validity of that.
        int equals = prop.indexOf("=");
        if (equals == -1) {
            throw new LaunchException(R("BBadProp", prop));
        }
        String key = prop.substring(0, equals);
        String value = prop.substring(equals + 1, prop.length());

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
    public PropertyDesc(String key, String value) {
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
