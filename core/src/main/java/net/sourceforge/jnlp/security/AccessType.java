package net.sourceforge.jnlp.security;

/**
 * The types of access which may need user permission.
 */
public enum AccessType {

    READ_FILE,
    WRITE_FILE,
    CREATE_DESKTOP_SHORTCUT,
    CLIPBOARD_READ,
    CLIPBOARD_WRITE,
    PRINTER,
    NETWORK,
    VERIFIED,
    UNVERIFIED,
    PARTIALLY_SIGNED,
    UNSIGNED, /* requires confirmation with 'high-security' setting */
    SIGNING_ERROR
}
