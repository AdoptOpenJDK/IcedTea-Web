package net.adoptopenjdk.icedteaweb;

import java.io.PrintWriter;
import java.io.StringWriter;

public class OutputUtils {
    public static String exceptionToString(final Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }
}
