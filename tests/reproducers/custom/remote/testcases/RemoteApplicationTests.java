/* RemoteApplicationTests.java
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
import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.annotations.KnownToFail;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.annotations.Remote;
import org.junit.Test;

@Remote
public class RemoteApplicationTests {

    private static ServerAccess server = new ServerAccess();
    private final List<String> l = Collections.unmodifiableList(Arrays.asList(new String[]{"-Xtrustall"}));
    private final List<String> ll = Collections.unmodifiableList(Arrays.asList(new String[]{"-Xtrustall", "-Xnofork"}));

    @Test
    @NeedsDisplay
    public void topCoderRemoteTest() throws Exception {
        RemoteApplicationSettings.RemoteApplicationTestcaseSettings settings = new RemoteApplicationSettings.TopCoder();
        ProcessResult pr = server.executeJavawsUponUrl(ll, settings.getUrl());
        settings.evaluate(pr);
    }

    @Test
    @NeedsDisplay
    public void sunSwingRemoteTest() throws Exception {
        RemoteApplicationSettings.RemoteApplicationTestcaseSettings settings = new RemoteApplicationSettings.SunSwingDemo();
        ProcessResult pr = server.executeJavawsUponUrl(l, settings.getUrl());
        settings.evaluate(pr);
    }

    @Test
    @NeedsDisplay
    public void fourierTransformRemoteTest() throws Exception {
        RemoteApplicationSettings.RemoteApplicationTestcaseSettings settings = new RemoteApplicationSettings.FourierTransform();
        ProcessResult pr = server.executeJavawsUponUrl(null, settings.getUrl());
        settings.evaluate(pr);
    }

    @Test
    @NeedsDisplay
    public void arboresRemoteTest() throws Exception {
        RemoteApplicationSettings.RemoteApplicationTestcaseSettings settings = new RemoteApplicationSettings.Arbores();
        ProcessResult pr = server.executeJavawsUponUrl(l, settings.getUrl());
        settings.evaluate(pr);
    }

    @Test
    @NeedsDisplay
    public void phetsimsRemoteTest() throws Exception {
        RemoteApplicationSettings.RemoteApplicationTestcaseSettings settings = new RemoteApplicationSettings.PhetSims();
        ProcessResult pr = server.executeJavawsUponUrl(l, settings.getUrl());
        settings.evaluate(pr);
    }

    @Test
    @NeedsDisplay
    public void arboresDepositRemoteTest() throws Exception {
        RemoteApplicationSettings.RemoteApplicationTestcaseSettings settings = new RemoteApplicationSettings.ArboresDeposit();
        ProcessResult pr = server.executeJavawsUponUrl(l, settings.getUrl());
        settings.evaluate(pr);
    }

    /*   This application need all permissions, but contains unsigned jar. Have exception but works at least somehow 
     */
    @Test
    @NeedsDisplay
    public void orawebCernChRemoteTest() throws Exception {
        RemoteApplicationSettings.RemoteApplicationTestcaseSettings settings = new RemoteApplicationSettings.OrawebCernCh();
        ProcessResult pr = server.executeJavawsUponUrl(ll, settings.getUrl());
        settings.evaluate(pr);
    }

    @Test
    @NeedsDisplay
    public void AviationWeatherRemoteTest() throws Exception {
        RemoteApplicationSettings.RemoteApplicationTestcaseSettings settings = new RemoteApplicationSettings.AviationWeather();
        ProcessResult pr = server.executeJavawsUponUrl(ll, settings.getUrl());
        settings.evaluate(pr);
    }

    @Test
    @NeedsDisplay
    public void fuseSysRemoteTest() throws Exception {
        RemoteApplicationSettings.RemoteApplicationTestcaseSettings settings = new RemoteApplicationSettings.FuseSwing();
        ProcessResult pr = server.executeJavawsUponUrl(l, settings.getUrl());
        settings.evaluate(pr);
    }

    @Test
    @NeedsDisplay
    public void gantProjectRemoteTest() throws Exception {
        RemoteApplicationSettings.RemoteApplicationTestcaseSettings settings = new RemoteApplicationSettings.GnattProject();
        ProcessResult pr = server.executeJavawsUponUrl(l, settings.getUrl());
        settings.evaluate(pr);
    }

    @Test
    @NeedsDisplay
    public void geogebraRemoteTest() throws Exception {
        RemoteApplicationSettings.RemoteApplicationTestcaseSettings settings = new RemoteApplicationSettings.GeoGebra();
        ProcessResult pr = server.executeJavawsUponUrl(ll, settings.getUrl());
        settings.evaluate(pr);
    }
}
