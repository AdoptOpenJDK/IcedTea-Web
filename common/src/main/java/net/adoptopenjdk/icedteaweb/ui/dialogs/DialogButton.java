package net.adoptopenjdk.icedteaweb.ui.dialogs;

import net.adoptopenjdk.icedteaweb.Assert;

import javax.swing.JButton;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DialogButton<R> {

    private final String text;

    private final Supplier<R> onAction;

    private final String description;

    private boolean enabled = true;

    private List<Consumer<Boolean>> enabledObservers = new CopyOnWriteArrayList<>();

    public DialogButton(final String text, final Supplier<R> onAction) {
        this(text, onAction, null);
    }

    public DialogButton(final String text, final Supplier<R> onAction, final String description) {
        this.text = Assert.requireNonBlank(text, "text");
        this.onAction = Assert.requireNonNull(onAction, "onAction");
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getText() {
        return text;
    }

    public Supplier<R> getOnAction() {
        return onAction;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
        enabledObservers.forEach(o -> o.accept(enabled));
    }

    public boolean isEnabled() {
        return enabled;
    }

    public JButton createButton(Consumer<R> actionResultConsumer) {
        final JButton button = new JButton(getText());
        if (getDescription() != null) {
            button.setToolTipText(getDescription());
        }
        button.addActionListener(e -> {
            final R result = getOnAction().get();
            Optional.ofNullable(actionResultConsumer).ifPresent(c -> c.accept(result));
        });
        enabledObservers.add(b -> button.setEnabled(b));
        button.setEnabled(isEnabled());
        return button;
    }
}
