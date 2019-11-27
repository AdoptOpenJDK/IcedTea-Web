// Copyright (C) 2010 Red Hat, Inc.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

package net.sourceforge.jnlp.proxy.old;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.proxy.util.config.ProxyConfigurationImpl;
import net.sourceforge.jnlp.proxy.util.ProxyConstants;
import net.sourceforge.jnlp.proxy.util.pac.PacEvaluator;
import net.sourceforge.jnlp.proxy.util.pac.PacUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import static net.sourceforge.jnlp.proxy.util.ProxyConstants.FTP_SCHEMA;
import static net.sourceforge.jnlp.proxy.util.ProxyConstants.HTTPS_SCHEMA;
import static net.sourceforge.jnlp.proxy.util.ProxyConstants.HTTP_SCHEMA;
import static net.sourceforge.jnlp.proxy.util.ProxyConstants.SOCKET_SCHEMA;

/**
 * A ProxySelector specific to JNLPs. This proxy uses the deployment
 * configuration to determine what to do.
 *
 * @see java.net.ProxySelector
 */
public abstract class JNLPProxySelector extends ProxySelector {

    private final static Logger LOG = LoggerFactory.getLogger(JNLPProxySelector.class);

    private PacEvaluator pacEvaluator = null;


    /** The proxy type. See PROXY_TYPE_* constants */
    private final ProxyType proxyType;


    /** the URL to the PAC file */
    private URL autoConfigUrl = null;

    private final ProxyConfigurationImpl proxyConfiguration;

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
            pacEvaluator = new PacEvaluator(autoConfigUrl);
        }

        proxyConfiguration = new ProxyConfigurationImpl();
        proxyConfiguration.setBypassLocal(Boolean.valueOf(config.getProperty(ConfigurationConstants.KEY_PROXY_BYPASS_LOCAL)));
        proxyConfiguration.setUseHttpForHttpsAndFtp(Boolean.valueOf(config.getProperty(ConfigurationConstants.KEY_PROXY_SAME)));
        proxyConfiguration.setHttpHost(getHost(config, ConfigurationConstants.KEY_PROXY_HTTP_HOST));
        proxyConfiguration.setHttpPort(getPort(config, ConfigurationConstants.KEY_PROXY_HTTP_PORT));
        proxyConfiguration.setHttpsHost(getHost(config, ConfigurationConstants.KEY_PROXY_HTTPS_HOST));
        proxyConfiguration.setHttpsPort(getPort(config, ConfigurationConstants.KEY_PROXY_HTTPS_PORT));
        proxyConfiguration.setFtpHost(getHost(config, ConfigurationConstants.KEY_PROXY_FTP_HOST));
        proxyConfiguration.setFtpPort(getPort(config, ConfigurationConstants.KEY_PROXY_FTP_PORT));
        proxyConfiguration.setSocksHost(getHost(config, ConfigurationConstants.KEY_PROXY_SOCKS4_HOST));
        proxyConfiguration.setSocksPort(getPort(config, ConfigurationConstants.KEY_PROXY_SOCKS4_PORT));
        String proxyBypass = config.getProperty(ConfigurationConstants.KEY_PROXY_BYPASS_LIST);
        if (proxyBypass != null) {
            StringTokenizer tokenizer = new StringTokenizer(proxyBypass, ",");
            while (tokenizer.hasMoreTokens()) {
                String host = tokenizer.nextToken();
                if (host != null && host.trim().length() != 0) {
                    proxyConfiguration.addToBypassList(host);
                }
            }
        }
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
                    if (proxyConfiguration.isBypassLocal() && isLocalHost(url.getHost())) {
                        return true;
                    }   if (proxyConfiguration.getBypassList().contains(url.getHost())) {
                    return true;
                }   break;
                case SOCKET_SCHEMA:
                    String host = uri.getHost();
                    if (proxyConfiguration.isBypassLocal() && isLocalHost(host)) {
                        return true;
                    }   if (proxyConfiguration.getBypassList().contains(host)) {
                    return true;
                }   break;
            }
        } catch (MalformedURLException e) {
            return false;
        }

        return false;
    }

    /**
     * @return true if the host is the hostname or the IP address of the
     * localhost
     * @param  host host to verify
     */
    private boolean isLocalHost(String host) {

        try {
            if (InetAddress.getByName(host).isLoopbackAddress()) {
                return true;
            }
        } catch (UnknownHostException e1) {
            // continue
        }

        try {
            if (host.equals(InetAddress.getLocalHost().getHostName())) {
                return true;
            }
        } catch (UnknownHostException e) {
            // continue
        }

        try {
            if (host.equals(InetAddress.getLocalHost().getHostAddress())) {
                return true;
            }
        } catch (UnknownHostException e) {
            // continue
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
        return getFromArguments(uri, proxyConfiguration.isUseHttpForHttpsAndFtp(), false,
                proxyConfiguration.getHttpsHost(), proxyConfiguration.getHttpsPort(),
                proxyConfiguration.getHttpHost(), proxyConfiguration.getHttpPort(),
                proxyConfiguration.getFtpHost(), proxyConfiguration.getFtpPort(),
                proxyConfiguration.getSocksHost(), proxyConfiguration.getSocksPort());
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
     * Returns a list of proxies by querying the firefox
     *
     * @param uri the uri to get proxies for
     * @return a list of proxies
     */
    protected abstract List<Proxy> getFromBrowser(URI uri);

}
