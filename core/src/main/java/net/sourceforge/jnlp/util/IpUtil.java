package net.sourceforge.jnlp.util;

import inet.ipaddr.HostName;
import inet.ipaddr.HostNameException;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import net.adoptopenjdk.icedteaweb.StringUtils;

import java.net.URI;
import java.net.URL;

public class IpUtil {
    public static boolean isLocalhostOrLoopback(final URL url) {
        return isLocalhostOrLoopback(url.getHost());
    }

    public static boolean isLocalhostOrLoopback(final URI uri) {
        return isLocalhostOrLoopback(uri.getHost());
    }

    /**
     * @param host host string to verify
     * @return true if the given host string is blank or represents or resolves to the hostname or the IP address
     * of localhost or the loopback address.
     */
    static boolean isLocalhostOrLoopback(final String host) {
        if (StringUtils.isBlank(host)) {
            return true; // java.net.InetAddress.getByName(host).isLoopbackAddress() returns true
        }
        final HostName hostName = new HostName(host);
        return hostName.resolvesToSelf();
    }

    /**
     * @param host host string to verify
     * @return true if the given host string is an IP address
     */
    static boolean isHostAnIPAddress(final String host) {

        final HostName hostName = new HostName(host);
        return hostName.asAddress(IPAddress.IPVersion.IPV4) != null;
    }

    static void validateIPHost(final String host) throws HostNameException {
        final HostName hostName = new HostName(host);
        hostName.validate();
    }
}
