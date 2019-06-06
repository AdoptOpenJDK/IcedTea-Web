package net.adoptopenjdk.icedteaweb.logging;

import java.util.Objects;

/**
 * Base class for {@link Logger} implementation.
 * This class provides implementation for common methods.
 */
public abstract class BaseLogger implements Logger {

    protected String expand(String msg, Object[] args) {
        if (msg == null) {
            return "null";
        }

        if (args == null) {
            return msg;
        }

        final int argsLength = args.length;

        if (argsLength == 0) {
            return msg;
        }

        final StringBuilder result = new StringBuilder();
        int lastIdx = 0;
        for (Object arg : args) {
            final String argString = Objects.toString(arg);
            final int idx = result.indexOf("{}", lastIdx);

            if (idx < 0) {
                break;
            }

            result.append(msg, lastIdx, idx).append(argString);
            lastIdx = idx + 2;
        }

        result.append(msg.substring(lastIdx));

        return result.toString();
    }
}