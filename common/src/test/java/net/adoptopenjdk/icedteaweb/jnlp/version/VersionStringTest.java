// Copyright (C) 2019 Karakun AG
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//
package net.adoptopenjdk.icedteaweb.jnlp.version;

import org.junit.Assert;
import org.junit.Test;

public class VersionStringTest {

    @Test
    public void testSimpleVersionsParsingAndToString() {
        // legal version-ids (typical)
        Assert.assertEquals("1.4.0_04 1.4*&1.4.1_02+", versionString("1.4.0_04 1.4*&1.4.1_02+").toString());
    }

    @Test
    public void testSimpleRangeParsingAndToString() {
        // range with only one element (most simplest case)
        Assert.assertEquals("1.0", versionString("1.0").toString());
        Assert.assertEquals("1.0+", versionString("1.0+").toString());
        Assert.assertEquals("1.0*", versionString("1.0*").toString());
        Assert.assertEquals("1_0_0-build42*", versionString("1_0_0-build42*").toString());
        Assert.assertEquals("1.3.0-SNAPSHOT", versionString("1.3.0-SNAPSHOT").toString());
        Assert.assertEquals("15.2.2_21.05.2019_11:43:34", versionString("15.2.2_21.05.2019_11:43:34").toString());

        // range, exact version-ids
        Assert.assertEquals("1 2", versionString("1 2").toString());
        Assert.assertEquals("1.0 2.0 3.0", versionString("1.0 2.0 3.0").toString());

        // range with modifiers
        Assert.assertEquals("1.5+ 2.0", versionString("1.5+ 2.0").toString());
        Assert.assertEquals("1.4+ 1.6+", versionString("1.4+ 1.6+").toString());
        Assert.assertEquals("1.0* 2.0*", versionString("1.0* 2.0*").toString());
    }

    @Test(expected = NullPointerException.class)
    public void testNullVersionString() {
        versionString(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyVersionString() {
        versionString("");
    }

    @Test
    public void testContains() {
        Assert.assertTrue(versionString("1.0+").contains("1.0"));
        Assert.assertTrue(versionString("1.0+").contains("1.1"));
        Assert.assertTrue(versionString("1.0+").contains("1.1.5"));
        Assert.assertTrue(versionString("1.0+").contains("6.0-beta"));
        Assert.assertTrue(versionString("1.0+").contains("2.0.0"));
        Assert.assertTrue(versionString("1.0*").contains("1.0.5"));
        Assert.assertTrue(versionString("1.2+").contains("1.2"));
        Assert.assertTrue(versionString("1.2+").contains("1.3"));
        Assert.assertTrue(versionString("1.2+").contains("2.0"));
        Assert.assertTrue(versionString("1.2+").contains("1.3.1"));
        Assert.assertTrue(versionString("1.2+").contains("1.2.1"));
        Assert.assertTrue(versionString("1.2+").contains("1.3.1-beta"));

        Assert.assertTrue(versionString("1.0 2.0").contains("1.0"));
        Assert.assertTrue(versionString("1.0 2.0").contains("2.0"));
        Assert.assertTrue(versionString("1.0+ 2.0").contains("1.5"));
        Assert.assertTrue(versionString("1.0+ 2.0").contains("1.5"));
        Assert.assertTrue(versionString("1.0* 2.0").contains("1.0.5"));
        Assert.assertTrue(versionString("1.1* 1.3*").contains("1.1"));
        Assert.assertTrue(versionString("1.1* 1.3*").contains("1.1.8"));
        Assert.assertTrue(versionString("1.1* 1.3*").contains("1.3"));
        Assert.assertTrue(versionString("1.1* 1.3*").contains("1.3.1"));
        Assert.assertTrue(versionString("1.1* 1.3*").contains("1.3.1-beta"));

        Assert.assertFalse(versionString("1.2+").contains("1.1"));
        Assert.assertFalse(versionString("1.2+").contains("1.1.8"));
        Assert.assertFalse(versionString("1.0 1.2").contains("1.3"));
        Assert.assertFalse(versionString("1.0 2.0").contains("1.5"));
        Assert.assertFalse(versionString("1.1* 1.3*").contains("1.2"));
        Assert.assertFalse(versionString("1.1* 1.3*").contains("2.0"));
        Assert.assertFalse(versionString("1.1* 1.3*").contains("1.2.1"));
    }

    @Test
    public void testStaticContains() {
        Assert.assertTrue((versionString("1.0+")).contains("1.0"));
        Assert.assertTrue((versionString("1.0+")).contains("1.1"));
        Assert.assertTrue((versionString("1.0+")).contains("1.1.5"));
        Assert.assertTrue((versionString("1.0+")).contains("6.0-beta"));
        Assert.assertTrue((versionString("1.0+")).contains("2.0.0"));

        Assert.assertTrue((versionString("1.0 2.0")).contains("1.0"));
        Assert.assertTrue((versionString("1.1* 1.3*")).contains("1.1"));

        Assert.assertFalse((versionString("1.2+")).contains("1.1"));
        Assert.assertFalse((versionString("1.0 1.2")).contains("1.3"));
        Assert.assertFalse((versionString("1.0 2.0")).contains("1.5"));
        Assert.assertFalse((versionString("1.1* 1.3*")).contains("1.2"));
        Assert.assertFalse((versionString("1.1* 1.3*")).contains("2.0"));
    }

    @Test
    public void testContainsSingleVersionRange() {
        Assert.assertTrue(versionString("2.0").containsSingleVersionRange());
        Assert.assertTrue(versionString("1.1+").containsSingleVersionRange());
        Assert.assertTrue(versionString("1.1*").containsSingleVersionRange());
        Assert.assertTrue(versionString("1.4.0_04&1.4.1_02").containsSingleVersionRange());

        Assert.assertFalse(versionString("1.0 2.0").containsSingleVersionRange());
        Assert.assertFalse(versionString("1.0+ 2.0*").containsSingleVersionRange());
    }

    @Test
    public void testToString() {
        Assert.assertEquals("2.0", versionString("2.0").toString());
        Assert.assertEquals("1.0 2.0", versionString("1.0 2.0").toString());
        Assert.assertEquals("1.0+ 2.0*", versionString("1.0+ 2.0*").toString());
    }

    private VersionString versionString(String s) {
        return VersionString.fromString(s);
    }
}
