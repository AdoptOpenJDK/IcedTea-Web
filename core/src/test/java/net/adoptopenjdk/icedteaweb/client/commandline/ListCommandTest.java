// Copyright (C) 2019 Karakun AG
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

package net.adoptopenjdk.icedteaweb.client.commandline;

import net.adoptopenjdk.icedteaweb.commandline.CommandLineOptions;
import org.junit.Test;

import java.util.Arrays;

import static net.adoptopenjdk.icedteaweb.client.commandline.CommandLine.SUCCESS;
import static net.sourceforge.jnlp.config.ConfigurationConstants.IGNORE_HEADLESS_CHECK;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_AUTO_DOWNLOAD_JRE;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_BROWSER_PATH;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_CACHE_COMPRESSION_ENABLED;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_CACHE_ENABLED;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_CACHE_MAX_SIZE;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_CONSOLE_STARTUP_MODE;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_CREATE_DESKTOP_SHORTCUT;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_ENABLE_APPLICATION_LOGGING_TOFILE;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_ENABLE_LEGACY_LOGBASEDFILELOG;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_ENABLE_LOGGING;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_ENABLE_LOGGING_HEADERS;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_ENABLE_LOGGING_TOFILE;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_ENABLE_LOGGING_TOSTREAMS;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_ENABLE_LOGGING_TOSYSTEMLOG;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_HTTPS_DONT_ENFORCE;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_JNLP_ASSOCIATIONS;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_JRE_DIR;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_JRE_INTSTALL_URL;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_LAUNCHER_RUST_BOOTCP_ADD;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_LAUNCHER_RUST_BOOTCP_REMOVE;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_LAUNCHER_RUST_CP_ADD;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_LAUNCHER_RUST_CP_REMOVE;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PLUGIN_JVM_ARGUMENTS;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_AUTO_CONFIG_URL;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_BYPASS_LIST;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_BYPASS_LOCAL;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_FTP_HOST;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_FTP_PORT;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_HTTPS_HOST;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_HTTPS_PORT;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_HTTP_HOST;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_HTTP_PORT;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_OVERRIDE_HOSTS;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_SAME;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_SOCKS4_HOST;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_SOCKS4_PORT;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_PROXY_TYPE;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_SECURITY_ALLOW_HIDE_WINDOW_WARNING;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_SECURITY_ASKGRANTDIALOG_NOTINCA;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_SECURITY_EXPIRED_WARNING;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_SECURITY_ITW_IGNORECERTISSUES;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_SECURITY_JSSE_HOSTMISMATCH_WARNING;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_SECURITY_LEVEL;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_SECURITY_NOTINCA_WARNING;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_SECURITY_PROMPT_USER;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_SECURITY_PROMPT_USER_FOR_JNLP;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_SECURITY_TRUSTED_POLICY;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_SMALL_SIZE_OVERRIDE_HEIGHT;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_SMALL_SIZE_OVERRIDE_THRESHOLD;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_SMALL_SIZE_OVERRIDE_WIDTH;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_STRICT_JNLP_CLASSLOADER;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_SYSTEM_CACHE_DIR;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_SYSTEM_CONFIG;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_SYSTEM_CONFIG_MANDATORY;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_SYSTEM_SECURITY_POLICY;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_SYSTEM_TRUSTED_CA_CERTS;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_SYSTEM_TRUSTED_CERTS;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_SYSTEM_TRUSTED_CLIENT_CERTS;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_SYSTEM_TRUSTED_JSSE_CA_CERTS;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_SYSTEM_TRUSTED_JSSE_CERTS;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_UPDATE_TIMEOUT;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_USER_CACHE_DIR;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_USER_LOCKS_DIR;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_USER_LOG_DIR;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_USER_NETX_RUNNING_FILE;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_USER_PERSISTENCE_CACHE_DIR;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_USER_SECURITY_POLICY;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_USER_TMP_DIR;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_USER_TRUSTED_CA_CERTS;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_USER_TRUSTED_CERTS;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_USER_TRUSTED_CLIENT_CERTS;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_USER_TRUSTED_JSSE_CA_CERTS;
import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_USER_TRUSTED_JSSE_CERTS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;

/**
 * Test the Iced-tea web settings commands.
 */
