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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.runtime.Translator;
import org.junit.Assert;
import org.junit.Test;

public class RemoteApplicationSettings {

    public static final String mustEmpty = "must be empty, was not";
    public static final String stdout = "Stdout";
    public static final String stderr = "Stderr";
    public static final String stdoutEmpty = stdout + " " + mustEmpty;
    public static final String stderrEmpty = stderr + " " + mustEmpty;

    public static URL createCatchedUrl(String r) {
        try {
            return new URL(r);
        } catch (MalformedURLException mex) {
            throw new RuntimeException(mex);
        }
    }

    public interface RemoteApplicationTestcaseSettings {

        public URL getUrl();

        public void evaluate(ProcessResult pr);
        
        public List<String> modifyParams(List<String> global);
    }

    public static abstract class StringBasedURL implements RemoteApplicationTestcaseSettings {

        URL u;

        public String clean(String s){
            s = s.replace(Translator.R("MACDisabledMessage"),"");
            s = s.replace(Translator.R("MACCheckSkipped", ".*", ".*"), "");
            s = s.replace(JNLPFile.TITLE_NOT_FOUND, "");
            s = s.replaceAll("Fontconfig warning.*", "");
            return  s.replaceAll("\\s*" + JNLPFile.TITLE_NOT_FOUND + "\\s*", "").trim();
            
        }
        @Override
        public URL getUrl() {
            return u;
        }

        public StringBasedURL(String r) {
            this.u = createCatchedUrl(r);
        }

        @Override
        public List<String> modifyParams(List<String> global) {
            return global;
        }
        
        
    }

    public static class FourierTransform extends StringBasedURL {

        public FourierTransform() {
            super("http://www.cs.brown.edu/exploratories/freeSoftware/repository/edu/brown/cs/exploratories/applets/fft1DApp/1d_fast_fourier_transform_java_jnlp.jnlp");
        }

        @Override
        public void evaluate(ProcessResult pr) {
            Assert.assertTrue(stdoutEmpty, clean(pr.stdout).length() == 0);
            Assert.assertTrue(clean(pr.stderr).length() == 0 || pr.stderr.contains(IllegalStateException.class.getName()));

        }
    }

    public static class OrawebCernCh extends StringBasedURL {

        public OrawebCernCh() {
            super("https://oraweb.cern.ch/pls/atlasintegration/docs/EMDH_atlas.jnlp");
        }

        @Override
        public void evaluate(ProcessResult pr) {
            Assert.assertTrue(stdoutEmpty, clean(pr.stdout).length() == 0);
            Assert.assertTrue(clean(pr.stderr).length() == 0 || pr.stderr.contains("Cannot grant permissions to unsigned jars. Application requested security permissions, but jars are not signed"));

        }

        @Override
        public List<String> modifyParams(List<String> global) {
            List l = new ArrayList(global);
            l.add("-J-Dhttps.protocols=TLSv1,SSLv3,SSLv2Hello");
            return l;
        }
        
    }

    public static class GnattProject extends StringBasedURL {

        public GnattProject() {
            super("http://ganttproject.googlecode.com/svn/webstart/ganttproject-2.0.10/ganttproject-2.0.10.jnlp");
        }

        @Override
        public void evaluate(ProcessResult pr) {
            Assert.assertTrue(stdout, clean(pr.stdout).length() == 0);
            Assert.assertTrue(pr.stderr.contains("Splash closed"));
            Assert.assertFalse(pr.stderr.contains("Exception"));

        }
    }

    public static class GeoGebra extends StringBasedURL {

        public GeoGebra() {
            super("http://www.geogebra.org/webstart/geogebra.jnlp");
        }

        @Override
        public void evaluate(ProcessResult pr) {
            //some debug coords are appearing
            Assert.assertTrue(pr.stdout.toLowerCase().contains("geogebra"));
            Assert.assertFalse(pr.stderr.toLowerCase().contains("exception"));

        }
    }

    public abstract static class NoOutputs extends StringBasedURL {

        public NoOutputs(String r) {
            super(r);
        }

        @Override
        public void evaluate(ProcessResult pr) {
            Assert.assertTrue(stdoutEmpty, pr.stdout.length() == 0);
            Assert.assertTrue(stderrEmpty, pr.stderr.length() == 0);

        }
    }

     public abstract static class NearlyNoOutputs extends StringBasedURL {

        public NearlyNoOutputs(String r) {
            super(r);
        }

        @Override
        public void evaluate(ProcessResult pr) {
            Assert.assertTrue(stdoutEmpty, clean(pr.stdout).length() == 0);
            Assert.assertTrue(stderrEmpty, clean(pr.stderr).length() == 0);

        }
    }
     
     public abstract static class NearlyNoOutputsOnWrongJRE extends NearlyNoOutputs {

        public NearlyNoOutputsOnWrongJRE(String r) {
            super(r);
        }

        
        
