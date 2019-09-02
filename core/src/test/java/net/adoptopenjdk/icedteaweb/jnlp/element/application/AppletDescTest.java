package net.adoptopenjdk.icedteaweb.jnlp.element.application;

import net.sourceforge.jnlp.util.logging.NoStdOutErrTest;
import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
import java.util.HashMap;


public class AppletDescTest extends NoStdOutErrTest {

    @Test
    public void testClassIsNotStrippedAccidentally() throws Exception {
        Assert.assertEquals("mainClass", createAppletWithMain("mainClass").getMainClass());
        Assert.assertEquals("main.Class", createAppletWithMain("main.Class").getMainClass());
        Assert.assertEquals("mainclass", createAppletWithMain("mainclass").getMainClass());
        Assert.assertEquals("mainAclass", createAppletWithMain("mainAclass").getMainClass());
        Assert.assertEquals("mainclassa", createAppletWithMain("mainclassa").getMainClass());
        Assert.assertEquals("main.classa", createAppletWithMain("main.classa").getMainClass());
    }

    @Test
    public void testClassIsStrippedProperly() throws Exception {
        Assert.assertEquals("main", createAppletWithMain("main.class").getMainClass());
    }

    private static AppletDesc createAppletWithMain(String mainClass) throws Exception {
        return new AppletDesc("appler", mainClass, new URL("http", "localhost", "doc"), 100, 100, new HashMap<>());
    }

    private static ApplicationDesc createApplicationWithMain(String mainClass) throws Exception {
        return new ApplicationDesc(mainClass, new String[0]);
    }

    @Test
    public void testClassIsNotStrippedAccidentallyApplication() throws Exception {
        Assert.assertEquals("mainClass", createApplicationWithMain("mainClass").getMainClass());
        Assert.assertEquals("main.Class", createApplicationWithMain("main.Class").getMainClass());
        Assert.assertEquals("mainclass", createApplicationWithMain("mainclass").getMainClass());
        Assert.assertEquals("mainAclass", createApplicationWithMain("mainAclass").getMainClass());
        Assert.assertEquals("mainclassa", createApplicationWithMain("mainclassa").getMainClass());
        Assert.assertEquals("main.classa", createApplicationWithMain("main.classa").getMainClass());
        Assert.assertEquals("main.class", createApplicationWithMain("main.class").getMainClass());
    }

}
