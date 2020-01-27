package net.adoptopenjdk.icedteaweb.client.parts.dialogs;

/**
 * Types of dialogs we can create
 */
public enum DialogType {
    CERT_WARNING,
    MORE_INFO,
    CERT_INFO,
    SINGLE_CERT_INFO,
    ACCESS_WARNING,
    PARTIALLY_SIGNED_WARNING,
    UNSIGNED_WARNING, /* requires confirmation with 'high-security' setting */
    APPLET_WARNING,
    AUTHENTICATION,
    UNSIGNED_EAS_NO_PERMISSIONS_WARNING, /* when Extended applet security is at High Security and no permission attribute is find, */
    MISSING_ALACA, /*alaca - Application-Library-Allowable-Codebase Attribute*/
    MATCHING_ALACA,
    SECURITY_511
}
