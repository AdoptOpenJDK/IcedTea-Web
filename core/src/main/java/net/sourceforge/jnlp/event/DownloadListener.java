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

/**
 * The listener that is notified of the state of resources being
 * downloaded by a ResourceTracker.  Events may be delivered on a
 * background thread, and the event methods should complete
 * quickly so that they do not slow down other downloading in
 * progress by tying up a thread.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.3 $
 */
public interface DownloadListener extends EventListener {

    /**
     * Called when a resource is checked for being up-to-date.
     * @param downloadEvent information about started update
     */
    public void updateStarted(DownloadEvent downloadEvent);

    /**
     * Called when a download starts.
     * @param downloadEvent  information about started download
     */
    public void downloadStarted(DownloadEvent downloadEvent);

    /**
     * Called when a download completed or there was an error.
     * @param downloadEvent  information about finished download
     */
    public void downloadCompleted(DownloadEvent downloadEvent);

}
