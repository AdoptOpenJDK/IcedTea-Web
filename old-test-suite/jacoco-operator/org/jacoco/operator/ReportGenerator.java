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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.MultiSourceFileLocator;
import org.jacoco.report.html.HTMLFormatter;
import org.jacoco.report.xml.XMLFormatter;

/**
 * This example creates a HTML report for eclipse like projects based on a
 * single execution data store called jacoco.exec. The report contains no
 * grouping information.
 *
 * The class files under test must be compiled with debug information, otherwise
 * source highlighting will not work.
 * 
 * Originally based on:
 * http://www.eclemma.org/jacoco/trunk/doc/examples/java/ReportGenerator.java
 */
public class ReportGenerator implements Runnable {

    private final String title;
    private final File executionDataFile;
    private final List<File> classesDirectories = new ArrayList<File>(1);
    private final List<File> sourceDirectories = new ArrayList<File>(1);
    private File reportDirectory;
    private File xmlOutput;
    private ExecutionDataStore executionDataStore;
    private SessionInfoStore sessionInfoStore;
    private String XML_DEF_NAME = "coverage-summary.xml";

    /**
     * Create a new generator based for the given project.
     *
     * @param projectDirectory
     */
    public ReportGenerator(final File projectDirectory) {
        this.title = projectDirectory.getName();
        this.executionDataFile = new File(projectDirectory, MergeTask.DEFAULT_NAME);
        this.classesDirectories.add(new File(projectDirectory, "bin"));
        this.sourceDirectories.add(new File(projectDirectory, "src"));
        this.reportDirectory = new File(projectDirectory, "coveragereport");
        this.xmlOutput = new File(projectDirectory, XML_DEF_NAME);
    }

    public ReportGenerator(String title, File exec, File classes, File sources, File htmlReport, File xmlReport) {
        this.title = title;
        this.executionDataFile = exec;
        if (classes != null) {
            this.classesDirectories.add(classes);
        }
        if (sources != null) {
            this.sourceDirectories.add(sources);
        }
        this.reportDirectory = htmlReport;

        this.xmlOutput = xmlReport;
    }

    public ReportGenerator(String title, File exec, List<File> classes, List<File> sources, File htmlReport, File xmlReport) {
        this.title = title;
        this.executionDataFile = exec;
        if (classes != null) {
            this.classesDirectories.addAll(classes);
        }
        if (sources != null) {
            this.sourceDirectories.addAll(sources);
        }
        this.reportDirectory = htmlReport;
        this.xmlOutput = xmlReport;
    }

    public ReportGenerator(String title, File exec, List<File> classes, List<File> sources, File report) {
        this.title = title;
        this.executionDataFile = exec;
        if (classes != null) {
            this.classesDirectories.addAll(classes);
        }
        if (sources != null) {
            this.sourceDirectories.addAll(sources);
        }
        this.reportDirectory = report;
        this.xmlOutput = new File(report, XML_DEF_NAME);
    }

    public ReportGenerator(String title, File exec, File htmlReport, File xmlReport) {
        this.title = title;
        this.executionDataFile = exec;
        this.reportDirectory = htmlReport;
        this.xmlOutput = xmlReport;
    }

    public ReportGenerator(String title, File exec, File report) {
        this.title = title;
        this.executionDataFile = exec;
        this.reportDirectory = report;
        this.xmlOutput = new File(report, XML_DEF_NAME);
    }

    public void addSource(File f) {
        sourceDirectories.add(f);

    }

    public void addClasses(File f) {
        classesDirectories.add(f);

    }

    /**
     * Create the report.
     *
     * @throws IOException
     */
    public void execute() throws IOException {

        // Read the jacoco.exec file. Multiple data stores could be merged
        // at this point
        loadExecutionData();

        // Run the structure analyzer on a single class folder to build up
        // the coverage model. The process would be similar if your classes
        // were in a jar file. Typically you would create a bundle for each
        // class folder and each jar you want in your report. If you have
        // more than one bundle you will need to add a grouping node to your
        // report
        final IBundleCoverage bundleCoverage = analyzeStructure();

        if (reportDirectory != null) {
            createHtmlReport(bundleCoverage);
        }
        if (xmlOutput != null) {
            createXmlReport(bundleCoverage);
        }

    }

    private void createHtmlReport(final IBundleCoverage bundleCoverage)
            throws IOException {

        // Create a concrete report visitor based on some supplied
        // configuration. In this case we use the defaults
        final HTMLFormatter htmlFormatter = new HTMLFormatter();
        final IReportVisitor visitor = htmlFormatter.createVisitor(new FileMultiReportOutput(reportDirectory));

        // Initialize the report with all of the execution and session
        // information. At this point the report doesn't know about the
        // structure of the report being created
        visitor.visitInfo(sessionInfoStore.getInfos(),
                executionDataStore.getContents());

        // Populate the report structure with the bundle coverage information.
        // Call visitGroup if you need groups in your report.
        MultiSourceFileLocator msf = new MultiSourceFileLocator(4);
        for (File file : sourceDirectories) {
            msf.add(new DirectorySourceFileLocator(
                    file, "utf-8", 4));
        }

        visitor.visitBundle(bundleCoverage, msf);

        // Signal end of structure information to allow report to write all
        // information out
        visitor.visitEnd();

    }

    private void createXmlReport(final IBundleCoverage bundleCoverage)
            throws IOException {

        OutputStream fos = new FileOutputStream(xmlOutput);
        try {
            // Create a concrete report visitor based on some supplied
            // configuration. In this case we use the defaults
            final XMLFormatter htmlFormatter = new XMLFormatter();
            final IReportVisitor visitor = htmlFormatter.createVisitor(fos);

            // Initialize the report with all of the execution and session
            // information. At this point the report doesn't know about the
            // structure of the report being created
            visitor.visitInfo(sessionInfoStore.getInfos(),
                    executionDataStore.getContents());

            // Populate the report structure with the bundle coverage information.
            // Call visitGroup if you need groups in your report.
            visitor.visitBundle(bundleCoverage, null);


            // Signal end of structure information to allow report to write all
            // information out
            visitor.visitEnd();
        } finally {
            if (fos != null) {
                fos.close();
            }
        }

    }

    private void loadExecutionData() throws IOException {
        final FileInputStream fis = new FileInputStream(executionDataFile);
        try {
            final ExecutionDataReader executionDataReader = new ExecutionDataReader(
                    fis);
            executionDataStore = new ExecutionDataStore();
            sessionInfoStore = new SessionInfoStore();

            executionDataReader.setExecutionDataVisitor(executionDataStore);
            executionDataReader.setSessionInfoVisitor(sessionInfoStore);

            while (executionDataReader.read()) {
            }
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
    }

    private IBundleCoverage analyzeStructure() throws IOException {
        final CoverageBuilder coverageBuilder = new CoverageBuilder();
        final Analyzer analyzer = new Analyzer(executionDataStore,
                coverageBuilder);
        for (File file : classesDirectories) {
            analyzer.analyzeAll(file);

        }

        return coverageBuilder.getBundle(title);
    }

    @Override
    public void run() {
        try {
            execute();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
