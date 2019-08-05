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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class VersionIdComparatorTest {

    @Test
    public void testComparingOfEqualVersionIds() {
        final VersionIdComparator comparator = comparator("1*");

        assertEquals(0, comparator.compare(versionId("1.0"), versionId("1")));
        assertEquals(0, comparator.compare(versionId("1-0"), versionId("1")));
        assertEquals(0, comparator.compare(versionId("1_0"), versionId("1")));
        assertEquals(0, comparator.compare(versionId("1"), versionId("1.0")));
        assertEquals(0, comparator.compare(versionId("1.0"), versionId("1.0")));
        assertEquals(0, comparator.compare(versionId("1.0"), versionId("1.0.0-0")));
        assertEquals(0, comparator.compare(versionId("1.0.0_0"), versionId("1.0.0")));
        assertEquals(0, comparator.compare(versionId("1.3"), versionId("1.3.0")));
        assertEquals(0, comparator.compare(versionId("1.3.0"), versionId("1.3")));
        assertEquals(0, comparator.compare(versionId("1.2.2.4"), versionId("1.2.2-004")));
    }

    @Test
    public void testComparingOfUnequalVersionIdsContainedInRang() {
        final VersionIdComparator comparator = comparator("1*");

        // less than
        assertLessThan(comparator, "1", "1.1");
        assertLessThan(comparator, "1.0", "1.1");
        assertLessThan(comparator, "1.1.0", "1.1.1");
        assertLessThan(comparator, "1.1", "1.1.1");
        assertLessThan(comparator, "1.4.2", "1.4.5");
        assertLessThan(comparator, "1.0.2", "1.1");

        // greater than
        assertGreaterThan(comparator, "1.1", "1");
        assertGreaterThan(comparator, "1.1", "1.0");
        assertGreaterThan(comparator, "1.1.1", "1.1.0");
        assertGreaterThan(comparator, "1.1.1", "1.1");
        assertGreaterThan(comparator, "1.4.5", "1.4.1");
        assertGreaterThan(comparator, "1.1", "1.0.2");

        // numeric elements have lower precedence than non-numeric elements
        assertLessThan(comparator, "1.0.1", "1.0.A");
        assertLessThan(comparator, "1.0.A", "1.0.B");
        assertLessThan(comparator, "1.0.B", "1.1.A");
        assertLessThan(comparator, "1.0.A", "1.0.ABC");
        assertLessThan(comparator, "1.0.0-build42", "1.0.1");
        assertLessThan(comparator, "1.0.0-build41", "1.0.0-build42");
        assertLessThan(comparator, "1.0.0-42", "1.0.0-build42");

        assertGreaterThan(comparator, "1.0.A", "1.0.1");
        assertGreaterThan(comparator, "1.0.B", "1.0.A");
        assertGreaterThan(comparator, "1.1.A", "1.0.B");
        assertGreaterThan(comparator, "1.0.ABC", "1.0.A");
        assertGreaterThan(comparator, "1.0.1", "1.0.0-build42");
        assertGreaterThan(comparator, "1.0.0-build42", "1.0.0-build41");
        assertGreaterThan(comparator, "1.0.0-build42", "1.0.0-42");
    }

    @Test
    public void testComparingOfUnequalVersionIdsNotContainedInRang() {
        final VersionIdComparator comparator = comparator("XXX*");

        // less than
        assertLessThan(comparator, "1", "2");
        assertLessThan(comparator, "1", "1.1");
        assertLessThan(comparator, "1.0", "1.1");
        assertLessThan(comparator, "1.1.0", "1.1.1");
        assertLessThan(comparator, "1.1", "1.1.1");
        assertLessThan(comparator, "1.4.2", "1.4.5");
        assertLessThan(comparator, "1.0.2", "1.1");

        // greater than
        assertGreaterThan(comparator, "2", "1");
        assertGreaterThan(comparator, "1.1", "1");
        assertGreaterThan(comparator, "1.1", "1.0");
        assertGreaterThan(comparator, "1.1.1", "1.1.0");
        assertGreaterThan(comparator, "1.1.1", "1.1");
        assertGreaterThan(comparator, "1.4.5", "1.4.1");
        assertGreaterThan(comparator, "1.1", "1.0.2");

        // numeric elements have lower precedence than non-numeric elements
        assertLessThan(comparator, "1.0.1", "1.0.A");
        assertLessThan(comparator, "1.0.A", "1.0.B");
        assertLessThan(comparator, "1.0.B", "1.1.A");
        assertLessThan(comparator, "1.0.A", "1.0.ABC");
        assertLessThan(comparator, "1.0.0-build42", "1.0.1");
        assertLessThan(comparator, "1.0.0-build41", "1.0.0-build42");
        assertLessThan(comparator, "1.0.0-42", "1.0.0-build42");

        assertGreaterThan(comparator, "1.0.A", "1.0.1");
        assertGreaterThan(comparator, "1.0.B", "1.0.A");
        assertGreaterThan(comparator, "1.1.A", "1.0.B");
        assertGreaterThan(comparator, "1.0.ABC", "1.0.A");
        assertGreaterThan(comparator, "1.0.1", "1.0.0-build42");
        assertGreaterThan(comparator, "1.0.0-build42", "1.0.0-build41");
        assertGreaterThan(comparator, "1.0.0-build42", "1.0.0-42");
    }

    @Test
    public void testComparingVersionIdsWhereOnlyOneIsContainedInRange() {
        final VersionIdComparator comparator = comparator("3");

        assertLessThan(comparator, "1", "3");
        assertLessThan(comparator, "2", "3");
        assertLessThan(comparator, "4", "3");
        assertLessThan(comparator, "5", "3");

        assertGreaterThan(comparator, "3", "1");
        assertGreaterThan(comparator, "3", "2");
        assertGreaterThan(comparator, "3", "4");
        assertGreaterThan(comparator, "3", "5");
    }

    @Test
    public void testComparingVersionIdsWhichAreContainedInDifferentPartsOfTheRange() {
        final VersionIdComparator comparator = comparator("A 1 B 2");

        assertGreaterThan(comparator, "1", "2");
        assertLessThan(comparator, "1", "A");
        assertGreaterThan(comparator, "1", "B");

        assertLessThan(comparator, "2", "1");
        assertLessThan(comparator, "2", "A");
        assertLessThan(comparator, "2", "B");

        assertGreaterThan(comparator, "A", "1");
        assertGreaterThan(comparator, "A", "2");
        assertGreaterThan(comparator, "A", "B");

        assertLessThan(comparator, "B", "1");
        assertGreaterThan(comparator, "B", "2");
        assertLessThan(comparator, "B", "A");
    }

    @Test
    public void testComparingVersionIdsWhichAreContainedInDifferentOrTheSamePartOfTheRange() {
        final VersionIdComparator comparator = comparator("A* 1* B* 2*");

        assertGreaterThan(comparator, "1", "2.2");
        assertLessThan(comparator, "1", "A.A");
        assertGreaterThan(comparator, "1", "B.B");
        assertLessThan(comparator, "1", "1.1");

        assertLessThan(comparator, "2", "1.1");
        assertLessThan(comparator, "2", "2.2");
        assertLessThan(comparator, "2", "A.A");
        assertLessThan(comparator, "2", "B.B");

        assertGreaterThan(comparator, "A", "1.1");
        assertGreaterThan(comparator, "A", "2.2");
        assertLessThan(comparator, "A", "A.A");
        assertGreaterThan(comparator, "A", "B.B");

        assertLessThan(comparator, "B", "1.1");
        assertLessThan(comparator, "B", "B.B");
        assertGreaterThan(comparator, "B", "2.2");
        assertLessThan(comparator, "B", "A.A");
    }

    private void assertGreaterThan(VersionIdComparator comparator, String versionId1, String versionId2) {
        assertTrue(comparator.compare(versionId(versionId1), versionId(versionId2)) > 0);
    }

    private void assertLessThan(VersionIdComparator comparator, String versionId1, String versionId2) {
        assertTrue(comparator.compare(versionId(versionId1), versionId(versionId2)) < 0);
    }

    private VersionIdComparator comparator(String s) {
        return new VersionIdComparator(VersionString.fromString(s));
    }

    private VersionId versionId(String s) {
        return VersionId.fromString(s);
    }

}
