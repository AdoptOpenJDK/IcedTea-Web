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

import java.net.URL;

public class RelatedContentDesc {

    /** title of the content */
    private String title = null;;

    /** the description of the content */
    private String description = null;

    /** the location of the content */
    private URL location = null;

    /** the icon for this related content */
    private IconDesc icon = null;

    /**
     * Create a related-content descriptor
     * @param href the url of the related content
     */
    public RelatedContentDesc(URL href) {
        this.location = href;
    }

    /**
     * Set the title of this content
     * @param title the title of this content
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the title of this content..
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set the description of this related content
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the description of the related content
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the location of the related content. Not null
     */
    public URL getLocation() {
        return location;
    }

    /**
     * Set the icon for this related content
     */
    public void setIconDesc(IconDesc icon) {
        this.icon = icon;
    }

    /**
     * Returns the icon descriptor for the realted content
     */
    public IconDesc getIcon() {
        return icon;
    }

}
