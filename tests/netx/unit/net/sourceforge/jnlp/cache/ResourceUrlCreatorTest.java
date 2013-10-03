package net.sourceforge.jnlp.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.net.MalformedURLException;
import java.net.URL;

import net.sourceforge.jnlp.DownloadOptions;
import net.sourceforge.jnlp.Version;

import org.junit.Test;

public class ResourceUrlCreatorTest {

    private static final Version VERSION_11 = new Version("1.1");
    private static final Version VERSION_20 = new Version("2.0");
    private static final Version VERSION_TWO = new Version("version two");
    private static final DownloadOptions DLOPTS_NOPACK_USEVERSION = new DownloadOptions(false, true);
    private static final DownloadOptions DLOPTS_NOPACK_NOVERSION = new DownloadOptions(false, false);

    private URL getResultUrl(String url, Version version,
            boolean usePack /*use pack.gz suffix*/, boolean useVersion /*use version suffix*/) throws MalformedURLException {
        Resource resource = Resource.getResource(new URL(url), version, null);
        return ResourceUrlCreator.getUrl(resource, usePack, useVersion);
    }

    private URL getResultUrl(String url, Version version, DownloadOptions downloadOptions) throws MalformedURLException {
        Resource resource = Resource.getResource(new URL(url), version, null);
        ResourceUrlCreator ruc = new ResourceUrlCreator(resource, downloadOptions);
        return ruc.getVersionedUrl();
    }

    @Test
    public void testVersionEncode() throws MalformedURLException {
        URL result = getResultUrl("http://example.com/versionEncode.jar", VERSION_11, false, true);
        assertEquals("http://example.com/versionEncode__V1.1.jar", result.toString());
    }

    @Test
    public void testVersionWithPeriods() throws MalformedURLException {
        URL result = getResultUrl("http://example.com/test.version.with.periods.jar", VERSION_11, false, true);
        // A previous bug had this as "test__V1.1.with.periods.jar"
        assertEquals("http://example.com/test.version.with.periods__V1.1.jar", result.toString());
    }

    @Test
    public void testPackEncode() throws MalformedURLException {
        URL result = getResultUrl("http://example.com/packEncode.jar", VERSION_11, true, false);
        assertEquals("http://example.com/packEncode.jar.pack.gz", result.toString());
    }

    @Test
    public void testVersionAndPackEncode() throws MalformedURLException {
        URL result = getResultUrl("http://example.com/versionAndPackEncode.jar", VERSION_11, true, true);
        assertEquals("http://example.com/versionAndPackEncode__V1.1.jar.pack.gz", result.toString());
    }

    @Test
    public void testGetVersionedUrl() throws MalformedURLException {
        URL result = getResultUrl("http://example.com/versionedUrl.jar", VERSION_11, DLOPTS_NOPACK_USEVERSION);
        assertEquals("http://example.com/versionedUrl.jar?version-id=1.1", result.toString());
    }

    @Test
    public void testGetNonVersionIdUrl() throws MalformedURLException {
        URL result = getResultUrl("http://example.com/nonVersionIdUrl.jar", VERSION_TWO, DLOPTS_NOPACK_USEVERSION);
        assertEquals("http://example.com/nonVersionIdUrl.jar", result.toString());
    }

    @Test
    public void testGetVersionedUrlWithQuery() throws MalformedURLException {
        URL result = getResultUrl("http://example.com/versionedUrlWithQuery.jar?i=1234abcd", VERSION_11, DLOPTS_NOPACK_USEVERSION);
        assertEquals("http://example.com/versionedUrlWithQuery.jar?i=1234abcd&version-id=1.1", result.toString());
    }

    @Test
    public void testGetVersionedUrlWithoutVersion() throws MalformedURLException {
        URL result = getResultUrl("http://example.com/versionedUrlWithoutVersion.jar", null, DLOPTS_NOPACK_NOVERSION);
        assertEquals("http://example.com/versionedUrlWithoutVersion.jar", result.toString());
    }

