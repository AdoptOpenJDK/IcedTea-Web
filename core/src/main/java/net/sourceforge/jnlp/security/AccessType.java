package net.sourceforge.jnlp.security;

/**
 * The types of access which may need user permission.
 */
public enum AccessType {

    READ_WRITE_FILE,
    READ_FILE,
    WRITE_FILE,
    CREATE_DESKTOP_SHORTCUT,
    CLIPBOARD_READ,
    CLIPBOARD_WRITE,
    PRINTER,
    NETWORK,

    // the following are certificate related states and have nothing to do with access types
    VERIFIED,
    UNVERIFIED,
    PARTIALLY_SIGNED,
    UNSIGNED, /* requires confirmation with 'high-security' setting */
    SIGNING_ERROR
}
