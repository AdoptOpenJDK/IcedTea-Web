package net.adoptopenjdk.icedteaweb.proxy.ie;

import net.adoptopenjdk.icedteaweb.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class RegistryQueryTest {

    @Test
    public void getRegistryValuesFromLines() throws Exception{
        //Given
        final String key = "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings";
        final String content = IOUtils.readContentAsUtf8String(RegistryQueryTest.class.getResourceAsStream("reg_query.out"));
        final List<String> lines = Arrays.asList(content.split(System.lineSeparator()));

        //When
        final Set<RegistryValue> values = RegistryQuery.getRegistryValuesFromLines(key, lines);

        //Then
        Assert.assertEquals(14, values.size());

        Assert.assertNotNull(findByName(values, "DisableCachingOfSSLPages"));
        Assert.assertEquals(RegistryValueType.REG_DWORD, findByName(values, "DisableCachingOfSSLPages").getType());
        Assert.assertEquals("0x0", findByName(values, "DisableCachingOfSSLPages").getValue());
        Assert.assertFalse(findByName(values, "DisableCachingOfSSLPages").getValueAsBoolean());

        Assert.assertNotNull(findByName(values, "MigrateProxy"));
        Assert.assertEquals(RegistryValueType.REG_DWORD, findByName(values, "MigrateProxy").getType());
        Assert.assertEquals("0x1", findByName(values, "MigrateProxy").getValue());
        Assert.assertTrue(findByName(values, "MigrateProxy").getValueAsBoolean());

        Assert.assertNotNull(findByName(values, "ProxyServer"));
        Assert.assertEquals(RegistryValueType.REG_SZ, findByName(values, "ProxyServer").getType());
        Assert.assertEquals("loooocalhost:80", findByName(values, "ProxyServer").getValue());
    }

    private RegistryValue findByName(Collection<RegistryValue> values, String name) {
        return values.stream().filter(v -> Objects.equals(v.getName(), name)).findAny().orElse(null);
    }
}