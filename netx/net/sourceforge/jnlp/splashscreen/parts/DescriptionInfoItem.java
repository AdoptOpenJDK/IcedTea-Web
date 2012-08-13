/* DescriptionInfoItem.java
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

/**
 *description element: A short statement about the application. Description 
 * elements are optional. The kind attribute defines how the description should
 * be used. It can have one of the following values:
 *
 *   * one-line: If a reference to the application is going to appear on one row
 *     in a list or a table, this description will be used.
 *   * short: If a reference to the application is going to be displayed in a
 *     situation where there is room for a paragraph, this description is used.
 *   * tooltip: If a reference to the application is going to appear in a
 *     tooltip, this description is used.
 *
 * Only one description element of each kind can be specified. A description
 *  element without a kind is used as a default value. Thus, if Java Web Start
 *  needs a description of kind short, and it is not specified in the JNLP file,
 *  then the text from the description without an attribute is used.
 *
 * All descriptions contain plain text. No formatting, such as with HTML tags,
 *  is supported.
 */
public class DescriptionInfoItem extends InfoItem {

    protected String kind;

    public DescriptionInfoItem(String value, String kind) {
        super(InfoItem.description, value);
        this.kind = kind;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public boolean isOfSameKind(DescriptionInfoItem o) {
        if (o.getKind() == null && getKind() == null) {
            return true;
        }
        if (o.getKind() == null && getKind() != null) {
            return false;
        }
        if (o.getKind() != null && getKind() == null) {
            return false;
        }
        return (o.getKind().equals(getKind()));
    }

    public boolean isSame(DescriptionInfoItem o) {
        return isOfSameKind(o) && isofSameType(o);

    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DescriptionInfoItem)) {
            return false;
        }
        DescriptionInfoItem o = (DescriptionInfoItem) obj;
        return super.equals(o) && isOfSameKind(o);


    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.kind != null ? this.kind.hashCode() : 0);
        hash = 59 * hash + (this.getType() != null ? this.getType().hashCode() : 0);
        hash = 59 * hash + (this.getValue() != null ? this.getValue().hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return super.toString() + " (" + getKind() + ")";
    }

  

    @Override
    public String toNiceString() {
        return super.toNiceString();
    }

}
