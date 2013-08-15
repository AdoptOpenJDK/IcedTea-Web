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

import org.junit.Assert;
import org.junit.Test;

import net.sourceforge.jnlp.ParserSettings;

public class ParserSettingsTest {

    @Test
    public void testDefaultSettings() {
       Assert.assertNotNull("Default parser settings should not be null", ParserSettings.getGlobalParserSettings());
    }

    @Test
    public void testNoArgsSameAsDefault() {
        ParserSettings defaultSettings, noArgs;
        defaultSettings = new ParserSettings();
        noArgs = ParserSettings.setGlobalParserSettingsFromArgs(new String[0]);

        Assert.assertTrue("isExtensionAllowed should have been equal", defaultSettings.isExtensionAllowed() == noArgs.isExtensionAllowed());
        Assert.assertTrue("isStrict should have been equal", defaultSettings.isStrict() == noArgs.isStrict());
        Assert.assertTrue("isMalformedXmlAllowed should have been equal", defaultSettings.isMalformedXmlAllowed() == noArgs.isMalformedXmlAllowed());
    }

    @Test
    public void testWithArgs() {
        ParserSettings settings = ParserSettings.setGlobalParserSettingsFromArgs(new String[] {
           "-strict",
           "-xml",
        });
        Assert.assertTrue("isStrict should have been true", settings.isStrict() == true);
        Assert.assertTrue("isMalformedXmlAllowed should have been false", settings.isMalformedXmlAllowed() == false);
        Assert.assertTrue("isExtensionAllowed should have been true", settings.isExtensionAllowed() == true);
    }

}
