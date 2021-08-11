package net.sourceforge.jnlp.config;

import java.nio.channels.FileLock;

public interface ConfigurationConstants {

    String DEPLOYMENT_CONFIG_FILE = "deployment.config";
    String ITW_DEPLOYMENT_CONFIG_FILE = "itw-deployment.config";

    String DEPLOYMENT_PROPERTIES = "deployment.properties";

    String APPLET_TRUST_SETTINGS = ".appletTrustSettings";

    String DEPLOYMENT_COMMENT = "Netx deployment configuration";

    int JNLP_ASSOCIATION_NEVER = 0;

    int JNLP_ASSOCIATION_ASK_USER = 2;

    int JNLP_ASSOCIATION_REPLACE_ASK = 3;

    /**
     * when set to as value of KEY_CONSOLE_STARTUP_MODE = "deployment.console.startup.mode",
     * then console is not visible by default, but may be shown
     */
    String CONSOLE_HIDE = "HIDE";

    /**
     * when set to as value of KEY_CONSOLE_STARTUP_MODE = "deployment.console.startup.mode",
     * then console show for both javaws and plugin
     */
    String CONSOLE_SHOW = "SHOW";

    /**
     * when set to as value of KEY_CONSOLE_STARTUP_MODE = "deployment.console.startup.mode",
     * then console is not visible by default, nop data are passed to it (save memory and cpu) but can not be shown
     */
    String CONSOLE_DISABLE = "DISABLE";

    /**
     * when set to as value of KEY_CONSOLE_STARTUP_MODE = "deployment.console.startup.mode",
     * then console show for  plugin
     */
    String CONSOLE_SHOW_PLUGIN = "SHOW_PLUGIN_ONLY";

    /**
     * when set to as value of KEY_CONSOLE_STARTUP_MODE = "deployment.console.startup.mode",
     * then console show for javaws
     */
    String CONSOLE_SHOW_JAVAWS = "SHOW_JAVAWS_ONLY";

    String KEY_USER_CACHE_DIR = "deployment.user.cachedir";

    String KEY_USER_PERSISTENCE_CACHE_DIR = "deployment.user.pcachedir";

    String KEY_SYSTEM_CACHE_DIR = "deployment.system.cachedir";

    String KEY_CACHE_MAX_SIZE = "deployment.cache.max.size";

    String KEY_CACHE_ENABLED = "deployment.javapi.cache.enabled";

    String KEY_CACHE_COMPRESSION_ENABLED = "deployment.cache.jarcompression";

    String KEY_USER_LOG_DIR = "deployment.user.logdir";

    String KEY_USER_TMP_DIR = "deployment.user.tmp";

    /**
     * the directory containing locks for single instance applications
     */
    String KEY_USER_LOCKS_DIR = "deployment.user.locksdir";

    /**
     * The netx_running file is used to indicate if any instances of netx are
     * running (this file may exist even if no instances are running). All netx
     * instances acquire a shared lock on this file. If this file can be locked
     * (using a {@link FileLock}) in exclusive mode, then other netx instances
     * are not running
     */
    String KEY_USER_NETX_RUNNING_FILE = "deployment.user.runningfile";

    String KEY_USER_SECURITY_POLICY = "deployment.user.security.policy";

    String KEY_USER_TRUSTED_CA_CERTS = "deployment.user.security.trusted.cacerts";

    String KEY_USER_TRUSTED_JSSE_CA_CERTS = "deployment.user.security.trusted.jssecacerts";

    String KEY_USER_TRUSTED_CERTS = "deployment.user.security.trusted.certs";

    String KEY_USER_TRUSTED_JSSE_CERTS = "deployment.user.security.trusted.jssecerts";

    String KEY_USER_TRUSTED_CLIENT_CERTS = "deployment.user.security.trusted.clientauthcerts";

    String KEY_SYSTEM_SECURITY_POLICY = "deployment.system.security.policy";

    String KEY_SYSTEM_TRUSTED_CA_CERTS = "deployment.system.security.cacerts";

    String KEY_SYSTEM_TRUSTED_JSSE_CA_CERTS = "deployment.system.security.jssecacerts";

    String KEY_SYSTEM_TRUSTED_CERTS = "deployment.system.security.trusted.certs";

    String KEY_SYSTEM_TRUSTED_JSSE_CERTS = "deployment.system.security.trusted.jssecerts";

    String KEY_SYSTEM_TRUSTED_CLIENT_CERTS = "deployment.system.security.trusted.clientautcerts";

    String KEY_SECURITY_ASKGRANTDIALOG_NOTINCA = "deployment.security.askgrantdialog.notinca";

