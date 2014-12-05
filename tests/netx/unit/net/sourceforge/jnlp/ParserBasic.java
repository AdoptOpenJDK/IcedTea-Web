/* ParserBasic.java
   Copyright (C) 2011 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 2.

IcedTea is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to
the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version.
*/

package net.sourceforge.jnlp;

import java.io.InputStream;
import java.util.List;
import net.sourceforge.jnlp.mock.DummyJNLPFile;
import net.sourceforge.jnlp.util.logging.NoStdOutErrTest;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/** Test that the parser works with basic jnlp files */
public class ParserBasic extends NoStdOutErrTest{

    private static Node root;
    private static Parser parser;

    @BeforeClass
    public static void setUp() throws ParseException {
        ClassLoader cl = ParserBasic.class.getClassLoader();
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }
        ParserSettings defaultParser = new ParserSettings();
        InputStream jnlpStream = cl.getResourceAsStream("net/sourceforge/jnlp/basic.jnlp");
        root = Parser.getRootNode(jnlpStream, defaultParser);
        parser = new Parser(new DummyJNLPFile(), null, root, defaultParser);
    }

    @Test
    public void testJNLP() {
        Assert.assertEquals("1.0", parser.getSpecVersion().toString());
        Assert.assertEquals("http://localhost/", parser.getCodeBase().toString());
        Assert.assertEquals("http://localhost/jnlp.jnlp", parser.getFileLocation().toString());
    }

    @Test
    public void testInformation() throws ParseException {
        List<InformationDesc> infos = parser.getInfo(root);
        Assert.assertNotNull(infos);
        Assert.assertEquals(1, infos.size());
        InformationDesc info = infos.get(0);
        Assert.assertNotNull(info);
    }

    @Test
    public void testInformationTitle() throws ParseException {
        InformationDesc info = parser.getInfo(root).get(0);
        Assert.assertEquals("Large JNLP", info.getTitle());
    }

    @Test
    public void testInformationVendor() throws ParseException {
        InformationDesc info = parser.getInfo(root).get(0);
        Assert.assertEquals("The IcedTea Project", info.getVendor());
    }

    @Test
    public void testInformationHomePage() throws ParseException {
        InformationDesc info = parser.getInfo(root).get(0);
        Assert.assertEquals("http://homepage/", info.getHomepage().toString());
    }

    @Test
    public void testInformationDescription() throws ParseException {
        InformationDesc info = parser.getInfo(root).get(0);
        Assert.assertEquals("one-line", info.getDescription("one-line"));
        Assert.assertEquals("short", info.getDescription("short"));
        Assert.assertEquals("tooltip", info.getDescription("tooltip"));
    }

    @Test
    public void testInformationOfflineAllowed() throws ParseException {
        InformationDesc info = parser.getInfo(root).get(0);
        Assert.assertEquals(true, info.isOfflineAllowed());

    }

    @Test
    public void testInformationIcon() throws ParseException {
        InformationDesc info = parser.getInfo(root).get(0);

        IconDesc[] icons = info.getIcons(IconDesc.DEFAULT);
        Assert.assertNotNull(icons);
        Assert.assertEquals(1, icons.length);
        IconDesc icon = icons[0];
        Assert.assertNotNull(icon);
        Assert.assertEquals("http://localhost/icon.png", icon.getLocation().toString());
        icons = info.getIcons(IconDesc.SPLASH);
        Assert.assertNotNull(icons);
        Assert.assertEquals(1, icons.length);
        icon = icons[0];
        Assert.assertNotNull(icon);
        Assert.assertEquals("http://localhost/splash.png", icon.getLocation().toString());

    }

    @Test
    public void testInformationShortcut() throws ParseException {
        InformationDesc info = parser.getInfo(root).get(0);

        ShortcutDesc shortcut = info.getShortcut();
        Assert.assertNotNull(shortcut);
        Assert.assertTrue(shortcut.isOnlineValue());
        Assert.assertTrue(shortcut.onDesktop());
        MenuDesc menu = shortcut.getMenu();
        Assert.assertNotNull(menu);
        Assert.assertEquals("submenu", menu.getSubMenu());
    }

    @Test
    public void testInformationAssociation() throws ParseException {
        InformationDesc info = parser.getInfo(root).get(0);
        AssociationDesc[] associations = info.getAssociations();
        Assert.assertNotNull(associations);
        Assert.assertEquals(1, associations.length);
        AssociationDesc association = associations[0];
        Assert.assertNotNull(association);
        String[] extensions = association.getExtensions();
        Assert.assertNotNull(extensions);
        Assert.assertEquals(1, extensions.length);
        String extension = extensions[0];
        Assert.assertNotNull(extension);
        Assert.assertEquals("*.foo", extension);
        String mimeType = association.getMimeType();
        Assert.assertNotNull(mimeType);
        Assert.assertEquals("foo/bar", mimeType);
    }

    @Test
    public void testInformationRelatedContent() throws ParseException {
        InformationDesc info = parser.getInfo(root).get(0);

        RelatedContentDesc[] relatedContents = info.getRelatedContents();
        Assert.assertNotNull(relatedContents);
        Assert.assertEquals(1, relatedContents.length);
        RelatedContentDesc relatedContent = relatedContents[0];
        Assert.assertNotNull(relatedContent);
        Assert.assertEquals("related-content title", relatedContent.getTitle());
        Assert.assertNotNull(relatedContent.getLocation());
        Assert.assertEquals("http://related-content/", relatedContent.getLocation().toString());
        Assert.assertEquals("decription of related-content", relatedContent.getDescription());
        IconDesc relatedIcon = relatedContent.getIcon();
        Assert.assertNotNull(relatedIcon.getLocation());
        Assert.assertEquals("http://localhost/related-content-icon.png", relatedIcon.getLocation().toString());

    }

    @Test
    public void testSecurity() throws ParseException {
        SecurityDesc security = parser.getSecurity(root);
        Assert.assertNotNull(security);
        Assert.assertEquals(SecurityDesc.ALL_PERMISSIONS, security.getSecurityType());
    }

    @Test
    public void testResources() throws ParseException {
        List<ResourcesDesc> allResources = parser.getResources(root, false);
        Assert.assertNotNull(allResources);
        Assert.assertEquals(1, allResources.size());
        ResourcesDesc resources = allResources.get(0);
        Assert.assertNotNull(resources);
    }

    @Test
    public void testResourcesJava() throws ParseException {
        ResourcesDesc resources = parser.getResources(root, false).get(0);
        JREDesc[] jres = resources.getJREs();
        Assert.assertNotNull(jres);
        Assert.assertEquals(1, jres.length);
        JREDesc jre = jres[0];
        Assert.assertNotNull(jre);
        Assert.assertEquals("1.3+", jre.getVersion().toString());
        Assert.assertEquals("http://java-url/", jre.getLocation().toString());
        Assert.assertEquals("64m", jre.getInitialHeapSize());
        Assert.assertEquals("128m", jre.getMaximumHeapSize());
    }

    @Test
    public void testResourcesJar() throws ParseException {
        ResourcesDesc resources = parser.getResources(root, false).get(0);

        boolean foundNative = false;
        boolean foundEager = false;
        boolean foundLazy = false;

        JARDesc[] jars = resources.getJARs();
        Assert.assertEquals(3, jars.length);
        for (int i = 0; i < jars.length; i++) {
            if (jars[i].isNative()) {
                foundNative = true;
                Assert.assertEquals("http://localhost/native.jar", jars[i].getLocation().toString());
            } else if (jars[i].isEager()) {
                foundEager = true;
                Assert.assertEquals("http://localhost/eager.jar", jars[i].getLocation().toString());
            } else if (jars[i].isLazy()) {
                foundLazy = true;
                Assert.assertEquals("http://localhost/lazy.jar", jars[i].getLocation().toString());
            } else {
                Assert.assertFalse(true);
            }
        }

        Assert.assertTrue(foundNative);
        Assert.assertTrue(foundLazy);
        Assert.assertTrue(foundEager);
    }

    @Test
    public void testResourcesExtensions() throws ParseException {
        ResourcesDesc resources = parser.getResources(root, false).get(0);

        ExtensionDesc[] extensions = resources.getExtensions();
        Assert.assertNotNull(extensions);
        Assert.assertEquals(1, extensions.length);
        ExtensionDesc extension = extensions[0];
        Assert.assertNotNull(extension);
        Assert.assertEquals("http://extension/", extension.getLocation().toString());
        Assert.assertEquals("extension", extension.getName());
        Assert.assertEquals("0.1.1", extension.getVersion().toString());
    }

    @Test
    public void testResourcesProperty() throws ParseException {
        ResourcesDesc resources = parser.getResources(root, false).get(0);

        PropertyDesc[] properties = resources.getProperties();
        Assert.assertNotNull(properties);
        Assert.assertEquals(1, properties.length);

        PropertyDesc property = properties[0];
        Assert.assertNotNull(property);
        Assert.assertEquals("key", property.getKey());
        Assert.assertEquals("value", property.getValue());
    }

    @Test
    public void testApplication() throws ParseException {
        ApplicationDesc app = (ApplicationDesc) parser.getLauncher(root);
        Assert.assertNotNull(app);
        Assert.assertEquals("MainClass", app.getMainClass());
        Assert.assertArrayEquals(new String[] { "arg1", "arg2" }, app.getArguments());
    }

}
