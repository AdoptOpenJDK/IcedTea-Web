// Copyright (C) 2019 Karakun AG
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
//
package net.adoptopenjdk.icedteaweb.launch;

import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.runtime.ApplicationInstance;

import java.net.URL;

/**
 * An interface defining the contract on how to launch an application specified in a JNLP file and
 * managed by a web start application environment.
 */
public interface ApplicationLauncher {
    /**
     * Launches the application specified in the JNLP file at the given URL location.
     *
     * @param location the URL of the JNLP file specifying the application to launch
     * @return a representation of the application described in the JNLP file
     * @throws LaunchException if there was an exception
     */
    ApplicationInstance launch(URL location) throws LaunchException;
}
