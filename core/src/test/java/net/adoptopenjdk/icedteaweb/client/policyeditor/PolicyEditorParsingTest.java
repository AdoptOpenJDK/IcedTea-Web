/*Copyright (C) 2014 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify it under the
terms of the GNU General Public License as published by the Free Software
Foundation, version 2.

IcedTea is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
IcedTea; see the file COPYING. If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is making a
combined work based on this library. Thus, the terms and conditions of the GNU
General Public License cover the whole combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent modules, and
to copy and distribute the resulting executable under terms of your choice,
provided that you also meet, for each linked independent module, the terms and
conditions of the license of that module. An independent module is a module
which is not derived from or based on this library. If you modify this library,
you may extend this exception to your version of the library, but you are not
obligated to do so. If you do not wish to do so, delete this exception
statement from your version.
*/
package net.adoptopenjdk.icedteaweb.client.policyeditor;

import net.adoptopenjdk.icedteaweb.io.FileUtils;
import net.sourceforge.jnlp.util.docprovider.formatters.formatters.PlainTextFormatter;
import org.junit.Before;
import org.junit.Test;
import sun.security.provider.PolicyParser;

import java.io.File;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PolicyEditorParsingTest {

    private static final Collection<PolicyParser.PrincipalEntry> EMPTY_PRINCIPALS = Collections.emptyList();
    private static final String EXAMPLE_CODEBASE = "http://example.com";
    private static final PolicyIdentifier DEFAULT_IDENTIFIER = new PolicyIdentifier(null, EMPTY_PRINCIPALS, null);
    private static final PolicyIdentifier EXAMPLE_IDENTIFIER = new PolicyIdentifier(null, EMPTY_PRINCIPALS, EXAMPLE_CODEBASE);

    private File file;
    private PolicyFileModel policyFileModel = new PolicyFileModel();
    private Map<PolicyEditorPermissions, Boolean> permissions;

    private static final String LINEBREAK = PlainTextFormatter.getLineSeparator();

    private static final String READ_PERMISSION = "permission java.io.FilePermission \"${user.home}\", \"read\";";
    private static final String WRITE_PERMISSION = "permission java.io.FilePermission \"${user.home}\", \"write\";";
    private static final String COMMENT_HEADER = "/* TEST COMMENT */" + LINEBREAK;

    private static final String NORMAL_POLICY = "grant {" + LINEBREAK
        + "\t" + READ_PERMISSION + LINEBREAK
        + "};" + LINEBREAK;

    private static final String NORMAL_POLICY_CRLF = "grant {" + "\r\n"
            + "\t" + READ_PERMISSION + "\r\n"
            + "};" + "\r\n";

    private static final String NORMAL_POLICY_LF = "grant {" + "\n"
            + "\t" + READ_PERMISSION + "\n"
            + "};" + "\n";

    private static final String NORMAL_POLICY_MIXED_ENDINGS = "grant {" + "\n\n"
            + "\t" + READ_PERMISSION + "\r\n"
            + "};" + "\n";

    private static final String NORMAL_POLICY_WITH_HEADER = COMMENT_HEADER + NORMAL_POLICY;

    private static final String CODEBASE_POLICY = "grant codeBase \"http://example.com\" {" + LINEBREAK
        + "\t" + READ_PERMISSION + LINEBREAK
        + "};" + LINEBREAK;

    private static final String MULTIPLE_PERMISSION_POLICY = "grant {" + LINEBREAK
        + "\t" + READ_PERMISSION + LINEBREAK
        + "\t" + WRITE_PERMISSION + LINEBREAK
        + "};" + LINEBREAK;

    private static final String COMMENT_BLOCKED_PERMISSION = "grant {" + LINEBREAK
        + "\t/*" + READ_PERMISSION + "*/" + LINEBREAK
        + "};" + LINEBREAK;

    private static final String COMMENT_BLOCKED_POLICY = "/*" + NORMAL_POLICY + "*/" + LINEBREAK;

    private static final String COMMENTED_PERMISSION = "grant {" + LINEBREAK
        + "\t//" + READ_PERMISSION + LINEBREAK
        + "};" + LINEBREAK;

    private static final String COMMENT_AFTER_PERMISSION = "grant {" + LINEBREAK
        + "\t" + READ_PERMISSION + " // comment" + LINEBREAK
        + "};" + LINEBREAK;

    private static final String MISSING_WHITESPACE_POLICY = "grant { " + READ_PERMISSION + " };";

    private static final String MULTIPLE_PERMISSIONS_PER_LINE = "grant {" + LINEBREAK
            + "\t" + READ_PERMISSION + " " + WRITE_PERMISSION + LINEBREAK
            + "};" + LINEBREAK;

    @Before
    public void createTempFile() throws Exception {
        file = Files.createTempFile("PolicyEditor", ".policy").toFile();
        file.deleteOnExit();
    }

    private void setupTest(final String policyContents, final PolicyIdentifier identifier) throws Exception {
        FileUtils.saveFileUtf8(policyContents, file);
        policyFileModel = new PolicyFileModel(file.getCanonicalFile());
        policyFileModel.openAndParsePolicyFile();
        policyFileModel.addIdentifier(DEFAULT_IDENTIFIER);
        policyFileModel.addIdentifier(identifier);
        permissions = policyFileModel.getCopyOfPermissions().get(identifier);
    }

    @Test
    public void testNormalPolicy() throws Exception {
        setupTest(NORMAL_POLICY, DEFAULT_IDENTIFIER);
        assertTrue("Permissions should include READ_LOCAL_FILES", permissions.get(PolicyEditorPermissions.READ_LOCAL_FILES));
        for (final PolicyEditorPermissions perm : permissions.keySet()) {
            if (!perm.equals(PolicyEditorPermissions.READ_LOCAL_FILES)) {
                assertFalse("Permission " + perm + " should not be granted", permissions.get(perm));
            }
        }
    }

    @Test
    public void testNormalPolicyWithCRLFEndings() throws Exception {
        // This is the same test as testNormalPolicy on systems where the line separator is \r\n
        setupTest(NORMAL_POLICY_CRLF, DEFAULT_IDENTIFIER);
        assertTrue("Permissions should include READ_LOCAL_FILES", permissions.get(PolicyEditorPermissions.READ_LOCAL_FILES));
        for (final PolicyEditorPermissions perm : permissions.keySet()) {
            if (!perm.equals(PolicyEditorPermissions.READ_LOCAL_FILES)) {
                assertFalse("Permission " + perm + " should not be granted", permissions.get(perm));
            }
        }
    }

    @Test
    public void testNormalPolicyWithLFEndings() throws Exception {
        // This is the same test as testNormalPolicy on systems where the line separator is \n
        setupTest(NORMAL_POLICY_LF, DEFAULT_IDENTIFIER);
        assertTrue("Permissions should include READ_LOCAL_FILES", permissions.get(PolicyEditorPermissions.READ_LOCAL_FILES));
        for (final PolicyEditorPermissions perm : permissions.keySet()) {
            if (!perm.equals(PolicyEditorPermissions.READ_LOCAL_FILES)) {
                assertFalse("Permission " + perm + " should not be granted", permissions.get(perm));
            }
        }
    }

    @Test
    public void testNormalPolicyWithMixedEndings() throws Exception {
        // This is the same test as testNormalPolicy on systems where the line separator is \n
        setupTest(NORMAL_POLICY_MIXED_ENDINGS, DEFAULT_IDENTIFIER);
        assertTrue("Permissions should include READ_LOCAL_FILES", permissions.get(PolicyEditorPermissions.READ_LOCAL_FILES));
        for (final PolicyEditorPermissions perm : permissions.keySet()) {
            if (!perm.equals(PolicyEditorPermissions.READ_LOCAL_FILES)) {
                assertFalse("Permission " + perm + " should not be granted", permissions.get(perm));
            }
        }
    }

    @Test
    public void testCommentHeaders() throws Exception {
        setupTest(COMMENT_HEADER, DEFAULT_IDENTIFIER);
        for (final PolicyEditorPermissions perm : permissions.keySet()) {
            assertFalse("Permission " + perm + " should not be granted", permissions.get(perm));
        }
    }

    @Test
    public void testCommentBlockedPermission() throws Exception {
        setupTest(COMMENT_BLOCKED_PERMISSION, DEFAULT_IDENTIFIER);
        for (final PolicyEditorPermissions perm : permissions.keySet()) {
            assertFalse("Permission " + perm + " should not be granted", permissions.get(perm));
        }
    }

    @Test
    public void testCommentBlockedPolicy() throws Exception {
        setupTest(COMMENT_BLOCKED_POLICY, DEFAULT_IDENTIFIER);
        for (final PolicyEditorPermissions perm : permissions.keySet()) {
            assertFalse("Permission " + perm + " should not be granted", permissions.get(perm));
        }
    }

    @Test
    public void testCommentedLine() throws Exception {
        setupTest(COMMENTED_PERMISSION, DEFAULT_IDENTIFIER);
        for (final PolicyEditorPermissions perm : permissions.keySet()) {
            assertFalse("Permission " + perm + " should not be granted", permissions.get(perm));
        }
    }

    @Test
    public void testMultiplePermissions() throws Exception {
        setupTest(MULTIPLE_PERMISSION_POLICY, DEFAULT_IDENTIFIER);

        assertTrue("Permissions should include READ_LOCAL_FILES", permissions.get(PolicyEditorPermissions.READ_LOCAL_FILES));
        assertTrue("Permissions should include WRITE_LOCAL_FILES", permissions.get(PolicyEditorPermissions.WRITE_LOCAL_FILES));
        for (final PolicyEditorPermissions perm : permissions.keySet()) {
            if (!perm.equals(PolicyEditorPermissions.READ_LOCAL_FILES) && !perm.equals(PolicyEditorPermissions.WRITE_LOCAL_FILES)) {
                assertFalse("Permission " + perm + " should not be granted", permissions.get(perm));
            }
        }
    }

    @Test
    public void testMultiplePermissionsPerLine() throws Exception {
        setupTest(MULTIPLE_PERMISSIONS_PER_LINE, DEFAULT_IDENTIFIER);

        assertTrue("Permissions should include READ_LOCAL_FILES", permissions.get(PolicyEditorPermissions.READ_LOCAL_FILES));
        assertTrue("Permissions should include WRITE_LOCAL_FILES", permissions.get(PolicyEditorPermissions.WRITE_LOCAL_FILES));
        for (final PolicyEditorPermissions perm : permissions.keySet()) {
            if (!perm.equals(PolicyEditorPermissions.READ_LOCAL_FILES) && !perm.equals(PolicyEditorPermissions.WRITE_LOCAL_FILES)) {
                assertFalse("Permission " + perm + " should not be granted", permissions.get(perm));
            }
        }
    }

    @Test
    public void testMissingWhitespace() throws Exception {
        setupTest(MISSING_WHITESPACE_POLICY, DEFAULT_IDENTIFIER);

        assertTrue("Permissions should include READ_LOCAL_FILES", permissions.get(PolicyEditorPermissions.READ_LOCAL_FILES));
        for (final PolicyEditorPermissions perm : permissions.keySet()) {
            if (!perm.equals(PolicyEditorPermissions.READ_LOCAL_FILES)) {
                assertFalse("Permission " + perm + " should not be granted", permissions.get(perm));
            }
        }
    }

    @Test
    public void testPolicyWithCodebase() throws Exception {
        setupTest(CODEBASE_POLICY, EXAMPLE_IDENTIFIER);

        assertTrue("Permissions should include READ_LOCAL_FILES", permissions.get(PolicyEditorPermissions.READ_LOCAL_FILES));
        for (final PolicyEditorPermissions perm : permissions.keySet()) {
            if (!perm.equals(PolicyEditorPermissions.READ_LOCAL_FILES)) {
                assertFalse("Permission " + perm + " should not be granted", permissions.get(perm));
            }
        }
    }

    @Test
    public void testCodebaseTrailingSlashesDoNotMatch() throws Exception {
        // note the trailing '/' - looks like the same URL but is not. JDK PolicyTool considers these as
        // different codeBases, so so does PolicyEditor
        setupTest(CODEBASE_POLICY, EXAMPLE_IDENTIFIER);

        for (final PolicyEditorPermissions perm : permissions.keySet()) {
            if (!perm.equals(PolicyEditorPermissions.READ_LOCAL_FILES)) {
                assertFalse("Permission " + perm + " should not be granted", permissions.get(perm));
            }
        }
    }

    @Test
    public void testCommentAfterPermission() throws Exception {
        setupTest(COMMENT_AFTER_PERMISSION, DEFAULT_IDENTIFIER);

        assertTrue("Permissions should include READ_LOCAL_FILES", permissions.get(PolicyEditorPermissions.READ_LOCAL_FILES));
        for (final PolicyEditorPermissions perm : permissions.keySet()) {
            if (!perm.equals(PolicyEditorPermissions.READ_LOCAL_FILES)) {
                assertFalse("Permission " + perm + " should not be granted", permissions.get(perm));
            }
        }
    }

    @Test
    public void testNormalPolicyWithHeader() throws Exception {
        setupTest(NORMAL_POLICY_WITH_HEADER, DEFAULT_IDENTIFIER);
        assertTrue("Permissions should include READ_LOCAL_FILES", permissions.get(PolicyEditorPermissions.READ_LOCAL_FILES));
        for (final PolicyEditorPermissions perm : permissions.keySet()) {
            if (!perm.equals(PolicyEditorPermissions.READ_LOCAL_FILES)) {
                assertFalse("Permission " + perm + " should not be granted", permissions.get(perm));
            }
        }
    }

}
