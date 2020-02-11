package net.adoptopenjdk.icedteaweb.ui.dialogs;

import javax.swing.JPanel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class ButtonBasedDialogWithResult<R> extends DialogWithResult<R> {

    private final List<DialogButton<R>> buttons;

    public ButtonBasedDialogWithResult(final String title, final DialogButton<R>... buttons) {
        this(title, Arrays.asList(buttons));
    }

    public ButtonBasedDialogWithResult(final String title, final List<DialogButton<R>> buttons) {
        super(title);
        this.buttons = new ArrayList<>(buttons);
    }

    protected JPanel createContentPane() {
        return createContentPane(buttons);
    }

    protected abstract JPanel createContentPane(final List<DialogButton<R>> buttons);

}
