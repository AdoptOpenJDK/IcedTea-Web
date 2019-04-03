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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import net.sourceforge.jnlp.util.logging.NoStdOutErrTest;
import org.junit.Test;

public class DirectoryValidatorTest extends NoStdOutErrTest{

    @Test
    public void testMainDirTestNotExists() {
        DirectoryValidator.DirectoryCheckResult result = DirectoryValidator.testDir(new File("/definitely/not/existing/file/efgrhisaes"), false, false);
        String s = result.getMessage();
        assertTrue(s.endsWith("\n"));
        assertTrue(s.split("\n").length == 3);
    }

    @Test
    public void testMainDirTestExistsAsFile() throws IOException {
        File f = File.createTempFile("test", "testMainDirs");
        f.deleteOnExit();
        DirectoryValidator.DirectoryCheckResult result = DirectoryValidator.testDir(f, false, false);
        String s = result.getMessage();
        assertTrue(s.endsWith("\n"));
        assertTrue(s.split("\n").length == 2);
    }

    @Test
    public void testMainDirTestExistsAsDir() throws IOException {
        File f = File.createTempFile("test", "testMainDirs");
        assertTrue(f.delete());
        assertTrue(f.mkdir());
        f.deleteOnExit();
        DirectoryValidator.DirectoryCheckResult result = DirectoryValidator.testDir(f, false, false);
        String s = result.getMessage();
        assertTrue(s.isEmpty());
    }

    @Test
    public void testMainDirTestExistsAsDirButNotWritable() throws IOException {
        File f = File.createTempFile("test", "testMainDirs");
        assertTrue(f.delete());
        assertTrue(f.mkdir());
        assertTrue(f.setWritable(false));
        f.deleteOnExit();
        DirectoryValidator.DirectoryCheckResult result = DirectoryValidator.testDir(f, false, false);
        String s = result.getMessage();
        assertTrue(s.endsWith("\n"));
        assertTrue(s.split("\n").length == 1);
        assertTrue(f.setWritable(true));
    }

    @Test
    public void testMainDirTestExistsAsDirButNotReadable() throws IOException {
        File f = File.createTempFile("test", "testMainDirs");
        assertTrue(f.delete());
        assertTrue(f.mkdir());
        f.deleteOnExit();
        assertTrue(f.setReadable(false));
        DirectoryValidator.DirectoryCheckResult result = DirectoryValidator.testDir(f, false, false);
        String s = result.getMessage();
        assertTrue(s.endsWith("\n"));
        assertTrue(s.split("\n").length == 1);
        assertTrue(f.setReadable(true));
    }

    @Test
    public void testMainDirTestNotExistsWithSubdir() {
        DirectoryValidator.DirectoryCheckResult result = DirectoryValidator.testDir(new File("/definitely/not/existing/file/efgrhisaes"), false, true);
        String s = result.getMessage();
        assertTrue(s.endsWith("\n"));
        assertTrue(s.split("\n").length == 3);
    }

    @Test
    public void testMainDirTestExistsAsFileWithSubdir() throws IOException {
        File f = File.createTempFile("test", "testMainDirs");
        f.deleteOnExit();
        DirectoryValidator.DirectoryCheckResult result = DirectoryValidator.testDir(f, false, true);
        String s = result.getMessage();
        assertTrue(s.endsWith("\n"));
        assertTrue(s.split("\n").length == 2);
    }

    @Test
    public void testMainDirTestExistsAsDirWithSubdir() throws IOException {
        File f = File.createTempFile("test", "testMainDirs");
        assertTrue(f.delete());
        assertTrue(f.mkdir());
        f.deleteOnExit();
        DirectoryValidator.DirectoryCheckResult result = DirectoryValidator.testDir(f, false, true);
        String s = result.getMessage();
        assertTrue(s.isEmpty());
    }

    @Test
    public void testMainDirTestExistsAsDirButNotWritableWithSubdir() throws IOException {
        File f = File.createTempFile("test", "testMainDirs");
        assertTrue(f.delete());
        assertTrue(f.mkdir());
        assertTrue(f.setWritable(false));
        f.deleteOnExit();
        DirectoryValidator.DirectoryCheckResult result = DirectoryValidator.testDir(f, false, true);
        String s = result.getMessage();
        assertTrue(s.endsWith("\n"));
        assertTrue(s.split("\n").length == 1);
        assertTrue(f.setWritable(true));
    }

    @Test
    public void testMainDirTestExistsAsDirButNotReadableWithSubdir() throws IOException {
        File f = File.createTempFile("test", "testMainDirs");
        assertTrue(f.delete());
        assertTrue(f.mkdir());
        f.deleteOnExit();
        f.setReadable(false);
        DirectoryValidator.DirectoryCheckResult result = DirectoryValidator.testDir(f, false, true);
        String s = result.getMessage();
        assertTrue(s.endsWith("\n"));
        assertTrue(s.split("\n").length == 1);
    }

    @Test
    public void testDirectoryCheckResult() {
        DirectoryValidator.DirectoryCheckResult r1 = new DirectoryValidator.DirectoryCheckResult(new File("a"));
        DirectoryValidator.DirectoryCheckResult r2 = new DirectoryValidator.DirectoryCheckResult(new File("b"));
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
    public void testDirectoryValidator() throws IOException {
        File f1 = File.createTempFile("test", "testMainDirs");
        File f2 = File.createTempFile("test", "testMainDirs");
        DirectoryValidator dv = new DirectoryValidator(Arrays.asList(f1, f2));

        assertTrue(f1.delete());
        assertTrue(f1.mkdir());
        assertTrue(f1.setWritable(false));
        f1.deleteOnExit();

        assertTrue(f2.delete());
        assertTrue(f2.mkdir());
        assertTrue(f2.setWritable(false));
        f2.deleteOnExit();


        DirectoryValidator.DirectoryCheckResults results1 = dv.ensureDirs();
        assertTrue(results1.results.size() == 2);
        assertTrue(results1.getFailures() == 2);
        assertTrue(results1.getPasses() == 4);
        String s1 = results1.getMessage();
        assertTrue(s1.endsWith("\n"));
        assertTrue(s1.split("\n").length == 2);

        assertTrue(f1.setWritable(true));

        DirectoryValidator.DirectoryCheckResults results2 = dv.ensureDirs();
        assertTrue(results2.results.size() == 2);
        assertTrue(results2.getFailures() == 1);
        assertTrue(results2.getPasses() == 5);
        String s2 = results2.getMessage();
        assertTrue(s2.endsWith("\n"));
        assertTrue(s2.split("\n").length == 1);

        assertTrue(f2.setWritable(true));

        DirectoryValidator.DirectoryCheckResults results3 = dv.ensureDirs();
        assertTrue(results3.results.size() == 2);
        assertTrue(results3.getFailures() == 0);
        assertTrue(results3.getPasses() == 6);
        String s3 = results3.getMessage();
        assertTrue(s3.isEmpty());

        assertTrue(f2.delete()); //will be created in dv.ensureDirs();

        DirectoryValidator.DirectoryCheckResults results4 = dv.ensureDirs();
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

        DirectoryValidator.DirectoryCheckResults results5 = dv.ensureDirs();
        assertTrue(results5.results.size() == 2);
        assertTrue(results5.getFailures() == 3);
        assertTrue(results5.getPasses() == 3);
        String s5 = results5.getMessage();
        assertTrue(s5.endsWith("\n"));
        assertTrue(s5.split("\n").length == 3);
        assertTrue(f2.setWritable(true));
        assertTrue(f4.delete());

    }
}
