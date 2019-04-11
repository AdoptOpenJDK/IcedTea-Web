/*
 Copyright (C) 2013 Red Hat, Inc.

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
//package MixedSigningAndTrustedOnlyPackage;
package MixedSigningAndTrustedOnlyPackage;


import net.sourceforge.jnlp.OptionsDefinitions;
import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.closinglisteners.AutoOkClosingListener;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.ManifestAttributesChecker;
import net.sourceforge.jnlp.tools.DeploymentPropertiesModifier;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MixedSigningAndTrustedOnly extends BrowserTest {

    static List<String> HEADLESS = Arrays.asList(new String[]{OptionsDefinitions.OPTIONS.HEADLESS.option});
    static List<String> HTML = Arrays.asList(new String[]{OptionsDefinitions.OPTIONS.HTML.option});
    static List<String> verbose = Arrays.asList(new String[]{OptionsDefinitions.OPTIONS.VERBOSE.option});

    public static final String PREFIX = "MixedSigningAndTrustedOnly";
    public static final String C1 = "Class1";
    public static final String C2 = "Class2";

    static final String ID11 = PREFIX + C1;
    static final String ID12 = PREFIX + C2;
    static final String ID21 = PREFIX + C1;
    static final String ID22 = PREFIX + C2;

    static final String RUNNING = " running"; //note the space
    static final String RUNNING1 = ID11 + RUNNING;
    static final String RUNNING2 = ID22 + RUNNING;

    static final String RESTRICTED_CONFIRM_SUFFIX = " Property read"; //note the space
    static final String NORMAL_CONFIRM_SUFFIX = " confirmed"; //same
    static final String REMOTE_PREFIX = "RemoteCall - "; //note the space
    static final String LOCAL_PREFIX = "LocalCall - "; //note the space

    static final String RESTRICTED11 = ID11 + RESTRICTED_CONFIRM_SUFFIX;
    static final String NORMAL11 = ID11 + NORMAL_CONFIRM_SUFFIX;
    static final String RESTRICTED12 = ID12 + RESTRICTED_CONFIRM_SUFFIX;
    static final String NORMAL12 = ID12 + NORMAL_CONFIRM_SUFFIX;

    static final String RESTRICTED21 = ID21 + RESTRICTED_CONFIRM_SUFFIX;
    static final String NORMAL21 = ID21 + NORMAL_CONFIRM_SUFFIX;
    static final String RESTRICTED22 = ID22 + RESTRICTED_CONFIRM_SUFFIX;
    static final String NORMAL22 = ID22 + NORMAL_CONFIRM_SUFFIX;

    public static final String NORMAL_SUFFIX = "_Normal";
    public static final String RESTRICTED_SUFFIX = "_Restricted";
    public static final String COMMAND_C1_NORMAL = PREFIX + C1 + NORMAL_SUFFIX;
    public static final String COMMAND_C1_RESTRICT = PREFIX + C1 + RESTRICTED_SUFFIX;
    public static final String COMMAND_C2_NORMAL = PREFIX + C2 + NORMAL_SUFFIX;
    public static final String COMMAND_C2_RESTRICT = PREFIX + C2 + RESTRICTED_SUFFIX;
    public static final String COMMAND_CAN_DIE = "canDie";
    public static final String COMMAND_CANTTT_DIE = "cantDie";

    public static final String BOTH = "Both";
    public static final String SIGNED = "Signed";
    public static final String MANIFESTED = "Manifest";
    public static final String UNSIGNED = "Unsigned";
    public static final String FIRST = "First";
    public static final String SECOND = "Second";
    public static final String JAR = ".jar";

    static final Archives BS = new Archives(PREFIX + BOTH + SIGNED + JAR);
    static final Archives BSM = new Archives(PREFIX + BOTH + SIGNED + MANIFESTED + JAR);
    static final Archives BU = new Archives(PREFIX + BOTH + UNSIGNED + JAR);
    static final Archives BUM = new Archives(PREFIX + BOTH + UNSIGNED + MANIFESTED + JAR);
    static final Archives FS = new Archives(PREFIX + FIRST + SIGNED + JAR);
    static final Archives FSM = new Archives(PREFIX + FIRST + SIGNED + MANIFESTED + JAR);
    static final Archives FU = new Archives(PREFIX + FIRST + UNSIGNED + JAR);
    static final Archives FUM = new Archives(PREFIX + FIRST + UNSIGNED + MANIFESTED + JAR);
    static final Archives SS = new Archives(PREFIX + SECOND + SIGNED + JAR);
    static final Archives SSM = new Archives(PREFIX + SECOND + SIGNED + MANIFESTED + JAR);
    static final Archives SU = new Archives(PREFIX + SECOND + UNSIGNED + JAR);
    static final Archives SUM = new Archives(PREFIX + SECOND + UNSIGNED + MANIFESTED + JAR);

    static final String CLOSE_STRING = AutoOkClosingListener.MAGICAL_OK_CLOSING_STRING;
    static final String USER_HOME = System.getProperty("user.home");

    private static final String MAIN_CLASS_KEY = "@MAIN_CLASS@";
    private static final String APPLET_ARCHIVES_KEY = "@APPLET_ARCHIVES@";
    private static final String APPLET_PARAMS_KEY = "@APPLET_PARAMS@";
    private static final String HREF_KEY = "@HREF@";
    private static final String JNLP_ARCHIVES_KEY = "@JNLP_ARCHIVES@";
    private static final String SECURITY_KEY = "@SECURITY_TAG@";
    private static final String JNLP_ARGS_KEY = "@JNLP_ARGS@";

    private static final String JNLP_SECURITY_TAG = "<security><all-permissions/></security>";

    static enum FileType {

        HTML, JNLP_APP, JNLP_APPLET
    }

    static class Archives {

        private final String urlOrName;
        private final boolean isMain;

        public Archives(String urlOrName, boolean isMain) {
            this.urlOrName = urlOrName;
            this.isMain = isMain;
        }

        public Archives(String s) {
            this(s, false);
        }

        public Archives asMain() {
            return new Archives(urlOrName, true);
        }

    }

    private static CharSequence createAppletArchives(Archives[] archives) {
        StringBuilder sb = new StringBuilder();
        if (archives == null || archives.length == 0) {
            return sb;
        }
        for (Archives string : archives) {
            sb.append(string.urlOrName).append(",");
        }
        sb.delete(sb.length() - 1, sb.length());
        return sb;
    }

    private static CharSequence createAppletParams(String[] params) {
        StringBuilder sb = new StringBuilder();
        if (params == null || params.length == 0) {
            return sb;
        }
        sb.append("<param name=\"command\" value=\"");
        for (String string : params) {
            sb.append(string).append(" ");
        }
        sb.delete(sb.length() - 1, sb.length());
        sb.append("\"/>");
        return sb;
    }

    private static CharSequence createJnlpArchives(Archives[] archives) {
        StringBuilder sb = new StringBuilder();
        if (archives == null || archives.length == 0) {
            return sb;
        }
        for (Archives string : archives) {
            if (string.isMain) {
                sb.append("<jar href=\"").append(string.urlOrName).append("\" main=\"true\" />").append("\n");
            } else {
                sb.append("<jar href=\"").append(string.urlOrName).append("\" />").append("\n");
            }
        }
        sb.delete(sb.length() - 1, sb.length());
        return sb;
    }

    private static CharSequence createJnlpParams(String[] params) {
        StringBuilder sb = new StringBuilder();
        if (params == null || params.length == 0) {
            return sb;
        }
        for (String string : params) {
            sb.append("<argument>").append(string).append("</argument>").append("\n");
        }
        sb.delete(sb.length() - 1, sb.length());
        return sb;
    }

    static String prepareFile(FileType type, String mainClassSuffix, Archives[] archives, String[] params, boolean security) throws IOException {
        String baseName = PREFIX;
        switch (type) {
            case HTML:
                baseName = baseName + ".html";
                break;
            case JNLP_APP:
                baseName = baseName + "App.jnlp";
                break;
            case JNLP_APPLET:
                baseName = baseName + "Applet.jnlp";
                break;
            default:
                throw new RuntimeException("Unknown type");
        }
        File src = new File(server.getDir(), baseName + ".in");
        String srcJnlp = ServerAccess.getContentOfStream(new FileInputStream(src));
        String resultJnlp = srcJnlp
                .replace(HREF_KEY, "") //trying...
                .replace(MAIN_CLASS_KEY, PREFIX + mainClassSuffix);

        switch (type) {
            case HTML:
                resultJnlp = resultJnlp
                        .replace(APPLET_ARCHIVES_KEY, createAppletArchives(archives))
                        .replace(APPLET_PARAMS_KEY, createAppletParams(params));
                break;
            case JNLP_APP:
                resultJnlp = resultJnlp
                        .replace(JNLP_ARCHIVES_KEY, createJnlpArchives(archives))
                        .replace(JNLP_ARGS_KEY, createJnlpParams(params));
                break;
            case JNLP_APPLET:
                resultJnlp = resultJnlp
                        .replace(JNLP_ARCHIVES_KEY, createJnlpArchives(archives))
                        .replace(APPLET_PARAMS_KEY, createAppletParams(params));
                break;
            default:
                throw new RuntimeException("Unknown type");
        }
        resultJnlp = resultJnlp
                .replace(SECURITY_KEY, security ? JNLP_SECURITY_TAG : "");
        File dest = new File(server.getDir(), baseName);
        ServerAccess.saveFile(resultJnlp, dest);
        return baseName;
    }

    @Test
    public void createAppletArchivesWorks() {
        CharSequence c1 = createAppletArchives(null);
        Assert.assertEquals("", c1.toString());
        CharSequence c2 = createAppletArchives(new Archives[]{new Archives("archive")});
        Assert.assertEquals("archive", c2.toString());
        CharSequence c3 = createAppletArchives(new Archives[]{new Archives("archive1"), new Archives("archive2")});
        Assert.assertEquals("archive1,archive2", c3.toString());
        CharSequence c4 = createAppletArchives(new Archives[]{new Archives("archive1"), new Archives("archive2", true), new Archives("archive3")});
        Assert.assertEquals("archive1,archive2,archive3", c4.toString());
    }

    @Test
    public void createAppletParamsWorks() {
        CharSequence c1 = createAppletParams(null);
        Assert.assertEquals("", c1.toString());
        CharSequence c2 = createAppletParams(new String[]{"archive"});
        Assert.assertEquals("<param name=\"command\" value=\"archive\"/>", c2.toString());
        CharSequence c3 = createAppletParams(new String[]{"archive1", "archive2"});
        Assert.assertEquals("<param name=\"command\" value=\"archive1 archive2\"/>", c3.toString());
        CharSequence c4 = createAppletParams(new String[]{"archive1", "archive2", "archive3"});
        Assert.assertEquals("<param name=\"command\" value=\"archive1 archive2 archive3\"/>", c4.toString());
    }

    @Test
    public void createJnlpParamsWorks() {
        CharSequence c1 = createJnlpParams(null);
        Assert.assertEquals("", c1.toString());
        CharSequence c2 = createJnlpParams(new String[]{"archive"});
        Assert.assertEquals("<argument>archive</argument>", c2.toString());
        CharSequence c3 = createJnlpParams(new String[]{"archive1", "archive2"});
        Assert.assertEquals("<argument>archive1</argument>\n<argument>archive2</argument>", c3.toString());
        CharSequence c4 = createJnlpParams(new String[]{"archive1", "archive2", "archive3"});
        Assert.assertEquals("<argument>archive1</argument>\n<argument>archive2</argument>\n<argument>archive3</argument>", c4.toString());
    }

    @Test
    public void createJnlpArchivesWorks() {

        CharSequence c1 = createJnlpArchives(null);
        Assert.assertEquals("", c1.toString());
        CharSequence c2 = createJnlpArchives(new Archives[]{new Archives("archive", true)});
        Assert.assertEquals("<jar href=\"archive\" main=\"true\" />", c2.toString());
        CharSequence c22 = createJnlpArchives(new Archives[]{new Archives("archive")});
        Assert.assertEquals("<jar href=\"archive\" />", c22.toString());
        CharSequence c3 = createJnlpArchives(new Archives[]{new Archives("archive1"), new Archives("archive2")});
        Assert.assertEquals("<jar href=\"archive1\" />\n<jar href=\"archive2\" />", c3.toString());
        CharSequence c4 = createJnlpArchives(new Archives[]{new Archives("archive1"), new Archives("archive2", true), new Archives("archive3")});
        Assert.assertEquals("<jar href=\"archive1\" />\n<jar href=\"archive2\" main=\"true\" />\n<jar href=\"archive3\" />", c4.toString());
    }

    static void assertAllOkC1(ProcessResult pr) {
        Assert.assertTrue(pr.stdout.contains(RUNNING1));
        Assert.assertFalse(pr.stdout.contains(RUNNING2));
        Assert.assertTrue(pr.stdout.contains(LOCAL_PREFIX + NORMAL11));
        Assert.assertTrue(pr.stdout.contains(REMOTE_PREFIX + NORMAL12));
        Assert.assertTrue(pr.stdout.contains(LOCAL_PREFIX + RESTRICTED11));
        Assert.assertTrue(pr.stdout.contains(REMOTE_PREFIX + RESTRICTED12));
    }

    static void assertAllOkC2(ProcessResult pr) {
        Assert.assertTrue(pr.stdout.contains(RUNNING2));
        Assert.assertFalse(pr.stdout.contains(RUNNING1));
        Assert.assertTrue(pr.stdout.contains(LOCAL_PREFIX + NORMAL22));
        Assert.assertTrue(pr.stdout.contains(REMOTE_PREFIX + NORMAL21));
        Assert.assertTrue(pr.stdout.contains(LOCAL_PREFIX + RESTRICTED22));
        Assert.assertTrue(pr.stdout.contains(REMOTE_PREFIX + RESTRICTED21));
    }

    static void assertC1C1okTransNotOk(ProcessResult pr) {
        Assert.assertFalse(pr.stdout.contains(RUNNING2));
        Assert.assertTrue(pr.stdout.contains(RUNNING1));
        Assert.assertTrue(pr.stdout.contains(LOCAL_PREFIX + NORMAL11));
        Assert.assertTrue(pr.stdout.contains(LOCAL_PREFIX + RESTRICTED11));
        Assert.assertFalse(pr.stdout.contains(REMOTE_PREFIX));
    }
    
     static void assertC2C2okTransNotOk(ProcessResult pr) {
        Assert.assertTrue(pr.stdout.contains(RUNNING2));
        Assert.assertFalse(pr.stdout.contains(RUNNING1));
        Assert.assertTrue(pr.stdout.contains(LOCAL_PREFIX + NORMAL22));
        Assert.assertTrue(pr.stdout.contains(LOCAL_PREFIX + RESTRICTED22));
        Assert.assertFalse(pr.stdout.contains(REMOTE_PREFIX));
    }
    
    
    static void assertC1C1OnlyUnrestrictedokTransNotOk(ProcessResult pr) {
        Assert.assertFalse(pr.stdout.contains(RUNNING2));
        Assert.assertTrue(pr.stdout.contains(RUNNING1));
        Assert.assertTrue(pr.stdout.contains(LOCAL_PREFIX + NORMAL11));
        Assert.assertFalse(pr.stdout.contains(LOCAL_PREFIX + RESTRICTED11));
        Assert.assertFalse(pr.stdout.contains(REMOTE_PREFIX));
    }
    
    static void assertC2C2OnlyUnrestrictedokTransNotOk(ProcessResult pr) {
        Assert.assertTrue(pr.stdout.contains(RUNNING2));
        Assert.assertFalse(pr.stdout.contains(RUNNING1));
        Assert.assertTrue(pr.stdout.contains(LOCAL_PREFIX + NORMAL22));
        Assert.assertFalse(pr.stdout.contains(LOCAL_PREFIX + RESTRICTED22));
        Assert.assertFalse(pr.stdout.contains(REMOTE_PREFIX));
    }


    //mostly useless, all tests are killed
    static void assertProcessOk(ProcessResult pr) {
        Assert.assertEquals(0, pr.returnValue.intValue());
    }

    //mostly useless, all tests are killed
    static void assertProcessNotOk(ProcessResult pr) {
        Assert.assertNotEquals(0, pr.returnValue.intValue());
    }

    static void assertAllButRestrictedC1(ProcessResult pr) {
        Assert.assertTrue(pr.stdout.contains(RUNNING1));
        Assert.assertTrue(pr.stdout.contains(LOCAL_PREFIX + NORMAL11));
        Assert.assertTrue(pr.stdout.contains(REMOTE_PREFIX + NORMAL12));
        assertNotRestricted(pr);
    }

    static void assertAllButRestrictedC2(ProcessResult pr) {
        Assert.assertTrue(pr.stdout.contains(RUNNING2));
        Assert.assertTrue(pr.stdout.contains(REMOTE_PREFIX + NORMAL21));
        Assert.assertTrue(pr.stdout.contains(LOCAL_PREFIX + NORMAL22));
        assertNotRestricted(pr);
    }

    static void assertNotRestricted(ProcessResult pr) {
        Assert.assertFalse(pr.stdout.contains(RESTRICTED11));
        Assert.assertFalse(pr.stdout.contains(RESTRICTED12));
        Assert.assertFalse(pr.stdout.contains(RESTRICTED21));
        Assert.assertFalse(pr.stdout.contains(RESTRICTED22));
    }

    static void assertLaunchException(ProcessResult pr) {
        Assert.assertTrue(pr.stderr.contains("net.sourceforge.jnlp.LaunchException"));
    }
    static void assertInitError(ProcessResult pr) {
        Assert.assertTrue(pr.stderr.contains("Fatal: Initialization Error"));
    }

    static void assertSecurityException(ProcessResult pr) {
        Assert.assertTrue(pr.stderr.contains("java.lang.SecurityException"));
    }

    static void assertAccessControlException(ProcessResult pr) {
        Assert.assertTrue(pr.stderr.contains("java.security.AccessControlException"));
    }

    static void assertAccessDenied(ProcessResult pr) {
        Assert.assertTrue(pr.stderr.contains("access denied"));
    }

    static void assertNone(ProcessResult pr) {
        Assert.assertFalse(pr.stdout.contains(RUNNING1));
        Assert.assertFalse(pr.stdout.contains(RUNNING2));
        Assert.assertFalse(pr.stdout.contains(NORMAL11));
        Assert.assertFalse(pr.stdout.contains(NORMAL12));
        Assert.assertFalse(pr.stdout.contains(NORMAL21));
        Assert.assertFalse(pr.stdout.contains(NORMAL22));
        Assert.assertFalse(pr.stdout.contains(RESTRICTED11));
        Assert.assertFalse(pr.stdout.contains(RESTRICTED12));
        Assert.assertFalse(pr.stdout.contains(RESTRICTED21));
        Assert.assertFalse(pr.stdout.contains(RESTRICTED22));
    }

    static DeploymentPropertiesModifier setDeploymentPropertiesImpl() throws IOException {
        DeploymentPropertiesModifier q = new DeploymentPropertiesModifier();
        File f = q.src.getFile();
        if (!f.exists()) {
            f.getParentFile().mkdirs();
            f.createNewFile();
        }
        q.setProperties(DeploymentConfiguration.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK, ManifestAttributesChecker.MANIFEST_ATTRIBUTES_CHECK.TRUSTED.name());
        return q;
    }

}
