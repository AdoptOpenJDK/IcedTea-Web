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

package net.sourceforge.jnlp.util.docprovider;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sourceforge.jnlp.config.Defaults;
import net.sourceforge.jnlp.OptionsDefinitions;
import net.sourceforge.jnlp.config.InfrastructureFileDescriptor;
import net.sourceforge.jnlp.config.Setting;
import net.sourceforge.jnlp.runtime.Translator;
import net.sourceforge.jnlp.util.docprovider.formatters.formatters.Formatter;
import net.sourceforge.jnlp.util.docprovider.formatters.formatters.HtmlFormatter;
import net.sourceforge.jnlp.util.docprovider.formatters.formatters.ManFormatter;
import net.sourceforge.jnlp.util.docprovider.formatters.formatters.PlainTextFormatter;
import net.sourceforge.jnlp.util.docprovider.formatters.formatters.ReplacingTextFormatter;

public abstract class TextsProvider {

    private final String encoding;
    private final Formatter formatter;
    private final boolean forceTitles;
    protected final boolean expandVariables;
    private boolean prepared = false;

    private boolean introduction = true;
    private boolean synopsis = true;
    private boolean description = true;
    private boolean commands = true;
    private boolean options = true;
    private boolean examples = true;
    private boolean files = true;
    private boolean bugs = true;
    private boolean authors = true;
    private boolean seeAlso = true;

    public TextsProvider(String encoding, Formatter formatter, boolean forceTitles, boolean expandFiles) {
        this.encoding = encoding;
        this.formatter = formatter;
        this.forceTitles = forceTitles;
        this.expandVariables = expandFiles;
    }

    public abstract String getId();

    public String getHeader() {
        return getFormatter().getHeaders(getId(), getEncoding());
    }

    public String getTail() {
        return getFormatter().getTail();
    }

    public String getIntroduction() {
        if (forceTitles) {
            return getFormatter().getTitle(ManFormatter.KnownSections.NAME);
        } else {
            return "";
        }
    }

    public String getSynopsis() {
        if (forceTitles) {
            return getFormatter().getTitle(ManFormatter.KnownSections.SYNOPSIS);
        } else {
            return "";
        }
    }

    public String getDescription() {
        if (forceTitles) {
            return getFormatter().getTitle(ManFormatter.KnownSections.DESCRIPTION);
        } else {
            return "";
        }
    }

    public String getOptions() {
        if (forceTitles) {
            return getFormatter().getTitle(ManFormatter.KnownSections.OPTIONS);
        } else {
            return "";
        }
    }

    public String getCommands() {
        if (forceTitles) {
            return getFormatter().getTitle(ManFormatter.KnownSections.COMMANDS);
        } else {
            return "";
        }
    }

    public String getExamples() {
        if (forceTitles) {
            return getFormatter().getTitle(ManFormatter.KnownSections.EXAMPLES);
        } else {
            return "";
        }
    }

    public String getFiles() {
        if (forceTitles) {
            return getFormatter().getTitle(ManFormatter.KnownSections.FILES);
        } else {
            return "";
        }
    }

    protected String getFilesAppendix() {
        if (!expandVariables) {
            return getFormatter().wrapParagraph(Translator.R("ITWTBdirs"));
        } else {
            return "";
        }
    }

