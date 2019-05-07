package net.adoptopenjdk.icedteaweb.manifest;

import net.sourceforge.jnlp.util.logging.NoStdOutErrTest;
import org.junit.Assert;
import org.junit.Test;

public class ManifestAttributeReaderTest extends NoStdOutErrTest {
    @Test
    public void testSplitEmptyOrNullEntryPoints() throws Exception {
        Assert.assertArrayEquals(null, ManifestAttributeReader.splitEntryPoints(""));
        Assert.assertArrayEquals(null, ManifestAttributeReader.splitEntryPoints("  "));
        Assert.assertArrayEquals(null, ManifestAttributeReader.splitEntryPoints(null));
    }

    @Test
    public void testSplitSingleEntryPoint() throws Exception {
        Assert.assertArrayEquals(new String[]{"a.b.c"}, ManifestAttributeReader.splitEntryPoints("  a.b.c  "));
        Assert.assertArrayEquals(new String[]{"a.b.c"}, ManifestAttributeReader.splitEntryPoints("a.b.c"));
        Assert.assertArrayEquals(new String[]{"a.b.c"}, ManifestAttributeReader.splitEntryPoints("  a.b.c"));
        Assert.assertArrayEquals(new String[]{"a.b.c"}, ManifestAttributeReader.splitEntryPoints("a.b.c  "));
    }

    @Test
    public void testSplitMultipleEntryPoints() throws Exception {
        Assert.assertArrayEquals(new String[]{"a.b.c", "cde"}, ManifestAttributeReader.splitEntryPoints("  a.b.c     cde"));
        Assert.assertArrayEquals(new String[]{"a.b.c", "cde"}, ManifestAttributeReader.splitEntryPoints("  a.b.c cde    "));
        Assert.assertArrayEquals(new String[]{"a.b.c", "cde"}, ManifestAttributeReader.splitEntryPoints("a.b.c         cde    "));
    }
}