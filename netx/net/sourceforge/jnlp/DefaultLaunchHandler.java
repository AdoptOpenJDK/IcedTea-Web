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

import net.sourceforge.jnlp.runtime.*;

/**
 * This default implementation shows prints the exception to
 * stdout and if not in headless mode displays the exception in a
 * dialog.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.1 $
 */
public class DefaultLaunchHandler implements LaunchHandler {

    /**
     * Called when the application could not be launched due to a
     * fatal error, such as the inability to find the main class
     * or non-parseable XML.
     */
    public void launchError(LaunchException exception) {
        printMessage(exception);
    }

    /**
     * Called when launching the application can not be launched
     * due to an error that is not fatal.  For example a JNLP file
     * that is not strictly correct yet does not necessarily
     * prohibit the system from attempting to launch the
     * application.
     *
     * @return true if the launch should continue, false to abort
     */
    public boolean launchWarning(LaunchException warning) {
        printMessage(warning);
        return true;
    }

    /**
     * Called when a security validation error occurs while
     * launching the application.
     *
     * @return true to allow the application to continue, false to stop it.
     */
    public boolean validationError(LaunchException security) {
        printMessage(security);
        return true;
    }

    /**
     * Called when an application, applet, or installer has been
     * launched successfully (the main method or applet start method
     * returned normally).
     *
     * @param application the launched application instance
     */
    public void launchCompleted(ApplicationInstance application) {
        //
    }

    /**
     * Print a message to stdout.
     */
    protected static void printMessage(LaunchException ex) {
        StringBuffer result = new StringBuffer();
        result.append("netx: ");
        result.append(ex.getCategory());
        if (ex.getSummary() != null) {
            result.append(": ");
            result.append(ex.getSummary());
        }

        if (JNLPRuntime.isDebug()) {
            if (ex.getCause() != null)
                ex.getCause().printStackTrace();
            else
                ex.printStackTrace();
        }

        Throwable causes[] = ex.getCauses();

        for (int i = 0; i < causes.length; i++) {
            result.append(" (");
            result.append(causes[i].getClass().getName());
            result.append(" ");
            result.append(causes[i].getMessage());
            result.append(")");
        }
    }

    /**
     * Do nothing on when initializing
     */
    @Override
    public void launchInitialized(JNLPFile file) {
        // do nothing
    }

    /**
     * Do nothing when starting
     */
    @Override
    public void launchStarting(ApplicationInstance application) {
        // do nothing
    }

}
