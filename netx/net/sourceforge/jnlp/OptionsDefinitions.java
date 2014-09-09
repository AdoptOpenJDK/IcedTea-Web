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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.sourceforge.jnlp.runtime.Translator.R;

public class OptionsDefinitions {

    public static enum OPTIONS {

        //javaws undocummented swithces
        TRUSTALL("-Xtrustall","BOTrustall"),
        //javaws control-options
        ABOUT("-about", "BOAbout"),
        VIEWER("-viewer", "BOViewer"),
        CLEARCACHE("-Xclearcache", "BXclearcache"),
        LICENSE("-license", "BOLicense"),
        HELP("-help", "BOHelp"),
        //javaws run-options
        VERSION("-version", "BOVersion"),
        ARG("-arg", "arg", "BOArg", NumberOfArguments.ONE_OR_MORE),
        PARAM("-param", "name=value", "BOParam", NumberOfArguments.ONE_OR_MORE),
        PROPERTY("-property", "name=value", "BOProperty", NumberOfArguments.ONE_OR_MORE),
        UPDATE("-update", "seconds", "BOUpdate", NumberOfArguments.ONE),
        VERBOSE("-verbose", "BOVerbose"),
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
        //itweb settings
        NODASHHELP("help", "IBOHelp"),
        LIST("list", "IBOList"),
        GET("get", "name", "IBOGet"),
        INFO("info", "name", "IBOInfo"),
        SET("set", "name value", "IBOSet"),
        RESETALL("reset", "all", "IBOResetAll"),
        RESET("reset", "name", "IBOReset"),
        CHECK("check", "name", "IBOCheck"),
        //policyeditor
        //-help
        FILE("-file", "policy_file", "PBOFile"),
        CODEBASE("-codebase", "url", "PBOCodebase");

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
        
        public boolean expectsEqualsChar() {
            return numberOfArguments == NumberOfArguments.EQUALS_CHAR;
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
        NONE("No argument expected"),
        ONE("Exactly one argument expected"),
        ONE_OR_MORE("Expected one or more arguments"),
        EQUALS_CHAR("Expected -param=value vaue declaration");
        
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
            OPTIONS.NODASHHELP,
            OPTIONS.LIST,
            OPTIONS.GET,
            OPTIONS.INFO,
            OPTIONS.SET,
            OPTIONS.RESETALL,
            OPTIONS.RESET,
            OPTIONS.CHECK
        });
    }

    public static List<OPTIONS> getPolicyEditorOptions() {
        return Arrays.asList(new OPTIONS[]{
            OPTIONS.HELP,
            OPTIONS.FILE,
            OPTIONS.CODEBASE}
        );
    }

    public static List<OPTIONS> getJavaWsControlOptions() {
        return Arrays.asList(new OPTIONS[]{
            OPTIONS.ABOUT,
            OPTIONS.VIEWER,
            OPTIONS.CLEARCACHE,
            OPTIONS.LICENSE,
            OPTIONS.HELP}
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
            OPTIONS.TRUSTNONE});
    }

    public static List<OPTIONS> getJavaWsOptions() {
        List<OPTIONS> l = new ArrayList<>();
        l.addAll(getJavaWsRuntimeOptions());
        l.addAll(getJavaWsControlOptions());
        //trustall is not returned by getJavaWsRuntimeOptions
        //or getJavaWsControlOptions, as it is not desitred in documentation
        l.add(OPTIONS.TRUSTALL);
        return l;
    }

    
}
