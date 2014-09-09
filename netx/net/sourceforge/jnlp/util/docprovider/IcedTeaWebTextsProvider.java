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
                        getFormatter().getBold(getId() + " ")
                        + "provides a Free Software web browser plugin running applets written in the Java programming language"
                        + " and an implementation of Java Web Start, originally based on the NetX project."
                        + getFormatter().getNewLine() + getFormatter().getNewLine()
                        + "NetX allows Java applets and applications to be downloaded over the network, cached, and (by default) run"
                        + " in a secure sandbox environment. Subsequent runs of the applet download the latest version automatically. Update"
                        + " and security settings, among others, can be set using the itw-settings command."
                        + getFormatter().getNewLine()
                        + getId() + " also includes a plugin to " + getFormatter().getUrl("http://www.java.com/en/download/testjava.jsp", "enable Java applets")
                        + " within web browsers."
                        + getFormatter().getNewLine()
                        + getFormatter().getOption("",Translator.R("BFileInfoAuthors"))
                        + getFormatter().getOption("",Translator.R("BFileInfoCopying"))
                        + getFormatter().getOption("",Translator.R("BFileInfoNews"))
                        + getFormatter().getNewLine() + getFormatter().getNewLine());

    }

    @Override
    public String getSynopsis() {
        return "";
    }

    @Override
    public String getDescription() {
        StringBuilder p1 = new StringBuilder();
        p1.append(getFormatter().getOption("Modular", "Easily add JNLP capabilities to an application."));
        p1.append(getFormatter().getOption("Saves Memory", "Launch programs in a shared JVM."));
        p1.append(getFormatter().getOption("Fast startup", "Runs applications from a cache for fast starting."));
        p1.append(getFormatter().getOption("Security", "Run any application in a sandbox or log its activities."));
        p1.append(getFormatter().getOption("Auto-Update", "Applications can auto-update without special code."));
        p1.append(getFormatter().getOption("Network Deployment", "Deploy to the internet, not with installers."));
        p1.append(getFormatter().getOption("Open Source", "GNU Lesser General Public License."));
        p1.append(getFormatter().getNewLine());

        StringBuilder p2 = new StringBuilder();
        p2.append("Visit the ").append(getFormatter().getUrl(IT_MAIN, "IcedTea project wiki"));
        p2.append(" or specifically the ").append(getFormatter().getUrl(ITW_HOME, "IcedTea-Web home")).append(" pages for more information.");
        p2.append(getFormatter().getNewLine());
        p2.append("Help with common issues with IcedTea-Web can be found ").append(getFormatter().getUrl(ITW_ISSUES, "here"));
        p2.append(getFormatter().getNewLine());

        String header = getFormatter().getBold("Features of NetX: ") + getFormatter().getNewLine();
        return super.getDescription() + getFormatter().wrapParagraph(header) + getFormatter().wrapParagraph(p1.toString()) + getFormatter().getNewLine() + getFormatter().wrapParagraph(p2.toString());
    }

    @Override
    public String getOptions() {
        String l1 = "A " + getFormatter().getUrl(IT_QUICK, "QuickStart") + " guide for the IcedTea project is available on the wiki.";
        String l2 = getFormatter().getUrl(ITW_STYLE, "Code style") + " guidelines and "
                + getFormatter().getUrl(ITW_ECLIPSE, "Eclipse setup") + " instructions for IcedTea-Web"
                + " are available as well. Patches should be accompanied by unit tests and"
                + getFormatter().getUrl(ITW_REPRODUCERS, "reproducers") + " before being sent to"
                + getFormatter().getUrl(DISTRO_PKG, "the mailing list");
        String header = getFormatter().getBold("Contributing:  ") + getFormatter().getNewLine();
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
