package net.adoptopenjdk.icedteaweb.commandline;

import java.util.Objects;

import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;

public enum CommandLineOptions {
    //javaws undocumented switches
    TRUSTALL("-Xtrustall","BOTrustall"),
    //javaws control-options
    ABOUT("-about", "BOAbout"),
    VIEWER("-viewer", "BOViewer"),
    CLEARCACHE("-Xclearcache", "BXclearcache", NumberOfArguments.NONE_OR_ONE),
    LISTCACHEIDS("-Xcacheids", "BXcacheids", NumberOfArguments.NONE_OR_ONE),
    LICENSE("-license", "BOLicense"),
    HELP1("-help", "BOHelp1"),
    //javaws run-options
    VERSION("-version", "BOVersion"),
    ARG("-arg", "arg", "BOArg", NumberOfArguments.ONE_OR_MORE),
    PARAM("-param", "name=value", "BOParam", NumberOfArguments.ONE_OR_MORE),
    PROPERTY("-property", "name=value", "BOProperty", NumberOfArguments.ONE_OR_MORE),
    UPDATE("-update", "seconds", "BOUpdate", NumberOfArguments.ONE),
    VERBOSE("-verbose", "BOVerbose"),
    DETAILS("-details", "BOVerbose"), //backward compatibility for itweb settings
    NOSEC("-nosecurity", "BONosecurity"),
    NOUPDATE("-noupdate", "BONoupdate"),
    HEADLESS("-headless", "BOHeadless"),
    STRICT("-strict", "BOStrict"),
    XML("-xml", "BOXml"),
    REDIRECT("-allowredirect", "BOredirect"),
    NOFORK("-Xnofork", "BXnofork"),
    NOHEADERS("-Xignoreheaders", "BXignoreheaders"),
    OFFLINE("-Xoffline", "BXoffline"),
    TRUSTNONE("-Xtrustnone","BOTrustnone"),
    JNLP("-jnlp","BOJnlp", NumberOfArguments.ONE),
    HTML("-html","BOHtml", NumberOfArguments.ONE_OR_MORE),
    BROWSER("-browser", "BrowserArg", NumberOfArguments.ONE_OR_MORE),
    //itweb settings
    LIST("-list", "IBOList"),
    GET("-get", "name", "IBOGet", NumberOfArguments.ONE_OR_MORE),
    INFO("-info", "name", "IBOInfo", NumberOfArguments.ONE_OR_MORE),
    SET("-set", "name value", "IBOSet", NumberOfArguments.EVEN_NUMBER_SUPPORTS_EQUALS_CHAR),
    RESETALL("-reset", "all", "IBOResetAll"),
    RESET("-reset", "name", "IBOReset", NumberOfArguments.ONE_OR_MORE),
    CHECK("-check", "IBOCheck"),
    HELP2("-help", "BOHelp2"),
    //policyeditor
    //-help
    FILE("-file", "policy_file", "PBOFile", NumberOfArguments.ONE),
    DEFAULTFILE("-defaultfile", "PBODefaultFile"),
    CODEBASE("-codebase", "url", "PBOCodebase", NumberOfArguments.ONE),
    SIGNEDBY("-signedby", "certificate_alias", "PBOSignedBy", NumberOfArguments.ONE),
    PRINCIPALS("-principals", "class_name principal_name", "PBOPrincipals", NumberOfArguments.EVEN_NUMBER_SUPPORTS_EQUALS_CHAR);

    private final String option;

    private final String helperString;

    private final String descriptionKey;

    private final NumberOfArguments numberOfArguments;

    CommandLineOptions(final String option, final String helperString, final String descriptionKey, final NumberOfArguments numberOfArguments) {
        this.descriptionKey = descriptionKey;
        this.option = option;
        this.helperString = helperString;
        this.numberOfArguments = numberOfArguments;
    }

    CommandLineOptions(final String option, final String helperString, final String descriptionKey) {
        this(option, helperString, descriptionKey, NumberOfArguments.NONE);
    }

    CommandLineOptions(final String option, final String descriptionKey, final NumberOfArguments numberOfArguments) {
        this(option, "", descriptionKey, numberOfArguments);
    }

    CommandLineOptions(final String option, final String descriptionKey) {
        this(option, "", descriptionKey);
    }

    public String getOption() {
        return option;
    }

    public String getHelperString() {
        return helperString;
    }

    public String getLocalizedDescription() {
        return R(descriptionKey);
    }

    public boolean hasEvenNumberSupportingEqualsChar() {
        return hasNumberOfArguments(NumberOfArguments.EVEN_NUMBER_SUPPORTS_EQUALS_CHAR);
    }

    public boolean hasOneOrMoreArguments() {
        return hasNumberOfArguments(NumberOfArguments.ONE_OR_MORE);
    }

    public boolean hasOneArgument() {
        return hasNumberOfArguments(NumberOfArguments.ONE);
    }

    private boolean hasNumberOfArguments(final NumberOfArguments number) {
        return Objects.equals(numberOfArguments, number);
    }

    public String getArgumentExplanation() {
        return numberOfArguments.getMessage();
    }
}
