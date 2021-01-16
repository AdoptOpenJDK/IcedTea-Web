package net.adoptopenjdk.icedteaweb.ui.swing.dialogresults;

import java.util.Objects;

public class ShortcutResult {

    public static ShortcutResult readValue(String s) {
        if (s.trim().isEmpty()) {
            return null;
        }
        String[] sq = s.split(",");
        ShortcutResult sr = new ShortcutResult(Boolean.valueOf(sq[3]));
        sr.browser = sq[0];
        sr.fixHref = Boolean.parseBoolean(sq[1]);
        if (!sq[2].equalsIgnoreCase("null")) {
            sr.shortcutType = AccessWarningPaneComplexReturn.Shortcut.valueOf(sq[2]);
        }
        return sr;
    }

    public String writeValue() {
        StringBuilder sb = new StringBuilder();
        sb.append(browser).append(",")
                .append(fixHref).append(",")
                .append(shortcutType).append(",")
                .append(create).append(",");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ShortcutResult)) {
            return false;
        }
        ShortcutResult sr = (ShortcutResult) obj;
        return this.create == sr.create && this.fixHref == sr.fixHref
                && this.browser.equals(sr.browser) && this.shortcutType == sr.shortcutType;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + Objects.hashCode(this.browser);
        hash = 89 * hash + (this.fixHref ? 1 : 0);
        hash = 89 * hash + Objects.hashCode(this.shortcutType);
        hash = 89 * hash + (this.create ? 1 : 0);
        return hash;
    }

    private String browser = "not_found_browser";
    private boolean fixHref = false;
    private AccessWarningPaneComplexReturn.Shortcut shortcutType = null;
    private final boolean create;

    ShortcutResult(String browser, boolean fixHref, AccessWarningPaneComplexReturn.Shortcut shortcutType, boolean create) {
        this.browser = browser;
        this.fixHref = fixHref;
        this.shortcutType = shortcutType;
        this.create = create;
    }

    public ShortcutResult(boolean create) {
        this.create = create;
    }

    public boolean isCreate() {
        return create;
    }

    public String getBrowser() {
        return browser;
    }

    public AccessWarningPaneComplexReturn.Shortcut getShortcutType() {
        return shortcutType;
    }

    public boolean isFixHref() {
        return fixHref;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public void setFixHref(boolean fixHref) {
        this.fixHref = fixHref;
    }

    public void setShortcutType(AccessWarningPaneComplexReturn.Shortcut shortcutType) {
        this.shortcutType = shortcutType;
    }

}
