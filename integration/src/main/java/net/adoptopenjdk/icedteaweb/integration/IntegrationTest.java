package net.adoptopenjdk.icedteaweb.integration;

import net.adoptopenjdk.icedteaweb.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Collection of util methods which cannot be static.
 */
public interface IntegrationTest {

    String PORT = "PORT";

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
            content = new String(IOUtils.readContent(in), UTF_8);
        }

        for (Map.Entry<String, String> replacePair : replacements.entrySet()) {
            content = content.replaceAll(Pattern.quote("${" + replacePair.getKey() + "}"), replacePair.getValue());
        }

        return content.getBytes(UTF_8);
    }

}
