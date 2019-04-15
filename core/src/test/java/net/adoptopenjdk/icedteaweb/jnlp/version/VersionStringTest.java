package net.adoptopenjdk.icedteaweb.jnlp.version;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import static net.adoptopenjdk.icedteaweb.jnlp.version.VersionId.DOT;
import static net.adoptopenjdk.icedteaweb.jnlp.version.VersionId.MINUS;
import static net.adoptopenjdk.icedteaweb.jnlp.version.VersionId.REGEXP_SEPARATOR;
import static net.adoptopenjdk.icedteaweb.jnlp.version.VersionId.REGEXP_VERSION_ID;
import static net.adoptopenjdk.icedteaweb.jnlp.version.VersionId.UNDERSCORE;
import static net.adoptopenjdk.icedteaweb.jnlp.version.VersionString.REGEXP_VERSION_RANGE;
import static net.adoptopenjdk.icedteaweb.jnlp.version.VersionString.REGEXP_VERSION_STRING;

public class VersionStringTest {
    @Ignore // until compound is implemented
    @Test
    public void testSimpleVersions() {
        // legal version-ids (typical)
        Assert.assertEquals("1.4.0_04 1.4*&amp;1.4.1_02+", VersionString.fromString("1.4.0_04 1.4*&amp;1.4.1_02+").toString());
    }

