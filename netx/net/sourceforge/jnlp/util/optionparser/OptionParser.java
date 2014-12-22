/*Copyright (C) 2014 Red Hat, Inc.

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

package net.sourceforge.jnlp.util.optionparser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sourceforge.jnlp.OptionsDefinitions;
import net.sourceforge.jnlp.util.logging.OutputController;

import static net.sourceforge.jnlp.runtime.Translator.R;

/** Parses for options (passed in as arguments)
 *  To use add entries to OPTIONS enum in OptionsDefinitions
 *  and make a static method return a list containing
 *  what OPTIONS are to be parsed for.
 */
public class OptionParser {

    private final String[] args;
    private final List<ParsedOption> parsedOptions;
    private final List<OptionsDefinitions.OPTIONS> possibleOptions;
    //List of all possible main arguments
    private final List<String> mainArgumentList = new ArrayList<>();
    private boolean evenNumberFound;

    public OptionParser(String[] args, List<OptionsDefinitions.OPTIONS> options) throws UnevenParameterException {
        this.args = Arrays.copyOf(args, args.length);
        this.possibleOptions = options;

        evenNumberFound = false;
        parsedOptions = new ArrayList<>();
        parseContents();
        if (evenNumberFound) {
            checkOptionHasEvenNumber();
        }

    }

    private void parseContents() {
        ParsedOption lastOption = null;
        for (String arg : args) {
            if (isOption(arg)) {
                lastOption = addOptionToList(arg);
            } else if (shouldAddParam(lastOption)) {
                lastOption.addParam(arg);
            } else if (isEvenNumberSupportingEqualsChars(lastOption)) {
                evenNumberFound = true;
                handleEvenNumberSupportingEqualsChar(lastOption, arg);
            } else {
                mainArgumentList.add(arg);
            }
        }
    }

    private void handleEvenNumberSupportingEqualsChar(final ParsedOption lastOption, String arg) {
        if (arg.contains("=")) {
            lastOption.addParam(arg.split("=")[0]);
            lastOption.addParam(arg.split("=", 2)[1]);
        } else {
            lastOption.addParam(arg);
        }
    }
    private boolean shouldAddParam(final ParsedOption lastOption) {
        return lastOption != null &&
                (oneOrMoreArguments(lastOption) || isOneArgumentNotFull(lastOption));
    }

    private boolean isOneArgumentNotFull(final ParsedOption lastOption) {
        return lastOption.getOption().hasOneArgument() && lastOption.getParams().size() == 0;
    }

    private boolean oneOrMoreArguments(final ParsedOption lastOption) {
        return lastOption.getOption().hasOneOrMoreArguments();
    }

    private boolean isEvenNumberSupportingEqualsChars(final ParsedOption lastOption) {
        return lastOption != null &&
                lastOption.getOption().hasEvenNumberSupportingEqualsChar();
    }

    private ParsedOption addOptionToList(final String arg) {
        ParsedOption option = new ParsedOption(argumentToOption(arg));
        if (arg.contains("=")) {
            option.addParam(arg.split("=", 2)[1]);
        }
        parsedOptions.add(option);
        return option;
    }

    private void checkOptionHasEvenNumber() throws UnevenParameterException {
        for (ParsedOption option : parsedOptions) {
            if (isEvenNumberSupportingEqualsChars(option)) {
                if (option.getParams().size() % 2 != 0){
                    throw new UnevenParameterException(R("OPUnevenParams", option.getOption().option));
                }
            }
        }
    }

    private OptionsDefinitions.OPTIONS argumentToOption(final String arg) {
        for (OptionsDefinitions.OPTIONS opt : possibleOptions) {
            if (stringEqualsOption(arg, opt)) {
                return opt;
            }
        }
        return null;
    }

    private boolean isOption(String input) {
        if (argumentToOption(input) != null) {
            return true;
        }
        return false;
    }

    protected static boolean stringEqualsOption(String input, OptionsDefinitions.OPTIONS opt) {
        String option = removeLeadingHyphens(opt.option);
        input = removeLeadingHyphens(input).split("=")[0];
        return input.equals(option);
    }

    private static String removeLeadingHyphens(final String input) {
        return input.replaceAll("^-*", "");
    }

    public boolean hasOption(OptionsDefinitions.OPTIONS option) {
        for (ParsedOption parsed : parsedOptions) {
            if (option == parsed.getOption()) {
                return true;
            }
        }
        return false;
    }

    public boolean mainArgExists() {
        if (mainArgumentList.size() > 0) {
            return true;
        }
        return false;
    }

    public String getMainArg() {
        if (mainArgExists()) {
            return mainArgumentList.get(0);
        }
        return "";
    }

    public List<String> getMainArgs() {
        return new ArrayList<>(mainArgumentList);
    }

    public String getParam(OptionsDefinitions.OPTIONS option) {
        List<String> params = getParams(option);
        if (params.size() > 0) {
            return getParams(option).get(0);
        }
        return "";
    }

    public List<String> getParams(OptionsDefinitions.OPTIONS option) {
        List<String> result = new ArrayList<>();
        for (ParsedOption parsed : parsedOptions) {
            if (parsed.getOption() == option) {
                for (String param : parsed.getParams()) {
                    result.add(param);
                }
            }
        }
        return result;
    }

    public int getNumberOfOptions() {
        return parsedOptions.size();
    }

    public void addOption(OptionsDefinitions.OPTIONS options, String... params) {
        ParsedOption x = new ParsedOption(options);
        for (String string : params) {
            x.addParam(string);
        }
        parsedOptions.add(x);
    }
}
