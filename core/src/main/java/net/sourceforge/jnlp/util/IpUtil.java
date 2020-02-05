package net.sourceforge.jnlp.util;

import inet.ipaddr.HostName;
import net.adoptopenjdk.icedteaweb.StringUtils;

import java.net.URI;
import java.net.URL;

public class IpUtil {
    public static boolean isLocalhostOrLoopback(URL url) {
        return isLocalhostOrLoopback(url.getHost());
    }

    public static boolean isLocalhostOrLoopback(URI uri) {
        return isLocalhostOrLoopback(uri.getHost());
    }

    /**
     * @param host host string to verify
     * @return true if the given host string represents or resolves to the hostname or the IP address
     * of localhost or the loopback address.
     */
    static boolean isLocalhostOrLoopback(String host) {
        if (StringUtils.isBlank(host)) {
            return false;
        }
        HostName hostName = new HostName(host);
        return hostName.resolvesToSelf();
    }
}
