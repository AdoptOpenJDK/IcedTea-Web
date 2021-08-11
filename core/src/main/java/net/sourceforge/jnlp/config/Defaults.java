/* Defaults.java
   Copyright (C) 2010 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify it under the
terms of the GNU General Public License as published by the Free Software
Foundation, version 2.

IcedTea is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
IcedTea; see the file COPYING. If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is making a
combined work based on this library. Thus, the terms and conditions of the GNU
General Public License cover the whole combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent modules, and
to copy and distribute the resulting executable under terms of your choice,
provided that you also meet, for each linked independent module, the terms and
conditions of the license of that module. An independent module is a module
which is not derived from or based on this library. If you modify this library,
you may extend this exception to your version of the library, but you are not
obligated to do so. If you do not wish to do so, delete this exception
statement from your version.
*/

package net.sourceforge.jnlp.config;


import net.adoptopenjdk.icedteaweb.config.ValidatorFactory;
import net.adoptopenjdk.icedteaweb.config.validators.SecurityValueValidator;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.ShortcutDesc;
import net.adoptopenjdk.icedteaweb.manifest.ManifestAttributesChecker;
import net.sourceforge.jnlp.proxy.ProxyType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.adoptopenjdk.icedteaweb.StreamUtils.loadServiceAsStream;
import static net.sourceforge.jnlp.config.PathsAndFiles.CACHE_DIR;
import static net.sourceforge.jnlp.config.PathsAndFiles.JAVA_POLICY;
import static net.sourceforge.jnlp.config.PathsAndFiles.LOCKS_DIR;
import static net.sourceforge.jnlp.config.PathsAndFiles.LOG_DIR;
import static net.sourceforge.jnlp.config.PathsAndFiles.MAIN_LOCK;
import static net.sourceforge.jnlp.config.PathsAndFiles.PCACHE_DIR;
import static net.sourceforge.jnlp.config.PathsAndFiles.SYS_CACERT;
import static net.sourceforge.jnlp.config.PathsAndFiles.SYS_CERT;
import static net.sourceforge.jnlp.config.PathsAndFiles.SYS_CLIENTCERT;
import static net.sourceforge.jnlp.config.PathsAndFiles.SYS_JSSECAC;
import static net.sourceforge.jnlp.config.PathsAndFiles.SYS_JSSECERT;
import static net.sourceforge.jnlp.config.PathsAndFiles.TMP_DIR;
import static net.sourceforge.jnlp.config.PathsAndFiles.USER_CACERTS;
import static net.sourceforge.jnlp.config.PathsAndFiles.USER_CERTS;
import static net.sourceforge.jnlp.config.PathsAndFiles.USER_CLIENTCERT;
import static net.sourceforge.jnlp.config.PathsAndFiles.USER_DEPLOYMENT_FILE;
import static net.sourceforge.jnlp.config.PathsAndFiles.USER_JSSECAC;
import static net.sourceforge.jnlp.config.PathsAndFiles.USER_JSSECER;

/**
 * This class stores the default configuration
 */
public class Defaults {

