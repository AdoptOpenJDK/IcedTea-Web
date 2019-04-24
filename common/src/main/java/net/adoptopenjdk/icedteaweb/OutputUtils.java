package net.adoptopenjdk.icedteaweb;

import java.io.PrintWriter;
import java.io.StringWriter;

public class OutputUtils {

    public static String exceptionToString(Throwable t) {
        if (t == null) {
            return null;
        }
        String s = "Error during processing of exception";
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            s = sw.toString();
            pw.close();
            sw.close();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return s;
    }
}
