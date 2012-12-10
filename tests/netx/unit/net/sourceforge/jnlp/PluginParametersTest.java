package net.sourceforge.jnlp;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.junit.Test;

public class PluginParametersTest {

    @Test
    public void testAttributeParseJavaPrefix() {
        // java_* aliases override older names:
        // http://java.sun.com/j2se/1.4.2/docs/guide/plugin/developer_guide/using_tags.html#in-nav

        Map<String, String> rawParams;
        Hashtable<String, String> params;

        rawParams = new HashMap<String, String>();
        rawParams.put("code", "codeValue");
        rawParams.put("java_code", "java_codeValue");
        params = PluginParameters.createParameterTable(rawParams);

        assertEquals("java_codeValue", params.get("code"));

        rawParams = new HashMap<String, String>();
        rawParams.put("codebase", "codebaseValue");
        rawParams.put("java_codebase", "java_codebaseValue");
        params = PluginParameters.createParameterTable(rawParams);

        assertEquals("java_codebaseValue", params.get("codebase"));

        rawParams = new HashMap<String, String>();
        rawParams.put("archive", "archiveValue");
        rawParams.put("java_archive", "java_archiveValue");
        params = PluginParameters.createParameterTable(rawParams);

        assertEquals("java_archiveValue", params.get("archive"));

        rawParams = new HashMap<String, String>();
        rawParams.put("object", "objectValue");
        rawParams.put("java_object", "java_objectValue");
        params = PluginParameters.createParameterTable(rawParams);

        assertEquals("java_objectValue", params.get("object"));

        rawParams = new HashMap<String, String>();
        rawParams.put("type", "typeValue");
        rawParams.put("java_type", "java_typeValue");
        params = PluginParameters.createParameterTable(rawParams);

        assertEquals("java_typeValue", params.get("type"));
    }

    @Test
    public void testEnsureJavaPrefixTakesPrecedence() {
        Map<String, String> params;
        params = new HashMap<String, String>();
        params.put("test", "testValue");
        params.put("java_test", "java_testValue");
        PluginParameters.ensureJavaPrefixTakesPrecedence(params, "test");
        assertEquals("java_testValue", params.get("test"));

        params = new HashMap<String, String>();
        params.put("test", "testValue");
        PluginParameters.ensureJavaPrefixTakesPrecedence(params, "test");
        assertEquals("testValue", params.get("test"));

        params = new HashMap<String, String>();
        params.put("java_test", "java_testValue");
        PluginParameters.ensureJavaPrefixTakesPrecedence(params, "test");
        assertEquals("java_testValue", params.get("test"));
    }

    @Test
    public void testAttributeParseCodeAttribute() {
        Map<String, String> rawParams;
        Hashtable<String, String> params;

        // Simple test of object tag being set
        rawParams = new HashMap<String, String>();
        rawParams.put("object", "objectValue");
        params = PluginParameters.createParameterTable(rawParams);
        assertEquals("objectValue", params.get("object"));

        // Classid tag gets used as code tag
        rawParams = new HashMap<String, String>();
        rawParams.put("classid", "classidValue");
        params = PluginParameters.createParameterTable(rawParams);
        assertEquals("classidValue", params.get("code"));

        // Java: gets stripped from code tag
        rawParams = new HashMap<String, String>();
        rawParams.put("code", "java:codeValue");
        params = PluginParameters.createParameterTable(rawParams);
        assertEquals("codeValue", params.get("code"));

        // Classid tag gets used as code tag, and java: is stripped
        rawParams = new HashMap<String, String>();
        rawParams.put("classid", "java:classidValue");
        params = PluginParameters.createParameterTable(rawParams);
        assertEquals("classidValue", params.get("code"));

        // Classid tag gets used as code tag, and clsid: is stripped
        rawParams = new HashMap<String, String>();
        rawParams.put("classid", "clsid:classidValue");
        params = PluginParameters.createParameterTable(rawParams);
        assertEquals(null, params.get("code"));

    }

    /**
     * Initialize PluginParameters without code/object parameters
     */
    @Test(expected = PluginParameterException.class)
    public void testConstructorWithNoCodeAndObjectParam() {
        Map<String, String> rawParams = new HashMap<String, String>();
        rawParams.put("classid", "clsid:classidValue");
        new PluginParameters(rawParams);
    }

    /**
     * Initialize PluginParameters with jnlp_href but no code/object parameters
     */
    @Test
    public void testConstructorWithOnlyJnlpHrefParam() {
        Map<String, String> rawParams = new HashMap<String, String>();
        rawParams.put("jnlp_href", "applet.jnlp");
        PluginParameters pluginParam = new PluginParameters(rawParams);
        assertEquals("applet.jnlp", pluginParam.getJNLPHref());
    }
}
