/* JeditorPaneBasedExceptionDialog.java
Copyright (C) 2012 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

IcedTea is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
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
exception statement from your version. */
package net.sourceforge.jnlp.splashscreen.parts;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import net.sourceforge.jnlp.runtime.Translator;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class JEditorPaneBasedExceptionDialogTest {

    private static RuntimeException eex = new RuntimeException("ex2");
    private static Exception ex = new Exception("ex1", eex);
    private static String ai = "Another info";
    private static InformationElement ec = new InformationElement();
    private static List<String> l = new ArrayList<String>(3);

    @BeforeClass
    public static void fillLists() {
        ec.setHomepage("item 1");
        ec.setTitle("item 2");
        ec.setvendor("item 3");
        ec.addDescription("item 4");
        l = JEditorPaneBasedExceptionDialog.infoElementToList(ec);

    }

    static void assertHtml(String s) {
        Assert.assertTrue("result of getText must be marked html", s.contains("html"));
        Assert.assertTrue("result of getText must be marked html", s.contains("body"));
        assertMarkup(s);
    }

    static void assertMarkup(String s) {
        Assert.assertTrue("result of getText must be marked in by  html markup", s.contains("<") && s.contains(">"));
        Assert.assertTrue("result of getText must be marked in by  html markup", s.contains("</") || s.contains("/>"));
    }

    private void assertAI(String s, boolean b) {
        if (b) {
            Assert.assertTrue("result of getText must contains annother info", s.contains(ai));
        } else {
            Assert.assertFalse("result of getText must NOT contains annother info", s.contains(ai));
        }
    }

    private void assertLL(String s, boolean b) {
        for (String i : l) {

            if (b) {
                Assert.assertTrue("result of getText must contains  info list", s.contains(i));
            } else {
                Assert.assertFalse("result of getText must NOT contains info list", s.contains(i));
            }
        }
    }

    private void assertFullException(String s, boolean b) {
        if (b) {
            Assert.assertTrue("result of getText must contains  complete exception", s.contains(ex.getMessage()));
            Assert.assertTrue("result of getText must contains  complete exception", s.contains(eex.getMessage()));
        } else {
            Assert.assertFalse("result of getText must contains not  complete exception", s.contains(ex.getMessage()));
            Assert.assertFalse("result of getText must contains  not complete exception", s.contains(eex.getMessage()));

        }
    }

    @Test
    public void getTextTest() {
        String s1 = JEditorPaneBasedExceptionDialog.getText(ex, l, ai, new Date());
        String s2 = JEditorPaneBasedExceptionDialog.getText(ex, l, null, new Date());
        String s3 = JEditorPaneBasedExceptionDialog.getText(ex, null, ai, new Date());
        String s4 = JEditorPaneBasedExceptionDialog.getText(null, l, ai, new Date());
        assertHtml(s1);
        assertHtml(s2);
        assertHtml(s3);
        assertHtml(s4);
        assertAI(s1, true);
        assertAI(s2, false);
        assertAI(s3, true);
        assertAI(s4, true);
        assertLL(s1, true);
        assertLL(s2, true);
        assertLL(s3, false);
        assertLL(s4, true);
        assertFullException(s1, true);
        assertFullException(s2, true);
        assertFullException(s3, true);
        assertFullException(s4, false);
        JEditorPaneBasedExceptionDialog d1 = new JEditorPaneBasedExceptionDialog(null, false, ex, ec, ai);
        JEditorPaneBasedExceptionDialog d2 = new JEditorPaneBasedExceptionDialog(null, false, ex, ec, null);
        JEditorPaneBasedExceptionDialog d3 = new JEditorPaneBasedExceptionDialog(null, false, ex, null, ai);
        JEditorPaneBasedExceptionDialog d4 = new JEditorPaneBasedExceptionDialog(null, false, null, ec, ai);
        Assert.assertTrue("message from dialog must be same as pattern", d1.getMessage().equals(s1));
        Assert.assertTrue("message from dialog must be same as pattern", d2.getMessage().equals(s2));
        Assert.assertTrue("message from dialog must be same as pattern", d3.getMessage().equals(s3));
        Assert.assertTrue("message from dialog must be same as pattern", d4.getMessage().equals(s4));

    }

    @Test
    public void getExceptionStackTraceAsString() {
        String t1 = JEditorPaneBasedExceptionDialog.getExceptionStackTraceAsString(ex);
        assertFullException(t1, true);
        String t2 = JEditorPaneBasedExceptionDialog.getExceptionStackTraceAsString(null);
        Assert.assertNotNull("For null empty result must not be null", t2);
        Assert.assertEquals("null input must result to empty string", "", t2);
    }

    @Test
    public void getExceptionStackTraceAsStrings() {
        String[] t1 = JEditorPaneBasedExceptionDialog.getExceptionStackTraceAsStrings(ex);
        assertFullException(Arrays.toString(t1), true);
        String[] t2 = JEditorPaneBasedExceptionDialog.getExceptionStackTraceAsStrings(null);
        Assert.assertNotNull("For null empty result must not be null", t2);
        Assert.assertArrayEquals("null input must result to empty array", new String[0], t2);
    }

    @Test
    public void formatListInfoList() {
        String t1 = JEditorPaneBasedExceptionDialog.formatListInfoList(l);
        assertMarkup(t1);
        assertLL(t1, true);
        String t2 = JEditorPaneBasedExceptionDialog.formatInfo(null);
        Assert.assertNotNull("For null empty result must not be null", t2);
        Assert.assertEquals("null input must result to empty string", "", t2);
    }

    @Test
    public void formatInfo() {
        String s = "SOME STRING";
        String t1 = JEditorPaneBasedExceptionDialog.formatInfo(s);
        assertMarkup(t1);
        Assert.assertTrue("Marked text must contains source", t1.contains(s));
        String t2 = JEditorPaneBasedExceptionDialog.formatInfo(null);
        Assert.assertNotNull("For null empty result must not be null", t2);
        Assert.assertEquals("null input must result to empty string", "", t2);

    }

    @Test
    public void infoElementToListTets() {

        List<String> tl = JEditorPaneBasedExceptionDialog.infoElementToList(ec);
        Assert.assertTrue("Transformed elemetn must contains all items ", tl.contains(l.get(0)));
        Assert.assertTrue("Transformed elemetn must contains all items ", tl.contains(l.get(1)));
        Assert.assertTrue("Transformed elemetn must contains all items ", tl.contains(l.get(2)));
        Assert.assertTrue("Transformed elemetn must contains all items ", tl.contains(l.get(3)));
    }
}
