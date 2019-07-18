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

import static net.adoptopenjdk.icedteaweb.jnlp.version.JNLPVersionSpecifications.REGEXP_CHAR;
import static net.adoptopenjdk.icedteaweb.jnlp.version.JNLPVersionSpecifications.REGEXP_SEPARATOR;
import static net.adoptopenjdk.icedteaweb.jnlp.version.JNLPVersionSpecifications.REGEXP_STRING;
import static net.adoptopenjdk.icedteaweb.jnlp.version.JNLPVersionSpecifications.REGEXP_VERSION_ID;
import static net.adoptopenjdk.icedteaweb.jnlp.version.VersionModifier.AMPERSAND;
import static net.adoptopenjdk.icedteaweb.jnlp.version.VersionModifier.ASTERISK;
import static net.adoptopenjdk.icedteaweb.jnlp.version.VersionModifier.PLUS;
import static net.adoptopenjdk.icedteaweb.jnlp.version.VersionSeparator.DOT;
import static net.adoptopenjdk.icedteaweb.jnlp.version.VersionSeparator.MINUS;
import static net.adoptopenjdk.icedteaweb.jnlp.version.VersionSeparator.SPACE;
import static net.adoptopenjdk.icedteaweb.jnlp.version.VersionSeparator.UNDERSCORE;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VersionIdTest {
    @Test
    public void testValidNumericVersionIds() {
        assertEquals("1", VersionId.fromString("1").toString());
        assertEquals("1.1", VersionId.fromString("1.1").toString());
        assertEquals("1.1.0", VersionId.fromString("1.1.0").toString());
        assertEquals("1.2.2-001", VersionId.fromString("1.2.2-001").toString());
        assertEquals("1.3.0-rc2-w", VersionId.fromString("1.3.0-rc2-w").toString());
        assertEquals("1.2.3_build42", VersionId.fromString("1.2.3_build42").toString());
        assertEquals("1.3.0-SNAPSHOT", VersionId.fromString("1.3.0-SNAPSHOT").toString());
        assertEquals("15.2.2_21.05.2019_11:43:34", VersionId.fromString("15.2.2_21.05.2019_11:43:34").toString());
    }

    @Test
    public void testValidAlphaNumericVersionIds() {
        assertEquals("A", VersionId.fromString("A").toString());
        assertEquals("A.B", VersionId.fromString("A.B").toString());
        assertEquals("A.B.1", VersionId.fromString("A.B.1").toString());
        assertEquals("A.B-1", VersionId.fromString("A.B-1").toString());
        assertEquals("1_3_0-rc2-w", VersionId.fromString("1_3_0-rc2-w").toString());
    }

    @Test
    public void testValidVersionIdsWithModifiers() {
        assertEquals("1+", VersionId.fromString("1+").toString());
        assertEquals("1*", VersionId.fromString("1*").toString());
        assertEquals("1.1.1+", VersionId.fromString("1.1.1+").toString());
        assertEquals("1.2.2-001+", VersionId.fromString("1.2.2-001+").toString());
        assertEquals("1.2.4_02+", VersionId.fromString("1.2.4_02+").toString());
        assertEquals("1.3.0-rc2-w+", VersionId.fromString("1.3.0-rc2-w+").toString());
        assertEquals("1.2.3_build42+", VersionId.fromString("1.2.3_build42+").toString());
    }

    @Test
    public void testValidVersionIdsWithCompound() {
        assertEquals("1.4&1.4.1_02", VersionId.fromString("1.4&1.4.1_02").toString());
        assertEquals("1.4*&1.4.1_02+", VersionId.fromString("1.4*&1.4.1_02+").toString());
        assertEquals("1.4+&1.4.1", VersionId.fromString("1.4+&1.4.1").toString());
        assertEquals("1.4", VersionId.fromString("1.4&1.4.1_02").toExactString());
        assertEquals("1.4", VersionId.fromString("1.4*&1.4.1_02+").toExactString());
        assertEquals("1.4", VersionId.fromString("1.4+&1.4.1").toExactString());
    }

    @Test(expected = NullPointerException.class)
    public void testNullVersionId() {
        VersionId.fromString(null).toString();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyVersionId() {
        VersionId.fromString("").toString();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVersionIdWithInvalidSpaceChar() {
        VersionId.fromString("1.0 beta").toString();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVersionIdWithInvalidAmpersandChar() {
        VersionId.fromString("1.0.0-&").toString();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVersionIdWithInvalidAsteriskModifierChar() {
        VersionId.fromString("1.0.0-*").toString();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVersionIdWithInvalidAsteriskModifierChar2() {
        VersionId.fromString("1.0.0*-build42").toString();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVersionIdWithInvalidPlusModifierChar() {
        VersionId.fromString("1.0.0-buildWithC++").toString();
    }

    @Test(expected = NullPointerException.class)
    public void testMatchesWithNullStringVersionId() {
        VersionId.fromString("1.0").matches((String) null);
    }

    @Test(expected = NullPointerException.class)
    public void testMatchesWithNullVersionId() {
        VersionId.fromString("1.0").matches((VersionId) null);
    }

    @Test
    public void testMatches() {
        assertTrue(VersionId.fromString("1.0").matches("1"));
        assertTrue(VersionId.fromString("1").matches("1.0"));
        assertTrue(VersionId.fromString("1.0").matches("1.0"));
        assertTrue(VersionId.fromString("1.0").matches("1.0.0-0"));
        assertTrue(VersionId.fromString("1.0.0_0").matches("1.0.0"));
        assertTrue(VersionId.fromString("1.3").matches("1.3.0"));
        assertTrue(VersionId.fromString("1.2.2.4").matches("1.2.2-004"));
        // not a match
        assertFalse(VersionId.fromString("1.0.4").matches("1.0"));
    }

    @Test
    public void testMatchesWithModifiers() {
        assertTrue(VersionId.fromString("1.0*").matches("1.0.4"));
        assertTrue(VersionId.fromString("1.5+").matches("1.5"));
        assertTrue(VersionId.fromString("1.0.3+").matches("1.0.4"));
        assertTrue(VersionId.fromString("1.5*").matches("1.5+"));
        assertTrue(VersionId.fromString("1.5+").matches("1.6"));
        assertTrue(VersionId.fromString("1.4.1_02+").matches("1.4.1_42"));
        // not a match
        assertFalse(VersionId.fromString("1.0.4").matches("1.0*"));
        assertFalse(VersionId.fromString("1.0.4").matches("1.0.3+"));
    }

    @Test
    public void testMatchesWithCompound() {
        // version-id with 1.4 as a prefix and that is not less than 1.4.1_02
        assertTrue(VersionId.fromString("1.4*&1.4.1_02+").matches("1.4.1_02"));

        assertTrue(VersionId.fromString("1.4*&1.4.1_02+").matches("1.4.1_42"));
        assertTrue(VersionId.fromString("1.4*&1.4.1_02+").matches("1.4.5"));
        assertTrue(VersionId.fromString("1.4*&1.4.6+").matches(VersionId.fromString("1.4.7")));
        // not a match
        assertFalse(VersionId.fromString("1.4*&1.4.1_02+").matches("1.4.1_01"));
    }

    @Test
    public void testIsExactMatchOf() {
        assertTrue(VersionId.fromString("1").isExactMatchOf(VersionId.fromString("1.0")));
        assertTrue(VersionId.fromString("1.0").isExactMatchOf(VersionId.fromString("1")));
        assertTrue(VersionId.fromString("1.0").isExactMatchOf(VersionId.fromString("1.0")));
        assertTrue(VersionId.fromString("1.3").isExactMatchOf(VersionId.fromString("1.3.0")));
        assertTrue(VersionId.fromString("1.3.0").isExactMatchOf(VersionId.fromString("1.3")));
        assertTrue(VersionId.fromString("1.2.2.4").isExactMatchOf(VersionId.fromString("1.2.2-004")));
        // no exact match
        assertFalse(VersionId.fromString("1.0-build42").isExactMatchOf(VersionId.fromString("1.0.0-build42")));
        assertFalse(VersionId.fromString("1.0-b42").isExactMatchOf(VersionId.fromString("1.0-B42")));
    }

    @Test
    public void testIsExactMatchOfWithModifiers() {
        assertTrue(VersionId.fromString("1.5+").isExactMatchOf(VersionId.fromString("1.5+")));
        assertTrue(VersionId.fromString("1.5*").isExactMatchOf(VersionId.fromString("1.5*")));
        assertTrue(VersionId.fromString("1.5+").isExactMatchOf(VersionId.fromString("1.5*")));
        assertTrue(VersionId.fromString("1.5+").isExactMatchOf(VersionId.fromString("1.5")));
        assertTrue(VersionId.fromString("1.5").isExactMatchOf(VersionId.fromString("1.5+")));

        // no exact match
        assertFalse(VersionId.fromString("1.5+").isExactMatchOf(VersionId.fromString("1.6")));
    }

    @Test
    public void testIsPrefixMatchOf() {
        assertTrue(VersionId.fromString("1.0").isPrefixMatchOf(VersionId.fromString("1.0.0")));
        assertTrue(VersionId.fromString("1.2.1").isPrefixMatchOf(VersionId.fromString("1.2.1-004")));
        assertTrue(VersionId.fromString("1.2.0.0").isPrefixMatchOf(VersionId.fromString("1.2")));
        assertTrue(VersionId.fromString("1.2.2.4").isPrefixMatchOf(VersionId.fromString("1.2.2-004_beta")));
        // no prefix match
        assertFalse(VersionId.fromString("1").isPrefixMatchOf(VersionId.fromString("2.1.0")));
        assertFalse(VersionId.fromString("1.2").isPrefixMatchOf(VersionId.fromString("1.3")));
        assertFalse(VersionId.fromString("1.2.1").isPrefixMatchOf(VersionId.fromString("1.2.10")));
    }

    @Test
    public void testIsPrefixMatchOfWithModifiers() {
        assertTrue(VersionId.fromString("1.0*").isPrefixMatchOf(VersionId.fromString("1.0.0")));
        assertTrue(VersionId.fromString("1.0+").isPrefixMatchOf(VersionId.fromString("1.0.0")));
        assertTrue(VersionId.fromString("2.0*").isPrefixMatchOf(VersionId.fromString("2.0.1")));
        assertTrue(VersionId.fromString("1.4+").isPrefixMatchOf(VersionId.fromString("1.4.6")));
        assertTrue(VersionId.fromString("1.4.3+").isPrefixMatchOf(VersionId.fromString("1.4.3-009")));
        // no prefix match
        assertFalse(VersionId.fromString("1.5+").isPrefixMatchOf(VersionId.fromString("1.6")));
        assertFalse(VersionId.fromString("1.5+").isPrefixMatchOf(VersionId.fromString("2.0.0")));
    }

    @Test
    public void testIsPrefixMatchOfWithCompounds() {
        assertTrue(VersionId.fromString("1.4*&1.4.3+").isPrefixMatchOf(VersionId.fromString("1.4.3-009")));
        assertTrue(VersionId.fromString("1.4*&1.4.5+").isPrefixMatchOf(VersionId.fromString("1.4.5+")));
        // not a prefix match
        assertFalse(VersionId.fromString("1.4*&1.4.5+").isPrefixMatchOf(VersionId.fromString("1.4.1")));
    }

   @Test
    public void testIsEqualTo() {
        assertTrue(VersionId.fromString("1.0").isEqualTo(VersionId.fromString("1")));
        assertTrue(VersionId.fromString("1.0").isEqualTo(VersionId.fromString("1.0")));
        assertTrue(VersionId.fromString("1.3").isEqualTo(VersionId.fromString("1.3.0")));
        assertTrue(VersionId.fromString("1.2.2.4").isEqualTo(VersionId.fromString("1.2.2-004")));
        assertTrue(VersionId.fromString("1.2.2-004").isEqualTo(VersionId.fromString("1.2.2.4.0")));
        // not considered to be equal
        assertFalse(VersionId.fromString("1.0").isEqualTo(null));
        assertFalse(VersionId.fromString("1.0-build42").isEqualTo(VersionId.fromString("1.0.0-build42")));
        assertFalse(VersionId.fromString("1.0-b42").isEqualTo(VersionId.fromString("1.0-B42")));
    }

    @Test
    public void testIsEqualToWithCompounds() {
        assertTrue(VersionId.fromString("1.4*&1.4.1_02+").isEqualTo(VersionId.fromString("1.4*&1.4.1_02+")));
        assertTrue(VersionId.fromString("1.4*&1.4.1").isEqualTo(VersionId.fromString("1.4*&1.4-001")));
        // not considered to be equal
        assertFalse(VersionId.fromString("1.4*&1.4.1+").isEqualTo(VersionId.fromString("1.4*&1.4.2")));
        assertFalse(VersionId.fromString("1.4*&1.4.1_02+").isEqualTo(VersionId.fromString("1.4*&1.4.1_03")));
    }

    @Test
    public void testIsGreaterThan() {
        // greater than
        assertTrue(VersionId.fromString("2").isGreaterThan(VersionId.fromString("1")));
        assertTrue(VersionId.fromString("1.1").isGreaterThan(VersionId.fromString("1")));
        assertTrue(VersionId.fromString("1.1").isGreaterThan(VersionId.fromString("1.0")));
        assertTrue(VersionId.fromString("1.1.1").isGreaterThan(VersionId.fromString("1.1.0")));
        assertTrue(VersionId.fromString("1.1.1").isGreaterThan(VersionId.fromString("1.1")));
        assertTrue(VersionId.fromString("1.0.1").isGreaterThan(VersionId.fromString("1.0.0-build42")));
        assertTrue(VersionId.fromString("1.4.5").isGreaterThan(VersionId.fromString("1.4.2")));

        // numeric elements have lower precedence than non-numeric elements
        assertTrue(VersionId.fromString("1.0.A").isGreaterThan(VersionId.fromString("1.0.1")));
        assertTrue(VersionId.fromString("1.1.A").isGreaterThan(VersionId.fromString("1.0.B")));
        assertTrue(VersionId.fromString("1.1.ABC").isGreaterThan(VersionId.fromString("1.0.A")));
        assertTrue(VersionId.fromString("1.0.0-build42").isGreaterThan(VersionId.fromString("1.0.0-build41")));
        assertTrue(VersionId.fromString("1.0.0-build42").isGreaterThan(VersionId.fromString("1.0.0-42")));

        // not greater than
        assertFalse(VersionId.fromString("1.0").isGreaterThan(VersionId.fromString("1")));
        assertFalse(VersionId.fromString("1.0").isGreaterThan(VersionId.fromString("1.0")));
        assertFalse(VersionId.fromString("1.0.2").isGreaterThan(VersionId.fromString("1.1")));
        assertFalse(VersionId.fromString("1.0.0-build42").isGreaterThan(VersionId.fromString("1.0.1")));
        assertFalse(VersionId.fromString("1.5+").isGreaterThan(VersionId.fromString("1.5")));
        assertFalse(VersionId.fromString("1.5").isGreaterThan(VersionId.fromString("1.5+")));
        assertFalse(VersionId.fromString("1.4.5+").isGreaterThan(VersionId.fromString("1.4.6")));
        assertFalse(VersionId.fromString("1.4*").isGreaterThan(VersionId.fromString("1.4.2")));
    }

    @Test
    public void testIsGreaterThanWithCompounds() {
        assertTrue(VersionId.fromString("1.4*&1.4.5").isGreaterThan(VersionId.fromString("1.4.2")));
        assertTrue(VersionId.fromString("1.4*&1.4.5+").isGreaterThan(VersionId.fromString("1.4.2+")));
        assertTrue(VersionId.fromString("1.4*&1.4.6+").isGreaterThan(VersionId.fromString("1.4.5")));
        // not greater than
        assertFalse(VersionId.fromString("1.4*&1.4.6+").isGreaterThan(VersionId.fromString("1.4.7")));
        assertFalse(VersionId.fromString("1.4*&1.4.6+").isGreaterThan(VersionId.fromString("1.4.6")));
        assertFalse(VersionId.fromString("1.4*&1.4.5+").isGreaterThan(VersionId.fromString("1.4.5+")));
    }

    @Test
    public void testIsGreaterThanOrEqual() {
        // greater than or equal
        assertTrue(VersionId.fromString("2").isGreaterThanOrEqual(VersionId.fromString("1")));
        assertTrue(VersionId.fromString("1.1").isGreaterThanOrEqual(VersionId.fromString("1")));
        assertTrue(VersionId.fromString("1.5+").isGreaterThanOrEqual(VersionId.fromString("1.5")));
        assertTrue(VersionId.fromString("1.0").isGreaterThanOrEqual(VersionId.fromString("1")));
        assertTrue(VersionId.fromString("1").isGreaterThanOrEqual(VersionId.fromString("1.0")));
        assertTrue(VersionId.fromString("1.0").isGreaterThanOrEqual(VersionId.fromString("1.0")));

        // less than
        assertFalse(VersionId.fromString("1.0").isGreaterThanOrEqual(VersionId.fromString("1.0.1")));
        assertFalse(VersionId.fromString("1.0.5").isGreaterThanOrEqual(VersionId.fromString("1.0.A")));
    }

    @Test
    public void testIsGreaterThanOrEqualWithCompounds() {
        assertTrue(VersionId.fromString("1.4*&1.4.5").isGreaterThanOrEqual(VersionId.fromString("1.4.2")));
        assertTrue(VersionId.fromString("1.4*&1.4.5+").isGreaterThanOrEqual(VersionId.fromString("1.4.2+")));
        assertTrue(VersionId.fromString("1.4*&1.4.6+").isGreaterThanOrEqual(VersionId.fromString("1.4.5")));
        assertTrue(VersionId.fromString("1.4*&1.4.5").isGreaterThanOrEqual(VersionId.fromString("1.4.5")));
        assertTrue(VersionId.fromString("1.4*&1.4.5+").isGreaterThanOrEqual(VersionId.fromString("1.4.5")));
        assertTrue(VersionId.fromString("1.4*&1.4.5+").isGreaterThanOrEqual(VersionId.fromString("1.4.5+")));

        // not greater than or equal
        assertFalse(VersionId.fromString("1.4*&1.4.6+").isGreaterThanOrEqual(VersionId.fromString("1.4.7")));
    }

    @Test
    public void testVersionIdSpecificationVersionId() {
        assertTrue("1.0".matches(REGEXP_VERSION_ID));
        assertTrue("1-0".matches(REGEXP_VERSION_ID));
        assertTrue("1.1.1".matches(REGEXP_VERSION_ID));
        assertTrue("1_1_1".matches(REGEXP_VERSION_ID));
        assertTrue("1.1.1-beta".matches(REGEXP_VERSION_ID));

        assertFalse("".matches(REGEXP_VERSION_ID));
        assertFalse("1.".matches(REGEXP_VERSION_ID));
        assertFalse("1-".matches(REGEXP_VERSION_ID));
        assertFalse("1_".matches(REGEXP_VERSION_ID));
        assertFalse("1..1".matches(REGEXP_VERSION_ID));
        assertFalse("10 0".matches(REGEXP_VERSION_ID));
    }

    @Test
    public void testVersionIdSpecificationVersionIdWithModifiers() {
        assertTrue("1.0+".matches(REGEXP_VERSION_ID));
        assertTrue("1.1*".matches(REGEXP_VERSION_ID));

        assertFalse("1.+".matches(REGEXP_VERSION_ID));
        assertFalse("1.2_*".matches(REGEXP_VERSION_ID));
        assertFalse("1.*".matches(REGEXP_VERSION_ID));
        assertFalse("1.2-*".matches(REGEXP_VERSION_ID));
    }

    /**
     * Test all version-id char tokens as defined by JSR-56 specification, Appendix A.
     */
    @Test
    public void testVersionIdSpecificationString() {
        assertTrue("12".matches(REGEXP_STRING));
        assertTrue("abc".matches(REGEXP_STRING));
        assertTrue("V1".matches(REGEXP_STRING));

        assertFalse("1 2".matches(REGEXP_STRING));
        assertFalse("1&1".matches(REGEXP_STRING));
        assertFalse("1.2".matches(REGEXP_STRING));
        assertFalse("1-2".matches(REGEXP_STRING));
        assertFalse("1_2".matches(REGEXP_STRING));
        assertFalse("12*".matches(REGEXP_STRING));
        assertFalse("12+".matches(REGEXP_STRING));
    }

    /**
     * Test all version-id char tokens as defined by JSR-56 specification, Appendix A.
     */
    @Test
    public void testVersionIdSpecificationChar() {
        // legal chars
        assertTrue("a".matches(REGEXP_CHAR));
        assertTrue("1".matches(REGEXP_CHAR));
        assertTrue("V".matches(REGEXP_CHAR));

        // illegal chars (separators)
        assertFalse(DOT.symbol().matches(REGEXP_CHAR));
        assertFalse(MINUS.symbol().matches(REGEXP_CHAR));
        assertFalse(UNDERSCORE.symbol().matches(REGEXP_CHAR));

        // illegal chars (modifiers)
        assertFalse(ASTERISK.symbol().matches(REGEXP_CHAR));
        assertFalse(PLUS.symbol().matches(REGEXP_CHAR));

        // illegal chars (others)
        assertFalse("".matches(REGEXP_CHAR));
        assertFalse(SPACE.symbol().matches(REGEXP_CHAR));
        assertFalse(AMPERSAND.symbol().matches(REGEXP_CHAR));
    }

    /**
     * Test all version-id separator tokens as defined by JSR-56 specification, Appendix A.
     */
    @Test
    public void testVersionIdSpecificationSeparator() {
        assertTrue(DOT.symbol().matches(REGEXP_SEPARATOR));
        assertTrue(MINUS.symbol().matches(REGEXP_SEPARATOR));
        assertTrue(UNDERSCORE.symbol().matches(REGEXP_SEPARATOR));
    }

    @Test
    public void testVersionIdAsTuple() {
        assertArrayEquals(new String[] {"1","3","0","rc2","w"}, VersionId.fromString("1.3.0-rc2-w").asTuple());
        assertArrayEquals(new String[] {"1","2","3","build42"}, VersionId.fromString("1_2_3_build42").asTuple());
        assertArrayEquals(new String[] {"A","B","C","D"}, VersionId.fromString("A-B_C.D").asTuple());

        assertArrayEquals(new String[] {"1","2","3"}, VersionId.fromString("1.2.3*").asTuple());
        assertArrayEquals(new String[] {"1","2","build42"}, VersionId.fromString("1.2-build42+").asTuple());
    }

    @Test
    public void testVersionIdAsNormalizedTuple() {
        assertArrayEquals(new String[] {"1","0","0"}, VersionId.fromString("1").asNormalizedTuple(3));
        assertArrayEquals(new String[] {"1","1","1"}, VersionId.fromString("1.1.1").asNormalizedTuple(3));
        assertArrayEquals(new String[] {"1","2","0","0","0"}, VersionId.fromString("1.2").asNormalizedTuple(5));
        assertArrayEquals(new String[] {"1","2","3","build42","0"}, VersionId.fromString("1.2.3-build42").asNormalizedTuple(5));

        assertArrayEquals(new String[] {"1","2"}, VersionId.fromString("1.2").asNormalizedTuple(1));
        assertArrayEquals(new String[] {"1","2"}, VersionId.fromString("1.2").asNormalizedTuple(2));
        assertArrayEquals(new String[] {"1","2","0"}, VersionId.fromString("1.2").asNormalizedTuple(3));

        assertArrayEquals(new String[] {"1","2"}, VersionId.fromString("1.2*").asNormalizedTuple(1));
        assertArrayEquals(new String[] {"1","2"}, VersionId.fromString("1.2+").asNormalizedTuple(2));
        assertArrayEquals(new String[] {"1","2","0"}, VersionId.fromString("1.2+").asNormalizedTuple(3));
    }

    @Test
    public void testToString() {
        assertEquals("1", VersionId.fromString("1").toString());
        assertEquals("1.0", VersionId.fromString("1.0").toString());
        assertEquals("1.0*", VersionId.fromString("1.0*").toString());
        assertEquals("1.0+", VersionId.fromString("1.0+").toString());
        assertEquals("1.0&1.0.5*", VersionId.fromString("1.0&1.0.5*").toString());
        assertEquals("1.0*&1.0.5", VersionId.fromString("1.0*&1.0.5").toString());
        assertEquals("1.0*&1.0.5+", VersionId.fromString("1.0*&1.0.5+").toString());
    }

    @Test
    public void testToExactString() {
        assertEquals("1", VersionId.fromString("1").toExactString());
        assertEquals("1.0", VersionId.fromString("1.0").toExactString());
        assertEquals("1.0", VersionId.fromString("1.0*").toExactString());
        assertEquals("1.0", VersionId.fromString("1.0+").toExactString());
        assertEquals("1.0", VersionId.fromString("1.0&1.0.5*").toExactString());
        assertEquals("1.0", VersionId.fromString("1.0*&1.0.5").toExactString());
        assertEquals("1.0.0", VersionId.fromString("1.0.0*&1.0.5+").toExactString());
    }

    @Test
    public void testIsCompoundVersion() {
        assertTrue(VersionId.fromString("1.4&1.4.1").isCompoundVersion());
        assertTrue(VersionId.fromString("1.4*&1.4.1").isCompoundVersion());
        assertTrue(VersionId.fromString("1.4*&1.4.1+").isCompoundVersion());
        // not compound
        assertFalse(VersionId.fromString("1.0").isCompoundVersion());
        assertFalse(VersionId.fromString("1.0.0-beta").isCompoundVersion());
        assertFalse(VersionId.fromString("1.0+").isCompoundVersion());
        assertFalse(VersionId.fromString("1.0.0-beta*").isCompoundVersion());
    }

    @Test
    public void testIsExactVersion() {
        assertTrue(VersionId.fromString("1.0").isExactVersion());
        assertTrue(VersionId.fromString("1.0.0-beta").isExactVersion());
        // not considered to be exact
        assertFalse(VersionId.fromString("1.0+").isExactVersion());
        assertFalse(VersionId.fromString("1.0.0-beta*").isExactVersion());
        assertFalse(VersionId.fromString("1.4&1.4.1").isExactVersion());
        assertFalse(VersionId.fromString("1.4*&1.4.1").isExactVersion());
        assertFalse(VersionId.fromString("1.4*&1.4.1+").isExactVersion());
    }

    @Test
    public void testHasPrefixMatchModifier() {
        assertTrue(VersionId.fromString("1*").hasPrefixMatchModifier());
        assertTrue(VersionId.fromString("1.0*").hasPrefixMatchModifier());
        assertTrue(VersionId.fromString("1.0.0-build42*").hasPrefixMatchModifier());
    }

    @Test
    public void testHasGreaterThanOrEqualMatchModifier() {
        assertTrue(VersionId.fromString("1+").hasGreaterThanOrEqualMatchModifier());
        assertTrue(VersionId.fromString("1.0+").hasGreaterThanOrEqualMatchModifier());
        assertTrue(VersionId.fromString("1.0.0-build42+").hasGreaterThanOrEqualMatchModifier());
    }
}