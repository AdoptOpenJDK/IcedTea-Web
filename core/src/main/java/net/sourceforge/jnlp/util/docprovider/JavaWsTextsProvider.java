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

import java.io.IOException;
import net.sourceforge.jnlp.OptionsDefinitions;
import net.sourceforge.jnlp.config.PathsAndFiles;
import net.sourceforge.jnlp.runtime.Translator;
import static net.sourceforge.jnlp.runtime.Translator.R;
import net.sourceforge.jnlp.util.docprovider.formatters.formatters.Formatter;

public class JavaWsTextsProvider extends TextsProvider {

    public JavaWsTextsProvider(String encoding, Formatter formatter, boolean forceTitles, boolean expandFiles) {
        super(encoding, formatter, forceTitles, expandFiles);
    }

    @Override
    public String getId() {
        return JAVAWS;
    }

    @Override
    public String getIntroduction() {
        return super.getIntroduction()
                + getFormatter().wrapParagraph(getFormatter().process(getId() + " " + Translator.R("JWSintro")));
    }

    @Override
    public String getSynopsis() {
        return super.getSynopsis()
                + getFormatter().wrapParagraph(getFormatter().process(getFormatter().getBold(" " + getId() + " ") + R("BOUsage") + getFormatter().getBreakAndBold() + getId() + " " + getFormatter().getBoldClosing() + R("BOUsage2")));
    }

    @Override
    public String getDescription() {
        return super.getDescription()
                + getFormatter().wrapParagraph(getFormatter().process(
                                getFormatter().getBold(getId() + " ")
                                + Translator.R("JWSdescL1", getFormatter().getBold(getId()+" "))
                                + getFormatter().getNewLine()+ getFormatter().getNewLine()
                                + Translator.R("JWSdescL2")));
    }

    @Override
    public String getOptions() {
        String title = super.getOptions();
        String add1 = Translator.R("JWSoptionsL1");
        String add2 = Translator.R("JWSoptionsL2");
        String add3 = Translator.R("JWSoptionsL3");
        String adds = getFormatter().wrapParagraph(add1 + getFormatter().getNewLine() + add2 + getFormatter().getNewLine() + add3);
        String runtime = getFormatter().getBold(Translator.R("JWSoptionsTitle1")) + getFormatter().getNewLine()
                + optionsToString(OptionsDefinitions.getJavaWsRuntimeOptions());
        String control = getFormatter().getBold(Translator.R("JWSoptionsTitle2")) + getFormatter().getNewLine()
                + optionsToString(OptionsDefinitions.getJavaWsControlOptions());
        return title + adds + getFormatter().wrapParagraph(control) + getFormatter().wrapParagraph(runtime);
    }

    @Override
    public String getExamples() {
        return super.getExamples()
                + getFormatter().wrapParagraph(
                        getFormatter().getOption(getId() + " "
                                + OptionsDefinitions.OPTIONS.ABOUT.option, Translator.R("JWSexampleL1"))
                        + getFormatter().getOption(getId() + " "
                                + OptionsDefinitions.OPTIONS.ABOUT.option + " "
                                + OptionsDefinitions.OPTIONS.HEADLESS.option, Translator.R("JWSexampleL2"))
                        + getFormatter().getOption(getId() + "  "
                                + OptionsDefinitions.OPTIONS.NOFORK.option + " "
                                + OptionsDefinitions.OPTIONS.NOHEADERS.option + " "
                                + OptionsDefinitions.OPTIONS.REDIRECT.option + " "
                                + OptionsDefinitions.OPTIONS.OFFLINE.option + " http://mypage.web/dangerous.jnlp", Translator.R("JWSexampleL3", "dangerous.jnlp", "mypage.web")));
    }

    @Override
    public String getFiles() {
        String s = super.getFiles() + getFiles(PathsAndFiles.getAllJavaWsFiles());
        s = s + getFilesAppendix();
        return s;

    }

    public static void main(String[] args) throws IOException {
        TextsProvider.main(new String[]{"all", "true", "3.51.a"});
    }

    @Override
    public String getCommands() {
        return "";
    }

}
