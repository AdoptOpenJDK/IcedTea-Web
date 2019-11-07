package jnlp.sample.jardiff;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
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

public class JarDiffTest {

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    @ParameterizedTest
    @CsvSource({
            "version-1.jar, version-2.jar, diff-1-to-2.jardiff",
            "version-2.jar, version-3.jar, diff-2-to-3.jardiff",
            "version-3.jar, version-4.jar, diff-3-to-4.jardiff",
            "version-4.jar, version-5.jar, diff-4-to-5.jardiff",
            "version-5.jar, version-6.jar, diff-5-to-6.jardiff",
            "version-6.jar, version-7.jar, diff-6-to-7.jardiff",
            "version-7.jar, version-8.jar, diff-7-to-8.jardiff"})
    public void testFromVersion1ToVersion2(final String jar1Name, final String jar2Name, final String diffName, @TempDir Path cacheFolder) throws Exception {
        //given
        final URL jar1Url = JarDiffTest.class.getResource(jar1Name);
        final URL jar2Url = JarDiffTest.class.getResource(jar2Name);
        final URL diffUrl = JarDiffTest.class.getResource(diffName);

        //when
        final Path createdJar = merge(jar1Url, diffUrl, cacheFolder);
        final Map<String, String> originalHashes = getMd5Hashes(jar2Url.getFile());
        final Map<String, String> createdHashes = getMd5Hashes(createdJar.toFile().getAbsolutePath());

        //than
        Assertions.assertEquals(originalHashes, createdHashes);
    }

    private Path merge(final URL jar1Url, final URL diffUrl, final Path outputDir) throws IOException {
        final Path createdJar = Paths.get(outputDir.toFile().getAbsolutePath(), "created.jar");
        try(final FileOutputStream os = new FileOutputStream(createdJar.toFile())) {
            new JarDiffPatcher().applyPatch(null, jar1Url.getFile(), diffUrl.getFile(), os);
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
