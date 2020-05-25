// Copyright (C) 2002 Jon A. Maxwell (JAM)
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

package net.adoptopenjdk.icedteaweb.resources;

import net.adoptopenjdk.icedteaweb.resources.cache.ResourceInfo;

/**
 * A policy that determines when a resource should be checked for
 * an updated version.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.3 $
 */
public class UpdatePolicy {

    public static UpdatePolicy ALWAYS = new UpdatePolicy(0);
    public static UpdatePolicy FORCE = new UpdatePolicy(Long.MIN_VALUE);
    public static UpdatePolicy NEVER = new UpdatePolicy(Long.MAX_VALUE);

    private final long timeDiff;

    /**
     * Create an update policy that only checks a file for being
     * updated if it has not been checked for longer than the
     * specified time.
     *
     * @param timeDiff how long in ms until update needed
     */
    public UpdatePolicy(long timeDiff) {
        this.timeDiff = timeDiff;
    }

    /**
     * @return whether the resource should be checked for being
     * up-to-date.
     * @param entry entry which should be cared
     */
    public boolean shouldUpdate(ResourceInfo entry) {
        long updated = entry.getDownloadedAt();
        long current = System.currentTimeMillis();

        return current - updated >= timeDiff;
    }

}
