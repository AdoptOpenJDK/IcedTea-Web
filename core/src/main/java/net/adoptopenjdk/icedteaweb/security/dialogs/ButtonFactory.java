package net.adoptopenjdk.icedteaweb.security.dialogs;

import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.ui.dialogs.DialogButton;

import java.util.function.Supplier;

public class ButtonFactory {
    private final static Translator TRANSLATOR = Translator.getInstance();

    public static <R> DialogButton<R> createOkButton(final Supplier<R> onAction) {
        return new DialogButton<>(TRANSLATOR.translate("ButOk"), onAction);
    }

    public static <R> DialogButton<R> createCancelButton(final Supplier<R> onAction) {
        return new DialogButton<>(TRANSLATOR.translate("ButCancel"), onAction);
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
