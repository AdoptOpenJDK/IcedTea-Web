/* 
 Copyright (C) 2012 Red Hat, Inc.

 This file is part of IcedTea.

 IcedTea is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by
 the Free Software Foundation, version 2.

 IcedTea is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with IcedTea; see the file COPYING.  If not, write to
 the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 02110-1301 USA.

 Linking this library statically or dynamically with other modules is
 making a combined work based on this library.  Thus, the terms and
 conditions of the GNU General Public License cover the whole
 combination.

 As a special exception, the copyright holders of this library give you
 permission to link this library with independent modules to produce an
 executable, regardless of the license terms of these independent
 modules, and to copy and distribute the resulting executable under
 terms of your choice, provided that you also meet, for each linked
 independent module, the terms and conditions of the license of that
 module.  An independent module is a module which is not derived from
 or based on this library.  If you modify this library, you may extend
 this exception to your version of the library, but you are not
 obligated to do so.  If you do not wish to do so, delete this
 exception statement from your version.
 */
package net.sourceforge.jnlp.config;

import net.adoptopenjdk.icedteaweb.config.validators.DirectoryCheckResult;
import net.adoptopenjdk.icedteaweb.config.validators.DirectoryCheckResults;
import net.adoptopenjdk.icedteaweb.config.validators.DirectoryValidator;
import net.adoptopenjdk.icedteaweb.testing.AnnotationConditionChecker;
import net.adoptopenjdk.icedteaweb.testing.annotations.WindowsIssue;
import net.sourceforge.jnlp.util.logging.NoStdOutErrTest;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;

public class DirectoryValidatorTest extends NoStdOutErrTest{

    @Rule
    public final AnnotationConditionChecker acc = new AnnotationConditionChecker();

    @Test
    public void testMainDirTestNotExists() {
        DirectoryCheckResult result = DirectoryValidator.testDir(new File("/definitely/not/existing/file/efgrhisaes"), false, false);
        String s = result.getMessage();
        assertTrue(s.endsWith("\n"));
        assertTrue(s.split("\n").length == 3);
    }

    @Test
    public void testMainDirTestExistsAsFile() throws IOException {
        File f = File.createTempFile("test", "testMainDirs");
        f.deleteOnExit();
        DirectoryCheckResult result = DirectoryValidator.testDir(f, false, false);
        String s = result.getMessage();
        assertTrue(s.endsWith("\n"));
        assertTrue(s.split("\n").length == 2);
    }

    @Test
    public void testMainDirTestExistsAsDir() throws IOException {
        File f = createTempDir();
        DirectoryCheckResult result = DirectoryValidator.testDir(f, false, false);
        String s = result.getMessage();
        assertTrue(s.isEmpty());
    }

    @Test
    @WindowsIssue
    public void testMainDirTestExistsAsDirButNotWritable() throws IOException {
        File f = createTempDir();
        assertTrue(f.setWritable(false));
        DirectoryCheckResult result = DirectoryValidator.testDir(f, false, false);
        String s = result.getMessage();
        assertTrue(s.endsWith("\n"));
        assertTrue(s.split("\n").length == 1);
        assertTrue(f.setWritable(true));
    }

    @Test
    @WindowsIssue
    public void testMainDirTestExistsAsDirButNotReadable() throws IOException {
        File f = createTempDir();
        assertTrue(f.setReadable(false));
        DirectoryCheckResult result = DirectoryValidator.testDir(f, false, false);
        String s = result.getMessage();
        assertTrue(s.endsWith("\n"));
        assertTrue(s.split("\n").length == 1);
        assertTrue(f.setReadable(true));
    }

    @Test
    public void testMainDirTestNotExistsWithSubdir() {
        DirectoryCheckResult result = DirectoryValidator.testDir(new File("/definitely/not/existing/file/efgrhisaes"), false, true);
        String s = result.getMessage();
        assertTrue(s.endsWith("\n"));
        assertTrue(s.split("\n").length == 3);
    }

    @Test
    public void testMainDirTestExistsAsFileWithSubdir() throws IOException {
        File f = File.createTempFile("test", "testMainDirs");
        f.deleteOnExit();
        DirectoryCheckResult result = DirectoryValidator.testDir(f, false, true);
        String s = result.getMessage();
        assertTrue(s.endsWith("\n"));
        assertTrue(s.split("\n").length == 2);
    }

    @Test
    public void testMainDirTestExistsAsDirWithSubdir() throws IOException {
        File f = createTempDir();
        DirectoryCheckResult result = DirectoryValidator.testDir(f, false, true);
        String s = result.getMessage();
        assertTrue(s.isEmpty());
    }

    @Test
    @WindowsIssue
    public void testMainDirTestExistsAsDirButNotWritableWithSubdir() throws IOException {
        File f = createTempDir();
        assertTrue(f.setWritable(false));
        DirectoryCheckResult result = DirectoryValidator.testDir(f, false, true);
        String s = result.getMessage();
        assertTrue(s.endsWith("\n"));
        assertTrue(s.split("\n").length == 1);
        assertTrue(f.setWritable(true));
    }

