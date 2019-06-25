// Copyright (C) 2019 Karakun AG
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

package net.adoptopenjdk.icedteaweb.integration;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import net.adoptopenjdk.icedteaweb.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.head;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.find;
import static net.adoptopenjdk.icedteaweb.integration.MapBuilder.replace;

/**
 * Collection of util methods which cannot be static.
 */
public interface IntegrationTest {

    String PORT = "PORT";
    String MAIN_CLASS = "MAIN_CLASS";

    default String setupServer(WireMockRule wireMock, final String jnlpFilename, Class<?> mainClass, String... resources) throws IOException {
        return setupServer(wireMock, Arrays.asList(jnlpFilename), mainClass, resources);
    }

    default String setupServer(WireMockRule wireMock, final List<String> jnlpFilenames, Class<?> mainClass, String... resources) throws IOException {
        for (String jnlpFilename : jnlpFilenames) {
            wireMock.stubFor(head(urlEqualTo("/" + jnlpFilename)).willReturn(
                    ok()
            ));
            wireMock.stubFor(get(urlEqualTo("/" + jnlpFilename)).willReturn(
                    aResponse().withBody(fileContent("jnlps/" + jnlpFilename,
                            replace(PORT).with(wireMock.port())
                                    .and(MAIN_CLASS).with(mainClass))
                    )
            ));
        }

        for (String resource : resources) {
            final String resourcePath = "resources/" + resource;
            wireMock.stubFor(head(urlEqualTo("/" + resourcePath)).willReturn(
                    ok()
            ));
            wireMock.stubFor(get(urlEqualTo("/" + resourcePath)).willReturn(
                    aResponse().withBody(fileContent(resourcePath))
            ));
        }

        final String favIconPath = "favicon.ico";
        wireMock.stubFor(head(urlEqualTo("/" + favIconPath)).willReturn(
                ok()
        ));
        wireMock.stubFor(get(urlEqualTo("/" + favIconPath)).willReturn(
                aResponse().withBody(fileContent("/javaws.ico"))
        ));


        return "http://localhost:" + wireMock.port() + "/" + jnlpFilenames.get(0);
    }

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
