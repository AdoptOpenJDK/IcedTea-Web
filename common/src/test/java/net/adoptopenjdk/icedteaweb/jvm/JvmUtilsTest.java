package net.adoptopenjdk.icedteaweb.jvm;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
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
    public void testValidJnlpPrefixProperty() {
        final String property = "jnlp.abc";
        final boolean result = JvmUtils.isValidSecureProperty(property);
        assertTrue(result);
    }

    @Test
    public void testValidJavawsPrefixProperty() {
        final String property = "javaws.efg";
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
        final String javaVMArgs = "-Dsun.java2d.d3d=true -Dsun.java2d.dpiaware=false -Djnlp.abc=def -Djavaws.xyz=uvw";
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

    @Test
    public void testJava9JavaVMArgs() {
        final String javaVMArgs = "--add-modules=java.scripting,java.sql --add-exports=java.base/sun.security.util=ALL-UNNAMED --add-exports=java.base/sun.security.x509=ALL-UNNAMED --add-exports=java.desktop/com.apple.eawt=ALL-UNNAMED --add-exports=java.desktop/com.sun.imageio.spi=ALL-UNNAMED --add-exports=java.desktop/com.sun.imageio.plugins.jpeg=ALL-UNNAMED --add-exports=javafx.graphics/com.sun.javafx.application=ALL-UNNAMED --add-exports=jdk.deploy/com.sun.deploy.config=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/jdk.internal.loader=ALL-UNNAMED --add-opens=java.base/jdk.internal.ref=ALL-UNNAMED --add-opens=java.desktop/javax.imageio.spi=ALL-UNNAMED --add-opens=java.desktop/javax.swing.text.html=ALL-UNNAMED --add-opens=java.prefs/java.util.prefs=ALL-UNNAMED";
        try {
            JvmUtils.checkVMArgs(javaVMArgs);
        } catch (IllegalArgumentException ile) {
            fail(ile.getMessage());
        }
    }

    @Test
    public void testJava9JavaVMArgsVolta() {
        final String java_vm_args = "-Dsun.java2d.d3d=false -Dsun.java2d.dpiaware=false --add-opens=java.desktop/sun.print=ALL-UNNAMED --add-exports=java.desktop/sun.print=ALL-UNNAMED --add-exports=java.desktop/sun.swing=ALL-UNNAMED --add-exports=java.desktop/sun.swing.table=ALL-UNNAMED --add-exports=java.desktop/sun.swing.plaf.synth=ALL-UNNAMED --add-opens=java.desktop/javax.swing.plaf.synth=ALL-UNNAMED --add-opens=java.desktop/javax.swing.plaf.basic=ALL-UNNAMED --add-opens=java.desktop/javax.swing=ALL-UNNAMED --add-opens=java.desktop/javax.swing.tree=ALL-UNNAMED --add-opens=java.desktop/java.awt.event=ALL-UNNAMED --add-exports=java.desktop/com.sun.java.swing.plaf.windows=ALL-UNNAMED --add-exports=java.desktop/sun.awt.shell=ALL-UNNAMED --add-exports=java.desktop/com.sun.awt=ALL-UNNAMED --add-exports=java.base/sun.security.action=ALL-UNNAMED";
        try {
            JvmUtils.checkVMArgs(java_vm_args);
        } catch (IllegalArgumentException ile) {
            fail(ile.getMessage());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJava9JavaBadVMArgs() {
        final String java_vm_bad_args = "-Dsun.java2d.d3d=false -Dsun.java2d.dpiaware=false  --add-opens=java.desktop/sun.print=ALL-UNNAMED --list-modules --add-exports=java.desktop/sun.print=ALL-UNNAMED --add-exports=java.desktop/sun.swing=ALL-UNNAMED --add-exports=java.desktop/sun.swing.table=ALL-UNNAMED --add-exports=java.desktop/sun.swing.plaf.synth=ALL-UNNAMED --add-opens=java.desktop/javax.swing.plaf.synth=ALL-UNNAMED --add-opens=java.desktop/javax.swing.plaf.basic=ALL-UNNAMED --add-opens=java.desktop/javax.swing=ALL-UNNAMED --add-opens=java.desktop/javax.swing.tree=ALL-UNNAMED --add-opens=java.desktop/java.awt.event=ALL-UNNAMED --add-exports=java.desktop/com.sun.java.swing.plaf.windows=ALL-UNNAMED --add-exports=java.desktop/sun.awt.shell=ALL-UNNAMED --add-exports=java.desktop/com.sun.awt=ALL-UNNAMED --add-exports=java.base/sun.security.action=ALL-UNNAMED";
        JvmUtils.checkVMArgs(java_vm_bad_args);
    }

    @Test
    public void testPredefModuleVMArgs() {

        final List<String> result = JvmUtils.mergeJavaModulesVMArgs(Collections.emptyList());

        assertEquals(JvmUtils.getPredefinedJavaModulesVMArgumentsMap().size(), result.size());
        assertTrue(result.contains("--add-reads=java.base=ALL-UNNAMED,java.desktop"));
        assertTrue(result.contains("--add-exports=java.desktop/sun.applet=ALL-UNNAMED,java.desktop,jdk.jsobject"));
    }

    @Test
    public void testMergeJavaModuleVMArgs() {

        final String[] usrStrArr = new String[]{"-DnoModuleArg=bbb", "--add-reads=java.base=ALL-UNNAMED,java.xxx"};
        final List<String> usrDefArgs = new ArrayList(Arrays.asList(usrStrArr));

        final List<String> result = JvmUtils.mergeJavaModulesVMArgs(usrDefArgs);
        assertEquals(JvmUtils.getPredefinedJavaModulesVMArgumentsMap().size() + 1, result.size());
        assertTrue(result.contains("-DnoModuleArg=bbb"));
        assertTrue(result.contains("--add-reads=java.base=ALL-UNNAMED,java.desktop,java.xxx"));
        assertTrue(!result.contains("--add-reads=java.base=ALL-UNNAMED,java.xxx"));
    }

    @Test
    public void testMergeDuplicateJavaModuleVMArgs() {

        final String[] usrStrArr = new String[]{"-DnoModuleArg=bbb", "--add-reads=java.base=ALL-UNNAMED,java.xxx", "--add-reads=java.base=ALL-UNNAMED,java.yyy"};
        final List<String> usrDefArgs = new ArrayList(Arrays.asList(usrStrArr));

        final List<String> result = JvmUtils.mergeJavaModulesVMArgs(usrDefArgs);
        assertTrue(result.contains("-DnoModuleArg=bbb"));
        assertTrue(result.contains("--add-reads=java.base=ALL-UNNAMED,java.desktop,java.xxx,java.yyy"));
        assertTrue(!result.contains("--add-reads=java.base=ALL-UNNAMED,java.xxx"));
        assertTrue(!result.contains("--add-reads=java.base=ALL-UNNAMED,java.yyy"));
    }

    @Test
    public void testNonPredefJavaModuleVMArgs() {
        final String[] usrStrArr = new String[]{"-DnoModuleArg=bbb", "--module-path=java.base=java.xxx", "--add-opens=java.base=java.aaa", "--add-modules=java.base=java.bbb", "--patch-module=java.base=java.ccc", "--add-reads=java.base=ALL-UNNAMED,java.yyy"};
        final List<String> usrDefArgs = new ArrayList(Arrays.asList(usrStrArr));

        final List<String> result = JvmUtils.mergeJavaModulesVMArgs(usrDefArgs);
        assertTrue(result.contains("-DnoModuleArg=bbb"));
        assertTrue(result.contains("--add-reads=java.base=ALL-UNNAMED,java.desktop,java.yyy"));
        assertTrue(result.contains("--module-path=java.base=java.xxx"));
        assertTrue(result.contains("--add-opens=java.base=java.aaa"));
        assertTrue(result.contains("--add-modules=java.base=java.bbb"));
        assertTrue(result.contains("--patch-module=java.base=java.ccc"));
    }

    @Test
    public void testValidJavaModuleVMArgs() {
        final String javaVMArgs = "-Dsun.java2d.d3d=true --add-reads=java.base=ALL-UNNAMED,java.desktop,java.yyy --module-path=java.base=java.xxx --add-opens=java.base=java.aaa --add-modules=java.base=java.bbb --patch-module=java.base=java.ccc";
        try {
            JvmUtils.checkVMArgs(javaVMArgs);
        } catch (IllegalArgumentException ile) {
            fail(ile.getMessage());
        }
    }

    @Test
    public void testValidJavaModuleVMArgsGithubIssue() {
        final String java_vm_args = "-Djnlp.ccc=ccc -XX:SurvivorRatio=6 -Xmx512m -Xms128m -XX:NewSize=96m -XX:MinHeapFreeRatio=20 -XX:MaxHeapFreeRatio=30 --add-modules=java.scripting,java.sql --add-exports=java.base/sun.security.util=ALL-UNNAMED --add-exports=java.base/sun.security.x509=ALL-UNNAMED --add-exports=java.desktop/com.apple.eawt=ALL-UNNAMED --add-exports=java.desktop/com.sun.imageio.spi=ALL-UNNAMED --add-exports=java.desktop/com.sun.imageio.plugins.jpeg=ALL-UNNAMED --add-exports=javafx.graphics/com.sun.javafx.application=ALL-UNNAMED --add-exports=jdk.deploy/com.sun.deploy.config=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/jdk.internal.loader=ALL-UNNAMED --add-opens=java.base/jdk.internal.ref=ALL-UNNAMED --add-opens=java.desktop/javax.imageio.spi=ALL-UNNAMED --add-opens=java.desktop/javax.swing.text.html=ALL-UNNAMED --add-opens=java.prefs/java.util.prefs=ALL-UNNAMED";
        try {
            JvmUtils.checkVMArgs(java_vm_args);
        } catch (IllegalArgumentException ile) {
            fail(ile.getMessage());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvaldiJavaModuleVMArgs() {
        final String usrArgs = "-Dsun.java2d.d3d=true --list-module=java.base=ALL-UNNAMED,java.xxx --add-reads=java.base=ALL-UNNAMED,java.yyy";
        JvmUtils.checkVMArgs(usrArgs);
    }

    @Test
    public void testMoreThanOneBlankBtwnArgs() {
        final String java_vm_args = "   -Djnlp.ccc=ccc  -XX:SurvivorRatio=6    -Dsun.java2d.d3d=true   ";
        try {
            JvmUtils.checkVMArgs(java_vm_args);
        } catch (IllegalArgumentException ile) {
            fail(ile.getMessage());
        }
    }
}