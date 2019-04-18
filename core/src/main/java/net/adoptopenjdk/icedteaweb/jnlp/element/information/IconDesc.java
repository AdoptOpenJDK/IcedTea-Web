// Copyright (C) 2001-2003 Jon A. Maxwell (JAM)
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

/**
 * The icon element.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.8 $
 *
 * @implSpec See <b>JSR-56, Section 3.5 Descriptor Information</b>
 * for a detailed specification of this class.
 */
public class IconDesc {
    public static final String ICON_ELEMENT = "icon";

    public static final String KIND_ATTRIBUTE = "kind";
    public static final String WIDTH_ATTRIBUTE = "width";
    public static final String HEIGHT_ATTRIBUTE = "height";
    public static final String SIZE_ATTRIBUTE = "size";
    public static final String DEPTH_ATTRIBUTE = "depth";
    public static final String HREF_ATTRIBUTE = "href";

    /** the location of the icon */
    private final URL location;

    /** Used to indicate the use of the icon, such as default, selected, disabled, rollover, splash, and shortcut. */
    private final IconKind kind;

    /** the width, or -1 if unknown*/
    private final int width;

    /** the height, or -1 if unknown*/
    private final int height;

    /** the depth, or -1 if unknown*/
    private final int depth;

    /** the size, or -1 if unknown*/
    private final int size;

    /**
     * Creates an icon descriptor with the specified information.
     *
     * @param location the location of the icon
     * @param kind the type of icon
     * @param width the width, or -1 if unknown
     * @param height the height, or -1 if unknown
     * @param depth the depth, or -1 if unknown
     * @param size the size, or -1 if unknown
     */
    public IconDesc(final URL location, final IconKind kind, final int width, final int height, final int depth, final int size) {
        this.location = location;
        this.kind = kind;
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.size = size;
    }

    /**
     * @return the location of the icon.
     */
    public URL getLocation() {
        return location;
    }

    /**
     * @return the icon type.
     */
    public IconKind getKind() {
        return kind;
    }

    /**
     * @return the icon width or -1 if not specified in the
     * JNLPFile.
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return the icon height or -1 if not specified in the
     * JNLPFile.
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return the icon size or -1 if not specified in the JNLPFile.
     */
    public int getSize() {
        return size;
    }

    /**
     * @return the icon depth or -1 if not specified in the
     * JNLPFile.
     */
    public int getDepth() {
        return depth;
    }

}
