package net.adoptopenjdk.icedteaweb.jnlp.element.information;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class DescriptionKindTest {
    @Test
    public void getValue() {
        Assert.assertEquals("one-line", DescriptionKind.ONE_LINE.getValue());
        Assert.assertEquals("short", DescriptionKind.SHORT.getValue());
        Assert.assertEquals("tooltip", DescriptionKind.TOOLTIP.getValue());
        Assert.assertEquals("default", DescriptionKind.DEFAULT.getValue());
    }

    @Test
    public void testFromString() {
        Assert.assertEquals(DescriptionKind.ONE_LINE, DescriptionKind.fromString("one-line"));
        Assert.assertEquals(DescriptionKind.SHORT, DescriptionKind.fromString("short"));
        Assert.assertEquals(DescriptionKind.TOOLTIP, DescriptionKind.fromString("tooltip"));
        Assert.assertEquals(DescriptionKind.DEFAULT, DescriptionKind.fromString("default"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromStringWithUnknownEnumValue() {
        DescriptionKind.fromString("unknown");
    }
}