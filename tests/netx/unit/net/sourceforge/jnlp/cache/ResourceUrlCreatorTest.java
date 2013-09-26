package net.sourceforge.jnlp.cache;

import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;

import net.sourceforge.jnlp.DownloadOptions;
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
        URL result = ResourceUrlCreator.getUrl(resource, true /*use pack suffix*/, true /*use version suffix*/);
        assertEquals("http://test__V1.1.jar.pack.gz", result.toString());
    }

    @Test
    public void testGetVersionedUrl() throws MalformedURLException {
        Resource resource = Resource.getResource(new URL("http://foo.com/bar.jar"), new Version("1.1"), null);
        ResourceUrlCreator ruc = new ResourceUrlCreator(resource, new DownloadOptions(false, true));
        URL result = ruc.getVersionedUrl(resource);
        assertEquals("http://foo.com/bar.jar?version-id=1.1", result.toString());
    }

    @Test
    public void testGetNonVersionIdUrl() throws MalformedURLException {
        Resource resource = Resource.getResource(new URL("http://foo.com/some.jar"), new Version("version two"), null);
        ResourceUrlCreator ruc = new ResourceUrlCreator(resource, new DownloadOptions(false, true));
        URL result = ruc.getVersionedUrl(resource);
        assertEquals("http://foo.com/some.jar", result.toString());
    }

    @Test
    public void testGetVersionedUrlWithQuery() throws MalformedURLException {
        Resource resource = Resource.getResource(new URL("http://bar.com/bar.jar?i=1234abcd"), new Version("1.1"), null);
        ResourceUrlCreator ruc = new ResourceUrlCreator(resource, new DownloadOptions(false, true));
        URL result = ruc.getVersionedUrl(resource);
        assertEquals("http://bar.com/bar.jar?i=1234abcd&version-id=1.1", result.toString());
    }

    @Test
    public void testGetVersionedUrlWithoutVersion() throws MalformedURLException {
        Resource resource = Resource.getResource(new URL("http://baz.com/bar.jar"), null, null);
        ResourceUrlCreator ruc = new ResourceUrlCreator(resource, new DownloadOptions(false, false));
        URL result = ruc.getVersionedUrl(resource);
        assertEquals("http://baz.com/bar.jar", result.toString());
    }

    @Test
    public void testGetVersionedUrlWithoutVersionWithQuery() throws MalformedURLException {
        Resource resource = Resource.getResource(new URL("http://rhat.com/bar.jar?i=1234abcd"), null, null);
        ResourceUrlCreator ruc = new ResourceUrlCreator(resource, new DownloadOptions(false, false));
        URL result = ruc.getVersionedUrl(resource);
        assertEquals("http://rhat.com/bar.jar?i=1234abcd", result.toString());
    }

    @Test
    public void testGetVersionedUrlWithLongQuery() throws MalformedURLException {
        Resource resource = Resource.getResource(new URL("http://yyz.com/bar.jar?i=1234&j=abcd"), new Version("2.0"), null);
        ResourceUrlCreator ruc = new ResourceUrlCreator(resource, new DownloadOptions(false, true));
        URL result = ruc.getVersionedUrl(resource);
        assertEquals("http://yyz.com/bar.jar?i=1234&j=abcd&version-id=2.0", result.toString());
    }
}
