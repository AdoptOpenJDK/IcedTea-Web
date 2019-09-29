/* CacheUtilTest.java
Copyright (C) 2012 Red Hat, Inc.

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
package net.sourceforge.jnlp.cache.cache;

import org.junit.Assert;
import org.junit.Test;


public class CacheIdTest {
    @Test
    public void CacheID(){
        CacheId cj11 = new CacheId.CacheJnlpId("a");
        CacheId cj12 = new CacheId.CacheJnlpId("a");
        CacheId cj2 = new CacheId.CacheJnlpId("b");
        CacheId cj31 = new CacheId.CacheJnlpId(null);
        CacheId cj32 = new CacheId.CacheJnlpId(null);
        CacheId cd11 = new CacheId.CacheDomainId("a");
        CacheId cd12 = new CacheId.CacheDomainId("a");
        CacheId cd2 = new CacheId.CacheDomainId("b");
        CacheId cd31 = new CacheId.CacheDomainId(null);
        CacheId cd32 = new CacheId.CacheDomainId(null);
        
        Assert.assertEquals(cj11, cj11);
        Assert.assertEquals(cj11, cj12);
        Assert.assertEquals(cd11, cd11);
        Assert.assertEquals(cd11, cd12);
        Assert.assertEquals(cj31, cj31);
        Assert.assertEquals(cj31, cj32);
        Assert.assertEquals(cd31, cd31);
        Assert.assertEquals(cd31, cd32);
        
        Assert.assertNotEquals(cj11, cj2);
        Assert.assertNotEquals(cj11, cj31);
        Assert.assertNotEquals(cd11, cd2);
        Assert.assertNotEquals(cd11, cd31);
        
        Assert.assertNotEquals(cj11, cd11);
        Assert.assertNotEquals(cj2, cd2);
        Assert.assertNotEquals(cj31, cd31);
        Assert.assertNotEquals(cj32, cd32);
    }
}
