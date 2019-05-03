/* ReadAuthorsTest.java
 Copyright (C) 2017 Red Hat, Inc.

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
package net.sourceforge.jnlp.util.docprovider;

import net.sourceforge.jnlp.util.docprovider.formatters.formatters.HtmlFormatter;
import net.sourceforge.jnlp.util.docprovider.formatters.formatters.ManFormatter;
import net.sourceforge.jnlp.util.docprovider.formatters.formatters.PlainTextFormatter;
import org.junit.Test;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;

public class ReadAuthorsTest {

    private static final String ANTISPAM_EMAIL = "t e s t @ t e s t . t e s t";

    public static final String PLAIN = "No formatting expected";
    public static final String EMAIL1 = "<test@test.test>";
    public static final String EMAIL1_RESULT = "<a href=\"mailto:t e s t @ t e s t . t e s t\" target=\"_top\"></a>";
    public static final String EMAIL2 = "Ester Tester <test@test.test>";
    public static final String EMAIL2_RESULT = "Ester Tester <a href=\"mailto:t e s t @ t e s t . t e s t\" target=\"_top\"></a>";
    public static final String URL = "OpenJDK http://openjdk.java.net";
    public static final String URL_RESULT = "OpenJDK <a href=\"http://openjdk.java.net\">http://openjdk.java.net</a>";

    @Test
    public void testPlainHtml() throws IOException {
        TextsProvider tp = new TextsProvider(UTF_8, new HtmlFormatter(), true, true) {
            @Override
            public String getId() {
                return "test1";
            }
        };
        String output = tp.readAuthorsImpl((PLAIN));
        assertEquals(PLAIN, output);
    }

    @Test
    public void testUrlHtml() throws IOException {
        TextsProvider tp = new TextsProvider(UTF_8, new HtmlFormatter(), true, true) {
            @Override
            public String getId() {
                return "test1";
            }
        };
        String output = tp.readAuthorsImpl((URL));
        assertEquals(URL_RESULT, output);
    }

    @Test
    public void testEmail1Html() throws IOException {
        TextsProvider tp = new TextsProvider(UTF_8, new HtmlFormatter(), true, true) {
            @Override
            public String getId() {
                return "test1";
            }
        };
        String output = tp.readAuthorsImpl((EMAIL1));
        assertEquals(EMAIL1_RESULT, output);
    }

    @Test
    public void testEmail2Html() throws IOException {
        TextsProvider tp = new TextsProvider(UTF_8, new HtmlFormatter(), true, true) {
            @Override
            public String getId() {
                return "test1";
            }
        };
        String output = tp.readAuthorsImpl((EMAIL2));
        assertEquals(EMAIL2_RESULT, output);
    }

    @Test
    public void testPlainPlain() throws IOException {
        TextsProvider tp = new TextsProvider(UTF_8, new PlainTextFormatter(), true, true) {
            @Override
            public String getId() {
                return "test1";
            }
        };
        String output = tp.readAuthorsImpl((PLAIN));
        assertEquals(PLAIN, output);
    }

    @Test
    public void testUrlPlain() throws IOException {
        TextsProvider tp = new TextsProvider(UTF_8, new PlainTextFormatter(), true, true) {
            @Override
            public String getId() {
                return "test1";
            }
        };
        String output = tp.readAuthorsImpl((URL));
        assertEquals(URL, output);
    }

    @Test
    public void testEmail1Plain() throws IOException {
        TextsProvider tp = new TextsProvider(UTF_8, new PlainTextFormatter(), true, true) {
            @Override
            public String getId() {
                return "test1";
            }
        };
        String output = tp.readAuthorsImpl((EMAIL1));
        assertEquals(EMAIL1, output
        );
    }

    @Test
    public void testEmail2Plain() throws IOException {
        TextsProvider tp = new TextsProvider(UTF_8, new PlainTextFormatter(), true, true) {
            @Override
            public String getId() {
                return "test1";
            }
        };
        String output = tp.readAuthorsImpl((EMAIL2));
        assertEquals(EMAIL2, output);
    }

    @Test
    public void testPlainMan() throws IOException {
        TextsProvider tp = new TextsProvider(UTF_8, new ManFormatter(), true, true) {
            @Override
            public String getId() {
                return "test1";
            }
        };
        String output = tp.readAuthorsImpl((PLAIN));
        assertEquals(PLAIN, output);
    }

    @Test
    public void testUrlMan() throws IOException {
        TextsProvider tp = new TextsProvider(UTF_8, new ManFormatter(), true, true) {
            @Override
            public String getId() {
                return "test1";
            }
        };
        String output = tp.readAuthorsImpl((URL));
        assertEquals(URL, output);
    }

    @Test
    public void testEmail1Man() throws IOException {
        TextsProvider tp = new TextsProvider(UTF_8, new ManFormatter(), true, true) {
            @Override
            public String getId() {
                return "test1";
            }
        };
        String output = tp.readAuthorsImpl((EMAIL1));
        assertEquals(EMAIL1, output
        );
    }

    @Test
    public void testEmail2Man() throws IOException {
        TextsProvider tp = new TextsProvider(UTF_8, new ManFormatter(), true, true) {
            @Override
            public String getId() {
                return "test1";
            }
        };
        String output = tp.readAuthorsImpl((EMAIL2));
        assertEquals(EMAIL2, output);
    }
}
