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

public class SimpleRangeTest {

    @Test
    public void testExactSimpleRangeParsingAndToString() {
        // numerical
        assertEquals("1", simpleRange("1").toString());
        assertEquals("1.1", simpleRange("1.1").toString());
        assertEquals("1.1.0", simpleRange("1.1.0").toString());
        assertEquals("1.2.2-001", simpleRange("1.2.2-001").toString());
        assertEquals("1.3.0-rc2-w", simpleRange("1.3.0-rc2-w").toString());
        assertEquals("1_3_0-rc2-w", simpleRange("1_3_0-rc2-w").toString());
        assertEquals("1.2.3_build42", simpleRange("1.2.3_build42").toString());
        assertEquals("1.3.0-SNAPSHOT", simpleRange("1.3.0-SNAPSHOT").toString());
        assertEquals("15.2.2_21.05.2019_11:43:34", simpleRange("15.2.2_21.05.2019_11:43:34").toString());
        assertEquals("15.2.2_2019.05.21T11:43:34", simpleRange("15.2.2_2019.05.21T11:43:34").toString());

        // alphabetical
        assertEquals("A", simpleRange("A").toString());
        assertEquals("A.B", simpleRange("A.B").toString());
        assertEquals("A.B.1", simpleRange("A.B.1").toString());
        assertEquals("A.B-1", simpleRange("A.B-1").toString());
    }

    @Test
    public void testSimpleRangeWithModifiersParsingAndToString() {
        assertEquals("1+", simpleRange("1+").toString());
        assertEquals("1*", simpleRange("1*").toString());
        assertEquals("1.1.1+", simpleRange("1.1.1+").toString());
        assertEquals("1.1.1*", simpleRange("1.1.1*").toString());
        assertEquals("1.2.2-001+", simpleRange("1.2.2-001+").toString());
        assertEquals("1.2.4_02+", simpleRange("1.2.4_02+").toString());
        assertEquals("1.3.0-rc2-w+", simpleRange("1.3.0-rc2-w+").toString());
        assertEquals("1.2.3_build42+", simpleRange("1.2.3_build42+").toString());
        assertEquals("1.2.3_build42*", simpleRange("1.2.3_build42*").toString());
    }

