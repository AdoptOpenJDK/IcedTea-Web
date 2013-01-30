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

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Thrown when a JNLP application, applet, or installer could not
 * be created.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.9 $
 */
public class LaunchException extends Exception {


    public static class LaunchExceptionWithStamp{
        private final LaunchException ex;
        private final Date stamp;

        private LaunchExceptionWithStamp(LaunchException ex) {
            this.ex=ex;
            this.stamp=new Date();
        }

        public LaunchException getEx() {
            return ex;
        }

        public Date getStamp() {
            return stamp;
        }



    }
    private static final List<LaunchExceptionWithStamp> launchExceptionChain = Collections.synchronizedList(new LinkedList<LaunchExceptionWithStamp>());

    private static final long serialVersionUID = 7283827853612357423L;

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
        super(severity + ": " + category + ": " + summary + " "
        	    + (description == null ? "" : description), cause);

        this.file = file;
        this.category = category;
        this.summary = summary;
        this.description = description;
        this.severity = severity;
        saveLaunchException(this);
    }

    /**
     * Creates a LaunchException with a cause.
     */
    public LaunchException(Throwable cause) {
        super(cause);
        saveLaunchException(this);
    }

    /**
     * Creates a LaunchException with a cause and detail message
     */
    public LaunchException(String message, Throwable cause) {
        super(message, cause);
        saveLaunchException(this);
    }

    /**
     * Constructs a LaunchException with the specified detail
     * message.
     *
     * @param message the detail message
     */
    public LaunchException(String message) {
        super(message);
        saveLaunchException(this);
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

    private synchronized void saveLaunchException(LaunchException ex) {
        launchExceptionChain.add(new LaunchExceptionWithStamp(ex));

    }

    public synchronized static List<LaunchExceptionWithStamp> getLaunchExceptionChain() {
        return launchExceptionChain;
    }
    
    

}
