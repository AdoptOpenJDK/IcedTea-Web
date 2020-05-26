package net.adoptopenjdk.icedteaweb.proxy.util;

import org.junit.Assert;
import org.junit.Test;

public class CidrUtilsTest {

    @Test
    public void testIsInRange1() {
        //given
        final String cidrNotation = "169.254/16";
        final String lastBeforeRange = "169.253.255.255";
        final String firstInRange = "169.254.0.0";
        final String someInRange = "169.254.23.55";
        final String lastInRange = "169.254.255.255";
        final String firstAfterRange = "169.255.0.0";

        //than
        Assert.assertFalse(CidrUtils.isInRange(cidrNotation, lastBeforeRange));
        Assert.assertTrue(CidrUtils.isInRange(cidrNotation, firstInRange));
        Assert.assertTrue(CidrUtils.isInRange(cidrNotation, someInRange));
        Assert.assertTrue(CidrUtils.isInRange(cidrNotation, lastInRange));
        Assert.assertFalse(CidrUtils.isInRange(cidrNotation, firstAfterRange));
    }

    @Test
    public void testIsInRange2() {
        //given
        final String cidrNotation = "169.254.0/16";
        final String lastBeforeRange = "169.253.255.255";
        final String firstInRange = "169.254.0.0";
        final String someInRange = "169.254.23.55";
        final String lastInRange = "169.254.255.255";
        final String firstAfterRange = "169.255.0.0";

        //than
        Assert.assertFalse(CidrUtils.isInRange(cidrNotation, lastBeforeRange));
        Assert.assertTrue(CidrUtils.isInRange(cidrNotation, firstInRange));
        Assert.assertTrue(CidrUtils.isInRange(cidrNotation, someInRange));
        Assert.assertTrue(CidrUtils.isInRange(cidrNotation, lastInRange));
        Assert.assertFalse(CidrUtils.isInRange(cidrNotation, firstAfterRange));
    }

    @Test
    public void testIsInRange3() {
        //given
        final String cidrNotation = "169.254.0.0/16";
        final String lastBeforeRange = "169.253.255.255";
        final String firstInRange = "169.254.0.0";
        final String someInRange = "169.254.23.55";
        final String lastInRange = "169.254.255.255";
        final String firstAfterRange = "169.255.0.0";

        //than
        Assert.assertFalse(CidrUtils.isInRange(cidrNotation, lastBeforeRange));
        Assert.assertTrue(CidrUtils.isInRange(cidrNotation, firstInRange));
        Assert.assertTrue(CidrUtils.isInRange(cidrNotation, someInRange));
        Assert.assertTrue(CidrUtils.isInRange(cidrNotation, lastInRange));
        Assert.assertFalse(CidrUtils.isInRange(cidrNotation, firstAfterRange));
    }

    @Test
    public void testIsInRange4() {
        //given
        final String cidrNotation = "169.254.1.80/30";
        final String lastBeforeRange = "169.254.1.79";
        final String firstInRange = "169.254.1.80";
        final String someInRange = "169.254.1.81";
        final String lastInRange = "169.254.1.83";
        final String firstAfterRange = "169.254.1.84";

        //than
        Assert.assertFalse(CidrUtils.isInRange(cidrNotation, lastBeforeRange));
        Assert.assertTrue(CidrUtils.isInRange(cidrNotation, firstInRange));
        Assert.assertTrue(CidrUtils.isInRange(cidrNotation, someInRange));
        Assert.assertTrue(CidrUtils.isInRange(cidrNotation, lastInRange));
        Assert.assertFalse(CidrUtils.isInRange(cidrNotation, firstAfterRange));
    }

}
