package net.adoptopenjdk.icedteaweb.jnlp.element.resource;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * ...
 */
public class PackageDescTest {

    private static final String PART = "part1";
    private static final boolean RECURSIVE = true;
    private static final boolean NOT_RECURSIVE = false;

    private static final String ROOT_PACKAGE = "foo.";
    private static final String PACKAGE1 = ROOT_PACKAGE + "bar.bar.";
    private static final String PACKAGE2 = ROOT_PACKAGE + "ele.fant.";
    private static final String CLASS1 = PACKAGE1 + "SomeClass";
    private static final String CLASS2 = PACKAGE1 + "OtherClass";
    private static final String CLASS3 = PACKAGE2 + "SomeClass";
    private static final String CLASS4 = PACKAGE2 + "OtherClass";
    private static final String SUFFIX = "*";

    @Test
    public void testExactClassName() {
        final PackageDesc packageDesc = new PackageDesc(CLASS1, PART, NOT_RECURSIVE);

        assertTrue(packageDesc.matches(CLASS1));
        assertFalse(packageDesc.matches(CLASS2));
        assertFalse(packageDesc.matches(CLASS3));
        assertFalse(packageDesc.matches(CLASS4));
    }

    @Test
    public void testPackageNameNotRecursive() {
        final PackageDesc packageDesc = new PackageDesc(PACKAGE1 + SUFFIX, PART, NOT_RECURSIVE);

        assertTrue(packageDesc.matches(CLASS1));
        assertTrue(packageDesc.matches(CLASS2));
        assertFalse(packageDesc.matches(CLASS3));
        assertFalse(packageDesc.matches(CLASS4));
    }

    @Test
    public void testPackageNameRecursive() {
        final PackageDesc packageDesc = new PackageDesc(PACKAGE1 + SUFFIX, PART, RECURSIVE);

        assertTrue(packageDesc.matches(CLASS1));
        assertTrue(packageDesc.matches(CLASS2));
        assertFalse(packageDesc.matches(CLASS3));
        assertFalse(packageDesc.matches(CLASS4));
    }

    @Test
    public void testRootPackageNameNotRecursive() {
        final PackageDesc packageDesc = new PackageDesc(ROOT_PACKAGE + SUFFIX, PART, NOT_RECURSIVE);

        assertFalse(packageDesc.matches(CLASS1));
        assertFalse(packageDesc.matches(CLASS2));
        assertFalse(packageDesc.matches(CLASS3));
        assertFalse(packageDesc.matches(CLASS4));
    }

    @Test
    public void testRootPackageNameRecursive() {
        final PackageDesc packageDesc = new PackageDesc(ROOT_PACKAGE + SUFFIX, PART, RECURSIVE);

        assertTrue(packageDesc.matches(CLASS1));
        assertTrue(packageDesc.matches(CLASS2));
        assertTrue(packageDesc.matches(CLASS3));
        assertTrue(packageDesc.matches(CLASS4));
    }
}
