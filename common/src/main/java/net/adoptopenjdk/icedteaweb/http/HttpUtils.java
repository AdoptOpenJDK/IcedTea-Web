/* 
 Copyright (C) 2011 Red Hat, Inc.

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
package net.adoptopenjdk.icedteaweb.http;

import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class HttpUtils {

    private static final Logger LOG = LoggerFactory.getLogger(HttpUtils.class);

    private HttpUtils() {
        // do not instantiate.
    }

    /**
     * Ensure a CloseableHttpConnection is fully read, required for correct behavior.
     * Any thrown IOException is consumed and logged
     * @param c the connection to be closed silently
     */
    public static void consumeAndCloseConnectionSilently(final CloseableConnection c) {
        try {
            consumeAndCloseConnection(c);
        } catch (final IOException ex) {
            LOG.warn("Following exception should be harmless, but may help in finding root cause.", ex);
        }
    }

    /**
     * Ensure a CloseableHttpConnection is fully read, required for correct behavior.
     * 
     * @param c connection to be closed
     * @throws IOException if an I/O exception occurs.
     */
    public static void consumeAndCloseConnection(final CloseableConnection c) throws IOException {
        try (final InputStream in = c.getInputStream()) {
            if (c instanceof CloseableHttpConnection) {
                final byte[] throwAwayBuffer = new byte[256];
                while (in.read(throwAwayBuffer) > 0) {
                    /* ignore contents */
                }
            }
        }
    }
}
