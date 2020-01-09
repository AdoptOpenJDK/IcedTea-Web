package net.adoptopenjdk.icedteaweb.proxy.ie;

import net.adoptopenjdk.icedteaweb.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RegistryQueryTest {

    @Test
    public void getRegistryValuesFromLines() throws Exception{
        //Given
        final String key = "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings";
        final String content = IOUtils.readContentAsUtf8String(RegistryQueryTest.class.getResourceAsStream("reg_query.out"));
        final List<String> lines = Arrays.asList(content.split("\\R"));

        //When
        final Map<String, RegistryValue> values = RegistryQuery.getRegistryValuesFromLines(key, lines);

        //Then
        Assert.assertEquals(14, values.size());

        final RegistryValue disableCachingOfSSLPages = values.get("DisableCachingOfSSLPages");
        Assert.assertNotNull(disableCachingOfSSLPages);
        Assert.assertEquals(RegistryValueType.REG_DWORD, disableCachingOfSSLPages.getType());
        Assert.assertEquals("0x0", disableCachingOfSSLPages.getValue());
        Assert.assertFalse(disableCachingOfSSLPages.getValueAsBoolean());

        final RegistryValue migrateProxy = values.get("MigrateProxy");
        Assert.assertNotNull(migrateProxy);
        Assert.assertEquals(RegistryValueType.REG_DWORD, migrateProxy.getType());
        Assert.assertEquals("0x1", migrateProxy.getValue());
        Assert.assertTrue(migrateProxy.getValueAsBoolean());

        final RegistryValue proxyServer = values.get("ProxyServer");
        Assert.assertNotNull(proxyServer);
        Assert.assertEquals(RegistryValueType.REG_SZ, proxyServer.getType());
        Assert.assertEquals("loooocalhost:80", proxyServer.getValue());

        final RegistryValue user_agent = values.get("User Agent");
        Assert.assertNotNull(user_agent);
        Assert.assertEquals(RegistryValueType.REG_SZ, user_agent.getType());
        Assert.assertEquals("Mozilla/4.0 (compatible; MSIE 8.0; Win32)", user_agent.getValue());
    }
}
