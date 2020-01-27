package net.adoptopenjdk.icedteaweb.integration.signing;

import net.adoptopenjdk.icedteaweb.client.parts.dialogs.DialogFactory;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.Dialogs;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.SecurityDialogsHolder;
import net.adoptopenjdk.icedteaweb.integration.DummyResourceTracker;
import net.adoptopenjdk.icedteaweb.integration.IntegrationTestResources;
import net.adoptopenjdk.icedteaweb.resources.ResourceTrackerFactory;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.YesNoSandboxLimited;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.JNLPFileFactory;
import net.sourceforge.jnlp.runtime.ApplicationInstance;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    void launchUnsignedApp(@Mock DialogFactory dialogFactory) throws Exception {
        final JNLPFile jnlpFile = new JNLPFileFactory().create(IntegrationTestResources.load("integration-app-25.jnlp"));
        final ResourceTrackerFactory resourceTrackerFactory = new DummyResourceTracker.Factory();

        when(dialogFactory.showUnsignedWarningDialog(jnlpFile)).thenReturn(YesNoSandboxLimited.yes());

        try (Dialogs.Uninstaller uninstaller = SecurityDialogsHolder.setSecurityDialogForTests(dialogFactory)){
            // when
            new ApplicationInstance(jnlpFile, resourceTrackerFactory);
        } finally {
            // then
            verify(dialogFactory).showUnsignedWarningDialog(jnlpFile);
        }
    }
}