    String KEY_SECURITY_NOTINCA_WARNING = "deployment.security.notinca.warning";

    String KEY_SECURITY_EXPIRED_WARNING = "deployment.security.expired.warning";

    String KEY_SECURITY_JSSE_HOSTMISMATCH_WARNING = "deployment.security.jsse.hostmismatch.warning";
    
    
    /**
     * Properties to manage to access Windows key stores
     */
    String KEY_SECURITY_USE_ROOTCA_STORE_TYPE_WINDOWS_ROOT = "deployment.security.use.rootca.store.type.windowsRoot";
    
    String KEY_SECURITY_USE_ROOTCA_STORE_TYPE_WINDOWS_MY = "deployment.security.use.rootca.store.type.windowsMy";        
    

    /**
     * Boolean. Only show security prompts to user if true
     */
    String KEY_SECURITY_PROMPT_USER = "deployment.security.askgrantdialog.show";

    //enum of AppletSecurityLevel in result
    String KEY_SECURITY_LEVEL = "deployment.security.level";

    String KEY_SECURITY_TRUSTED_POLICY = "deployment.security.trusted.policy";

    /**
     * Boolean. Only give AWTPermission("showWindowWithoutWarningBanner") if true
     */
    String KEY_SECURITY_ALLOW_HIDE_WINDOW_WARNING = "deployment.security.sandbox.awtwarningwindow";

    /**
     * Boolean. Only prompt user for granting any JNLP permissions if true
     */
    String KEY_SECURITY_PROMPT_USER_FOR_JNLP = "deployment.security.sandbox.jnlp.enhanced";

    String KEY_PARALLEL_RESOURCE_DOWNLOAD_COUNT = "deployment.cache.parallelDownloadCount";

    /**
     * Boolean. Only install the custom authenticator if true
     */
    String KEY_SECURITY_ITW_IGNORECERTISSUES = "deployment.security.itw.ignorecertissues";

    /**
     * Boolean. Create regular files instead of restricted files if true.
     */
    String KEY_SECURITY_DISABLE_RESTRICTED_FILES = "deployment.security.itw.disablerestrictedfiles";

    String KEY_STRICT_JNLP_CLASSLOADER = "deployment.jnlpclassloader.strict";

    /**
     * Boolean. Do not prefer https over http
     */
    String KEY_HTTPS_DONT_ENFORCE = "deployment.https.noenforce";

    /**
     * the proxy type. possible values are {@code JNLPProxySelector.PROXY_TYPE_*}
     */
    String KEY_PROXY_TYPE = "deployment.proxy.type";

    /**
     * Boolean. If true, the http host/port should be used for https and ftp as well
     */
    String KEY_PROXY_SAME = "deployment.proxy.same";

    String KEY_PROXY_AUTO_CONFIG_URL = "deployment.proxy.auto.config.url";

    String KEY_PROXY_BYPASS_LIST = "deployment.proxy.bypass.list";

    String KEY_PROXY_BYPASS_LOCAL = "deployment.proxy.bypass.local";

    String KEY_PROXY_HTTP_HOST = "deployment.proxy.http.host";

    String KEY_PROXY_HTTP_PORT = "deployment.proxy.http.port";

    String KEY_PROXY_HTTPS_HOST = "deployment.proxy.https.host";

    String KEY_PROXY_HTTPS_PORT = "deployment.proxy.https.port";

    String KEY_PROXY_FTP_HOST = "deployment.proxy.ftp.host";

    String KEY_PROXY_FTP_PORT = "deployment.proxy.ftp.port";

    String KEY_PROXY_SOCKS4_HOST = "deployment.proxy.socks.host";

    String KEY_PROXY_SOCKS4_PORT = "deployment.proxy.socks.port";

    String KEY_PROXY_OVERRIDE_HOSTS = "deployment.proxy.override.hosts";

    /*
     * Logging
     */
    String KEY_ENABLE_DEBUG_LOGGING = "deployment.log"; //same as verbose or ICEDTEAPLUGIN_DEBUG=true

    String KEY_ENABLE_LOGGING_HEADERS = "deployment.log.headers"; //will add header OutputContorll.getHeader To all messages

    String KEY_ENABLE_LOGGING_TOFILE = "deployment.log.file";

    String KEY_ENABLE_APPLICATION_LOGGING_TOFILE = "deployment.log.file.clientapp"; //also client app will log to its separate file

    String KEY_ENABLE_LEGACY_LOGBASEDFILELOG = "deployment.log.file.legacylog";

    String KEY_ENABLE_LOGGING_TOSTREAMS = "deployment.log.stdstreams";

