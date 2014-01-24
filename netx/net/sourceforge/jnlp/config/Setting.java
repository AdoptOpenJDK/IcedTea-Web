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

/**
 * Represents a value for a configuration. Provides methods to get the value
 * as well as marking the value as locked.
 *
 * Each instance of this class has an associated ValueChecker. This checker
 * can be used to check if the current value is valid. The default value
 * _must_ be valid. Null values can not originate externally so are (mostly)
 * considered valid.
 */
public class Setting<T> {

    private String name = null;
    private String description = null;
    private boolean locked = false;
    private ValueValidator validator = null;
    private T defaultValue = null;
    private T value = null;
    private String source = null;

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
    public Setting(String name, String description, boolean locked,
            ValueValidator validator, T defaultValue, T value, String source) {
        this.name = name;
        this.description = description;
        this.locked = locked;
        this.validator = validator;
        this.source = source;
        this.defaultValue = defaultValue;
        this.value = value;
    }

    /**
     * Creates a new Settings object by cloning the values from another
     * Settings object
     * @param other a Settings object to initialize settings from
     */
    public Setting(Setting<T> other) {
        this(other.name, other.description, other.locked, other.validator,
                other.defaultValue, other.value, other.source);
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
    public T getDefaultValue() {
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
    public T getValue() {
        return value;
    }

    /**
     * @return true if this setting is locked
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Marks this setting as locked or unlocked. Setting the value is not
     * enforced by this class.
     *
     * @param locked whether to mark this setting as locked or not locked.
     */
    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    /**
     * Sets the source of the current value of this Setting. Maybe a string
     * like "internal" or the location of the properties file
     *
     * @param source the source of the value
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Note that setting the value is not enforced - it is the caller's
     * responsibility to check if a value is locked or not before setting a
     * new value
     *
     * @param value the new value
     */
    public void setValue(T value) {
        this.value = value;
    }

}
