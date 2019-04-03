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

import java.net.*;
import java.util.*;

import net.sourceforge.jnlp.cache.*;

/**
 * This event is sent during the launch of an
 * application.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.3 $
 */
public class DownloadEvent extends EventObject {

    /** the tracker */
    final transient private ResourceTracker tracker;

    /** the resource */
    final transient private Resource resource;

    /**
     * Creates a launch event for the specified application
     * instance.
     *
     * @param source the resource tracker
     * @param resource the resource
     */
    public DownloadEvent(ResourceTracker source, Resource resource) {
        super(source);

        this.tracker = source;
        this.resource = resource;
    }

    /**
     * @return the tracker that owns the resource.
     */
    public ResourceTracker getTracker() {
        return tracker;
    }

    /**
     * @return the location of the resource being downloaded.
     */
    public URL getResourceLocation() {
        return resource.getLocation();
    }

}
