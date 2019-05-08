package net.adoptopenjdk.icedteaweb.manifest;

import net.sourceforge.jnlp.util.logging.NoStdOutErrTest;
import org.junit.Assert;
import org.junit.Test;

public class ManifestAttributesReaderTest extends NoStdOutErrTest {
    @Test
    public void testSplitEmptyOrNullEntryPoints() throws Exception {
        Assert.assertArrayEquals(null, ManifestAttributesReader.splitEntryPoints(""));
        Assert.assertArrayEquals(null, ManifestAttributesReader.splitEntryPoints("  "));
        Assert.assertArrayEquals(null, ManifestAttributesReader.splitEntryPoints(null));
    }

    @Test
    public void testSplitSingleEntryPoint() throws Exception {
        Assert.assertArrayEquals(new String[]{"a.b.c"}, ManifestAttributesReader.splitEntryPoints("  a.b.c  "));
        Assert.assertArrayEquals(new String[]{"a.b.c"}, ManifestAttributesReader.splitEntryPoints("a.b.c"));
        Assert.assertArrayEquals(new String[]{"a.b.c"}, ManifestAttributesReader.splitEntryPoints("  a.b.c"));
        Assert.assertArrayEquals(new String[]{"a.b.c"}, ManifestAttributesReader.splitEntryPoints("a.b.c  "));
    }

    @Test
    public void testSplitMultipleEntryPoints() throws Exception {
        Assert.assertArrayEquals(new String[]{"a.b.c", "cde"}, ManifestAttributesReader.splitEntryPoints("  a.b.c     cde"));
        Assert.assertArrayEquals(new String[]{"a.b.c", "cde"}, ManifestAttributesReader.splitEntryPoints("  a.b.c cde    "));
        Assert.assertArrayEquals(new String[]{"a.b.c", "cde"}, ManifestAttributesReader.splitEntryPoints("a.b.c         cde    "));
    }
}