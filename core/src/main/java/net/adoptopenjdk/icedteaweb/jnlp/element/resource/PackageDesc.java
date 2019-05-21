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

import java.util.Objects;

/**
 * The package element can be used to indicate to the JNLP Client which packages are implemented in which JAR files.
 *
 * @implSpec See <b>JSR-56, Section 4.5 Package Element</b>
 * for a detailed specification of this class.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.6 $
 */
public class PackageDesc {
    public static final String NAME_ATTRIBUTE = "name";
    public static final String PART_ATTRIBUTE = "part";
    public static final String RECURSIVE_ATTRIBUTE = "recursive";

    private static final String ASTERIX_SUFFIX = ".*";
    /** the package name */
    private final String name;

    /** the part required by the package */
    private final String part;

    /** whether the package includes subpackages */
    private final boolean recursive;

    /**
     * Create a package descriptor.
     *
     * @param name the package name
     * @param part the part required by the package
     * @param recursive whether the package includes subpackages
     */
    public PackageDesc(final String name, final String part, final boolean recursive) {
        this.name = Objects.requireNonNull(name);
        this.part = part;
        this.recursive = recursive;
    }

    /**
     * @return whether the specified class is part of this package.
     *
     * @param className the fully qualified class name

     */
    public boolean matches(final String className) {
        // form 1: exact class
        if (Objects.equals(name, className)) {
            return true;
        }
        // form 2: package.*
        Objects.requireNonNull(className);
        if (name.endsWith(ASTERIX_SUFFIX)) {
            final String pkName = name.substring(0, name.length() - 1);
            if (className.startsWith(pkName)) {
                String postfix = className.substring(pkName.length() + 1);
                return recursive || !postfix.contains(".");
            }
        }
        return false;
    }

    /**
     * @return the package name.
     */
    public String getName() {
        return name;
    }

    /**
     * @return the part name.
     */
    public String getPart() {
        return part;
    }

    /**
     * @return whether subpackages should be matched by this
     * package.
     */
    public boolean isRecursive() {
        return recursive;
    }

}
