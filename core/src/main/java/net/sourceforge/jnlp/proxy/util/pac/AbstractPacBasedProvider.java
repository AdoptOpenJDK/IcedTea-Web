package net.sourceforge.jnlp.proxy.util.pac;

import net.adoptopenjdk.icedteaweb.Assert;
import net.sourceforge.jnlp.proxy.ProxyProvider;

import java.net.Proxy;
import java.net.URI;
import java.util.Collections;
import java.util.List;

public abstract class AbstractPacBasedProvider implements ProxyProvider {

    public final static String NAME = "ConfigBasedAutoConfigUrlProxyProvider";


    protected abstract PacEvaluator getPacEvaluator();

    @Override
    public List<Proxy> select(final URI uri) throws Exception {
        Assert.requireNonNull(uri, "uri");
        final PacEvaluator pacEvaluator = getPacEvaluator();
        Assert.requireNonNull(pacEvaluator, "pacEvaluator");
        final String proxiesString = pacEvaluator.getProxies(uri.toURL());
        final List<Proxy> proxies = PacUtils.getProxiesFromPacResult(proxiesString);
        return Collections.unmodifiableList(proxies);
    }

}
