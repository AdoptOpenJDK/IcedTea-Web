package net.adoptopenjdk.icedteaweb.security.dialogs;

import javax.swing.JCheckBox;

public enum AllowDeny {
    ALLOW, DENY;

    public static AllowDeny valueOf(final JCheckBox checkbox) {
        return checkbox.isSelected() ? ALLOW : DENY;
    }
}
