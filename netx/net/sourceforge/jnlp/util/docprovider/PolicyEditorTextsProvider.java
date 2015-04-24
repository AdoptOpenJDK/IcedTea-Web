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
import net.sourceforge.jnlp.util.docprovider.formatters.formatters.Formatter;

/**
 *
 * @author jvanek
 */
public class PolicyEditorTextsProvider extends TextsProvider {

    public PolicyEditorTextsProvider(String encoding, Formatter formatter, boolean forceTitles, boolean expandFiles) {
        super(encoding, formatter, forceTitles, expandFiles);
    }

    @Override
    public String getId() {
        return POLICY_EDITOR;
    }

    @Override
    public String getIntroduction() {
        return super.getIntroduction()
                + getFormatter().wrapParagraph(
                        getFormatter().process(getId() + " "+Translator.R("PEintro")));
    }

    @Override
    public String getSynopsis() {
        return super.getSynopsis()
                + getFormatter().wrapParagraph(getFormatter().process(getFormatter().getBoldOpening() + " " + getId() + " " + 
                        getFormatter().getBoldCloseNwlineBoldOpen() + getId() + " [-file] " + getFormatter().getBoldClosing() + Translator.R("PEsynopseP1") + " "+getFormatter().getBold("[-codebase] ") + Translator.R("PEsynopseP2")));
    }

    @Override
    public String getDescription() {
        return super.getDescription()
                + getFormatter().wrapParagraph(getFormatter().process(getFormatter().getBold(getId() + " ")
                                + Translator.R("PEdescL1")
                                + getFormatter().getNewLine() + getFormatter().getNewLine()
                                + Translator.R("PEdescL2")));
    }

    @Override
    public String getOptions() {
        return super.getOptions()
                + getFormatter().wrapParagraph(optionsToString(OptionsDefinitions.getPolicyEditorOptions()));
    }

    @Override
    public String getExamples() {
        String title = super.getExamples();
        String s = "";
        if (expandVariables) {
            s = s + getFormatter().getOption(getId() + " -file " + PathsAndFiles.JAVA_POLICY.getFullPath(), Translator.R("PEexampleL1"));

        } else {
            s = s + getFormatter().getOption(getId() + " -file " + PathsAndFiles.JAVA_POLICY.toString(), Translator.R("PEexampleL1"));
        }
        return title + getFormatter().wrapParagraph(
                getFormatter().getOption(getId(), Translator.R("PEexampleL2")) + s);
    }

    @Override
    public String getFiles() {
        return super.getFiles() + getFiles(PathsAndFiles.getAllPEFiles()) + getFilesAppendix();

    }

    public static void main(String[] args) throws IOException {
        TextsProvider.main(new String[]{"all", "true", "3.51.a"});
    }

    @Override
    public String getCommands() {
        return "";
    }
}
