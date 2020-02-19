package net.adoptopenjdk.icedteaweb.security.dialog;

import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.ui.dialogs.DialogButton;

import java.util.function.Supplier;

public class ButtonFactory {
    private static final Translator TRANSLATOR = Translator.getInstance();

    public static <R> DialogButton<R> createAllowButton(final Supplier<R> onAction) {
        return new DialogButton<>(TRANSLATOR.translate("ButAllow"), onAction);
    }

    public static <R> DialogButton<R> createCreateButton(final Supplier<R> onAction) {
        return new DialogButton<>(TRANSLATOR.translate("ButCreate"), onAction);
    }

    public static <R> DialogButton<R> createDenyButton(final Supplier<R> onAction) {
        return new DialogButton<>(TRANSLATOR.translate("ButDeny"), onAction);
    }

    public static <R> DialogButton<R> createCancelButton(final Supplier<R> onAction) {
        return new DialogButton<>(TRANSLATOR.translate("ButCancel"), onAction);
    }

    public static <R> DialogButton<R> createCloseButton(final Supplier<R> onAction) {
        return new DialogButton<>(TRANSLATOR.translate("ButClose"), onAction);
    }

   public static <R> DialogButton<R> createCancelButton(String toolTipText, final Supplier<R> onAction) {
        return new DialogButton<>(TRANSLATOR.translate("ButCancel"), onAction, toolTipText);
    }

    public static <R> DialogButton<R> createRunButton(final Supplier<R> onAction) {
        return new DialogButton<>(TRANSLATOR.translate("ButRun"), onAction, TRANSLATOR.translate("CertWarnRunTip"));
    }

    public static <R> DialogButton<R> createSandboxButton(final Supplier<R> onAction) {
        return new DialogButton<>(TRANSLATOR.translate("ButSandbox"), onAction, TRANSLATOR.translate("CertWarnSandboxTip"));
    }

    public static <R> DialogButton<R> createYesButton(final Supplier<R> onAction) {
        return new DialogButton<>(TRANSLATOR.translate("ButYes"), onAction, TRANSLATOR.translate("CertWarnHTTPSAcceptTip"));
    }

    public static <R> DialogButton<R> createNoButton(final Supplier<R> onAction) {
        return new DialogButton<>(TRANSLATOR.translate("ButNo"), onAction, TRANSLATOR.translate("CertWarnHTTPSRejectTip"));
    }
}
