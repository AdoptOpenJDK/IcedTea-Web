package net.sourceforge.jnlp.util.whitelist;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.StringUtils;
import net.adoptopenjdk.icedteaweb.client.BasicExceptionDialog;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.IpUtil;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import static net.sourceforge.jnlp.config.ConfigurationConstants.KEY_SECURITY_SERVER_WHITELIST;

public class UrlWhiteListUtils {

    private static final Logger LOG = LoggerFactory.getLogger(UrlWhiteListUtils.class);

    private static List<WhitelistEntry> applicationUrlWhiteList;

    public static List<WhitelistEntry> getApplicationUrlWhiteList() {
        if (applicationUrlWhiteList == null) {
            applicationUrlWhiteList = loadWhitelistFromConfiguration(KEY_SECURITY_SERVER_WHITELIST);
        }
        return applicationUrlWhiteList;
    }

    public static List<WhitelistEntry> loadWhitelistFromConfiguration(final String whitelistPropertyName) {
        return JNLPRuntime.getConfiguration().getPropertyAsList(whitelistPropertyName)
                .stream()
                .filter(s -> !StringUtils.isBlank(s))
                .map(UrlWhiteListUtils::parseEntry)
                .collect(Collectors.toList());
    }

    public static boolean isUrlInWhitelist(final URL url, final List<WhitelistEntry> whiteList) {
        Assert.requireNonNull(url, "url");
        Assert.requireNonNull(whiteList, "whiteList");

        if (whiteList.isEmpty()) {
            return true; // empty whitelist == allow all connections
        }

        // is it localhost or loopback
        if (IpUtil.isLocalhostOrLoopback(url)) {
            return true; // localhost need not be in whitelist
        }

        return whiteList.stream().anyMatch(wlEntry -> wlEntry.matches(url));
    }

    static WhitelistEntry parseEntry(final String wlUrlStr) {
        Assert.requireNonNull(wlUrlStr, "wlUrlStr");
        return WhitelistEntry.parse(wlUrlStr);
    }

    public static void validateWithApplicationWhiteList(URL url) {
        Assert.requireNonNull(url, "url");

        // Validate with whitelist specified in deployment.properties. localhost is considered valid.
        final boolean found = isUrlInWhitelist(url, getApplicationUrlWhiteList());
        if (!found) {
            BasicExceptionDialog.show(new SecurityException(Translator.R("SWPInvalidURL") + ": " + url));
            LOG.error("Resource URL not In Whitelist: {}", url);
            JNLPRuntime.exit(-1);
        }
    }
}
