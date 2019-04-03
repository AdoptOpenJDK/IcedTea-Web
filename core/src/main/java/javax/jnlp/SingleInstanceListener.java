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
 * This interface specifies a listener which is notified whenever a new instance
 * of the web start application is launched.
 *
 */
public interface SingleInstanceListener {

    /**
     * This method is called when a new instance of the application is launched.
     * The arguments passed to the new instance are passed into this method.
     *
     * @param arguments the arguments passed to the new instance of the
     *        application
     */
    void newActivation(String[] arguments);

}
