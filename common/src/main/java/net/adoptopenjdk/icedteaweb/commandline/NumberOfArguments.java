package net.adoptopenjdk.icedteaweb.commandline;

import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;

public enum NumberOfArguments {
    NONE("NOAnone"),
    ONE("NOAone"),
    ONE_OR_MORE("NOAonemore"),
    NONE_OR_ONE("NOAnonorone"),
    EVEN_NUMBER_SUPPORTS_EQUALS_CHAR("NOAevennumber");

    final String messageKey;

    NumberOfArguments(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessage() {
        return R(messageKey);
    }
}
