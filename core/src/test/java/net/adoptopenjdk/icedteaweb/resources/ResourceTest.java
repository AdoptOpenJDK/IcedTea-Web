/* ResourceTest.java
 Copyright (C) 2014 Red Hat, Inc.

 This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify it under the
terms of the GNU General Public License as published by the Free Software
Foundation, version 2.

IcedTea is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
IcedTea; see the file COPYING. If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is making a
combined work based on this library. Thus, the terms and conditions of the GNU
General Public License cover the whole combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent modules, and
to copy and distribute the resulting executable under terms of your choice,
provided that you also meet, for each linked independent module, the terms and
conditions of the license of that module. An independent module is a module
which is not derived from or based on this library. If you modify this library,
you may extend this exception to your version of the library, but you are not
obligated to do so. If you do not wish to do so, delete this exception
statement from your version.
*/

package net.adoptopenjdk.icedteaweb.resources;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.sourceforge.jnlp.DownloadOptions;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static net.adoptopenjdk.icedteaweb.resources.ResourceStatus.INCOMPLETE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ResourceTest {

    @Test
    public void testGetLocation() throws Exception {
        final URL url = new URL("http://example.com/applet.jar");
        final VersionString requestVersion = VersionString.fromString("1.0");

        final Resource res = Resource.createOrGetResource(url, requestVersion, null, UpdatePolicy.ALWAYS);

        assertEquals("Locations should match each other", url, res.getLocation());
        assertEquals("Versions should match each other.", res.getRequestVersion(), requestVersion);
    }

    @Test
    public void testTransferredIsZero() throws Exception {
        final Resource res = createResource();
        assertEquals(0, res.getTransferred());
    }

    @Test
    public void testSizeIsNegativeOne() throws Exception {
        final Resource res = createResource();
        assertEquals(-1, res.getSize());
    }

    @Test
    public void testSetSize() throws Exception {
        final Resource res = createResource();
        final long newSize = res.getSize() + 10;

        res.setSize(newSize);

        assertEquals(newSize, res.getSize());
    }

    @Test
    public void testNewResourceIsUninitialized() throws Exception {
        Resource res = createResource();
        assertTrue("Resource should not have had any status flags set", res.hasStatus(INCOMPLETE));
    }

    private static Resource createResource() throws MalformedURLException {
        final URL dummyUrl = new URL("http://example.com/applet.jar");
        return Resource.createOrGetResource(dummyUrl, VersionString.fromString("1.0"), DownloadOptions.NONE, UpdatePolicy.ALWAYS);
    }
}
