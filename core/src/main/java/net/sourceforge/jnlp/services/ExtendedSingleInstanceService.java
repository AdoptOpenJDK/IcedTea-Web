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

package net.sourceforge.jnlp.services;

import javax.jnlp.SingleInstanceService;

import net.sourceforge.jnlp.JNLPFile;

/**
 * Extends SingleInstanceService to provide a few additional methods that are
 * required to initialize SingleInstanceService and check things. These methods
 * are not exposed publicly
 *
 * @author <a href="mailto:omajid@redhat.com">Omair Majid</a>
 *
 */
interface ExtendedSingleInstanceService extends SingleInstanceService {

    /**
     * Check if the instance identified by this jnlp file is already running
     *
     * @param jnlpFile The JNLPFile that specifies the application
     *
     * @throws InstanceExistsException if an instance of this application
     *         already exists
     *
     */
    void checkSingleInstanceRunning(JNLPFile jnlpFile);

    /**
     * Start a single instance service based on the current application
     */
    void initializeSingleInstance();

}
