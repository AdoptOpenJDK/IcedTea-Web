package net.adoptopenjdk.icedteaweb.resources.jardiff;

import org.junit.Assert;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class JarDiffMergerTest {

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    @Test
    public void testFromVersion1ToVersion2() throws Exception {
        //given
        final URL jar1Url = JarDiffMergerTest.class.getResource("version-1.jar");
        final URL jar2Url = JarDiffMergerTest.class.getResource("version-2.jar");
        final URL diffUrl = JarDiffMergerTest.class.getResource("diff-1-to-2.jardiff");

        //when
        final Path createdJar = merge(jar1Url, diffUrl);
        final Map<String, String> originalHashes = getMd5Hashes(jar2Url.getFile());
        final Map<String, String> createdHashes = getMd5Hashes(createdJar.toFile().getAbsolutePath());

        //than
        Assert.assertEquals(originalHashes, createdHashes);
    }

    @Test
    public void testFromVersion2ToVersion3() throws Exception {
        //given
        final URL jar1Url = JarDiffMergerTest.class.getResource("version-2.jar");
        final URL jar2Url = JarDiffMergerTest.class.getResource("version-3.jar");
        final URL diffUrl = JarDiffMergerTest.class.getResource("diff-2-to-3.jardiff");

        //when
        final Path createdJar = merge(jar1Url, diffUrl);
        final Map<String, String> originalHashes = getMd5Hashes(jar2Url.getFile());
        final Map<String, String> createdHashes = getMd5Hashes(createdJar.toFile().getAbsolutePath());

        //than
        Assert.assertEquals(originalHashes, createdHashes);
    }

    @Test
    public void testFromVersion3ToVersion4() throws Exception {
        //given
        final URL jar1Url = JarDiffMergerTest.class.getResource("version-3.jar");
        final URL jar2Url = JarDiffMergerTest.class.getResource("version-4.jar");
        final URL diffUrl = JarDiffMergerTest.class.getResource("diff-3-to-4.jardiff");

        //when
        final Path createdJar = merge(jar1Url, diffUrl);
        final Map<String, String> originalHashes = getMd5Hashes(jar2Url.getFile());
        final Map<String, String> createdHashes = getMd5Hashes(createdJar.toFile().getAbsolutePath());

        //than
        Assert.assertEquals(originalHashes, createdHashes);
    }

    @Test
    public void testFromVersion4ToVersion5() throws Exception {
        //given
        final URL jar1Url = JarDiffMergerTest.class.getResource("version-4.jar");
        final URL jar2Url = JarDiffMergerTest.class.getResource("version-5.jar");
        final URL diffUrl = JarDiffMergerTest.class.getResource("diff-4-to-5.jardiff");

        //when
        final Path createdJar = merge(jar1Url, diffUrl);
        final Map<String, String> originalHashes = getMd5Hashes(jar2Url.getFile());
        final Map<String, String> createdHashes = getMd5Hashes(createdJar.toFile().getAbsolutePath());

        //than
        Assert.assertEquals(originalHashes, createdHashes);
    }

    @Test
    public void testFromVersion5ToVersion6() throws Exception {
        //given
        final URL jar1Url = JarDiffMergerTest.class.getResource("version-5.jar");
        final URL jar2Url = JarDiffMergerTest.class.getResource("version-6.jar");
        final URL diffUrl = JarDiffMergerTest.class.getResource("diff-5-to-6.jardiff");

        //when
        final Path createdJar = merge(jar1Url, diffUrl);
        final Map<String, String> originalHashes = getMd5Hashes(jar2Url.getFile());
        final Map<String, String> createdHashes = getMd5Hashes(createdJar.toFile().getAbsolutePath());

        //than
        Assert.assertEquals(originalHashes, createdHashes);
    }

    private Path merge(final URL jar1Url, final URL diffUrl) throws IOException {
        final Path tempDirectory = Files.createTempDirectory("jardiff");
        final Path createdJar = Paths.get(tempDirectory.toFile().getAbsolutePath(), "created.jar");
        try (final FileOutputStream os = new FileOutputStream(createdJar.toFile())) {
            JarDiffMerger.merge(new JarFile(jar1Url.getFile()), new JarFile(diffUrl.getFile()), new JarOutputStream(os));
        }
        return createdJar;
    }

    private Map<String, String> getMd5Hashes(final String jarPath) throws Exception {
        final Map<String, String> entryToMd5 = new HashMap<>();
        try (final JarFile jarFile = new JarFile(jarPath)) {

            final Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                final JarEntry entry = entries.nextElement();
                if (!entry.isDirectory()) {
                    try (final DigestInputStream inputStream = new DigestInputStream(jarFile.getInputStream(entry), MessageDigest.getInstance("MD5"))) {
                        while (inputStream.read() != -1) {
                        }
                        entryToMd5.put(entry.getName(), bytesToHex(inputStream.getMessageDigest().digest()));
                    }
                }
            }
        }
        return Collections.unmodifiableMap(entryToMd5);
    }

    private String bytesToHex(final byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}
