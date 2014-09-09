
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
                + getFormatter().wrapParagraph(getFormatter().process(getId() + " - a Java Web Start client"));
    }

    @Override
    public String getSynopsis() {
        return super.getSynopsis()
                + getFormatter().wrapParagraph(getFormatter().process("@BOLD_OPEN@ " + getId() + " @BOLD_CLOSE@" + R("BOUsage") + "@NWLINE_BOLD_OPEN@javaws @BOLD_CLOSE@" + R("BOUsage2")));
    }

    @Override
    public String getDescription() {
        return super.getDescription()
                + getFormatter().wrapParagraph(getFormatter().process(
                                "@BOLD_OPEN@ " + getId() + " @BOLD_CLOSE@"
                                + "is an implementation of a JNLP  client. It uses a JNLP (Java Network Launch Protocol) file to securely run a remote Java application or a Java applet.  This implementation of"
                                + "@BOLD_OPEN@ " + getId() + " @BOLD_CLOSE@" + "is from the IcedTea project and is based on the NetX project."
                                + "@NWLINE@@NWLINE@"
                                + "A JNLP file is an xml file that describes how to securely run a remote Java application or a Java applet."));
    }

    @Override
    public String getOptions() {
        String title = super.getOptions();
        String add1 = "When specifying options, the name of the jnlp file must be the last argument to javaws - all the options must preceede it.";
        String add2 = "The jnlp-file can either be a url or a local path.";
        String adds = getFormatter().wrapParagraph(add1 + getFormatter().getNewLine() + add2);
        String runtime = getFormatter().getBold("run-options:") + getFormatter().getNewLine()
                + optionsToString(OptionsDefinitions.getJavaWsRuntimeOptions());
        String control = getFormatter().getBold("control-options:") + getFormatter().getNewLine()
                + optionsToString(OptionsDefinitions.getJavaWsControlOptions());
        return title + adds + getFormatter().wrapParagraph(control) + getFormatter().wrapParagraph(runtime);
    }

    @Override
    public String getExamples() {
        return super.getExamples()
                + getFormatter().wrapParagraph(
                        getFormatter().getOption(getId() + " -about", " Shows basic help and about informations")
                        + getFormatter().getOption(getId() + " -about -headless", " Shows basic help and about informations in commandline")
                        + getFormatter().getOption(getId() + "  -Xnofork -Xignoreheaders -allowredirect -Xoffline http://mypage.web/dangerous.jnlp", " Will start dangerous.jnlp application, originally form mypage.web, without downloading it, without headers check and in forced single VM"));
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
