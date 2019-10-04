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

import net.adoptopenjdk.icedteaweb.Assert;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

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

    private final ChainedResourceBundle resources;

    private Translator() {
        this(DEFAULT_RESOURCE_BUNDLE_BASE_NAME, Locale.getDefault());
    }

    Translator(final String fullyQualifiedBundleBaseName, Locale locale) {
        this(getBundle(fullyQualifiedBundleBaseName, locale), locale);
    }

    private static ResourceBundle getBundle(String fullyQualifiedBundleBaseName, Locale locale) {
        try {
            return ResourceBundle.getBundle(fullyQualifiedBundleBaseName, locale);
        } catch (Exception ex) {
            throw new IllegalStateException("No bundle found for locale '" + locale +
                    "' and missing base resource bundle '" + DEFAULT_RESOURCE_BUNDLE_BASE_NAME + "'.");
        }
    }

    Translator(ResourceBundle resources, Locale locale) {
        this.resources = new ChainedResourceBundle(resources, locale);
    }

    public static Translator getInstance() {
        return TranslatorHolder.getTranslator();
    }

    public static void addBundle(String fullyQualifiedBundleBaseName) {
        addBundle(getBundle(fullyQualifiedBundleBaseName, getInstance().resources.getLocale()));
    }

    public static void addBundle(ResourceBundle bundle) {
        getInstance().addBundleImpl(bundle);
    }

    void addBundleImpl(ResourceBundle bundle) {
        resources.addBundle(bundle);
    }

    /**
     * Return a translated (localized) version of the message
     *
     * @param message the message to translate
     * @return a string representing the localized message
     */
    public static String R(String message) {
        return getInstance().translate(message, new Object[0]);
    }

    /**
     * @param message key to be found in properties
     * @param params  params to be expanded to message
     * @return the localized string for the message
     */
    public static String R(final String message, final Object... params) {
        return getInstance().translate(message, params);
    }

    /**
     * @param key  key to be found in properties
     * @param args params to be expanded to message
     * @return the localized resource string using the specified arguments.
     */
    public String translate(final String key, final Object... args) {
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
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Key '" + key + "' to lookup resource bundle text must not be null.");
        } catch (MissingResourceException | ClassCastException e) {
            if (Objects.equals(key, MISSING_RESOURCE_PLACEHOLDER)) {
                throw new IllegalStateException("No missing resource placeholder key '" + key + "' found in resource bundles.");
            } else {
                // try with custom fallback placeholder that should be included in the bundle
                final String languageCode = Locale.getDefault().getLanguage();
                return translate(MISSING_RESOURCE_PLACEHOLDER, key, languageCode);
            }
        }
    }

    private static class ChainedResourceBundle extends ResourceBundle {

        private final Locale locale;
        private final List<ResourceBundle> bundles = new ArrayList<>();

        /**
         * Initializing constructor
         */
        private ChainedResourceBundle(ResourceBundle bundle, Locale locale) {
            Assert.requireNonNull(bundle, "bundle");
            this.locale = Assert.requireNonNull(locale, "locale");
            bundles.add(bundle);
        }

        void addBundle(ResourceBundle bundle) {
            Assert.requireNonNull(bundle, "bundle");
            bundles.add(0, bundle);
        }

        @Override
        public String getBaseBundleName() {
            return bundles.stream()
                    .map(ResourceBundle::getBaseBundleName)
                    .collect(Collectors.joining(","));
        }

        @Override
        public Locale getLocale() {
            return locale;
        }

        @Override
        public boolean containsKey(String key) {
            return bundles.stream().anyMatch(b -> b.containsKey(key));
        }

        @Override
        public Set<String> keySet() {
            return bundles.stream().flatMap(rb -> rb.keySet().stream()).collect(Collectors.toSet());
        }

        @Override
        public Enumeration<String> getKeys() {
            final Iterator<String> it = keySet().iterator();
            return new Enumeration<String>() {
                @Override
                public boolean hasMoreElements() {
                    return it.hasNext();
                }

                @Override
                public String nextElement() {
                    return it.next();
                }
            };
        }

        @Override
        protected Object handleGetObject(final String key) {
            for (ResourceBundle bundle : bundles) {
                try {
                    return bundle.getString(key);
                } catch (MissingResourceException ignored) {
                    // ignore and try next bundle in the list
                }
            }
            return null;
        }
    }
}
