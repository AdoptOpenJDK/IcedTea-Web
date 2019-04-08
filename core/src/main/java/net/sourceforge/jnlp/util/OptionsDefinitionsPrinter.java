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
package net.sourceforge.jnlp.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.sourceforge.jnlp.util.docprovider.TextsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.adoptopenjdk.icedteaweb.option.OptionsDefinitions.OPTIONS;

/**
 * This class allows to print the various set of options.
 */
public class OptionsDefinitionsPrinter {
    private final static Logger LOG = LoggerFactory.getLogger(OptionsDefinitionsPrinter.class);

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
            OPTIONS.LISTCACHEIDS,
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
        final List<OPTIONS> l = new ArrayList<>();
        l.addAll(getJavaWsRuntimeOptions());
        l.addAll(getJavaWsControlOptions());
        //trustall is not returned by getJavaWsRuntimeOptions
        //or getJavaWsControlOptions, as it is not desired in documentation
        l.add(OPTIONS.TRUSTALL);
        return l;
    }

    public static void main(String[] args) throws IOException {
        if ((args == null) || (args.length == 0)) {
            LOG.error("Missing one of the arguments: {} | {} | {}",
                    TextsProvider.JAVAWS, TextsProvider.ITWEB_SETTINGS, TextsProvider.POLICY_EDITOR);
            System.exit(0);
        }
        switch (args[0]) {
            case TextsProvider.JAVAWS:
                printOptions(getJavaWsOptions());
                break;
            case TextsProvider.ITWEB_SETTINGS:
                printOptions(getItwsettingsCommands());
                break;
            case TextsProvider.POLICY_EDITOR:
                printOptions(getPolicyEditorOptions());
                break;
            default:
                break;
        }
    }

    private static void printOptions(final List<OPTIONS> options) {
        if (options != null && !options.isEmpty()) {
            final StringBuilder sb = new StringBuilder();
            for (OPTIONS option : options) {
                sb.append(option.option).append(" ");
            }
            System.out.println(sb.toString().trim());
        }
    }
}
