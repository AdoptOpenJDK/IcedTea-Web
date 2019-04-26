package net.sourceforge.jnlp.config;

import net.sourceforge.jnlp.runtime.JNLPRuntime;

import java.io.File;
import java.util.Optional;

public interface FilesystemConstants {

    String ICEDTEA_SO = "IcedTeaPlugin.so";

    String CACHE_INDEX_FILE_NAME = "recently_used";

    String WINDIR = "WINDIR";

    String TMP_PROP = "java.io.tmpdir";

    String JAVA_PROP = "java.home";

    String USER_PROP = "user.name";

    String SECURITY_WORD = "security";

    String DEPLOYMENT_SUBDIR_DIR = "icedtea-web";

    String VARIABLE = JNLPRuntime.isWindows() ? "%" : "$";

    String HOME_PROP = "user.home";

    String XDG_CONFIG_HOME_VAR = "XDG_CONFIG_HOME";

    String XDG_CACHE_HOME_VAR = "XDG_CACHE_HOME";

    String XDG_RUNTIME_DIR_VAR = "XDG_RUNTIME_DIR";

    String XDG_DATA_HOME_VAR = "XDG_DATA_HOME";

    String BASE_DATA_HOME = System.getProperty(HOME_PROP) +  File.separator + ".local" + File.separator + "share";
    String BASE_CACHE_HOME = System.getProperty(HOME_PROP) + File.separator + ".cache";
    String BASE_CONFIG_HOME = System.getProperty(HOME_PROP) + File.separator + ".config";
    String BASE_RUNTIME_HOME = System.getProperty(TMP_PROP);

    String XDG_CONFIG_HOME = System.getenv(XDG_CONFIG_HOME_VAR);
    String XDG_CACHE_HOME = System.getenv(XDG_CACHE_HOME_VAR);
    String XDG_RUNTIME_HOME = System.getenv(XDG_RUNTIME_DIR_VAR);
    String XDG_DATA_HOME = System.getenv(XDG_DATA_HOME_VAR);

    String CONFIG_HOME = Optional.ofNullable(XDG_CONFIG_HOME).orElse(BASE_CONFIG_HOME);
    String CACHE_HOME = Optional.ofNullable(XDG_CACHE_HOME).orElse(BASE_CACHE_HOME);
    String RUNTIME_HOME = Optional.ofNullable(XDG_RUNTIME_HOME).orElse(BASE_RUNTIME_HOME);
    String DATA_HOME = Optional.ofNullable(XDG_DATA_HOME).orElse(BASE_DATA_HOME);
}
