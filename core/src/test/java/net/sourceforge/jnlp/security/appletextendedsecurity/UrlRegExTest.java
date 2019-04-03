/*   Copyright (C) 2015 Red Hat, Inc.

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
package net.sourceforge.jnlp.security.appletextendedsecurity;

import org.junit.Assert;
import org.junit.Test;

public class UrlRegExTest {

    @Test
    public void testSimpleUnquote2() throws Exception {
        Assert.assertEquals("aabbccddee", UrlRegEx.simpleUnquote("aa\\Qbb\\Ecc\\Qdd\\Eee"));
        Assert.assertEquals("aabbccddee", UrlRegEx.simpleUnquote("aa\\Qbb\\Ecc\\Qdd\\Eee"));
        Assert.assertEquals("a\\Ea\\Ee\\Ee", UrlRegEx.simpleUnquote("a\\Ea\\Q\\E\\E\\Q\\Ee\\Ee"));
        Assert.assertEquals("http://url.cz/", UrlRegEx.simpleUnquote("\\Qhttp://url.cz/\\E"));
        Assert.assertEquals("http://url.cz/.*", UrlRegEx.simpleUnquote("\\Qhttp://url.cz/\\E.*"));
        Assert.assertEquals("http://ur\\El.cz/.*", UrlRegEx.simpleUnquote("\\Qhttp://ur\\E\\E\\Ql.cz/\\E.*"));
    }
    
    @Test
    public void testSimpleUnquote1() throws Exception {
        Assert.assertEquals("\\Q", UrlRegEx.simpleUnquote("\\Q\\Q\\E"));
        Assert.assertEquals("a\\Q", UrlRegEx.simpleUnquote("a\\Q\\Q\\E"));
        Assert.assertEquals("\\Qb", UrlRegEx.simpleUnquote("\\Q\\Q\\Eb"));
        Assert.assertEquals("a\\Qb", UrlRegEx.simpleUnquote("a\\Q\\Q\\Eb"));
        Assert.assertEquals("abc", UrlRegEx.simpleUnquote("a\\Qb\\Ec"));
        Assert.assertEquals("aabbcc", UrlRegEx.simpleUnquote("aa\\Qbb\\Ecc"));
        Assert.assertEquals("aabb", UrlRegEx.simpleUnquote("aa\\Qbb\\E"));
        Assert.assertEquals("bbcc", UrlRegEx.simpleUnquote("\\Qbb\\Ecc"));
        Assert.assertEquals("aacc", UrlRegEx.simpleUnquote("aa\\Q\\Ecc"));
        Assert.assertEquals("a", UrlRegEx.simpleUnquote("\\Qa\\E"));
        Assert.assertEquals("ab", UrlRegEx.simpleUnquote("\\Qab\\E"));
        Assert.assertEquals("", UrlRegEx.simpleUnquote("\\Q\\E"));
        Assert.assertEquals("", UrlRegEx.simpleUnquote(""));
        Assert.assertEquals("a", UrlRegEx.simpleUnquote("a"));
        Assert.assertEquals("ab", UrlRegEx.simpleUnquote("ab"));
        Assert.assertEquals("abc", UrlRegEx.simpleUnquote("abc"));
        Assert.assertEquals("Q", UrlRegEx.simpleUnquote("Q"));
        Assert.assertEquals("QE", UrlRegEx.simpleUnquote("QE"));
        Assert.assertEquals("Q\\E", UrlRegEx.simpleUnquote("Q\\E"));
        Assert.assertEquals("\\E", UrlRegEx.simpleUnquote("\\E"));
        Assert.assertEquals("\\E\\E\\E", UrlRegEx.simpleUnquote("\\E\\E\\E"));
    }

    @Test
    public void testReplaceAll1() throws Exception {
        Assert.assertEquals("abcd", UrlRegEx.replaceLast("abcd", "X", "Y"));
        Assert.assertEquals("abcD", UrlRegEx.replaceLast("abcd", "d", "D"));
        Assert.assertEquals("abcDef", UrlRegEx.replaceLast("abcdef", "d", "D"));
        Assert.assertEquals("abcdD", UrlRegEx.replaceLast("abcdd", "d", "D"));
        Assert.assertEquals("Abcd", UrlRegEx.replaceLast("abcd", "a", "A"));
        Assert.assertEquals("aAbcd", UrlRegEx.replaceLast("aabcd", "a", "A"));
    }

    @Test
    public void testReplaceAll2() throws Exception {
        Assert.assertEquals("abcd", UrlRegEx.replaceLast("abcd", "abcde", ""));
        Assert.assertEquals("abc", UrlRegEx.replaceLast("abcd", "d", ""));
        Assert.assertEquals("abcef", UrlRegEx.replaceLast("abcdef", "d", ""));
        Assert.assertEquals("bcdef", UrlRegEx.replaceLast("abcdef", "a", ""));
        Assert.assertEquals("abcdef", UrlRegEx.replaceLast("aabcdef", "a", ""));
        Assert.assertEquals("ab", UrlRegEx.replaceLast("abcd", "cd", ""));
        Assert.assertEquals("abf", UrlRegEx.replaceLast("abcdef", "cde", ""));
        Assert.assertEquals("cdef", UrlRegEx.replaceLast("abcdef", "ab", ""));
        Assert.assertEquals("acdef", UrlRegEx.replaceLast("aabcdef", "ab", ""));
        Assert.assertEquals("", UrlRegEx.replaceLast("abc", "abc", ""));
    }

    @Test
    public void testReplaceAll3() throws Exception {
        Assert.assertEquals("abcd", UrlRegEx.replaceLast("abcd", "xyz", "ABCDE"));
        Assert.assertEquals("abcDD", UrlRegEx.replaceLast("abcd", "d", "DD"));
        Assert.assertEquals("abcDDDef", UrlRegEx.replaceLast("abcdef", "d", "DDD"));
        Assert.assertEquals("AAbcdef", UrlRegEx.replaceLast("abcdef", "a", "AA"));
        Assert.assertEquals("aAAAbcdef", UrlRegEx.replaceLast("aabcdef", "a", "AAA"));
        Assert.assertEquals("abXCDY", UrlRegEx.replaceLast("abcd", "cd", "XCDY"));
        Assert.assertEquals("abXCDEYZf", UrlRegEx.replaceLast("abcdef", "cde", "XCDEYZ"));
        Assert.assertEquals("XABYcdef", UrlRegEx.replaceLast("abcdef", "ab", "XABY"));
        Assert.assertEquals("aABCDcdef", UrlRegEx.replaceLast("aabcdef", "ab", "ABCD"));
        Assert.assertEquals("ABC", UrlRegEx.replaceLast("abc", "abc", "ABC"));
        Assert.assertEquals("ABCE", UrlRegEx.replaceLast("abc", "abc", "ABCE"));
    }

    @Test
    public void testExact() throws Exception {
        String s1 = "string";
        String s2 = "reg.*ex";
        String s3 = "reg\\Eex";
        String s4 = "reg\\.\\*ex";
        UrlRegEx a1 = UrlRegEx.exact(s1);
        UrlRegEx a2 = UrlRegEx.exact(s2);
        UrlRegEx a3 = UrlRegEx.exact(s3);
        UrlRegEx a4 = UrlRegEx.exact(s4);
        Assert.assertEquals(s1, a1.getRegEx());
        Assert.assertEquals(s2, a2.getRegEx());
        Assert.assertEquals(s3, a3.getRegEx());
        Assert.assertEquals(s4, a4.getRegEx());
        Assert.assertTrue("regXXXex".matches(a2.getRegEx()));
        Assert.assertFalse("regXXXex".matches(a4.getRegEx()));

        Assert.assertEquals(s1, a1.getFilteredRegEx());
        Assert.assertEquals(s2, a2.getFilteredRegEx());
        Assert.assertEquals(s3, a3.getFilteredRegEx());
        Assert.assertEquals(s4, a4.getFilteredRegEx());
    }

    @Test
    public void testQuote1() throws Exception {
        String s1 = "string";
        String s2 = "reg.*ex";
        String s3 = "reg\\.\\*ex";
        UrlRegEx a1 = UrlRegEx.quote(s1);
        UrlRegEx a2 = UrlRegEx.quote(s2);
        UrlRegEx a3 = UrlRegEx.quote(s3);
        Assert.assertEquals("\\Q" + s1 + "\\E", a1.getRegEx());
        Assert.assertEquals("\\Q" + s2 + "\\E", a2.getRegEx());
        Assert.assertEquals("\\Q" + s3 + "\\E", a3.getRegEx());
        Assert.assertTrue("string".matches(a1.getRegEx()));
        Assert.assertFalse("regXXXex".matches(a2.getRegEx()));
        Assert.assertTrue("reg.*ex".matches(a2.getRegEx()));
        Assert.assertFalse("regXXXex".matches(a3.getRegEx()));
        Assert.assertFalse("reg.*ex".matches(a3.getRegEx()));
        Assert.assertTrue("reg\\.\\*ex".matches(a3.getRegEx()));
        
        Assert.assertEquals(s1, a1.getFilteredRegEx());
        Assert.assertEquals(s2, a2.getFilteredRegEx());
        Assert.assertEquals(s3, a3.getFilteredRegEx());

    }

    @Test
    public void testQuote2() throws Exception {
        String s1 = "stri\\Eng";
        String s2 = "reg.*ex";
        String s3 = "reg\\.\\*ex";
        UrlRegEx a1 = UrlRegEx.quote(s1);
        UrlRegEx a2 = UrlRegEx.quote(s2);
        UrlRegEx a3 = UrlRegEx.quote(s3);
        Assert.assertNotEquals("\\Qstri\\Eng\\E", a1.getRegEx());
        Assert.assertEquals("\\Q" + s2 + "\\E", a2.getRegEx());
        Assert.assertEquals("\\Q" + s3 + "\\E", a3.getRegEx());
        Assert.assertFalse("regXXXex".matches(a2.getRegEx()));
        Assert.assertTrue("reg.*ex".matches(a2.getRegEx()));
        Assert.assertTrue("stri\\Eng".matches(a1.getRegEx()));
        
        Assert.assertEquals(s1, a1.getFilteredRegEx());
        Assert.assertEquals(s2, a2.getFilteredRegEx());
        Assert.assertEquals(s3, a3.getFilteredRegEx());

    }
}
