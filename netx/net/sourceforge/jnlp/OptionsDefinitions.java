/*
   Copyright (C) 2008 Red Hat, Inc.

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
package net.sourceforge.jnlp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.sourceforge.jnlp.runtime.Translator.R;
import net.sourceforge.jnlp.util.docprovider.TextsProvider;

public class OptionsDefinitions {

    public static enum OPTIONS {

        //javaws undocummented swithces
        TRUSTALL("-Xtrustall","BOTrustall"),
        //javaws control-options
        ABOUT("-about", "BOAbout"),
        VIEWER("-viewer", "BOViewer"),
        CLEARCACHE("-Xclearcache", "BXclearcache"),
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
        CHECK("-check", "name", "IBOCheck"),
        HELP2("-help", "BOHelp2"),
        //policyeditor
        //-help
        FILE("-file", "policy_file", "PBOFile", NumberOfArguments.ONE),
        DEFAULTFILE("-defaultfile", "PBODefaultFile"),
        CODEBASE("-codebase", "url", "PBOCodebase", NumberOfArguments.ONE),
        SIGNEDBY("-signedby", "certificate_alias", "PBOSignedBy", NumberOfArguments.ONE),
        PRINCIPALS("-principals", "class_name principal_name", "PBOPrincipals", NumberOfArguments.EVEN_NUMBER_SUPPORTS_EQUALS_CHAR);

        public final String option;

        public final String helperString;
        public final String decriptionKey;
        private final NumberOfArguments numberOfArguments;

        OPTIONS(String option, String helperString, String decriptionKey, NumberOfArguments numberOfArguments) {
            this.decriptionKey = decriptionKey;
            this.option = option;
            this.helperString = helperString;
            this.numberOfArguments = numberOfArguments;
        }

        OPTIONS(String option, String helperString, String decriptionKey) {
            this(option, helperString, decriptionKey, NumberOfArguments.NONE);
        }

        OPTIONS(String option, String decriptionKey, NumberOfArguments numberOfArguments) {
            this(option, "", decriptionKey, numberOfArguments);
        }

        OPTIONS(String option, String decriptionKey) {
            this(option, "", decriptionKey);
        }

        public String getLocalizedDescription() {
            return R(decriptionKey);
        }

        public boolean hasNoArguments() {
            return numberOfArguments == NumberOfArguments.NONE;
        }

        public boolean hasEvenNumberSupportingEqualsChar() {
            return numberOfArguments == NumberOfArguments.EVEN_NUMBER_SUPPORTS_EQUALS_CHAR;
        }

        public boolean hasOneOrMoreArguments() {
            return numberOfArguments == NumberOfArguments.ONE_OR_MORE;
        }

        public boolean hasOneArgument() {
            return numberOfArguments == NumberOfArguments.ONE;
        }

        public String getArgumentExplanation() {
            return numberOfArguments.getMessage();
        }
    }

    private enum NumberOfArguments {
        NONE("NOAnone"),
        ONE("NOAone"),
        ONE_OR_MORE("NOAonemore"),
        EVEN_NUMBER_SUPPORTS_EQUALS_CHAR("NOAevennumber");

        String messageKey;

        NumberOfArguments(String messageKey) {
            this.messageKey = messageKey;
        }

        public String getMessage() {
            return R(messageKey);
        }
    }

    public static List<OPTIONS> getItwsettingsCommands() {
        return Arrays.asList(new OPTIONS[]{
            OPTIONS.HELP2,
            OPTIONS.LIST,
            OPTIONS.GET,
            OPTIONS.INFO,
            OPTIONS.SET,
            OPTIONS.RESET,
            OPTIONS.RESETALL,
            OPTIONS.HEADLESS,
            OPTIONS.CHECK,
            OPTIONS.VERBOSE
        });
    }

    public static List<OPTIONS> getPolicyEditorOptions() {
        return Arrays.asList(new OPTIONS[]{
            OPTIONS.HELP1,
            OPTIONS.FILE,
            OPTIONS.DEFAULTFILE,
            OPTIONS.CODEBASE,
            OPTIONS.SIGNEDBY,
            OPTIONS.PRINCIPALS,
            OPTIONS.VERBOSE
            }
        );
    }

    public static List<OPTIONS> getJavaWsControlOptions() {
        return Arrays.asList(new OPTIONS[]{
            OPTIONS.ABOUT,
            OPTIONS.VIEWER,
            OPTIONS.CLEARCACHE,
            OPTIONS.LICENSE,
            OPTIONS.HELP1}
        );
    }

    public static List<OPTIONS> getJavaWsRuntimeOptions() {
        return Arrays.asList(new OPTIONS[]{
            OPTIONS.VERSION,
            OPTIONS.ARG,
            OPTIONS.PARAM,
            OPTIONS.PROPERTY,
            OPTIONS.UPDATE,
            OPTIONS.VERBOSE,
            OPTIONS.NOSEC,
            OPTIONS.NOUPDATE,
            OPTIONS.HEADLESS,
            OPTIONS.STRICT,
            OPTIONS.XML,
            OPTIONS.REDIRECT,
            OPTIONS.NOFORK,
            OPTIONS.NOHEADERS,
            OPTIONS.OFFLINE,
            OPTIONS.TRUSTNONE,
            OPTIONS.JNLP,
            OPTIONS.HTML,
            OPTIONS.BROWSER
        });
    }

    public static List<OPTIONS> getJavaWsOptions() {
        List<OPTIONS> l = new ArrayList<>();
        l.addAll(getJavaWsRuntimeOptions());
        l.addAll(getJavaWsControlOptions());
        //trustall is not returned by getJavaWsRuntimeOptions
        //or getJavaWsControlOptions, as it is not desired in documentation
        l.add(OPTIONS.TRUSTALL);
        return l;
    }

    public static void main(String[] args) throws IOException {
        if (args[0].equals(TextsProvider.JAVAWS)) {
            printOptions(getJavaWsOptions());
        } else if (args[0].equals(TextsProvider.ITWEB_SETTINGS)) {
            printOptions(getItwsettingsCommands());
        } else if (args[0].equals(TextsProvider.POLICY_EDITOR)) {
            printOptions(getPolicyEditorOptions());
        }
    }

    private static void printOptions(List<OPTIONS> options) {
        StringBuilder sb = new StringBuilder();
        for (OPTIONS option : options) {
            sb.append(option.option).append(" ");
        }
        System.out.println(sb.toString().trim());
    }

}
