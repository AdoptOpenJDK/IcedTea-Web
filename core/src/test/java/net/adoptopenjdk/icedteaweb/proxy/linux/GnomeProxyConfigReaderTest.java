package net.adoptopenjdk.icedteaweb.proxy.linux;

import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

import static net.adoptopenjdk.icedteaweb.proxy.linux.LinuxProxyProvider.LinuxProxyMode.MANUAL;
import static net.adoptopenjdk.icedteaweb.proxy.linux.LinuxProxyProvider.LinuxProxyMode.NO_PROXY;
import static net.adoptopenjdk.icedteaweb.proxy.linux.LinuxProxyProvider.LinuxProxyMode.PAC;
import static net.adoptopenjdk.icedteaweb.proxy.linux.LinuxProxyProviderConstants.GNOME_PROXY_AUTOCONFIG_URL;
import static net.adoptopenjdk.icedteaweb.proxy.linux.LinuxProxyProviderConstants.GNOME_PROXY_FTP_HOST;
import static net.adoptopenjdk.icedteaweb.proxy.linux.LinuxProxyProviderConstants.GNOME_PROXY_FTP_PORT;
import static net.adoptopenjdk.icedteaweb.proxy.linux.LinuxProxyProviderConstants.GNOME_PROXY_HTTPS_HOST;
import static net.adoptopenjdk.icedteaweb.proxy.linux.LinuxProxyProviderConstants.GNOME_PROXY_HTTPS_PORT;
import static net.adoptopenjdk.icedteaweb.proxy.linux.LinuxProxyProviderConstants.GNOME_PROXY_HTTP_PORT;
import static net.adoptopenjdk.icedteaweb.proxy.linux.LinuxProxyProviderConstants.GNOME_PROXY_IGNORE_HOSTS;
import static net.adoptopenjdk.icedteaweb.proxy.linux.LinuxProxyProviderConstants.GNOME_PROXY_MODE;
import static net.adoptopenjdk.icedteaweb.proxy.linux.LinuxProxyProviderConstants.GNOME_PROXY_SOCKS_HOST;
import static net.adoptopenjdk.icedteaweb.proxy.linux.LinuxProxyProviderConstants.GNOME_PROXY_SOCKS_PORT;

/**
 * ...
 */
public class GnomeProxyConfigReaderTest {

    public static final String SAMPLE_OUTPUT = "" +
            "org.gnome.system.proxy use-same-proxy true\n" +
            "org.gnome.system.proxy mode 'none'\n" +
            "org.gnome.system.proxy autoconfig-url '/tmp/foo/pac.js'\n" +
            "org.gnome.system.proxy ignore-hosts ['localhost', '127.0.0.0/8', '::1']\n" +
            "org.gnome.system.proxy.ftp host ''\n" +
            "org.gnome.system.proxy.ftp port 0\n" +
            "org.gnome.system.proxy.socks host ''\n" +
            "org.gnome.system.proxy.socks port 0\n" +
            "org.gnome.system.proxy.http host '127.0.0.2'\n" +
            "org.gnome.system.proxy.http port 8080\n" +
            "org.gnome.system.proxy.http use-authentication false\n" +
            "org.gnome.system.proxy.http authentication-password ''\n" +
            "org.gnome.system.proxy.http authentication-user ''\n" +
            "org.gnome.system.proxy.http enabled false\n" +
            "org.gnome.system.proxy.https host ''\n" +
            "org.gnome.system.proxy.https port 0\n";

    @Test
    public void parseGnomeSettings() {
        // when
        final Map<String, String> result = GnomeProxyConfigReader.parseGnomeSettings(SAMPLE_OUTPUT);

        // then
        Assert.assertEquals("'none'", result.get(GNOME_PROXY_MODE));
        Assert.assertEquals("'/tmp/foo/pac.js'", result.get(GNOME_PROXY_AUTOCONFIG_URL));
        Assert.assertEquals("['localhost', '127.0.0.0/8', '::1']", result.get(GNOME_PROXY_IGNORE_HOSTS));
        Assert.assertEquals("8080", result.get(GNOME_PROXY_HTTP_PORT));
    }

    @Test
    public void convertToSettingsForNoProxy() throws MalformedURLException {
        // given
        final Map<String, String> values = GnomeProxyConfigReader.parseGnomeSettings(SAMPLE_OUTPUT);

        // when
        final LinuxProxySettings result = GnomeProxyConfigReader.convertToSettings(values);

        // then
        Assert.assertEquals(NO_PROXY, result.getMode());
    }

    @Test
    public void convertToSettingsForPacProxy() throws MalformedURLException {
        // given
        final Map<String, String> values = GnomeProxyConfigReader.parseGnomeSettings(SAMPLE_OUTPUT);
        values.put(GNOME_PROXY_MODE, "'auto'");
        values.put(GNOME_PROXY_AUTOCONFIG_URL, "'file:///tmp/foo/pac.js'");

        // when
        final LinuxProxySettings result = GnomeProxyConfigReader.convertToSettings(values);

        // then
        Assert.assertEquals(PAC, result.getMode());
        Assert.assertEquals(new URL("file:///tmp/foo/pac.js"), result.getAutoConfigUrl());
    }

