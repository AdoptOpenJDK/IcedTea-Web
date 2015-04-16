// Copyright (C) 2001-2003 Jon A. Maxwell (JAM)
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

/**
 * The icon element.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.8 $
 */
public class IconDesc {

    /** default icon */
    public static final Object DEFAULT = "default";

    /** selected icon */
    public static final Object SELECTED = "selected";

    /** disabled icon */
    public static final Object DISABLED = "disabled";

    /** rollover icon */
    public static final Object ROLLOVER = "rollover";

    /** splash icon */
    public static final Object SPLASH = "splash";

    /** destop shortcut icon */
    public static final Object SHORTCUT = "shortcut";

    /** the location of the icon */
    private final URL location;

    /** the type of icon*/
    private final Object kind;

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
    IconDesc(URL location, Object kind, int width, int height, int depth, int size) {
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
    public Object getKind() {
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
