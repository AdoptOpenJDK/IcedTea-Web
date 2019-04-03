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

import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess.AutoClose;
import net.sourceforge.jnlp.annotations.TestInBrowsers;
import net.sourceforge.jnlp.browsertesting.BrowserTest;
import net.sourceforge.jnlp.browsertesting.Browsers;

import org.junit.Assert;
import org.junit.Test;

public class DocumentBaseEncodingTests extends BrowserTest {

    static final private String urlPattern = "http://localhost:\\d+";

    private String escapePattern(String plainText) {
        return "\\Q" + plainText + "\\E";
    }

    // Surround a pattern with two plain text matches and wildcards to match any occurence
    private String surroundPattern(String plainText1, String pattern, String plainText2) {
        return "(?s).*" + escapePattern(plainText1) + pattern + escapePattern(plainText2) + "\\W.*";
    }

    private void testEncoding(String urlParam, String encodedUrlParam) throws Exception {
        ProcessResult pr = server.executeBrowser("Document Base Encoding.html" + urlParam, AutoClose.CLOSE_ON_CORRECT_END);
        final String codeBasePattern = surroundPattern("CodeBase: ", urlPattern, "/");
        final String documentBasePattern = surroundPattern("DocumentBase: ", urlPattern, "/Document%20Base%20Encoding.html" + encodedUrlParam);

        Assert.assertTrue("DocumentBaseEncoding stdout should match '" + codeBasePattern + "' but did not.", 
                pr.stdout.matches(codeBasePattern));
        Assert.assertTrue("DocumentBaseEncoding stdout should match '" + documentBasePattern + "' but did not.", 
                pr.stdout.matches(documentBasePattern));
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.one })
    public void testSpacesInUrl() throws Exception {
        testEncoding("?spaces test", "?spaces%20test");
    }

    @Test
    @TestInBrowsers(testIn = { Browsers.one })
    public void testComplexParameterInUrl() throws Exception {

        String urlParam = "?testkey=http%3A%2F%2Ftest.com%3Ftest%3Dtest"; // test value is 'http://test.com?test=test' percent-encoded
        testEncoding(urlParam, urlParam /* Already encoded. */);
    }
}
