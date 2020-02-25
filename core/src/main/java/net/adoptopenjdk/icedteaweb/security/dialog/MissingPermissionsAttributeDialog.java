package net.adoptopenjdk.icedteaweb.security.dialog;

import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.security.dialog.panel.ReferencesPanel;
import net.sourceforge.jnlp.JNLPFile;

import java.net.URL;

/**
 * This security dialog is shown if the permissions attribute is not provided in the JNLP. The user can decide
 * to run the application and remember the decision to show this dialog again for the application or domain.
 *
 * The Permissions attribute is used to verify that the permissions level requested by the application when
 * it runs matches the permissions level that was set when the JAR file was created.
 */
public class MissingPermissionsAttributeDialog extends MissingAttributeDialog {
    private static final Logger LOG = LoggerFactory.getLogger(MissingPermissionsAttributeDialog.class);
    private static final Translator TRANSLATOR = Translator.getInstance();

    public MissingPermissionsAttributeDialog(final String message, final JNLPFile file) {
        super(message, file);
    }

    public static MissingPermissionsAttributeDialog create(final JNLPFile file) {
        String message = createMessage(file.getTitle(), file.getNotNullProbableCodeBase());
        return new MissingPermissionsAttributeDialog(message, file);
    }

    @Override
    protected String createTitle() {
        return TRANSLATOR.translate("MissingPermissionsAttribute");
    }

    @Override
    protected ReferencesPanel createMoreInformationPanel() {
        return new ReferencesPanel(TRANSLATOR.translate("MissingPermissionsAttributeMoreInfo"));
    }

    private static String createMessage(final String applicationName, final URL codebase) {
        return TRANSLATOR.translate("MissingPermissionsAttributeMessage", applicationName, codebase);
    }
}
