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

import java.util.Arrays;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import net.sourceforge.jnlp.Version;

import org.junit.AfterClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class ResourceTest {

    private static final int UNINITIALIZED = Resource.UNINITIALIZED;
    private static final int CONNECT = Resource.CONNECT;
    private static final int CONNECTING = Resource.CONNECTING;
    private static final int CONNECTED = Resource.CONNECTED;
    private static final int DOWNLOAD = Resource.DOWNLOAD;
    private static final int DOWNLOADING = Resource.DOWNLOADING;
    private static final int DOWNLOADED = Resource.DOWNLOADED;
    private static final int ERROR = Resource.ERROR;
    private static final int STARTED = Resource.STARTED;

    @Test
    public void testNewResourceIsUninitialized() throws Exception {
        Resource res = createResource("NewResource");
        assertTrue("Resource should not have had any status flags set", hasFlag(res, UNINITIALIZED));
    }

    @Test
    public void testSetFlag() throws Exception {
        Resource res = createResource("SetFlag");
        setStatus(res, Arrays.asList(Integer.valueOf(CONNECT)));
        assertFalse("Resource should have been initialized", hasFlag(res, UNINITIALIZED));
        assertTrue("Resource should have had CONNECT set", hasFlag(res, CONNECT));
        assertTrue("Resource should have only had CONNECT set", hasOnly(res, Arrays.asList(Integer.valueOf(CONNECT))));
    }

    @Test
    public void testSetMultipleFlags() throws Exception {
        Resource res = createResource("SetFlags");
        setStatus(res, Arrays.asList(Integer.valueOf(CONNECT), Integer.valueOf(DOWNLOAD)));
        assertFalse("Resource should have been initialized", hasFlag(res, UNINITIALIZED));
        assertTrue("Resource should have had CONNECT set", hasFlag(res, CONNECT));
        assertTrue("Resource should have had DOWNLOAD set", hasFlag(res, DOWNLOAD));
        assertTrue("Resource should have only had CONNECT and DOWNLOAD set", hasOnly(res, Arrays.asList(Integer.valueOf(CONNECT), Integer.valueOf(DOWNLOAD))));
    }

    @Test
    public void testChangeStatus() throws Exception {
        Resource res = createResource("ChangeStatus");
        setStatus(res, Arrays.asList(Integer.valueOf(CONNECT)));
        assertTrue("Resource should have had CONNECT set", hasFlag(res, CONNECT));
        assertTrue("Resource should have only had CONNECT set", hasOnly(res, Arrays.asList(Integer.valueOf(CONNECT))));

        Collection<Integer> downloadFlags = Arrays.asList(DOWNLOAD, DOWNLOADING, DOWNLOADED);
        Collection<Integer> connectFlags = Arrays.asList(CONNECT, CONNECTING, CONNECTED);
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

    private static void setStatus(Resource resource, Collection<Integer> flags) {
        for (Integer flag : flags) {
            resource.status = resource.status | flag;
        }
    }

    private static void changeStatus(Resource resource, Collection<Integer> clear, Collection<Integer> add) {
        int setMask = 0, unsetMask = 0;
        for (Integer setFlag : add) {
            setMask = setMask | setFlag;
        }
        for (Integer unsetFlag : clear) {
            unsetMask = unsetMask | unsetFlag;
        }
        resource.changeStatus(unsetMask, setMask);
    }

    private static boolean hasOnly(Resource resource, Collection<Integer> flags) {
        int mask = 0;
        for (Integer flag : flags) {
            mask = mask | flag;
        }
        return (resource.status ^ mask) == 0;
    }

    private static boolean hasFlag(Resource resource, int flag) {
        return resource.isSet(flag);
    }

}
