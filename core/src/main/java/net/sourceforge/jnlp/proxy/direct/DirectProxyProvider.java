package net.sourceforge.jnlp.proxy.direct;

import net.sourceforge.jnlp.proxy.ProxyProvider;

import java.net.Proxy;
import java.net.URI;
import java.util.Collections;
import java.util.List;

public class DirectProxyProvider implements ProxyProvider {

    @Override
    public List<Proxy> select(final URI uri) {
        return Collections.singletonList(Proxy.NO_PROXY);
    }

}
