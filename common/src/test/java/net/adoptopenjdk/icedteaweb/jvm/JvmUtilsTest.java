package net.adoptopenjdk.icedteaweb.jvm;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class JvmUtilsTest {

    @Test
    public void testValidProperty() {
        final String property = "sun.java2d.d3d";
        final boolean result = JvmUtils.isValidSecureProperty(property);
        assertTrue(result);
    }

    @Test
    public void testInvalidProperty() {
        final String property = "-Dsun.java2d.d3d";
        final boolean result = JvmUtils.isValidSecureProperty(property);
        assertFalse(result);
    }

    @Test
    public void testValidPropertyAsJavaVMArg() {
        final String javaVMArg = "-Dsun.java2d.d3d=true";
        final boolean result = JvmUtils.isSecurePropertyAValidJVMArg(javaVMArg);
        assertTrue(result);
    }

    @Test
    public void testInvalidPropertyAsJavaVMArgWithMissingProperty() {
        final String javaVMArg = "-D";
        final boolean result = JvmUtils.isSecurePropertyAValidJVMArg(javaVMArg);
        assertFalse(result);
    }

    @Test
    public void testValidPropertyAsJavaVMArgWithoutEqual() {
        final String javaVMArg = "-Dsun.java2d.d3d";
        final boolean result = JvmUtils.isSecurePropertyAValidJVMArg(javaVMArg);
        assertTrue(result);
    }

    @Test
    public void testValidPropertyAsJavaVMArgWithMissingValue() {
        final String javaVMArg = "-Dsun.java2d.d3d=";
        final boolean result = JvmUtils.isSecurePropertyAValidJVMArg(javaVMArg);
        assertTrue(result);
    }

    @Test
    public void testValidPropertyInJavaVMArgs() {
        final String javaVMArgs = "-Dsun.java2d.d3d=true -Dsun.java2d.dpiaware=false";
        try {
            JvmUtils.checkVMArgs(javaVMArgs);
        } catch (IllegalArgumentException ile) {
            fail(ile.getMessage());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidPropertyInJavaVMArgs() {
        final String javaVMArgs = "-Dsun.java2d.d3d=true -Dunknown=x";
        JvmUtils.checkVMArgs(javaVMArgs);
    }

    @Test
    public void testJava8JavaVMArgs() {
        final String javaVMArgs = "-XX:SurvivorRatio=6 -XX:PrintCMSStatistics=8 -XX:ParallelGCThreads=5 -XX:+UseParallelOldGC -XX:-UseParallelOldGC -XX:+UseParallelScavenge -XX:-UseParallelScavenge";
        try {
            JvmUtils.checkVMArgs(javaVMArgs);
        } catch (IllegalArgumentException ile) {
            fail(ile.getMessage());
        }
    }
}