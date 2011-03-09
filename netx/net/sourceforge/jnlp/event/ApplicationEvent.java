// Copyright (C) 2002 Jon A. Maxwell (JAM)
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

package net.sourceforge.jnlp.event;

import java.util.*;

import net.sourceforge.jnlp.runtime.*;

/**
 * This event is sent when an application is terminated.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.5 $
 */
public class ApplicationEvent extends EventObject {

    /** the application instance */
    transient private ApplicationInstance application;

    /**
     * Creates a launch event for the specified application
     * instance.
     *
     * @param source the application instance
     */
    public ApplicationEvent(ApplicationInstance source) {
        super(source);

        this.application = source;
    }

    /**
     * Returns the application instance.
     */
    public ApplicationInstance getApplication() {
        return application;
    }

}
