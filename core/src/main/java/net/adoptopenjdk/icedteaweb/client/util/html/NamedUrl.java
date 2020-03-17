package net.adoptopenjdk.icedteaweb.client.util.html;

import java.net.URL;

public class NamedUrl {
    private String name;
    private URL url;

    public NamedUrl(final String name, final URL url) {
        this.name = name;
        this.url = url;
    }

    public static NamedUrl of(final String name, final URL url) {
        return new NamedUrl(name, url);
    }

    public String getName() {
        return name;
    }

    public URL getUrl() {
        return url;
    }
}
