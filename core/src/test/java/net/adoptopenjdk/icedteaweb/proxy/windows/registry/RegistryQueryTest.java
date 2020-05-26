package net.adoptopenjdk.icedteaweb.proxy.windows.registry;

import net.adoptopenjdk.icedteaweb.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RegistryQueryTest {

    @Test
    public void getRegistryValuesFromLines() throws Exception {
        //Given
        final String key = "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings";
        final String content = IOUtils.readContentAsUtf8String(RegistryQueryTest.class.getResourceAsStream("reg_query.out"));
        final List<String> lines = Arrays.asList(content.split(System.lineSeparator()));

        //When
        final Map<String, RegistryValue> values = RegistryQuery.getRegistryValuesFromLines(key, lines);

        //Then
        Assert.assertEquals(14, values.size());

        Assert.assertNotNull(values.get("DisableCachingOfSSLPages"));
        Assert.assertEquals(RegistryValueType.REG_DWORD, values.get("DisableCachingOfSSLPages").getType());
        Assert.assertEquals("0x0", values.get("DisableCachingOfSSLPages").getValue());
        Assert.assertFalse(values.get("DisableCachingOfSSLPages").getValueAsBoolean());

        Assert.assertNotNull(values.get("MigrateProxy"));
        Assert.assertEquals(RegistryValueType.REG_DWORD, values.get("MigrateProxy").getType());
        Assert.assertEquals("0x1", values.get("MigrateProxy").getValue());
        Assert.assertTrue(values.get("MigrateProxy").getValueAsBoolean());

        Assert.assertNotNull(values.get("ProxyServer"));
        Assert.assertEquals(RegistryValueType.REG_SZ, values.get("ProxyServer").getType());
        Assert.assertEquals("loooocalhost:80", values.get("ProxyServer").getValue());

        Assert.assertNotNull(values.get("AutoConfigURL"));
        Assert.assertEquals(RegistryValueType.REG_SZ, values.get("AutoConfigURL").getType());
        Assert.assertEquals("huhu", values.get("AutoConfigURL").getValue());
    }

    @Test
    public void getRegistryValuesFromLinesWithNullValue() throws Exception {
        //Given
        final String key = "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings";
        final String content = IOUtils.readContentAsUtf8String(RegistryQueryTest.class.getResourceAsStream("reg_query_with_null_value.out"));
        final List<String> lines = Arrays.asList(content.split(System.lineSeparator()));

        //When
        final Map<String, RegistryValue> values = RegistryQuery.getRegistryValuesFromLines(key, lines);

        //Then
        Assert.assertEquals(1, values.size());

        Assert.assertNotNull(values.get("AutoConfigURL"));
        Assert.assertEquals(RegistryValueType.REG_SZ, values.get("AutoConfigURL").getType());
        Assert.assertNull(values.get("AutoConfigURL").getValue());
    }

}
