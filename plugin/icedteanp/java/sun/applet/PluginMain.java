/* VoidPluginCallRequest -- represent Java-to-JavaScript requests
   Copyright (C) 2008  Red Hat

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

/*
 * Copyright 1999-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package sun.applet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.PasswordAuthentication;
import java.net.ProxySelector;
import java.util.Enumeration;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import net.sourceforge.jnlp.security.VariableX509TrustManager;

/**
 * The main entry point into PluginAppletViewer.
 */
public class PluginMain
{

    // the files where stdout/stderr are sent to
    public static final String PLUGIN_STDERR_FILE = System.getProperty("user.home") + "/.icedteaplugin/java.stderr";
    public static final String PLUGIN_STDOUT_FILE = System.getProperty("user.home") + "/.icedteaplugin/java.stdout";

	final boolean redirectStreams = System.getenv().containsKey("ICEDTEAPLUGIN_DEBUG");
	static PluginStreamHandler streamHandler;
	
    // This is used in init().	Getting rid of this is desirable but depends
    // on whether the property that uses it is necessary/standard.
    public static final String theVersion = System.getProperty("java.version");
    
    private PluginAppletSecurityContext securityContext;

    /**
     * The main entry point into AppletViewer.
     */
    public static void main(String args[])
	throws IOException
    {
        if (args.length != 2 || !(new File(args[0]).exists()) || !(new File(args[1]).exists())) {
            System.err.println("Invalid pipe names provided. Refusing to proceed.");
            System.exit(1);
        }

    	try {
    		PluginMain pm = new PluginMain(args[0], args[1]);
    	} catch (Exception e) {
    		e.printStackTrace();
    		System.err.println("Something very bad happened. I don't know what to do, so I am going to exit :(");
    		System.exit(1);
    	}
    }

    public PluginMain(String inPipe, String outPipe) {
    	
    	try {
    		File errFile = new File(PLUGIN_STDERR_FILE);
    		File outFile = new File(PLUGIN_STDOUT_FILE);

    		System.setErr(new TeeOutputStream(new FileOutputStream(errFile), System.err));
    		System.setOut(new TeeOutputStream(new FileOutputStream(outFile), System.out));
    	} catch (Exception e) {
    		PluginDebug.debug("Unable to redirect streams");
    		e.printStackTrace();
    	}

    	connect(inPipe, outPipe);

    	securityContext = new PluginAppletSecurityContext(0);
    	securityContext.prePopulateLCClasses();
    	securityContext.setStreamhandler(streamHandler);
    	AppletSecurityContextManager.addContext(0, securityContext);

		PluginAppletViewer.setStreamhandler(streamHandler);
		PluginAppletViewer.setPluginCallRequestFactory(new PluginCallRequestFactory());

    	init();

		// Streams set. Start processing.
		streamHandler.startProcessing();
    }

