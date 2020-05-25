// Copyright (C) 2009 Red Hat, Inc.
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

package javax.jnlp;

import java.io.File;
import java.io.IOException;

/**
 * ExtendedService provides additional support to the current JNLP API,
 * to allow applications to open a specific file or files in the client's file system.
 *
 * @since 1.5
 */
public interface ExtendedService {

    /**
     * Allows the application to open the specified file, even if the application is running in the
     * untrusted execution environment. If the application would not otherwise have permission to
     * access the file, the JNLP CLient should warn user of the potential security risk.
     * The contents of the file is returned as a FileContents object.
     *
     * @param file the file object
     * @return A FileContents object with information about the opened file
     * @throws IOException if there is any I/O error
     */
    FileContents openFile(File file) throws IOException;

    /**
     * Allows the application to open the specified files, even if the application is running in the
     * untrusted execution environment. If the application would not otherwise have permission to
     * access the files, the JNLP CLient should warn user of the potential security risk.
     * The contents of each file is returned as a FileContents object in the FileContents array.
     *
     * @param files the array of files
     * @return A FileContents[] object with information about each opened file
     * @throws IOException if there is any I/O error
     */
    FileContents[] openFiles(File[] files) throws IOException;
}
