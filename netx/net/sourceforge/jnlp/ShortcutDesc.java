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

public final class ShortcutDesc {

    /** Never create a shortcut */
    public static final String CREATE_NEVER = "NEVER";
    /** Always create a shortcut */
    public static final String CREATE_ALWAYS = "ALWAYS";
    /** Always ask user whether to create a shortcut */
    public static final String CREATE_ASK_USER = "ASK_USER";
    /** Ask user whether to create a shortcut but only if jnlp file asks for it */
    public static final String CREATE_ASK_USER_IF_HINTED = "ASK_IF_HINTED";
    /** Create a desktop shortcut without prompting if the jnlp asks for it */
    public static final String CREATE_ALWAYS_IF_HINTED = "ALWAYS_IF_HINTED";

    /** the application wants to be placed on the desktop */
    private boolean onDesktop = false;

    /** the application needs to be launched online */
    private boolean requiresOnline = true;

    /** the menu descriptor */
    private MenuDesc menu = null;

    /**
     * Create a new Shortcut descriptor
     * @param requiresOnline whether the shortcut requires connectivity
     * @param onDesktop whether the shortcut wants to be placed on the desktop
     */
    public ShortcutDesc(boolean requiresOnline, boolean onDesktop) {
        this.requiresOnline = requiresOnline;
        this.onDesktop = onDesktop;
    }

    /**
     * Returns whether the shortcut requires being online
     */
    public boolean isOnline() {
        return requiresOnline;
    }

    /**
     * Return whether the shortcut should be placed on the desktop
     */
    public boolean onDesktop() {
        return onDesktop;
    }

    /**
     * Add a shortcut to the 'start menu'
     * (whatever that means on gnome/kde/other ...)
     * @param menu if/what menu this shortcut should be added to
     */
    public void addMenu(MenuDesc menu) {
        this.menu = menu;
    }

    /**
     * Returns the menu this shortcut should be added to
     */
    public MenuDesc getMenu() {
        return menu;
    }

}
