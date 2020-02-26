package net.adoptopenjdk.icedteaweb.security.dialog;

import net.adoptopenjdk.icedteaweb.client.util.gridbag.GridBagPanelBuilder;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.security.dialog.panel.ReferencesPanel;
import net.sourceforge.jnlp.JNLPFile;

import java.net.URL;
import java.util.Set;

/**
 * Missing Application-Library-Allowable-Codebase (ALAC) Attribute Dialog. This dialog is shown if
 * the ALAC attribute is not provided in the manifest. The dialog lists the multiple hosts that
 * correspond to the locations of the JAR file and the JNLP file. The user can decide to run the
 * application and remember the decision to show this dialog again for the application or domain.
 *
 * The ALAC attribute identifies the locations where your signed application is expected to be found.
 */
public class MissingALACAttributeDialog extends MissingAttributeDialog {
    private static final Logger LOG = LoggerFactory.getLogger(MissingALACAttributeDialog.class);
    private static final Translator TRANSLATOR = Translator.getInstance();

    private static Set<URL> locations;

    public MissingALACAttributeDialog(final String message, final JNLPFile file) {
        super(message, file);
    }

    public static MissingALACAttributeDialog create(final JNLPFile file, final Set<URL> locations) {
        MissingALACAttributeDialog.locations = locations;
        String message = createMessage(file.getTitle(), file.getNotNullProbableCodeBase());
        return new MissingALACAttributeDialog(message, file);
    }

    @Override
    protected String createTitle() {
        return TRANSLATOR.translate("MissingALACAttribute");
    }

    @Override
    protected void getAdditionalApplicationDetails(final GridBagPanelBuilder gridBuilder) {
        gridBuilder.addKeyValueRow(TRANSLATOR.translate("Codebase"), file.getNotNullProbableCodeBase().toString());
        gridBuilder.addHorizontalSpacer();
        gridBuilder.addComponentRow(createLocationList(locations));
    }

    private static ReferencesPanel createLocationList(final Set<URL> locations) {
        return new ReferencesPanel(TRANSLATOR.translate("MissingALACAttributeLocationListTitle"), locations);
    }

    @Override
    protected ReferencesPanel createMoreInformationPanel() {
        return new ReferencesPanel(TRANSLATOR.translate("MissingALACAttributeMoreInfo"));
    }

    private static String createMessage(final String applicationName, final URL codebase) {
        return TRANSLATOR.translate("MissingALACAttributeMessage", applicationName, codebase);
    }
}
