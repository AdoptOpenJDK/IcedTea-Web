/* SimpleTest1Test.java
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.ServerLauncher;
import net.sourceforge.jnlp.annotations.Bug;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

import org.junit.Test;

@Bug(id = "PR2591")
public class SimpleTest1CountRequests {

    private static final ServerAccess server = new ServerAccess();
    private static ServerLauncher server0;
    private static final Map<String, Map<String, Integer>> counter = new HashMap<>();
    private static final List<String> hv = Arrays.asList(new String[]{ServerAccess.VERBOSE_OPTION, ServerAccess.HEADLES_OPTION});

    @BeforeClass
    public static void createCountingServer() {
        server0 = ServerAccess.getIndependentInstance();
        server0.setRequestsCounter(counter);
    }

    @AfterClass
    public static void stopCountingServer() {
        server0.stop();
    }

    @Bug(id = "PR2591")
    @Test
    public void testSimpletest1EachResourceOnePerRun() throws Exception {
        server0.setSupportingHeadRequest(true);
        counter.clear();
        ProcessResult pr = ServerAccess.executeProcessUponURL(server.getJavawsLocation(),
                hv,
                server0.getUrl("/simpletest1.jnlp"),
                null,
                null
        );
        SimpleTest1Test.checkLaunched(pr);
        Assert.assertTrue(counter.get("./simpletest1.jnlp").get("GET").equals(1)); //2 without bugfix
        Assert.assertTrue(counter.get("./simpletest1.jnlp").get("HEAD").equals(1));
        Assert.assertTrue(counter.get("./simpletest1.jar").get("GET").equals(1));//2 without bugfix
        Assert.assertTrue(counter.get("./simpletest1.jar").get("HEAD").equals(1));

    }

    @Bug(id = "PR2591")
    @Test
    public void testSimpletest1EachResourceOnePerRunHeadsOff() throws Exception {
        server0.setSupportingHeadRequest(false);
        counter.clear();
        ProcessResult pr = ServerAccess.executeProcessUponURL(server.getJavawsLocation(),
                hv,
                server0.getUrl("/simpletest1.jnlp"),
                null,
                null
        );
        SimpleTest1Test.checkLaunched(pr);
        Assert.assertTrue(counter.get("./simpletest1.jnlp").get("GET").equals(2)); //3 without bugfix
        Assert.assertTrue(counter.get("./simpletest1.jnlp").get("HEAD") == null);
        Assert.assertTrue(counter.get("./simpletest1.jar").get("GET").equals(2));//3 without bugfix
        Assert.assertTrue(counter.get("./simpletest1.jar").get("HEAD") == (null));

    }

}
