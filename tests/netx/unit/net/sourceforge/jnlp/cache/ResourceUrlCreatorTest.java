package net.sourceforge.jnlp.cache;

import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;

import net.sourceforge.jnlp.Version;

import org.junit.Test;

public class ResourceUrlCreatorTest {

    @Test
    public void testVersionEncode() throws MalformedURLException {
        Resource resource = Resource.getResource(new URL("http://test.jar"), new Version("1.1"), null);
        URL result = ResourceUrlCreator.getUrl(resource, false /*don't use pack suffix*/, true /*use version suffix*/);
        assertEquals("http://test__V1.1.jar", result.toString());
    }

    @Test
    public void testVersionWithPeriods() throws MalformedURLException {
        Resource resource = Resource.getResource(new URL("http://test.with.periods.jar"), new Version("1.1"), null);
        URL result = ResourceUrlCreator.getUrl(resource, false /*don't use pack suffix*/, true /*use version suffix*/);

        // A previous bug had this as "test__V1.1.with.periods.jar"
        assertEquals("http://test.with.periods__V1.1.jar", result.toString());
    }

    @Test
    public void testPackEncode() throws MalformedURLException {
        Resource resource = Resource.getResource(new URL("http://test.jar"), new Version("1.1"), null);
        URL result = ResourceUrlCreator.getUrl(resource, true /*use pack suffix*/, false /*don't use version suffix*/);
        assertEquals("http://test.jar.pack.gz", result.toString());
    }

    @Test
    public void testVersionAndPackEncode() throws MalformedURLException {
        Resource resource = Resource.getResource(new URL("http://test.jar"), new Version("1.1"), null);
        URL result = ResourceUrlCreator.getUrl(resource, true /*use pack suffix*/, true/*use version suffix*/);
        assertEquals("http://test__V1.1.jar.pack.gz", result.toString());
    }
}
