package net.adoptopenjdk.icedteaweb.security.dialog;

import net.adoptopenjdk.icedteaweb.client.util.gridbag.GridBagPanelBuilder;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.image.ImageGallery;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.ui.dialogs.DialogButton;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.NamePassword;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * <img src="doc-files/AuthenticationDialog.png"></img>
 */
public class AuthenticationDialog extends BasicSecurityDialog<Optional<NamePassword>> {
    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationDialog.class);
    private static final Translator TRANSLATOR = Translator.getInstance();

    private final DialogButton<Optional<NamePassword>> loginButton;
    private final DialogButton<Optional<NamePassword>> cancelButton;
    private JTextField usernameTextField;
    private JPasswordField passwordTextField;

    private AuthenticationDialog(final String message) {
        super(message);
        loginButton = ButtonFactory.createLoginButton(() -> {
            final NamePassword value = new NamePassword(usernameTextField.getText(), passwordTextField.getPassword());
            return Optional.of(value);
        });
        cancelButton = ButtonFactory.createCancelButton(Optional::empty);
    }

    /**
     * @param host   hostname of the site or proxy requesting authentication
     * @param port   port number for the requested connection
     * @param prompt the prompt string given by the requestor
     * @param type   type that defines that requestor is a Proxy or a Server
     * @return
     */
    public static AuthenticationDialog create(final String host, final int port, final String prompt, final String type) {
        String message = TRANSLATOR.translate("SAuthenticationPrompt", type, host, prompt);
        return new AuthenticationDialog(message);
    }

    public Dimension getPreferredSize() {
        return new Dimension(500, super.getPreferredSize().height);
    }

    @Override
    protected ImageIcon createIcon() {
        return ImageGallery.LOGIN.asImageIcon();
    }

    @Override
    protected String createTitle() {
        return TRANSLATOR.translate("CVPasswordTitle");
    }

    @Override
    protected JComponent createDetailPaneContent() {
        final GridBagPanelBuilder gridBuilder = new GridBagPanelBuilder();
        try {
            usernameTextField = new JTextField("", 20);
            gridBuilder.addKeyComponentRow(TRANSLATOR.translate("Username"), usernameTextField);
            passwordTextField = new JPasswordField("", 20);
            gridBuilder.addKeyComponentRow(TRANSLATOR.translate("Password"), passwordTextField);

        } catch (final Exception e) {
            LOG.error("Error while trying to read properties for AuthenticationDialog!", e);
        }
        return gridBuilder.createGrid();
    }

    @Override
    protected List<DialogButton<Optional<NamePassword>>> createButtons() {
        return Arrays.asList(loginButton, cancelButton);
    }
}
