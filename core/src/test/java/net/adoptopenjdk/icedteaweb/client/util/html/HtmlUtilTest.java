package net.adoptopenjdk.icedteaweb.client.util.html;

import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class HtmlUtilTest {

    @Test
    public void testListOfFiveLinksWithMaxDisplayedThree() throws MalformedURLException {
        final List<NamedUrl> namedUrls = Arrays.asList(
                NamedUrl.of("first", new URL("http://localhost")),
                NamedUrl.of("first", new URL("http://localhost")),
                NamedUrl.of("first", new URL("http://localhost")),
                NamedUrl.of("first", new URL("http://localhost")),
                NamedUrl.of("first", new URL("http://localhost"))
        );


        final String listOfThreeLinks = HtmlUtil.unorderedListOf(namedUrls, 3);

        Assert.assertEquals("<ul>" +
                "<li><a href=http://localhost>first</a></li>" +
                "<li><a href=http://localhost>first</a></li>" +
                "<li><a href=http://localhost>first</a>" +
                "</li><li>and 2 more.</li>" +
                "</ul>", listOfThreeLinks);
    }
}