public class ListCommandTest extends AbstractCommandTest {
    /**
     * Test whether the {@code -list}, command executes and terminates with {@link CommandLine#SUCCESS}.
     */
    @Test
    public void testListCommand() {
        // GIVEN -----------
        final String[] args = {"-list"}; // use literals for readability

        // test if literals still backed by constants
        assertThat(Arrays.asList(args), contains(CommandLineOptions.LIST.getOption()));

        // WHEN ------------
        final int status = getCommandLine(args).handle();

        // THEN ------------
        assertEquals(SUCCESS, status);

        // Networking related settings
        assertThat(getOutContent(), containsString(KEY_PROXY_TYPE));
        assertThat(getOutContent(), containsString(KEY_PROXY_SAME));
        assertThat(getOutContent(), containsString(KEY_PROXY_AUTO_CONFIG_URL));
        assertThat(getOutContent(), containsString(KEY_PROXY_BYPASS_LIST));
        assertThat(getOutContent(), containsString(KEY_PROXY_BYPASS_LOCAL));
        assertThat(getOutContent(), containsString(KEY_PROXY_HTTP_HOST));
        assertThat(getOutContent(), containsString(KEY_PROXY_HTTP_PORT));
        assertThat(getOutContent(), containsString(KEY_PROXY_HTTPS_HOST));
        assertThat(getOutContent(), containsString(KEY_PROXY_HTTPS_PORT));
        assertThat(getOutContent(), containsString(KEY_PROXY_FTP_HOST));
        assertThat(getOutContent(), containsString(KEY_PROXY_FTP_PORT));
        assertThat(getOutContent(), containsString(KEY_PROXY_SOCKS4_HOST));
        assertThat(getOutContent(), containsString(KEY_PROXY_SOCKS4_PORT));
        assertThat(getOutContent(), containsString(KEY_PROXY_OVERRIDE_HOSTS));

        // Logging related settings
        assertThat(getOutContent(), containsString(KEY_USER_LOG_DIR));
        assertThat(getOutContent(), containsString(KEY_ENABLE_LOGGING));
        assertThat(getOutContent(), containsString(KEY_ENABLE_LOGGING_HEADERS));
        assertThat(getOutContent(), containsString(KEY_ENABLE_LOGGING_TOFILE));
        assertThat(getOutContent(), containsString(KEY_ENABLE_LOGGING_TOSTREAMS));
        assertThat(getOutContent(), containsString(KEY_ENABLE_LOGGING_TOSYSTEMLOG));
        assertThat(getOutContent(), containsString(KEY_ENABLE_APPLICATION_LOGGING_TOFILE));
        assertThat(getOutContent(), containsString(KEY_ENABLE_LEGACY_LOGBASEDFILELOG));

        // Manifest related settings
        assertThat(getOutContent(), containsString(KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK));

        // Console related settings
        assertThat(getOutContent(), containsString(KEY_CONSOLE_STARTUP_MODE));

        // Security and access control related settings
        assertThat(getOutContent(), containsString(KEY_SECURITY_PROMPT_USER));
        assertThat(getOutContent(), containsString(KEY_SECURITY_LEVEL));
        assertThat(getOutContent(), containsString(KEY_SECURITY_TRUSTED_POLICY));
        assertThat(getOutContent(), containsString(KEY_SECURITY_ALLOW_HIDE_WINDOW_WARNING));
        assertThat(getOutContent(), containsString(KEY_SECURITY_PROMPT_USER_FOR_JNLP));
        assertThat(getOutContent(), containsString(KEY_SECURITY_ITW_IGNORECERTISSUES));
        assertThat(getOutContent(), containsString(KEY_SECURITY_EXPIRED_WARNING));
        assertThat(getOutContent(), containsString(KEY_SECURITY_JSSE_HOSTMISMATCH_WARNING));
        assertThat(getOutContent(), containsString(KEY_SECURITY_NOTINCA_WARNING));
        assertThat(getOutContent(), containsString(KEY_SECURITY_ASKGRANTDIALOG_NOTINCA));
        assertThat(getOutContent(), containsString(KEY_STRICT_JNLP_CLASSLOADER));
        assertThat(getOutContent(), containsString(KEY_HTTPS_DONT_ENFORCE));
        assertThat(getOutContent(), containsString(KEY_USER_SECURITY_POLICY));
        assertThat(getOutContent(), containsString(KEY_USER_TRUSTED_CA_CERTS));
        assertThat(getOutContent(), containsString(KEY_USER_TRUSTED_JSSE_CERTS));
        assertThat(getOutContent(), containsString(KEY_USER_TRUSTED_CERTS));
        assertThat(getOutContent(), containsString(KEY_USER_TRUSTED_JSSE_CA_CERTS));
        assertThat(getOutContent(), containsString(KEY_USER_TRUSTED_CLIENT_CERTS));
        assertThat(getOutContent(), containsString(KEY_SYSTEM_SECURITY_POLICY));
        assertThat(getOutContent(), containsString(KEY_SYSTEM_TRUSTED_CA_CERTS));
        assertThat(getOutContent(), containsString(KEY_SYSTEM_TRUSTED_JSSE_CA_CERTS));
        assertThat(getOutContent(), containsString(KEY_SYSTEM_TRUSTED_CERTS));
        assertThat(getOutContent(), containsString(KEY_SYSTEM_TRUSTED_JSSE_CERTS));
        assertThat(getOutContent(), containsString(KEY_SYSTEM_TRUSTED_CLIENT_CERTS));

        // Desktop integration related settings
        assertThat(getOutContent(), containsString(KEY_JNLP_ASSOCIATIONS));
        assertThat(getOutContent(), containsString(KEY_CREATE_DESKTOP_SHORTCUT));
        assertThat(getOutContent(), containsString(KEY_JRE_INTSTALL_URL));
        assertThat(getOutContent(), containsString(KEY_AUTO_DOWNLOAD_JRE));
        assertThat(getOutContent(), containsString(KEY_BROWSER_PATH));
        assertThat(getOutContent(), containsString(KEY_UPDATE_TIMEOUT));
        assertThat(getOutContent(), containsString(IGNORE_HEADLESS_CHECK));

        // JVM related settings
        assertThat(getOutContent(), containsString(KEY_PLUGIN_JVM_ARGUMENTS));
        assertThat(getOutContent(), containsString(KEY_JRE_DIR));

        // Remote configuration related settings
        assertThat(getOutContent(), containsString(KEY_SYSTEM_CONFIG));
        assertThat(getOutContent(), containsString(KEY_SYSTEM_CONFIG_MANDATORY));

        // Applet size control settings
        assertThat(getOutContent(), containsString(KEY_SMALL_SIZE_OVERRIDE_WIDTH));
        assertThat(getOutContent(), containsString(KEY_SMALL_SIZE_OVERRIDE_HEIGHT));
        assertThat(getOutContent(), containsString(KEY_SMALL_SIZE_OVERRIDE_THRESHOLD));

        // Cache related settings
        assertThat(getOutContent(), containsString(KEY_USER_CACHE_DIR));
        assertThat(getOutContent(), containsString(KEY_USER_PERSISTENCE_CACHE_DIR));
        assertThat(getOutContent(), containsString(KEY_SYSTEM_CACHE_DIR));
        assertThat(getOutContent(), containsString(KEY_CACHE_ENABLED));
        assertThat(getOutContent(), containsString(KEY_CACHE_MAX_SIZE));
        assertThat(getOutContent(), containsString(KEY_CACHE_COMPRESSION_ENABLED));

        // Other settings
        assertThat(getOutContent(), containsString(KEY_USER_TMP_DIR));
        assertThat(getOutContent(), containsString(KEY_USER_LOCKS_DIR));
        assertThat(getOutContent(), containsString(KEY_USER_NETX_RUNNING_FILE));

        // Native (rust) settings
        assertThat(getOutContent(), containsString(KEY_LAUNCHER_RUST_CP_ADD));
        assertThat(getOutContent(), containsString(KEY_LAUNCHER_RUST_CP_REMOVE));
        assertThat(getOutContent(), containsString(KEY_LAUNCHER_RUST_BOOTCP_ADD));
        assertThat(getOutContent(), containsString(KEY_LAUNCHER_RUST_BOOTCP_REMOVE));
    }
}