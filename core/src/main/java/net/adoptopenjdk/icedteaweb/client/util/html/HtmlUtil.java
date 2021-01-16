package net.adoptopenjdk.icedteaweb.client.util.html;

import net.adoptopenjdk.icedteaweb.i18n.Translator;

import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class HtmlUtil {
    private static final Translator TRANSLATOR = Translator.getInstance();

    public static String unorderedListOf(Set<URL> namedUrls, int maxDisplayed) {
        return unorderedListOf(namedUrls.stream()
                .map(url -> NamedUrl.of(url.toString(), url))
                .collect(Collectors.toList()), maxDisplayed);
    }

    public static String unorderedListOf(List<NamedUrl> namedUrls, int maxDisplayed) {
        if (namedUrls == null || namedUrls.isEmpty() || maxDisplayed <= 0) {
            return "";
        }

        final StringBuilder sb = new StringBuilder();

        sb.append("<ul>");

        namedUrls.stream()
                .limit(maxDisplayed)
                .forEach(url -> {
                    sb.append("<li>")
                            .append("<a href=").append(url.getUrl().toExternalForm()).append(">")
                            .append(url.getName())
                            .append("</a>")
                            .append("</li>");
                });

        if (namedUrls.size() > maxDisplayed) {
            sb.append("<li>")
                    .append(TRANSLATOR.translate("AndMore", namedUrls.size() - maxDisplayed))
                    .append("</li>");
        }

        sb.append("</ul>");

        return sb.toString();
    }
}