    @Test(expected = NullPointerException.class)
    public void testNullSimpleRange() {
        simpleRange(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptySimpleRange() {
        simpleRange("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSimpleRangeWithInvalidSpaceChar() {
        simpleRange("1.0 beta");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSimpleRangeWithInvalidAmpersandChar() {
        simpleRange("1&1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVersionIdWithInvalidTrailingSeparatorChar() {
        simpleRange("1.0.0-");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVersionIdWithInvalidAsteriskModifierChar() {
        simpleRange("1.0.0*-build42");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVersionIdWithDuplicatedModifierChar() {
        simpleRange("1.0.0-buildWithC++");
    }

    @Test(expected = NullPointerException.class)
    public void testMatchesWithNullStringVersionId() {
        simpleRange("1.0").matches((String) null);
    }

    @Test(expected = NullPointerException.class)
    public void testMatchesWithNullVersionId() {
        simpleRange("1.0").matches((VersionId) null);
    }

    @Test
    public void testExactMatches() {
        assertTrue(simpleRange("1.0").matches("1"));
        assertTrue(simpleRange("1-0").matches("1"));
        assertTrue(simpleRange("1_0").matches("1"));
        assertTrue(simpleRange("1").matches("1.0"));
        assertTrue(simpleRange("1.0").matches("1.0"));
        assertTrue(simpleRange("1.0").matches("1.0.0-0"));
        assertTrue(simpleRange("1.0.0_0").matches("1.0.0"));
        assertTrue(simpleRange("1.3").matches("1.3.0"));
        assertTrue(simpleRange("1.2.2.4").matches("1.2.2-004"));
        // not a match
        assertFalse(simpleRange("1.0.4").matches("1.0"));
        assertFalse(simpleRange("1.0.4").matches("1.4"));
        assertFalse(simpleRange("1.0.4").matches("1.0.3"));
    }

    @Test
    public void testMatchesWithPrefixModifiers() {
        assertTrue(simpleRange("1.0*").matches("1"));
        assertTrue(simpleRange("1.0*").matches("1.0"));
        assertTrue(simpleRange("1.0*").matches("1.0.0"));
        assertTrue(simpleRange("1.0*").matches("1.0.4"));
        assertTrue(simpleRange("2.0*").matches("2.0.1"));
        // not a match
        assertFalse(simpleRange("1.5*").matches("1.6"));
        assertFalse(simpleRange("1.5*").matches("2.0.0"));
    }

    @Test
    public void testMatchesWithGreaterOrEqualsModifiers() {
        assertTrue(simpleRange("1.0+").matches("1.0.0"));
        assertTrue(simpleRange("1.4+").matches("1.4.6"));
        assertTrue(simpleRange("1.4.3+").matches("1.4.3-009"));
        assertTrue(simpleRange("1.5+").matches("1.5"));
        assertTrue(simpleRange("1.0.3+").matches("1.0.4"));
        assertTrue(simpleRange("1.5+").matches("1.6"));
        assertTrue(simpleRange("1.5+").matches("2.0"));
        assertTrue(simpleRange("1.4.1_02+").matches("1.4.1_42"));
        // not a match
        assertFalse(simpleRange("2.0.1+").matches("2.0.0"));
    }

    @Test
    public void testEqualsWithoutModifier() {
        assertTrue(simpleRange("1").isEqualTo(simpleRange("1.0")));
        assertTrue(simpleRange("1.0").isEqualTo(simpleRange("1")));
        assertTrue(simpleRange("1.0").isEqualTo(simpleRange("1.0")));
        assertTrue(simpleRange("1.3").isEqualTo(simpleRange("1.3.0")));
        assertTrue(simpleRange("1.3.0").isEqualTo(simpleRange("1.3")));
        assertTrue(simpleRange("1.2.2.4").isEqualTo(simpleRange("1.2.2-004")));
        assertTrue(simpleRange("1.2.2-004").isEqualTo(simpleRange("1.2.2.4.0")));
        // not equals
        assertFalse(simpleRange("1.0").isEqualTo(null));
        assertFalse(simpleRange("1.5").isEqualTo(simpleRange("1.5+")));
        assertFalse(simpleRange("1.5").isEqualTo(simpleRange("1.5*")));
        assertFalse(simpleRange("1.0-build42").isEqualTo(simpleRange("1.0.0-build42")));
        assertFalse(simpleRange("1.0-b42").isEqualTo(simpleRange("1.0-B42")));
    }

    @Test
    public void testEqualsWithModifier() {
        assertTrue(simpleRange("1.5+").isEqualTo(simpleRange("1.5+")));
        assertTrue(simpleRange("1.5*").isEqualTo(simpleRange("1.5*")));
        // not equals
        assertFalse(simpleRange("1.5+").isEqualTo(null));
        assertFalse(simpleRange("1.5+").isEqualTo(simpleRange("1.5*")));
        assertFalse(simpleRange("1.5+").isEqualTo(simpleRange("1.5")));
        assertFalse(simpleRange("1.5+").isEqualTo(simpleRange("1.6+")));
    }

    @Test
    public void testIsExactVersion() {
        assertTrue(simpleRange("1.0").isExactVersion());
        assertTrue(simpleRange("1.0.0-beta").isExactVersion());
        // not considered to be exact
        assertFalse(simpleRange("1.0+").isExactVersion());
        assertFalse(simpleRange("1.0.0-beta*").isExactVersion());
    }

    @Test
    public void testHasPrefixMatchModifier() {
        assertTrue(simpleRange("1*").hasPrefixMatchModifier());
        assertTrue(simpleRange("1.0*").hasPrefixMatchModifier());
        assertTrue(simpleRange("1.0.0-build42*").hasPrefixMatchModifier());

        assertFalse(simpleRange("1.0+").hasPrefixMatchModifier());
    }

    @Test
    public void testHasGreaterThanOrEqualMatchModifier() {
        assertTrue(simpleRange("1+").hasGreaterThanOrEqualMatchModifier());
        assertTrue(simpleRange("1.0+").hasGreaterThanOrEqualMatchModifier());
        assertTrue(simpleRange("1.0.0-build42+").hasGreaterThanOrEqualMatchModifier());

        assertFalse(simpleRange("1.0*").hasGreaterThanOrEqualMatchModifier());
    }

    private SimpleRange simpleRange(String s) {
        return SimpleRange.fromString(s);
    }
}
