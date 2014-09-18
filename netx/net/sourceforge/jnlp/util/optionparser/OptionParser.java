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

import net.sourceforge.jnlp.OptionsDefinitions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Parses for options (passed in as arguments)
 *  To use add entries to OPTIONS enum in OptionsDefinitions
 *  and make a static method return a list containing
 *  what OPTIONS are to be parsed for.
 */
public class OptionParser {

    private final String[] args;
    private final Map<OptionsDefinitions.OPTIONS, List<String>> parsedOptions;
    private final List<OptionsDefinitions.OPTIONS> possibleOptions;
    private final String MAINARG = "mainArg";
    //null represents all values that are parsed that don't have a corresponding option which are potential main args
    private final OptionsDefinitions.OPTIONS mainArg = null;
    private List<String> result;


    public OptionParser(String[] args, List<OptionsDefinitions.OPTIONS> options) {
        this.args = Arrays.copyOf(args, args.length);
        this.possibleOptions = options;

        result = new ArrayList<String>();
        parsedOptions = new HashMap<>();
        result = parseContents(args, result);

    }

    private List<String> parseContents(final String[] args, List<String> result) {
        String lastOption = "";
        int i = 0;
        while (i < args.length) {
            if (isOption(args[i])) {
                result.clear();
                if (args[i].contains("=")) {
                    result.add(args[i].split("=")[1]);
                    lastOption = args[i].split("=")[0];
                    parsedOptions.put(getOption(lastOption), new ArrayList<String>(result));
                } else {
                    lastOption = args[i];
                    if (parsedOptions.keySet().contains(getOption(lastOption))) {
                        if (getOption(lastOption).hasOneOrMoreArguments()) {
                            result = new ArrayList<>(parsedOptions.get(getOption(lastOption)));
                        }
                    }
                    if (getOption(lastOption).hasNoArguments()) {
                        parsedOptions.put(getOption(lastOption), null);
                        lastOption = MAINARG;
                    }
                }
            } else {
                result.add(args[i]);
                parsedOptions.put(getOption(lastOption), new ArrayList<String>(result));
                if (getOption(lastOption) != null) {
                    if (getOption(lastOption).hasOneArgument()) {
                        lastOption = MAINARG;
                        result.clear();
                    }
                }
            }
            i++;
        }
        return result;
    }

    public void findMainArg() {
        int i = args.length - 1;
        if (!(parsedOptions.keySet().contains(mainArg))) {
            while (i >= 0) {
                if (!isOption(args[i])) {
                    if(i > 1) {
                        if(isOption(args[i - 1])) {
                            if(!getOption(args[i - 1]).hasOneArgument() && !getOption(args[i -1]).hasOneOrMoreArguments()) {
                                addMainArg(i);
                                break;
                            }
                        } else {
                            addMainArg(i);
                            break;
                        }
                    } else {
                        addMainArg(i);
                        break;
                    }
                }
                i--;
            }
        }
    }

    private void addMainArg(final int i) {
        for (OptionsDefinitions.OPTIONS op : parsedOptions.keySet()) {
            if (!(parsedOptions.get(op) == null)) {
                if (parsedOptions.get(op).contains(args[i])) {
                    result.clear();
                    result.add(args[i]);
                    parsedOptions.get(op).remove(parsedOptions.get(op).indexOf(args[i]));
                    break;
                }
            }
        }
        parsedOptions.put(mainArg, result);
    }

    private boolean isOption(String input) {
        for(OptionsDefinitions.OPTIONS opt : possibleOptions){
            if (stringEqualsOption(input, opt)) {
                return true;
            }
        }
        return false;
    }

    private OptionsDefinitions.OPTIONS getOption(String input) {
        for(OptionsDefinitions.OPTIONS opt : possibleOptions){
            if (stringEqualsOption(input, opt)) {
                return opt;
            }
        }
        return mainArg;
    }

    private boolean stringEqualsOption(String input, OptionsDefinitions.OPTIONS opt) {
        String option = opt.option.replaceAll("^-","").split("=")[0];
        input = input.replaceAll("^-","").split("=")[0];
        if (input.equals(option)) {
            return true;
        }
        return false;
    }

    public boolean hasOption(OptionsDefinitions.OPTIONS option) {
        if (parsedOptions.containsKey(option)) {
                    return true;
        }
        return false;
    }

    public boolean mainArgExists() {
        if (parsedOptions.keySet().contains(mainArg)) {
            return true;
        }
        return false;
    }

    public String getMainArg() {
        if (mainArgExists()) {
            return parsedOptions.get(mainArg).toArray()[0].toString();
        }
        return "";
    }

    public List<String> getMainArgs() {
        if (mainArgExists()) {
            return parsedOptions.get(mainArg);
        }
        return new ArrayList<>();
    }

    public String getValue(OptionsDefinitions.OPTIONS option) {
        if (parsedOptions.get(option) != null) {
            return parsedOptions.get(option).toString().substring(1, parsedOptions.get(option).toString().length() - 1);
        }
        return "";
    }

    public List<String> getValues(OptionsDefinitions.OPTIONS option) {
        if (parsedOptions.get(option) != null) {
            return parsedOptions.get(option);
        }
        return new ArrayList<>();
    }
}
