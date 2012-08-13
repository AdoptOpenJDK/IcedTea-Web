/* InfoItem.java
Copyright (C) 2012 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

IcedTea is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
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
exception statement from your version. */
package net.sourceforge.jnlp.splashscreen.parts;

import net.sourceforge.jnlp.InformationDesc;
import net.sourceforge.jnlp.runtime.Translator;

/**
 * The optional kind="splash" attribute may be used in an icon element to
 * indicate that the image is to be used as a "splash" screen during the launch
 * of an application. If the JNLP file does not contain an icon element with
 * kind="splash" attribute, Java Web Start will construct a splash screen using
 * other items from the information Element.
 * If the JNLP file does not contain any icon images, the splash image will
 * consist of the application's title and vendor, as taken from the JNLP file.
 *
 * items not used inside
 */
public class InfoItem {

    public static final String SPLASH = "SPLASH";
    public static final String title = "title";
    public static final String vendor = "vendor";
    public static final String homepage = "homepage";
    public static final String homepageHref = "href";
    public static final String description = "description";
    public static final String descriptionKind = "kind";
    public static final String descriptionKindOneLine = (String) InformationDesc.ONE_LINE;
    //when no kind is specified, then it should behave as short
    public static final String descriptionKindShort = (String) InformationDesc.SHORT;
    public static final String descriptionKindToolTip = (String) InformationDesc.TOOLTIP;
    protected String type;
    protected String value;


    public InfoItem(String type, String value) {
        this.type = type;
        this.value = value;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    public boolean isofSameType(InfoItem o) {
        return ((getType().equals(o.getType())));


    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof InfoItem)) {
            return false;
        }
        InfoItem o = (InfoItem) obj;
        return isofSameType(o) && (getValue().equals(o.getValue()));



    }

    @Override
    public String toString() {
        return type + ": " + value;
    }


    

    public String toNiceString() {
        String key = SPLASH + type;
        return localise(key, value);
    }

    public static String localise(String key, String s) {
        return Translator.R(key) + ": " + s;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.getType() != null ? this.getType().hashCode() : 0);
        hash = 59 * hash + (this.getValue() != null ? this.getValue().hashCode() : 0);
        return hash;
    }
}
