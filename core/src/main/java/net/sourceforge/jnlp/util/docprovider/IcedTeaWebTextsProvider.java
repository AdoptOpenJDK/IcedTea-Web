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
import net.sourceforge.jnlp.runtime.Translator;
import net.sourceforge.jnlp.util.docprovider.formatters.formatters.Formatter;

public class IcedTeaWebTextsProvider extends TextsProvider {

    public IcedTeaWebTextsProvider(String encoding, Formatter formatter, boolean forceTitles, boolean expandFiles) {
        super(encoding, formatter, forceTitles, expandFiles);
    }

    @Override
    public String getIntroduction() {
        return super.getIntroduction()
                + getFormatter().wrapParagraph(
                        Translator.R("ITWintroL1",getFormatter().getBold(getId() + " "))
                        + getFormatter().getNewLine(2)
                        + Translator.R("ITWintroL2")
                        + getFormatter().getNewLine()
                        + Translator.R("ITWintroL3", getId(), getFormatter().getUrl("http://www.java.com/en/download/testjava.jsp", Translator.R("ITWintroUrlCaption")))
                        + getFormatter().getNewLine()
                        + getFormatter().getOption("",Translator.R("BFileInfoAuthors"))
                        + getFormatter().getOption("",Translator.R("BFileInfoCopying"))
                        + getFormatter().getOption("",Translator.R("BFileInfoNews"))
                        + getFormatter().getNewLine(2));

    }

    @Override
    public String getSynopsis() {
        return "";
    }

    @Override
    public String getDescription() {
        StringBuilder p1 = new StringBuilder();
        //there is 7 pairs of keys ITWdescO X title/text. I was to lazy to enumerate them manually
        for (int x = 1; x <= 7; x++) {
            p1.append(getFormatter().getOption(Translator.R("ITWdescO" + x + "title"), Translator.R("ITWdescO" + x + "text")));
        }
        p1.append(getFormatter().getNewLine());

        StringBuilder p2 = new StringBuilder();

        p2.append(Translator.R("ITWdescL1",
                getFormatter().getUrl(IT_MAIN, Translator.R("ITWdescWikiUrlTitle")),
                getFormatter().getUrl(ITW_HOME, Translator.R("ITWdescItwWikiUrlTitle"))));
        p2.append(getFormatter().getNewLine());
        p2.append(Translator.R("ITWdescL2", getFormatter().getUrl(ITW_ISSUES, Translator.R("ITWdescIssuesUrlTitle"))));
        p2.append(getFormatter().getNewLine());

        String header = getFormatter().getBold(Translator.R("ITWdescL3") + " ") + getFormatter().getNewLine();
        return super.getDescription() + getFormatter().wrapParagraph(header) + getFormatter().wrapParagraph(p1.toString()) + getFormatter().getNewLine() + getFormatter().wrapParagraph(p2.toString());
    }

    @Override
    public String getOptions() {
        String l1 = Translator.R("ITWoptionsL1",getFormatter().getUrl(IT_QUICK,Translator.R("ITWoptionsQuickStartUrlCaption")));
        String l2 = Translator.R("ITWoptionsL2", 
                getFormatter().getUrl(ITW_STYLE, Translator.R("ITWoptionsCodeUrlUrlCaption")),
                getFormatter().getUrl(ITW_ECLIPSE, Translator.R("ITWoptionsEclipseUrlCaption")),
                 getFormatter().getUrl(ITW_REPRODUCERS,Translator.R( "ITWoptionsReproducersUrlCaption")),
                 getFormatter().getUrl(DISTRO_PKG, Translator.R("ITWoptionsDistroUrlCaption")));
        String header = getFormatter().getBold(Translator.R("ITWoptionsL3")) + getFormatter().getNewLine();
        return getFormatter().wrapParagraph(header) + getFormatter().wrapParagraph(l1) + getFormatter().wrapParagraph(l2);
    }

    @Override
    public String getFiles() {
        return "";

    }

    @Override
    public String getExamples() {
        return "";

    }
    
    @Override
    public String getId() {
        return ITW;
    }

    public static void main(String[] args) throws IOException {
        TextsProvider.main(new String[]{"all", "true", "3.51.a"});
    }

    @Override
    public String getCommands() {
        return "";
    }

}
