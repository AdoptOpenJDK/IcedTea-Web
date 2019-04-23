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

import java.net.URL;
import net.adoptopenjdk.icedteaweb.Assert;

/**
 * The optional related-content element describes an additional piece of related content, such as a
 * readme file, help pages, or links to registration pages, as a hint to the JNLP Client.
 * The application is asking that this content be included in its desktop integration.
 * The related-content element has a mandatory href attribute.
 *
 * @implSpec See <b>JSR-56, Section 3.5 Descriptor Information</b>
 * for a detailed specification of this class.
 */
public class RelatedContentDesc {
    public static final String RELATED_CONTENT_ELEMENT = "related-content";
    public static final String HREF_ATTRIBUTE = "href";

    public static final String TITLE_ELEMENT = "title";
    public static final String DESCRIPTION_ELEMENT = "description";
    public static final String ICON_ELEMENT = "icon";

    /** The name of the related content. */
    private String title = null;

    /** A short description of the related content. */
    private String description = null;

    /** The mandatory location of the content. */
    private final URL location;

    /** The icon can be used by the JNLP Client to identify the related content to the user. */
    private IconDesc icon = null;

    /**
     * Create a related-content descriptor.
     *
     * @param location the location of the related content
     */
    public RelatedContentDesc(final URL location) {
        Assert.requireNonNull(location, "location");

        this.location = location;
    }

    /**
     * Set the title of this content.
     * @param title the title of this content
     */
    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * @return the title of this content.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set the description of this related content.
     *
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
     * @return the mandatory location of the related content.
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
     * @return the icon descriptor for the related content
     */
    public IconDesc getIcon() {
        return icon;
    }
}
