/* BasicValueCheckers.java
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

import net.sourceforge.jnlp.runtime.ManifestAttributesChecker;

import java.io.File;
import static net.sourceforge.jnlp.runtime.Translator.R;

import java.net.URL;
import java.util.Arrays;
import java.util.Locale;

/**
 * Provides {@link ValueValidator} implementations for some common value types
 *
 * @see #getBooleanValidator()
 * @see #getFilePathValidator()
 * @see #getRangedIntegerValidator(int, int)
 * @see #getStringValidator(String[])
 * @see #getUrlValidator()
 */
public class BasicValueValidators {

    /**
     * Checks if a value is a valid boolean
     */
    private static class BooleanValidator implements ValueValidator {

        @Override
        public void validate(Object value) throws IllegalArgumentException {
            Object possibleValue = value;

            if (possibleValue instanceof String) {
                String lower = ((String) possibleValue).toLowerCase(Locale.ENGLISH);
                if (lower.equals(Boolean.TRUE.toString())
                        || (lower.equals(Boolean.FALSE.toString()))) {
                    possibleValue = Boolean.valueOf(lower);
                }
            }

            if (!(possibleValue instanceof Boolean)) {
                throw new IllegalArgumentException();
            }
        }

        @Override
        public String getPossibleValues() {
            return R("VVPossibleBooleanValues", Boolean.TRUE.toString(), Boolean.FALSE.toString());
        }
    };

    /**
     * Checks if a value is a valid file path (not a valid file!). The actual
     * file may or may not exist
     */
    //package private for testing purposes
    static class FilePathValidator implements ValueValidator {
        
        @Override
        public void validate(Object value) throws IllegalArgumentException {
            if (value == null) {
                return;
            }

            Object possibleValue = value;

            if (!(possibleValue instanceof String)) {
                throw new IllegalArgumentException("Value should be string!");
            }

            String possibleFile = (String) possibleValue;
            
                boolean absolute = new File(possibleFile).isAbsolute();
                if (!absolute) {
                    throw new IllegalArgumentException("File must be absolute");
                }

        }

        @Override
        public String getPossibleValues() {
            return R("VVPossibleFileValues");
        }

    }

    /**
     * Checks that the value is an Integer or Long (or a String representation
     * of one) that is within a desired range).
     */
    private static class RangedIntegerValidator implements ValueValidator {
        private int low = 0;
        private int high = 0;

        public RangedIntegerValidator(int low, int high) {
            this.low = low;
            this.high = high;
        }

        @Override
        public void validate(Object value) throws IllegalArgumentException {
            Object possibleValue = value;

            long actualValue = 0;
            try {
                if (possibleValue instanceof String) {
                    actualValue = Long.valueOf((String) possibleValue);
                } else if (possibleValue instanceof Integer) {
                    actualValue = (Integer) possibleValue;
                } else if (possibleValue instanceof Long) {
                    actualValue = (Long) possibleValue;
                } else {
                    throw new IllegalArgumentException("Must be an integer");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Must be an integer");

            }

            if (actualValue < low || actualValue > high) {
                throw new IllegalArgumentException("Not in range from " + low + " to " + high);
            }
        }

        @Override
        public String getPossibleValues() {
            return R("VVPossibleRangedIntegerValues", low, high);
        }

    };

    /**
     * Checks that the value is one of the acceptable String values
     */
    private static class StringValueValidator implements ValueValidator {
        String[] options = null;

        public StringValueValidator(String[] acceptableOptions) {
            options = acceptableOptions;
        }

        @Override
        public void validate(Object value) throws IllegalArgumentException {
            Object possibleValue = value;
            if (!(possibleValue instanceof String)) {
                throw new IllegalArgumentException("Must be a string");
            }

            String stringVal = (String) possibleValue;
            boolean found = false;
            for (String knownVal : options) {
                if (knownVal.equals(stringVal)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                throw new IllegalArgumentException();
            }
        }

        @Override
        public String getPossibleValues() {
            return Arrays.toString(options);
        }

    }

    /**
     * Checks that the value is one of the acceptable single String values
     * or an acceptable combination of String values
     */
    private static class MultipleStringValueValidator implements ValueValidator {
        private final String[] singleOptions;
        private final String[] comboOptions;

        public MultipleStringValueValidator(String[] singleOptions, String[] comboOptions) {
            this.singleOptions = singleOptions;
            this.comboOptions = comboOptions;
        }

