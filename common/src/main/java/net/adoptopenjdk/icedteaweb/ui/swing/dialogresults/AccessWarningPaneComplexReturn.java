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
package net.adoptopenjdk.icedteaweb.ui.swing.dialogresults;

import java.util.EnumSet;
import java.util.Objects;

public class AccessWarningPaneComplexReturn implements DialogResult {

    public static AccessWarningPaneComplexReturn readValue(String s) {
        String[] sq = s.split(",");
        Primitive regularReturn = Primitive.valueOf(sq[0]);
        //the replace is fixing case of not existing shortcuts at all
        sq = s.replace("()", "( )").split("[()]");
        ShortcutResult d = null;
        if (sq.length > 1) {
            d = ShortcutResult.readValue(sq[1]);
        }
        ShortcutResult m = null;
        if (sq.length > 3) {
            m = ShortcutResult.readValue(sq[3]);
        }
        AccessWarningPaneComplexReturn a = new AccessWarningPaneComplexReturn(regularReturn);
        a.desktop = d;
        a.menu = m;
        return a;
    }

    @Override
    public String writeValue() {
        StringBuilder sb = new StringBuilder();
        sb.append(regularReturn.writeValue()).append(",D(");
        if (desktop != null) {
            sb.append(desktop.writeValue());
        }
        sb.append(")M(");
        if (menu != null) {
            sb.append(menu.writeValue());
        }
        sb.append(")");
        return sb.toString();
    }

    public enum Shortcut {

        BROWSER, GENERATED_JNLP, JNLP_HREF, JAVAWS_HTML;

        public static String allValues() {
            EnumSet<Shortcut> all = EnumSet.of(BROWSER, GENERATED_JNLP, JNLP_HREF, JAVAWS_HTML);
            return all.toString();
        }
    }

    public static class ShortcutResult {

        public static ShortcutResult readValue(String s) {
            if (s.trim().isEmpty()) {
                return null;
            }
            String[] sq = s.split(",");
            ShortcutResult sr = new ShortcutResult(Boolean.valueOf(sq[3]));
            sr.browser = sq[0];
            sr.fixHref = Boolean.parseBoolean(sq[1]);
            if (!sq[2].equalsIgnoreCase("null")) {
                sr.shortcutType = Shortcut.valueOf(sq[2]);
            }
            return sr;
        }

        public String writeValue() {
            StringBuilder sb = new StringBuilder();
            sb.append(browser).append(",")
                    .append(fixHref).append(",")
                    .append(shortcutType).append(",")
                    .append(create).append(",");
            return sb.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ShortcutResult)) {
                return false;
            }
            ShortcutResult sr = (ShortcutResult) obj;
            return this.create == sr.create && this.fixHref == sr.fixHref
                    && this.browser.equals(sr.browser) && this.shortcutType == sr.shortcutType;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 89 * hash + Objects.hashCode(this.browser);
            hash = 89 * hash + (this.fixHref ? 1 : 0);
            hash = 89 * hash + Objects.hashCode(this.shortcutType);
            hash = 89 * hash + (this.create ? 1 : 0);
            return hash;
        }

        private String browser = "not_found_browser";
        private boolean fixHref = false;
        private Shortcut shortcutType = null;
        private final boolean create;

        ShortcutResult(String browser, boolean fixHref, Shortcut shortcutType, boolean create) {
            this.browser = browser;
            this.fixHref = fixHref;
            this.shortcutType = shortcutType;
            this.create = create;
        }

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

    private final YesNo regularReturn;
    private ShortcutResult desktop;
    private ShortcutResult menu;

    public AccessWarningPaneComplexReturn(boolean b) {
        if (b) {
            this.regularReturn = YesNo.yes();
        } else {
            this.regularReturn = YesNo.no();
        }
    }

    public AccessWarningPaneComplexReturn(Primitive regularReturn) {
        this.regularReturn = new YesNo(regularReturn);
    }

    public void setDesktop(ShortcutResult desktop) {
        this.desktop = desktop;
    }

    public ShortcutResult getDesktop() {
        return desktop;
    }

    public void setMenu(ShortcutResult menu) {
        this.menu = menu;
    }

    public ShortcutResult getMenu() {
        return menu;
    }

    @Override
    public boolean toBoolean() {
        if (regularReturn == null) {
            return false;
        }
        return regularReturn.toBoolean();
    }

    public YesNo getRegularReturn() {
        return regularReturn;
    }

    @Override
    public int getButtonIndex() {
        if (regularReturn == null) {
            return Primitive.NO.getLegacyButton();
        } else {
            return regularReturn.getButtonIndex();
        }
    }

}
