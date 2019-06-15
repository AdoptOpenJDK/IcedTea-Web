package net.sourceforge.jnlp.runtime;

import net.sourceforge.jnlp.config.Setting;
import org.junit.Test;

import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_SYSTEM_SECURITY_POLICY;

public class JNLPPolicyTest {

    @Test
    public void config_location_for_windows_loads() {
        final String fileURI = "file://C:/Users/philippe doussot/.config/icedtea-web/security/java.policy";
        System.setProperty(KEY_SYSTEM_SECURITY_POLICY, fileURI);
        JNLPRuntime.getConfiguration().getRaw().put(KEY_SYSTEM_SECURITY_POLICY,
                new Setting<>(KEY_SYSTEM_SECURITY_POLICY, "", false, null, KEY_SYSTEM_SECURITY_POLICY, fileURI, fileURI)
        );
        new JNLPPolicy();
    }

    @Test
    public void config_location_for_nix_loads() {
        final String fileURI = "file://a/b/c/java.policy";
        System.setProperty(KEY_SYSTEM_SECURITY_POLICY, fileURI);
        JNLPRuntime.getConfiguration().getRaw().put(KEY_SYSTEM_SECURITY_POLICY,
                new Setting<>(KEY_SYSTEM_SECURITY_POLICY, "", false, null, KEY_SYSTEM_SECURITY_POLICY, fileURI, fileURI)
        );
        new JNLPPolicy();
    }

    @Test
    public void config_location_for_uri_loads() {
        final String fileURI = "http://my:8080/policy/locationjava.policy";
        System.setProperty(KEY_SYSTEM_SECURITY_POLICY, fileURI);
        JNLPRuntime.getConfiguration().getRaw().put(KEY_SYSTEM_SECURITY_POLICY,
                new Setting<>(KEY_SYSTEM_SECURITY_POLICY, "", false, null, KEY_SYSTEM_SECURITY_POLICY, fileURI, fileURI)
        );
        new JNLPPolicy();
    }

}