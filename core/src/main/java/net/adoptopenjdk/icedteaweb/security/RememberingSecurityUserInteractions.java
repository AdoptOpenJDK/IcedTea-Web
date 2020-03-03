package net.adoptopenjdk.icedteaweb.security;

import net.adoptopenjdk.icedteaweb.lockingfile.StorageIoException;
import net.adoptopenjdk.icedteaweb.security.dialog.DialogProvider;
import net.adoptopenjdk.icedteaweb.security.dialog.result.AllowDeny;
import net.adoptopenjdk.icedteaweb.security.dialog.result.RememberableResult;
import net.adoptopenjdk.icedteaweb.userdecision.UserDecisions;
import net.adoptopenjdk.icedteaweb.userdecision.UserDecisionsFileStore;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

import java.util.Optional;

import static net.adoptopenjdk.icedteaweb.userdecision.UserDecision.Key.RUN_UNSIGNED_APPLICATION;
import static net.adoptopenjdk.icedteaweb.userdecision.UserDecision.of;

/**
 * Interactions with user for concerning security and permission related decisions.
 */
public class RememberingSecurityUserInteractions implements SecurityUserInteractions {
    private final UserDecisions userDecisions;

    public RememberingSecurityUserInteractions() {
        this(new UserDecisionsFileStore());
    }

    RememberingSecurityUserInteractions(UserDecisions userDecisions) {
        this.userDecisions = userDecisions;
    }

    public AllowDeny askUserForPermissionToRunUnsignedApplication(final JNLPFile file) {
        DeploymentConfiguration conf = JNLPRuntime.getConfiguration();
        if (conf == null) {
            throw new StorageIoException("JNLPRuntime configuration is null. Try to reinstall IcedTea-Web");
        }
        String s = conf.getProperty(ConfigurationConstants.KEY_SECURITY_LEVEL);

        // TODO: refactor usage of magic string to Enum
        if ("VERY_HIGH".equalsIgnoreCase(s)) {
            return AllowDeny.DENY;
        }

        if ("MEDIUM".equalsIgnoreCase(s)) {
            return AllowDeny.ALLOW;
        }

        final Optional<AllowDeny> remembered = this.userDecisions.getUserDecisions(RUN_UNSIGNED_APPLICATION, file, AllowDeny.class);

        return remembered.orElseGet(() -> {
            final RememberableResult<AllowDeny> dialogResult = DialogProvider.showUnsignedWarningDialog(file);
            userDecisions.save(dialogResult.getRemember(), file, of(RUN_UNSIGNED_APPLICATION, dialogResult.getResult()));
            return dialogResult.getResult();
        });
    }
}