	public void connect(String inPipe, String outPipe) {
		try {
			streamHandler = new PluginStreamHandler(new FileInputStream(inPipe), new FileOutputStream(outPipe));
	    	PluginDebug.debug("Streams initialized");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private static void init() {
		Properties avProps = new Properties();

		// ADD OTHER RANDOM PROPERTIES
		// XXX 5/18 need to revisit why these are here, is there some
		// standard for what is available?

		// Standard browser properties
		avProps.put("browser", "sun.applet.AppletViewer");
		avProps.put("browser.version", "1.06");
		avProps.put("browser.vendor", "Sun Microsystems Inc.");
		avProps.put("http.agent", "Java(tm) 2 SDK, Standard Edition v" + theVersion);

		// Define which packages can be extended by applets
		// XXX 5/19 probably not needed, not checked in AppletSecurity
		avProps.put("package.restrict.definition.java", "true");
		avProps.put("package.restrict.definition.sun", "true");

		// Define which properties can be read by applets.
		// A property named by "key" can be read only when its twin
		// property "key.applet" is true.  The following ten properties
		// are open by default.	 Any other property can be explicitly
		// opened up by the browser user by calling appletviewer with
		// -J-Dkey.applet=true
		avProps.put("java.version.applet", "true");
		avProps.put("java.vendor.applet", "true");
		avProps.put("java.vendor.url.applet", "true");
		avProps.put("java.class.version.applet", "true");
		avProps.put("os.name.applet", "true");
		avProps.put("os.version.applet", "true");
		avProps.put("os.arch.applet", "true");
		avProps.put("file.separator.applet", "true");
		avProps.put("path.separator.applet", "true");
		avProps.put("line.separator.applet", "true");
		
		avProps.put("javaplugin.nodotversion", "160_17");
		avProps.put("javaplugin.version", "1.6.0_17");
		avProps.put("javaplugin.vm.options", "");

		// Read in the System properties.  If something is going to be
		// over-written, warn about it.
		Properties sysProps = System.getProperties();
		for (Enumeration e = sysProps.propertyNames(); e.hasMoreElements(); ) {
			String key = (String) e.nextElement();
			String val = (String) sysProps.getProperty(key);
			avProps.setProperty(key, val);
		}

		// INSTALL THE PROPERTY LIST
		System.setProperties(avProps);


		try {
		    SSLSocketFactory sslSocketFactory;
		    SSLContext context = SSLContext.getInstance("SSL");
		    TrustManager[] trust = new TrustManager[] { VariableX509TrustManager.getInstance() };
		    context.init(null, trust, null);
		    sslSocketFactory = context.getSocketFactory();
		    
		    HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory);
		} catch (Exception e) {
		    System.err.println("Unable to set SSLSocketfactory (may _prevent_ access to sites that should be trusted)! Continuing anyway...");
		    e.printStackTrace();
		}

		// plug in a custom authenticator and proxy selector
        Authenticator.setDefault(new CustomAuthenticator());
        ProxySelector.setDefault(new PluginProxySelector());
        
        CookieManager ckManager = new PluginCookieManager();
        CookieHandler.setDefault(ckManager);
	}

    static boolean messageAvailable() {
    	return streamHandler.messageAvailable();
    }

    static String getMessage() {
    	return streamHandler.getMessage();
    }
    
    static class CustomAuthenticator extends Authenticator {
        
        public PasswordAuthentication getPasswordAuthentication() {

            // No security check is required here, because the only way to 
            // set parameters for which auth info is needed 
            // (Authenticator:requestPasswordAuthentication()), has a security 
            // check

            String type = this.getRequestorType() == RequestorType.PROXY ? "proxy" : "web"; 

            // request auth info from user
            PasswordAuthenticationDialog pwDialog = new PasswordAuthenticationDialog();
            PasswordAuthentication auth = pwDialog.askUser(this.getRequestingHost(), this.getRequestingPort(), this.getRequestingPrompt(), type);
            
            // send it along
            return auth;
        }
    }

    /**
     * Behaves like the 'tee' command, sends output to both actual std stream and a
     * file
     */
    class TeeOutputStream extends PrintStream {

        // Everthing written to TeeOutputStream is written to this file
        PrintStream logFile;

        public TeeOutputStream(FileOutputStream fileOutputStream,
                PrintStream stdStream) {
            super(stdStream);
            logFile = new PrintStream(fileOutputStream);
        }

        @Override
        public boolean checkError() {
            boolean thisError = super.checkError();
            boolean fileError = logFile.checkError();

            return thisError || fileError;
        }

        @Override
        public void close() {
            logFile.close();
            super.close();
        }

        @Override
        public void flush() {
            logFile.flush();
            super.flush();
        }

        /*
         * The big ones: these do the actual writing
         */

        @Override
        public void write(byte[] buf, int off, int len) {
            logFile.write(buf, off, len);

            if (!redirectStreams)
                super.write(buf, off, len);
        }

        @Override
        public void write(int b) {
            logFile.write(b);
            
            if (!redirectStreams)
                super.write(b);
        }
    }
    
}