    protected String getFiles(List<InfrastructureFileDescriptor> files) {
        StringBuilder sb = new StringBuilder();
        Set<Map.Entry<String, Setting<String>>> defs = Defaults.getDefaults().entrySet();
        Collections.sort(files, new Comparator<InfrastructureFileDescriptor>() {

            @Override
            public int compare(InfrastructureFileDescriptor o1, InfrastructureFileDescriptor o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        for (InfrastructureFileDescriptor f : files) {
            String path = expandVariables ? f.getFullPath() : f.toString();
            String modified = "";
            String fGetFullPath=removeFileProtocol(f.getFullPath());
            String fGetDefaultFullPath=removeFileProtocol(f.getDefaultFullPath());
            if (!fGetFullPath.equals(fGetDefaultFullPath) && expandVariables){
                modified=getFormatter().getBold("["+Translator.R("BUTmodified")+"] ");
            }
            String controlledBy = "";
            for (Map.Entry<String, Setting<String>> entry : defs) {
                if (matchSttingsValueWithInfrastrucutreFile(entry.getValue(), f)) {
                    controlledBy = " " + Translator.R("BUTControlledBy", getFormatter().getBold(entry.getKey()));
                    
                    break;
                }
            }
            sb.append(getFormatter().getOption(path, modified+f.getDescription() + controlledBy));
        }
        return formatter.wrapParagraph(sb.toString());
    }

    protected boolean matchSttingsValueWithInfrastrucutreFile(Setting<String> entry, InfrastructureFileDescriptor f) {
        if (entry == null || entry.getDefaultValue() == null) {
            return false;
        }
        return entry.getDefaultValue().equals(f.getDefaultFullPath()) || entry.getDefaultValue().equals("file://" + f.getDefaultFullPath());

    }

    public Formatter getFormatter() {
        return formatter;
    }

    protected String getEncoding() {
        return encoding;
    }

    protected String optionsToString(List<OptionsDefinitions.OPTIONS> opts) {
        Collections.sort(opts, new Comparator<OptionsDefinitions.OPTIONS>() {

            @Override
            public int compare(OptionsDefinitions.OPTIONS o1, OptionsDefinitions.OPTIONS o2) {
                return o1.option.compareToIgnoreCase(o2.option);
            }
        });

        StringBuilder sb = new StringBuilder();
        for (OptionsDefinitions.OPTIONS o : opts) {
            sb.append(getFormatter().getOption(o.option + " " + o.helperString, o.getLocalizedDescription()+"("+o.getArgumentExplanation()+")"));

        }
        return sb.toString();
    }

    public static final String IT_BASE = "http://icedtea.classpath.org/wiki";
    public static final String ITW_HOME = IT_BASE + "/IcedTea-Web";
    public static final String ITW_EAS = IT_BASE + "/Extended_Applets_Security";
    public static final String ITW_STYLE = ITW_HOME + "#Code_style";
    public static final String ITW_ECLIPSE = ITW_HOME + "/DevelopingWithEclipse";
    public static final String ITW_REPO = "http://icedtea.classpath.org/hg/icedtea-web";

    public static final String JAVAWS = "javaws";
    public static final String ITWEB_SETTINGS = "itweb-settings";
    public static final String ITW = "icedtea-web";
    public static final String ITW_PLUGIN = "icedtea-web-plugin";
    public static final String POLICY_EDITOR = "policyeditor";

    public static final String DISTRO_PKG = "http://mail.openjdk.java.net/mailman/listinfo/distro-pkg-dev";

    public static final String IT_MAIN = IT_BASE + "/Main_Page";
    public static final String IT_QUICK = IT_BASE + "/DeveloperQuickStart";
    public static final String ITW_ISSUES = ITW_HOME + "#Common_Issues";
    public static final String ITW_REPRODUCERS = IT_BASE + "/Reproducers";
    public static final String ITW_BUGS = ITW_HOME + "#Filing_bugs";
    public static final String ITW_PLUGIN_URL = ITW_HOME + "#Plugin";
    public static final String ITW_BUGZILLAHOME = "http://icedtea.classpath.org/bugzilla";

    public String getBugs() {

        StringBuilder sb = new StringBuilder();
        sb.append(getFormatter().process(Translator.R("ITWTBbugs")+":"));
        sb.append(getFormatter().getNewLine());
        sb.append(getFormatter().getUrl(ITW_BUGS));
        sb.append(getFormatter().getNewLine());
        sb.append(getFormatter().getUrl(ITW_BUGZILLAHOME));
        sb.append(getFormatter().getNewLine());
        sb.append(getFormatter().getNewLine());
        sb.append(getFormatter().process(Translator.R("ITWTBdebug")));
        sb.append(getFormatter().getNewLine());
        if (forceTitles) {
            return getFormatter().getTitle(ManFormatter.KnownSections.BUGS) + getFormatter().wrapParagraph(sb.toString());
        } else {
            return getFormatter().wrapParagraph(sb.toString());
        }
    }

    public String getAuthors() {
        if (forceTitles) {
            return getFormatter().getTitle(ManFormatter.KnownSections.AUTHOR)
                    + getFormatter().wrapParagraph(
                            getFormatter().process(Translator.R("ITWTBdebug"))
                            + getFormatter().getNewLine());
        } else {
            return getFormatter().wrapParagraph(
                    getFormatter().process(Translator.R("ITWTBdebug"))
                    + getFormatter().getNewLine());
        }
    }

    public String getSeeAlso() {

        StringBuilder sb = new StringBuilder();
        sb.append(getFormatter().getSeeAlso(ITW));
        sb.append(getFormatter().getSeeAlso(JAVAWS));
        sb.append(getFormatter().getSeeAlso(ITW_PLUGIN));
        sb.append(getFormatter().getSeeAlso(ITWEB_SETTINGS));
        sb.append(getFormatter().getSeeAlso(POLICY_EDITOR));
        sb.append(getFormatter().getSeeAlso("policytool"));
        sb.append(getFormatter().getSeeAlso("java"));
        sb.append(getFormatter().getNewLine());
        sb.append(getFormatter().getUrl(ITW_HOME));
        sb.append(getFormatter().getNewLine());
        sb.append(getFormatter().getUrl(ITW_REPO));
        sb.append(getFormatter().getNewLine());
        if (forceTitles) {
            return getFormatter().getTitle(ManFormatter.KnownSections.SEE_ALSO) + getFormatter().wrapParagraph(sb.toString());
        } else {
            return getFormatter().wrapParagraph(sb.toString());
        }

    }

    public TextsProvider prepare() {
        if (!prepared) {
            writeToStringReal();
            prepared = true;
        }
        return this;
    }

    public String writeToString() {
        //first walktrhough builds index (html), set longest options(textt)
        return prepare().writeToStringReal();
    }

    private String writeToStringReal() {
        StringBuilder sb = new StringBuilder();
        sb.append(getHeader());
        sb.append(PlainTextFormatter.getLineSeparator());
        if (getFormatter() instanceof HtmlFormatter) {
            sb.append(((HtmlFormatter) getFormatter()).generateLogo());
            sb.append(PlainTextFormatter.getLineSeparator());
            sb.append(((HtmlFormatter) getFormatter()).generateIndex());
            sb.append(PlainTextFormatter.getLineSeparator());
        }
        if (isIntroduction()) {
            sb.append(getIntroduction());
            sb.append(PlainTextFormatter.getLineSeparator());
        }
        if (isSynopsis()) {
            sb.append(getSynopsis());
            sb.append(PlainTextFormatter.getLineSeparator());
        }
        if (isDescription()) {
            sb.append(getDescription());
            sb.append(PlainTextFormatter.getLineSeparator());
        }
        if (isCommands()) {
            sb.append(getCommands());
            sb.append(PlainTextFormatter.getLineSeparator());
        }
        if (isOptions()) {
            sb.append(getOptions());
            sb.append(PlainTextFormatter.getLineSeparator());
        }
        if (isExamples()) {
            sb.append(getExamples());
            sb.append(PlainTextFormatter.getLineSeparator());
        }
        if (isFiles()) {
            sb.append(getFiles());
            sb.append(PlainTextFormatter.getLineSeparator());
        }
        if (isBugs()) {
            sb.append(getBugs());
            sb.append(PlainTextFormatter.getLineSeparator());
        }
        if (isAuthors()) {
            sb.append(getAuthors());
            sb.append(PlainTextFormatter.getLineSeparator());
        }
        if (isSeeAlso()) {
            sb.append(getSeeAlso());
            sb.append(PlainTextFormatter.getLineSeparator());
        }
        sb.append(getTail());
        return sb.toString();
    }

    public void writeToWriter(Writer w) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(w)) {
            bw.write(writeToString());
        }

    }

    public void writeToStream(OutputStream os) throws IOException {
        try (OutputStreamWriter osw = new OutputStreamWriter(os, encoding)) {
            writeToWriter(osw);
        }
    }

    public void writeToFile(File f) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(f)) {
            writeToStream(fos);
        }

    }

    public void writeToDir(File dir) throws IOException {
        writeToFile(new File(dir, getId() + getFormatter().getFileSuffix()));

    }

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println(" * IcedTea-Web self documentation tool list of arguments *");
            System.out.println(" * arument version - last parameter of each command, is used when there is no internal versionknown *");
            System.out.println(" *                 - is mandatory, but not used if real version do exists *");
            System.out.println(" * argument expand - one before last argument, false/true - is used to not/expand variables *");
            System.out.println(" * -------------------- *");
            System.out.println("all expand version - will generate all reports in theirs defaults into current directory");
            System.out.println("html targetDir expand version - will generate html documentation to target dir");
            System.out.println("htmlIntro targetFile  expand version - will generate html intro page to specified file");
            System.out.println("man encoding targetDir expand version - will generate man documentation to target dir in desired encoding");
            System.out.println("plain targetDir maxLineWidth expand version - will generate plain text documentation to target dir in desired encoding");
            System.out.println("                                            - maxLineWidth is in <5," + Integer.MAX_VALUE + ">");
        } else {
            ReplacingTextFormatter.backupVersion = args[args.length - 1];
            boolean expand = Boolean.valueOf(args[args.length - 2]);
            switch (args[0]) {
                case "all":
                    generateAll(new File(System.getProperty("user.dir")), expand);
                    break;
                case "html":
                    generateOnlineHtmlHelp(new File(args[1]), expand);
                    break;
                case "htmlIntro":
                    generateItwIntro(new File(args[1]), expand);
                    break;
                case "man":
                    generateManText(args[1], new File(args[2]), expand);
                    break;
                case "plain":
                    generatePlainTextDocs(new File(args[1]), Integer.valueOf(args[2]), expand);
                    break;
                default:
                    System.out.println("unknown param");
                    main(new String[0]);

            }
        }
    }

    public static void generateItwIntro(File f, boolean expand) throws IOException {
        IcedTeaWebTextsProvider itw = new IcedTeaWebTextsProvider("utf-8", new HtmlFormatter(false, true, false), false, expand);
        itw.setSeeAlso(false);
        itw.writeToFile(f);
    }

    public static void generateAll(File f, boolean expand) throws IOException {
        generateOnlineHtmlHelp(f, expand);
        generateManText("UTF-8", f, expand);
        generatePlainTextDocs(f, 160, expand);

    }

    private static final String logo_name = "itw_logo.png";
    private static final String logo_url = "/net/sourceforge/jnlp/resources/" + logo_name;

    public static void generateRuntimeHtmlTexts(File f) throws IOException {
        generateHtmlTexts(f, false, true);
    }

    public static void generateOnlineHtmlHelp(File f, boolean expand) throws IOException {
        generateHtmlTexts(f, true, expand);
    }

    public static void generateHtmlTexts(File dir, boolean includeXmlHeader, boolean expand) throws IOException {
        generateHtmlTextsUtf8(dir, true, true, includeXmlHeader, true, expand);
    }

    public static void generateHtmlTextsUtf8(File dir, boolean allowContext, boolean allowLogo, boolean includeXmlHeader, boolean titles, boolean expand) throws IOException {
        generateHtmlTexts("UTF-8", dir, allowContext, allowLogo, includeXmlHeader, titles, expand);
    }

    public static void generateHtmlTexts(String encoding, File dir, boolean allowContext, boolean allowLogo, boolean includeXmlHeader, boolean titles, boolean expand) throws IOException {
        if (allowLogo) {
            File flogo = new File(dir, logo_name);
            try (InputStream is = TextsProvider.class.getResourceAsStream(logo_url);
                    OutputStream os = new FileOutputStream(flogo);) {
                copy(is, os);
                os.flush();
            }
        }
        JavaWsTextsProvider javaws = new JavaWsTextsProvider(encoding, new HtmlFormatter(allowContext, allowLogo, includeXmlHeader), titles, expand);
        javaws.writeToDir(dir);
        ItwebSettingsTextsProvider itws = new ItwebSettingsTextsProvider(encoding, new HtmlFormatter(allowContext, allowLogo, includeXmlHeader), titles, expand);
        itws.writeToDir(dir);
        PolicyEditorTextsProvider pe = new PolicyEditorTextsProvider(encoding, new HtmlFormatter(allowContext, allowLogo, includeXmlHeader), titles, expand);
        pe.writeToDir(dir);
        IcedTeaWebTextsProvider itw = new IcedTeaWebTextsProvider(encoding, new HtmlFormatter(allowContext, allowLogo, includeXmlHeader), titles, expand);
        itw.writeToDir(dir);
        ItwebPluginTextProvider pl = new ItwebPluginTextProvider(encoding, new HtmlFormatter(allowContext, allowLogo, includeXmlHeader), titles, expand);
        pl.writeToDir(dir);

    }

    public static void generateManText(String encoding, File dir, boolean expand) throws IOException {
        generateManText(encoding, dir, true, expand);
    }

    public static void generateManText(String encoding, File dir, boolean titles, boolean expand) throws IOException {
        JavaWsTextsProvider javaws = new JavaWsTextsProvider(encoding, new ManFormatter(), titles, expand);
        javaws.writeToDir(dir);
        ItwebSettingsTextsProvider itws = new ItwebSettingsTextsProvider(encoding, new ManFormatter(), titles, expand);
        itws.writeToDir(dir);
        PolicyEditorTextsProvider pe = new PolicyEditorTextsProvider(encoding, new ManFormatter(), titles, expand);
        pe.writeToDir(dir);
        IcedTeaWebTextsProvider itw = new IcedTeaWebTextsProvider(encoding, new ManFormatter(), titles, expand);
        itw.writeToDir(dir);
        ItwebPluginTextProvider pl = new ItwebPluginTextProvider(encoding, new ManFormatter(), titles, expand);
        pl.writeToDir(dir);

    }

    public static void generatePlainTextDocs(File dir, int lineWidth, boolean expand) throws IOException {
        generatePlainTextDocs("UTF-8", dir, PlainTextFormatter.DEFAULT_INDENT, lineWidth, true, expand);
    }

    public static void generatePlainTextDocs(String encoding, File dir, String indent, int lineWidth, boolean titles, boolean expand) throws IOException {
        JavaWsTextsProvider javaws = new JavaWsTextsProvider(encoding, new PlainTextFormatter(indent, lineWidth), titles, expand);
        javaws.writeToDir(dir);
        ItwebSettingsTextsProvider itws = new ItwebSettingsTextsProvider(encoding, new PlainTextFormatter(indent, lineWidth), titles, expand);
        itws.writeToDir(dir);
        PolicyEditorTextsProvider pe = new PolicyEditorTextsProvider(encoding, new PlainTextFormatter(indent, lineWidth), titles, expand);
        pe.writeToDir(dir);
        IcedTeaWebTextsProvider itw = new IcedTeaWebTextsProvider(encoding, new PlainTextFormatter(indent, lineWidth), titles, expand);
        itw.writeToDir(dir);
        ItwebPluginTextProvider pl = new ItwebPluginTextProvider(encoding, new PlainTextFormatter(indent, lineWidth), titles, expand);
        pl.writeToDir(dir);

    }

    private static final int BUF_SIZE = 0x1000; // 4K

    private static long copy(InputStream from, OutputStream to) throws IOException {
        byte[] buf = new byte[BUF_SIZE];
        long total = 0;
        while (true) {
            int r = from.read(buf);
            if (r == -1) {
                break;
            }
            to.write(buf, 0, r);
            total += r;
        }
        return total;
    }

    /**
     * @return the introduction
     */
    public boolean isIntroduction() {
        return introduction;
    }

    /**
     * @param introduction the introduction to set
     */
    public void setIntroduction(boolean introduction) {
        this.introduction = introduction;
    }

    /**
     * @return the synopsis
     */
    public boolean isSynopsis() {
        return synopsis;
    }

    /**
     * @param synopsis the synopsis to set
     */
    public void setSynopsis(boolean synopsis) {
        this.synopsis = synopsis;
    }

    /**
     * @return the description
     */
    public boolean isDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(boolean description) {
        this.description = description;
    }

    /**
     * @return the commands
     */
    public boolean isCommands() {
        return commands;
    }

    /**
     * @param commands the commands to set
     */
    public void setCommands(boolean commands) {
        this.commands = commands;
    }

    /**
     * @return the options
     */
    public boolean isOptions() {
        return options;
    }

    /**
     * @param options the options to set
     */
    public void setOptions(boolean options) {
        this.options = options;
    }

    /**
     * @return the examples
     */
    public boolean isExamples() {
        return examples;
    }

    /**
     * @param examples the examples to set
     */
    public void setExamples(boolean examples) {
        this.examples = examples;
    }

    /**
     * @return the files
     */
    public boolean isFiles() {
        return files;
    }

    /**
     * @param files the files to set
     */
    public void setFiles(boolean files) {
        this.files = files;
    }

    /**
     * @return the bugs
     */
    public boolean isBugs() {
        return bugs;
    }

    /**
     * @param bugs the bugs to set
     */
    public void setBugs(boolean bugs) {
        this.bugs = bugs;
    }

    /**
     * @return the authors
     */
    public boolean isAuthors() {
        return authors;
    }

    /**
     * @param authors the authors to set
     */
    public void setAuthors(boolean authors) {
        this.authors = authors;
    }

    /**
     * @return the seeAlso
     */
    public boolean isSeeAlso() {
        return seeAlso;
    }

    /**
     * @param seeAlso the seeAlso to set
     */
    public void setSeeAlso(boolean seeAlso) {
        this.seeAlso = seeAlso;
    }

    private String removeFileProtocol(String s) {
        if (s == null){
            return s;
        }
        if (s.startsWith("file://")){
            s = s.substring(7);
        }
        return s;
    }

}