    @Test
    public void convertToSettingsForManualProxyOnlyHttpSet() throws MalformedURLException {
        // given
        final Map<String, String> values = GnomeProxyConfigReader.parseGnomeSettings(SAMPLE_OUTPUT);
        values.put(GNOME_PROXY_MODE, "'manual'");

        // when
        final LinuxProxySettings result = GnomeProxyConfigReader.convertToSettings(values);

        // then
        Assert.assertEquals(MANUAL, result.getMode());
        Assert.assertFalse(result.isSocksEnabled());
        Assert.assertFalse(result.isFtpEnabled());
        Assert.assertTrue(result.isHttpEnabled());
        Assert.assertTrue(result.isHttpsEnabled());
        Assert.assertEquals("127.0.0.2", result.getHttpHost());
        Assert.assertEquals("127.0.0.2", result.getHttpsHost());
        Assert.assertEquals(8080, result.getHttpPort());
        Assert.assertEquals(8080, result.getHttpsPort());
    }

    @Test
    public void convertToSettingsForManualProxyOnlySocksSet() throws MalformedURLException {
        // given
        final Map<String, String> values = GnomeProxyConfigReader.parseGnomeSettings(SAMPLE_OUTPUT);
        values.put(GNOME_PROXY_MODE, "'manual'");
        values.put(GNOME_PROXY_HTTP_PORT, "0");
        values.put(GNOME_PROXY_SOCKS_HOST, "'127.1.1.1'");
        values.put(GNOME_PROXY_SOCKS_PORT, "8081");

        // when
        final LinuxProxySettings result = GnomeProxyConfigReader.convertToSettings(values);

        // then
        Assert.assertEquals(MANUAL, result.getMode());
        Assert.assertTrue(result.isSocksEnabled());
        Assert.assertTrue(result.isFtpEnabled());
        Assert.assertTrue(result.isHttpEnabled());
        Assert.assertTrue(result.isHttpsEnabled());
        Assert.assertEquals("127.1.1.1", result.getSocksHost());
        Assert.assertEquals("127.1.1.1", result.getFtpHost());
        Assert.assertEquals("127.1.1.1", result.getHttpHost());
        Assert.assertEquals("127.1.1.1", result.getHttpsHost());
        Assert.assertEquals(8081, result.getSocksPort());
        Assert.assertEquals(8081, result.getFtpPort());
        Assert.assertEquals(8081, result.getHttpPort());
        Assert.assertEquals(8081, result.getHttpsPort());
    }

    @Test
    public void convertToSettingsForManualProxyAllSet() throws MalformedURLException {
        // given
        final Map<String, String> values = GnomeProxyConfigReader.parseGnomeSettings(SAMPLE_OUTPUT);
        values.put(GNOME_PROXY_MODE, "'manual'");
        values.put(GNOME_PROXY_SOCKS_HOST, "'127.1.1.1'");
        values.put(GNOME_PROXY_SOCKS_PORT, "8081");
        values.put(GNOME_PROXY_FTP_HOST, "'127.1.1.2'");
        values.put(GNOME_PROXY_FTP_PORT, "8082");
        values.put(GNOME_PROXY_HTTPS_HOST, "'127.1.1.3'");
        values.put(GNOME_PROXY_HTTPS_PORT, "8083");

        // when
        final LinuxProxySettings result = GnomeProxyConfigReader.convertToSettings(values);

        // then
        Assert.assertEquals(MANUAL, result.getMode());
        Assert.assertTrue(result.isSocksEnabled());
        Assert.assertTrue(result.isFtpEnabled());
        Assert.assertTrue(result.isHttpEnabled());
        Assert.assertTrue(result.isHttpsEnabled());
        Assert.assertEquals("127.1.1.1", result.getSocksHost());
        Assert.assertEquals("127.1.1.2", result.getFtpHost());
        Assert.assertEquals("127.0.0.2", result.getHttpHost());
        Assert.assertEquals("127.1.1.3", result.getHttpsHost());
        Assert.assertEquals(8081, result.getSocksPort());
        Assert.assertEquals(8082, result.getFtpPort());
        Assert.assertEquals(8080, result.getHttpPort());
        Assert.assertEquals(8083, result.getHttpsPort());
    }

    @Test
    public void convertToSettingsForDefaultExclusionList() throws MalformedURLException {
        // given
        final Map<String, String> values = GnomeProxyConfigReader.parseGnomeSettings(SAMPLE_OUTPUT);
        values.put(GNOME_PROXY_MODE, "'manual'");

        // when
        final LinuxProxySettings result = GnomeProxyConfigReader.convertToSettings(values);

        // then
        Assert.assertEquals(Arrays.asList("localhost", "127.0.0.0/8", "::1"), result.getExceptionList());
        Assert.assertTrue(result.isLocalhostExcluded());
    }

    @Test
    public void convertToSettingsForManualExclusionList() throws MalformedURLException {
        // given
        final Map<String, String> values = GnomeProxyConfigReader.parseGnomeSettings(SAMPLE_OUTPUT);
        values.put(GNOME_PROXY_MODE, "'manual'");
        values.put(GNOME_PROXY_IGNORE_HOSTS, "['google.com', '192.168.1.1']");

        // when
        final LinuxProxySettings result = GnomeProxyConfigReader.convertToSettings(values);

        // then
        Assert.assertEquals(Arrays.asList("google.com", "192.168.1.1"), result.getExceptionList());
        Assert.assertFalse(result.isLocalhostExcluded());
    }

}