    @Test
    public void testGetVersionedUrlWithoutVersionWithQuery() throws MalformedURLException {
        URL result = getResultUrl("http://example.com/versionedUrlWithoutVersionWithQuery.jar?i=1234abcd", null, DLOPTS_NOPACK_NOVERSION);
        assertEquals("http://example.com/versionedUrlWithoutVersionWithQuery.jar?i=1234abcd", result.toString());
    }

    @Test
    public void testGetVersionedUrlWithLongQuery() throws MalformedURLException {
        URL result = getResultUrl("http://example.com/versionedUrlWithLongQuery.jar?i=1234&j=abcd", VERSION_20, DLOPTS_NOPACK_USEVERSION);
        assertEquals("http://example.com/versionedUrlWithLongQuery.jar?i=1234&j=abcd&version-id=2.0", result.toString());
    }

    @Test
    public void testPercentEncoded() throws MalformedURLException {
        URL result = getResultUrl("http://example.com/percent encoded.jar", null, DLOPTS_NOPACK_USEVERSION);
        assertEquals("http://example.com/percent encoded.jar", result.toString());
    }

    @Test
    public void testPercentEncodedOnlyOnce() throws MalformedURLException {
        URL result = getResultUrl("http://example.com/percent%20encoded%20once.jar", null, DLOPTS_NOPACK_USEVERSION);
        assertEquals("http://example.com/percent%20encoded%20once.jar", result.toString());
    }

    @Test
    public void testPartiallyEncodedUrl() throws MalformedURLException {
        URL result = getResultUrl("http://example.com/partially encoded%20url.jar", null, DLOPTS_NOPACK_USEVERSION);
        assertEquals("http://example.com/partially encoded%20url.jar", result.toString());
    }

    @Test
    public void testVersionedEncodedUrl() throws MalformedURLException {
        URL result = getResultUrl("http://example.com/versioned%20encoded.jar", VERSION_11, DLOPTS_NOPACK_USEVERSION);
        assertEquals("http://example.com/versioned%20encoded.jar?version-id=1.1", result.toString());
    }

    @Test
    public void testInvalidVersionedUrl() throws MalformedURLException {
        URL result = getResultUrl("http://example.com/invalid versioned url.jar", VERSION_11, DLOPTS_NOPACK_USEVERSION);
        assertEquals("http://example.com/invalid versioned url.jar?version-id=1.1", result.toString());
    }

    @Test
    public void testLongComplexUrl() throws MalformedURLException {
        String URL =
            "https://example.com/,DSID=64c19c5b657df383835706571a7c7216,DanaInfo=example.com,CT=java+JICAComponents/complexOne.jar";
        URL result = getResultUrl(URL, null, DLOPTS_NOPACK_USEVERSION);
        assertEquals(URL, result.toString());
    }

    @Test
    public void testLongComplexVersionedUrl() throws MalformedURLException {
        String URL =
            "https://example.com/,DSID=64c19c5b657df383835706571a7c7216,DanaInfo=example.com,CT=java+JICAComponents/complexTwo.jar";
        URL result = getResultUrl(URL, VERSION_11, DLOPTS_NOPACK_USEVERSION);
        assertEquals(URL + "?version-id=" + VERSION_11, result.toString());
    }

    @Test
    public void testUserInfoAndVersioning() throws MalformedURLException {
        URL result = getResultUrl("http://foo:bar@example.com/userInfoAndVersion.jar", VERSION_11, DLOPTS_NOPACK_USEVERSION);
        assertEquals("http://foo:bar@example.com/userInfoAndVersion.jar?version-id=1.1", result.toString());
    }

    @Test
    public void testPortAndVersioning() throws MalformedURLException {
        URL result = getResultUrl("http://example.com:1234/portAndVersioning.jar", VERSION_11, DLOPTS_NOPACK_USEVERSION);
        assertEquals("http://example.com:1234/portAndVersioning.jar?version-id=1.1", result.toString());
    }

}
