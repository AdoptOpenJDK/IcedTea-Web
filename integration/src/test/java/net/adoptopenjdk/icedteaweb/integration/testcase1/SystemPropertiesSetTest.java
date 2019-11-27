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

package net.adoptopenjdk.icedteaweb.integration.testcase1;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import net.adoptopenjdk.icedteaweb.integration.IntegrationTest;
import net.adoptopenjdk.icedteaweb.integration.TemporaryItwHome;
import net.adoptopenjdk.icedteaweb.integration.testcase1.applications.SimpleJavaApplication;
import org.junit.Rule;
import org.junit.Test;

import java.time.ZonedDateTime;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static net.adoptopenjdk.icedteaweb.integration.ItwLauncher.launchItwHeadless;
import static net.adoptopenjdk.icedteaweb.integration.testcase1.applications.SimpleJavaApplication.SYSTEM_PROPERTIES_FILE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

/**
 * TODO: Scenario Description
 */
public class SystemPropertiesSetTest implements IntegrationTest {
    private static final String JAR_NAME = "App-SimpleJavaApplication.jar";

    @Rule
    public TemporaryItwHome tmpItwHome = new TemporaryItwHome();
    @Rule
    public WireMockRule wireMock = new WireMockRule(wireMockConfig().dynamicPort());

    @Test(timeout = 100_000)
    public void testLaunchWithSystemProperty() throws Exception {
        // given
        final ZonedDateTime someTime = now();
        final String jnlpUrl = setupServer(wireMock)
                .servingJnlp("SimpleJavaApplicationWithProperties.jnlp").withMainClass(SimpleJavaApplication.class)
                .withHeadRequest().lastModifiedAt(someTime)
                .withGetRequest().lastModifiedAt(someTime)
                .servingResource(JAR_NAME).withoutVersion()
                .withHeadRequest().lastModifiedAt(someTime)
                .withGetRequest().lastModifiedAt(someTime)
                .getHttpUrl();

        tmpItwHome.createTrustSettings(jnlpUrl);

        // when
        // needs no-security as setting of properties is prohibited in sandboxed mode
        final int result = launchItwHeadless(jnlpUrl, NO_SECURITY, "-property", "key3=SystemPropertyAsCommandLineArgument");

        // then
        assertThat("Managed application return code", result, is(0));
        assertThat(getCachedFileAsProperties(tmpItwHome, SYSTEM_PROPERTIES_FILE).getProperty("key1"), containsString("SystemPropertyViaJnlpFile_Used"));
        assertThat(getCachedFileAsProperties(tmpItwHome, SYSTEM_PROPERTIES_FILE).getProperty("key2"), containsString("System Property Via Jnlp File2"));
        assertThat(getCachedFileAsProperties(tmpItwHome, SYSTEM_PROPERTIES_FILE).getProperty("key3"), containsString("SystemPropertyAsCommandLineArgument"));

        // The "deployment.javaws" flag is always set to "IcedTea-Web" to make it possible
        // for the started application to detect the execution context.
        assertThat(getCachedFileAsProperties(tmpItwHome, SYSTEM_PROPERTIES_FILE).getProperty("deployment.javaws"), containsString("IcedTea-Web"));
    }
}
