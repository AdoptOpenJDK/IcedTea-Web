package net.adoptopenjdk.icedteaweb.manifest;

import org.junit.Assert;
import org.junit.Test;

public class ManifestAttributeReaderTest {

    @Test
    public void splitEmptyEntryPointsReturnsTests() throws Exception {
        Assert.assertArrayEquals(null, ManifestAttributeReader.splitEntryPoints("  "));
        Assert.assertArrayEquals(null, ManifestAttributeReader.splitEntryPoints(null));
    }

    @Test
    public void ensureSingleEntryPointIsParsed() throws Exception {
        Assert.assertArrayEquals(new String[]{"a.b.c"}, ManifestAttributeReader.splitEntryPoints("  a.b.c  "));
        Assert.assertArrayEquals(new String[]{"a.b.c"}, ManifestAttributeReader.splitEntryPoints("a.b.c"));
        Assert.assertArrayEquals(new String[]{"a.b.c"}, ManifestAttributeReader.splitEntryPoints("  a.b.c"));
        Assert.assertArrayEquals(new String[]{"a.b.c"}, ManifestAttributeReader.splitEntryPoints("a.b.c  "));
    }


    @Test
    public void ensureMultipleEntryPointsAreParsed() throws Exception {
        Assert.assertArrayEquals(new String[]{"a.b.c", "cde"}, ManifestAttributeReader.splitEntryPoints("  a.b.c     cde"));
        Assert.assertArrayEquals(new String[]{"a.b.c", "cde"}, ManifestAttributeReader.splitEntryPoints("  a.b.c cde    "));
        Assert.assertArrayEquals(new String[]{"a.b.c", "cde"}, ManifestAttributeReader.splitEntryPoints("a.b.c         cde    "));
    }
}