// Copyright (C) 2001-2003 Jon A. Maxwell (JAM)
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

package net.sourceforge.jnlp;

import java.io.*;
import java.util.*;

import net.sourceforge.jnlp.util.*;

/**
 * Thrown when a JNLP application, applet, or installer could not
 * be created.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.9 $
 */
public class LaunchException extends Exception {

    /** the original exception */
    private Throwable cause = null;

    /** the file being launched */
    private JNLPFile file;

    /** the category of the exception */
    private String category;

    /** summary */
    private String summary;

    /** description of the action that was taking place */
    private String description;

    /** severity of the warning/error */
    private String severity;

    /**
     * Creates a LaunchException without detail message.
     */
    public LaunchException(JNLPFile file, Exception cause, String severity, String category, String summary, String description) {
        super(severity + ": " + category + ": " + summary);

        this.file = file;
        this.category = category;
        this.summary = summary;
        this.description = description;
        this.severity = severity;

        // replace with setCause when no longer 1.3 compatible
        this.cause = cause;
    }

    /**
     * Creates a LaunchException with a cause.
     */
    public LaunchException(Throwable cause) {
        this(cause.getMessage());

        // replace with setCause when no longer 1.3 compatible
        this.cause = cause;
    }

    /**
     * Creates a LaunchException with a cause and detail message
     */
    public LaunchException(String message, Throwable cause) {
        this(message + ": " + cause.getMessage());

        // replace with setCause when no longer 1.3 compatible
        this.cause = cause;
    }

    /**
     * Constructs a LaunchException with the specified detail
     * message.
     *
     * @param message the detail message
     */
    public LaunchException(String message) {
        super(message);
    }

    /**
     * Returns the JNLPFile being launched.
     */
    public JNLPFile getFile() {
        return file;
    }

    /**
     * Returns the category string, a short description of the
     * exception suitable for displaying in a window title.
     */
    public String getCategory() {
        return category;
    }

    /**
     * Returns a one-sentence summary of the problem.
     */
    public String getSummary() {
        return summary;
    }

    /**
     * Return a description of the exception and the action being
     * performed when the exception occurred.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns a short description of the severity of the problem.
     */
    public String getSeverity() {
        return severity;
    }

    /**
     * Return the cause of the launch exception or null if there
     * is no cause exception.
     */
    public Throwable getCause() {
        return cause;
    }

    /**
     * Returns the causes for this exception.  This method is
     * useful on JRE 1.3 since getCause is not a standard method,
     * and will be removed once netx no longer supports 1.3.
     */
    public Throwable[] getCauses() {
        ArrayList<Throwable> result = new ArrayList<Throwable>();

        Reflect r = new Reflect();
        Throwable cause = this.cause;

        while (cause != null) {
            result.add(cause);
            cause = (Throwable) r.invoke(cause, "getCause");
        }

        return result.toArray(new Throwable[0]);
    }

    /**
     * Print the stack trace and the cause exception (1.3
     * compatible)
     */
    public void printStackTrace(PrintStream stream) {
        super.printStackTrace(stream);

        if (cause != null) {
            stream.println("Caused by: ");
            cause.printStackTrace(stream);
        }
    }

    /**
     * Print the stack trace and the cause exception (1.3
     * compatible)
     */
    public void printStackTrace(PrintWriter stream) {
        super.printStackTrace(stream);

        if (cause != null) {
            stream.println("Caused by: ");
            cause.printStackTrace(stream);
        }
    }

}
