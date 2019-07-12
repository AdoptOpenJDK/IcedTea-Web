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

import net.sourceforge.jnlp.JNLPFile;

import java.util.List;

/**
 * An interface defining the contract on how to launch a new JVM including the web start application environment.
 *
 * The main reason for doing so is changing the JVM version or passing different/extra arguments.
 */
public interface JvmLauncher {

    /**
     * Launches the web start application environment with the given JNLP file and arguments.
     *
     * @param jnlpFile the JNLP file to launch in the new JVM.
     * @param args arguments to pass to the new JVM
     * @throws Exception if there was an exception
     */
    void launchExternal(JNLPFile jnlpFile, List<String> args) throws Exception;
}
