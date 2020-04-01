package net.adoptopenjdk.icedteaweb.config;

import net.adoptopenjdk.icedteaweb.config.validators.BooleanValidator;
import net.adoptopenjdk.icedteaweb.config.validators.FilePathValidator;
import net.adoptopenjdk.icedteaweb.config.validators.MultipleStringValueValidator;
import net.adoptopenjdk.icedteaweb.config.validators.PortValidator;
import net.adoptopenjdk.icedteaweb.config.validators.RangedIntegerValidator;
import net.adoptopenjdk.icedteaweb.config.validators.RustCpValidator;
import net.adoptopenjdk.icedteaweb.config.validators.StringValueValidator;
import net.adoptopenjdk.icedteaweb.config.validators.UrlValidator;
import net.adoptopenjdk.icedteaweb.config.validators.ValidatorUtils;
import net.adoptopenjdk.icedteaweb.config.validators.ValueValidator;
import net.adoptopenjdk.icedteaweb.manifest.ManifestAttributesChecker;
import net.sourceforge.jnlp.config.DeploymentConfiguration;

/**
 * Provides {@link net.adoptopenjdk.icedteaweb.config.validators.ValueValidator} implementations for some common value types
 *
 * @see ValidatorFactory#createBooleanValidator()
 * @see ValidatorFactory#createFilePathValidator()
 * @see ValidatorFactory#createRangedIntegerValidator(int, int)
 * @see ValidatorFactory#createStringValidator(String[])
 * @see ValidatorFactory#createUrlValidator()
 */
public class ValidatorFactory {

    /**
     * @return a {@link net.adoptopenjdk.icedteaweb.config.validators.ValueValidator} that can be used to check if an object is
     * a valid Boolean
     */
    public static ValueValidator createBooleanValidator() {
        return new BooleanValidator();
    }

    /**
     * @return a {@link net.adoptopenjdk.icedteaweb.config.validators.ValueValidator} that can be used to check if an object is
     * a String containing a valid file path or not
     */
    public static ValueValidator createFilePathValidator() {
        return new FilePathValidator();
    }

    public static ValueValidator createBrowserPathValidator() {
        return new ValueValidator() {
            @Override
            public void validate(final Object value) throws IllegalArgumentException {
                if (value == null) {
                    return;
                }
                if (!(value instanceof String)) {
                    throw new IllegalArgumentException("Value should be string!");
                }
               if (ValidatorUtils.verifyFileOrCommand((String)value) == null){
                    //just warn?
                    throw new IllegalArgumentException("Value should be file, or on PATH, or known keyword. See possible values.");
               }
            }

            @Override
            public String getPossibleValues() {
                return DeploymentConfiguration.VVPossibleBrowserValues();
            }
        };
    }

    /**
     * Returns a {@link net.adoptopenjdk.icedteaweb.config.validators.ValueValidator} that checks if an object represents a
     * valid integer (it is a Integer or Long or a String representation of
     * one), within the given range. The values are inclusive.
     * @param low the lowest valid value
     * @param high the highest valid value
     * @return value validator for given range
     */
    public static ValueValidator createRangedIntegerValidator(final int low, final int high) {
        return new RangedIntegerValidator(low, high);
    }

    /**
     * Returns a {@link net.adoptopenjdk.icedteaweb.config.validators.ValueValidator} that checks if an object is a string from
     * one of the provided Strings.
     * @param validValues an array of Strings which are considered valid
     * @return validator for given strings
     */
    public static ValueValidator createStringValidator(final String[] validValues) {
        return new StringValueValidator(validValues);
    }

    /**
     * Returns a {@link net.adoptopenjdk.icedteaweb.config.validators.ValueValidator} that checks if an object is a string from
     * one of the provided single NumberOfArguments Strings or a combination from
     * the provided combination Strings.
     * @param singleValues an array of Strings which are considered valid only by themselves
     * @param comboValues an array of Strings which are considered valid in any combination
     *                    with themselves
     * @return validator forgiven strings
     */
    public static ValueValidator createMultipleStringValidator(final String[] singleValues, final String[] comboValues) {
        return new MultipleStringValueValidator(singleValues, comboValues);
    }

    /**
     * @return a {@link net.adoptopenjdk.icedteaweb.config.validators.ValueValidator} that checks if an object is a string
     * from the possible single or combination ManifestAttributeCheck values
     */
    public static ValueValidator createManifestAttributeCheckValidator() {
        return createMultipleStringValidator(new String[] {
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

    /**
     * @return a {@link net.adoptopenjdk.icedteaweb.config.validators.ValueValidator} that checks if an object represents a
     * valid url
     */
    public static ValueValidator createUrlValidator() {
        return new UrlValidator();
    }

    /**
     * @return a {@link net.adoptopenjdk.icedteaweb.config.validators.PortValidator} that checks if a port is null or
     * within a valid port range
     */
    public static PortValidator createPortValidator() {
        return new PortValidator();
    }

    public static ValueValidator createRustCpValidator() {
        return new RustCpValidator();
    }
 }
