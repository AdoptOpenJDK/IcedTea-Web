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

package net.adoptopenjdk.icedteaweb.integration.reproducers.progressclass;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import net.adoptopenjdk.icedteaweb.client.parts.downloadindicator.DefaultDownloadIndicator;
import net.adoptopenjdk.icedteaweb.integration.IntegrationTest;
import net.adoptopenjdk.icedteaweb.integration.TemporaryItwHome;
import net.adoptopenjdk.icedteaweb.integration.reproducers.progressclass.applications.ProgressClassManagedApplication;
import net.sourceforge.jnlp.runtime.Boot;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static net.adoptopenjdk.icedteaweb.integration.reproducers.progressclass.applications.ProgressClassManagedApplication.PROGRESS_CLASS_OUTPUT_FILE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

/**
 * TODO: Scenario Description
 */
public class ManagedApplicationWithProgressClassTest implements IntegrationTest {
    private static final String JAR_NAME = "App-ProgressClassManagedApplication.jar";

    @Rule
    public TemporaryItwHome tmpItwHome = new TemporaryItwHome();
    @Rule
    public WireMockRule wireMock = new WireMockRule(wireMockConfig().dynamicPort());

    @Test(timeout = 100_000)
    // This test does not run on CI as it is not headless. When you specify -headless in the args list for Boot below,
    // the functionality under test is not executed in ITW.
    // To run this test remove the @Ignore and run it manually in an environment that does not require headless run,
    // e.g. in the IDE.
    @Ignore
    public void testSuccessfullyLaunchSimpleJavaApplication() throws IOException {
        // given
        final String jnlpUrl = setupServer(wireMock, "ManagedApplicationWithProgressClass.jnlp", ProgressClassManagedApplication.class, JAR_NAME);
        tmpItwHome.createTrustSettings(jnlpUrl);

        final DefaultDownloadIndicator testDownloadIndicator = new DefaultDownloadIndicator() {
            @Override
            public int getInitialDelay() {
                // pimp the initial delay so that the progress indicator really shows up
                return 1;
            }
        };

        JNLPRuntime.setDefaultDownloadIndicator(testDownloadIndicator);

        // when
        final String[] args = {"-jnlp", jnlpUrl, "-nosecurity", "-Xnofork"};
        final int result = Boot.mainWithReturnCode(args);

        // then
        assertThat(result, is(SUCCESS));
        assertThat(hasCachedFile(tmpItwHome, JAR_NAME), is(true));
        assertThat(getCachedFileAsString(tmpItwHome, PROGRESS_CLASS_OUTPUT_FILE), containsString("MyDownloadServiceListener.progress called"));
    }
}
