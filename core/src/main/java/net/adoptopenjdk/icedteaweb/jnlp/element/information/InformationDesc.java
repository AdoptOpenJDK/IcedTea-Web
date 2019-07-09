// Copyright (C) 2001-2003 Jon A. Maxwell (JAM)
// Copyright (C) 2009 Red Hat, Inc.
// Copyright (C) 2019 Karakun AG
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

package net.adoptopenjdk.icedteaweb.jnlp.element.information;

import net.adoptopenjdk.icedteaweb.Assert;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static net.adoptopenjdk.icedteaweb.jnlp.element.information.AssociationDesc.ASSOCIATION_ELEMENT;
import static net.adoptopenjdk.icedteaweb.jnlp.element.information.DescriptionKind.DEFAULT;
import static net.adoptopenjdk.icedteaweb.jnlp.element.information.DescriptionKind.ONE_LINE;
import static net.adoptopenjdk.icedteaweb.jnlp.element.information.DescriptionKind.SHORT;
import static net.adoptopenjdk.icedteaweb.jnlp.element.information.DescriptionKind.TOOLTIP;
import static net.adoptopenjdk.icedteaweb.jnlp.element.information.HomepageDesc.HOMEPAGE_ELEMENT;

/**
 * The information element contains information intended to be consumed by the JNLP Client to integrate
 * the application into the desktop, provide user feedback, etc.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @implSpec See <b>JSR-56, Section 3.5 Descriptor Information</b>
 * for a detailed specification of this class.
 */

// There is an understanding between this class and the parser
// that description and icon types are keyed by "icon-"+kind and
// "description-"+kind, and that other types are keyed by their
// specification name.

public class InformationDesc {
    public static final String INFORMATION_ELEMENT = "information";

    public static final String OS_ATTRIBUTE = "os";
    public static final String ARCH_ATTRIBUTE = "arch";

    public static final String LOCALE_ATTRIBUTE = "locale";

    public static final String TITLE_ELEMENT = "title";
    public static final String VENDOR_ELEMENT = "vendor";
    public static final String OFFLINE_ALLOWED_ELEMENT = "offline-allowed";

    /**
     * The locales for which the information element should be used.
     */
    final private Locale[] locales;

    /**
     * Specifies the operating system for which the information element should be considered. If the value is a
     * prefix of the os.name system property, then the information element can be used. If the attribute is not
     * specified, it matches all operating systems.
     */
    private final String os;

    /**
     * Specifies the architecure for which the information element should be considered. If the value is a prefix
     * of the os.arch system property, then the information element can be used. If the attribute is not specified,
     * it matches all architecures.
     */
    private final String arch;

    /**
     * the data as list of key,value pairs
     */
    private List<Object> info;

    public final boolean strict;

    /**
     * Create an information element object.
     *
     * @param locales the locales for which the information element should be used
     */
    InformationDesc(final Locale[] locales) {
        this(locales, false);
    }

    /**
     * Create an information element object.
     *
     * @param locales the locales for which the information element should be used
     * @param strict  whether parser was strict
     */
    public InformationDesc(final Locale[] locales, final boolean strict) {
        this(locales, null, null, strict);
    }

    /**
     * Create an information element object.
     *
     * @param locales the locales for which the information element should be used
     * @param os      the operating system for which the information element should be considered
     * @param arch    the architecure for which the information element should be considered
     */
    public InformationDesc(final Locale[] locales, final String os, final String arch) {
        this(locales, os, arch, false);
    }

    /**
     * Create an information element object.
     *
     * @param locales the locales for which the information element should be used
     * @param os      the operating system for which the information element should be considered
     * @param arch    the architecure for which the information element should be considered
     * @param strict  whether parser was strict
     */
    public InformationDesc(final Locale[] locales, final String os, final String arch, final boolean strict) {
        this.locales = locales;
        this.os = os;
        this.arch = arch;
        this.strict = strict;
    }

    /**
     * @return the application's title.
     */
    public String getTitle() {
        return (String) getItem(InformationDesc.TITLE_ELEMENT);
    }

    /**
     * @return the application's vendor.
     */
    public String getVendor() {
        return (String) getItem(VENDOR_ELEMENT);
    }

    /**
     * @return the application's homepage.
     */
    public URL getHomepage() {
        return (URL) getItem(HOMEPAGE_ELEMENT);
    }

    /**
     * A short statement about the application. Longer descriptions should be put on a separate web page
     * and referred to using the homepage element.
     *
     * @return the default description for the application.
     */
    public String getDescription() {
        String result = getDescription(DEFAULT);

        // try to find any description if default is null
        if (result == null)
            result = getDescription(ONE_LINE);
        if (result == null)
            result = getDescription(SHORT);
        if (result == null)
            result = getDescription(TOOLTIP);

        return result;
    }

    /**
     * @param kind one of {@link DescriptionKind}
     * @return the application's description of the specified type.
     */
    public String getDescription(final DescriptionKind kind) {
        final String result = getDescriptionStrict(kind);
        if (result == null)
            return (String) getItem("description-" + DEFAULT.getValue());
        else
            return result;
    }

    /**
     * @param kind one of {@link DescriptionKind}
     * @return the application's description of the specified type.
     */
    public String getDescriptionStrict(DescriptionKind kind) {
        return (String) getItem("description-" + kind.getValue());

    }

