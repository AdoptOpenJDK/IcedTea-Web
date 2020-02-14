package net.adoptopenjdk.icedteaweb.security.dialogs;

import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember.RememberResult;
import net.adoptopenjdk.icedteaweb.i18n.Translator;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;
import java.awt.FlowLayout;

public class RememberUserDecisionPanel extends JPanel {
    private static final Translator TRANSLATOR = Translator.getInstance();

    final JRadioButton forApplicationRadioButton = new JRadioButton(TRANSLATOR.translate("EXAWrememberByApp"));
    final JRadioButton forDomainRadioButton = new JRadioButton(TRANSLATOR.translate("EXAWrememberByPage"));
    final JRadioButton doNotRememberRadioButton = new JRadioButton(TRANSLATOR.translate("EXAWdontRemember"), true);

    public RememberUserDecisionPanel() {
        super(new FlowLayout(FlowLayout.CENTER));
        this.setBorder(new EmptyBorder(0, 0, 0, 0));

        this.add(forApplicationRadioButton);
        this.add(forDomainRadioButton);
        this.add(doNotRememberRadioButton);

        forApplicationRadioButton.setToolTipText(TRANSLATOR.translate("EXAWrememberByAppTooltip"));
        forDomainRadioButton.setToolTipText(TRANSLATOR.translate("EXAWrememberByPageTooltip"));
        doNotRememberRadioButton.setToolTipText(TRANSLATOR.translate("EXAWdontRememberTooltip"));

        final ButtonGroup bg = new ButtonGroup();
        bg.add(forApplicationRadioButton);
        bg.add(forDomainRadioButton);
        bg.add(doNotRememberRadioButton);

        this.validate();
    }

    public RememberResult getResult() {
        if (forApplicationRadioButton.isSelected()) {
            return RememberResult.REMEMBER_BY_APPLICATION;
        }
        if (forDomainRadioButton.isSelected()) {
            return RememberResult.REMEMBER_BY_DOMAIN;
        }
        return RememberResult.DO_NOT_REMEMBER;
    }
}
