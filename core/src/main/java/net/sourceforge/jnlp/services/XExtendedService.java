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

import java.io.File;
import java.io.IOException;

import javax.jnlp.ExtendedService;
import javax.jnlp.FileContents;

import net.sourceforge.jnlp.security.SecurityDialogs.AccessType;

/**
 * Implementation of ExtendedService
 *
 * @author <a href="mailto:omajid@redhat.com">Omair Majid</a>
 *
 */
public class XExtendedService implements ExtendedService {

    public FileContents openFile(File file) throws IOException {

        File secureFile = new File(file.getPath());

        /* FIXME: this opens a file with read/write mode, not just read or write */
        if (ServiceUtil.checkAccess(AccessType.READ_FILE, new Object[] { secureFile.getAbsolutePath() })) {
            return (FileContents) ServiceUtil.createPrivilegedProxy(FileContents.class,
                    new XFileContents(secureFile));
        } else {
            return null;
        }

    }

    public FileContents[] openFiles(File[] files) throws IOException {
        FileContents[] contents = new FileContents[files.length];
        for (int i = 0; i < files.length; i++) {
            contents[i] = openFile(files[i]);
        }
        return contents;
    }

}