    String KEY_ENABLE_LOGGING_TOSYSTEMLOG = "deployment.log.system";

    /*
     * manifest check
     */
    String KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK = "deployment.manifest.attributes.check";

    String KEY_ASSUME_FILE_STEM_IN_CODEBASE = "deployment.assumeFileSystemInCodebase";

    /**
     * Console initial status.
     * One of CONSOLE_* values
     * See declaration above:
     * CONSOLE_HIDE = "HIDE";
     * CONSOLE_SHOW = "SHOW";
     * CONSOLE_DISABLE = "DISABLE";
     * CONSOLE_SHOW_PLUGIN = "SHOW_PLUGIN_ONLY";
     * CONSOLE_SHOW_JAVAWS = "SHOW_JAVAWS_ONLY";
     */
    String KEY_CONSOLE_STARTUP_MODE = "deployment.console.startup.mode";

    String KEY_JNLP_ASSOCIATIONS = "deployment.javaws.associations";

    String KEY_CREATE_DESKTOP_SHORTCUT = "deployment.javaws.shortcut";

    String KEY_JRE_INTSTALL_URL = "deployment.javaws.installURL";

    String KEY_AUTO_DOWNLOAD_JRE = "deployment.javaws.autodownload";

    String KEY_BROWSER_PATH = "deployment.browser.path";

    //for legacy reasons, also $BROWSER variable is supported
    String BROWSER_ENV_VAR = "BROWSER";

    // both browser.path and BROWSER can ave those for-fun keys:
    String ALWAYS_ASK = "ALWAYS-ASK";

    String INTERNAL_HTML = "INTERNAL-HTML";

    String LEGACY_WIN32_URL__HANDLER = "rundll32 url.dll,FileProtocolHandler ";

    String KEY_UPDATE_TIMEOUT = "deployment.javaws.update.timeout";

    String IGNORE_HEADLESS_CHECK = "deployment.headless.ignore";

    /*
     * JVM arguments for plugin
     */
    String KEY_PLUGIN_JVM_ARGUMENTS = "deployment.plugin.jvm.arguments";

    String KEY_JRE_DIR = "deployment.jre.dir";

    /**
     * remote configuration properties
     */
    String KEY_SYSTEM_CONFIG = "deployment.system.config";

    String KEY_SYSTEM_CONFIG_MANDATORY = "deployment.system.config.mandatory";

    /**
     * Possibility to control hack which resizes very small applets
     */
    String KEY_SMALL_SIZE_OVERRIDE_THRESHOLD = "deployment.small.size.threshold";

    String KEY_SMALL_SIZE_OVERRIDE_WIDTH = "deployment.small.size.override.width";

    String KEY_SMALL_SIZE_OVERRIDE_HEIGHT = "deployment.small.size.override.height";

    String VV_POSSIBLE_BROWSER_VALUES = "VVPossibleBrowserValues";
    String ICEDTEA_SO = "IcedTeaPlugin.so";
    String CACHE_INDEX_FILE_NAME = "recently_used";
    String WINDIR = "WINDIR";
    String SECURITY_WORD = "security";
    String DEPLOYMENT_SUBDIR_DIR = "icedtea-web";
    String XDG_CONFIG_HOME_VAR = "XDG_CONFIG_HOME";
    String XDG_CACHE_HOME_VAR = "XDG_CACHE_HOME";
    String XDG_RUNTIME_DIR_VAR = "XDG_RUNTIME_DIR";
    String XDG_DATA_HOME_VAR = "XDG_DATA_HOME";
    String WIN_VARIABLE_PREFIX = "%";
    String UNIX_VARIABLE_PREFIX = "$";

    /*
     * Native (rust)
     */
    String KEY_LAUNCHER_RUST_CP_ADD = "deployment.launcher.rust.cp.add";
    String KEY_LAUNCHER_RUST_CP_REMOVE = "deployment.launcher.rust.cp.remove";
    String KEY_LAUNCHER_RUST_BOOTCP_ADD = "deployment.launcher.rust.bootcp.add";
    String KEY_LAUNCHER_RUST_BOOTCP_REMOVE = "deployment.launcher.rust.bootcp.remove";

    /*
     * CSV
     */
    String KEY_SECURITY_SERVER_WHITELIST = "deployment.security.whitelist";
    String KEY_JVM_ARGS_WHITELIST = "deployment.jvm.arguments.whitelist";

    /*
     * HTTP Connection properties
     */
    String KEY_HTTPCONNECTION_CONNECT_TIMEOUT = "deployment.connection.connectTimeout";
    String KEY_HTTPCONNECTION_READ_TIMEOUT = "deployment.connection.readTimeout";
}
