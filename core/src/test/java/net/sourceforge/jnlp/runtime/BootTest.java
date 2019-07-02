/*
 Copyright (C) 2017 Red Hat, Inc.

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
package net.sourceforge.jnlp.runtime;

import net.adoptopenjdk.icedteaweb.commandline.CommandLineOptionsDefinition;
import net.adoptopenjdk.icedteaweb.commandline.CommandLineOptionsParser;
import net.sourceforge.jnlp.util.logging.NoStdOutErrTest;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class BootTest extends NoStdOutErrTest {

    @Test
    public void fixJnlpProtocolTest() throws Exception {
        Assert.assertEquals("http://www.com/file.jnlp", Boot.fixJnlpProtocol("jnlp://www.com/file.jnlp"));
        Assert.assertEquals("https://www.com/file.jnlp", Boot.fixJnlpProtocol("jnlps://www.com/file.jnlp"));
        Assert.assertEquals("http://www.com/file.jnlp", Boot.fixJnlpProtocol("jnlp:http://www.com/file.jnlp"));
        Assert.assertEquals("https://www.com/file.jnlp", Boot.fixJnlpProtocol("jnlp:https://www.com/file.jnlp"));
        Assert.assertEquals("http://www.com/file.jnlp", Boot.fixJnlpProtocol("jnlps:http://www.com/file.jnlp"));
        Assert.assertEquals("https://www.com/file.jnlp", Boot.fixJnlpProtocol("jnlps:https://www.com/file.jnlp"));
    }

    @Test
    public void fixJnlpProtocolTestWithNullArgument() {
        assertNull(Boot.fixJnlpProtocol(null));
    }

    @Test
    public void getValidJnlpFileLocationFromCommandLineArguments() {
        // given
        String[] args = {"-nosecurity", "-Xnofork", "-jnlp", "https://www.somedomain.com/some.jnlp"};
        CommandLineOptionsParser optionsParser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getJavaWsOptions());

        // when
        final String jnlpFileLocation = Boot.getJnlpFileLocationFromCommandLineArguments(optionsParser);

        // then
        assertThat(jnlpFileLocation, is("https://www.somedomain.com/some.jnlp"));
    }

    @Test
    public void getValidJnlpFileLocationFromCommandLineArgumentsWithUnknownExtraArgument() {
        // given
        String[] args = {"-nosecurity", "-Xnofork", "-jnlp", "https://www.somedomain.com/some.jnlp", "-unknownExtraArgument"};
        CommandLineOptionsParser optionsParser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getJavaWsOptions());

        // when
        final String jnlpFileLocation = Boot.getJnlpFileLocationFromCommandLineArguments(optionsParser);

        // then
        assertThat(jnlpFileLocation, is("https://www.somedomain.com/some.jnlp"));
    }

    @Test
    public void getValidJnlpFileLocationFromCommandLineArgumentsWithHtmlArgument() {
        // given
        String[] args = {"-nosecurity", "-Xnofork", "-html", "https://www.somedomain.com/some.jnlp"};
        CommandLineOptionsParser optionsParser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getJavaWsOptions());

        // when
        final String jnlpFileLocation = Boot.getJnlpFileLocationFromCommandLineArguments(optionsParser);

        // then
        assertThat(jnlpFileLocation, is("https://www.somedomain.com/some.jnlp"));
    }

    @Test
    public void getValidJnlpFileLocationFromCommandLineArgumentsWithUnknownExtraArgumentWithoutExplizitJnlpOption() {
        // given
        String[] args = {"-nosecurity", "-Xnofork", "https://www.somedomain.com/some.jnlp", "-unknownExtraArgument"};
        CommandLineOptionsParser optionsParser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getJavaWsOptions());

        // when
        final String jnlpFileLocation = Boot.getJnlpFileLocationFromCommandLineArguments(optionsParser);

        // then
        assertThat(jnlpFileLocation, is("https://www.somedomain.com/some.jnlp"));
    }

    @Test
    public void dontFindJnlpFileLocationInCommandLineArguments() {
        String[] args = {"-nosecurity", "-Xnofork"};
        CommandLineOptionsParser optionsParser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getJavaWsOptions());

        assertNull(Boot.getJnlpFileLocationFromCommandLineArguments(optionsParser));
    }

    @Test
    public void dontFindJnlpFileLocationInCommandLineArgumentsWithExplizitJnlpOption() {
        String[] args = {"-nosecurity", "-Xnofork", "-jnlp"};
        CommandLineOptionsParser optionsParser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getJavaWsOptions());

        assertNull(Boot.getJnlpFileLocationFromCommandLineArguments(optionsParser));
    }
}
