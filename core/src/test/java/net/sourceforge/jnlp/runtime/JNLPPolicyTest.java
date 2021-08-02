package net.sourceforge.jnlp.runtime;

import net.sourceforge.jnlp.util.logging.OutputController;
import net.sourceforge.jnlp.util.logging.StdInOutErrController;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_SYSTEM_SECURITY_POLICY;

public class JNLPPolicyTest {

    private static ByteArrayOutputStream logCapture;

    @BeforeClass
    public static void setUpClass() {
        logCapture = new ByteArrayOutputStream();
        OutputController.getLogger().setInOutErrController(new StdInOutErrController(logCapture, logCapture));
    }

    @AfterClass
    public static void tearDownClass() {
        OutputController.getLogger().setInOutErrController(StdInOutErrController.getInstance());
    }

    @Test
    public void config_location_for_windows_loads() throws Exception {
        final String fileURI = "file://C:/Users/philippe doussot/.config/icedtea-web/security/java.policy";
        System.setProperty(KEY_SYSTEM_SECURITY_POLICY, fileURI);
        JNLPRuntime.getConfiguration().setProperty(KEY_SYSTEM_SECURITY_POLICY, fileURI);
        new JNLPPolicy();

        System.out.println(logCapture.toString(UTF_8.name()));
        Assert.assertFalse(logCapture.toString(UTF_8.name()).contains("ERROR"));
    }

    @Test
    public void config_location_for_nix_loads() throws Exception {
        final String fileURI = "file://a/b/c/java.policy";
        System.setProperty(KEY_SYSTEM_SECURITY_POLICY, fileURI);
        JNLPRuntime.getConfiguration().setProperty(KEY_SYSTEM_SECURITY_POLICY, fileURI);
        new JNLPPolicy();

        System.out.println(logCapture.toString(UTF_8.name()));
        Assert.assertFalse(logCapture.toString(UTF_8.name()).contains("ERROR"));
    }

    @Test
    public void config_location_for_nix_loads_with_space() throws Exception {
        final String fileURI = "file://a/b b/c/java.policy";
        System.setProperty(KEY_SYSTEM_SECURITY_POLICY, fileURI);
        JNLPRuntime.getConfiguration().setProperty(KEY_SYSTEM_SECURITY_POLICY, fileURI);
        new JNLPPolicy();

        System.out.println(logCapture.toString(UTF_8.name()));
        Assert.assertFalse(logCapture.toString(UTF_8.name()).contains("ERROR"));
    }

    @Test
    public void config_location_for_uri_loads() throws Exception {
        final String fileURI = "http://my:8080/policy/locationjava.policy";
        System.setProperty(KEY_SYSTEM_SECURITY_POLICY, fileURI);
        JNLPRuntime.getConfiguration().setProperty(KEY_SYSTEM_SECURITY_POLICY, fileURI);
        new JNLPPolicy();

        System.out.println(logCapture.toString(UTF_8.name()));
        Assert.assertFalse(logCapture.toString(UTF_8.name()).contains("ERROR"));
    }

}
