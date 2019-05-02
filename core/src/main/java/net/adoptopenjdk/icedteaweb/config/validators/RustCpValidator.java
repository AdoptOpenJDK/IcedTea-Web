package net.adoptopenjdk.icedteaweb.config.validators;

import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;

public class RustCpValidator implements ValueValidator {


    @Override
    public void validate(final Object value) throws IllegalArgumentException {
        //cant be wrong...
        //but we need that getPossibleValues description
    }


    @Override
    public String getPossibleValues() {
            return R("VVRustCpModifiers");
    }

}
