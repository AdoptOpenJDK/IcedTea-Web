/* ParserSettings.java
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

import net.sourceforge.jnlp.util.optionparser.OptionParser;

/**
 * Contains settings to be used by the Parser while parsing JNLP files.
 *
 * Immutable and therefore thread-safe.
 */
public class ParserSettings {

    private static ParserSettings globalParserSettings = new ParserSettings();

    private final boolean isStrict;
    private final boolean extensionAllowed;
    private final boolean malformedXmlAllowed;

    /** Create a new ParserSettings with the defautl parser settings */
    public ParserSettings() {
        this(false, true, true);
    }

    /** Create a new ParserSettings object
     * @param strict true if parser should be stric
     * @param extensionAllowed true if extensions are allowed
     * @param malformedXmlAllowed true if xml sanitizer should be used
     */
    public ParserSettings(boolean strict, boolean extensionAllowed, boolean malformedXmlAllowed) {
        this.isStrict = strict;
        this.extensionAllowed = extensionAllowed;
        this.malformedXmlAllowed = malformedXmlAllowed;
    }

    /** @return true if extensions to the spec are allowed */
    public boolean isExtensionAllowed() {
        return extensionAllowed;
    }

    /** @return true if parsing malformed xml is allowed */
    public boolean isMalformedXmlAllowed() {
        return malformedXmlAllowed;
    }

    /** @return true if strict parsing mode is to be used */
    public boolean isStrict() {
        return isStrict;
    }

    /**
     * @return the global parser settings in use.
     */
    public static ParserSettings getGlobalParserSettings() {
        return globalParserSettings;
    }

    /**
     * Set the global ParserSettings to match the given settings.
     * @param parserSettings to be used
     */
    public static void setGlobalParserSettings(ParserSettings parserSettings) {
        globalParserSettings = parserSettings;
    }

    /**
     * @param optionParser to be read as source for globaPArserSettings
     * @return the ParserSettings to be used according to arguments specified
     * at boot on the command line. These settings are also stored so they
     * can be retrieved at a later time.
     */
    public static ParserSettings setGlobalParserSettingsFromOptionParser(OptionParser optionParser) {
        ParserSettings settings = new
                ParserSettings(optionParser.hasOption(OptionsDefinitions.OPTIONS.STRICT), true,
                !optionParser.hasOption(OptionsDefinitions.OPTIONS.XML));
        setGlobalParserSettings(settings);
        return globalParserSettings;
    }

}
