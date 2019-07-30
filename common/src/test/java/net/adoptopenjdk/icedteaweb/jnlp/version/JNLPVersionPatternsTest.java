package net.adoptopenjdk.icedteaweb.jnlp.version;

import org.junit.Test;

import static net.adoptopenjdk.icedteaweb.jnlp.version.JNLPVersionPatterns.REGEXP_CHAR;
import static net.adoptopenjdk.icedteaweb.jnlp.version.JNLPVersionPatterns.REGEXP_SEPARATOR;
import static net.adoptopenjdk.icedteaweb.jnlp.version.JNLPVersionPatterns.REGEXP_SIMPLE_RANGE;
import static net.adoptopenjdk.icedteaweb.jnlp.version.JNLPVersionPatterns.REGEXP_STRING;
import static net.adoptopenjdk.icedteaweb.jnlp.version.JNLPVersionPatterns.REGEXP_VERSION_ID;
import static net.adoptopenjdk.icedteaweb.jnlp.version.JNLPVersionPatterns.REGEXP_VERSION_RANGE;
import static net.adoptopenjdk.icedteaweb.jnlp.version.JNLPVersionPatterns.REGEXP_VERSION_STRING;
import static net.adoptopenjdk.icedteaweb.jnlp.version.VersionModifier.AMPERSAND;
import static net.adoptopenjdk.icedteaweb.jnlp.version.VersionModifier.ASTERISK;
import static net.adoptopenjdk.icedteaweb.jnlp.version.VersionModifier.PLUS;
import static net.adoptopenjdk.icedteaweb.jnlp.version.VersionSeparator.DOT;
import static net.adoptopenjdk.icedteaweb.jnlp.version.VersionSeparator.MINUS;
import static net.adoptopenjdk.icedteaweb.jnlp.version.VersionSeparator.SPACE;
import static net.adoptopenjdk.icedteaweb.jnlp.version.VersionSeparator.UNDERSCORE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for ensuring the REGEXP are OK.
 */
public class JNLPVersionPatternsTest {

  @Test
  public void testRegexpForVersionId() {
    assertTrue("1.0".matches(REGEXP_VERSION_ID));
    assertTrue("1-0".matches(REGEXP_VERSION_ID));
    assertTrue("1.1.1".matches(REGEXP_VERSION_ID));
    assertTrue("1_1_1".matches(REGEXP_VERSION_ID));
    assertTrue("1.1.1-beta".matches(REGEXP_VERSION_ID));

    assertFalse("1.0+".matches(REGEXP_VERSION_ID));
    assertFalse("1.0*".matches(REGEXP_VERSION_ID));
    assertFalse("1.0&".matches(REGEXP_VERSION_ID));
    assertFalse("1&1".matches(REGEXP_VERSION_ID));
    assertFalse("1+1".matches(REGEXP_VERSION_ID));
    assertFalse("1*1".matches(REGEXP_VERSION_ID));

    assertFalse("".matches(REGEXP_VERSION_ID));
    assertFalse("1.".matches(REGEXP_VERSION_ID));
    assertFalse("1-".matches(REGEXP_VERSION_ID));
    assertFalse("1_".matches(REGEXP_VERSION_ID));
    assertFalse("1..1".matches(REGEXP_VERSION_ID));
    assertFalse("10 0".matches(REGEXP_VERSION_ID));
  }

  @Test
  public void testRegexpForSimpleRange() {
    assertTrue("1.1".matches(REGEXP_SIMPLE_RANGE));
    assertTrue("1.1*".matches(REGEXP_SIMPLE_RANGE));
    assertTrue("1.1+".matches(REGEXP_SIMPLE_RANGE));
    assertTrue("1.1.0_build42*".matches(REGEXP_SIMPLE_RANGE));

    assertFalse("1.+".matches(REGEXP_SIMPLE_RANGE));
    assertFalse("1.2_*".matches(REGEXP_SIMPLE_RANGE));
    assertFalse("1.*".matches(REGEXP_SIMPLE_RANGE));
    assertFalse("1.2-*".matches(REGEXP_SIMPLE_RANGE));
  }

  @Test
  public void testRegexpForString() {
    assertTrue("12".matches(REGEXP_STRING));
    assertTrue("abc".matches(REGEXP_STRING));
    assertTrue("V1".matches(REGEXP_STRING));

    assertFalse("1 2".matches(REGEXP_STRING));
    assertFalse("1.2".matches(REGEXP_STRING));
    assertFalse("1-2".matches(REGEXP_STRING));
    assertFalse("1_2".matches(REGEXP_STRING));
    assertFalse("1*1".matches(REGEXP_STRING));
    assertFalse("1+1".matches(REGEXP_STRING));
    assertFalse("1&1".matches(REGEXP_STRING));
    assertFalse("12*".matches(REGEXP_STRING));
    assertFalse("12+".matches(REGEXP_STRING));
    assertFalse("12&".matches(REGEXP_STRING));
  }

  /**
   * Test all version-id char tokens as defined by JSR-56 specification, Appendix A.
   */
  @Test
  public void testRegexpForChar() {
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
  public void testRegexpForSeparator() {
    assertTrue(DOT.symbol().matches(REGEXP_SEPARATOR));
    assertTrue(MINUS.symbol().matches(REGEXP_SEPARATOR));
    assertTrue(UNDERSCORE.symbol().matches(REGEXP_SEPARATOR));
  }


  /**
   * Test all version-string expressions as defined by JSR-56 specification, Appendix A.
   */
  @Test
  public void testVersionIdSpecificationVersionString() {
    // version-string with only one element
    assertTrue("1.0".matches(REGEXP_VERSION_STRING));
    assertTrue("1.0+".matches(REGEXP_VERSION_STRING));
    assertTrue("1.0*".matches(REGEXP_VERSION_STRING));
    assertTrue("1.1.0_build42*".matches(REGEXP_VERSION_STRING));

    // version-string, exact version-ids
    assertTrue("1 2".matches(REGEXP_VERSION_STRING));
    assertTrue("1.0 3.0".matches(REGEXP_VERSION_STRING));

    // version-string with modifiers
    assertTrue("1.5+ 2.0.3".matches(REGEXP_VERSION_STRING));
    assertTrue("1.4+ 1.6+".matches(REGEXP_VERSION_STRING));
    assertTrue("1.0* 2.0*".matches(REGEXP_VERSION_STRING));

    // version-string with compound ampersand
    assertTrue("1.4.0_04 1.4*&amp;1.4.1_02".matches(REGEXP_VERSION_STRING));
  }

  /**
   * Test all version-range expressions as defined by JSR-56 specification, Appendix A.
   */
  @Test
  public void testVersionIdSpecificationVersionRange() {
    assertTrue("1.0".matches(REGEXP_VERSION_RANGE));
    assertTrue("1.0+".matches(REGEXP_VERSION_RANGE));
    assertTrue("1.4*".matches(REGEXP_VERSION_RANGE));
    assertTrue("1.4*&amp;1.4.1_02".matches(REGEXP_VERSION_RANGE));
  }
}
