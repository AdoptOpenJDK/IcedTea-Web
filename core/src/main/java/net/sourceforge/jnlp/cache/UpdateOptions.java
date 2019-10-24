package net.sourceforge.jnlp.cache;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.jnlp.element.update.UpdateCheck;
import net.adoptopenjdk.icedteaweb.jnlp.element.update.UpdatePolicy;

public class UpdateOptions {
    // update details from the JNLP file
    final private UpdateCheck updateCheck;
    private UpdatePolicy updatePolicy;

    // update policy option values
    private boolean promptUpdateLaunchCachedVersion;
    private boolean promptRunCancelAndAbortRunningTheApplication;
    private boolean alwaysDownloadUpdates;

    public UpdateOptions(final UpdateCheck updateCheck, final UpdatePolicy updatePolicy) {
        this.updateCheck = Assert.requireNonNull(updateCheck, "updateCheck");
        this.updatePolicy = Assert.requireNonNull(updatePolicy, "updatePolicy");
        alwaysDownloadUpdates =  updatePolicy == UpdatePolicy.ALWAYS; // initialize when policy is always
    }

    public UpdateOptions(final UpdateCheck updateCheck, final boolean alwaysDownloadUpdates) {
        this.updateCheck = updateCheck;
        this.alwaysDownloadUpdates = alwaysDownloadUpdates;
    }

    public void setAlwaysDownloadUpdates(final boolean alwaysDownloadUpdates) {
        this.alwaysDownloadUpdates = alwaysDownloadUpdates;
    }

    public void setLaunchCachedVersion(final boolean launchCachedVersion) {
        if (updatePolicy != UpdatePolicy.PROMPT_UPDATE) {
            throw new IllegalStateException("Update policy must be prompt-update.");
        }
        this.promptUpdateLaunchCachedVersion = launchCachedVersion;
        this.alwaysDownloadUpdates = !launchCachedVersion;
    }

    public void setCancelAndAbortRunningTheApplication(final boolean runCancelAndAbortRunningTheApplication) {
        if (updatePolicy != UpdatePolicy.PROMPT_RUN) {
            throw new IllegalStateException("Update policy must be prompt-run.");
        }
        this.promptRunCancelAndAbortRunningTheApplication = runCancelAndAbortRunningTheApplication;
        this.alwaysDownloadUpdates = !runCancelAndAbortRunningTheApplication;
    }

    // either set always or set by user prompt result

    boolean alwaysDownloadUpdates() {
        return alwaysDownloadUpdates;
    }

    // update policy user prompt results

    public boolean launchCachedVersion() {
        return promptUpdateLaunchCachedVersion;
    }

    public boolean runCancelAndAbortRunningTheApplication() {
        return promptRunCancelAndAbortRunningTheApplication;
    }

    // update checks

    public boolean alwaysCheckForUpdates() {
        return updateCheck == UpdateCheck.ALWAYS;
    }

    public boolean shouldCheckForUpdatesUntilTimeout() {
        return updateCheck == UpdateCheck.TIMEOUT;
    }

    public boolean shouldCheckForUpdatesInTheBackground() {
        return updateCheck == UpdateCheck.BACKGROUND;
    }
}
