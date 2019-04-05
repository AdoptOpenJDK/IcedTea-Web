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

import java.io.File;
import java.io.IOException;

/**
 * This interface provides a way for the JNLP application to open specific files
 * in the client's system. It asks permission from the user before opening any
 * files.
 *
 * @author <a href="mailto:omajid@redhat.com">Omair Majid</a>
 *
 */
public interface ExtendedService {

    /**
     * Open a file on the client' system and return its contents. The user must
     * grant permission to the application for this to work.
     *
     * @param file the file to open
     * @return the opened file as a {@link FileContents} object
     * @throws IOException on any io problems
     */
    FileContents openFile(File file) throws IOException;

    /**
     * Opens multiple files on the user's sytem and returns their contents as a
     * {@link FileContents} array
     *
     * @param files the files to open
     * @return an array of FileContents objects
     * @throws IOException on any io problems
     */
    FileContents[] openFiles(File[] files) throws IOException;
}
