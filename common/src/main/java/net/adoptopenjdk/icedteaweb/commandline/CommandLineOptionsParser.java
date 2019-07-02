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

package net.adoptopenjdk.icedteaweb.commandline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Parses for options (passed in as arguments)
 * To use add entries to CommandLineOptions enum in CommandLineOptions
 * and make a static method return a list containing
 * what CommandLineOptions are to be parsed for.
 */
public class CommandLineOptionsParser {

    private static final String LEADING_HYPHEN = "^-*";

    private static final String EQUALS_CHAR = "=";

    private final List<ParsedCommandLineOption> parsedOptions;

    private final List<CommandLineOptions> possibleOptions;

    //List of all possible main arguments
    private final List<String> mainArgumentList = new ArrayList<>();

    public CommandLineOptionsParser(final String[] args, final List<CommandLineOptions> options) throws UnevenParameterException {
        this.possibleOptions = options;

        boolean evenNumberFound = false;
        parsedOptions = new ArrayList<>();

        ParsedCommandLineOption lastOption = null;
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

        if (evenNumberFound) {
            checkOptionHasEvenNumber();
        }
    }

    public boolean hasOption(final CommandLineOptions option) {
        return parsedOptions.stream()
                .filter(o -> Objects.equals(o.getOption(), option))
                .count() > 0;
    }

    public boolean mainArgExists() {
        return mainArgumentList.size() > 0;
    }

    public String getMainArg() {
        if (mainArgumentList.size() > 0) {
            return mainArgumentList.get(0);
        }
        return null;
    }

    public List<String> getMainArgs() {
        return Collections.unmodifiableList(mainArgumentList);
    }

    public String getParam(final CommandLineOptions option) {
        return getParams(option).stream().findAny().orElse("");
    }

    public List<String> getParams(final CommandLineOptions option) {
        return parsedOptions.stream()
                .filter(o -> Objects.equals(option, o.getOption()))
                .flatMap(o -> o.getParams().stream())
                .collect(Collectors.toList());
    }

    @Deprecated // Only used in test
    public int getNumberOfOptions() {
        return parsedOptions.size();
    }

    private void handleEvenNumberSupportingEqualsChar(final ParsedCommandLineOption lastOption, final String arg) {
        if (arg.contains(EQUALS_CHAR)) {
            lastOption.addParam(arg.split(EQUALS_CHAR)[0]);
            lastOption.addParam(arg.split(EQUALS_CHAR, 2)[1]);
        } else {
            lastOption.addParam(arg);
        }
    }

    private boolean shouldAddParam(final ParsedCommandLineOption lastOption) {
        return lastOption != null &&
                (oneOrMoreArguments(lastOption) || isOneArgumentNotFull(lastOption));
    }

    private boolean isOneArgumentNotFull(final ParsedCommandLineOption lastOption) {
        return lastOption.getOption().hasOneArgument() && lastOption.getParams().isEmpty();
    }

    private boolean oneOrMoreArguments(final ParsedCommandLineOption lastOption) {
        return lastOption.getOption().hasOneOrMoreArguments();
    }

    private boolean isEvenNumberSupportingEqualsChars(final ParsedCommandLineOption lastOption) {
        return lastOption != null &&
                lastOption.getOption().hasEvenNumberSupportingEqualsChar();
    }

    private ParsedCommandLineOption addOptionToList(final String arg) {
        final ParsedCommandLineOption option = new ParsedCommandLineOption(argumentToOption(arg));
        if (arg.contains(EQUALS_CHAR)) {
            option.addParam(arg.split(EQUALS_CHAR, 2)[1]);
        }
        parsedOptions.add(option);
        return option;
    }

    private void checkOptionHasEvenNumber() throws UnevenParameterException {
        parsedOptions.stream()
                .filter(o -> isEvenNumberSupportingEqualsChars(o))
                .filter(o -> o.getParams().size() % 2 != 0)
                .findAny()
                .ifPresent(o -> {
                    throw new UnevenParameterException("For option " + o.getOption().getOption() + " expected an even number of params.");
                });
    }

    private CommandLineOptions argumentToOption(final String arg) {
        return possibleOptions.stream()
                .filter(o -> stringEqualsOption(arg, o))
                .findAny()
                .orElse(null);
    }

    private boolean isOption(final String input) {
        return argumentToOption(input) != null;
    }

    public static boolean stringEqualsOption(final String input, final CommandLineOptions opt) {
        final String option = removeLeadingHyphens(opt.getOption());
        final String convertedInput = removeLeadingHyphens(input).split("=")[0];
        return convertedInput.equals(option);
    }

    private static String removeLeadingHyphens(final String input) {
        return input.replaceAll(LEADING_HYPHEN, "");
    }
}
