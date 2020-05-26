package net.adoptopenjdk.icedteaweb.proxy.windows;

import net.adoptopenjdk.icedteaweb.proxy.ProxyProvider;
import net.adoptopenjdk.icedteaweb.proxy.direct.DirectProxyProvider;
import net.adoptopenjdk.icedteaweb.proxy.pac.PacBasedProxyProvider;
import net.adoptopenjdk.icedteaweb.proxy.windows.registry.RegistryQueryResult;
import net.adoptopenjdk.icedteaweb.proxy.windows.registry.RegistryValue;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import org.junit.Assert;
import org.junit.Test;

import java.net.Proxy;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static net.adoptopenjdk.icedteaweb.proxy.windows.WindowsProxyConstants.AUTO_CONFIG_URL_VAL;
import static net.adoptopenjdk.icedteaweb.proxy.windows.WindowsProxyConstants.PROXY_ENABLED_VAL;
import static net.adoptopenjdk.icedteaweb.proxy.windows.WindowsProxyConstants.PROXY_SERVER_REGISTRY_VAL;
import static net.adoptopenjdk.icedteaweb.proxy.windows.registry.RegistryValueType.REG_DWORD;
import static net.adoptopenjdk.icedteaweb.proxy.windows.registry.RegistryValueType.REG_SZ;

public class WindowsProxyUtilsTest {

    @Test
    public void createInternalProxyWithPac() throws Exception {
        //given
        final DeploymentConfiguration config = new DeploymentConfiguration();
        final Map<String, RegistryValue> proxyRegistryEntries = new HashMap<>();
        final String pacUrl = WindowsProxyUtilsTest.class.getResource("simple-pac.js").toExternalForm();
        proxyRegistryEntries.put(AUTO_CONFIG_URL_VAL, new RegistryValue(AUTO_CONFIG_URL_VAL, REG_SZ, pacUrl));
        final RegistryQueryResult queryResult = new RegistryQueryResult(proxyRegistryEntries);

        //when
        final ProxyProvider proxyProvider = WindowsProxyUtils.createInternalProxy(config, queryResult);

        //than
        Assert.assertNotNull(proxyProvider);
        Assert.assertTrue(proxyProvider instanceof PacBasedProxyProvider);
        Assert.assertEquals(Collections.singletonList(Proxy.NO_PROXY), proxyProvider.select(new URI("http://some-url")));
    }

    @Test
    public void createInternalProxyWithoutPac() throws Exception {
        //given
        final DeploymentConfiguration config = new DeploymentConfiguration();
        final Map<String, RegistryValue> proxyRegistryEntries = new HashMap<>();
        proxyRegistryEntries.put(AUTO_CONFIG_URL_VAL, new RegistryValue(AUTO_CONFIG_URL_VAL, REG_SZ, null));
        proxyRegistryEntries.put(PROXY_ENABLED_VAL, new RegistryValue(PROXY_ENABLED_VAL, REG_DWORD, "0x0"));
        final RegistryQueryResult queryResult = new RegistryQueryResult(proxyRegistryEntries);

        //when
        final ProxyProvider proxyProvider = WindowsProxyUtils.createInternalProxy(config, queryResult);

        //than
        Assert.assertNotNull(proxyProvider);
        Assert.assertEquals(DirectProxyProvider.getInstance(), proxyProvider);
    }

    @Test
    public void createInternalProxyWithoutPac2() throws Exception {
        //given
        final DeploymentConfiguration config = new DeploymentConfiguration();
        final Map<String, RegistryValue> proxyRegistryEntries = new HashMap<>();
        proxyRegistryEntries.put(PROXY_ENABLED_VAL, new RegistryValue(PROXY_ENABLED_VAL, REG_DWORD, "0x0"));
        final RegistryQueryResult queryResult = new RegistryQueryResult(proxyRegistryEntries);

        //when
        final ProxyProvider proxyProvider = WindowsProxyUtils.createInternalProxy(config, queryResult);

        //than
        Assert.assertNotNull(proxyProvider);
        Assert.assertEquals(DirectProxyProvider.getInstance(), proxyProvider);
    }

    @Test
    public void createInternalProxyWithoutProxyUrl() throws Exception {
        //given
        final DeploymentConfiguration config = new DeploymentConfiguration();
        final Map<String, RegistryValue> proxyRegistryEntries = new HashMap<>();
        proxyRegistryEntries.put(PROXY_ENABLED_VAL, new RegistryValue(PROXY_ENABLED_VAL, REG_DWORD, "0x1"));
        final RegistryQueryResult queryResult = new RegistryQueryResult(proxyRegistryEntries);

        //when
        final ProxyProvider proxyProvider = WindowsProxyUtils.createInternalProxy(config, queryResult);

        //than
        Assert.assertNotNull(proxyProvider);
        Assert.assertEquals(DirectProxyProvider.getInstance(), proxyProvider);
    }

    @Test
    public void createInternalProxyWithoutProxyUrl2() throws Exception {
        //given
        final DeploymentConfiguration config = new DeploymentConfiguration();
        final Map<String, RegistryValue> proxyRegistryEntries = new HashMap<>();
        proxyRegistryEntries.put(PROXY_ENABLED_VAL, new RegistryValue(PROXY_ENABLED_VAL, REG_DWORD, "0x1"));
        proxyRegistryEntries.put(PROXY_SERVER_REGISTRY_VAL, new RegistryValue(PROXY_SERVER_REGISTRY_VAL, REG_SZ, null));
        final RegistryQueryResult queryResult = new RegistryQueryResult(proxyRegistryEntries);

        //when
        final ProxyProvider proxyProvider = WindowsProxyUtils.createInternalProxy(config, queryResult);

        //than
        Assert.assertNotNull(proxyProvider);
        Assert.assertEquals(DirectProxyProvider.getInstance(), proxyProvider);
    }
}