    @Test
    @WindowsIssue
    public void testMainDirTestExistsAsDirButNotReadableWithSubdir() throws IOException {
        File f = createTempDir();
        f.setReadable(false);
        DirectoryCheckResult result = DirectoryValidator.testDir(f, false, true);
        String s = result.getMessage();
        assertTrue(s.endsWith("\n"));
        assertTrue(s.split("\n").length == 1);
    }

    @Test
    public void testDirectoryCheckResult() {
        DirectoryCheckResult r1 = new DirectoryCheckResult(new File("a"));
        DirectoryCheckResult r2 = new DirectoryCheckResult(new File("b"));
        r1.subDir = r2;
        assertTrue(r1.getMessage().isEmpty());
        assertTrue(r2.getMessage().isEmpty());
        assertTrue(r1.getFailures() == 0);
        assertTrue(r2.getFailures() == 0);
        assertTrue(r1.getPasses() == 6);
        assertTrue(r2.getPasses() == 3);
        r1.correctPermissions = false;
        r2.isDir = false;
        assertTrue(r1.getMessage().split("\n").length == 2);
        assertTrue(r2.getMessage().split("\n").length == 1);
        assertTrue(r1.getFailures() == 2);
        assertTrue(r2.getFailures() == 1);
        assertTrue(r1.getPasses() == 4);
        assertTrue(r2.getPasses() == 2);
        r1.exists = false;
        r2.exists = false;
        assertTrue(r1.getMessage().split("\n").length == 4);
        assertTrue(r2.getMessage().split("\n").length == 2);
        assertTrue(r1.getFailures() == 4);
        assertTrue(r2.getFailures() == 2);
        assertTrue(r1.getPasses() == 2);
        assertTrue(r2.getPasses() == 1);
        r1.isDir = false;
        r2.correctPermissions = false;
        assertTrue(r1.getMessage().split("\n").length == 6);
        assertTrue(r2.getMessage().split("\n").length == 3);
        assertTrue(r1.getFailures() == 6);
        assertTrue(r2.getFailures() == 3);
        assertTrue(r1.getPasses() == 0);
        assertTrue(r2.getPasses() == 0);
    }

    @Test
    @WindowsIssue
    public void testDirectoryValidator() throws IOException {
        File f1 = createTempDir();
        File f2 = createTempDir();
        DirectoryValidator dv = new DirectoryValidator(Arrays.asList(f1, f2));

        assertTrue(f1.setWritable(false));
        assertTrue(f2.setWritable(false));

        DirectoryCheckResults results1 = dv.ensureDirs();
        assertTrue(results1.results.size() == 2);
        assertTrue(results1.getFailures() == 2);
        assertTrue(results1.getPasses() == 4);
        String s1 = results1.getMessage();
        assertTrue(s1.endsWith("\n"));
        assertTrue(s1.split("\n").length == 2);

        assertTrue(f1.setWritable(true));

        DirectoryCheckResults results2 = dv.ensureDirs();
        assertTrue(results2.results.size() == 2);
        assertTrue(results2.getFailures() == 1);
        assertTrue(results2.getPasses() == 5);
        String s2 = results2.getMessage();
        assertTrue(s2.endsWith("\n"));
        assertTrue(s2.split("\n").length == 1);

        assertTrue(f2.setWritable(true));

        DirectoryCheckResults results3 = dv.ensureDirs();
        assertTrue(results3.results.size() == 2);
        assertTrue(results3.getFailures() == 0);
        assertTrue(results3.getPasses() == 6);
        String s3 = results3.getMessage();
        assertTrue(s3.isEmpty());

        assertTrue(f2.delete()); //will be created in dv.ensureDirs();

        DirectoryCheckResults results4 = dv.ensureDirs();
        assertTrue(results4.results.size() == 2);
        assertTrue(results4.getFailures() == 0);
        assertTrue(results4.getPasses() == 6);
        String s4 = results4.getMessage();
        assertTrue(s4.isEmpty());

        File f3 = File.createTempFile("test", "testMainDirs", f2);
        File f4 = File.createTempFile("test", "testMainDirs", f2);
        assertTrue(f4.delete());
        assertTrue(f3.delete()); 
        assertTrue(f4.mkdir());
        assertTrue(f2.setWritable(false)); //now f2 will not be recreated
        
        dv= new DirectoryValidator(Arrays.asList(f3, f4));

        DirectoryCheckResults results5 = dv.ensureDirs();
        assertTrue(results5.results.size() == 2);
        assertTrue(results5.getFailures() == 3);
        assertTrue(results5.getPasses() == 3);
        String s5 = results5.getMessage();
        assertTrue(s5.endsWith("\n"));
        assertTrue(s5.split("\n").length == 3);
        assertTrue(f2.setWritable(true));
        assertTrue(f4.delete());

    }

    private File createTempDir() throws IOException {
        File f1 = File.createTempFile("test", "testMainDirs");
        assertTrue(f1.delete());
        assertTrue(f1.mkdir());
        f1.deleteOnExit();
        return f1;
    }
}
