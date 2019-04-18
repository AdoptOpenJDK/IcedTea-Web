package net.adoptopenjdk.icedteaweb.jnlp.element.information;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class IconKindTest {

    @Test
    public void getValue() {
        Assert.assertEquals("default", IconKind.DEFAULT.getValue());
        Assert.assertEquals("selected", IconKind.SELECTED.getValue());
        Assert.assertEquals("disabled", IconKind.DISABLED.getValue());
        Assert.assertEquals("rollover", IconKind.ROLLOVER.getValue());
        Assert.assertEquals("splash", IconKind.SPLASH.getValue());
        Assert.assertEquals("default", IconKind.DEFAULT.getValue());
    }

    @Test
    public void testFromString() {
        Assert.assertEquals(IconKind.DEFAULT, IconKind.fromString("default"));
        Assert.assertEquals(IconKind.SELECTED, IconKind.fromString("selected"));
        Assert.assertEquals(IconKind.DISABLED, IconKind.fromString("disabled"));
        Assert.assertEquals(IconKind.ROLLOVER, IconKind.fromString("rollover"));
        Assert.assertEquals(IconKind.SPLASH, IconKind.fromString("splash"));
        Assert.assertEquals(IconKind.SHORTCUT, IconKind.fromString("shortcut"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromStringWithUnknownEnumValue() {
        IconKind.fromString("unknown");
    }
}