package net.adoptopenjdk.icedteaweb.security.dialog.panel;

import net.adoptopenjdk.icedteaweb.StringUtils;
import net.adoptopenjdk.icedteaweb.client.util.gridbag.ComponentRow;
import net.adoptopenjdk.icedteaweb.client.util.gridbag.GridBagRow;
import net.adoptopenjdk.icedteaweb.client.util.gridbag.KeyValueRow;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.InformationDesc;
import net.sourceforge.jnlp.JNLPFile;

import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import java.awt.Font;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.ofNullable;
import static net.adoptopenjdk.icedteaweb.ui.ApplicationStyleConstants.PRIMARY_WARNING_COLOR;

public class LayoutPartsBuilder {
    private static final Translator TRANSLATOR = Translator.getInstance();

    public static List<GridBagRow> getApplicationDetails(JNLPFile file) {
        final List<GridBagRow> rows = new ArrayList<>();

        final JLabel applicationSectionTitle = new JLabel(TRANSLATOR.translate("ApplicationDetails"));
        applicationSectionTitle.setFont(applicationSectionTitle.getFont().deriveFont(Font.BOLD));
        applicationSectionTitle.setBorder(new EmptyBorder(0, 0, 5, 0));

        rows.add(new ComponentRow(applicationSectionTitle));

        if (file.isUnsigend()) {
            final JLabel unsignedWarningLabel = new JLabel(TRANSLATOR.translate("SUnverifiedJnlp"));
            unsignedWarningLabel.setBorder(new EmptyBorder(0, 0, 5, 0));
            unsignedWarningLabel.setForeground(PRIMARY_WARNING_COLOR);

            rows.add(new ComponentRow(unsignedWarningLabel));
        }

        final String name = ofNullable(file)
                .map(JNLPFile::getInformation)
                .map(InformationDesc::getTitle)
                .orElse(TRANSLATOR.translate("SNoAssociatedCertificate"));
        rows.add(new KeyValueRow(TRANSLATOR.translate("Name"), name));

        final String publisher = ofNullable(file)
                .map(JNLPFile::getInformation)
                .map(InformationDesc::getVendor)
                .map(v -> v + " " + TRANSLATOR.translate("SUnverified"))
                .orElse(TRANSLATOR.translate("SNoAssociatedCertificate"));
        rows.add(new KeyValueRow(TRANSLATOR.translate("Publisher"), publisher));


        final String fromFallback = ofNullable(file)
                .map(JNLPFile::getSourceLocation)
                .map(URL::getAuthority)
                .orElse("");

        final String from = ofNullable(file)
                .map(JNLPFile::getInformation)
                .map(InformationDesc::getHomepage)
                .map(URL::toString)
                .map(i -> !StringUtils.isBlank(i) ? i : null)
                .orElse(fromFallback);
        rows.add(new KeyValueRow(TRANSLATOR.translate("From"), from));
        return rows;
    }

}
