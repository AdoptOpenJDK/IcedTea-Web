package net.adoptopenjdk.icedteaweb.security.dialog;

import net.adoptopenjdk.icedteaweb.client.util.gridbag.GridBagPanelBuilder;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.image.ImageGallery;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.security.dialog.panel.ReferencesPanel;
import net.sourceforge.jnlp.JNLPFile;

import javax.swing.ImageIcon;
import java.net.URL;
import java.util.Set;

/**
 * The ALAC attribute identifies the locations where your signed application is expected to be found.
 */
public class MatchingALACAttributeDialog extends MissingAttributeDialog {
    private static final Logger LOG = LoggerFactory.getLogger(MatchingALACAttributeDialog.class);
    private static final Translator TRANSLATOR = Translator.getInstance();

    private static Set<URL> locations;

    public MatchingALACAttributeDialog(final String message, final JNLPFile file) {
        super(message, file);
    }

    public static MatchingALACAttributeDialog create(final JNLPFile file, final Set<URL> locations) {
        MatchingALACAttributeDialog.locations = locations;
        String message = createMessage(file.getTitle(), file.getNotNullProbableCodeBase());
        return new MatchingALACAttributeDialog(message, file);
    }

    @Override
    protected ImageIcon createIcon() {
        return ImageGallery.QUESTION.asImageIcon();
    }

    @Override
    protected String createTitle() {
        return TRANSLATOR.translate("MatchingALACAttribute");
    }

    @Override
    protected void getAdditionalApplicationDetails(final GridBagPanelBuilder gridBuilder) {
        gridBuilder.addKeyValueRow(TRANSLATOR.translate("Codebase"), file.getNotNullProbableCodeBase().toString());
        gridBuilder.addHorizontalSpacer();
        gridBuilder.addComponentRow(createLocationList(locations));
    }

    private static ReferencesPanel createLocationList(final Set<URL> locations) {
        return new ReferencesPanel(TRANSLATOR.translate("MatchingALACAttributeLocationListTitle"), locations);
    }

    @Override
    protected ReferencesPanel createMoreInformationPanel() {
        return new ReferencesPanel(TRANSLATOR.translate("MatchingALACAttributeMoreInfo"));
    }

    private static String createMessage(final String applicationName, final URL codebase) {
        return TRANSLATOR.translate("MatchingALACAttributeMessage", applicationName, codebase);
    }
}
