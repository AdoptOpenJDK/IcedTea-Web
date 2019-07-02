/*Copyright (C) 2014 Red Hat, Inc.

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


package net.adoptopenjdk.icedteaweb.commandline;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class CommandLineOptionsParserTest {

    @Test
    public void testGetSingleOptionValue() {
        String[] args = {"-update", "blob"};
        CommandLineOptionsParser parser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getJavaWsOptions());

        String value = parser.getParam(CommandLineOptions.UPDATE);
        assertEquals("blob", value);
    }

    @Test
    public void testGetSingleOptionMultipleValues() {
        String[] args = {"-arg", "blob", "meow"};
        CommandLineOptionsParser parser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getJavaWsOptions());

        List<String> values = parser.getParams(CommandLineOptions.ARG);
        assertEquals(0, values.indexOf("blob"));
        assertEquals(1, values.indexOf("meow"));
        assertEquals(2, values.size());
    }

    @Test
    public void testGetDifferentOptionValues() {
        String[] args = {"-param", "blob", "-arg", "yelp"};
        CommandLineOptionsParser parser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getJavaWsOptions());

        List<String> values = parser.getParams(CommandLineOptions.PARAM);
        assertEquals(0, values.indexOf("blob"));
        assertEquals(1, values.size());

        values = parser.getParams(CommandLineOptions.ARG);
        assertEquals(0, values.indexOf("yelp"));
        assertEquals(1, values.size());

    }

    @Test
    public void testSupportedOptionValueNotBeingUsed() {
        String[] args = {};
        CommandLineOptionsParser parser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getJavaWsOptions());

        List<String> values = parser.getParams(CommandLineOptions.ARG);
        assertEquals(0, values.size());
    }

    @Test
    public void testOptionValueWithNoArgument() {
        String[] args = {"-arg"};
        CommandLineOptionsParser parser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getJavaWsOptions());

        List<String> values = parser.getParams(CommandLineOptions.ARG);
        assertEquals(0, values.size());
    }

    @Test
    public void testOneOptionMultipleTimesMultipleValues() {
        String[] args = {"-arg", "poke", "blob", "-arg", "meep"};
        CommandLineOptionsParser parser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getJavaWsOptions());

        List<String> values = parser.getParams(CommandLineOptions.ARG);
        assertEquals(3, values.size());
        assertEquals(0, values.indexOf("poke"));
        assertEquals(1, values.indexOf("blob"));
        assertEquals(2, values.indexOf("meep"));
    }

    @Test
    public void testMultipleOptionsMultipleValues() {
        String[] args = {"-param", "poke", "blob", "-arg", "meep", "feep", "blurp"};
        CommandLineOptionsParser parser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getJavaWsOptions());

        List<String> values = parser.getParams(CommandLineOptions.PARAM);
        assertEquals(2, values.size());
        assertEquals(0, values.indexOf("poke"));
        assertEquals(1, values.indexOf("blob"));
        values = parser.getParams(CommandLineOptions.ARG);
        assertEquals(3, values.size());
        assertEquals(0, values.indexOf("meep"));
        assertEquals(1, values.indexOf("feep"));
        assertEquals(2, values.indexOf("blurp"));
    }

    @Test
    public void testCheckOptionExists() {
        String[] args = {"-headless", "-fish", "-busybee", "boat"};
        CommandLineOptionsParser parser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getJavaWsOptions());

        boolean value = parser.hasOption(CommandLineOptions.HEADLESS);
        assertTrue(value);
    }

    @Test
    public void testCheckOptionExistsAsNotFirstArg() {
        String[] args = {"-run", "fish", "-castle", "-headless"};
        CommandLineOptionsParser parser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getJavaWsOptions());

        boolean value = parser.hasOption(CommandLineOptions.HEADLESS);
        assertTrue(value);
    }

    @Test
    public void testCheckOptionNotExists() {
        String[] args = {"-run", "fish", "-castle", "cat"};
        CommandLineOptionsParser parser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getJavaWsOptions());

        boolean value = parser.hasOption(CommandLineOptions.HEADLESS);
        assertFalse(value);
    }

    @Test
    public void testMultipleOptionsWithMainArgAtTheEnd() {
        String[] args = {"-arg", "-update=green", "-version",
                "-headless", "-arg", "-about",
                "-arg", "blah1", "blah2", "blah3", "-noupdate", "-arg",
                "blah4", "blah5", "blah6", "-headless", "File.jnlp"};
        CommandLineOptionsParser parser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getJavaWsOptions());

        List<String> values = parser.getParams(CommandLineOptions.ARG);
        assertEquals(6, values.size());
        assertTrue(parser.mainArgExists());
        assertEquals("File.jnlp", parser.getMainArg());
        assertEquals(0, values.indexOf("blah1"));
        assertEquals(1, values.indexOf("blah2"));
        assertEquals(2, values.indexOf("blah3"));
        assertEquals(3, values.indexOf("blah4"));
        assertEquals(4, values.indexOf("blah5"));
        assertEquals(5, values.indexOf("blah6"));
    }

    @Test
    public void testMultipleOptionsWithNoArgsCombinedWithMultipleOptions() {
        String[] args = {"-arg", "-update=green", "-version",
                "-arg", "-about",
                "-arg", "blah1", "blah2", "blah3", "-about", "-arg",
                "blah4", "blah5", "blah6", "File.jnlp", "-headless", "-noupdate"};
        CommandLineOptionsParser parser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getJavaWsOptions());
        assertTrue(parser.hasOption(CommandLineOptions.ABOUT));
    }

    @Test
    public void testMainArgExists() {
        String[] args = {"File.jnlp"};
        CommandLineOptionsParser parser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getJavaWsOptions());

        assertTrue(parser.mainArgExists());
        assertEquals("File.jnlp", parser.getMainArg());
    }

    @Test
    public void testMultipleMainArgsReturnsFirstMainArg() {
        String[] args = {"File.jnlp", "FileTwo,jnlp", "FileThree.jnlp"};
        CommandLineOptionsParser parser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getJavaWsOptions());

        assertTrue(parser.mainArgExists());
        assertEquals("File.jnlp", parser.getMainArg());
    }

    @Test
    public void testSameTagMultipleTimesWithMainArg() {
        String[] args = {"-headless", "-headless", "File.jnlp", "-headless", "-headless", "-headless"};
        CommandLineOptionsParser parser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getJavaWsOptions());

        assertTrue(parser.mainArgExists());
        assertEquals("File.jnlp", parser.getMainArg());
        assertTrue(parser.hasOption(CommandLineOptions.HEADLESS));
    }

    @Test
    public void testSameTagMultipleTimesWithoutMainArg() {
        String[] args = {"-headless", "-headless", "-headless", "-headless", "-headless"};
        CommandLineOptionsParser parser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getJavaWsOptions());

        assertFalse(parser.mainArgExists());
        assertEquals(null, parser.getMainArg());
        assertTrue(parser.hasOption(CommandLineOptions.HEADLESS));
    }

    @Test
    public void testMultipleArgTagSurroundingMainArgAfterNoArgOption() {
        String[] args = {"-arg", "blue", "green", "red", "-headless", "File.jnlp", "-arg", "yellow", "purple"};
        CommandLineOptionsParser parser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getJavaWsOptions());
        List<String> values = parser.getParams(CommandLineOptions.ARG);
        assertEquals(0, values.indexOf("blue"));
        assertEquals(1, values.indexOf("green"));
        assertEquals(2, values.indexOf("red"));
        assertEquals(3, values.indexOf("yellow"));
        assertEquals(4, values.indexOf("purple"));
        assertTrue(parser.mainArgExists());
        assertEquals("File.jnlp", parser.getMainArg());
        assertTrue(parser.hasOption(CommandLineOptions.HEADLESS));
    }

    @Test
    public void testOptionWithDashInMiddleWontBeAltered() {
        String[] args = {"ar-g", "blue", "green", "red"};

        CommandLineOptionsParser parser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getJavaWsOptions());
        List<String> values = parser.getParams(CommandLineOptions.ARG);
        assertEquals(values.size(), 0);
    }

    @Test
    public void testGetParamsWithNoValueHasNoValues() {
        String[] args = {"-arg"};

        CommandLineOptionsParser parser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getJavaWsOptions());
        List<String> values = parser.getParams(CommandLineOptions.ARG);
        assertEquals(0, values.size());
    }

    @Test
    public void testOnlyFirstDashIsAcceptable() {
        String[] args = {"-arg", "blue", "a-rg", "-headless", "-arg", "green", "-ar-g"};

        CommandLineOptionsParser parser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getJavaWsOptions());
        List<String> values = parser.getParams(CommandLineOptions.ARG);
        assertEquals(4, values.size());
        assertEquals(0, values.indexOf("blue"));
        assertEquals(1, values.indexOf("a-rg"));
        assertEquals(2, values.indexOf("green"));
        assertEquals(3, values.indexOf("-ar-g"));
    }

    @Test
    public void testOptionsSyntaxPositive() {
        assertTrue(CommandLineOptionsParser.stringEqualsOption("-headless", CommandLineOptions.HEADLESS));
        assertTrue(CommandLineOptionsParser.stringEqualsOption("headless", CommandLineOptions.HEADLESS));
        assertTrue(CommandLineOptionsParser.stringEqualsOption("--headless", CommandLineOptions.HEADLESS));
        assertTrue(CommandLineOptionsParser.stringEqualsOption("---headless", CommandLineOptions.HEADLESS));
    }

    @Test
    public void testOptionsSyntaxNegative() {
        assertFalse(CommandLineOptionsParser.stringEqualsOption(" -headless", CommandLineOptions.HEADLESS));
        assertFalse(CommandLineOptionsParser.stringEqualsOption("h-eadless", CommandLineOptions.HEADLESS));
        assertFalse(CommandLineOptionsParser.stringEqualsOption("headless-", CommandLineOptions.HEADLESS));
        assertFalse(CommandLineOptionsParser.stringEqualsOption("- -headless", CommandLineOptions.HEADLESS));
        assertFalse(CommandLineOptionsParser.stringEqualsOption("--- ---headless", CommandLineOptions.HEADLESS));
        assertFalse(CommandLineOptionsParser.stringEqualsOption("- ---headless", CommandLineOptions.HEADLESS));
        assertFalse(CommandLineOptionsParser.stringEqualsOption("--- -headless", CommandLineOptions.HEADLESS));
    }

    @Test
    public void testOptionWithEqualsParamIsValid() {
        String[] args = {"-arg=blue"};
        CommandLineOptionsParser parser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getJavaWsOptions());

        List<String> values = parser.getParams(CommandLineOptions.ARG);
        assertEquals(1, values.size());
        assertEquals(0, values.indexOf("blue"));
    }

    @Test
    public void testMultipleOptionWithEqualsParamIsValid() {
        String[] args = {"-arg=blue", "-property=red", "-param=green"};
        CommandLineOptionsParser parser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getJavaWsOptions());

        List<String> values = parser.getParams(CommandLineOptions.ARG);
        assertEquals(1, values.size());
        assertEquals(0, values.indexOf("blue"));
        values = parser.getParams(CommandLineOptions.PROPERTY);
        assertEquals(1, values.size());
        assertEquals(0, values.indexOf("red"));
        values = parser.getParams(CommandLineOptions.PARAM);
        assertEquals(1, values.size());
        assertEquals(0, values.indexOf("green"));
    }

    @Test
    public void testSameOptionWithEqualsParamMultipleTimesIsValid() {
        String[] args = {"-arg=blue", "-arg=red", "-arg=green"};
        CommandLineOptionsParser parser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getJavaWsOptions());

        List<String> values = parser.getParams(CommandLineOptions.ARG);
        assertEquals(3, values.size());
        assertEquals(0, values.indexOf("blue"));
        assertEquals(1, values.indexOf("red"));
        assertEquals(2, values.indexOf("green"));
    }

    @Test
    public void testParamsCanHaveEqualsSigns() {
        String[] args = {"-arg", "colour=red", "height=50", "width=222", "circular=true"};
        CommandLineOptionsParser parser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getJavaWsOptions());

        List<String> values = parser.getParams(CommandLineOptions.ARG);
        assertEquals(4, values.size());
        assertEquals(0, values.indexOf("colour=red"));
        assertEquals(1, values.indexOf("height=50"));
        assertEquals(2, values.indexOf("width=222"));
        assertEquals(3, values.indexOf("circular=true"));
    }

    @Test
    public void testParamsCanHaveDashes() {
        String[] args = {"-arg", "-red", "-koala", "-panda", "-grizzly"};
        CommandLineOptionsParser parser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getJavaWsOptions());

        List<String> values = parser.getParams(CommandLineOptions.ARG);
        assertEquals(4, values.size());
        assertEquals(0, values.indexOf("-red"));
        assertEquals(1, values.indexOf("-koala"));
        assertEquals(2, values.indexOf("-panda"));
        assertEquals(3, values.indexOf("-grizzly"));
    }

    @Test
    public void testParamsCanHaveDashesAndEqualsSigns() {
        //given:
        final String[] args = {"-arg", "-red=colour", "-koala=animal", "-panda=bear", "-grizzly=bear"};
        final CommandLineOptionsParser parser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getJavaWsOptions());

        //when:
        final List<String> values = parser.getParams(CommandLineOptions.ARG);

        //then:
        assertEquals(4, values.size());
        assertEquals(0, values.indexOf("-red=colour"));
        assertEquals(1, values.indexOf("-koala=animal"));
        assertEquals(2, values.indexOf("-panda=bear"));
        assertEquals(3, values.indexOf("-grizzly=bear"));
    }

    @Test
    public void testMainArgAfterNoArgOption() {
        String[] args = {"-arg", "-red=colour", "-headless", "File.jnlp", "-arg", "-grizzly=bear"};
        CommandLineOptionsParser parser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getJavaWsOptions());

        assertEquals("File.jnlp", parser.getMainArg());
    }

    @Test
    public void testMainArgAfterOneArgOption() {
        String[] args = {"-arg", "-red=colour", "-update", "200", "File.jnlp", "-arg", "-grizzly=bear"};
        CommandLineOptionsParser parser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getJavaWsOptions());

        assertEquals("File.jnlp", parser.getMainArg());
    }

    @Test
    public void testMainArgAfterManyArgsOptionIsNotAccepted() {
        String[] args = {"-arg", "-red=colour", "-arg", "200", "File.jnlp", "-arg", "-grizzly=bear"};
        CommandLineOptionsParser parser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getJavaWsOptions());

        assertFalse(parser.mainArgExists());
        assertNotEquals("File.jnlp", parser.getMainArg());
    }

    @Test
    public void testOptionWithMultipleEqualSignsOnlyParsesFirstEquals() {
        String[] args = {"-arg=grizzly=panda=goldfish=mouse"};
        CommandLineOptionsParser parser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getJavaWsOptions());

        List<String> values = parser.getParams(CommandLineOptions.ARG);

        assertEquals(1, values.size());
        assertEquals(0, values.indexOf("grizzly=panda=goldfish=mouse"));
    }

    @Test
    public void testGetParam() {
        String[] args = {"-arg", "blue"};
        CommandLineOptionsParser parser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getJavaWsOptions());

        String value = parser.getParam(CommandLineOptions.ARG);
        assertEquals("blue", value);
    }

    @Test
    public void testGetParamWithManyParams() {
        String[] args = {"-arg", "blue", "red", "green"};
        CommandLineOptionsParser parser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getJavaWsOptions());

        String value = parser.getParam(CommandLineOptions.ARG);
        assertEquals("blue", value);
    }

    @Test
    public void testGetParamWithNoParams() {
        String[] args = {"-arg"};
        CommandLineOptionsParser parser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getJavaWsOptions());

        String value = parser.getParam(CommandLineOptions.ARG);
        assertEquals("", value);
    }

    @Test
    public void testGetNumberOfOptions() {
        String[] args = {"-arg", "-version", "-param", "-property", "-update"};
        CommandLineOptionsParser parser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getJavaWsOptions());
        assertEquals(5, parser.getNumberOfOptions());
    }

    @Test
    public void testGetNumberOfOptionsWithOtherOptions() {
        String[] args = {"-arg", "-version", "-param", "-property", "-update", "-set", "-reset"};
        CommandLineOptionsParser parser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getJavaWsOptions());
        assertEquals(5, parser.getNumberOfOptions());
    }

    @Test
    public void testEvenNumberSupportsEqualsChar() {
        String[] args = {"-set", "yes", "no", "blue=red", "green", "orange", "yellow=purple=roseyred"};
        CommandLineOptionsParser parser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getItwsettingsCommands());
        List<String> values = parser.getParams(CommandLineOptions.SET);
        assertEquals("yes", values.get(0));
        assertEquals("no", values.get(1));
        assertEquals("blue", values.get(2));
        assertEquals("red", values.get(3));
        assertEquals("green", values.get(4));
        assertEquals("orange", values.get(5));
        assertEquals("yellow", values.get(6));
        assertEquals("purple=roseyred", values.get(7));
    }

    @Test(expected = UnevenParameterException.class)
    public void testEvenNumberSupportsEqualsCharThrowsExceptionWhenParametersIsUneven() {
        String[] args = {"-set", "yes", "no", "blue=red", "green"};
        CommandLineOptionsParser parser = new CommandLineOptionsParser(args, CommandLineOptionsDefinition.getItwsettingsCommands());
    }
}