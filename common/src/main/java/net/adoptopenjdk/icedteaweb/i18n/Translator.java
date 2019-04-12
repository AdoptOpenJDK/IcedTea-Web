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

package net.adoptopenjdk.icedteaweb.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;

import static java.lang.String.format;

/**
 * Utility class to provide simple methods to help localize messages.
 */
public class Translator {
    static final String DEFAULT_RESOURCE_BUNDLE_BASE_NAME = "net.adoptopenjdk.icedteaweb.i18n.Messages";
    static final String MISSING_RESOURCE_PLACEHOLDER = "RNoResource";

    private static class TranslatorHolder {

        //https://en.wikipedia.org/wiki/Double-checked_locking#Usage_in_Java
        //https://en.wikipedia.org/wiki/Initialization_on_demand_holder_idiom
        private static final Translator INSTANCE = new Translator();

        private static Translator getTranslator() {
            return TranslatorHolder.INSTANCE;
        }
    }

    private final ResourceBundle resources;

    private Translator() {
        this(DEFAULT_RESOURCE_BUNDLE_BASE_NAME);
    }

    Translator(final String fullyQualifiedBundleBaseName) {
        try {
            resources = ResourceBundle.getBundle(fullyQualifiedBundleBaseName);
        } catch (Exception ex) {
            throw new IllegalStateException(
                    format("No bundle found for locale '%s' and missing base resource bundle '%s'.",
                    Locale.getDefault().toString(), DEFAULT_RESOURCE_BUNDLE_BASE_NAME)
            );
        }
    }

    Translator(ResourceBundle resources) {
        this.resources = resources;
    }

    private static Translator getInstance() {
        return TranslatorHolder.getTranslator();
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
    public static String R(final String message, final Object... params) {
        return getInstance().translate(message, params);
    }
   
    /**
     * @return the localized resource string using the specified arguments.
     * @param key key to be found in properties
     * @param args params to be expanded to message
     */
    protected String translate(final String key, final Object... args) {
        return MessageFormat.format(translate(key), args);
    }

    /**
     * Returns the localized resource string identified by the
     * specified key. If the message is empty, a null is
     * returned.
     */
    private String translate(final String key) {
        try {
            return resources.getString(key);
        }
        catch (NullPointerException e) {
            throw new IllegalArgumentException(format("Key '%s' to lookup resource bundle text must not be null.", key));
        }
        catch (MissingResourceException | ClassCastException e) {
            if (Objects.equals(key, MISSING_RESOURCE_PLACEHOLDER)) {
                throw new IllegalStateException(

                        format("No missing resource placeholder key '%s' found in resource bundles.", key));
            } else {
                // try with custom fallback placeholder that should be included in the bundle
                final String languageCode = Locale.getDefault().getLanguage();
                return translate(MISSING_RESOURCE_PLACEHOLDER, new Object[]{key, languageCode});
            }
        }
    }
}
