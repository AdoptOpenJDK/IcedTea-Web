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

package net.adoptopenjdk.icedteaweb.jnlp.element.information;

import java.net.URL;

public class RelatedContentDesc {
    public static final String RELATED_CONTENT_ELEMENT = "related-content";

    public static final String TITLE_ELEMENT = "title";
    public static final String DESCRIPTION_ELEMENT = "description";
    public static final String ICON_ELEMENT = "icon";

    /** title of the content */
    private String title = null;

    /** the description of the content */
    private String description = null;

    /** the location of the content */
    private final URL location;

    /** the icon for this related content */
    private IconDesc icon = null;

    /**
     * Create a related-content descriptor
     * @param href the url of the related content
     */
    public RelatedContentDesc(final URL href) {
        this.location = href;
    }

    /**
     * Set the title of this content
     * @param title the title of this content
     */
    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * @return the title of this content..
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set the description of this related content
     * @param description to be set
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * @return the description of the related content
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the location of the related content. Not null
     */
    public URL getLocation() {
        return location;
    }

    /**
     * Set the icon for this related content
     * @param icon set to be used
     */
    public void setIconDesc(final IconDesc icon) {
        this.icon = icon;
    }

    /**
     * @return the icon descriptor for the realted content
     */
    public IconDesc getIcon() {
        return icon;
    }

}
