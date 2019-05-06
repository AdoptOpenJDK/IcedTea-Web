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

import net.adoptopenjdk.icedteaweb.commandline.CommandLineOptions;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.sourceforge.jnlp.config.Defaults;
import net.sourceforge.jnlp.config.InfrastructureFileDescriptor;
import net.sourceforge.jnlp.config.Setting;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.docprovider.formatters.formatters.Formatter;
import net.sourceforge.jnlp.util.docprovider.formatters.formatters.HtmlFormatter;
import net.sourceforge.jnlp.util.docprovider.formatters.formatters.ManFormatter;
import net.sourceforge.jnlp.util.docprovider.formatters.formatters.PlainTextFormatter;
import net.sourceforge.jnlp.util.docprovider.formatters.formatters.ReplacingTextFormatter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.adoptopenjdk.icedteaweb.IcedTeaWebConstants.JAVAWS;
import static net.adoptopenjdk.icedteaweb.JvmPropertyConstants.USER_DIR;

public abstract class TextsProvider {

    private static String authorsLine = null;

    private final Charset encoding;
    private final Formatter formatter;
    private final boolean forceTitles;
    protected final boolean expandVariables;
    private boolean prepared = false;
    private String authorLineImpl = null;

    private boolean seeAlso = true;

    public TextsProvider(Charset encoding, Formatter formatter, boolean forceTitles, boolean expandFiles) {
        this.encoding = encoding;
        this.formatter = formatter;
        this.forceTitles = forceTitles;
        this.expandVariables = expandFiles;
    }

    public abstract String getId();

    private String getHeader() {
        return getFormatter().getHeaders(getId(), getEncoding());
    }

    private String getTail() {
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
        files.sort((o1, o2) -> o1.toString().compareTo(o2.toString()));
        for (InfrastructureFileDescriptor f : files) {
            String path = expandVariables ? f.getFullPath() : f.toString();
            String modified = "";
            String fGetFullPath = removeFileProtocol(f.getFullPath());
            String fGetDefaultFullPath = removeFileProtocol(f.getDefaultFullPath());
            if (!fGetFullPath.equals(fGetDefaultFullPath) && expandVariables) {
                modified = getFormatter().getBold("[" + Translator.R("BUTmodified") + "] ");
            }
            String controlledBy = "";
            for (Map.Entry<String, Setting<String>> entry : defs) {
                if (matchStringsValueWithInfrastructureFile(entry.getValue(), f)) {
                    controlledBy = " " + Translator.R("BUTControlledBy", getFormatter().getBold(entry.getKey()));
                    break;
                }
            }
            sb.append(getFormatter().getOption(path, modified + f.getDescription() + controlledBy));
        }
        return formatter.wrapParagraph(sb.toString());
    }

    protected boolean matchStringsValueWithInfrastructureFile(Setting<String> entry, InfrastructureFileDescriptor f) {
        if (entry == null || entry.getDefaultValue() == null) {
            return false;
        }
        return entry.getDefaultValue().equals(f.getDefaultFullPath()) || entry.getDefaultValue().equals("file://" + f.getDefaultFullPath());

    }

    public Formatter getFormatter() {
        return formatter;
    }

    private Charset getEncoding() {
        return encoding;
    }

    protected String optionsToString(List<CommandLineOptions> opts) {
        opts.sort((o1, o2) -> o1.getOption().compareToIgnoreCase(o2.getOption()));

        StringBuilder sb = new StringBuilder();
        for (CommandLineOptions o : opts) {
            sb.append(getFormatter().getOption(o.getOption() + " " + o.getHelperString(), o.getLocalizedDescription() + "(" + o.getArgumentExplanation() + ")"));

        }
        return sb.toString();
    }

    private static final String IT_NEW_HOME = "https://github.com/AdoptOpenJDK/icedtea-web";
    private static final String IT_BASE = "http://icedtea.classpath.org/wiki";
    public static final String ITW_HOME = IT_BASE + "/IcedTea-Web";
    public static final String ITW_EAS = IT_BASE + "/Extended_Applets_Security";
    public static final String ITW_STYLE = ITW_HOME + "#Code_style";
    public static final String ITW_ECLIPSE = ITW_HOME + "/DevelopingWithEclipse";
    private static final String ITW_REPO = "https://github.com/AdoptOpenJDK/icedtea-web.git";

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
    public static final String ITW_BUGZILLAHOME = "https://github.com/AdoptOpenJDK/icedtea-web/issues";