    /**
     * This is more or less a straight copy from the deployment
     * configuration page, with occasional replacements of "" or no-defaults
     * with null
     */
    private static final List<Setting> DEFAULTS = Arrays.asList(
            /*
             * infrastructure
             */
            Setting.createDefault(
                    ConfigurationConstants.KEY_USER_CACHE_DIR,
                    CACHE_DIR.getDefaultFullPath(),
                    ValidatorFactory.createFilePathValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_USER_PERSISTENCE_CACHE_DIR,
                    PCACHE_DIR.getDefaultFullPath(),
                    ValidatorFactory.createFilePathValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_SYSTEM_CACHE_DIR,
                    null,
                    ValidatorFactory.createFilePathValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_USER_LOG_DIR,
                    LOG_DIR.getDefaultFullPath(),
                    ValidatorFactory.createFilePathValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_USER_TMP_DIR,
                    TMP_DIR.getDefaultFullPath(),
                    ValidatorFactory.createFilePathValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_USER_LOCKS_DIR,
                    LOCKS_DIR.getDefaultFullPath(),
                    ValidatorFactory.createFilePathValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_USER_NETX_RUNNING_FILE,
                    MAIN_LOCK.getDefaultFullPath(),
                    ValidatorFactory.createFilePathValidator()
            ),

            /*
             * certificates and policy files
             */
            Setting.createDefault(
                    ConfigurationConstants.KEY_USER_SECURITY_POLICY,
                    "file://" + JAVA_POLICY.getDefaultFullPath(),
                    ValidatorFactory.createUrlValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_USER_TRUSTED_CA_CERTS,
                    USER_CACERTS.getDefaultFullPath(),
                    ValidatorFactory.createFilePathValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_USER_TRUSTED_JSSE_CA_CERTS,
                    USER_JSSECAC.getDefaultFullPath(),
                    ValidatorFactory.createFilePathValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_USER_TRUSTED_CERTS,
                    USER_CERTS.getDefaultFullPath(),
                    ValidatorFactory.createFilePathValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_USER_TRUSTED_JSSE_CERTS,
                    USER_JSSECER.getDefaultFullPath(),
                    ValidatorFactory.createFilePathValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_USER_TRUSTED_CLIENT_CERTS,
                    USER_CLIENTCERT.getDefaultFullPath(),
                    ValidatorFactory.createFilePathValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_SYSTEM_SECURITY_POLICY,
                    null,
                    ValidatorFactory.createUrlValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_SYSTEM_TRUSTED_CA_CERTS,
                    SYS_CACERT.getDefaultFullPath(),
                    ValidatorFactory.createFilePathValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_SYSTEM_TRUSTED_JSSE_CA_CERTS,
                    SYS_JSSECAC.getDefaultFullPath(),
                    ValidatorFactory.createFilePathValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_SYSTEM_TRUSTED_CERTS,
                    SYS_CERT.getDefaultFullPath(),
                    ValidatorFactory.createFilePathValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_SYSTEM_TRUSTED_JSSE_CERTS,
                    SYS_JSSECERT.getDefaultFullPath(),
                    ValidatorFactory.createFilePathValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_SYSTEM_TRUSTED_CLIENT_CERTS,
                    SYS_CLIENTCERT.getDefaultFullPath(),
                    ValidatorFactory.createFilePathValidator()
            ),

            /*
             * Windows certificates key stores
             */
            Setting.createDefault(
                    ConfigurationConstants.KEY_SECURITY_USE_ROOTCA_STORE_TYPE_WINDOWS_ROOT,
                    String.valueOf(false),
                    ValidatorFactory.createBooleanValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_SECURITY_USE_ROOTCA_STORE_TYPE_WINDOWS_MY,
                    String.valueOf(false),
                    ValidatorFactory.createBooleanValidator()
            ),               
            
            
            /*
             * security access and control
             */
            Setting.createDefault(
                    ConfigurationConstants.KEY_SECURITY_PROMPT_USER,
                    String.valueOf(true),
                    ValidatorFactory.createBooleanValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_SECURITY_ASKGRANTDIALOG_NOTINCA,
                    String.valueOf(true),
                    ValidatorFactory.createBooleanValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_SECURITY_NOTINCA_WARNING,
                    String.valueOf(true),
                    ValidatorFactory.createBooleanValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_SECURITY_EXPIRED_WARNING,
                    String.valueOf(true),
                    ValidatorFactory.createBooleanValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_SECURITY_JSSE_HOSTMISMATCH_WARNING,
                    String.valueOf(true),
                    ValidatorFactory.createBooleanValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_SECURITY_TRUSTED_POLICY,
                    null,
                    ValidatorFactory.createFilePathValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_SECURITY_ALLOW_HIDE_WINDOW_WARNING,
                    String.valueOf(true),
                    ValidatorFactory.createBooleanValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_SECURITY_PROMPT_USER_FOR_JNLP,
                    String.valueOf(true),
                    ValidatorFactory.createBooleanValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_STRICT_JNLP_CLASSLOADER,
                    String.valueOf(true),
                    ValidatorFactory.createBooleanValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_HTTPS_DONT_ENFORCE,
                    String.valueOf(false),
                    ValidatorFactory.createBooleanValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_SECURITY_ITW_IGNORECERTISSUES,
                    String.valueOf(false),
                    ValidatorFactory.createBooleanValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_SECURITY_DISABLE_RESTRICTED_FILES,
                    String.valueOf(false),
                    ValidatorFactory.createBooleanValidator()
            ),

            /*
             * networking
             */
            Setting.createDefault(
                    ConfigurationConstants.KEY_PROXY_TYPE,
                    String.valueOf(ProxyType.PROXY_TYPE_SYSTEM.getConfigValue()),
                    ValidatorFactory.createRangedIntegerValidator(ProxyType.PROXY_TYPE_UNKNOWN.getConfigValue(),
                            ProxyType.PROXY_TYPE_SYSTEM.getConfigValue())
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_PROXY_SAME,
                    String.valueOf(false),
                    ValidatorFactory.createBooleanValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_PROXY_AUTO_CONFIG_URL,
                    null,
                    ValidatorFactory.createUrlValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_PROXY_BYPASS_LIST,
                    null,
                    null
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_PROXY_BYPASS_LOCAL,
                    String.valueOf(false),
                    ValidatorFactory.createBooleanValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_PROXY_HTTP_HOST,
                    null,
                    null
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_PROXY_HTTP_PORT,
                    null,
                    ValidatorFactory.createPortValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_PROXY_HTTPS_HOST,
                    null,
                    null
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_PROXY_HTTPS_PORT,
                    null,
                    ValidatorFactory.createPortValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_PROXY_FTP_HOST,
                    null,
                    null
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_PROXY_FTP_PORT,
                    null,
                    ValidatorFactory.createPortValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_PROXY_SOCKS4_HOST,
                    null,
                    null
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_PROXY_SOCKS4_PORT,
                    null,
                    ValidatorFactory.createPortValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_PROXY_OVERRIDE_HOSTS,
                    null,
                    null
            ),

            /*
             * cache and optional package repository
             */
            Setting.createDefault(
                    ConfigurationConstants.KEY_CACHE_MAX_SIZE,
                    "-1",
                    ValidatorFactory.createRangedIntegerValidator(-1, Integer.MAX_VALUE)
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_CACHE_COMPRESSION_ENABLED,
                    String.valueOf(0),
                    ValidatorFactory.createRangedIntegerValidator(0, 10)
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_CACHE_ENABLED,
                    String.valueOf(false),
                    ValidatorFactory.createBooleanValidator()
            ),

            /*
             * java console
             */
            Setting.createDefault(
                    ConfigurationConstants.KEY_CONSOLE_STARTUP_MODE,
                    ConfigurationConstants.CONSOLE_HIDE,
                    ValidatorFactory.createStringValidator(new String[]{
                            ConfigurationConstants.CONSOLE_DISABLE,
                            ConfigurationConstants.CONSOLE_HIDE,
                            ConfigurationConstants.CONSOLE_SHOW,
                            ConfigurationConstants.CONSOLE_SHOW_PLUGIN,
                            ConfigurationConstants.CONSOLE_SHOW_JAVAWS
                    })
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_ENABLE_DEBUG_LOGGING,
                    String.valueOf(false),
                    ValidatorFactory.createBooleanValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_ENABLE_LOGGING_HEADERS,
                    String.valueOf(false),
                    ValidatorFactory.createBooleanValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_ENABLE_LOGGING_TOFILE,
                    String.valueOf(false),
                    ValidatorFactory.createBooleanValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_ENABLE_APPLICATION_LOGGING_TOFILE,
                    String.valueOf(true),
                    ValidatorFactory.createBooleanValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_ENABLE_LEGACY_LOGBASEDFILELOG,
                    String.valueOf(false),
                    ValidatorFactory.createBooleanValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_ENABLE_LOGGING_TOSTREAMS,
                    String.valueOf(true),
                    ValidatorFactory.createBooleanValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_ENABLE_LOGGING_TOSYSTEMLOG,
                    String.valueOf(true),
                    ValidatorFactory.createBooleanValidator()
            ),

            /*
             * JNLP association
             */
            Setting.createDefault(
                    ConfigurationConstants.KEY_JNLP_ASSOCIATIONS,
                    String.valueOf(ConfigurationConstants.JNLP_ASSOCIATION_ASK_USER),
                    ValidatorFactory.createRangedIntegerValidator(ConfigurationConstants.JNLP_ASSOCIATION_NEVER,
                            ConfigurationConstants.JNLP_ASSOCIATION_REPLACE_ASK)
            ),

            /*
             * desktop integration
             */
            Setting.createDefault(
                    ConfigurationConstants.KEY_CREATE_DESKTOP_SHORTCUT,
                    ShortcutDesc.CREATE_ASK_USER_IF_HINTED,
                    ValidatorFactory.createStringValidator(new String[]{
                            ShortcutDesc.CREATE_ALWAYS,
                            ShortcutDesc.CREATE_ALWAYS_IF_HINTED,
                            ShortcutDesc.CREATE_ASK_USER,
                            ShortcutDesc.CREATE_ASK_USER_IF_HINTED,
                            ShortcutDesc.CREATE_NEVER
                    })
            ),

            /*
             * jre selection
             */
            Setting.createDefault(
                    ConfigurationConstants.KEY_JRE_INTSTALL_URL,
                    null,
                    ValidatorFactory.createUrlValidator()
            ),

            /*
             * jre management
             */
            Setting.createDefault(
                    ConfigurationConstants.KEY_AUTO_DOWNLOAD_JRE,
                    String.valueOf(false),
                    ValidatorFactory.createBooleanValidator()
            ),

            /*
             * browser selection
             */
            Setting.createDefault(
                    ConfigurationConstants.KEY_BROWSER_PATH,
                    null,
                    ValidatorFactory.createBrowserPathValidator()
            ),

            /*
             * check for update timeout
             */
            Setting.createDefault(
                    ConfigurationConstants.KEY_UPDATE_TIMEOUT,
                    String.valueOf(500),
                    ValidatorFactory.createRangedIntegerValidator(0, 10000)
            ),
            Setting.createDefault(
                    ConfigurationConstants.IGNORE_HEADLESS_CHECK,
                    String.valueOf(false),
                    ValidatorFactory.createBooleanValidator()
            ),

            /*
             * JVM arguments for plugin
             */
            Setting.createDefault(
                    ConfigurationConstants.KEY_PLUGIN_JVM_ARGUMENTS,
                    null,
                    null
            ),

            /*
             * unsigned applet security level
             */
            Setting.createDefault(
                    ConfigurationConstants.KEY_SECURITY_LEVEL,
                    null,
                    new SecurityValueValidator()
            ),

            Setting.createDefault(
                    ConfigurationConstants.KEY_PARALLEL_RESOURCE_DOWNLOAD_COUNT,
                    //Based on https://docs.pushtechnology.com/cloud/latest/manual/html/designguide/solution/support/connection_limitations.html
                    String.valueOf(6),
                    ValidatorFactory.createRangedIntegerValidator(1, 24)
            ),

            /*
             * JVM executable for itw
             */
            Setting.createDefault(
                    ConfigurationConstants.KEY_JRE_DIR,
                    null,
                    null
            ),
            /*
             * enable manifest-attributes checks
             */
            Setting.createDefault(
                    ConfigurationConstants.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK,
                    String.valueOf(ManifestAttributesChecker.MANIFEST_ATTRIBUTES_CHECK.ALL),
                    ValidatorFactory.createManifestAttributeCheckValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_ASSUME_FILE_STEM_IN_CODEBASE,
                    String.valueOf(false),
                    ValidatorFactory.createBooleanValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_SYSTEM_CONFIG,
                    null,
                    ValidatorFactory.createUrlValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_SYSTEM_CONFIG_MANDATORY,
                    String.valueOf(false),
                    ValidatorFactory.createBooleanValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_SMALL_SIZE_OVERRIDE_WIDTH,
                    String.valueOf(800),
                    ValidatorFactory.createRangedIntegerValidator(-9999, +9999)
                    //0 is disabling it; negative is enforcing it
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_SMALL_SIZE_OVERRIDE_HEIGHT,
                    String.valueOf(600),
                    ValidatorFactory.createRangedIntegerValidator(-9999, +9999)
                    //0 is disabling it; negative is enforcing it
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_SMALL_SIZE_OVERRIDE_THRESHOLD,
                    String.valueOf(10),
                    ValidatorFactory.createRangedIntegerValidator(0, 1000)
                    // threshold when applet is considered as too small
            ),

            /*
             * Native (rust) only - beggin
             */
            Setting.createDefault(
                    ConfigurationConstants.KEY_LAUNCHER_RUST_CP_ADD,
                    "",
                    ValidatorFactory.createRustCpValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_LAUNCHER_RUST_CP_REMOVE,
                    "",
                    ValidatorFactory.createRustCpValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_LAUNCHER_RUST_BOOTCP_ADD,
                    null,
                    ValidatorFactory.createRustCpValidator()
            ),
            Setting.createDefault(
                    ConfigurationConstants.KEY_LAUNCHER_RUST_BOOTCP_REMOVE,
                    "",
                    ValidatorFactory.createRustCpValidator()
            ),

            Setting.createDefault(
                    ConfigurationConstants.KEY_SECURITY_SERVER_WHITELIST,
                    null,
                    null
            ),

            Setting.createDefault(
                    ConfigurationConstants.KEY_JVM_ARGS_WHITELIST,
                    null,
                    null
            ),

            Setting.createDefault(
                    ConfigurationConstants.KEY_HTTPCONNECTION_CONNECT_TIMEOUT,
                    String.valueOf(10000),
                    ValidatorFactory.createRangedIntegerValidator(0, Integer.MAX_VALUE)
            ),

            Setting.createDefault(
                    ConfigurationConstants.KEY_HTTPCONNECTION_READ_TIMEOUT,
                    String.valueOf(10000),
                    ValidatorFactory.createRangedIntegerValidator(0, Integer.MAX_VALUE)
            )
    );

    private static final List<Setting> additionalDefaults = loadServiceAsStream(DefaultsProvider.class)
            .flatMap(provider -> provider.getDefaults().stream())
            .collect(Collectors.toList());

    /**
     * Get the default settings for deployment
     *
     * @return the default settings for deployment
     */
    public static Map<String, Setting> getDefaults() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkRead(USER_DEPLOYMENT_FILE.getDefaultFullPath());
            sm.checkRead(USER_DEPLOYMENT_FILE.getFullPath());
        }

        return Stream.concat(additionalDefaults.stream(), DEFAULTS.stream())
                .map(Setting::copy)
                .collect(Collectors.toMap(Setting::getName, Function.identity()));
    }
}
