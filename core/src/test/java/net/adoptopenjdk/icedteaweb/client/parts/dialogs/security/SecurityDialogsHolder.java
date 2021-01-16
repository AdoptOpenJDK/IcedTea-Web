package net.adoptopenjdk.icedteaweb.client.parts.dialogs.security;

import net.adoptopenjdk.icedteaweb.client.parts.dialogs.DialogFactory;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.Dialogs;

/**
 * Helper class for manipulating the implementation of the {@link Dialogs}.
 */
public class SecurityDialogsHolder {

    /**
     * The returned {@link AutoCloseable} must be called at the end of the test to allow other tests to set their own dialogs.
     */
    public static RevertDialogsToDefault setSecurityDialogForTests(DialogFactory dialogs) {
        return Dialogs.setDialogFactory(dialogs)::uninstall;
    }

    public interface RevertDialogsToDefault extends AutoCloseable {
        @Override
        void close();
    }
}
