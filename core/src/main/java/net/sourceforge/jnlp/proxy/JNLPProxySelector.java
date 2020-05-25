// Copyright (C) 2010 Red Hat, Inc.
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

package net.sourceforge.jnlp.proxy;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.StringUtils;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.proxy.pac.PacEvaluator;
import net.sourceforge.jnlp.proxy.pac.PacEvaluatorFactory;
import net.sourceforge.jnlp.proxy.pac.PacUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static net.sourceforge.jnlp.proxy.ProxyConstants.FTP_SCHEMA;
import static net.sourceforge.jnlp.proxy.ProxyConstants.HTTPS_SCHEMA;
import static net.sourceforge.jnlp.proxy.ProxyConstants.HTTP_SCHEMA;
import static net.sourceforge.jnlp.proxy.ProxyConstants.SOCKET_SCHEMA;
import static net.sourceforge.jnlp.util.IpUtil.isLocalhostOrLoopback;

/**
 * A ProxySelector specific to JNLPs. This proxy uses the deployment
 * configuration to determine what to do.
 *
 * @see java.net.ProxySelector
 */
public abstract class JNLPProxySelector extends ProxySelector {

    private final static Logger LOG = LoggerFactory.getLogger(JNLPProxySelector.class);

    private PacEvaluator pacEvaluator = null;

    /** the URL to the PAC file */
    private URL autoConfigUrl = null;



    /** whether localhost should be bypassed for proxy purposes */
    private boolean bypassLocal = false;



    /** The proxy type. See PROXY_TYPE_* constants */
    private final ProxyType proxyType;

    private final String proxyHttpHost;
    private final int proxyHttpPort;
    private final String proxyHttpsHost;
    private final int proxyHttpsPort;
    private final String proxyFtpHost;
    private final int proxyFtpPort;
    private final String proxySocks4Host;
    private final int proxySocks4Port;

    /** a list of URLs that should be bypassed for proxy purposes */
    private final List<String> bypassList;

    /**
     * whether the http proxy should be used for https and ftp protocols as well
     */
    private final boolean sameProxy;

    public JNLPProxySelector(DeploymentConfiguration config) {
        Assert.requireNonNull(config, "config");
        final int proxyTypeConfigValue = Integer.valueOf(config.getProperty(ConfigurationConstants.KEY_PROXY_TYPE));
        proxyType = ProxyType.getForConfigValue(proxyTypeConfigValue);
        final String autoConfigUrlProperty = config.getProperty(ConfigurationConstants.KEY_PROXY_AUTO_CONFIG_URL);

        if (autoConfigUrlProperty != null) {
            try {
                autoConfigUrl = new URL(autoConfigUrlProperty);
            } catch (final MalformedURLException e) {
                LOG.error("Can not parse auto config url for proxy: '" +autoConfigUrl + "'" , e);
            }
        }

        if (autoConfigUrl != null) {
            pacEvaluator = PacEvaluatorFactory.getPacEvaluator(autoConfigUrl);
        }

        bypassList = config.getPropertyAsList(ConfigurationConstants.KEY_PROXY_BYPASS_LIST).stream()
                .filter(s -> !StringUtils.isBlank(s))
                .collect(Collectors.toList());

        bypassLocal = Boolean.valueOf(config
                .getProperty(ConfigurationConstants.KEY_PROXY_BYPASS_LOCAL));

        sameProxy = Boolean.valueOf(config.getProperty(ConfigurationConstants.KEY_PROXY_SAME));

        proxyHttpHost = getHost(config, ConfigurationConstants.KEY_PROXY_HTTP_HOST);
        proxyHttpPort = getPort(config, ConfigurationConstants.KEY_PROXY_HTTP_PORT);

        proxyHttpsHost = getHost(config, ConfigurationConstants.KEY_PROXY_HTTPS_HOST);
        proxyHttpsPort = getPort(config, ConfigurationConstants.KEY_PROXY_HTTPS_PORT);

        proxyFtpHost = getHost(config, ConfigurationConstants.KEY_PROXY_FTP_HOST);
        proxyFtpPort = getPort(config, ConfigurationConstants.KEY_PROXY_FTP_PORT);

        proxySocks4Host = getHost(config, ConfigurationConstants.KEY_PROXY_SOCKS4_HOST);
        proxySocks4Port = getPort(config, ConfigurationConstants.KEY_PROXY_SOCKS4_PORT);
    }

