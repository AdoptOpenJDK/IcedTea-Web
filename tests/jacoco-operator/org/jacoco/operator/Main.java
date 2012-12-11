/*
Copyright (C) 2012 Red Hat, Inc.

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

package org.jacoco.operator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Commandline launcher
 */
public class Main {

    //main switches
    private static final String MERGE = "merge";
    private static final String REPORT = "report";
    //switches
    private static final String die_on_failure = "--die-soon";
    //merge
    private static final String output_file = "--output-file";
    private static final String input_files = "--input-files";
    //report
    private static final String html_output = "--html-output";
    private static final String xml_output = "--xml-output";
    private static final String input_srcs = "--input-srcs";
    private static final String input_builds = "--input-builds";
    private static final String title = "--title";
    private static String input_file = "--input-file";
    /**
     * *
     */
    private static boolean dieOnFailure = false;
    private static boolean warned = false;

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            printHelp();
            System.exit(0);
        }

        Runnable r = null;
        if (args[0].equalsIgnoreCase(MERGE)) {
            r = proceedMerge(cutFirstParam(args));
        } else if (args[0].equalsIgnoreCase(REPORT)) {
            r = proceedReport(cutFirstParam(args));
        } else {
            System.err.println("Unsuported main switch `" + args[0] + "`, use " + MERGE + " or " + REPORT);
            printHelp();
            System.exit(1);
        }
        if (dieOnFailure && warned) {
            System.err.println(die_on_failure + " is specified and warning occured. Exiting");
            System.exit(2);
        }
        r.run();

    }

    private static void printHelp() {
        System.out.println("Usage: java `classpath` org.jacoco.operator.Main [" + MERGE + "|" + REPORT + "] switches/files");
        System.out.println("    order of switches does not matter");
        System.out.println("  Merge usage: java `classpath` org.jacoco.operator.Main " + MERGE + " " + output_file + " file  " + input_files + " file file file ...");
        System.out.println("  Report usage: java `classpath` org.jacoco.operator.Main " + REPORT + " " + html_output + " file " + xml_output + " file  " + input_srcs + " file file file ... " + input_builds + " file file file " + title + " titleOfReport " + input_file + " file");
        System.out.println("Where:");
        System.out.println("  classpath should contain this application, and complete jacoco, and sometimes asm3 (depends on jacoco bundle)");
        System.out.println("    " + die_on_failure + " - can be set as first parameter (after main switch), each warning then will cause exit of application");
        System.out.println("  " + MERGE);
        System.out.println("    " + output_file + " - is file where merged inputs will be saved");
        System.out.println("    " + input_files + " - is list of files which will be merged into output file");
        System.out.println("  " + REPORT);
        System.out.println("    " + html_output + " - name of directory into which report will be generated. Should be empty or not yet exist");
        System.out.println("    " + xml_output + " - is name of file into which xml report will be written");
        System.out.println("    " + input_srcs + " - jars, zips or directories with java sources which will be used during report generation");
        System.out.println("    " + input_builds + " - jars, zips or directories with compiled java classes, debug information must be present");
        System.out.println("    " + title + " - title of report");
        System.out.println("    " + input_file + " - input file with recorded coverage-run-session. By default jacoco saves into " + MergeTask.DEFAULT_NAME);

    }

    private static String[] cutFirstParam(String[] args) {
        String[] arg = new String[args.length - 1];
        System.arraycopy(args, 1, arg, 0, arg.length);
        return arg;
    }

    private static Runnable proceedMerge(String[] a) throws IOException {
        String doing = null;
        String outputFile = null;
        List<String> inputFiles = new ArrayList<String>(2);
        for (String s : a) {
            if (s.startsWith("--")) {
                if (s.equalsIgnoreCase(die_on_failure)) {
                    doing = null;
                    dieOnFailure = true;
                } else if (s.equalsIgnoreCase(output_file)) {
                    doing = output_file;
                } else if (s.equalsIgnoreCase(input_files)) {
                    doing = input_files;
                } else {
                    warnOrDie("Unknown Switch for merge " + s);
                    doing = null;
                }
            } else {
                if (doing == null) {
                    warnOrDie("Missing switch during processing of " + s);
                } else {
                    if (doing.equalsIgnoreCase(output_file)) {
                        outputFile = s;
                    } else if (doing.equalsIgnoreCase(input_files)) {
                        inputFiles.add(s);
                    } else {
                        warnOrDie("Unknown processing of switch of" + doing);
                    }

                }
            }
        }
        throwIfNullOrEmpty(outputFile, "empty output file");
        File ff = new File(outputFile);
        if (ff.exists()) {
            warnOrDie("Warning, output file " + ff.getAbsolutePath() + " exists");
        }
        MergeTask m = new MergeTask(ff);
        for (String string : inputFiles) {
            if (checkIfNotNullOrEmpty(string)) {
                File f = new File(string);
                if (!f.exists()) {
                    warnOrDie("Warning, input coverage " + f.getAbsolutePath() + " does not exists!");
                }
                m.addInputFile(f);
            }
        }
        return m;

    }

    private static Runnable proceedReport(String[] a) throws IOException {
        String doing = null;
        String htmlDir = null;
        String xmlFile = null;
        List<String> inputSrcs = new ArrayList<String>(1);
        List<String> inputBuilds = new ArrayList<String>(1);
        String titleValue = null;
        String inputFile = null;
        for (String s : a) {
            if (s.startsWith("--")) {
                if (s.equalsIgnoreCase(die_on_failure)) {
                    doing = null;
                    dieOnFailure = true;
                } else if (s.equalsIgnoreCase(html_output)) {
                    doing = html_output;
                } else if (s.equalsIgnoreCase(xml_output)) {
                    doing = xml_output;
                } else if (s.equalsIgnoreCase(input_srcs)) {
                    doing = input_srcs;
                } else if (s.equalsIgnoreCase(input_builds)) {
                    doing = input_builds;
                } else if (s.equalsIgnoreCase(title)) {
                    doing = title;
                } else if (s.equalsIgnoreCase(input_file)) {
                    doing = input_file;
                } else {
                    warnOrDie("Unknown Switch for report " + s);
                    doing = null;
                }
            } else {
                if (doing == null) {
                    warnOrDie("Missing switch during processing of " + s);
                } else {
                    if (doing.equalsIgnoreCase(html_output)) {
                        htmlDir = s;
                    } else if (doing.equalsIgnoreCase(xml_output)) {
                        xmlFile = s;
                    } else if (doing.equalsIgnoreCase(input_srcs)) {
                        inputSrcs.add(s);
                    } else if (doing.equalsIgnoreCase(input_builds)) {
                        inputBuilds.add(s);
                    } else if (doing.equalsIgnoreCase(title)) {
                        titleValue = s;
                    } else if (doing.equalsIgnoreCase(input_file)) {
                        inputFile = s;
                    } else {
                        warnOrDie("Unknown processing of switch of " + doing);
                    }

                }
            }
        }
        File finalHtmlFile = null;
        if (checkIfNotNullOrEmpty(htmlDir)) {
            finalHtmlFile = new File(htmlDir);
            if (finalHtmlFile.exists()) {
                warnOrDie("Warning, direcotry for html report exists! " + finalHtmlFile.getAbsolutePath());
            }
        }
        File finalXmlFile = null;
        if (checkIfNotNullOrEmpty(xmlFile)) {
            finalXmlFile = new File(xmlFile);
            if (finalXmlFile.exists()) {
                warnOrDie("Warning, file for xml report exists! " + finalHtmlFile.getAbsolutePath());
            }
        }
        if (chckIfNUllOrEmpty(titleValue)) {
            titleValue = "Coverage report";
        }
        throwIfNullOrEmpty(inputFile, "No coverage data file specified!");
        File finalInputFile = new File(inputFile);

        ReportGenerator rg = new ReportGenerator(titleValue, finalInputFile, finalHtmlFile, finalXmlFile);

        for (String string : inputSrcs) {
            if (checkIfNotNullOrEmpty(string)) {
                File f = new File(string);
                if (!f.exists()) {
                    warnOrDie("Warning, input source " + f.getAbsolutePath() + " does not exists!");
                }
                rg.addSource(f);
            }
        }
        for (String string : inputBuilds) {
            if (checkIfNotNullOrEmpty(string)) {
                File f = new File(string);
                if (!f.exists()) {
                    warnOrDie("Warning, input build " + f.getAbsolutePath() + " does not exists!");
                }
                rg.addClasses(f);
            }
        }
        return rg;
    }

    private static String throwIfNullOrEmpty(String outputFile, String message) throws RuntimeException {
        if (chckIfNUllOrEmpty(outputFile)) {
            throw new RuntimeException(message);
        }
        return outputFile;
    }

    private static boolean checkIfNotNullOrEmpty(String string) {
        return string != null && string.trim().length() != 0;
    }

    private static boolean chckIfNUllOrEmpty(String outputFile) {
        return outputFile == null || outputFile.trim().length() == 0;
    }

    private static void warnOrDie(String string) {
        System.err.println(string);
        warned = true;

    }
}
