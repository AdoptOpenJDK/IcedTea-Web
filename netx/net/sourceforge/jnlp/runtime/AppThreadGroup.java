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

package net.sourceforge.jnlp.runtime;

/**
 * Thread group for a JNLP application.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.5 $
 */
public class AppThreadGroup extends ThreadGroup {

    /** the app */
    private ApplicationInstance app = null;

    /**
     * Creates new JavaAppThreadGroup
     *
     * @param name of the App
     */
    public AppThreadGroup(ThreadGroup parent, String name) {
        super(parent, name);
    }

    /**
     * Sets the JNLP app this group is for; can only be called once.
     */
    public void setApplication(ApplicationInstance app) {
        if (this.app != null)
            throw new IllegalStateException("Application can only be set once");

        this.app = app;
    }

    /**
     * Returns the JNLP app for this thread group.
     */
    public ApplicationInstance getApplication() {
        return app;
    }

    /**
     * Handle uncaught exceptions for the app.
     */
    public void uncaughtException(Thread t, Throwable e) {
        super.uncaughtException(t, e);
    }

}
