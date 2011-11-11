/* ResourcesTestl.java
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
package net.sourceforge.jnlp;

import java.io.File;
import java.io.FileFilter;
import java.net.URI;
import org.junit.Assert;

import org.junit.Test;

public class ResourcesTest {

    private static ServerAccess server = new ServerAccess();

    @Test
    public void testResourcesExists() throws Exception {
        File[] simpleContent = server.getDir().listFiles(new FileFilter() {

            public boolean accept(File file) {
                if (!file.isDirectory()) {
                    return true;
                } else {
                    return false;
                }
            }
        });
        Assert.assertNotNull(simpleContent);
        Assert.assertTrue(simpleContent.length > 5);

        for (int i = 0; i < simpleContent.length; i++) {
            File file = simpleContent[i];
            System.err.print(file.getName());
            //server port have in fact no usage in converting filename to uri-like-filename.
            //But if there is null, instead if some number, then nullpointer exception is thrown (Integer->int).
            //So I'm using "real" currently used port, instead of some random value.
            URI u = new URI((String)null,(String)null,(String)null,server.getPort(),file.getName(),(String)null,null);
            System.err.println(" ("+u.toString()+")");
            String fname=u.toString();
            if (file.getName().toLowerCase().endsWith(".jnlp")) {
                String c = server.getResourceAsString("/" + fname);
                Assert.assertTrue(c.contains("<"));
                Assert.assertTrue(c.contains(">"));
                Assert.assertTrue(c.contains("jnlp"));
                Assert.assertTrue(c.contains("resources"));
                Assert.assertTrue(c.replaceAll("\\s*", "").contains("</jnlp>"));

            } else {
                byte[] c = server.getResourceAsBytes("/" + fname).toByteArray();
                Assert.assertEquals(c.length, file.length());
            }

        }

    }
}
