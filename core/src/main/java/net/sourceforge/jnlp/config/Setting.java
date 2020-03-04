/* Setting.java
   Copyright (C) 2010 Red Hat, Inc.

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

package net.sourceforge.jnlp.config;

import net.adoptopenjdk.icedteaweb.config.validators.ValueValidator;

import java.net.URL;

import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;

/**
 * Represents a value for a configuration. Provides methods to get the value
 * as well as marking the value as locked.
 *
 * Each instance of this class has an associated ValueChecker. This checker
 * can be used to check if the current value is valid. The default value
 * _must_ be valid. Null values can not originate externally so are (mostly)
 * considered valid.
 */
public class Setting {

    public static Setting createDefault(final String key, final String value, final ValueValidator validator) {
        return new Setting(key, R("Unknown"), false, validator, value, value, R("DCSourceInternal"));
    }

    public static Setting createUnknown(final String key, final String value) {
        return new Setting(key, R("Unknown"), false, null, null, value, R("Unknown"));
    }

    public static Setting createFromPropertyFile(final String key, final String value, boolean locked, final URL propertyFile) {
        return new Setting(key, R("Unknown"), locked, null, null, value, propertyFile.toExternalForm());
    }

    private final String name;
    private final String description;
    private final ValueValidator validator;
    private final String source;
    private final String defaultValue;

    private String value;
    private boolean locked;

    /**
     * Creates a new Settings object
     *
     * @param name the name of this setting
     * @param description a human readable description of this setting
     * @param locked whether this setting is currently locked
     * @param validator the {@link ValueValidator} that can be used to validate
     * the value
     * @param defaultValue the default value of this setting. If this is not a
     * recognized setting, use null.
     * @param value the initial value of this setting
     * @param source the origin of the value (a file, or perhaps "{@code <internal>}")
     */
    private Setting(String name, String description, boolean locked,
                   ValueValidator validator, String defaultValue, String value, String source) {
        this.name = name;
        this.description = description;
        this.locked = locked;
        this.validator = validator;
        this.source = source;
        this.defaultValue = defaultValue;
        this.value = value;
    }

    /**
     * Creates a copy of this setting
     */
    public Setting copy() {
        return new Setting(name, description, locked, validator, defaultValue, value, source);
    }

    /**
     * Creates a copy of this setting and copies the following attributes from {@code src}:
     * - value
     * - locked
     * - source
     */
    Setting copyValuesFrom(Setting src) {
        if (locked) {
            return this;
        }

        final String newValue = src.value != null ? src.value : value;
        final String newSource = src.value != null ? src.source : source;

        return new Setting(name, description, src.locked, validator, defaultValue, newValue, newSource);
    }

    /**
     * @return the {@link ValueValidator} that can be used to check if
     * the current value is valid
     */
    public ValueValidator getValidator() {
        return validator;
    }

    /**
     * @return the default value for this setting. May be null if this is not
     * one of the supported settings
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * @return a human readable description of this setting
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the name (like foo.bar.baz) of this setting
     */
    public String getName() {
        return name;
    }

    /**
     * @return the source of the current value of this setting. May be a string
     * like "internal" or it may be the location of the properties file
     */
    public String getSource() {
        return source;
    }

    /**
     * @return the current value of this setting
     */
    public String getValue() {
        return value;
    }

    /**
     * @return true if this setting is locked
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Note that setting the value is not enforced - it is the caller's
     * responsibility to check if a value is locked or not before setting a
     * new value
     *
     * @param value the new value
     */
    public void setValue(String value) {
        this.value = value;
    }

    public void setLocked(final boolean locked) {
        this.locked = locked;
    }

    @Override
    public String toString() {
        return value;
    }

}
