/* ReadPropertiesSignedTest.java
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


import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.sourceforge.jnlp.ServerAccess;
import org.junit.Assert;
import org.junit.Test;

public class ReadPropertiesSignedTest {

    private static ServerAccess server = new ServerAccess();
    private final List<String> l=Collections.unmodifiableList(Arrays.asList(new String[] {"-Xtrustall"}));

    String accessMatcher = "(?s).*java.security.AccessControlException.{0,5}access denied.{0,5}java.util.PropertyPermission.{0,5}" + "user.name.{0,5}read" + ".*";

    @Test
    public void ReadSignedPropertiesWithoutPermissionsWithXtrustAll() throws Exception {
        //no request for permissions
        ServerAccess.ProcessResult pr=server.executeJavawsHeadless(l,"/ReadPropertiesSigned1.jnlp");
        Assert.assertTrue("Stderr should match "+accessMatcher+" but did not",pr.stderr.matches(accessMatcher));
        String ss="ClassNotFoundException";
        Assert.assertFalse("Stderr should not contains "+ss+" but did",pr.stderr.contains(ss));
        Assert.assertTrue("stdout lenght should be <2 but was "+pr.stdout.length(),pr.stdout.length()<2); // /home/user or /root or eanything else :(
        Assert.assertFalse("should not be terminated but was",pr.wasTerminated);
        Assert.assertEquals((Integer)0, pr.returnValue);
    }

    @Test
    public void ReadSignedPropertiesWithPermissionsWithXtrustAll() throws Exception {
        //request for allpermissions
        ServerAccess.ProcessResult pr=server.executeJavawsHeadless(l,"/ReadPropertiesSigned2.jnlp");
        Assert.assertFalse("Stderr should NOT match "+accessMatcher+" but did",pr.stderr.matches(accessMatcher));
        String ss="ClassNotFoundException";
        Assert.assertFalse("Stderr should not contains "+ss+" but did",pr.stderr.contains(ss));
        Assert.assertTrue("stdout lenght should be >= but was "+pr.stdout.length(),pr.stdout.length()>=4); // /home/user or /root or eanything else :(
        Assert.assertFalse("should not be terminated but was",pr.wasTerminated);
        Assert.assertEquals((Integer)0, pr.returnValue);
    }

    @Test
    public void EnsureXtrustallNotAffectingUnsignedBehaviour() throws Exception {
        ServerAccess.ProcessResult pr=server.executeJavawsHeadless(l,"/ReadProperties1.jnlp");
        Assert.assertTrue("Stderr should match "+accessMatcher+" but did not",pr.stderr.matches(accessMatcher));
        String ss="ClassNotFoundException";
        Assert.assertFalse("Stderr should not contains "+ss+" but did",pr.stderr.contains(ss));
        Assert.assertFalse("stdout lenght should not be  >2 but was "+pr.stdout.length(),pr.stdout.length()>2);
        Assert.assertFalse("should not be terminated but was",pr.wasTerminated);
        Assert.assertEquals((Integer)0, pr.returnValue);
        ServerAccess.ProcessResult pr2=server.executeJavawsHeadless(null,"/ReadProperties1.jnlp");
        Assert.assertEquals(pr.stderr, pr2.stderr);
        Assert.assertEquals(pr.stdout, pr2.stdout);

    }
  }
