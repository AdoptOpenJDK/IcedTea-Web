package net.adoptopenjdk.icedteaweb.config.validators;

import net.adoptopenjdk.icedteaweb.manifest.ManifestAttributesChecker;

public class ManifestAttributeCheckValidator extends MultipleStringValueValidator {

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
