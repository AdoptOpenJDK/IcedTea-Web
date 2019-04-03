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
package net.sourceforge.jnlp.security;

import java.security.Permission;
import net.sourceforge.jnlp.config.InfrastructureFileDescriptor;
import net.sourceforge.jnlp.config.PathsAndFiles;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

public class KeyStoresTest {

    private class DummySM extends SecurityManager {
        boolean called = false;
        
        @Override
        public void checkPermission(Permission perm) {
           called=true;
        }

    }

    @AfterClass
    public static void removeClassLaoder() {
        System.setSecurityManager(null);
    }

    @Test
    public void getKeyStoreUserLocationTest() {
        InfrastructureFileDescriptor s;
        System.setSecurityManager(null);
        s = KeyStores.getKeyStoreLocation(KeyStores.Level.USER, KeyStores.Type.CA_CERTS);
        Assert.assertEquals(s.getFile(), PathsAndFiles.USER_CACERTS.getFile());
        s = KeyStores.getKeyStoreLocation(KeyStores.Level.USER, KeyStores.Type.CERTS);
        Assert.assertEquals(s.getFile(), PathsAndFiles.USER_CERTS.getFile());
        s = KeyStores.getKeyStoreLocation(KeyStores.Level.USER, KeyStores.Type.CLIENT_CERTS);
        Assert.assertEquals(s.getFile(), PathsAndFiles.USER_CLIENTCERT.getFile());
        s = KeyStores.getKeyStoreLocation(KeyStores.Level.USER, KeyStores.Type.JSSE_CA_CERTS);
        Assert.assertEquals(s.getFile(), PathsAndFiles.USER_JSSECAC.getFile());
        s = KeyStores.getKeyStoreLocation(KeyStores.Level.USER, KeyStores.Type.JSSE_CERTS);
        Assert.assertEquals(s.getFile(), PathsAndFiles.USER_JSSECER.getFile());
    }

    @Test
    public void getKeyStoreSystemLocationTest() {
        InfrastructureFileDescriptor s;
        System.setSecurityManager(null);
        s = KeyStores.getKeyStoreLocation(KeyStores.Level.SYSTEM, KeyStores.Type.CA_CERTS);
        Assert.assertEquals(s.getFile(), PathsAndFiles.SYS_CACERT.getFile());
        s = KeyStores.getKeyStoreLocation(KeyStores.Level.SYSTEM, KeyStores.Type.CERTS);
        Assert.assertEquals(s.getFile(), PathsAndFiles.SYS_CERT.getFile());
        s = KeyStores.getKeyStoreLocation(KeyStores.Level.SYSTEM, KeyStores.Type.CLIENT_CERTS);
        Assert.assertEquals(s.getFile(), PathsAndFiles.SYS_CLIENTCERT.getFile());
        s = KeyStores.getKeyStoreLocation(KeyStores.Level.SYSTEM, KeyStores.Type.JSSE_CA_CERTS);
        Assert.assertEquals(s.getFile(), PathsAndFiles.SYS_JSSECAC.getFile());
        s = KeyStores.getKeyStoreLocation(KeyStores.Level.SYSTEM, KeyStores.Type.JSSE_CERTS);
        Assert.assertEquals(s.getFile(), PathsAndFiles.SYS_JSSECERT.getFile());
    }

    @Test
    public void getKeyStoreUserLocationTestSM() {
        DummySM dm = new DummySM();
        System.setSecurityManager(dm);
        InfrastructureFileDescriptor s;
        s = KeyStores.getKeyStoreLocation(KeyStores.Level.USER, KeyStores.Type.CA_CERTS);
        Assert.assertEquals(s.getFile(), PathsAndFiles.USER_CACERTS.getFile());
        s = KeyStores.getKeyStoreLocation(KeyStores.Level.USER, KeyStores.Type.CERTS);
        Assert.assertEquals(s.getFile(), PathsAndFiles.USER_CERTS.getFile());
        s = KeyStores.getKeyStoreLocation(KeyStores.Level.USER, KeyStores.Type.CLIENT_CERTS);
        Assert.assertEquals(s.getFile(), PathsAndFiles.USER_CLIENTCERT.getFile());
        s = KeyStores.getKeyStoreLocation(KeyStores.Level.USER, KeyStores.Type.JSSE_CA_CERTS);
        Assert.assertEquals(s.getFile(), PathsAndFiles.USER_JSSECAC.getFile());
        s = KeyStores.getKeyStoreLocation(KeyStores.Level.USER, KeyStores.Type.JSSE_CERTS);
        Assert.assertEquals(s.getFile(), PathsAndFiles.USER_JSSECER.getFile());
        Assert.assertEquals(true, dm.called);
    }

    @Test
    public void getKeyStoreSystemLocationTestSM() {
        DummySM dm = new DummySM();
        System.setSecurityManager(dm);
        InfrastructureFileDescriptor s;
        s = KeyStores.getKeyStoreLocation(KeyStores.Level.SYSTEM, KeyStores.Type.CA_CERTS);
        Assert.assertEquals(s.getFile(), PathsAndFiles.SYS_CACERT.getFile());
        s = KeyStores.getKeyStoreLocation(KeyStores.Level.SYSTEM, KeyStores.Type.CERTS);
        Assert.assertEquals(s.getFile(), PathsAndFiles.SYS_CERT.getFile());
        s = KeyStores.getKeyStoreLocation(KeyStores.Level.SYSTEM, KeyStores.Type.CLIENT_CERTS);
        Assert.assertEquals(s.getFile(), PathsAndFiles.SYS_CLIENTCERT.getFile());
        s = KeyStores.getKeyStoreLocation(KeyStores.Level.SYSTEM, KeyStores.Type.JSSE_CA_CERTS);
        Assert.assertEquals(s.getFile(), PathsAndFiles.SYS_JSSECAC.getFile());
        s = KeyStores.getKeyStoreLocation(KeyStores.Level.SYSTEM, KeyStores.Type.JSSE_CERTS);
        Assert.assertEquals(s.getFile(), PathsAndFiles.SYS_JSSECERT.getFile());
        Assert.assertEquals(true, dm.called);
    } 

}
