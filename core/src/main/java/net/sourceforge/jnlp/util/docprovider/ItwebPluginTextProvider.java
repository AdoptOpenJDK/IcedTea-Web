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
import net.sourceforge.jnlp.config.PathsAndFiles;
import net.sourceforge.jnlp.runtime.Translator;
import net.sourceforge.jnlp.util.docprovider.formatters.formatters.Formatter;

/**
 *
 * @author jvanek
 */
public class ItwebPluginTextProvider extends TextsProvider {

    public ItwebPluginTextProvider(String encoding, Formatter formatter, boolean forceTitles, boolean expandFiles) {
        super(encoding, formatter, forceTitles, expandFiles);
    }

    @Override
    public String getId() {
        return ITW_PLUGIN;
    }

    @Override
    public String getIntroduction() {
        return super.getIntroduction()
                + getFormatter().wrapParagraph(
                        getFormatter().process(getId() + " " + Translator.R("ITWPintro")));
    }

    @Override
    public String getSynopsis() {
        //yes, thsi really returns arch of jdk. So just nice nicly looking, mostly ok workaround.
        //fix needs native code
        String jdkArch = System.getProperty("os.arch") + "x";

        String mozillas;
        if (expandVariables) {
            mozillas = getFormatter().getOption(PathsAndFiles.MOZILA_USER.getFile().getParent(), PathsAndFiles.MOZILA_USER.getDescription());
        } else {
            mozillas = getFormatter().getOption(PathsAndFiles.MOZILA_USER.getDirViaAcronym(), PathsAndFiles.MOZILA_USER.getDescription());
        }
        if (!expandVariables || jdkArch.endsWith("64")) {
            if (expandVariables) {
                mozillas += getFormatter().getOption(PathsAndFiles.MOZILA_GLOBAL_64.getDirViaAcronym(), PathsAndFiles.MOZILA_GLOBAL_64.getDescription());
            } else {
                mozillas += getFormatter().getOption(PathsAndFiles.MOZILA_GLOBAL_64.getFile().getParent(), PathsAndFiles.MOZILA_GLOBAL_64.getDescription());
            }
        }
        if (!expandVariables || !jdkArch.endsWith("64")) {
            if (expandVariables) {
                mozillas += getFormatter().getOption(PathsAndFiles.MOZILA_GLOBAL_32.getDirViaAcronym(), PathsAndFiles.MOZILA_GLOBAL_32.getDescription());
            } else {
                mozillas += getFormatter().getOption(PathsAndFiles.MOZILA_GLOBAL_32.getFile().getParent(), PathsAndFiles.MOZILA_GLOBAL_32.getDescription());
            }
        }

        String operas = "";
        if (!expandVariables || jdkArch.endsWith("64")) {
            if (expandVariables) {
                operas += getFormatter().getOption(PathsAndFiles.OPERA_64.getDirViaAcronym(), PathsAndFiles.OPERA_64.getDescription());
            } else {
                operas += getFormatter().getOption(PathsAndFiles.OPERA_64.getFile().getParent(), PathsAndFiles.OPERA_64.getDescription());
            }
        }
        if (!expandVariables || !jdkArch.endsWith("64")) {
            if (expandVariables) {
                operas += getFormatter().getOption(PathsAndFiles.OPERA_32.getDirViaAcronym(), PathsAndFiles.OPERA_32.getDescription());
            } else {
                operas += getFormatter().getOption(PathsAndFiles.OPERA_32.getFile().getParent(), PathsAndFiles.OPERA_32.getDescription());
            }
        }
        return super.getSynopsis()
                + getFormatter().getBold(getId() + " ") + getFormatter().process(Translator.R("ITWPsynopsL1")) + getFormatter().getNewLine()
                + getFormatter().wrapParagraph(
                        getFormatter().process(Translator.R("ITWPsynopsL2", PathsAndFiles.ICEDTEA_SO, getFormatter().getUrl(ITW_PLUGIN_URL))) + getFormatter().getNewLine()
                        + getFormatter().process(Translator.R("ITWPsynopsL3")) + getFormatter().getNewLine()
                        + mozillas)
                + getFormatter().wrapParagraph(getFormatter().process(Translator.R("ITWPsynopsL4")) + getFormatter().getNewLine()
                        + operas)
                + getFormatter().wrapParagraph(Translator.R("ITWPtrademarks"));
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getOptions() {
        return "";
    }

    @Override
    public String getExamples() {
        return "";
    }

    @Override
    public String getFiles() {
        String s = super.getFiles() + getFiles(PathsAndFiles.getAllPluginFiles());
        s = s + getFilesAppendix();
        return s;

    }

    public static void main(String[] args) throws IOException {
        TextsProvider.main(new String[]{"all", "false", "3.51.a"});
    }

    @Override
    public String getCommands() {
        return "";
    }



}
