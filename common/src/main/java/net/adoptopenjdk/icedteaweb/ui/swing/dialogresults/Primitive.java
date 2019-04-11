package net.adoptopenjdk.icedteaweb.ui.swing.dialogresults;

public enum Primitive {

    YES(0), NO(1), CANCEL(2), SANDBOX(2), SKIP(0);

    private final int legacyButton;

    Primitive(int legacyButton) {
        this.legacyButton = legacyButton;
    }

    public int getLegacyButton() {
        return legacyButton;
    }

}
