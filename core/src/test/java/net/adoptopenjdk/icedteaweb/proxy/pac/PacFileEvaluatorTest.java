package net.adoptopenjdk.icedteaweb.proxy.pac;

import org.junit.Assert;
import org.junit.Test;

import java.net.URI;

public class PacFileEvaluatorTest {

    @Test
    public void testSimplePac() throws Exception {
        //given
        final URI uri = new URI("http://anyserver:8080");
        final PacFileEvaluator evaluator = new PacFileEvaluator(PacFileEvaluatorTest.class.getResource("simple-pac.js"), new NoopPacProxyCache());

        //when
        final String proxy = evaluator.getProxies(uri);

        //than
        Assert.assertNotNull(proxy);
        Assert.assertEquals("PROXY proxy.example.com:8080; DIRECT", proxy);
    }

    @Test
    public void testSimplePac2() throws Exception {
        //given
        final URI uri = new URI("http://myserver:8080");
        final PacFileEvaluator evaluator = new PacFileEvaluator(PacFileEvaluatorTest.class.getResource("simple-pac.js"), new NoopPacProxyCache());

        //when
        final String proxy = evaluator.getProxies(uri);

        //than
        Assert.assertNotNull(proxy);
        Assert.assertEquals("DIRECT", proxy);
    }

}
