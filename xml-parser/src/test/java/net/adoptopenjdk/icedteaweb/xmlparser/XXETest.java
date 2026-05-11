package net.adoptopenjdk.icedteaweb.xmlparser;

import org.junit.Assert;
import org.junit.Test;
import java.io.InputStream;

public class XXETest {

    @Test
    public void testXXEIsDisabled() throws ParseException {
        InputStream is = XXETest.class.getClassLoader().getResourceAsStream("jnlps/xxe.jnlp");
        Assert.assertNotNull("xxe.jnlp not found", is);

        XMLParser parser = new XMLParser();
        XmlNode root = parser.getRootNode(is);

        Assert.assertNotNull(root);
        XmlNode information = root.getChildren("information").get(0);
        XmlNode title = information.getChildren("title").get(0);

        String titleValue = title.getNodeValue();
     //   Assert.assertFalse("XXE entity should not be resolved and contain sensitive data", titleValue.contains("root:x"));
        Assert.assertEquals("XXE Test ", titleValue);
    }
}
