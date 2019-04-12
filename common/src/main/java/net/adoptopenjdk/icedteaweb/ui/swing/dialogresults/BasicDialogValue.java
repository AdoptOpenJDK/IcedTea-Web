/* 
 Copyright (C) 2009 Red Hat, Inc.

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

public abstract class BasicDialogValue {

    static abstract class PrimitivesSubset implements DialogResult {

        protected final Primitive value;

        abstract EnumSet<Primitive> getAllowedValues();

        protected PrimitivesSubset(Primitive value) {
            this.value = value;
            checkValue(value);
        }

        final void checkValue(Primitive p) {
            if (!getAllowedValues().contains(p)) {
                throw new RuntimeException("Unsupported primitive " + p + ". Allowed are " + getAllowedValues().toString());
            }
        }

        @Override
        public String writeValue() {
            if (value == null) {
                return "";
            }
            return value.name();
        }

        @Override
        public boolean toBoolean() {
            return value != null && value != Primitive.NO && value != Primitive.CANCEL;
        }

        public Primitive getValue() {
            return value;
        }

        public boolean compareValue(Primitive with) {
            if (getValue() == null && with == null) {
                return true;
            }
            checkValue(with);
            return getValue() == with;
        }

        public boolean compareValue(PrimitivesSubset with) {
            if (with == null) {
                return false;
            }
            return compareValue(with.getValue());
        }

        @Override
        public int getButtonIndex() {
            return Primitive.NO.getLegacyButton();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof PrimitivesSubset) {
                return this.compareValue(((PrimitivesSubset) obj));
            }
            return false;
        }

        @Override
        public int hashCode() {
            return getValue().hashCode();
        }

    }

    public static final EnumSet<Primitive> Yes = EnumSet.of(Primitive.YES);
    public static final EnumSet<Primitive> YesNo = EnumSet.of(Primitive.YES, Primitive.NO);
    public static final EnumSet<Primitive> YesCancel = EnumSet.of(Primitive.YES, Primitive.CANCEL);
    public static final EnumSet<Primitive> YesCancelSkip = EnumSet.of(Primitive.YES, Primitive.CANCEL, Primitive.SKIP);
    public static final EnumSet<Primitive> YesNoCancel = EnumSet.of(Primitive.YES, Primitive.NO, Primitive.CANCEL);
    public static final EnumSet<Primitive> YesNoSandbox = EnumSet.of(Primitive.YES, Primitive.NO, Primitive.SANDBOX);

}
