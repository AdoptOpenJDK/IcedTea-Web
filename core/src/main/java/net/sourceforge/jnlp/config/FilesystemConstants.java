package net.sourceforge.jnlp.config;

import net.sourceforge.jnlp.runtime.JNLPRuntime;

public interface FilesystemConstants {

    String ICEDTEA_SO = "IcedTeaPlugin.so";

    String CACHE_INDEX_FILE_NAME = "recently_used";

    String XDG_CONFIG_HOME_VAR = "XDG_CONFIG_HOME";

    String XDG_CACHE_HOME_VAR = "XDG_CACHE_HOME";

    String XDG_RUNTIME_DIR_VAR = "XDG_RUNTIME_DIR";

    String XDG_DATA_HOME = "XDG_DATA_HOME";

    String WINDIR = "WINDIR";

    String TMP_PROP = "java.io.tmpdir";

    String HOME_PROP = "user.home";

    String JAVA_PROP = "java.home";

    String USER_PROP = "user.name";

    String VARIABLE = JNLPRuntime.isWindows() ? "%" : "$";

    String SECURITY_WORD = "security";
}
