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

package net.sourceforge.jnlp.cache;

import java.net.*;
import javax.jnlp.*;

import net.sourceforge.jnlp.runtime.*;

/**
 * A DownloadIndicator creates DownloadServiceListeners that are
 * notified of resources being transferred and their progress.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.8 $
 */
public interface DownloadIndicator {

    /**
     * Return a download service listener that displays the progress
     * of downloading resources. Update messages may be reported
     * for URLs that are not included initially.
     * <p>
     * Progress messages are sent as if the DownloadServiceListener
     * were listening to a DownloadService request. The listener
     * will receive progress messages from time to time during the
     * download.
     * </p>
     *
     * @param app JNLP application downloading the files, or null if not applicable
     * @param downloadName name identifying the download to the user
     * @param resources initial urls to display, empty if none known at start
     * @return dedicated listener
     */
    public DownloadServiceListener getListener(ApplicationInstance app,
                                               String downloadName,
                                               URL resources[]);

    /**
     * Indicates that a download service listener that was obtained
     * from the getDownloadListener method will no longer be used.
     * This method can be used to ensure that progress dialogs are
     * properly removed once a particular download is finished.
     *
     * @param listener the listener that is no longer in use
     */
    public void disposeListener(DownloadServiceListener listener);

    /**
     * Return the desired time in milliseconds between updates.
     * Updates are not guarenteed to occur based on this value; for
     * example, they may occur based on the download percent or some
     * other factor.
     *
     * @return rate in milliseconds, must be &gt;= 0
     */
    public int getUpdateRate();

    /**
     * Return a time in milliseconds to wait for a download to
     * complete before obtaining a listener for the download.  This
     * value can be used to skip lengthy operations, such as
     * initializing a GUI, for downloads that complete quickly.  The
     * getListener method is not called if the download completes
     * in less time than the returned delay.
     *
     * @return delay in milliseconds, must be &gt;= 0
     */
    public int getInitialDelay();

}
