// Copyright (C) 2009 Red Hat, Inc.
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

package javax.jnlp;

/**
 * The SingleInstanceService provides a way to ensure that only one instance of
 * the application is ever running - singleton behavior at the application
 * level.
 *
 */
public interface SingleInstanceService {

    /**
     * Adds the specified SingleInstanceListener to the notification list. This
     * listener is notified when a new instance of the application is started.
     *
     *
     * @param listener the single instance listener to be added. No action is
     *        performed if it is null.
     */
    void addSingleInstanceListener(SingleInstanceListener listener);

    /**
     * Removes the specified SingleInstanceListener from the notification list.
     * This listener will not be notified if a new instance of the application
     * is started.
     *
     * @param listener the single instance listener to be removed. No action is
     *        performed if it is null or not in the notification list.
     */
    void removeSingleInstanceListener(SingleInstanceListener listener);
}
