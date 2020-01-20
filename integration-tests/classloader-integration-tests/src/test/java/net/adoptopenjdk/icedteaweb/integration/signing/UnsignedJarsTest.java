package net.adoptopenjdk.icedteaweb.integration.signing;

import net.adoptopenjdk.icedteaweb.integration.DummyResourceTracker;
import net.adoptopenjdk.icedteaweb.integration.IntegrationTestResources;
import net.adoptopenjdk.icedteaweb.resources.ResourceTrackerFactory;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.JNLPFileFactory;
import net.sourceforge.jnlp.runtime.ApplicationInstance;
import org.junit.jupiter.api.Test;

/**
 * Test with unsigned jars.
 */
class UnsignedJarsTest {
    @Test
    void launchUnsignedApp() throws Exception {
        final JNLPFile jnlpFile = new JNLPFileFactory().create(IntegrationTestResources.load("integration-app-25.jnlp"));
        final ResourceTrackerFactory resourceTrackerFactory = new DummyResourceTracker.Factory();

        new ApplicationInstance(jnlpFile, resourceTrackerFactory, false);
    }
}
