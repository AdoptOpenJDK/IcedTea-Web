/* Defaults.java
   Copyright (C) 2010 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 2.

IcedTea is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to
the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version.
*/

package net.sourceforge.jnlp.config;


import net.adoptopenjdk.icedteaweb.config.ValidatorFactory;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.ShortcutDesc;
import net.adoptopenjdk.icedteaweb.config.validators.SecurityValueValidator;
import net.adoptopenjdk.icedteaweb.config.validators.ValueValidator;
import net.sourceforge.jnlp.runtime.JNLPProxySelector;
import net.adoptopenjdk.icedteaweb.manifest.ManifestAttributesChecker;

import java.util.HashMap;
import java.util.Map;

import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;
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
     * Get the default settings for deployment
     * @return the default settings for deployment
     */
    public static Map<String, Setting<String>> getDefaults() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkRead(USER_DEPLOYMENT_FILE.getDefaultFullPath());
            sm.checkRead(USER_DEPLOYMENT_FILE.getFullPath());
        }

       
        /*
         * This is more or less a straight copy from the deployment
         * configuration page, with occasional replacements of "" or no-defaults
         * with null
         *
         * The format of this is:
         * name
         * checker (can be null or a ValueChecker)
         * value (can be null or a String)
         */

        Object[][] defaults = new Object[][] {
                /* infrastructure */
                {
                        ConfigurationConstants.KEY_USER_CACHE_DIR,
                        ValidatorFactory.createFilePathValidator(),
                        CACHE_DIR.getDefaultFullPath()
                },
                {
                        ConfigurationConstants.KEY_USER_PERSISTENCE_CACHE_DIR,
                        ValidatorFactory.createFilePathValidator(),
                        PCACHE_DIR.getDefaultFullPath()
                },
                {
                        ConfigurationConstants.KEY_SYSTEM_CACHE_DIR,
                        ValidatorFactory.createFilePathValidator(),
                        null
                },
                {
                        ConfigurationConstants.KEY_USER_LOG_DIR,
                        ValidatorFactory.createFilePathValidator(),
                        LOG_DIR.getDefaultFullPath()
                },
                {
                        ConfigurationConstants.KEY_USER_TMP_DIR,
                        ValidatorFactory.createFilePathValidator(),
                        TMP_DIR.getDefaultFullPath()
                },
                {
                        ConfigurationConstants.KEY_USER_LOCKS_DIR,
                        ValidatorFactory.createFilePathValidator(),
                        LOCKS_DIR.getDefaultFullPath()
                },
                {
                        ConfigurationConstants.KEY_USER_NETX_RUNNING_FILE,
                        ValidatorFactory.createFilePathValidator(),
                        MAIN_LOCK.getDefaultFullPath()
                },
                /* certificates and policy files */
                {
                        ConfigurationConstants.KEY_USER_SECURITY_POLICY,
                        ValidatorFactory.createUrlValidator(),

                        "file://" + JAVA_POLICY.getDefaultFullPath()
                },
                {
                        ConfigurationConstants.KEY_USER_TRUSTED_CA_CERTS,
                        ValidatorFactory.createFilePathValidator(),
                        USER_CACERTS.getDefaultFullPath()
                },
                {
                        ConfigurationConstants.KEY_USER_TRUSTED_JSSE_CA_CERTS,
                        ValidatorFactory.createFilePathValidator(),
                        USER_JSSECAC.getDefaultFullPath()
                },
                {
                        ConfigurationConstants.KEY_USER_TRUSTED_CERTS,
                        ValidatorFactory.createFilePathValidator(),
                        USER_CERTS.getDefaultFullPath()
                },
                {
                        ConfigurationConstants.KEY_USER_TRUSTED_JSSE_CERTS,
                        ValidatorFactory.createFilePathValidator(),
                        USER_JSSECER.getDefaultFullPath()
                },
                {
                        ConfigurationConstants.KEY_USER_TRUSTED_CLIENT_CERTS,
                        ValidatorFactory.createFilePathValidator(),
                        USER_CLIENTCERT.getDefaultFullPath()
                },
                {
                        ConfigurationConstants.KEY_SYSTEM_SECURITY_POLICY,
                        ValidatorFactory.createUrlValidator(),
                        null
                },
                {
                        ConfigurationConstants.KEY_SYSTEM_TRUSTED_CA_CERTS,
                        ValidatorFactory.createFilePathValidator(),
                        SYS_CACERT.getDefaultFullPath()
                },
                {
                        ConfigurationConstants.KEY_SYSTEM_TRUSTED_JSSE_CA_CERTS,
                        ValidatorFactory.createFilePathValidator(),
                        SYS_JSSECAC.getDefaultFullPath()
                },
                {
                        ConfigurationConstants.KEY_SYSTEM_TRUSTED_CERTS,
                        ValidatorFactory.createFilePathValidator(),
                        SYS_CERT.getDefaultFullPath()
                },
                {
                        ConfigurationConstants.KEY_SYSTEM_TRUSTED_JSSE_CERTS,
                        ValidatorFactory.createFilePathValidator(),
                        SYS_JSSECERT.getDefaultFullPath()
                },
                {
                        ConfigurationConstants.KEY_SYSTEM_TRUSTED_CLIENT_CERTS,
                        ValidatorFactory.createFilePathValidator(),
                        SYS_CLIENTCERT.getDefaultFullPath()
                },
                /* security access and control */
                {
                        ConfigurationConstants.KEY_SECURITY_PROMPT_USER,
                        ValidatorFactory.createBooleanValidator(),
                        String.valueOf(true)
                },
                {
                        ConfigurationConstants.KEY_SECURITY_ASKGRANTDIALOG_NOTINCA,
                        ValidatorFactory.createBooleanValidator(),
                        String.valueOf(true)
                },
                {
                        ConfigurationConstants.KEY_SECURITY_NOTINCA_WARNING,
                        ValidatorFactory.createBooleanValidator(),
                        String.valueOf(true)
                },
                {
                        ConfigurationConstants.KEY_SECURITY_EXPIRED_WARNING,
                        ValidatorFactory.createBooleanValidator(),
                        String.valueOf(true)
                },
                {
                        ConfigurationConstants.KEY_SECURITY_JSSE_HOSTMISMATCH_WARNING,
                        ValidatorFactory.createBooleanValidator(),
                        String.valueOf(true)
                },
                {
                        ConfigurationConstants.KEY_SECURITY_TRUSTED_POLICY,
                        ValidatorFactory.createFilePathValidator(),
                        null
                },
                {
                        ConfigurationConstants.KEY_SECURITY_ALLOW_HIDE_WINDOW_WARNING,
                        ValidatorFactory.createBooleanValidator(),
                        String.valueOf(true)
                },
                {
                        ConfigurationConstants.KEY_SECURITY_PROMPT_USER_FOR_JNLP,
                        ValidatorFactory.createBooleanValidator(),
                        String.valueOf(true)
                },
                {
                        ConfigurationConstants.KEY_STRICT_JNLP_CLASSLOADER,
                        ValidatorFactory.createBooleanValidator(),

                        String.valueOf(true)
                },
                {
                        ConfigurationConstants.KEY_HTTPS_DONT_ENFORCE,
                        ValidatorFactory.createBooleanValidator(),
                        String.valueOf(false)
                },
                {
                        ConfigurationConstants.KEY_SECURITY_ITW_IGNORECERTISSUES,
                        ValidatorFactory.createBooleanValidator(),
                        String.valueOf(false)
                },
                {
                        ConfigurationConstants.KEY_SECURITY_PROMPT_USER_FOR_JNLP,
                        ValidatorFactory.createBooleanValidator(),
                        String.valueOf(true)
                },
                /* networking */
                {
                        ConfigurationConstants.KEY_PROXY_TYPE,
                        ValidatorFactory.createRangedIntegerValidator(JNLPProxySelector.PROXY_TYPE_UNKNOWN, JNLPProxySelector.PROXY_TYPE_BROWSER),
                        String.valueOf(JNLPProxySelector.PROXY_TYPE_BROWSER)
                },
                {
                        ConfigurationConstants.KEY_PROXY_SAME,
                        ValidatorFactory.createBooleanValidator(),
                        String.valueOf(false)
                },
                {
                        ConfigurationConstants.KEY_PROXY_AUTO_CONFIG_URL,
                        ValidatorFactory.createUrlValidator(),
                        null
                },
                {
                        ConfigurationConstants.KEY_PROXY_BYPASS_LIST,
                        null,
                        null
                },
                {
                        ConfigurationConstants.KEY_PROXY_BYPASS_LOCAL,
                        null,
                        null
                },
                {
                        ConfigurationConstants.KEY_PROXY_HTTP_HOST,
                        null,
                        null
                },
                {
                        ConfigurationConstants.KEY_PROXY_HTTP_PORT,
                        null,
                        null
                },
                {
                        ConfigurationConstants.KEY_PROXY_HTTPS_HOST,
                        null,
                        null
                },
                {
                        ConfigurationConstants.KEY_PROXY_HTTPS_PORT,
                        null,
                        null
                },
                {
                        ConfigurationConstants.KEY_PROXY_FTP_HOST,
                        null,
                        null
                },
                {
                        ConfigurationConstants.KEY_PROXY_FTP_PORT,
                        null,
                        null
                },
                {
                        ConfigurationConstants.KEY_PROXY_SOCKS4_HOST,
                        null,
                        null
                },
                {
                        ConfigurationConstants.KEY_PROXY_SOCKS4_PORT,
                        null,
                        null
                },
                {
                        ConfigurationConstants.KEY_PROXY_OVERRIDE_HOSTS,
                        null,
                        null
                },
                /* cache and optional package repository */
                {
                        ConfigurationConstants.KEY_CACHE_MAX_SIZE,
                        ValidatorFactory.createRangedIntegerValidator(-1, Integer.MAX_VALUE),
                        "-1"
                },
                {
                        ConfigurationConstants.KEY_CACHE_COMPRESSION_ENABLED,
                        ValidatorFactory.createRangedIntegerValidator(0, 10),
                        String.valueOf(0)
                },
                {
                        ConfigurationConstants.KEY_CACHE_ENABLED,
                        ValidatorFactory.createBooleanValidator(),
                        String.valueOf(false)
                },
                /* java console */
                {
                        ConfigurationConstants.KEY_CONSOLE_STARTUP_MODE,
                        ValidatorFactory.createStringValidator(new String[] {
                                ConfigurationConstants.CONSOLE_DISABLE,
                                ConfigurationConstants.CONSOLE_HIDE,
                                ConfigurationConstants.CONSOLE_SHOW,
                                ConfigurationConstants.CONSOLE_SHOW_PLUGIN,
                                ConfigurationConstants.CONSOLE_SHOW_JAVAWS
                        }),
                        ConfigurationConstants.CONSOLE_HIDE
                },
                {
                        ConfigurationConstants.KEY_ENABLE_LOGGING,
                        ValidatorFactory.createBooleanValidator(),
                        String.valueOf(false)
                },
                {
                        ConfigurationConstants.KEY_ENABLE_LOGGING_HEADERS,
                        ValidatorFactory.createBooleanValidator(),
                        String.valueOf(false)
                },
                {
                        ConfigurationConstants.KEY_ENABLE_LOGGING_TOFILE,
                        ValidatorFactory.createBooleanValidator(),
                        String.valueOf(false)
                },
                {
                        ConfigurationConstants.KEY_ENABLE_APPLICATION_LOGGING_TOFILE,
                        ValidatorFactory.createBooleanValidator(),
                        String.valueOf(true)
                },
                 {
                         ConfigurationConstants.KEY_ENABLE_LEGACY_LOGBASEDFILELOG,
                        ValidatorFactory.createBooleanValidator(),
                        String.valueOf(false)
                },
                {
                        ConfigurationConstants.KEY_ENABLE_LOGGING_TOSTREAMS,
                        ValidatorFactory.createBooleanValidator(),
                        String.valueOf(true)
                },                
                {
                        ConfigurationConstants.KEY_ENABLE_LOGGING_TOSYSTEMLOG,
                        ValidatorFactory.createBooleanValidator(),
                        String.valueOf(true)
                },
                /* JNLP association */
                {
                        ConfigurationConstants.KEY_JNLP_ASSOCIATIONS,
                        ValidatorFactory.createRangedIntegerValidator(ConfigurationConstants.JNLP_ASSOCIATION_NEVER,
                                ConfigurationConstants.JNLP_ASSOCIATION_REPLACE_ASK),
                        String.valueOf(ConfigurationConstants.JNLP_ASSOCIATION_ASK_USER)
                },
                /* desktop integration */
                {
                        ConfigurationConstants.KEY_CREATE_DESKTOP_SHORTCUT,
                        ValidatorFactory.createStringValidator(new String[] {
                                ShortcutDesc.CREATE_ALWAYS,
                                ShortcutDesc.CREATE_ALWAYS_IF_HINTED,
                                ShortcutDesc.CREATE_ASK_USER,
                                ShortcutDesc.CREATE_ASK_USER_IF_HINTED,
                                ShortcutDesc.CREATE_NEVER
                        }),
                        ShortcutDesc.CREATE_ASK_USER_IF_HINTED
                },
                /* jre selection */
                {
                        ConfigurationConstants.KEY_JRE_INTSTALL_URL,
                        ValidatorFactory.createUrlValidator(),
                        null
                },
                /* jre management */
                {
                        ConfigurationConstants.KEY_AUTO_DOWNLOAD_JRE,
                        ValidatorFactory.createBooleanValidator(),
                        String.valueOf(false)
                },
                /* browser selection */
                {
                        ConfigurationConstants.KEY_BROWSER_PATH,
                        ValidatorFactory.createBrowserPathValidator(),
                        null
                },
                /* check for update timeout */
                {
                        ConfigurationConstants.KEY_UPDATE_TIMEOUT,
                        ValidatorFactory.createRangedIntegerValidator(0, 10000),
                        String.valueOf(500)
                },
                {
                        ConfigurationConstants.IGNORE_HEADLESS_CHECK,
                        ValidatorFactory.createBooleanValidator(),
                        String.valueOf(false)
                },
                //JVM arguments for plugin
                {
                        ConfigurationConstants.KEY_PLUGIN_JVM_ARGUMENTS,
                        null,
                        null
                },
               //unsigned applet security level
                {
                        ConfigurationConstants.KEY_SECURITY_LEVEL,
                new SecurityValueValidator(),
                null
                },
                //JVM executable for itw
                {
                        ConfigurationConstants.KEY_JRE_DIR,
                        null,
                        null
                },
                //enable manifest-attributes checks
                {
                        ConfigurationConstants.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK,
                        ValidatorFactory.createManifestAttributeCheckValidator(),
                        String.valueOf(ManifestAttributesChecker.MANIFEST_ATTRIBUTES_CHECK.ALL)
                },
                {
                        ConfigurationConstants.KEY_SYSTEM_CONFIG,
                        ValidatorFactory.createUrlValidator(),
                        null
                },
                {
                        ConfigurationConstants.KEY_SYSTEM_CONFIG_MANDATORY,
                        ValidatorFactory.createBooleanValidator(),
                        String.valueOf(false)
                } ,
                {
                        ConfigurationConstants.KEY_SMALL_SIZE_OVERRIDE_WIDTH,
                        ValidatorFactory.createRangedIntegerValidator(-9999, +9999),
                        String.valueOf(800)//0 is disabling it; negative is enforcing it
                },
                {
                        ConfigurationConstants.KEY_SMALL_SIZE_OVERRIDE_HEIGHT,
                        ValidatorFactory.createRangedIntegerValidator(-9999, +9999),
                        String.valueOf(600)//0 is disabling it; negative is enforcing it
                },
                {
                        ConfigurationConstants.KEY_SMALL_SIZE_OVERRIDE_THRESHOLD,
                        ValidatorFactory.createRangedIntegerValidator(0, 1000),
                        String.valueOf(10)// threshold when applet is considered as too small
                },
                //**************
                //* Native (rust) only - beggin
                //**************
                {
                        ConfigurationConstants.KEY_LAUNCHER_RUST_CP_ADD,
                       ValidatorFactory.createRustCpValidator(),
                        ""
                },
                {
                        ConfigurationConstants.KEY_LAUNCHER_RUST_CP_REMOVE,
                        ValidatorFactory.createRustCpValidator(),
                        ""
                },
                {
                        ConfigurationConstants.KEY_LAUNCHER_RUST_BOOTCP_ADD,
                        ValidatorFactory.createRustCpValidator(),
                        null
                },
                {
                        ConfigurationConstants.KEY_LAUNCHER_RUST_BOOTCP_REMOVE,
                        ValidatorFactory.createRustCpValidator(),
                        ""
                }
                //**************
                //* Native (rust) only - end
                //**************
        };

        HashMap<String, Setting<String>> result = new HashMap<>();
        for (Object[] default1 : defaults) {
            String name = (String) default1[0];
            ValueValidator checker = (ValueValidator) default1[1];
            String actualValue = (String) default1[2];
            boolean locked = false;
            Setting<String> value = new Setting<>(name, R("Unknown"), locked, checker, actualValue, actualValue, R("DCSourceInternal"));
            result.put(name, value);
        }

        return result;
    }
}
