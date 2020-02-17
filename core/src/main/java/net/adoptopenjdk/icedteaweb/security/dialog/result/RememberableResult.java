package net.adoptopenjdk.icedteaweb.security.dialog.result;

import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember.Remember;

public class RememberableResult<T> {
    private final T result;
    private final Remember remember;

    public RememberableResult(final T result, final Remember remember) {
        this.result = result;
        this.remember = remember;
    }

    public T getResult() {
        return result;
    }

    public Remember getRemember() {
        return remember;
    }
}
