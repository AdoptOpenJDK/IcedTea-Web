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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VersionIdTest {

    @Test
    public void testValidNumericVersionIds() {
        assertEquals("1", versionId("1").toString());
        assertEquals("1.1", versionId("1.1").toString());
        assertEquals("1.1.0", versionId("1.1.0").toString());
        assertEquals("1.2.2-001", versionId("1.2.2-001").toString());
        assertEquals("1.3.0-rc2-w", versionId("1.3.0-rc2-w").toString());
        assertEquals("1.2.3_build42", versionId("1.2.3_build42").toString());
        assertEquals("1.3.0-SNAPSHOT", versionId("1.3.0-SNAPSHOT").toString());
        assertEquals("15.2.2_21.05.2019_11:43:34", versionId("15.2.2_21.05.2019_11:43:34").toString());
        assertEquals("15.2.2_2019.05.21T11:43:34", versionId("15.2.2_2019.05.21T11:43:34").toString());
    }

    @Test
    public void testValidAlphaNumericVersionIds() {
        assertEquals("A", versionId("A").toString());
        assertEquals("A.B", versionId("A.B").toString());
        assertEquals("A.B.1", versionId("A.B.1").toString());
        assertEquals("A.B-1", versionId("A.B-1").toString());
        assertEquals("1_3_0-rc2-w", versionId("1_3_0-rc2-w").toString());
    }

    @Test(expected = NullPointerException.class)
    public void testNullVersionId() {
        versionId(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyVersionId() {
        versionId("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVersionIdWithInvalidSpaceChar() {
        versionId("1.0 beta");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVersionIdWithInvalidAmpersandChar() {
        versionId("1&1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVersionIdWithInvalidAsteriskModifierChar() {
        versionId("1.0.0*");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVersionIdWithInvalidAsteriskModifierChar2() {
        versionId("1.0.0*-build42");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVersionIdWithInvalidPlusModifierChar() {
        versionId("1.0.0-buildWithC+");
    }

    @Test
    public void testMatches() {
        assertTrue(versionId("1.0").isEqualTo(versionId("1")));
        assertTrue(versionId("1-0").isEqualTo(versionId("1")));
        assertTrue(versionId("1_0").isEqualTo(versionId("1")));
        assertTrue(versionId("1").isEqualTo(versionId("1.0")));
        assertTrue(versionId("1.0").isEqualTo(versionId("1.0")));
        assertTrue(versionId("1.0").isEqualTo(versionId("1.0.0-0")));
        assertTrue(versionId("1.0.0_0").isEqualTo(versionId("1.0.0")));
        assertTrue(versionId("1.3").isEqualTo(versionId("1.3.0")));
        assertTrue(versionId("1.3.0").isEqualTo(versionId("1.3")));
        assertTrue(versionId("1.2.2.4").isEqualTo(versionId("1.2.2-004")));

        // not a match
        assertFalse(versionId("1.0.4").isEqualTo(versionId("1.0")));
        assertFalse(versionId("1.0-build42").isEqualTo(versionId("1.0.0-build42")));
        assertFalse(versionId("1.0-b42").isEqualTo(versionId("1.0-B42")));
    }

    @Test
    public void testIsPrefixMatchOf() {
        assertTrue(versionId("1.0").isPrefixMatchOf(versionId("1.0.0")));
        assertTrue(versionId("2.0").isPrefixMatchOf(versionId("2.0.1")));
        assertTrue(versionId("1.4").isPrefixMatchOf(versionId("1.4.6")));
        assertTrue(versionId("1.4.3").isPrefixMatchOf(versionId("1.4.3-009")));
        assertTrue(versionId("1.2.1").isPrefixMatchOf(versionId("1.2.1-004")));
        assertTrue(versionId("1.2.0.0").isPrefixMatchOf(versionId("1.2")));
        assertTrue(versionId("1.2.2.4").isPrefixMatchOf(versionId("1.2.2-004_beta")));
        // no prefix match
        assertFalse(versionId("1").isPrefixMatchOf(versionId("2.1.0")));
        assertFalse(versionId("1.5").isPrefixMatchOf(versionId("1.6")));
        assertFalse(versionId("1.5").isPrefixMatchOf(versionId("2.0.0")));
        assertFalse(versionId("1.2").isPrefixMatchOf(versionId("1.3")));
        assertFalse(versionId("1.2.1").isPrefixMatchOf(versionId("1.2.10")));
    }

    @Test
    public void testIsLessThan() {
        // greater than
        assertTrue(versionId("1").isLessThan(versionId("2")));
        assertTrue(versionId("1").isLessThan(versionId("1.1")));
        assertTrue(versionId("1.0").isLessThan(versionId("1.1")));
        assertTrue(versionId("1.1.0").isLessThan(versionId("1.1.1")));
        assertTrue(versionId("1.1").isLessThan(versionId("1.1.1")));
        assertTrue(versionId("1.0.0-build42").isLessThan(versionId("1.0.1")));
        assertTrue(versionId("1.4.2").isLessThan(versionId("1.4.5")));

        // numeric elements have lower precedence than non-numeric elements
        assertTrue(versionId("1.0.1").isLessThan(versionId("1.0.A")));
        assertTrue(versionId("1.0.B").isLessThan(versionId("1.1.A")));
        assertTrue(versionId("1.0.A").isLessThan(versionId("1.0.ABC")));
        assertTrue(versionId("1.0.0-build41").isLessThan(versionId("1.0.0-build42")));
        assertTrue(versionId("1.0.0-42").isLessThan(versionId("1.0.0-build42")));

        // not greater than
        assertFalse(versionId("1.0").isLessThan(versionId("1")));
        assertFalse(versionId("1.0").isLessThan(versionId("1.0")));
        assertFalse(versionId("1.1").isLessThan(versionId("1.0")));
        assertFalse(versionId("1.1").isLessThan(versionId("1.0.2")));
        assertFalse(versionId("1.0.1").isLessThan(versionId("1.0.0-build42")));
    }

    @Test
    public void testVersionIdAsTuple() {
        assertTuples(versionId("1.3.0-rc2-w").asTuple(), "1", "3", "0", "rc2", "w");
        assertTuples(versionId("1_2_3_build42").asTuple(), "1", "2", "3", "build42");
        assertTuples(versionId("A-B_C.D").asTuple(), "A", "B", "C", "D");

        assertTuples(versionId("1.2.3").asTuple(), "1", "2", "3");
        assertTuples(versionId("1.2-build42").asTuple(), "1", "2", "build42");
    }

    @Test
    public void testVersionIdAsNormalizedTuple() {
        assertTuples(versionId("1").asNormalizedTuple(3), "1", "0", "0");
        assertTuples(versionId("1.1.1").asNormalizedTuple(3), "1", "1", "1");
        assertTuples(versionId("1.2").asNormalizedTuple(5), "1", "2", "0", "0", "0");
        assertTuples(versionId("1.2.3-build42").asNormalizedTuple(5), "1", "2", "3", "build42", "0");

        assertTuples(versionId("1.2").asNormalizedTuple(1), "1", "2");
        assertTuples(versionId("1.2").asNormalizedTuple(2), "1", "2");
        assertTuples(versionId("1.2").asNormalizedTuple(3), "1", "2", "0");
    }

    private VersionId versionId(String s) {
        return VersionId.fromString(s);
    }

    private void assertTuples(String[] tuples, String... expected) {
        assertArrayEquals(expected, tuples);
    }
}
