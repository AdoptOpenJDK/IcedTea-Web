package net.adoptopenjdk.icedteaweb.security.dialog.result;

public class CreateShortcutResult {
    private final AllowDeny createDesktopShortcut;
    private final AllowDeny createMenuShortcut;


    public CreateShortcutResult(final AllowDeny createDesktopShortcut, final AllowDeny createMenuShortcut) {
        this.createDesktopShortcut = createDesktopShortcut;
        this.createMenuShortcut = createMenuShortcut;
    }

    public AllowDeny getCreateDesktopShortcut() {
        return createDesktopShortcut;
    }

    public AllowDeny getCreateMenuShortcut() {
        return createMenuShortcut;
    }
}