    /**
     * Returns the icons specified by the JNLP file.
     *
     * @param kind one of {@link IconKind}
     * @return an array of zero of more IconDescs of the specified icon type
     */
    public IconDesc[] getIcons(IconKind kind) {
        List<Object> icons = getItems("icon-" + kind.getValue());

        return icons.toArray(new IconDesc[icons.size()]);
    }

    /**
     * Returns the URL of the icon closest to the specified size and
     * kind.  This method will not return an icon smaller than the
     * specified width and height unless there are no other icons
     * available.
     *
     * @param kind   the kind of icon to get
     * @param width  desired width of icon
     * @param height desired height of icon
     * @return the closest icon by size or null if no icons declared
     */
    public URL getIconLocation(IconKind kind, int width, int height) {
        IconDesc[] icons = getIcons(kind);
        if (icons.length == 0)
            return null;

        IconDesc best = null;
        for (IconDesc icon : icons) {
            if (icon.getWidth() >= width && icon.getHeight() >= height) {
                if (best == null) {
                    best = icon;
                }
                if (icon.getWidth() <= best.getWidth() && icon.getHeight() <= best.getHeight()) {
                    best = icon;
                }
            }
        }

        // FIXME if there's no larger icon, choose the closest smaller icon
        // instead of the first
        if (best == null)
            best = icons[0];

        return best.getLocation();
    }

    /**
     * @return the locales for the information.
     */
    public Locale[] getLocales() {
        return locales;
    }

    /**
     * @return the operating system for which the information element should be considered
     */
    public String getOs() {
        return os;
    }

    /**
     * @return the architecure for which the information element should be considered
     */
    public String getArch() {
        return arch;
    }

    /**
     * @return whether offline execution allowed.
     */
    public boolean isOfflineAllowed() {
        if (strict) {
            return null != getItem(OFFLINE_ALLOWED_ELEMENT);
        }
        else {
            // by default itw ignore this switch. Most applications are misusing it
            return true;
        }
    }

    /**
     * @return whether the resources specified in the JNLP file may
     * be shared by more than one instance in the same JVM
     * (JNLP extension).  This is an extension to the JNLP spec and
     * will always return false for standard JNLP files.
     */
    public boolean isSharingAllowed() {
        return null != getItem("sharing-allowed");
    }

    /**
     * @return the associations specified in the JNLP file
     */
    public AssociationDesc[] getAssociations() {
        final List<AssociationDesc> associations = (List) getItems(ASSOCIATION_ELEMENT);

        Collections.reverse(associations);

        final Set<ExtensionAndMimeType> alreadySeen = new HashSet<>();
        final List<AssociationDesc> result = new ArrayList<>();

        for (AssociationDesc association : associations) {
            final ExtensionAndMimeType key = new ExtensionAndMimeType(association.getExtensions(), association.getMimeType());
            if (!alreadySeen.contains(key)) {
                alreadySeen.add(key);
                result.add(association);
            }
        }

        Collections.reverse(result);

        return result.toArray(new AssociationDesc[0]);
    }

    /**
     * @return the shortcut specified by this JNLP file
     */
    public ShortcutDesc getShortcut() {
        return (ShortcutDesc) getItem(ShortcutDesc.SHORTCUT_ELEMENT);
    }

    /**
     * @return the related-contents specified by this JNLP file
     */
    public RelatedContentDesc[] getRelatedContents() {
        List<Object> relatedContents = getItems(RelatedContentDesc.RELATED_CONTENT_ELEMENT);

        return relatedContents.toArray(new RelatedContentDesc[relatedContents.size()]);
    }

    /**
     * @param key key to find item
     * @return the last item matching the specified key.
     */
    private Object getItem(final String key) {
        final List<Object> items = getItems(key);
        if (items.isEmpty())
            return null;
        else
            return items.get(items.size() - 1);
    }

    /**
     * @param key key to find item
     * @return all items matching the specified key.
     */
    public List<Object> getItems(final Object key) {
        if (info == null)
            return Collections.emptyList();

        final List<Object> result = new ArrayList<>();
        for (int i = 0; i < info.size(); i += 2)
            if (info.get(i).equals(key))
                result.add(info.get(i + 1));

        return result;
    }

    /**
     * Add an information item (description, icon, etc) under a
     * specified key name.
     *
     * @param key   key to place value to
     * @param value value to be placed to key
     */
    public void addItem(final String key, final Object value) {
        Assert.requireNonNull(key, "key");

        if (info == null)
            info = new ArrayList<>();

        info.add(key);
        info.add(value);
    }

    public Map<String, List<Object>> getItems() {
        final Map<String, List<Object>> result = new HashMap<>();

        if (info != null) {
            for (int i = 0; i < info.size(); i += 2) {
                final String key = (String) info.get(i);
                final Object value = info.get(i + 1);
                result.computeIfAbsent(key, k -> new ArrayList()).add(value);
            }
        }

        return result;
    }


    private static class ExtensionAndMimeType {
        private final String[] extensions;
        private final String mimeType;

        private ExtensionAndMimeType(String[] extensions, String mimeType) {
            this.extensions = extensions;
            this.mimeType = mimeType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ExtensionAndMimeType that = (ExtensionAndMimeType) o;
            return Arrays.equals(extensions, that.extensions) &&
                    mimeType.equals(that.mimeType);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(mimeType);
            result = 31 * result + Arrays.hashCode(extensions);
            return result;
        }
    }
}