    /**
     * Uses the given key to get a host from the configuration
     */
    private String getHost(DeploymentConfiguration config, String key) {
        String proxyHost = config.getProperty(key);
        if (proxyHost != null) {
            proxyHost = proxyHost.trim();
        }
        return proxyHost;
    }

    /**
     * Uses the given key to get a port from the configuration
     */
    private int getPort(DeploymentConfiguration config, String key) {
        int proxyPort = ProxyConstants.FALLBACK_PROXY_PORT;
        String port;
        port = config.getProperty(key);
        if (port != null && port.trim().length() != 0) {
            try {
                proxyPort = Integer.valueOf(port);
            } catch (NumberFormatException e) {
                LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
            }
        }
        return proxyPort;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ioe);
    }

    /**
     * {@inheritDoc}
     * @return list of proxies on URI
     */
    @Override
    public List<Proxy> select(URI uri) {
        LOG.debug("Selecting proxy for: {}", uri);
        
        if (inBypassList(uri)) {
            List<Proxy> proxies = Arrays.asList(new Proxy[] { Proxy.NO_PROXY });
            LOG.debug("Selected proxies: {}", Arrays.toString(proxies.toArray()));
            return proxies;
        }

        List<Proxy> proxies = new ArrayList<>();

        switch (proxyType) {
            case PROXY_TYPE_MANUAL:
                proxies.addAll(getFromConfiguration(uri));
                break;
            case PROXY_TYPE_AUTO:
                proxies.addAll(getFromPAC(uri));
                break;
            case PROXY_TYPE_BROWSER:
                proxies.addAll(getFromBrowser(uri));
                break;
            case PROXY_TYPE_UNKNOWN:
                // fall through
            case PROXY_TYPE_NONE:
                // fall through
            default:
                proxies.add(Proxy.NO_PROXY);
                break;
        }

        LOG.debug("Selected proxies: {}", Arrays.toString(proxies.toArray()));
        return proxies;
    }

    /**
     * Returns true if the uri should be bypassed for proxy purposes
     */
    private boolean inBypassList(URI uri) {
        try {
            String scheme = uri.getScheme();
            /* scheme can be http/https/ftp/socket */
            switch (scheme) {
                case HTTP_SCHEMA:
                case HTTPS_SCHEMA:
                case FTP_SCHEMA:
                    URL url = uri.toURL();
                    if (bypassLocal && isLocalhostOrLoopback(url)) {
                        return true;
                    }
                    if (bypassList.contains(url.getHost())) {
                        return true;
                    }
                    break;
                case SOCKET_SCHEMA:
                    if (bypassLocal && isLocalhostOrLoopback(uri)) {
                        return true;
                    }
                    if (bypassList.contains(uri.getHost())) {
                        return true;
                    }
                    break;
            }
        } catch (MalformedURLException e) {
            return false;
        }

        return false;
    }

    /**
     * Returns a list of proxies by using the information in the deployment
     * configuration
     *
     * @param uri uri to read
     * @return a List of Proxy objects
     */
    private List<Proxy> getFromConfiguration(URI uri) {
        return getFromArguments(uri, sameProxy, false,
                proxyHttpsHost, proxyHttpsPort,
                proxyHttpHost, proxyHttpPort,
                proxyFtpHost, proxyFtpPort,
                proxySocks4Host, proxySocks4Port);
    }

    /**
     * Returns a list of proxies by using the arguments
     *
     * @param uri name and code says it all
     * @param sameProxy name and code says it all
     * @param sameProxyIncludesSocket name and code says it all
     * @param proxyHttpsHost name and code says it all
     * @param proxyHttpsPort name and code says it all
     * @param proxyHttpHost name and code says it all
     * @param proxyHttpPort name and code says it all
     * @param proxyFtpHost name and code says it all
     * @param proxyFtpPort name and code says it all
     * @param proxySocks4Host name and code says it all
     * @param proxySocks4Port name and code says it all
     * @return a List of Proxy objects
     */
    protected static List<Proxy> getFromArguments(URI uri,
            boolean sameProxy, boolean sameProxyIncludesSocket,
            String proxyHttpsHost, int proxyHttpsPort,
            String proxyHttpHost, int proxyHttpPort,
            String proxyFtpHost, int proxyFtpPort,
            String proxySocks4Host, int proxySocks4Port) {

        List<Proxy> proxies = new ArrayList<>();

        String scheme = uri.getScheme();

        boolean socksProxyAdded = false;

        if (sameProxy) {
            if (proxyHttpHost != null) {
                SocketAddress sa = new InetSocketAddress(proxyHttpHost, proxyHttpPort);
                if ((scheme.equals(HTTPS_SCHEMA) || scheme.equals(HTTP_SCHEMA) || scheme.equals(FTP_SCHEMA))) {
                    Proxy proxy = new Proxy(Type.HTTP, sa);
                    proxies.add(proxy);
                } else if (scheme.equals(SOCKET_SCHEMA) && sameProxyIncludesSocket) {
                    Proxy proxy = new Proxy(Type.SOCKS, sa);
                    proxies.add(proxy);
                    socksProxyAdded = true;
                }
            }
        } else if (scheme.equals(HTTP_SCHEMA) && proxyHttpHost != null) {
            SocketAddress sa = new InetSocketAddress(proxyHttpHost, proxyHttpPort);
            proxies.add(new Proxy(Type.HTTP, sa));
        } else if (scheme.equals(HTTPS_SCHEMA) && proxyHttpsHost != null) {
            SocketAddress sa = new InetSocketAddress(proxyHttpsHost, proxyHttpsPort);
            proxies.add(new Proxy(Type.HTTP, sa));
        } else if (scheme.equals(FTP_SCHEMA) && proxyFtpHost != null) {
            SocketAddress sa = new InetSocketAddress(proxyFtpHost, proxyFtpPort);
            proxies.add(new Proxy(Type.HTTP, sa));
        }

        if (!socksProxyAdded && (proxySocks4Host != null)) {
            SocketAddress sa = new InetSocketAddress(proxySocks4Host, proxySocks4Port);
            proxies.add(new Proxy(Type.SOCKS, sa));
        }

        if (proxies.isEmpty()) {
            proxies.add(Proxy.NO_PROXY);
        }

        return proxies;
    }

    /**
     * Returns a list of proxies by using the Proxy Auto Config (PAC) file. See
     * http://en.wikipedia.org/wiki/Proxy_auto-config#The_PAC_file for more
     * information.
     *
     * @param uri uri to PAC
     * @return a List of valid Proxy objects
     */
    private List<Proxy> getFromPAC(URI uri) {
        if (autoConfigUrl == null || uri.getScheme().equals(SOCKET_SCHEMA)) {
            return Arrays.asList(new Proxy[] { Proxy.NO_PROXY });
        }

        List<Proxy> proxies = new ArrayList<>();

        try {
            String proxiesString = pacEvaluator.getProxies(uri.toURL());
            proxies.addAll(PacUtils.getProxiesFromPacResult(proxiesString));
        } catch (MalformedURLException e) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
            proxies.add(Proxy.NO_PROXY);
        }

        return proxies;
    }

    /**
     * Returns a list of proxies by querying the browser
     *
     * @param uri the uri to get proxies for
     * @return a list of proxies
     */
    protected abstract List<Proxy> getFromBrowser(URI uri);

}
