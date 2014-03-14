/*   Copyright (C) 2013 Red Hat, Inc.

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
package net.sourceforge.jnlp.security.appletextendedsecurity;

import net.sourceforge.jnlp.runtime.Translator;

public enum ExecuteAppletAction {

    ALWAYS, NEVER, YES, SANDBOX, NO;

    public String toChar() {
        switch (this) {
            case ALWAYS:
                return "A";
            case NEVER:
                return "N";
            case YES:
                return "y";
            case SANDBOX:
                return "s";
            case NO:
                return "n";
        }
        throw new RuntimeException("Unknown ExecuteUnsignedApplet");
    }

    public String toExplanation() {
        switch (this) {
            case ALWAYS:
                return Translator.R("APPEXTSECunsignedAppletActionAlways");
            case NEVER:
                return Translator.R("APPEXTSECunsignedAppletActionNever");
            case YES:
                return Translator.R("APPEXTSECunsignedAppletActionYes");
            case SANDBOX:
                return Translator.R("APPEXTSECunsignedAppletActionSandbox");
            case NO:
                return Translator.R("APPEXTSECunsignedAppletActionNo");
        }
        throw new RuntimeException("Unknown UnsignedAppletAction");
    }

    public static ExecuteAppletAction fromString(String s) {
        if (s.startsWith("A")) {
            return ExecuteAppletAction.ALWAYS;
        } else if (s.startsWith("N")) {
            return ExecuteAppletAction.NEVER;
        } else if (s.startsWith("y")) {
            return ExecuteAppletAction.YES;
        } else if (s.startsWith("s")) {
            return ExecuteAppletAction.SANDBOX;
        } else if (s.startsWith("n")) {
            return ExecuteAppletAction.NO;
        } else {
            throw new RuntimeException("Unknown ExecuteUnsignedApplet for " + s);
        }
    }

    @Override
    public String toString() {
        return toChar() + " - " + toExplanation();
    }
}
