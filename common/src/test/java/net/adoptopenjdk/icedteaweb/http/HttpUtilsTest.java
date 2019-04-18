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
package net.adoptopenjdk.icedteaweb.http;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

public class HttpUtilsTest {

    @Test
    public void shouldNotThrowExceptionFromInputStream() {
        HttpUtils.consumeAndCloseConnectionSilently(httpConnectionWithBrokenInputStream());
    }

    @Test
    public void shouldNotThrowExceptionFromHttpConnection() {
        HttpUtils.consumeAndCloseConnectionSilently(brokenHttpConnection());
    }

    @Test(expected = IOException.class)
    public void shouldThrowExceptionFromInputStream() throws IOException {
        HttpUtils.consumeAndCloseConnection(httpConnectionWithBrokenInputStream());
    }

    @Test(expected = IOException.class)
    public void shouldThrowExceptionFromHttpConnection() throws IOException {
        HttpUtils.consumeAndCloseConnection(brokenHttpConnection());
    }

    //
    // Helper methods
    //

    private CloseableHttpConnection brokenHttpConnection() {
        return new CloseableHttpConnection(getHttpURLConnection(() -> {
            throw new IOException("just for testing");
        }));
    }

    private CloseableHttpConnection httpConnectionWithBrokenInputStream() {
        final InputStream in = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("just for testing");
            }
        };

        return new CloseableHttpConnection(getHttpURLConnection(() -> in));
    }

    private HttpURLConnection getHttpURLConnection(InputStreamSupplier inputStreamSupplier) {
        return new HttpURLConnection(null) {

            @Override
            public InputStream getInputStream() throws IOException {
                return inputStreamSupplier.get();
            }

            @Override
            public void disconnect() {
                Assert.fail();
            }

            @Override
            public boolean usingProxy() {
                Assert.fail();
                return false;
            }

            @Override
            public void connect() {
                Assert.fail();
            }
        };
    }

    private interface InputStreamSupplier {
        InputStream get() throws IOException;
    }
}