    @Test
    public void testSimpleRange() {
        // simple range with only one element (most simplest case)
        Assert.assertEquals("1.0", VersionString.fromString("1.0").toString());
        Assert.assertEquals("1.0+", VersionString.fromString("1.0+").toString());
        Assert.assertEquals("1.0*", VersionString.fromString("1.0*").toString());
        Assert.assertEquals("1_0_0-build42*", VersionString.fromString("1_0_0-build42*").toString());

        // simple range, exact version-ids
        Assert.assertEquals("1 2", VersionString.fromString("1 2").toString());
        Assert.assertEquals("1.0 2.0 3.0", VersionString.fromString("1.0 2.0 3.0").toString());

        // simple range with modifiers
        Assert.assertEquals("1.5+ 2.0", VersionString.fromString("1.5+ 2.0").toString());
        Assert.assertEquals("1.4+ 1.6+", VersionString.fromString("1.4+ 1.6+").toString());
        Assert.assertEquals("1.0* 2.0*", VersionString.fromString("1.0* 2.0*").toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullVersionString() {
        VersionString.fromString(null).toString();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyVersionString() {
        VersionString.fromString("").toString();
    }

    @Test
    public void testContains() {
        Assert.assertTrue(VersionString.fromString("1.0+").contains("1.0"));
        Assert.assertTrue(VersionString.fromString("1.0+").contains("1.1"));
        Assert.assertTrue(VersionString.fromString("1.0+").contains("1.1.5"));
        Assert.assertTrue(VersionString.fromString("1.0+").contains("6.0-beta"));
        Assert.assertTrue(VersionString.fromString("1.0+").contains("2.0.0"));
        Assert.assertTrue(VersionString.fromString("1.0*").contains("1.0.5"));
        Assert.assertTrue(VersionString.fromString("1.2+").contains("1.2"));
        Assert.assertTrue(VersionString.fromString("1.2+").contains("1.3"));
        Assert.assertTrue(VersionString.fromString("1.2+").contains("2.0"));
        Assert.assertTrue(VersionString.fromString("1.2+").contains("1.3.1"));
        Assert.assertTrue(VersionString.fromString("1.2+").contains("1.2.1"));
        Assert.assertTrue(VersionString.fromString("1.2+").contains("1.3.1-beta"));

        Assert.assertTrue(VersionString.fromString("1.0 2.0").contains("1.0"));
        Assert.assertTrue(VersionString.fromString("1.0 2.0").contains("2.0"));
        Assert.assertTrue(VersionString.fromString("1.0+ 2.0").contains("1.5"));
        Assert.assertTrue(VersionString.fromString("1.0+ 2.0").contains("1.5"));
        Assert.assertTrue(VersionString.fromString("1.0* 2.0").contains("1.0.5"));
        Assert.assertTrue(VersionString.fromString("1.1* 1.3*").contains("1.1"));
        Assert.assertTrue(VersionString.fromString("1.1* 1.3*").contains("1.1.8"));
        Assert.assertTrue(VersionString.fromString("1.1* 1.3*").contains("1.3"));
        Assert.assertTrue(VersionString.fromString("1.1* 1.3*").contains("1.3.1"));
        Assert.assertTrue(VersionString.fromString("1.1* 1.3*").contains("1.3.1-beta"));

        Assert.assertFalse(VersionString.fromString("1.2+").contains("1.1"));
        Assert.assertFalse(VersionString.fromString("1.2+").contains("1.1.8"));
        Assert.assertFalse(VersionString.fromString("1.0 1.2").contains("1.3"));
        Assert.assertFalse(VersionString.fromString("1.0 2.0").contains("1.5"));
        Assert.assertFalse(VersionString.fromString("1.1* 1.3*").contains("1.2"));
        Assert.assertFalse(VersionString.fromString("1.1* 1.3*").contains("2.0"));
        Assert.assertFalse(VersionString.fromString("1.1* 1.3*").contains("1.2.1"));
    }

    @Test
    public void testContainsGreaterThan() {
        Assert.assertTrue(VersionString.fromString("2.0").containsGreaterThan("1.5"));
        Assert.assertTrue(VersionString.fromString("1.1+").containsGreaterThan("1.0"));
        Assert.assertTrue(VersionString.fromString("1.1*").containsGreaterThan("1.0.1"));
        Assert.assertTrue(VersionString.fromString("5.0+").containsGreaterThan("3.1"));

        Assert.assertFalse(VersionString.fromString("1.0+").containsGreaterThan("1.0"));
        Assert.assertFalse(VersionString.fromString("1.0*").containsGreaterThan("1.0.5"));

        Assert.assertTrue(VersionString.fromString("1.0 2.0").containsGreaterThan("1.0"));
        Assert.assertTrue(VersionString.fromString("1.0+ 2.0").containsGreaterThan("1.5"));
        Assert.assertTrue(VersionString.fromString("1.0 2.0 3.0").containsGreaterThan("2.0.0"));
    }

    @Test
    public void testToString() {
        Assert.assertEquals("2.0", VersionString.fromString("2.0").toString());
        Assert.assertEquals("1.0 2.0", VersionString.fromString("1.0 2.0").toString());
        Assert.assertEquals("1.0+ 2.0*", VersionString.fromString("1.0+ 2.0*").toString());
    }

    /**
     * Test all version-string expressions as defined by JSR-56 specification, Appendix A.
     */
    @Test
    public void testVersionIdSpecificationVersionString() {
        // version-string with only one element
        Assert.assertTrue("1.0".matches(REGEXP_VERSION_STRING));
        Assert.assertTrue("1.0+".matches(REGEXP_VERSION_STRING));
        Assert.assertTrue("1.0*".matches(REGEXP_VERSION_STRING));
        Assert.assertTrue("1.1.0_build42*".matches(REGEXP_VERSION_STRING));

        // version-string, exact version-ids
        Assert.assertTrue("1 2".matches(REGEXP_VERSION_STRING));
        Assert.assertTrue("1.0 3.0".matches(REGEXP_VERSION_STRING));

        // version-string with modifiers
        Assert.assertTrue("1.5+ 2.0.3".matches(REGEXP_VERSION_STRING));
        Assert.assertTrue("1.4+ 1.6+".matches(REGEXP_VERSION_STRING));
        Assert.assertTrue("1.0* 2.0*".matches(REGEXP_VERSION_STRING));

        // version-string with compound ampersand
        Assert.assertTrue("1.4.0_04 1.4*&amp;1.4.1_02".matches(REGEXP_VERSION_STRING));
    }

    /**
     * Test all version-range expressions as defined by JSR-56 specification, Appendix A.
     */
    @Test
    public void testVersionIdSpecificationVersionRange() {
        Assert.assertTrue("1.0".matches(REGEXP_VERSION_RANGE));
        Assert.assertTrue("1.0+".matches(REGEXP_VERSION_RANGE));
        Assert.assertTrue("1.4*".matches(REGEXP_VERSION_RANGE));
        Assert.assertTrue("1.4*&amp;1.4.1_02".matches(REGEXP_VERSION_RANGE));
    }

    /**
     * Test all simple-range expressions as defined by JSR-56 specification, Appendix A.
     */
    @Test
    public void testVersionIdSpecificationSimpleRange() {
        // simple range with only one element
        Assert.assertTrue("1.1".matches(REGEXP_VERSION_ID));
        Assert.assertTrue("1.1+".matches(REGEXP_VERSION_ID));
        Assert.assertTrue("1.1*".matches(REGEXP_VERSION_ID));
        Assert.assertTrue("1.1.0_build42*".matches(REGEXP_VERSION_ID));
    }

    /**
     * Test all version string modifiers as defined by JSR-56 specification, Appendix A.
     */
    @Test
    public void testVersionIdSpecificationSeparator() {
        Assert.assertTrue(DOT.matches(REGEXP_SEPARATOR));
        Assert.assertTrue(MINUS.matches(REGEXP_SEPARATOR));
        Assert.assertTrue(UNDERSCORE.matches(REGEXP_SEPARATOR));
    }
}