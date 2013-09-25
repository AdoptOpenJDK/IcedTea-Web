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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.ServerLauncher;
import net.sourceforge.jnlp.util.logging.OutputController;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class HttpUtilsTest {
    
    private static PrintStream[]  backedUpStream = new PrintStream[4];
    private static ByteArrayOutputStream currentErrorStream;
     
      @BeforeClass
    //keeping silent outputs from launched jvm
    public static void redirectErr() throws IOException {
        for (int i = 0; i < backedUpStream.length; i++) {
            if (backedUpStream[i] == null) {
                switch (i) {
                    case 0:
                        backedUpStream[i] = System.out;
                        break;
                    case 1:
                        backedUpStream[i] = System.err;
                        break;
                    case 2:
                        backedUpStream[i] = OutputController.getLogger().getOut();
                        break;
                    case 3:
                        backedUpStream[i] = OutputController.getLogger().getErr();
                        break;
                }

            }

        }
        currentErrorStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(currentErrorStream));
        System.setErr(new PrintStream(currentErrorStream));
        OutputController.getLogger().setOut(new PrintStream(currentErrorStream));
        OutputController.getLogger().setErr(new PrintStream(currentErrorStream));


    }

    @AfterClass
    public static void redirectErrBack() throws IOException {
        ServerAccess.logErrorReprint(currentErrorStream.toString("utf-8"));
        System.setOut(backedUpStream[0]);
        System.setErr(backedUpStream[1]);
        OutputController.getLogger().setOut(backedUpStream[2]);
        OutputController.getLogger().setErr(backedUpStream[3]);


    }

    @Test
    public void consumeAndCloseConnectionSilentlyTest() throws IOException {
        redirectErr();
        try{
        Exception exception = null;
        try {
            HttpUtils.consumeAndCloseConnectionSilently(new HttpURLConnection(null) {
                @Override
                public void disconnect() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public boolean usingProxy() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void connect() throws IOException {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            });
        } catch (Exception ex) {
            ServerAccess.logException(ex);
            exception = ex;
        }
        Assert.assertNull("no exception expected - was" + exception, exception);



        try {
            HttpUtils.consumeAndCloseConnectionSilently(new HttpURLConnection(new URL("http://localhost/blahblah")) {
                @Override
                public void disconnect() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public boolean usingProxy() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void connect() throws IOException {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            });
        } catch (Exception ex) {
            ServerAccess.logException(ex);
            exception = ex;
        }
        Assert.assertNull("no exception expected - was" + exception, exception);
        
        ServerLauncher serverLauncher =ServerAccess.getIndependentInstance(System.getProperty("user.dir"), ServerAccess.findFreePort());
        try{
                try {
            HttpUtils.consumeAndCloseConnectionSilently(new HttpURLConnection(serverLauncher.getUrl("definitelyNotExisitnfFileInHappyMemoryOfAdam")) { //:)
                @Override
                public void disconnect() {
                   
                }

                @Override
                public boolean usingProxy() {
                    return false;
                }

                @Override
                public void connect() throws IOException {
                   
                }
            });
        } catch (Exception ex) {
            ServerAccess.logException(ex);
            exception = ex;
        }
        Assert.assertNull("no exception expected - was" + exception, exception);
        }finally{
            try{
            serverLauncher.stop();
            }catch(Exception ex){
                ServerAccess.logException(ex);
            }
        }
        }finally{
            redirectErrBack();
        }
    }
    
    @Test
    public void consumeAndCloseConnectionTest() throws IOException {
        redirectErr();
        try{
        Exception exception = null;
        try {
            HttpUtils.consumeAndCloseConnection(new HttpURLConnection(null) {
                @Override
                public void disconnect() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public boolean usingProxy() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void connect() throws IOException {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            });
        } catch (Exception ex) {
            ServerAccess.logException(ex);
            exception = ex;
        }
        Assert.assertNotNull("exception expected - wasnt" + exception, exception);



        try {
            HttpUtils.consumeAndCloseConnection(new HttpURLConnection(new URL("http://localhost/blahblah")) {
                @Override
                public void disconnect() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public boolean usingProxy() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void connect() throws IOException {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            });
        } catch (Exception ex) {
            ServerAccess.logException(ex);
            exception = ex;
        }
        Assert.assertNotNull("exception expected - wasnt" + exception, exception);
        
        ServerLauncher s =ServerAccess.getIndependentInstance(System.getProperty("user.dir"), ServerAccess.findFreePort());
        try{
                try {
            HttpUtils.consumeAndCloseConnection(new HttpURLConnection(s.getUrl("blahblahblah")) {
                @Override
                public void disconnect() {
                   
                }

                @Override
                public boolean usingProxy() {
                    return false;
                }

                @Override
                public void connect() throws IOException {
                   
                }
            });
        } catch (Exception ex) {
            ServerAccess.logException(ex);
            exception = ex;
        }
        Assert.assertNotNull(" exception expected - wasnt" + exception, exception);
        }finally{
            try{
            s.stop();
            }catch(Exception ex){
                ServerAccess.logException(ex);
            }
        }
        }finally{
            redirectErrBack();
        }
    }
}
