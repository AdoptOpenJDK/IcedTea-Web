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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.sourceforge.jnlp.config.Defaults;
import net.sourceforge.jnlp.OptionsDefinitions;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.config.InfrastructureFileDescriptor;
import net.sourceforge.jnlp.config.PathsAndFiles;
import net.sourceforge.jnlp.config.Setting;
import net.sourceforge.jnlp.config.ValueValidator;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.runtime.Translator;
import net.sourceforge.jnlp.util.docprovider.formatters.formatters.Formatter;

public class ItwebSettingsTextsProvider extends TextsProvider {

    public ItwebSettingsTextsProvider(String encoding, Formatter formatter, boolean forceTitles, boolean expandFiles) {
        super(encoding, formatter, forceTitles, expandFiles);
    }

    @Override
    public String getId() {
        return ITWEB_SETTINGS;
    }

    @Override
    public String getIntroduction() {
        return super.getIntroduction()
                + getFormatter().wrapParagraph(
                        getFormatter().process(getId() + " " + Translator.R("ITWSintro")));
    }

    @Override
    public String getSynopsis() {
        return super.getSynopsis()
                + getFormatter().wrapParagraph(
                        getFormatter().getBoldOpening() + getId() + " "
                        + getFormatter().getBoldCloseNwlineBoldOpen()
                        + getId() + " " + getFormatter().getBoldClosing()
                        + Translator.R("ITWSsynops"));
    }

    @Override
    public String getDescription() {
        return super.getDescription()
                + getFormatter().wrapParagraph(getFormatter().getBold(getId() + " ") + getFormatter().process(
                                Translator.R("IWSdescL1")
                                + getFormatter().getNewLine() + getFormatter().getNewLine()
                                + Translator.R("IWSdescL2")
                                + getFormatter().getNewLine() + getFormatter().getNewLine()
                                + Translator.R("IWSdescL3")));

    }

    @Override
    public String getOptions() {
        return "";
    }

    @Override
    public String getCommands() {
        return super.getDescription()
                + getFormatter().wrapParagraph(optionsToString(OptionsDefinitions.getItwsettingsCommands()));
    }

    @Override
    public String getExamples() {
        return super.getExamples()
                + getFormatter().wrapParagraph(
                        getFormatter().getOption(getId(), Translator.R("IWSexampleL1"))
                        + getFormatter().getOption(getId() + "  " + OptionsDefinitions.OPTIONS.RESET.option + " " + DeploymentConfiguration.KEY_PROXY_TYPE, " " + Translator.R("IWSexampleL2", DeploymentConfiguration.KEY_PROXY_TYPE)))
                + getFormatter().getNewLine()
                + getFormatter().wrapParagraph(getKpMinorTitle() + getFormatter().getNewLine()
                        + getFormatter().wrapParagraph(getProperties()));

    }

    @Override
    public String getFiles() {
        return super.getFiles() + getFiles(PathsAndFiles.getAllItWebSettingsFiles()) + getFilesAppendix();

    }

    public static void main(String[] args) throws IOException {
        TextsProvider.main(new String[]{"all", "true", "3.51.a"});
    }

    private String getProperties() {
        StringBuilder sb = new StringBuilder();
        List<Map.Entry<String, Setting<String>>> defaults = new ArrayList<>(Defaults.getDefaults().entrySet());
        Collections.sort(defaults, new Comparator<Map.Entry<String, Setting<String>>>() {
            @Override
            public int compare(Map.Entry<String, Setting<String>> o1, Map.Entry<String, Setting<String>> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });
        List<InfrastructureFileDescriptor> files = PathsAndFiles.getAllFiles();
        for (Map.Entry<String, Setting<String>> entry : defaults) {
            String defaultValue = entry.getValue().getDefaultValue();
            String fileAcronom = null;
            for (InfrastructureFileDescriptor f : files) {
                if (matchSttingsValueWithInfrastrucutreFile(entry.getValue(), f)) {
                    fileAcronom = f.toString();
                    break;
                }
            }
            String setValue = JNLPRuntime.getConfiguration().getProperty(entry.getKey());
            if (defaultValue == null) {
                defaultValue = "null";
            }
            if (setValue == null) {
                setValue = "null";
            }
            String value;
            if (expandVariables) {
                if (defaultValue.equals(setValue)) {
                    value = defaultValue;
                } else {
                    value = setValue + " (" + Translator.R("ITWSdefault") + ": " + defaultValue + ")";
                }
            } else {
                if (fileAcronom == null) {
                    value = defaultValue;
                } else {
                    value = fileAcronom;
                }
            }
            ValueValidator v = entry.getValue().getValidator();
            if (v != null && v.getPossibleValues() != null && !v.getPossibleValues().trim().isEmpty()) {
                value = value + " (" + Translator.R("IWSpossible") + " " + v.getPossibleValues() + ")";
            }
            sb.append(getFormatter().getOption(entry.getKey(), value));
        }
        return sb.toString();
    }

    public String getKpMinorTitle() {
        if (expandVariables) {
            return getFormatter().getBold(Translator.R("IWSexampleL3") + " " + Translator.R("IWSexampleL31"));
        } else {
            return getFormatter().getBold(Translator.R("IWSexampleL3") + " " + Translator.R("IWSexampleL32"));
        }
    }
}
