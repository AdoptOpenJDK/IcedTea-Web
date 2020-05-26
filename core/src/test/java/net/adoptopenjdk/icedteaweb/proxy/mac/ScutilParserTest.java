package net.adoptopenjdk.icedteaweb.proxy.mac;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class ScutilParserTest {

    @Test
    public void testParse() {

        //given:
        final List<String> lines = readLines("out1.txt");

        //when:
        final MacProxySettings proxySettings = ScutilUtil.parse(lines);

        //then:
        Assert.assertNotNull(proxySettings);
        Assert.assertTrue(proxySettings.isHttpEnabled());
        Assert.assertEquals("example.proxy", proxySettings.getHttpHost());
        Assert.assertEquals(80, proxySettings.getHttpPort());
        Assert.assertTrue(proxySettings.isHttpsEnabled());
        Assert.assertEquals("example.https.proxy", proxySettings.getHttpsHost());
        Assert.assertEquals(88, proxySettings.getHttpsPort());
        Assert.assertFalse(proxySettings.isFtpEnabled());
        Assert.assertFalse(proxySettings.isSocksEnabled());
        Assert.assertFalse(proxySettings.isAutoDiscoveryEnabled());
        Assert.assertFalse(proxySettings.isAutoConfigEnabled());
        Assert.assertFalse(proxySettings.isExcludeSimpleHostnames());
    }

    @Test
    public void testParseWithList() {

        //given:
        final List<String> lines = readLines("out2.txt");

        //when:
        final MacProxySettings proxySettings = ScutilUtil.parse(lines);

        //then:
        Assert.assertNotNull(proxySettings);
        Assert.assertFalse(proxySettings.getExceptionList().isEmpty());
        Assert.assertTrue(proxySettings.getExceptionList().contains("*.local"));
        Assert.assertTrue(proxySettings.getExceptionList().contains("169.254/16"));
    }

    private List<String> readLines(final String file) {
        final List<String> lines = new ArrayList<>();
        try (final Scanner sc = new Scanner(ScutilParserTest.class.getResourceAsStream(file))) {
            while (sc.hasNextLine()) {
                lines.add(sc.nextLine());
            }
        }
        return Collections.unmodifiableList(lines);
    }
}
