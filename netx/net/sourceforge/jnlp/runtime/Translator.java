// Copyright (C) 2010 Red Hat, Inc.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

package net.sourceforge.jnlp.runtime;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Utility class to provide simple methods to help localize messages
 */
public class Translator {

    private static class TranslatorHolder {

        //https://en.wikipedia.org/wiki/Double-checked_locking#Usage_in_Java
        //https://en.wikipedia.org/wiki/Initialization_on_demand_holder_idiom
        private static final Translator INSTANCE = new Translator();

        private static Translator getTransaltor() {
            return TranslatorHolder.INSTANCE;
        }
    }

    /**
     * the localized resource strings
     */
    private final ResourceBundle resources;

    Translator() {
        this("net.sourceforge.jnlp.resources.Messages");
    }

    Translator(String s) {
        try {
            resources = ResourceBundle.getBundle(s);
        } catch (Exception ex) {
            throw new IllegalStateException("No bundles found for Locale: " + Locale.getDefault().toString()
                    + "and missing base resource bundle in netx.jar:net/sourceforge/jnlp/resource/Messages.properties");
        }
    }

    Translator(ResourceBundle resources) {
        this.resources = resources;
    }

    public static Translator getInstance() {
        return TranslatorHolder.getTransaltor();
    }

    /**
     * Return a translated (localized) version of the message
     * @param message the message to translate
     * @return a string representing the localized message
     */
    public static String R(String message) {
        return R(message, new Object[0]);
    }

    /**
     * @param message key to be found in properties
     * @param params params to be expanded to message
     * @return the localized string for the message
     */
    public static String R(String message, Object... params) {
        return getInstance().getMessage(message, params);
    }

   
    /**
     * @return the localized resource string using the specified arguments.
     * @param key key to be found in properties
     * @param args params to be expanded to message
     */
    protected String getMessage(String key, Object... args) {
        return MessageFormat.format(getMessage(key), args);
    }

    /**
     * Returns the localized resource string identified by the
     * specified key. If the message is empty, a null is
     * returned.
     */
    private String getMessage(String key) {
        try {
            String result = resources.getString(key);
            if (result.length() == 0)
                return "";
            else
                return result;
        } catch (NullPointerException e) {
            return getMessage("RNoResource", new Object[]{key});
        } catch (MissingResourceException | ClassCastException e) {
            if (key == "RNoResource") {
                return "No localized text found";
            } else {
                return getMessage("RNoResource", new Object[]{key});
            }
        }
    }
}
