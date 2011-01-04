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

import static net.sourceforge.jnlp.runtime.Translator.R;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.jnlp.ShortcutDesc;
import net.sourceforge.jnlp.runtime.JNLPProxySelector;

/**
 * This class stores the default configuration
 */
public class Defaults {

    /**
     * Get the default settings for deployment
     */
    public static Map<String, Setting<String>> getDefaults() {
        File userFile = new File(System.getProperty("user.home") + File.separator + DeploymentConfiguration.DEPLOYMENT_DIR
                + File.separator + DeploymentConfiguration.DEPLOYMENT_PROPERTIES);

        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkRead(userFile.toString());
        }

        final String SYSTEM_HOME = System.getProperty("java.home");
        final String SYSTEM_SECURITY = SYSTEM_HOME + File.separator + "lib" + File.separator + "security";

        final String USER_HOME = System.getProperty("user.home") + File.separator + DeploymentConfiguration.DEPLOYMENT_DIR;
        final String USER_SECURITY = USER_HOME + File.separator + "security";

        final String LOCKS_DIR = System.getProperty("java.io.tmpdir") + File.separator
                + System.getProperty("user.name") + File.separator + "netx" + File.separator
                + "locks";

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
                        DeploymentConfiguration.KEY_USER_CACHE_DIR,
                        BasicValueValidators.getFilePathValidator(),
                        USER_HOME + File.separator + "cache"
                },
                {
                        DeploymentConfiguration.KEY_USER_PERSISTENCE_CACHE_DIR,
                        BasicValueValidators.getFilePathValidator(),
                        USER_HOME + File.separator + "pcache"
                },
                {
                        DeploymentConfiguration.KEY_SYSTEM_CACHE_DIR,
                        BasicValueValidators.getFilePathValidator(),
                        null
                },
                {
                        DeploymentConfiguration.KEY_USER_LOG_DIR,
                        BasicValueValidators.getFilePathValidator(),
                        USER_HOME + File.separator + "log"
                },
                {
                        DeploymentConfiguration.KEY_USER_TMP_DIR,
                        BasicValueValidators.getFilePathValidator(),
                        USER_HOME + File.separator + "tmp"
                },
                {
                        DeploymentConfiguration.KEY_USER_LOCKS_DIR,
                        BasicValueValidators.getFilePathValidator(),
                        LOCKS_DIR
                },
                {
                        DeploymentConfiguration.KEY_USER_NETX_RUNNING_FILE,
                        BasicValueValidators.getFilePathValidator(),
                        LOCKS_DIR + File.separator + "netx_running"
                },
                /* certificates and policy files */
                {
                        DeploymentConfiguration.KEY_USER_SECURITY_POLICY,
                        BasicValueValidators.getUrlValidator(),
                        "file://" + USER_SECURITY + File.separator + "java.policy"
                },
                {
                        DeploymentConfiguration.KEY_USER_TRUSTED_CA_CERTS,
                        BasicValueValidators.getFilePathValidator(),
                        USER_SECURITY + File.separator + "trusted.cacerts"
                },
                {
                        DeploymentConfiguration.KEY_USER_TRUSTED_JSSE_CA_CERTS,
                        BasicValueValidators.getFilePathValidator(),
                        USER_SECURITY + File.separator + "trusted.jssecacerts"
                },
                {
                        DeploymentConfiguration.KEY_USER_TRUSTED_CERTS,
                        BasicValueValidators.getFilePathValidator(),
                        USER_SECURITY + File.separator + "trusted.certs"
                },
                {
                        DeploymentConfiguration.KEY_USER_TRUSTED_JSSE_CERTS,
                        BasicValueValidators.getFilePathValidator(),
                        USER_SECURITY + File.separator + "trusted.jssecerts"
                },
                {
                        DeploymentConfiguration.KEY_USER_TRUSTED_CLIENT_CERTS,
                        BasicValueValidators.getFilePathValidator(),
                        USER_SECURITY + File.separator + "trusted.clientcerts"
                },
                {
                        DeploymentConfiguration.KEY_SYSTEM_SECURITY_POLICY,
                        BasicValueValidators.getUrlValidator(),
                        null
                },
                {
                        DeploymentConfiguration.KEY_SYSTEM_TRUSTED_CA_CERTS,
                        BasicValueValidators.getFilePathValidator(),
                        SYSTEM_SECURITY + File.separator + "cacerts"
                },
                {
                        DeploymentConfiguration.KEY_SYSTEM_TRUSTED_JSSE_CA_CERTS,
                        BasicValueValidators.getFilePathValidator(),
                        SYSTEM_SECURITY + File.separator + "jssecacerts"
                },
                {
                        DeploymentConfiguration.KEY_SYSTEM_TRUSTED_CERTS,
                        BasicValueValidators.getFilePathValidator(),
                        SYSTEM_SECURITY + File.separator + "trusted.certs"
                },
                {
                        DeploymentConfiguration.KEY_SYSTEM_TRUSTED_JSSE_CERTS,
                        BasicValueValidators.getFilePathValidator(),
                        SYSTEM_SECURITY + File.separator + "trusted.jssecerts"
                },
                {
                        DeploymentConfiguration.KEY_SYSTEM_TRUSTED_CLIENT_CERTS,
                        BasicValueValidators.getFilePathValidator(),
                        SYSTEM_SECURITY + File.separator + "trusted.clientcerts"
                },
                /* security access and control */
                {
                        DeploymentConfiguration.KEY_SECURITY_PROMPT_USER,
                        BasicValueValidators.getBooleanValidator(),
                        String.valueOf(true)
                },
                {
                        "deployment.security.askgrantdialog.notinca",
                        BasicValueValidators.getBooleanValidator(),
                        String.valueOf(true)
                },
                {
                        "deployment.security.notinca.warning",
                        BasicValueValidators.getBooleanValidator(),
                        String.valueOf(true)
                },
                {
                        "deployment.security.expired.warning",
                        BasicValueValidators.getBooleanValidator(),
                        String.valueOf(true)
                },
                {
                        "deployment.security.jsse.hostmismatch.warning",
                        BasicValueValidators.getBooleanValidator(),
                        String.valueOf(true)
                },
                {
                        DeploymentConfiguration.KEY_SECURITY_TRUSTED_POLICY,
                        BasicValueValidators.getFilePathValidator(),
                        null
                },
                {
                        DeploymentConfiguration.KEY_SECURITY_ALLOW_HIDE_WINDOW_WARNING,
                        BasicValueValidators.getBooleanValidator(),
                        String.valueOf(true)
                },
                {
                        DeploymentConfiguration.KEY_SECURITY_PROMPT_USER_FOR_JNLP,
                        BasicValueValidators.getBooleanValidator(),
                        String.valueOf(true)
                },
                {
                        DeploymentConfiguration.KEY_SECURITY_INSTALL_AUTHENTICATOR,
                        BasicValueValidators.getBooleanValidator(),
                        String.valueOf(true)
                },
                /* networking */
                {
                        DeploymentConfiguration.KEY_PROXY_TYPE,
                        BasicValueValidators.getRangedIntegerValidator(JNLPProxySelector.PROXY_TYPE_UNKNOWN, JNLPProxySelector.PROXY_TYPE_BROWSER),
                        String.valueOf(JNLPProxySelector.PROXY_TYPE_BROWSER)
                },
                {
                        DeploymentConfiguration.KEY_PROXY_SAME,
                        BasicValueValidators.getBooleanValidator(),
                        String.valueOf(false)
                },
                {
                        DeploymentConfiguration.KEY_PROXY_AUTO_CONFIG_URL,
                        BasicValueValidators.getUrlValidator(),
                        null
                },
                {
                        DeploymentConfiguration.KEY_PROXY_BYPASS_LIST,
                        null,
                        null
                },
                {
                        DeploymentConfiguration.KEY_PROXY_BYPASS_LOCAL,
                        null,
                        null
                },
                {
                        DeploymentConfiguration.KEY_PROXY_HTTP_HOST,
                        null,
                        null
                },
                {
                        DeploymentConfiguration.KEY_PROXY_HTTP_PORT,
                        null,
                        null
                },
                {
                        DeploymentConfiguration.KEY_PROXY_HTTPS_HOST,
                        null,
                        null
                },
                {
                        DeploymentConfiguration.KEY_PROXY_HTTPS_PORT,
                        null,
                        null
                },
                {
                        DeploymentConfiguration.KEY_PROXY_FTP_HOST,
                        null,
                        null
                },
                {
                        DeploymentConfiguration.KEY_PROXY_FTP_PORT,
                        null,
                        null
                },
                {
                        DeploymentConfiguration.KEY_PROXY_SOCKS4_HOST,
                        null,
                        null
                },
                {
                        DeploymentConfiguration.KEY_PROXY_SOCKS4_PORT,
                        null,
                        null
                },
                {
                        DeploymentConfiguration.KEY_PROXY_OVERRIDE_HOSTS,
                        null,
                        null
                },
                /* cache and optional package repository */
                {
                        "deployment.cache.max.size",
                        BasicValueValidators.getRangedIntegerValidator(-1, Integer.MAX_VALUE),
                        String.valueOf("-1")
                },
                {
                        "deployment.cache.jarcompression",
                        BasicValueValidators.getRangedIntegerValidator(0, 10),
                        String.valueOf(0)
                },
                {
                        "deployment.javapi.cache.enabled",
                        BasicValueValidators.getBooleanValidator(),
                        String.valueOf(false)
                },
                /* java console */
                {
                        DeploymentConfiguration.KEY_CONSOLE_STARTUP_MODE,
                        BasicValueValidators.getStringValidator(new String[] {
                                DeploymentConfiguration.CONSOLE_DISABLE,
                                DeploymentConfiguration.CONSOLE_HIDE,
                                DeploymentConfiguration.CONSOLE_SHOW
                        }),
                        DeploymentConfiguration.CONSOLE_HIDE
                },
                /* tracing and logging */
                {
                        DeploymentConfiguration.KEY_ENABLE_TRACING,
                        BasicValueValidators.getBooleanValidator(),
                        String.valueOf(false)
                },
                {
                        DeploymentConfiguration.KEY_ENABLE_LOGGING,
                        BasicValueValidators.getBooleanValidator(),
                        String.valueOf(false)
                },
                /* JNLP association */
                {
                        DeploymentConfiguration.KEY_JNLP_ASSOCIATIONS,
                        BasicValueValidators.getRangedIntegerValidator(DeploymentConfiguration.JNLP_ASSOCIATION_NEVER,
                                DeploymentConfiguration.JNLP_ASSOCIATION_REPLACE_ASK),
                        String.valueOf(DeploymentConfiguration.JNLP_ASSOCIATION_ASK_USER)
                },
                /* desktop integration */
                {
                        DeploymentConfiguration.KEY_CREATE_DESKTOP_SHORTCUT,
                        BasicValueValidators.getStringValidator(new String[] {
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
                        DeploymentConfiguration.KEY_JRE_INTSTALL_URL,
                        BasicValueValidators.getUrlValidator(),
                        null
                },
                /* jre management */
                {
                        DeploymentConfiguration.KEY_AUTO_DOWNLOAD_JRE,
                        BasicValueValidators.getBooleanValidator(),
                        String.valueOf(false)
                },
                /* browser selection */
                {
                        DeploymentConfiguration.KEY_BROWSER_PATH,
                        BasicValueValidators.getFilePathValidator(),
                        null
                },
                /* check for update timeout */
                {
                        DeploymentConfiguration.KEY_UPDATE_TIMEOUT,
                        BasicValueValidators.getRangedIntegerValidator(0, 10000),
                        String.valueOf(500)
                }
        };

        HashMap<String, Setting<String>> result = new HashMap<String, Setting<String>>();
        for (int i = 0; i < defaults.length; i++) {
            String name = (String) defaults[i][0];
            ValueValidator checker = (ValueValidator) defaults[i][1];
            String actualValue = (String) defaults[i][2];
            boolean locked = false;
            Setting<String> value = new Setting<String>(name, R("Unknown"), locked, checker, actualValue, actualValue, R("DCSourceInternal"));
            result.put(name, value);
        }

        return result;
    }
}