    private String getBugs() {

        StringBuilder sb = new StringBuilder();
        sb.append(getFormatter().process(Translator.R("ITWTBbugs") + ":"));
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

    private String getAuthors() {
        if (forceTitles) {
            return getFormatter().getTitle(ManFormatter.KnownSections.AUTHOR)
                    + generateAuthorsSection(authorLineImpl);
        } else {
            return generateAuthorsSection(authorLineImpl);
        }
    }

    private String getSeeAlso() {

        StringBuilder sb = new StringBuilder();
        sb.append(getFormatter().getSeeAlso(ITW));
        sb.append(getFormatter().getSeeAlso(JAVAWS));
        sb.append(getFormatter().getSeeAlso(ITW_PLUGIN));
        sb.append(getFormatter().getSeeAlso(ITWEB_SETTINGS));
        sb.append(getFormatter().getSeeAlso(POLICY_EDITOR));
        sb.append(getFormatter().getSeeAlso("policytool"));
        sb.append(getFormatter().getSeeAlso("java"));
        sb.append(getFormatter().getNewLine());
        sb.append(getFormatter().getUrl(IT_NEW_HOME));
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
        //first walk through builds index (html), set longest options(textt)
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
        sb.append(getIntroduction());
        sb.append(PlainTextFormatter.getLineSeparator());

        sb.append(getSynopsis());
        sb.append(PlainTextFormatter.getLineSeparator());

        sb.append(getDescription());
        sb.append(PlainTextFormatter.getLineSeparator());

        sb.append(getCommands());
        sb.append(PlainTextFormatter.getLineSeparator());

        sb.append(getOptions());
        sb.append(PlainTextFormatter.getLineSeparator());

        sb.append(getExamples());
        sb.append(PlainTextFormatter.getLineSeparator());

        sb.append(getFiles());
        sb.append(PlainTextFormatter.getLineSeparator());

        sb.append(getBugs());
        sb.append(PlainTextFormatter.getLineSeparator());

        sb.append(getAuthors());
        sb.append(PlainTextFormatter.getLineSeparator());

        if (isSeeAlso()) {
            sb.append(getSeeAlso());
            sb.append(PlainTextFormatter.getLineSeparator());
        }
        sb.append(getTail());
        return sb.toString();
    }

    private void writeToWriter(Writer w) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(w)) {
            bw.write(writeToString());
        }

    }

    private void writeToStream(OutputStream os) throws IOException {
        try (OutputStreamWriter osw = new OutputStreamWriter(os, encoding)) {
            writeToWriter(osw);
        }
    }

    public void writeToFile(File f) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(f)) {
            writeToStream(fos);
        }

    }

    private void writeToDir(File dir) throws IOException {
        writeToFile(new File(dir, getId() + getFormatter().getFileSuffix()));

    }

    public static void main(String[] args) throws IOException {
        // Shutdown hook from OutputController was causing hanging build on Windows. It's not used on headless.
        JNLPRuntime.setHeadless(true);

        if (args.length == 0) {
            System.out.println(" * IcedTea-Web self documentation tool list of arguments *");
            System.out.println(" * argument version - last parameter of each command, is used when there is no internal versionknown *");
            System.out.println(" *                  - is mandatory, but not used if real version do exists *");
            System.out.println(" * argument expand - one before last argument, false/true - is used to not/expand variables *");
            System.out.println(" * -------------------- *");
            System.out.println("all expand version - will generate all reports in theirs defaults into current directory");
            System.out.println("html targetDir expand version - will generate html documentation to target dir");
            System.out.println("htmlIntro targetFile  expand version - will generate html intro page to specified file");
            System.out.println("man encoding targetDir expand version - will generate man documentation to target dir in desired encoding");
            System.out.println("plain targetDir maxLineWidth expand version - will generate plain text documentation to target dir in desired encoding");
            System.out.println("                                            - maxLineWidth is in <5," + Integer.MAX_VALUE + ">");
            System.out.println("to generate informations about authors from a file, use argument '-authorFile' with path to AUTHORS file located in icedtea-web."
                    + "\n eg. -authorFile=/home/user/icedtea-web/AUTHORS");
        } else {
            List<String> argsList = new ArrayList<>(Arrays.asList(args));
            for (String s : argsList) {
                if (s.startsWith("-authorString=")) {
                    authorsLine = s.split("=")[1];
                    argsList.remove(s);
                    break;
                }
            }
            ReplacingTextFormatter.backupVersion = argsList.get(argsList.size() - 1);
            boolean expand = Boolean.valueOf(argsList.get(argsList.size() - 2));
            switch (argsList.get(0)) {
                case "all":
                    generateAll(new File(System.getProperty(USER_DIR)), expand);
                    break;
                case "html":
                    generateOnlineHtmlHelp(new File(argsList.get(1)), expand);
                    break;
                case "htmlIntro":
                    generateItwIntro(new File(argsList.get(1)), expand);
                    break;
                case "man":
                    generateManText(Charset.forName(argsList.get(1)), new File(argsList.get(2)), expand);
                    break;
                case "plain":
                    generatePlainTextDocs(new File(argsList.get(1)), Integer.valueOf(argsList.get(2)), expand);
                    break;
                default:
                    System.out.println("unknown param");
                    main(new String[0]);

            }
        }
    }

    private static void generateItwIntro(File f, boolean expand) throws IOException {
        IcedTeaWebTextsProvider itw = new IcedTeaWebTextsProvider(UTF_8, new HtmlFormatter(false, true, false), false, expand);
        //!!AUTHORS FILE IS NOT NEEDED IN THIS METHOD, AUTHORS ARE GENERATED SEPARATELY INTO ANOTHER TAB
        itw.setSeeAlso(false);
        itw.writeToFile(f);
    }

    private static void generateAll(File f, boolean expand) throws IOException {
        generateOnlineHtmlHelp(f, expand);
        generateManText(UTF_8, f, expand);
        generatePlainTextDocs(f, 160, expand);

    }

    private static final String logo_name = "itw_logo.png";
    private static final String logo_url = "/net/sourceforge/jnlp/resources/" + logo_name;

    public static void generateRuntimeHtmlTexts(File f) throws IOException {
        generateHtmlTexts(f, false, true);
    }

    private static void generateOnlineHtmlHelp(File f, boolean expand) throws IOException {
        generateHtmlTexts(f, true, expand);
    }

    private static void generateHtmlTexts(File dir, boolean includeXmlHeader, boolean expand) throws IOException {
        generateHtmlTextsUtf8(dir, includeXmlHeader, expand);
    }

    private static void generateHtmlTextsUtf8(File dir, boolean includeXmlHeader, boolean expand) throws IOException {
        generateHtmlTexts(dir, true, true, includeXmlHeader, true, expand);
    }

    private static void generateHtmlTexts(File dir, boolean allowContext, boolean allowLogo, boolean includeXmlHeader, boolean titles, boolean expand) throws IOException {
        if (allowLogo) {
            File flogo = new File(dir, logo_name);
            try (InputStream is = TextsProvider.class.getResourceAsStream(logo_url);
                    OutputStream os = new FileOutputStream(flogo)) {
                copy(is, os);
                os.flush();
            }
        }
        JavaWsTextsProvider javaws = new JavaWsTextsProvider(UTF_8, new HtmlFormatter(allowContext, allowLogo, includeXmlHeader), titles, expand);
        ItwebSettingsTextsProvider itws = new ItwebSettingsTextsProvider(UTF_8, new HtmlFormatter(allowContext, allowLogo, includeXmlHeader), titles, expand);
        PolicyEditorTextsProvider pe = new PolicyEditorTextsProvider(UTF_8, new HtmlFormatter(allowContext, allowLogo, includeXmlHeader), titles, expand);
        IcedTeaWebTextsProvider itw = new IcedTeaWebTextsProvider(UTF_8, new HtmlFormatter(allowContext, allowLogo, includeXmlHeader), titles, expand);
        ItwebPluginTextProvider pl = new ItwebPluginTextProvider(UTF_8, new HtmlFormatter(allowContext, allowLogo, includeXmlHeader), titles, expand);
        TextsProvider[] providers = new TextsProvider[]{javaws, itws, pe, itw, pl};
        for (TextsProvider provider : providers) {
            provider.setAuthorFilePath(authorsLine);
            provider.writeToDir(dir);
        }

    }

    private static void generateManText(Charset encoding, File dir, boolean expand) throws IOException {
        generateManText(encoding, dir, true, expand);
    }

    private static void generateManText(Charset encoding, File dir, boolean titles, boolean expand) throws IOException {
        JavaWsTextsProvider javaws = new JavaWsTextsProvider(encoding, new ManFormatter(), titles, expand);
        ItwebSettingsTextsProvider itws = new ItwebSettingsTextsProvider(encoding, new ManFormatter(), titles, expand);
        PolicyEditorTextsProvider pe = new PolicyEditorTextsProvider(encoding, new ManFormatter(), titles, expand);
        IcedTeaWebTextsProvider itw = new IcedTeaWebTextsProvider(encoding, new ManFormatter(), titles, expand);
        ItwebPluginTextProvider pl = new ItwebPluginTextProvider(encoding, new ManFormatter(), titles, expand);
        TextsProvider[] providers = new TextsProvider[]{javaws, itws, pe, itw, pl};
        for (TextsProvider provider : providers) {
            provider.setAuthorFilePath(authorsLine);
            provider.writeToDir(dir);
        }

    }

    private static void generatePlainTextDocs(File dir, int lineWidth, boolean expand) throws IOException {
        generatePlainTextDocs(UTF_8, dir, lineWidth, expand);
    }

    private static void generatePlainTextDocs(Charset encoding, File dir, int lineWidth, boolean expand) throws IOException {
        JavaWsTextsProvider javaws = new JavaWsTextsProvider(encoding, new PlainTextFormatter(PlainTextFormatter.DEFAULT_INDENT, lineWidth), true, expand);
        ItwebSettingsTextsProvider itws = new ItwebSettingsTextsProvider(encoding, new PlainTextFormatter(PlainTextFormatter.DEFAULT_INDENT, lineWidth), true, expand);
        PolicyEditorTextsProvider pe = new PolicyEditorTextsProvider(encoding, new PlainTextFormatter(PlainTextFormatter.DEFAULT_INDENT, lineWidth), true, expand);
        IcedTeaWebTextsProvider itw = new IcedTeaWebTextsProvider(encoding, new PlainTextFormatter(PlainTextFormatter.DEFAULT_INDENT, lineWidth), true, expand);
        ItwebPluginTextProvider pl = new ItwebPluginTextProvider(encoding, new PlainTextFormatter(PlainTextFormatter.DEFAULT_INDENT, lineWidth), true, expand);
        TextsProvider[] providers = new TextsProvider[]{javaws, itws, pe, itw, pl};
        for (TextsProvider provider : providers) {
            provider.setAuthorFilePath(authorsLine);
            provider.writeToDir(dir);
        }

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

    private void setAuthorFilePath(String authorFilePath) {
        this.authorLineImpl = authorFilePath;
    }

    /**
     * @return the seeAlso
     */
    private boolean isSeeAlso() {
        return seeAlso;
    }

    /**
     * @param seeAlso the seeAlso to set
     */
    public void setSeeAlso(boolean seeAlso) {
        this.seeAlso = seeAlso;
    }

    private String removeFileProtocol(String s) {
        if (s == null) {
            return s;
        }
        if (s.startsWith("file://")) {
            s = s.substring(7);
        }
        return s;
    }

    String readAuthors(String authors) {
        try {
            return readAuthorsImpl(authors);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    String readAuthorsImpl(String authors) throws IOException {
        String[] toMark = authors.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String word : toMark) {
            if (getFormatter() instanceof HtmlFormatter) {
                word = word.replaceAll("\n", getFormatter().getNewLine());
            }
            if (word.contains("@")) {
                sb.append(getFormatter().process(getFormatter().getAddressLink(word))).append(" ");
            } else if (word.contains("://")) {
                sb.append(getFormatter().process(getFormatter().getUrl(word))).append(" ");
            } else if (word.equals("")) {
                sb.append(" ");
            } else {
                sb.append(word).append(" ");
            }

        }
        return sb.toString().substring(0, sb.length()-1);
    }

    private String generateAuthorsSection(String authors) {
        if (authors == null || authors.isEmpty()) {
            return getFormatter().wrapParagraph(
                    getFormatter().process(Translator.R("ITWdocsMissingAuthors"))
                    + getFormatter().getNewLine());
        } else {
            return getFormatter().wrapParagraph(
                    getFormatter().process(readAuthors(authors))
                    + getFormatter().getNewLine());
        }
    }
}
