package net.adoptopenjdk.icedteaweb.integration.signing;

import net.adoptopenjdk.icedteaweb.client.parts.dialogs.DialogFactory;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.Dialogs;
import net.adoptopenjdk.icedteaweb.integration.DummyResourceTracker;
import net.adoptopenjdk.icedteaweb.integration.IntegrationTestResources;
import net.adoptopenjdk.icedteaweb.resources.ResourceTrackerFactory;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.YesNoSandboxLimited;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.JNLPFileFactory;
import net.sourceforge.jnlp.runtime.ApplicationInstance;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test with unsigned jars.
 */
@ExtendWith(MockitoExtension.class)
class UnsignedJarsTest {

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    @Disabled
    void launchUnsignedApp(@Mock DialogFactory dialogFactory) throws Exception {
        final JNLPFile jnlpFile = new JNLPFileFactory().create(IntegrationTestResources.load("integration-app-25.jnlp"));
        final ResourceTrackerFactory resourceTrackerFactory = new DummyResourceTracker.Factory();

        when(dialogFactory.showUnsignedWarningDialog(jnlpFile)).thenReturn(YesNoSandboxLimited.yes());

        try (Dialogs.Uninstaller uninstaller = Dialogs.setDialogFactory(dialogFactory)) {
            // when
            final ThreadGroup threadGroup = new ThreadGroup("Test-Group");
            new ApplicationInstance(jnlpFile, resourceTrackerFactory, threadGroup);
        }
        verify(dialogFactory).showUnsignedWarningDialog(jnlpFile);
    }
}
