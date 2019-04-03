/*
 Copyright (C) 2012 Red Hat, Inc.

 This file is part of IcedTea.

 IcedTea is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2, or (at your option)
 any later version.

 IcedTea is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with IcedTea; see the file COPYING.  If not, write to the
 Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 02110-1301 USA.

 Linking this library statically or dynamically with other modules is
 making a combined work based on this library.  Thus, the terms and
 conditions of the GNU General Public License cover the whole
 combination.

 As a special exception, the copyright holders of this library give you
 permission to link this library with independent modules to produce an
 executable, regardless of the license terms of these independent
 modules, and to copy and distribute the resulting executable under
 terms of your choice, provided that you also meet, for each linked
 independent module, the terms and conditions of the license of that
 module.  An independent module is a module which is not derived from
 or based on this library.  If you modify this library, you may extend
 this exception to your version of the library, but you are not
 obligated to do so.  If you do not wish to do so, delete this
 exception statement from your version. */
package net.sourceforge.jnlp.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

/**
 * A wrapper over {@link java.util.jar.JarFile} that verifies zip headers to
 * protect against GIFAR attacks.
 *
 * @see <a href="http://en.wikipedia.org/wiki/Gifar">Gifar</a>
 */
public class JarFile extends java.util.jar.JarFile implements Closeable {

    public JarFile(String name) throws IOException {
        super(name);
        verifyZipHeader(new File(name));
    }

    public JarFile(String name, boolean verify) throws IOException {
        super(name, verify);
        verifyZipHeader(new File(name));
    }

    public JarFile(File file) throws IOException {
        super(file);
        verifyZipHeader(file);
    }

    public JarFile(File file, boolean verify) throws IOException {
        super(file, verify);
        verifyZipHeader(file);
    }

    public JarFile(File file, boolean verify, int mode) throws IOException {
        super(file, verify, mode);
        verifyZipHeader(file);
    }

    /**
     * The ZIP specification requires that the zip header for all entries in a
     * zip-compressed archive must start with a well known "PK" which is
     * defined as hex x50 x4b x03 x04.
     * <p>
     * Note - this is not file-header, it is item-header.
     * <p>
     * Actually most of compressing formats have some n-bytes headers. Eg:
     * http://www.gzip.org/zlib/rfc-gzip.html#header-trailer for ID1 and ID2 so
     * in case that some differently compressed jars will come to play, this is
     * the place where to fix it.
     *
     * @see <a href="http://www.pkware.com/documents/casestudies/APPNOTE.TXT">ZIP Specification</a>
     */
    private static final byte[] ZIP_ENTRY_HEADER_SIGNATURE = new byte[] {0x50, 0x4b, 0x03, 0x04};

    /**
     * Verify the header for the zip entry.
     * <p>
     * Although zip specification allows to skip all corrupted entries, it is
     * not safe for jars since it allows a different format to fake itself as
     * a Jar.
     */
    private void verifyZipHeader(File file) throws IOException {
        if (!JNLPRuntime.isIgnoreHeaders()) {
            InputStream s = new FileInputStream(file);

            /*
             * Theoretically, a valid ZIP file can begin with anything. We
             * ensure it begins with a valid entry header to confirm it only
             * contains zip entries.
             */

            try {
                byte[] buffer = new byte[ZIP_ENTRY_HEADER_SIGNATURE.length];
                /*
                 * for case that new byte[] will accidently initialize same
                 * sequence as zip header and during the read the buffer will not be filled
                 */
                for (int i = 0; i < buffer.length; i++) {
                    buffer[i] = 0;
                }
                int toRead = ZIP_ENTRY_HEADER_SIGNATURE.length;
                int readSoFar = 0;
                int n = 0;
                /*
                 * this is used instead of s.read(buffer) for case of block and
                 * so returned not-fully-filled dbuffer
                 */ 
                while ((n = s.read(buffer, readSoFar, buffer.length - readSoFar)) != -1) {
                    readSoFar += n;
                    if (readSoFar == toRead) {
                        break;
                    }
                }
                for (int i = 0; i < buffer.length; i++) {
                    if (buffer[i] != ZIP_ENTRY_HEADER_SIGNATURE[i]) {
                        throw new InvalidJarHeaderException("Jar " + file.getName() + " do not heave valid header. You can skip this check by -Xignoreheaders");
                    }
                }
            } finally {
                s.close();
            }
        }
    }
}
