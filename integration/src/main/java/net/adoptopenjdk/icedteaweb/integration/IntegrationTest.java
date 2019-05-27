package net.adoptopenjdk.icedteaweb.integration;

import net.adoptopenjdk.icedteaweb.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.find;
import static org.junit.Assert.assertTrue;

/**
 * Collection of util methods which cannot be static.
 */
public interface IntegrationTest {

    String PORT = "PORT";
    String MAIN_CLASS = "MAIN_CLASS";

    default byte[] fileContent(String file) throws IOException {
        try (final InputStream in = getClass().getResourceAsStream(file)) {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            IOUtils.copy(in, out);
            return out.toByteArray();
        }
    }

    default byte[] fileContent(String file, MapBuilder replacements) throws IOException {
        return fileContent(file, replacements.build());
    }

    default byte[] fileContent(String file, Map<String, String> replacements) throws IOException {
        String content;
        try (final InputStream in = getClass().getResourceAsStream(file)) {
            content = IOUtils.readContentAsString(in, UTF_8);
        }

        for (Map.Entry<String, String> replacePair : replacements.entrySet()) {
            content = content.replaceAll(Pattern.quote("${" + replacePair.getKey() + "}"), replacePair.getValue());
        }

        return content.getBytes(UTF_8);
    }

    default String getCachedFileAsString(TemporaryItwHome tmpItwHome, String fileName) throws IOException {
        final File helloFile = getCachedFile(tmpItwHome, fileName);
        final String content;
        try (final InputStream inputStream = new FileInputStream(helloFile)) {
            content = IOUtils.readContentAsString(inputStream, UTF_8);
        }
        return content;
    }

    default Properties getCachedFileAsProperties(TemporaryItwHome tmpItwHome, String fileName) throws IOException {
            final File helloFile = getCachedFile(tmpItwHome, fileName);
            final Properties result = new Properties();
            try (final InputStream inputStream = new FileInputStream(helloFile)) {
                result.load(inputStream);
            }
            return result;
    }

    default boolean hasCachedFile(TemporaryItwHome tmpItwHome, String fileName) throws IOException {
        return getCachedFile(tmpItwHome, fileName).isFile();
    }

    default File getCachedFile(TemporaryItwHome tmpItwHome, String fileName) throws IOException {
        final List<Path> stream = find(Paths.get(tmpItwHome.getCacheHome().getAbsolutePath()), 100,
                (path, attr) -> path.getFileName().toString().equals(fileName))
                .collect(Collectors.toList());

        final int numHints = stream.size();
        if (numHints == 1) {
            return stream.get(0).toFile();
        }

        throw new RuntimeException("found " + numHints + " cache files with name " + fileName);
    }


}
