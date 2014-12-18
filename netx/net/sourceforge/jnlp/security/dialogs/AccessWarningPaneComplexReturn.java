package net.sourceforge.jnlp.security.dialogs;

/* 
   Copyright (C) 2008 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 2.

IcedTea is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to
the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version.
*/

public class AccessWarningPaneComplexReturn  {

    
    public static class ShortcutResult {

        public static enum Shortcut {

            BROWSER, GENERATED_JNLP, JNLP_HREF, JAVAWS_HTML;
        }

        private String browser = "not_found_browser";
        private boolean fixHref = false;
        private Shortcut shortcutType = null;
        private final boolean create;

        public ShortcutResult(boolean create) {
            this.create = create;
        }

        public boolean isCreate() {
            return create;
        }

        public String getBrowser() {
            return browser;
        }

        public Shortcut getShortcutType() {
            return shortcutType;
        }

        public boolean isFixHref() {
            return fixHref;
        }

        public void setBrowser(String browser) {
            this.browser = browser;
        }

        public void setFixHref(boolean fixHref) {
            this.fixHref = fixHref;
        }

        public void setShortcutType(Shortcut shortcutType) {
            this.shortcutType = shortcutType;
        }
        
    }

    
        public static enum RemeberType {

            REMEMBER_BY_APP, REMEMBER_BY_DOMAIN, REMEMBER_DONT;
        }
        
    
    private final int regularReturn;
    private ShortcutResult dekstop;
    private ShortcutResult menu;
    private RemeberType rember;


    //0 = true; legacy...:-/
    public AccessWarningPaneComplexReturn(int regularReturn) {
        this.regularReturn = regularReturn;
    }

    public void setDekstop(ShortcutResult dekstop) {
        this.dekstop = dekstop;
    }

    public ShortcutResult getDekstop() {
        return dekstop;
    }

    public void setMenu(ShortcutResult menu) {
        this.menu = menu;
    }

    public ShortcutResult getMenu() {
        return menu;
    }

    public void setRember(RemeberType rember) {
        this.rember = rember;
    }

    public RemeberType getRember() {
        return rember;
    }

 

    public int getRegularReturn() {
        return regularReturn;
    }
    
    public boolean getRegularReturnAsBoolean() {
        return regularReturn == 0 ? true : false;
    }

}
