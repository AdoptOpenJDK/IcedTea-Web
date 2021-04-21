package net.adoptopenjdk.icedteaweb.client.controlpanel;

import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.sourceforge.jnlp.util.ImageResources;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.util.List;

import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;

public class DefaultControlPanelStyle implements ControlPanelStyle {
    @Override
    public String getDialogTitle() {
        return Translator.R("CPHead");
    }

    @Override
    public JPanel createHeader() {

        final JLabel about = new JLabel(R("CPMainDescriptionShort"));
        about.setFont(about.getFont().deriveFont(about.getFont().getSize2D() + 2).deriveFont(Font.BOLD));
        about.setForeground(UIManager.getColor("TextPane.caretForeground"));

        final JLabel description = new JLabel(R("CPMainDescriptionLong"));
        description.setBorder(new EmptyBorder(2, 0, 2, 0));
        description.setForeground(UIManager.getColor("TextPane.caretForeground"));

        final JPanel descriptionPanel = new JPanel(new GridLayout(0, 1));
        descriptionPanel.setBackground(UIManager.getColor("TextPane.background"));
        descriptionPanel.add(about);
        descriptionPanel.add(description);

        final JLabel image = new JLabel();
        image.setIcon(new ImageIcon(ImageResources.INSTANCE.getApplicationImages().get(0)));


        final JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UIManager.getColor("TextPane.background"));
        topPanel.add(descriptionPanel, BorderLayout.LINE_START);
        topPanel.add(image, BorderLayout.LINE_END);
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        return topPanel;
    }

    @Override
    public List<? extends Image> getDialogIcons() {
        return ImageResources.INSTANCE.getApplicationImages();
    }
}
