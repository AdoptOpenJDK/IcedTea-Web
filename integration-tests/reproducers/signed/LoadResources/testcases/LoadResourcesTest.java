/* 
 Copyright (C) 2011 Red Hat, Inc.

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

import net.sourceforge.jnlp.OptionsDefinitions;
import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.annotations.Bug;
import net.sourceforge.jnlp.closinglisteners.AutoOkClosingListener;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class LoadResourcesTest {

    private static final ServerAccess server = new ServerAccess();

    static void checkLaunched(ProcessResult pr, boolean app1, boolean javaws) {
        String s11 = "LoadResources started";
        String s12 = "LoadResourcesPackaged started";
        if (app1) {
            assertContains(s12, pr);
            assertNotContains(s11, pr);
        } else {
            assertContains(s11, pr);
            assertNotContains(s12, pr);
        }
        if (!javaws) {
            String c01 = "[CONSTRUCTOR]context(some.file): Pass - ";
            //without fix for 2968, this fails
            assertContains(c01, pr);
            String c02 = "[CONSTRUCTOR]class(some.file): Pass - ";
            assertContains(c02, pr);
            String c03 = "[CONSTRUCTOR]system(some.file): Fail - ";
            //why so?, still everywhere...
            assertContains(c03, pr);
            String c04 = "[CONSTRUCTOR]context(LoadResources.class): Pass - ";
            //without fix for 2968, this fails
            assertContains(c04, pr);
            String c05 = "[CONSTRUCTOR]class(LoadResources.class): Pass - ";
            assertContains(c05, pr);
            String c06 = "[CONSTRUCTOR]system(LoadResources.class): Fail - ";
            assertContains(c06, pr);
            String c07 = "[CONSTRUCTOR]context(some/pkg/LoadResourcesPackaged.class): Pass - ";
            assertContains(c07, pr);
            String c08 = "[CONSTRUCTOR]class(some/pkg/LoadResourcesPackaged.class): Pass - ";
            assertContains(c08, pr);
            String c09 = "[CONSTRUCTOR]system(some/pkg/LoadResourcesPackaged.class): Fail - ";
            assertContains(c09, pr);
            String c10 = "[CONSTRUCTOR]this(some.file): Pass - ";
            assertContains(c10, pr);
            String c11 = "[CONSTRUCTOR]this(LoadResources.class): Pass - ";
            assertContains(c11, pr);
            String c12 = "[CONSTRUCTOR]this(some/pkg/LoadResourcesPackaged.class): Pass - ";
            assertContains(c12, pr);
            String c13 = "[INIT]context(some.file): Fail - ";
            // this is weird. Why fix for 2968 fixed only constructor?
            assertContains(c13, pr);
            String c14 = "[INIT]class(some.file): Pass - ";
            //safest as ususlly
            assertContains(c14, pr);
            String c15 = "[INIT]system(some.file): Fail - ";
            //failing as usually
            assertContains(c15, pr);
            String c16 = "[INIT]context(LoadResources.class): Fail - ";
            // this is weird. Why fix for 2968 fixed only constructor?
            assertContains(c16, pr);
            String c17 = "[INIT]class(LoadResources.class): Pass - ";
            assertContains(c17, pr);
            String c18 = "[INIT]system(LoadResources.class): Fail - ";
            assertContains(c18, pr);
            String c19 = "[INIT]context(some/pkg/LoadResourcesPackaged.class): Fail - ";
            assertContains(c19, pr);
            String c20 = "[INIT]class(some/pkg/LoadResourcesPackaged.class): Pass - ";
            assertContains(c20, pr);
            String c21 = "[INIT]system(some/pkg/LoadResourcesPackaged.class): Fail - ";
            assertContains(c21, pr);
            String c22 = "[INIT]this(some.file): Pass - ";
            assertContains(c22, pr);
            String c23 = "[INIT]this(LoadResources.class): Pass - ";
            assertContains(c23, pr);
            String c24 = "[INIT]this(some/pkg/LoadResourcesPackaged.class): Pass - ";
            assertContains(c24, pr);
            String c25 = "[START]context(some.file): Fail - ";
            //still the surprise
            assertContains(c15, pr);
            String c26 = "[START]class(some.file): Pass - ";
            assertContains(c16, pr);
            String c27 = "[START]system(some.file): Fail - ";
            assertContains(c27, pr);
            String c28 = "[START]context(LoadResources.class): Fail - ";
            assertContains(c28, pr);
            String c29 = "[START]class(LoadResources.class): Pass - ";
            assertContains(c29, pr);
            String c30 = "[START]system(LoadResources.class): Fail - ";
            assertContains(c30, pr);
            String c31 = "[START]context(some/pkg/LoadResourcesPackaged.class): Fail - ";
            assertContains(c31, pr);
            String c32 = "[START]class(some/pkg/LoadResourcesPackaged.class): Pass - ";
            assertContains(c32, pr);
            String c33 = "[START]system(some/pkg/LoadResourcesPackaged.class): Fail - ";
            assertContains(c33, pr);
            String c34 = "[START]this(some.file): Pass - ";
            assertContains(c34, pr);
            String c35 = "[START]this(LoadResources.class): Pass - ";
            assertContains(c35, pr);
            String c36 = "[START]this(some/pkg/LoadResourcesPackaged.class): Pass - ";
            assertContains(c36, pr);
        } else {
            String c1 = "[MAIN]context(some.file): Pass - ";
            //javaws was never affeted by 2968
            assertContains(c1, pr);
            String c2 = "[MAIN]class(some.file): Pass - ";
            assertContains(c2, pr);
            String c3 = "[MAIN]system(some.file): Fail - ";
            assertContains(c3, pr);
            String c4 = "[MAIN]context(LoadResources.class): Pass - ";
            //interesting difference compared to start/init of same call
            assertContains(c4, pr);
            String c5 = "[MAIN]class(LoadResources.class): Pass - ";
            assertContains(c5, pr);
            String c6 = "[MAIN]system(LoadResources.class): Fail - ";
            assertContains(c6, pr);
            String c7 = "[MAIN]context(some/pkg/LoadResourcesPackaged.class): Pass - ";
            //interesting difference compared to start/init of same call
            assertContains(c7, pr);
            String c8 = "[MAIN]class(some/pkg/LoadResourcesPackaged.class): Pass - ";
            assertContains(c8, pr);
            String c9 = "[MAIN]system(some/pkg/LoadResourcesPackaged.class): Fail - ";
            assertContains(c9, pr);

        }

    }

    private static void assertContains(String c01, ProcessResult pr) {
        Assert.assertTrue("loadResourcesTest stdout should contains " + c01 + " bud didn't", pr.stdout.contains(c01));
    }

    private static void assertNotContains(String s11, ProcessResult pr) {
        Assert.assertFalse("loadResourcesTest stdout should contains " + s11 + " bud didn", pr.stdout.contains(s11));
    }

    @Test
    public void loadResourcesTest1() throws Exception {
        //with args, dont forget -jnlp
        final List<String> args = Arrays.asList(new String[]{
            OptionsDefinitions.OPTIONS.ARG.option,
            "type1",
            "some.file",
            OptionsDefinitions.OPTIONS.JNLP.option});
        ProcessResult pr = server.executeJavawsHeadless(args, "/LoadResources1.jnlp");
        checkLaunched(pr, false, true);
    }

    @Test
    public void loadResourcesTest2() throws Exception {
        //with args, dont forget -jnlp
        final List<String> args = Arrays.asList(new String[]{
            OptionsDefinitions.OPTIONS.ARG.option,
            "type1",
            "some.file",
            OptionsDefinitions.OPTIONS.JNLP.option});
        ProcessResult pr = server.executeJavawsHeadless(args, "/LoadResources2.jnlp");
        checkLaunched(pr, true, true);
    }

    @Test
    @Bug(id = "2968")
    public void loadResourcesAppletTest1() throws Exception {
        //with args, dont forget -jnlp
        final List<String> args = Arrays.asList(new String[]{
            OptionsDefinitions.OPTIONS.JNLP.option});
        ProcessResult pr = server.executeJavawsHeadless(args, "/LoadResourcesApplet1.jnlp", new AutoOkClosingListener(), null, null);
        checkLaunched(pr, false, false);
    }

    @Test
    @Bug(id = "2968")
    public void loadResourcesTestApplet2() throws Exception {
        //with args, dont forget -jnlp
        final List<String> args = Arrays.asList(new String[]{
            OptionsDefinitions.OPTIONS.JNLP.option});
        ProcessResult pr = server.executeJavawsHeadless(args, "/LoadResourcesApplet2.jnlp", new AutoOkClosingListener(), null, null);
        checkLaunched(pr, true, false);
    }

}
