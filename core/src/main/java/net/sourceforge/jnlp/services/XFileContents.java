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

package net.sourceforge.jnlp.services;

import java.io.*;
import javax.jnlp.*;

/**
 * File contents.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.6 $
 */
class XFileContents implements FileContents {

    /** the file */
    private File file;

    /**
     * Create a file contents implementation for the file.
     */
    protected XFileContents(File file) {
        // create a safe copy
        this.file = new File(file.getPath());
    }

    /**
     *
     * @throws IOException if an I/O exception occurs.
     */
    public boolean canRead() throws IOException {
        return file.canRead();
    }

    /**
     *
     * @throws IOException if an I/O exception occurs.
     */
    public boolean canWrite() throws IOException {
        return file.canWrite();
    }

    /**
     *
     * @throws IOException if an I/O exception occurs.
     */
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }

    /**
     *
     * @throws IOException if an I/O exception occurs.
     */
    public long getLength() throws IOException {
        return file.length();
    }

    /**
     *
     * @throws IOException if an I/O exception occurs.
     */
    public long getMaxLength() throws IOException {
        return Long.MAX_VALUE;
    }

    /**
     *
     * @throws IOException if an I/O exception occurs.
     */
    public String getName() throws IOException {
        return file.getName();
    }

    /**
     *
     * @throws IOException if an I/O exception occurs.
     */
    public OutputStream getOutputStream(boolean overwrite) throws IOException {
        // file.getPath compatible with pre-1.4 JREs
        return new FileOutputStream(file.getPath(), !overwrite);
    }

    /**
     *
     * @throws IOException if an I/O exception occurs.
     */
    public JNLPRandomAccessFile getRandomAccessFile(String mode) throws IOException {
        return new XJNLPRandomAccessFile(file, mode);
    }

    /**
     *
     * @throws IOException if an I/O exception occurs.
     */
    public long setMaxLength(long maxlength) throws IOException {
        return maxlength;
    }

}
