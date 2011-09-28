/* CachedJarFileCallback.java
   Copyright (C) 2011 Red Hat, Inc.
   Copyright (c) 1997, 2006, Oracle and/or its affiliates. All rights reserved.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 2.

IcedTea is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to
the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
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
exception statement from your version.
*/

package net.sourceforge.jnlp.runtime;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;

import net.sourceforge.jnlp.util.UrlUtils;

import sun.net.www.protocol.jar.URLJarFile;
import sun.net.www.protocol.jar.URLJarFileCallBack;

/**
 * Invoked by URLJarFile to get a JarFile corresponding to a URL.
 *
 * Large parts of this class are based on JarFileFactory and URLJarFile.
 */
final class CachedJarFileCallback implements URLJarFileCallBack {

    private static final CachedJarFileCallback INSTANCE = new CachedJarFileCallback();

    public synchronized static CachedJarFileCallback getInstance() {
        return INSTANCE;
    }

    /* our managed cache */
    private final Map<URL, URL> mapping;

    private CachedJarFileCallback() {
        mapping = new ConcurrentHashMap<URL, URL>();
    }

    protected void addMapping(URL remoteUrl, URL localUrl) {
        mapping.put(remoteUrl, localUrl);
    }

    @Override
    public JarFile retrieve(URL url) throws IOException {
        URL localUrl = mapping.get(url);

        if (localUrl == null) {
            /*
             * If the jar url is not known, treat it as it would be treated in
             * general by URLJarFile.
             */
            return cacheJarFile(url);
        }

        if (UrlUtils.isLocalFile(localUrl)) {
            // if it is known to us, just return the cached file
            JarFile returnFile = new JarFile(localUrl.getPath());
            
            try {
                
                // Blank out the class-path because:
                // 1) Web Start does not support it
                // 2) For the plug-in, we want to cache files from class-path so we do it manually
                returnFile.getManifest().getMainAttributes().putValue("Class-Path", "");

                if (JNLPRuntime.isDebug()) {
                    System.err.println("Class-Path attribute cleared for " + returnFile.getName());
                }

            } catch (NullPointerException npe) {
                // Discard NPE here. Maybe there was no manifest, maybe there were no attributes, etc.
            }

            return returnFile;
        } else {
            // throw new IllegalStateException("a non-local file in cache");
            return null;
        }

    }

    /*
     * This method is a copy of URLJarFile.retrieve() without the callback check.
     */
    private JarFile cacheJarFile(URL url) throws IOException {
        JarFile result = null;

        final int BUF_SIZE = 2048;

        /* get the stream before asserting privileges */
        final InputStream in = url.openConnection().getInputStream();

        try {
            result =
                    AccessController.doPrivileged(new PrivilegedExceptionAction<JarFile>() {
                        @Override
                        public JarFile run() throws IOException {
                            OutputStream out = null;
                            File tmpFile = null;
                            try {
                                tmpFile = File.createTempFile("jar_cache", null);
                                tmpFile.deleteOnExit();
                                out = new FileOutputStream(tmpFile);
                                int read = 0;
                                byte[] buf = new byte[BUF_SIZE];
                                while ((read = in.read(buf)) != -1) {
                                    out.write(buf, 0, read);
                                }
                                out.close();
                                out = null;
                                return new URLJarFile(tmpFile, null);
                            } catch (IOException e) {
                                if (tmpFile != null) {
                                    tmpFile.delete();
                                }
                                throw e;
                            } finally {
                                if (in != null) {
                                    in.close();
                                }
                                if (out != null) {
                                    out.close();
                                }
                            }
                        }
                    });
        } catch (PrivilegedActionException pae) {
            throw (IOException) pae.getException();
        }

        return result;
    }

}
