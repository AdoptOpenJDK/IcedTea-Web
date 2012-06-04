/*
 * Copyright 2012 Red Hat, Inc.
 * This file is part of IcedTea, http://icedtea.classpath.org
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
 */

package net.sourceforge.jnlp;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;

import net.sourceforge.jnlp.cache.UpdatePolicy;

import org.junit.Test;

public class PluginBridgeTest {
    private class MockJNLPCreator extends JNLPCreator {

        private URL JNLPHref;

        public URL getJNLPHref() {
            return JNLPHref;
        }

        public JNLPFile create(URL location, Version version, boolean strict,
                UpdatePolicy policy, URL forceCodebase) throws IOException, ParseException {
            JNLPHref = location;
            return new MockJNLPFile();
        }
    }

    private class MockJNLPFile extends JNLPFile {
        public AppletDesc getApplet() {
            return new AppletDesc(null, null, null, 0, 0, new HashMap<String, String>());
        }

        public ResourcesDesc getResources() {
            return new ResourcesDesc(null, null, null, null);
        }
    }

    @Test
    public void testAbsoluteJNLPHref() throws MalformedURLException, Exception {
        URL codeBase = new URL("http://undesired.absolute.codebase.com");
        String absoluteLocation = "http://absolute.href.com/test.jnlp";
        Hashtable<String, String> atts = new Hashtable<String, String>();
        atts.put("jnlp_href", absoluteLocation);
        MockJNLPCreator mockCreator = new MockJNLPCreator();
        PluginBridge pb = new PluginBridge(codeBase, null, "", "", 0, 0, atts, "", mockCreator);
        assertEquals(absoluteLocation, mockCreator.getJNLPHref().toExternalForm());
    }

    @Test
    public void testRelativeJNLPHref() throws MalformedURLException, Exception {
        URL codeBase = new URL("http://desired.absolute.codebase.com/");
        String relativeLocation = "sub/dir/test.jnlp";
        Hashtable<String, String> atts = new Hashtable<String, String>();
        atts.put("jnlp_href", relativeLocation);
        MockJNLPCreator mockCreator = new MockJNLPCreator();
        PluginBridge pb = new PluginBridge(codeBase, null, "", "", 0, 0, atts, "", mockCreator);
        assertEquals(codeBase.toExternalForm() + relativeLocation,
                     mockCreator.getJNLPHref().toExternalForm());
    }

    @Test
    public void testNoSubDirInCodeBase() throws MalformedURLException, Exception {
        String desiredDomain = "http://desired.absolute.codebase.com";
        URL codeBase = new URL(desiredDomain + "/undesired/sub/dir");
        String relativeLocation = "/app/test/test.jnlp";
        Hashtable<String, String> atts = new Hashtable<String, String>();
        atts.put("jnlp_href", relativeLocation);
        MockJNLPCreator mockCreator = new MockJNLPCreator();
        PluginBridge pb = new PluginBridge(codeBase, null, "", "", 0, 0, atts, "", mockCreator);
        assertEquals(desiredDomain + relativeLocation,
                     mockCreator.getJNLPHref().toExternalForm());
    }

}