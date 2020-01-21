package net.adoptopenjdk.icedteaweb.integration.signing;

import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.SecurityDialogs.Dialogs;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.SecurityDialogsHolder;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.SecurityDialogsHolder.RevertDialogsToDefault;
import net.adoptopenjdk.icedteaweb.integration.DummyResourceTracker;
import net.adoptopenjdk.icedteaweb.integration.IntegrationTestResources;
import net.adoptopenjdk.icedteaweb.resources.ResourceTrackerFactory;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.YesNoSandbox;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.JNLPFileFactory;
import net.sourceforge.jnlp.runtime.ApplicationInstance;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test with unsigned jars.
 */
@ExtendWith(MockitoExtension.class)
class UnsignedJarsTest {
    @Test
    void launchUnsignedApp(@Mock Dialogs dialogs) throws Exception {
        final JNLPFile jnlpFile = new JNLPFileFactory().create(IntegrationTestResources.load("integration-app-25.jnlp"));
        final ResourceTrackerFactory resourceTrackerFactory = new DummyResourceTracker.Factory();

        when(dialogs.showPartiallySignedWarningDialog(any(), any(), any())).thenReturn(YesNoSandbox.yes());

        try (final RevertDialogsToDefault r = SecurityDialogsHolder.setSecurityDialogForTests(dialogs)) {

            // when
            new ApplicationInstance(jnlpFile, resourceTrackerFactory);
        } finally {

            // then
            verify(dialogs).showPartiallySignedWarningDialog(any(), any(), any());
        }
    }
}
