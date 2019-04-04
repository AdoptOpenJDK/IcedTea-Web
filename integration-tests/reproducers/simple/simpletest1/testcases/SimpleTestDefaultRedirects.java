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

import net.sourceforge.jnlp.OptionsDefinitions;
import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.ServerLauncher;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class SimpleTestDefaultRedirects {

    private static final ServerAccess server = new ServerAccess();

    private static final String D = "-J-Dhttp.maxRedirects=20"; //default - https://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html ,  but...
    //unluckily, setting http.maxRedirects to eg 1 do not have testing benefit
    //as httpconnection then jsut throws exception instead of returning header to app's investigations
    //I doubt it is worthy to struggle with setInstanceFollowRedirects in production code
    
    private static final List<String> hr = Arrays.asList(new String[]{D, ServerAccess.HEADLES_OPTION, OptionsDefinitions.OPTIONS.REDIRECT.option});
    private static final List<String> hrv = Arrays.asList(new String[]{D, ServerAccess.VERBOSE_OPTION, ServerAccess.HEADLES_OPTION, OptionsDefinitions.OPTIONS.REDIRECT.option});
    private static final List<String> hv = Arrays.asList(new String[]{D, ServerAccess.VERBOSE_OPTION, ServerAccess.HEADLES_OPTION});

/* creates redirecting instances so oe can debug itw against it */
//    public static void main(String[] args) throws InterruptedException, MalformedURLException {
//        ServerLauncher[] servers = new ServerLauncher[3];
//
//        ServerLauncher server0 = ServerAccess.getIndependentInstance();
//        server0.setRedirect(ServerAccess.getInstance()); //redirecting to normal server singleton
//        server0.setRedirectCode(301);
//        servers[0] = server0;
//
//        ServerLauncher server1 = ServerAccess.getIndependentInstance();
//        server1.setRedirect(server0);
//        server1.setRedirectCode(301);
//        servers[1] = server1;
//
//        ServerLauncher server2 = ServerAccess.getIndependentInstance();
//        server2.setRedirect(server1);
//        server2.setRedirectCode(301);
//        servers[2] = server2;
//
//        System.out.println(server0);
//        System.out.println(server1);
//        System.out.println(server2);
//
//        try {
//            System.out.println(server0.getUrl("/" + "simpletest1.jnlp"));
//            System.out.println(server1.getUrl("/" + "simpletest1.jnlp"));
//            System.out.println(server2.getUrl("/" + "simpletest1.jnlp"));
//            while (true) {
//                Thread.sleep(100);
//            }
//        } finally {
//            for (ServerLauncher server : servers) {
//                server.stop();
//            }
//        }
//    }

    public void testbody(List<String> args, boolean pass, int... redirectCodes) throws Exception {
        testbody(args, pass, -1, redirectCodes);
    }

    public void testbody(List<String> args, boolean pass, int breakChain, int... redirectCodes) throws Exception {
        if (redirectCodes.length < 1) {
            throw new RuntimeException("At least one redrection server pelase");
        }
        ServerLauncher[] servers = new ServerLauncher[redirectCodes.length];

        ServerLauncher server0 = ServerAccess.getIndependentInstance();
        server0.setRedirect(ServerAccess.getInstance()); //redirecting to normal server singleton
        server0.setRedirectCode(redirectCodes[0]); //redirecting by first code
        servers[0] = server0;

        //create redirect chain
        for (int i = 1; i < redirectCodes.length; i++) {
            ServerLauncher serverI = ServerAccess.getIndependentInstance();
            serverI.setRedirect(servers[i - 1]); //redirecting to pevious in chain
            serverI.setRedirectCode(redirectCodes[i]); //by  given code
            servers[i] = serverI;

        }
        testbody(args, pass, breakChain, servers);
    }

    public void testbody(List<String> args, boolean pass, int breakChain, ServerLauncher[] servers) throws Exception {
        if (breakChain >= 0) {
            servers[breakChain].setRedirect(null);
            servers[breakChain].stop();
        }
        ServerLauncher server3012378 = servers[servers.length - 1];
        try {
            //now connect to last in chain and we should always get reposnse from ServerAccess.getInstance()
            ProcessResult pr = ServerAccess.executeProcessUponURL(server.getJavawsLocation(),
                    args,
                    server3012378.getUrl("/" + "simpletest1.jnlp"),
                    null,
                    null
            );
            SimpleTest1Test.checkLaunched(pr, !pass, false);
            if (pass) {
                Assert.assertTrue(0 == pr.returnValue);
            } else {
                Assert.assertFalse(0 == pr.returnValue);
            }
        } finally {
            for (int i = 0; i < servers.length; i++) {
                if (i != breakChain) {
                    ServerLauncher serverI = servers[i];
                    try {
                        serverI.setRedirect(null);
                        serverI.stop();
                    } catch (Exception ex) {
                        ServerAccess.logException(ex);
                    }
                }
            }
        }
    }

    // note, tonly 308 needs help form ITW,others are redirected autmatically in http connection
    // https://docs.oracle.com/javase/7/docs/api/java/net/HttpURLConnection.html#setInstanceFollowRedirects%28boolean%29
    public void testbody308(boolean pass, int... redirectCodes) throws Exception {
        if (pass) {
            testbody(hr, pass, redirectCodes);
        } else {
            testbody(hv, pass, redirectCodes);
        }
    }

    public void testbodyOthers(boolean pass, int... redirectCodes) throws Exception {
        if (pass) {
            testbody(hr, true, redirectCodes);
        } else {
            testbody(hv, true, redirectCodes);
        }
    }

    //some chains tests
    @Test
    public void testSimpletest1RedirectChain1AllowedOk() throws Exception {
        testbodyOthers(true, 301, 302, 303, 307);
    }

    @Test
    public void testSimpletest1RedirectChain1NotAllowedOk() throws Exception {
        testbodyOthers(false, 301, 302, 303, 307);
    }

    @Test
    public void testSimpletest1RedirectChain2AllowedOk() throws Exception {
        testbody308(true, 301, 308, 302, 303, 307);
    }

    @Test
    public void testSimpletest1RedirectChain2NotAllowedOk() throws Exception {
        server.executeJavawsClearCache();
        testbody308(false, 301, 308, 302, 303, 307);
    }

    @Test
    public void testSimpletest1RedirectChain3AllowedBroken() throws Exception {
        server.executeJavawsClearCache();
        testbody(hrv, false, 1, new int[]{301, 302, 302, 303, 307});
    }

    @Test
    public void testSimpletest1RedirectChain3NotAllowedBroken() throws Exception {
        server.executeJavawsClearCache();
        testbody(hv, false, 1, new int[]{301, 302, 302, 303, 307});
    }

    @Test
    public void testSimpletest1RedirectChain3AlowedCycle() throws Exception {
        server.executeJavawsClearCache();
        ServerLauncher[] servers = new ServerLauncher[3];

        ServerLauncher server0 = ServerAccess.getIndependentInstance();
        server0.setRedirect(ServerAccess.getInstance()); //redirecting to normal server singleton
        server0.setRedirectCode(301);
        servers[0] = server0;

        ServerLauncher server1 = ServerAccess.getIndependentInstance();
        server1.setRedirectCode(301);
        servers[1] = server1;

        ServerLauncher server2 = ServerAccess.getIndependentInstance();
        server2.setRedirectCode(301);
        servers[2] = server2;

        server1.setRedirect(server2);
        server2.setRedirect(server1);
        testbody(hrv, false, -1, servers);
    }

    //end chains
    // basic tests
    @Test
    public void testSimpletest1Redirect301AllowedOk() throws Exception {
        testbodyOthers(true, 301);
    }

    @Test
    public void testSimpletest1Redirect301NotAllowedOk() throws Exception {
        testbodyOthers(false, 301);
    }

    @Test
    public void testSimpletest1Redirect302AllowedOk() throws Exception {
        testbodyOthers(true, 302);
    }

    @Test
    public void testSimpletest1Redirect302NotAllowedOk() throws Exception {
        testbodyOthers(false, 302);
    }

    @Test
    public void testSimpletest1Redirect303AllowedOk() throws Exception {
        testbodyOthers(true, 303);
    }

    @Test
    public void testSimpletest1Redirect331NotAllowedOk() throws Exception {
        testbodyOthers(false, 303);
    }

    @Test
    public void testSimpletest1Redirect307AllowedOk() throws Exception {
        testbodyOthers(true, 307);
    }

    @Test
    public void testSimpletest1Redirect307NotAllowedOk() throws Exception {
        testbodyOthers(false, 307);
    }

    @Test
    public void testSimpletest1Redirect308AllowedOk() throws Exception {
        testbody308(true, 308);
    }

    @Test
    public void testSimpletest1Redirect308NotAllowedOk() throws Exception {
        server.executeJavawsClearCache();
        testbody308(false, 308);
    }
    // end basic tests

}
