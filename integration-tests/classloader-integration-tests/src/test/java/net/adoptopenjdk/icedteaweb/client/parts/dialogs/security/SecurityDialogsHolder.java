package net.adoptopenjdk.icedteaweb.client.parts.dialogs.security;

/**
 * Helper class for manipulating the implementation of the {@link SecurityDialogs}.
 */
public class SecurityDialogsHolder {

    /**
     * The returned {@link AutoCloseable} must be called at the end of the test to allow other tests to set their own dialogs.
     */
    public static RevertDialogsToDefault setSecurityDialogForTests(SecurityDialogs.Dialogs dialogs) {
        return SecurityDialogs.setDialogForTesting(dialogs)::run;
    }

    public interface RevertDialogsToDefault extends AutoCloseable {
        @Override
        void close();
    }
}
