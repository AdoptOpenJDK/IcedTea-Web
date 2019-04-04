/* ParserSettingsTest.java
   Copyright (C) 2013 Red Hat, Inc.

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

import net.sourceforge.jnlp.util.optionparser.OptionParser;
import org.junit.Assert;
import org.junit.Test;

import net.sourceforge.jnlp.ParserSettings;

public class ParserSettingsTest {

    @Test
    public void testDefaultSettings() {
       Assert.assertNotNull("Default parser settings should not be null", ParserSettings.getGlobalParserSettings());
    }

    @Test
    public void testSetGlobalParserSettings() {
        ParserSettings globalSettings, settings;
        settings = new ParserSettings(false, false, false);
        ParserSettings.setGlobalParserSettings(settings);
        globalSettings = ParserSettings.getGlobalParserSettings();
        Assert.assertEquals(settings.isStrict(), globalSettings.isStrict());
        Assert.assertEquals(settings.isMalformedXmlAllowed(), globalSettings.isMalformedXmlAllowed());
        Assert.assertEquals(settings.isExtensionAllowed(), globalSettings.isExtensionAllowed());
    }

    @Test
    public void testSetGlobalParserSettingsFromOptionParser() {
        String args[] = {"-xml", "-strict"};

        OptionParser optionParser = new OptionParser(args, OptionsDefinitions.getJavaWsOptions());
        ParserSettings settings = ParserSettings.setGlobalParserSettingsFromOptionParser(optionParser);
        ParserSettings globalSettings = ParserSettings.getGlobalParserSettings();

        Assert.assertEquals(settings.isStrict(), globalSettings.isStrict());
        Assert.assertEquals(settings.isExtensionAllowed(), globalSettings.isExtensionAllowed());
        Assert.assertEquals(settings.isMalformedXmlAllowed(), globalSettings.isMalformedXmlAllowed());
    }

    @Test
    public void testSetGlobalParserSettingsFromOptionParserHasSameOptionsAsOptionParser() {
        String args[] = {"-xml", "-strict"};

        OptionParser optionParser = new OptionParser(args, OptionsDefinitions.getJavaWsOptions());
        ParserSettings settings = ParserSettings.setGlobalParserSettingsFromOptionParser(optionParser);

        Assert.assertEquals(settings.isStrict(), optionParser.hasOption(OptionsDefinitions.OPTIONS.STRICT));
        Assert.assertEquals(settings.isMalformedXmlAllowed(), !optionParser.hasOption(OptionsDefinitions.OPTIONS.XML));
    }
}
