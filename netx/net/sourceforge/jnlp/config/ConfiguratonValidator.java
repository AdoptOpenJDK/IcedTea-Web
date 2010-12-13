/* Validator.java
   Copyright (C) 2010 Red Hat, Inc.

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

package net.sourceforge.jnlp.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Validates a DeploymentConfiguration by identifying settings with
 * unrecognized names or incorrect values.
 */
public class ConfiguratonValidator {

    private List<Setting<String>> incorrectEntries;
    private List<Setting<String>> unrecognizedEntries;
    private Map<String, Setting<String>> toValidate = null;

    private boolean validated = false;

    /**
     * @param toValidate the settings to validate
     */
    public ConfiguratonValidator(Map<String, Setting<String>> toValidate) {
        this.toValidate = toValidate;
    }

    /**
     * Validates the settings used in the constructor. Use
     * {@link #getIncorrectSetting()} and {@link #getUnrecognizedSetting()} to
     * get the list of incorrect or unrecognized settings.
     */
    public void validate() {
        incorrectEntries = new ArrayList<Setting<String>>();
        unrecognizedEntries = new ArrayList<Setting<String>>();

        Map<String, Setting<String>> knownGood = Defaults.getDefaults();

        for (String key : toValidate.keySet()) {
            // check for known incorrect settings
            if (knownGood.containsKey(key)) {
                Setting<String> good = knownGood.get(key);
                Setting<String> unknown = toValidate.get(key);
                ValueValidator checker = good.getValidator();
                if (checker != null) {
                    try {
                        checker.validate(unknown.getValue());
                    } catch (IllegalArgumentException e) {
                        Setting<String> strange = new Setting<String>(unknown);
                        strange.setValue(unknown.getValue());
                        incorrectEntries.add(strange);
                    }
                }
            } else {
                // check for unknown settings
                Setting<String> strange = new Setting<String>(toValidate.get(key));
                unrecognizedEntries.add(strange);
            }
        }

        validated = true;
    }

    /**
     * @return a list of settings which have incorrect values
     */
    public List<Setting<String>> getIncorrectSetting() {
        if (!validated) {
            throw new IllegalStateException();
        }

        return new ArrayList<Setting<String>>(incorrectEntries);
    }

    /**
     * @return a list of settings which are not recognized
     */
    public List<Setting<String>> getUnrecognizedSetting() {
        if (!validated) {
            throw new IllegalStateException();
        }
        return new ArrayList<Setting<String>>(unrecognizedEntries);
    }

}
