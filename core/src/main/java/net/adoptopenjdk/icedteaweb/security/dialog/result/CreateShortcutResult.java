package net.adoptopenjdk.icedteaweb.security.dialog.result;

import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember.RememberResult;

public class CreateShortcutResult {
    private final AllowDeny createDesktopShortcut;
    private final AllowDeny createMenuShortcut;
    private final RememberResult rememberResult;


    public CreateShortcutResult(final AllowDeny createDesktopShortcut, final AllowDeny createMenuShortcut, final RememberResult rememberResult) {
        this.createDesktopShortcut = createDesktopShortcut;
        this.createMenuShortcut = createMenuShortcut;
        this.rememberResult = rememberResult;
    }

    public AllowDeny getCreateDesktopShortcut() {
        return createDesktopShortcut;
    }

    public AllowDeny getCreateMenuShortcut() {
        return createMenuShortcut;
    }

    public RememberResult getRememberResult() {
        return rememberResult;
    }
}
