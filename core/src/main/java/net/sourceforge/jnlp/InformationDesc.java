// Copyright (C) 2001-2003 Jon A. Maxwell (JAM)
// Copyright (C) 2009 Red Hat, Inc.
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

package net.sourceforge.jnlp;

import java.net.*;
import java.util.*;

/**
 * The information element.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.9 $
 */
public class InformationDesc {

    // There is an understanding between this class and the parser
    // that description and icon types are keyed by "icon-"+kind and
    // "description-"+kind, and that other types are keyed by their
    // specification name.

    /** one-line description */
    /**http://docs.oracle.com/javase/6/docs/technotes/guides/javaws/developersguide/syntax.html**/
    public static final Object ONE_LINE = "one-line";

    /** short description */
    public static final Object SHORT = "short";

    /** tooltip description */
    public static final Object TOOLTIP = "tooltip";

    /** default description */
    public static final Object DEFAULT = "default";

    /** the locales for the information */
    final private Locale locales[];

    /** the data as list of key,value pairs */
    private List<Object> info;

    final boolean strict;
    /**
     * Create an information element object.
     *
     * @param locales the locales the information is for
     * @param strict whether parser was strict
     */
    public InformationDesc(Locale locales[], boolean strict) {
        this.locales = locales;
        this.strict = strict;
    }
    
    InformationDesc(Locale locales[]) {
        this(locales, false);
    }

    /**
     * @return the application's title.
     */
    public String getTitle() {
        return (String) getItem("title");
    }

    /**
     * @return the application's vendor.
     */
    public String getVendor() {
        return (String) getItem("vendor");
    }

    /**
     * @return the application's homepage.
     */
    public URL getHomepage() {
        return (URL) getItem("homepage");
    }

    /**
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
     * @return the application's description of the specified type.
     *
     * @param kind one of Information.SHORT, Information.ONE_LINE,
     * Information.TOOLTIP, Information.DEFAULT
     */
    public String getDescription(Object kind) {
        String result = getDescriptionStrict(kind);
        if (result == null)
            return (String) getItem("description-" + DEFAULT);
        else
            return result;
    }

      /**
     * @return the application's description of the specified type.
     *
     * @param kind one of Information.SHORT, Information.ONE_LINE,
     * Information.TOOLTIP, Information.DEFAULT
     */
    public String getDescriptionStrict(Object kind) {
        return (String) getItem("description-" + kind);
        
    }

    /**
     * Returns the icons specified by the JNLP file.
     *
     * @param kind one of IconDesc.SELECTED, IconDesc.DISABLED,
     * IconDesc.ROLLOVER, IconDesc.SPLASH, IconDesc.DEFAULT
     * @return an array of zero of more IconDescs of the specified icon type
     */
    public IconDesc[] getIcons(Object kind) {
        List<Object> icons = getItems("icon-" + kind);

        return icons.toArray(new IconDesc[icons.size()]);
    };

    /**
     * Returns the URL of the icon closest to the specified size and
     * kind.  This method will not return an icon smaller than the
     * specified width and height unless there are no other icons
     * available.
     *
     * @param kind the kind of icon to get
     * @param width desired width of icon
     * @param height desired height of icon
     * @return the closest icon by size or null if no icons declared
     */
    public URL getIconLocation(Object kind, int width, int height) {
        IconDesc icons[] = getIcons(kind);
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
     * @return whether offline execution allowed.
     */
    public boolean isOfflineAllowed() {
        if (strict) {
            return null != getItem("offline-allowed");
        } else {
            // by deault itw ignore this switch. Most applications are missusing it
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
        List<Object> associations = getItems("association");

        return associations.toArray(new AssociationDesc[associations.size()]);
    }

    /**
     * @return the shortcut specified by this JNLP file
     */
    public ShortcutDesc getShortcut() {
        return (ShortcutDesc) getItem("shortcut");
    }

    /**
     * @return the related-contents specified by this JNLP file
     */
    public RelatedContentDesc[] getRelatedContents() {
        List<Object> relatedContents = getItems("related-content");

        return relatedContents.toArray(new RelatedContentDesc[relatedContents.size()]);
    }

    /**
     * @param key key to find item
     * @return the last item matching the specified key.
     */
    protected Object getItem(Object key) {
        List<Object> items = getItems(key);
        if (items.isEmpty())
            return null;
        else
            return items.get(items.size() - 1);
    }

    /**
     * @param key key to find item
     * @return all items matching the specified key.
     */
    protected List<Object> getItems(Object key) {
        if (info == null)
            return Collections.emptyList();

        List<Object> result = new ArrayList<>();
        for (int i = 0; i < info.size(); i += 2)
            if (info.get(i).equals(key))
                result.add(info.get(i + 1));

        return result;
    }

    /**
     * Add an information item (description, icon, etc) under a
     * specified key name.
     * @param key key to place value to
     * @param value value to be placed to key
     */
    protected void addItem(String key, Object value) {
        if (info == null)
            info = new ArrayList<>();

        info.add(key);
        info.add(value);
    }

}
