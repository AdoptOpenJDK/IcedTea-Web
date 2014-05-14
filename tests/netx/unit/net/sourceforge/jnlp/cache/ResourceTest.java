/* ResourceTest.java
 Copyright (C) 2014 Red Hat, Inc.

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

package net.sourceforge.jnlp.cache;

import static net.sourceforge.jnlp.cache.Resource.Status.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.EnumSet;

import net.sourceforge.jnlp.Version;

import org.junit.Test;

public class ResourceTest {

    @Test
    public void testNewResourceIsUninitialized() throws Exception {
        Resource res = createResource("NewResource");
        assertTrue("Resource should not have had any status flags set", isUninitialized(res));
    }

    @Test
    public void testSetFlag() throws Exception {
        Resource res = createResource("SetFlag");
        setStatus(res, EnumSet.of(CONNECT));
        assertFalse("Resource should have been initialized", isUninitialized(res));
        assertTrue("Resource should have had CONNECT set", hasFlag(res, CONNECT));
        assertTrue("Resource should have only had CONNECT set", hasOnly(res, EnumSet.of(CONNECT)));
    }

    @Test
    public void testSetMultipleFlags() throws Exception {
        Resource res = createResource("SetFlags");
        setStatus(res, EnumSet.of(CONNECT, DOWNLOAD));
        assertFalse("Resource should have been initialized", isUninitialized(res));
        assertTrue("Resource should have had CONNECT set", hasFlag(res, CONNECT));
        assertTrue("Resource should have had DOWNLOAD set", hasFlag(res, DOWNLOAD));
        assertTrue("Resource should have only had CONNECT and DOWNLOAD set", hasOnly(res, EnumSet.of(CONNECT, DOWNLOAD)));
    }

    @Test
    public void testChangeStatus() throws Exception {
        Resource res = createResource("ChangeStatus");
        setStatus(res, EnumSet.of(CONNECT));
        assertTrue("Resource should have had CONNECT set", hasFlag(res, CONNECT));
        assertTrue("Resource should have only had CONNECT set", hasOnly(res, EnumSet.of(CONNECT)));

        Collection<Resource.Status> downloadFlags = EnumSet.of(DOWNLOAD, DOWNLOADING, DOWNLOADED);
        Collection<Resource.Status> connectFlags = EnumSet.of(CONNECT, CONNECTING, CONNECTED);
        changeStatus(res, connectFlags, downloadFlags);

        assertTrue("Resource should have had DOWNLOAD set", hasFlag(res, DOWNLOAD));
        assertTrue("Resource should have had DOWNLOADING set", hasFlag(res, DOWNLOADING));
        assertTrue("Resource should have had DOWNLOADED set", hasFlag(res, DOWNLOADED));
        assertTrue("Resource should have only had DOWNLOAD{,ING,ED} flags set", hasOnly(res, downloadFlags));
        assertFalse("Resource should not have had CONNECT set", hasFlag(res, CONNECT));
    }

    private static Resource createResource(String testName) throws MalformedURLException {
        URL dummyUrl = new URL("http://example.com/applet" + testName + ".jar");
        return Resource.getResource(dummyUrl, new Version("1.0"), UpdatePolicy.ALWAYS);
    }

    private static void setStatus(Resource resource, Collection<Resource.Status> flags) {
        resource.setStatusFlags(flags);
    }

    private static void changeStatus(Resource resource, Collection<Resource.Status> clear, Collection<Resource.Status> add) {
        resource.changeStatus(clear, add);
    }

    private static boolean hasOnly(Resource resource, Collection<Resource.Status> flags) {
        for (final Resource.Status flag : flags) { // ensure all the specified flags are set
            if (!resource.isSet(flag)) {
                return false;
            }
        }
        for (final Resource.Status flag : Resource.Status.values()) { // ensure all other flags are unset
            if (resource.isSet(flag) && !flags.contains(flag)) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasFlag(Resource resource, Resource.Status flag) {
        return resource.isSet(flag);
    }

    private static boolean isUninitialized(Resource resource) {
        return !resource.isInitialized();
    }

}
