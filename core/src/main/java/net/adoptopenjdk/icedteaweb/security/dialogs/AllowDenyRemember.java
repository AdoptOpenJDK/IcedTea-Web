package net.adoptopenjdk.icedteaweb.security.dialogs;

import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember.RememberResult;

public class AllowDenyRemember {
    private final AllowDenyResult allowDenyResult;
    private final RememberResult rememberResult;

    AllowDenyRemember(final AllowDenyResult allowDenyResult, final RememberResult rememberResult) {
        this.allowDenyResult = allowDenyResult;
        this.rememberResult = rememberResult;
    }

    public AllowDenyResult getAllowDenyResult() {
        return allowDenyResult;
    }

    public RememberResult getRememberResult() {
        return rememberResult;
    }
}
