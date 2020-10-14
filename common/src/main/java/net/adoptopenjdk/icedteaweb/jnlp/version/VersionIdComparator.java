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
package net.adoptopenjdk.icedteaweb.jnlp.version;

import net.adoptopenjdk.icedteaweb.Assert;

import java.util.Comparator;

/**
 * Comparator to compare two version-ids in the context of a version-string.
 * <p>
 * If two or more version-id match the given version-string,
 * the JNLP Client should use the one matching the earlier version-range in the version-string.
 * <p>
 * If two or more version-id match a given version-range,
 * the JNLP Client should use the one with the highest version-id.
 * <p>
 * See JSR-56 Specification, Appendix A.
 */
public class VersionIdComparator implements Comparator<VersionId> {

    private final VersionString versionString;

    public VersionIdComparator(final VersionString versionString) {
        this.versionString = Assert.requireNonNull(versionString, "versionString");
    }

    @Override
    public int compare(final VersionId o1, final VersionId o2) {
        return versionString.compare(o1, o2);
    }
}
