package net.sourceforge.jnlp.proxy;

import java.net.URL;

public class ProxyConfigurationBuilder {

    public ProxyConfigurationBuilder withHttpSettings(final String host, final int port) {
        return this;
    }

    public ProxyConfigurationBuilder withHttpsSettings(final String host, final int port) {
        return this;
    }

    public ProxyConfigurationBuilder withFtpSettings(final String host, final int port) {
        return this;
    }

    public ProxyConfigurationBuilder withSocksSettings(final String host, final int port) {
        return this;
    }

    public ProxyConfigurationBuilder withBypassUrl(final URL url) {
        return this;
    }

    public ProxyConfigurationBuilder withBypassLocal(final boolean byPassLocal) {
        return this;
    }

    public ProxyConfigurationBuilder withUseHttpForHttpsAndFtp(final boolean useHttpForHttpsAndFtp) {
        return this;
    }
}
