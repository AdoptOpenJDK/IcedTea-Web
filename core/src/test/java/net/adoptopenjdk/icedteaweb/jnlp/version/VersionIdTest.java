package net.adoptopenjdk.icedteaweb.jnlp.version;

import org.junit.Ignore;
import org.junit.Test;

import static net.adoptopenjdk.icedteaweb.jnlp.version.VersionId.AMPERSAND;
import static net.adoptopenjdk.icedteaweb.jnlp.version.VersionId.ASTERISK;
import static net.adoptopenjdk.icedteaweb.jnlp.version.VersionId.DOT;
import static net.adoptopenjdk.icedteaweb.jnlp.version.VersionId.MINUS;
import static net.adoptopenjdk.icedteaweb.jnlp.version.VersionId.PLUS;
import static net.adoptopenjdk.icedteaweb.jnlp.version.VersionId.REGEXP_CHAR;
import static net.adoptopenjdk.icedteaweb.jnlp.version.VersionId.REGEXP_SEPARATOR;
import static net.adoptopenjdk.icedteaweb.jnlp.version.VersionId.REGEXP_STRING;
import static net.adoptopenjdk.icedteaweb.jnlp.version.VersionId.REGEXP_VERSION_ID;
import static net.adoptopenjdk.icedteaweb.jnlp.version.VersionId.SPACE;
import static net.adoptopenjdk.icedteaweb.jnlp.version.VersionId.UNDERSCORE;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class VersionIdTest {
    @Test
    public void testValidVersionIds() {
        // legal version-ids (typical)
        assertEquals("1", VersionId.fromString("1").toString());
        assertEquals("1.1", VersionId.fromString("1.1").toString());
        assertEquals("1.1.0", VersionId.fromString("1.1.0").toString());
        assertEquals("1.2.2-001", VersionId.fromString("1.2.2-001").toString());
        assertEquals("1.3.0-rc2-w", VersionId.fromString("1.3.0-rc2-w").toString());
        assertEquals("1.2.3_build42", VersionId.fromString("1.2.3_build42").toString());

        // legal version-ids (exotic)
        assertEquals("A", VersionId.fromString("A").toString());
        assertEquals("A.B", VersionId.fromString("A.B").toString());
        assertEquals("A.B.1", VersionId.fromString("A.B.1").toString());
        assertEquals("A.B-1", VersionId.fromString("A.B-1").toString());
        assertEquals("1_3_0-rc2-w", VersionId.fromString("1_3_0-rc2-w").toString());
    }

    @Test
    public void testValidVersionIdsWithModifiers() {
        assertEquals("1+", VersionId.fromString("1+").toString());
        assertEquals("1.1+", VersionId.fromString("1.1+").toString());
        assertEquals("1.1.1+", VersionId.fromString("1.1.1+").toString());
        assertEquals("1.2.2-001+", VersionId.fromString("1.2.2-001+").toString());
        assertEquals("1.2.4_02+", VersionId.fromString("1.2.4_02+").toString());
        assertEquals("1.3.0-rc2-w+", VersionId.fromString("1.3.0-rc2-w+").toString());
        assertEquals("1.2.3_build42+", VersionId.fromString("1.2.3_build42+").toString());

        assertEquals("1*", VersionId.fromString("1*").toString());
        assertEquals("1.4*", VersionId.fromString("1.4*").toString());
        assertEquals("1.1_1*", VersionId.fromString("1.1_1*").toString());
    }

    @Ignore // until compound is implemented
    @Test
    public void testValidVersionIdsWithCompound() {
        // TODO
        fail();
    }

    @Test(expected = IllegalArgumentException.class)
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
        VersionId.fromString("1.0.0-build&run").toString();
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

    @Test
    public void testIsMatchOf() {
        assertTrue(VersionId.fromString("1.0").isMatchOf("1"));
        assertTrue(VersionId.fromString("1.0*").isMatchOf("1.0.4"));

        assertTrue(VersionId.fromString("1.5").isMatchOf("1.5"));
        assertTrue(VersionId.fromString("1.0").isMatchOf("1.0.0-0"));
        assertTrue(VersionId.fromString("1.3").isMatchOf("1.3.0"));
        assertTrue(VersionId.fromString("1.2.2.4").isMatchOf("1.2.2-004"));

        assertTrue(VersionId.fromString("1.5+").isMatchOf("1.5"));
        assertTrue(VersionId.fromString("1.5").isMatchOf("1.5+"));
        assertTrue(VersionId.fromString("1.5*").isMatchOf("1.5+"));
        assertTrue(VersionId.fromString("1.5+").isMatchOf("1.6"));
        assertFalse(VersionId.fromString("1.6").isMatchOf("1.5+"));

        assertFalse(VersionId.fromString("1.0.4").isMatchOf("1.0"));
        assertFalse(VersionId.fromString("1.0.4").isMatchOf("1.0*"));
    }

    @Test
    public void testIsExactMatchOf() {
        assertTrue(VersionId.fromString("1").isExactMatchOf(VersionId.fromString("1.0")));
        assertTrue(VersionId.fromString("1.0").isExactMatchOf(VersionId.fromString("1")));
        assertTrue(VersionId.fromString("1.0").isExactMatchOf(VersionId.fromString("1.0")));
        assertTrue(VersionId.fromString("1.3").isExactMatchOf(VersionId.fromString("1.3.0")));
        assertTrue(VersionId.fromString("1.3.0").isExactMatchOf(VersionId.fromString("1.3")));
        assertTrue(VersionId.fromString("1.2.2.4").isExactMatchOf(VersionId.fromString("1.2.2-004")));

        assertTrue(VersionId.fromString("1.5+").isExactMatchOf(VersionId.fromString("1.5+")));
        assertTrue(VersionId.fromString("1.5*").isExactMatchOf(VersionId.fromString("1.5*")));
        assertTrue(VersionId.fromString("1.5+").isExactMatchOf(VersionId.fromString("1.5*")));
        assertTrue(VersionId.fromString("1.5+").isExactMatchOf(VersionId.fromString("1.5")));
        assertTrue(VersionId.fromString("1.5").isExactMatchOf(VersionId.fromString("1.5+")));

        // no exact isMatchOf
        assertFalse(VersionId.fromString("1.0-build42").isExactMatchOf(VersionId.fromString("1.0.0-build42")));
        assertFalse(VersionId.fromString("1.0-b42").isExactMatchOf(VersionId.fromString("1.0-B42")));
        assertFalse(VersionId.fromString("1.5+").isExactMatchOf(VersionId.fromString("1.6")));
    }

    @Test
    public void testIsPrefixMatchOf() {
        assertTrue(VersionId.fromString("1.0").isPrefixMatchOf(VersionId.fromString("1.0.0")));
        assertTrue(VersionId.fromString("1.0*").isPrefixMatchOf(VersionId.fromString("1.0.0")));
        assertTrue(VersionId.fromString("1.0+").isPrefixMatchOf(VersionId.fromString("1.0.0")));
        assertTrue(VersionId.fromString("2.0*").isPrefixMatchOf(VersionId.fromString("2.0.1")));
        assertTrue(VersionId.fromString("1.2.1").isPrefixMatchOf(VersionId.fromString("1.2.1-004")));
        assertTrue(VersionId.fromString("1.2.0.0").isPrefixMatchOf(VersionId.fromString("1.2")));
        assertTrue(VersionId.fromString("1.2.2.4").isPrefixMatchOf(VersionId.fromString("1.2.2-004_beta")));

        assertFalse(VersionId.fromString("1").isPrefixMatchOf(VersionId.fromString("2.1.0")));
        assertFalse(VersionId.fromString("1.2").isPrefixMatchOf(VersionId.fromString("1.3")));
        assertFalse(VersionId.fromString("1.2.1").isPrefixMatchOf(VersionId.fromString("1.2.10")));
        assertFalse(VersionId.fromString("1.5+").isPrefixMatchOf(VersionId.fromString("1.6")));
        assertFalse(VersionId.fromString("1.5+").isPrefixMatchOf(VersionId.fromString("2.0.0")));
    }

   @Test
    public void testIsEqualTo() {
        assertTrue(VersionId.fromString("1.0").isEqualTo(VersionId.fromString("1")));
        assertTrue(VersionId.fromString("1.0").isEqualTo(VersionId.fromString("1.0")));
        assertTrue(VersionId.fromString("1.3").isEqualTo(VersionId.fromString("1.3.0")));
        assertTrue(VersionId.fromString("1.2.2.4").isEqualTo(VersionId.fromString("1.2.2-004")));

        // not equal
        assertFalse(VersionId.fromString("1.0").isEqualTo(null));
        assertFalse(VersionId.fromString("1.0-build42").isEqualTo(VersionId.fromString("1.0.0-build42")));
        assertFalse(VersionId.fromString("1.0-b42").isEqualTo(VersionId.fromString("1.0-B42")));
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

        // equal or less than
        assertFalse(VersionId.fromString("1.0").isGreaterThan(VersionId.fromString("1")));
        assertFalse(VersionId.fromString("1.0").isGreaterThan(VersionId.fromString("1.0")));
        assertFalse(VersionId.fromString("1.0.2").isGreaterThan(VersionId.fromString("1.1")));
        assertFalse(VersionId.fromString("1.0.0-build42").isGreaterThan(VersionId.fromString("1.0.1")));

        assertFalse(VersionId.fromString("1.5+").isGreaterThan(VersionId.fromString("1.5")));
        assertFalse(VersionId.fromString("1.5").isGreaterThan(VersionId.fromString("1.5+")));

        // numeric elements have lower precedence than non-numeric elements
        assertTrue(VersionId.fromString("1.0.A").isGreaterThan(VersionId.fromString("1.0.1")));
        assertTrue(VersionId.fromString("1.1.A").isGreaterThan(VersionId.fromString("1.0.B")));
        assertTrue(VersionId.fromString("1.1.ABC").isGreaterThan(VersionId.fromString("1.0.A")));
        assertTrue(VersionId.fromString("1.0.0-build42").isGreaterThan(VersionId.fromString("1.0.0-build41")));
        assertTrue(VersionId.fromString("1.0.0-build42").isGreaterThan(VersionId.fromString("1.0.0-42")));
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
        assertFalse(DOT.matches(REGEXP_CHAR));
        assertFalse(MINUS.matches(REGEXP_CHAR));
        assertFalse(UNDERSCORE.matches(REGEXP_CHAR));

        // illegal chars (modifiers)
        assertFalse(ASTERISK.matches(REGEXP_CHAR));
        assertFalse(PLUS.matches(REGEXP_CHAR));

        // illegal chars (others)
        assertFalse("".matches(REGEXP_CHAR));
        assertFalse(SPACE.matches(REGEXP_CHAR));
        assertFalse(AMPERSAND.matches(REGEXP_CHAR));
    }

    /**
     * Test all version-id separator tokens as defined by JSR-56 specification, Appendix A.
     */
    @Test
    public void testVersionIdSpecificationSeparator() {
        assertTrue(DOT.matches(REGEXP_SEPARATOR));
        assertTrue(MINUS.matches(REGEXP_SEPARATOR));
        assertTrue(UNDERSCORE.matches(REGEXP_SEPARATOR));
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
    public void testIsExactVersionId() {
        assertTrue(VersionId.fromString("1.0").isExactVersionId());
        assertTrue(VersionId.fromString("1.0.0-beta").isExactVersionId());

        assertFalse(VersionId.fromString("1.0+").isExactVersionId());
        assertFalse(VersionId.fromString("1.0.0-beta*").isExactVersionId());
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