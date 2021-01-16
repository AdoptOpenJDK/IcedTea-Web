package net.adoptopenjdk.icedteaweb.jnlp.element.resource;

import net.sourceforge.jnlp.JNLPFile;
import org.junit.Assert;
import org.junit.Test;

import java.util.Locale;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.GERMAN;

/**
 * ...
 */
public class JNLPResourcesTest {

    @Test
    public void testFilterOs0arch0english() {
        JNLPResources filtered = getResources().filterResources(ENGLISH, "os0", "arch0");
        final Map<String, String> properties = filtered.getPropertiesMap();
        Assert.assertEquals("general", properties.get("general"));
        Assert.assertEquals("general", properties.get("os"));
        Assert.assertEquals("general", properties.get("arch"));
        Assert.assertEquals("general", properties.get("locale"));
    }

    @Test
    public void testFilterOs1arch0english() {
        JNLPResources filtered = getResources().filterResources(ENGLISH, "os1", "arch0");
        final Map<String, String> properties = filtered.getPropertiesMap();
        Assert.assertEquals("general", properties.get("general"));
        Assert.assertEquals("os1", properties.get("os"));
        Assert.assertEquals("general", properties.get("arch"));
        Assert.assertEquals("general", properties.get("locale"));
    }

    @Test
    public void testFilterOs1arch1english() {
        JNLPResources filtered = getResources().filterResources(ENGLISH, "os1", "arch1");
        final Map<String, String> properties = filtered.getPropertiesMap();
        Assert.assertEquals("general", properties.get("general"));
        Assert.assertEquals("os1", properties.get("os"));
        Assert.assertEquals("arch1", properties.get("arch"));
        Assert.assertEquals("general", properties.get("locale"));
    }

    @Test
    public void testFilterOs2arch2german() {
        JNLPResources filtered = getResources().filterResources(GERMAN, "os2", "arch2");
        final Map<String, String> properties = filtered.getPropertiesMap();
        Assert.assertEquals("general", properties.get("general"));
        Assert.assertEquals("os2", properties.get("os"));
        Assert.assertEquals("arch2", properties.get("arch"));
        Assert.assertEquals("german", properties.get("locale"));
    }

    private JNLPResources getResources() {
        final JNLPFile jnlpFile = null;

        final ResourcesDesc general = new ResourcesDesc(jnlpFile, locale(), os(), arch());
        general.addResource(new PropertyDesc("general", "general"));
        general.addResource(new PropertyDesc("os", "general"));
        general.addResource(new PropertyDesc("arch", "general"));
        general.addResource(new PropertyDesc("locale", "general"));

        final ResourcesDesc os1 = new ResourcesDesc(jnlpFile, locale(), os("os1"), arch());
        os1.addResource(new PropertyDesc("os", "os1"));

        final ResourcesDesc os1arch1 = new ResourcesDesc(jnlpFile, locale(), os("os1"), arch("arch1"));
        os1arch1.addResource(new PropertyDesc("arch", "arch1"));

        final ResourcesDesc os2arch2 = new ResourcesDesc(jnlpFile, locale(), os("os2"), arch("arch2"));
        os2arch2.addResource(new PropertyDesc("os", "os2"));
        os2arch2.addResource(new PropertyDesc("arch", "arch2"));

        final ResourcesDesc german = new ResourcesDesc(jnlpFile, locale(GERMAN), os(), arch());
        german.addResource(new PropertyDesc("locale", "german"));

        return new JNLPResources(asList(general, os1, os1arch1, os2arch2, german));
    }

    private Locale[] locale(Locale... locale) {
        return locale;
    }

    private String[] os(String... os) {
        return os;
    }

    private String[] arch(String... arch) {
        return arch;
    }
}
