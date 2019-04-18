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

/**
 * The optional menu element can be used to indicate an application's preference for putting a menu item
 * in the users start menus. The menu element can have a sub-menu attribute.
 *
 * @implSpec See <b>JSR-56, Section 3.5 Descriptor Information</b>
 * for a detailed specification of this class.
 */
public class MenuDesc {
    public static final String SUBMENU_ATTRIBUTE = "submenu";

    /**
     * The optional submenu attribute can be used to indicate an application's preference for where to
     * place the menu item, and can contain any string value.
     */
    private final String subMenu;

    /**
     * Create a new menu descriptor
     * @param subMenu sub-menu of this menu if any or null.
     */
    public MenuDesc(final String subMenu) {
        this.subMenu = subMenu;
    }

    /**
     * @return the submenu for this menu entry.
     */
    public String getSubMenu() {
        return subMenu;
    }
}