        @Override
        public void validate(Object value) throws IllegalArgumentException {
            Object possibleValue = value;
            if (!(possibleValue instanceof String)) {
                throw new IllegalArgumentException("Must be a string");
            }

            String stringVal = (String) possibleValue;
            boolean found = false;
            for (String knownVal : singleOptions) {
                if (knownVal.equals(stringVal)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                String[] possibleCombo = splitCombination(stringVal);
                for (String val : possibleCombo) {
                    if (comboOptionsContains(val)) {
                        found = true;
                    } else {
                        throw new IllegalArgumentException();
                    }
                }
            }

            if (!found) {
                throw new IllegalArgumentException();
            }
        }

        private boolean comboOptionsContains(String possibleVal) {
            for (String value : comboOptions) {
                if (value.equals(possibleVal)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String getPossibleValues() {
            String message = "(Values that can be used alone only): " + Arrays.toString(singleOptions) +
                    " (Values that can be used in combination separated by the delimiter \""
                    + DELIMITER + "\" with no space expected ): " + Arrays.toString(comboOptions);
            return message;
        }

    }

    private final static String DELIMITER = ",";

    public static String[] splitCombination(String val) {
        return val.split(DELIMITER);
    }

    private static class ManifestAttributeCheckValidator extends MultipleStringValueValidator {

        public ManifestAttributeCheckValidator() {
            super(new String[] {
                ManifestAttributesChecker.MANIFEST_ATTRIBUTES_CHECK.ALL.toString(),
                ManifestAttributesChecker.MANIFEST_ATTRIBUTES_CHECK.NONE.toString()
            }, new String[] {
                ManifestAttributesChecker.MANIFEST_ATTRIBUTES_CHECK.ALAC.toString(),
                ManifestAttributesChecker.MANIFEST_ATTRIBUTES_CHECK.CODEBASE.toString(),
                ManifestAttributesChecker.MANIFEST_ATTRIBUTES_CHECK.ENTRYPOINT.toString(),
                ManifestAttributesChecker.MANIFEST_ATTRIBUTES_CHECK.PERMISSIONS.toString(),
                ManifestAttributesChecker.MANIFEST_ATTRIBUTES_CHECK.TRUSTED.toString()
            });
        }
    }
    /**
     * Checks that the value is a URL
     */
    private static class UrlValidator implements ValueValidator {

        @Override
        public void validate(Object value) throws IllegalArgumentException {
            if (value == null) {
                return;
            }
            try {
                new URL((String) value);
            } catch (Exception e) {
                throw new IllegalArgumentException();
            }
        }

        @Override
        public String getPossibleValues() {
            return R("VVPossibleUrlValues");
        }

    }

    /**
     * @return a {@link ValueValidator} that can be used to check if an object is
     * a valid Boolean
     */
    public static ValueValidator getBooleanValidator() {
        return new BooleanValidator();
    }

    /**
     * @return a {@link ValueValidator} that can be used to check if an object is
     * a String containing a valid file path or not
     */
    public static ValueValidator getFilePathValidator() {
        return new FilePathValidator();
    }

    /**
     * Returns a {@link ValueValidator} that checks if an object represents a
     * valid integer (it is a Integer or Long or a String representation of
     * one), within the given range. The values are inclusive.
     * @param low the lowest valid value
     * @param high the highest valid value
     * @return value validator for given range
     */
    public static ValueValidator getRangedIntegerValidator(int low, int high) {
        return new RangedIntegerValidator(low, high);
    }

    /**
     * Returns a {@link ValueValidator} that checks if an object is a string from
     * one of the provided Strings.
     * @param validValues an array of Strings which are considered valid
     * @return validator for given strings
     */
    public static ValueValidator getStringValidator(String[] validValues) {
        return new StringValueValidator(validValues);
    }

    /**
     * Returns a {@link ValueValidator} that checks if an object is a string from
     * one of the provided single option Strings or a combination from
     * the provided combination Strings.
     * @param singleValues an array of Strings which are considered valid only by themselves
     * @param comboValues an array of Strings which are considered valid in any combination
     *                    with themselves
     * @return validator forgiven strings
     */
    public static ValueValidator getMultipleStringValidator(String[] singleValues, String[] comboValues) {
        return new MultipleStringValueValidator(singleValues, comboValues);
    }

    /**
     * @return a {@link ValueValidator} that checks if an object is a string
     * from the possible single or combination ManifestAttributeCheck values
     */
    public static ValueValidator getManifestAttributeCheckValidator() {
        return new ManifestAttributeCheckValidator();
    }

    /**
     * @return a {@link ValueValidator} that checks if an object represents a
     * valid url
     */
    public static ValueValidator getUrlValidator() {
        return new UrlValidator();
    }

}
