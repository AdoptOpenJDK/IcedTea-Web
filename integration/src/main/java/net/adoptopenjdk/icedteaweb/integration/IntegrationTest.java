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
import junit.framework.AssertionFailedError;
import net.adoptopenjdk.icedteaweb.commandline.CommandLineOptions;
import net.adoptopenjdk.icedteaweb.integration.serversetup.JnlpServer1;
import net.adoptopenjdk.icedteaweb.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static java.nio.file.Files.find;

/**
 * Collection of util methods which cannot be static.
 */
public interface IntegrationTest {

    int SUCCESS = 0;

    String PORT = "PORT";
    String MAIN_CLASS = "MAIN_CLASS";

    String NO_SECURITY = CommandLineOptions.NOSEC.getOption();

    default JnlpServer1 setupServer(WireMockRule wireMock) {
        return new WireMockConfigBuilder(wireMock, getClass());
    }

    default String getCachedFileAsString(TemporaryItwHome tmpItwHome, String fileName) throws IOException {
        final File helloFile = getCachedFile(tmpItwHome, fileName);
        final String content;
        try (final InputStream inputStream = new FileInputStream(helloFile)) {
            content = IOUtils.readContentAsUtf8String(inputStream);
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

        throw new AssertionFailedError("found " + numHints + " cache files with name " + fileName);
    }

    default ZonedDateTime now() {
        return ZonedDateTime.now(ZoneOffset.UTC);
    }
}
