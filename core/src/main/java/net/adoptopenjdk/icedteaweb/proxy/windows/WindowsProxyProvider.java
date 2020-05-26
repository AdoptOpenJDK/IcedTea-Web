package net.adoptopenjdk.icedteaweb.proxy.windows;

import net.adoptopenjdk.icedteaweb.proxy.ProxyProvider;
import net.adoptopenjdk.icedteaweb.proxy.windows.registry.RegistryQuery;
import net.adoptopenjdk.icedteaweb.proxy.windows.registry.RegistryQueryResult;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

import java.net.Proxy;
import java.net.URI;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static net.adoptopenjdk.icedteaweb.proxy.windows.WindowsProxyConstants.PROXY_REGISTRY_KEY;

public class WindowsProxyProvider implements ProxyProvider {

    private final ProxyProvider internalProvider;

    public WindowsProxyProvider(final DeploymentConfiguration config, final ExecutorService ioExecutor) throws Exception {
        final RegistryQueryResult queryResult = RegistryQuery.getAllValuesForKey(PROXY_REGISTRY_KEY, ioExecutor);
        internalProvider = WindowsProxyUtils.createInternalProxy(config, queryResult);
    }

    @Override
    public List<Proxy> select(final URI uri) throws Exception {
        return internalProvider.select(uri);
    }
}
