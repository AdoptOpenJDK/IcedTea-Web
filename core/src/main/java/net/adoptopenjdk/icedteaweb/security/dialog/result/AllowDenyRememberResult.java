package net.adoptopenjdk.icedteaweb.security.dialog.result;

import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember.RememberResult;

public class AllowDenyRememberResult {
    private final AllowDeny allowDenyResult;
    private final RememberResult rememberResult;

    public AllowDenyRememberResult(final AllowDeny allowDenyResult, final RememberResult rememberResult) {
        this.allowDenyResult = allowDenyResult;
        this.rememberResult = rememberResult;
    }

    public AllowDeny getAllowDenyResult() {
        return allowDenyResult;
    }

    public RememberResult getRememberResult() {
        return rememberResult;
    }
}