        @Override
        public void evaluate(ProcessResult pr) {
            Assert.assertTrue(stdoutEmpty, removeJreVersionWarning(clean(pr.stdout)).length() == 0);
            Assert.assertTrue(stderrEmpty, removeJreVersionWarning(clean(pr.stderr)).length() == 0);

        }

    }

    private static final String pattern = ".*" + Translator.R("JREversionDontMatch", ".*", ".*") + ".*";
     

    private static String removeJreVersionWarning(String clean) {
        return clean.replaceAll(pattern, "");
    }

     @Test
     public void testJREversionDontMatchRemoval(){
         Assert.assertTrue(removeJreVersionWarning(Translator.R("JREversionDontMatch", "1.8.0-pre.whatever", "{0}")).isEmpty());
         Assert.assertTrue(removeJreVersionWarning(Translator.R("JREversionDontMatch", "{0}", "{1}")).isEmpty());
         Assert.assertTrue(removeJreVersionWarning(Translator.R("JREversionDontMatch", "1.3.0-pre-pac", "1.8.0-pre.whatever}")).isEmpty());
         Assert.assertTrue(removeJreVersionWarning(Translator.R("JREversionDontMatch", "", "")).isEmpty());
         Assert.assertTrue(removeJreVersionWarning(Translator.R("JREversionDontMatch", " - - - - ", " - - - ")).isEmpty());
         Assert.assertFalse(removeJreVersionWarning("AA\n"+Translator.R("JREversionDontMatch", "1.3+", "1.7")+"\nBB").equals("AA\nBB"));
     }

    public static class Arbores extends NearlyNoOutputsOnWrongJRE {

        public Arbores() {
            super("http://www.arbores.ca/AnnuityCalc.jnlp");
        }
    }

    public static class PhetSims extends NearlyNoOutputs {

        public PhetSims() {
            super("http://phetsims.colorado.edu/sims/circuit-construction-kit/circuit-construction-kit-dc_en.jnlp");
        }
    }

    public static class TopCoder extends NearlyNoOutputs {

        public TopCoder() {
            super("http://www.topcoder.com/contest/arena/ContestAppletProd.jnlp");
        }
    }

    public static class SunSwingDemo extends NearlyNoOutputs {

        public SunSwingDemo() throws MalformedURLException {
            super("http://java.sun.com/docs/books/tutorialJWS/uiswing/events/ex6/ComponentEventDemo.jnlp");
        }
    }

    public static class ArboresDeposit extends NearlyNoOutputsOnWrongJRE {

        public ArboresDeposit() throws MalformedURLException {
            super("http://www.arbores.ca/Deposit.jnlp");
        }
    }

    public static class AviationWeather extends StringBasedURL {

        @Override
        public void evaluate(ProcessResult pr) {
            Assert.assertTrue(stdoutEmpty, clean(pr.stdout).length() == 0);
            Assert.assertTrue(clean(pr.stderr).length() == 0 || (clean(pr.stderr).contains("Cannot read File Manager history data file,")
                    && pr.stderr.contains("FileMgr will be initialized with default options")));

        }

        public AviationWeather() {
            super("http://aviationweather.gov/static/adds/java/fpt/fpt.jnlp");
        }
    }

    public static class FuseSwing extends NearlyNoOutputs {

        public FuseSwing() {
            super("http://www.progx.org/users/Gfx/apps/fuse-swing-demo.jnlp");
        }
    }

    @Test
    public void remoteApplicationSettingsAreWorking() throws Exception {
        RemoteApplicationTestcaseSettings s5 = new FourierTransform();
        Assert.assertNotNull(s5.getUrl());
        RemoteApplicationTestcaseSettings s4 = new Arbores();
        Assert.assertNotNull(s4.getUrl());
        RemoteApplicationTestcaseSettings s3 = new PhetSims();
        Assert.assertNotNull(s3.getUrl());
        RemoteApplicationTestcaseSettings s2 = new TopCoder();
        Assert.assertNotNull(s2.getUrl());
        RemoteApplicationTestcaseSettings s1 = new SunSwingDemo();
        Assert.assertNotNull(s1.getUrl());
        RemoteApplicationTestcaseSettings s6 = new ArboresDeposit();
        Assert.assertNotNull(s6.getUrl());
        RemoteApplicationTestcaseSettings s7 = new OrawebCernCh();
        Assert.assertNotNull(s7.getUrl());
        RemoteApplicationTestcaseSettings s8 = new AviationWeather();
        Assert.assertNotNull(s8.getUrl());
        RemoteApplicationTestcaseSettings s9 = new FuseSwing();
        Assert.assertNotNull(s9.getUrl());
        RemoteApplicationTestcaseSettings s10 = new GnattProject();
        Assert.assertNotNull(s10.getUrl());
        RemoteApplicationTestcaseSettings s11 = new GeoGebra();
        Assert.assertNotNull(s11.getUrl());

    }
}
