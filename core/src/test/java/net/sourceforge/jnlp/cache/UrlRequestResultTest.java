package net.sourceforge.jnlp.cache;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UrlRequestResultTest {
    @Test
    public void testUrlRequestResultIsRedirect() throws MalformedURLException {
        final UrlRequestResult urlRequestResult = new UrlRequestResult(303, new URL("https://some.url.com"), null, new Date().getTime(), 42);

        assertTrue(urlRequestResult.isRedirect());
    }

   @Test
    public void testUrlRequestResultIsNoRedirect() throws MalformedURLException {
        final UrlRequestResult urlRequestResult = new UrlRequestResult(201, new URL("https://some.url.com"), null, new Date().getTime(), 42);

        assertFalse(urlRequestResult.isRedirect());
    }

    @Test (expected = IllegalStateException.class)
    public void testUrlRequestResultIsIllegalRedirect() {
        final UrlRequestResult urlRequestResult = new UrlRequestResult(301, null, null, new Date().getTime(), 42);

        assertFalse(urlRequestResult.isRedirect());
    }

   @Test
    public void testUrlRequestResultIsSuccess() throws MalformedURLException {
        final UrlRequestResult urlRequestResult = new UrlRequestResult(201, new URL("https://some.url.com"), null, new Date().getTime(), 42);

        assertTrue(urlRequestResult.isSuccess());
    }

    @Test
    public void testUrlRequestResultIsNoSuccess() {
        final UrlRequestResult urlRequestResult = new UrlRequestResult(500, null, null, new Date().getTime(), 42);

        assertFalse(urlRequestResult.isSuccess());
    }
}
