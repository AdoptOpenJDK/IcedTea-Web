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

//in jdk6 java.util.jar.JarFile is not Closeable - fixing
//overwritening  class can add duplicate occurence of interface so this should be perfectly safe
public class JarFile extends java.util.jar.JarFile implements Closeable{

    public JarFile(String name) throws IOException {
       super(name);
       verifyZipHeader(new File(name));
    }

    /**
     */
    public JarFile(String name, boolean verify) throws IOException {
        super(name, verify);
        verifyZipHeader(new File(name));
    }

    /**
     */
    public JarFile(File file) throws IOException {
        super(file);
        verifyZipHeader(file);
    }

    /**
     */
    public JarFile(File file, boolean verify) throws IOException {
        super(file, verify);
        verifyZipHeader(file);
    }

    /*
     */
    public JarFile(File file, boolean verify, int mode) throws IOException {
        super(file, verify, mode);
         verifyZipHeader(file);
    }
    
    
    
    
    /**
     * According to specification -
     * http://www.pkware.com/documents/casestudies/APPNOTE.TXT or just google
     * around zip header all entries in zip-compressed must start with well
     * known "PK" which is defined as hexa x50 x4b x03 x04, which in decimal are
     * 80 75 3 4.
     * 
     * Note - this is not file-header, it is item-header.
     *
     * Actually most of compressing formats have some n-bytes header se eg:
     * http://www.gzip.org/zlib/rfc-gzip.html#header-trailer for ID1 and ID2 so
     * in case that some differently compressed jars will come to play, this is
     * the palce where to fix it.
     *
     */
    private static final byte[] ZIP_LOCAL_FILE_HEADER_SIGNATURE = new byte[]{80, 75, 3, 4};

    /**
     * This method is checking first four bytes of jar-file against
     * ZIP_LOCAL_FILE_HEADER_SIGNATURE
     *
     * Although zip specification allows to skip all corrupted entries, it is
     * not safe for jars. If first four bytes of file are not zip
     * ZIP_LOCAL_FILE_HEADER_SIGNATURE then exception is thrown
     * 
     * As noted, ZIP_LOCAL_FILE_HEADER_SIGNATURE is not ile-header, but is item-header.
     * Possible attack is using the fact that entries without header are considered
     * corrupted and so can be ignoered. However, for other they can have some meaning.
     * 
     * So for our purposes we must insists on first record to be valid.
     *
     * @param file
     * @throws IOException
     * @throws InvalidJarHeaderException
     */
    public static void verifyZipHeader(File file) throws IOException {
        if (!JNLPRuntime.isIgnoreHeaders()) {
            InputStream s = new FileInputStream(file);
            try {
                byte[] buffer = new byte[ZIP_LOCAL_FILE_HEADER_SIGNATURE.length];
                /*
                 * for case that new byte[] will accidently initialize same
                 * sequence as zip header and during the read the buffer will not be filled
                 */                
                for (int i = 0; i < buffer.length; i++) {
                    buffer[i] = 0;
                }
                int toRead = ZIP_LOCAL_FILE_HEADER_SIGNATURE.length;
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
                    if (buffer[i] != ZIP_LOCAL_FILE_HEADER_SIGNATURE[i]) {
                        throw new InvalidJarHeaderException("Jar " + file.getName() + " do not heave valid header. You can skip this check by -Xignoreheaders");
                    }
                }
            } finally {
                s.close();
            }
        }
    }
}
