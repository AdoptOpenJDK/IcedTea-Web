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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VersionRangeTest {
    @Test
    public void testExactVersionRangesParsingAndToString() {
        // numerical
        assertEquals("1", versionRange("1").toString());
        assertEquals("1.1", versionRange("1.1").toString());
        assertEquals("1.1.0", versionRange("1.1.0").toString());
        assertEquals("1.2.2-001", versionRange("1.2.2-001").toString());
        assertEquals("1.3.0-rc2-w", versionRange("1.3.0-rc2-w").toString());
        assertEquals("1_3_0-rc2-w", versionRange("1_3_0-rc2-w").toString());
        assertEquals("1.2.3_build42", versionRange("1.2.3_build42").toString());
        assertEquals("1.3.0-SNAPSHOT", versionRange("1.3.0-SNAPSHOT").toString());
        assertEquals("15.2.2_21.05.2019_11:43:34", versionRange("15.2.2_21.05.2019_11:43:34").toString());
        assertEquals("15.2.2_2019.05.21T11:43:34", versionRange("15.2.2_2019.05.21T11:43:34").toString());

        // alphabetical
        assertEquals("A", versionRange("A").toString());
        assertEquals("A.B", versionRange("A.B").toString());
        assertEquals("A.B.1", versionRange("A.B.1").toString());
        assertEquals("A.B-1", versionRange("A.B-1").toString());
    }

    @Test
    public void testVersionRangeWithModifiersParsingAndToString() {
        assertEquals("1+", versionRange("1+").toString());
        assertEquals("1*", versionRange("1*").toString());
        assertEquals("1.1.1+", versionRange("1.1.1+").toString());
        assertEquals("1.1.1*", versionRange("1.1.1*").toString());
        assertEquals("1.2.2-001+", versionRange("1.2.2-001+").toString());
        assertEquals("1.2.4_02+", versionRange("1.2.4_02+").toString());
        assertEquals("1.3.0-rc2-w+", versionRange("1.3.0-rc2-w+").toString());
        assertEquals("1.2.3_build42+", versionRange("1.2.3_build42+").toString());
        assertEquals("1.2.3_build42*", versionRange("1.2.3_build42*").toString());
    }

    @Test
    public void testCompoundVersionRangeParsingAndToString() {
        assertEquals("1.4&1.4.1_02", versionRange("1.4&1.4.1_02").toString());
        assertEquals("1.4*&1.4.1_02+", versionRange("1.4*&1.4.1_02+").toString());
        assertEquals("1.4+&1.4.1", versionRange("1.4+&1.4.1").toString());
    }

    @Test(expected = NullPointerException.class)
    public void testNullVersionRange() {
        versionRange(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyVersionRange() {
        versionRange("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVersionRangeWithInvalidSpaceChar() {
        versionRange("1.0 beta");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVersionRangeWithInvalidAmpersandChar() {
        versionRange("1.0.0-&");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVersionRangeWithInvalidAsteriskModifierChar() {
        versionRange("1.0.0-*");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVersionRangeWithInvalidAsteriskModifierChar2() {
        versionRange("1.0.0*-build42");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVersionRangeWithInvalidPlusModifierChar() {
        versionRange("1.0.0-buildWithC++");
    }

    @Test(expected = NullPointerException.class)
    public void testMatchesWithNullStringVersionId() {
        versionRange("1.0").contains((String) null);
    }

    @Test(expected = NullPointerException.class)
    public void testContainsWithNullVersionId() {
        versionRange("1.0").contains((VersionId) null);
    }

    @Test
    public void testContainsWithExactMatches() {
        assertTrue(versionRange("1.0").contains("1"));
        assertTrue(versionRange("1-0").contains("1"));
        assertTrue(versionRange("1_0").contains("1"));
        assertTrue(versionRange("1").contains("1.0"));
        assertTrue(versionRange("1.0").contains("1.0"));
        assertTrue(versionRange("1.0").contains("1.0.0-0"));
        assertTrue(versionRange("1.0.0_0").contains("1.0.0"));
        assertTrue(versionRange("1.3").contains("1.3.0"));
        assertTrue(versionRange("1.2.2.4").contains("1.2.2-004"));
        // not a match
        assertFalse(versionRange("1.0.4").contains("1.0"));
        assertFalse(versionRange("1.0.4").contains("1.4"));
        assertFalse(versionRange("1.0.4").contains("1.0.3"));
    }

    @Test
    public void testContainsWithPrefixModifiers() {
        assertTrue(versionRange("1.0*").contains("1"));
        assertTrue(versionRange("1.0*").contains("1.0"));
        assertTrue(versionRange("1.0*").contains("1.0.0"));
        assertTrue(versionRange("1.0*").contains("1.0.4"));
        assertTrue(versionRange("2.0*").contains("2.0.1"));
        // not a match
        assertFalse(versionRange("1.5*").contains("1.6"));
        assertFalse(versionRange("1.5*").contains("2.0.0"));
    }

    @Test
    public void testContainsWithGreaterOrEqualsModifiers() {
        assertTrue(versionRange("1.0+").contains("1.0.0"));
        assertTrue(versionRange("1.4+").contains("1.4.6"));
        assertTrue(versionRange("1.4.3+").contains("1.4.3-009"));
        assertTrue(versionRange("1.5+").contains("1.5"));
        assertTrue(versionRange("1.0.3+").contains("1.0.4"));
        assertTrue(versionRange("1.5+").contains("1.6"));
        assertTrue(versionRange("1.5+").contains("2.0"));
        assertTrue(versionRange("1.4.1_02+").contains("1.4.1_42"));
        // not a match
        assertFalse(versionRange("2.0.1+").contains("2.0.0"));
    }

    @Test
    public void testContainsWithCompound() {
        // version-id with 1.4 as a prefix and that is not less than 1.4.1_02
        assertTrue(versionRange("1.4*&1.4.1_02+").contains("1.4.1_02"));
        assertTrue(versionRange("1.4*&1.4.1_02+").contains("1.4.1_42"));
        assertTrue(versionRange("1.4*&1.4.1_02+").contains("1.4.5"));

        // not a match
        assertFalse(versionRange("1.4*&1.4.1_02+").contains("1.4.1_01"));
    }

    @Test
    public void testEqualsWithoutModifier() {
        assertTrue(versionRange("1").isEqualTo(versionRange("1.0")));
        assertTrue(versionRange("1.0").isEqualTo(versionRange("1")));
        assertTrue(versionRange("1.0").isEqualTo(versionRange("1.0")));
        assertTrue(versionRange("1.3").isEqualTo(versionRange("1.3.0")));
        assertTrue(versionRange("1.3.0").isEqualTo(versionRange("1.3")));
        assertTrue(versionRange("1.2.2.4").isEqualTo(versionRange("1.2.2-004")));
        assertTrue(versionRange("1.2.2-004").isEqualTo(versionRange("1.2.2.4.0")));
        // not equals
        assertFalse(versionRange("1.0").isEqualTo(null));
        assertFalse(versionRange("1.5").isEqualTo(versionRange("1.5+")));
        assertFalse(versionRange("1.5").isEqualTo(versionRange("1.5*")));
        assertFalse(versionRange("1.0-build42").isEqualTo(versionRange("1.0.0-build42")));
        assertFalse(versionRange("1.0-b42").isEqualTo(versionRange("1.0-B42")));
    }

    @Test
    public void testEqualsWithModifier() {
        assertTrue(versionRange("1.5+").isEqualTo(versionRange("1.5+")));
        assertTrue(versionRange("1.5*").isEqualTo(versionRange("1.5*")));
        // not equals
        assertFalse(versionRange("1.5+").isEqualTo(null));
        assertFalse(versionRange("1.5+").isEqualTo(versionRange("1.5*")));
        assertFalse(versionRange("1.5+").isEqualTo(versionRange("1.5")));
        assertFalse(versionRange("1.5+").isEqualTo(versionRange("1.6+")));
    }

    @Test
    public void testIsEqualToWithCompounds() {
        assertTrue(versionRange("1.4*&1.4.1_02+").isEqualTo(versionRange("1.4*&1.4.1_02+")));
        assertTrue(versionRange("1.4*&1.4.1").isEqualTo(versionRange("1.4*&1.4-001")));
        // not considered to be equal
        assertFalse(versionRange("1.4*&1.4.1+").isEqualTo(versionRange("1.4*&1.4.2")));
        assertFalse(versionRange("1.4*&1.4.1_02+").isEqualTo(versionRange("1.4*&1.4.1_03")));
    }

    @Test
    public void testIsExactVersion() {
        assertTrue(versionRange("1.0").isExactVersion());
        assertTrue(versionRange("1.0.0-beta").isExactVersion());
        // not considered to be exact
        assertFalse(versionRange("1.0+").isExactVersion());
        assertFalse(versionRange("1.0.0-beta*").isExactVersion());
        assertFalse(versionRange("1.4&1.4.1").isExactVersion());
        assertFalse(versionRange("1.4*&1.4.1").isExactVersion());
        assertFalse(versionRange("1.4*&1.4.1+").isExactVersion());
    }

    @Test
    public void testHasPrefixMatchModifier() {
        assertTrue(versionRange("1*").hasPrefixMatchModifier());
        assertTrue(versionRange("1.0*").hasPrefixMatchModifier());
        assertTrue(versionRange("1.0.0-build42*").hasPrefixMatchModifier());
        // not considered to have prefix modifier
        assertFalse(versionRange("1.0+").hasPrefixMatchModifier());
        assertFalse(versionRange("1.4&1.4.1").hasPrefixMatchModifier());
        assertFalse(versionRange("1.4*&1.4.1").hasPrefixMatchModifier());
        assertFalse(versionRange("1.4*&1.4.1*").hasPrefixMatchModifier());
    }

    @Test
    public void testHasGreaterThanOrEqualMatchModifier() {
        assertTrue(versionRange("1+").hasGreaterThanOrEqualMatchModifier());
        assertTrue(versionRange("1.0+").hasGreaterThanOrEqualMatchModifier());
        assertTrue(versionRange("1.0.0-build42+").hasGreaterThanOrEqualMatchModifier());
        // not considered to have greater or equal modifier
        assertFalse(versionRange("1.0*").hasGreaterThanOrEqualMatchModifier());
        assertFalse(versionRange("1.4&1.4.1").hasGreaterThanOrEqualMatchModifier());
        assertFalse(versionRange("1.4+&1.4.1").hasGreaterThanOrEqualMatchModifier());
        assertFalse(versionRange("1.4+&1.4.1+").hasGreaterThanOrEqualMatchModifier());
    }

    @Test
    public void testIsCompoundVersion() {
        assertTrue(versionRange("1.4&1.4.1").isCompoundVersion());
        assertTrue(versionRange("1.4*&1.4.1").isCompoundVersion());
        assertTrue(versionRange("1.4*&1.4.1+").isCompoundVersion());
        // not compound
        assertFalse(versionRange("1.0").isCompoundVersion());
        assertFalse(versionRange("1.0.0-beta").isCompoundVersion());
        assertFalse(versionRange("1.0+").isCompoundVersion());
        assertFalse(versionRange("1.0.0-beta*").isCompoundVersion());
    }

    private VersionRange versionRange(String s) {
        return VersionRange.fromString(s);
    }
}
