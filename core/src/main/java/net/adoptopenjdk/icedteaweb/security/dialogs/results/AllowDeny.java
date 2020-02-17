package net.adoptopenjdk.icedteaweb.security.dialogs.results;

import javax.swing.JCheckBox;

public enum AllowDeny {
    ALLOW, DENY;

    public static AllowDeny valueOf(final JCheckBox checkbox) {
        return checkbox.isSelected() ? ALLOW : DENY;
    }
}
