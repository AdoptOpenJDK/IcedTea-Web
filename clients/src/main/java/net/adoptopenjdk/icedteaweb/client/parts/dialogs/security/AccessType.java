package net.adoptopenjdk.icedteaweb.client.parts.dialogs.security;

/**
 * The types of access which may need user permission.
 */
public enum AccessType {

    READ_FILE,
    WRITE_FILE,
    CREATE_DESTKOP_SHORTCUT,
    CLIPBOARD_READ,
    CLIPBOARD_WRITE,
    PRINTER,
    NETWORK,
    VERIFIED,
    UNVERIFIED,
    PARTIALLYSIGNED,
    UNSIGNED, /* requires confirmation with 'high-security' setting */
    SIGNING_ERROR
}
