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


package net.sourceforge.jnlp.util.optionparser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import net.sourceforge.jnlp.OptionsDefinitions;
import org.junit.Test;

import java.util.List;

public class OptionParserTest {

    @Test
    public void testGetSingleOptionValue() {
        String[] args = {"-update", "blob"};
        OptionParser parser = new OptionParser(args, OptionsDefinitions.getJavaWsOptions());

        String value = parser.getValue(OptionsDefinitions.OPTIONS.UPDATE);
        assertEquals("blob", value);
    }

    @Test
    public void testGetSingleOptionMultipleValues() {
        String[] args = {"-arg", "blob", "meow"};
        OptionParser parser = new OptionParser(args, OptionsDefinitions.getJavaWsOptions());

        List<String> values = parser.getValues(OptionsDefinitions.OPTIONS.ARG);
        assertEquals(0, values.indexOf("blob"));
        assertEquals(1, values.indexOf("meow"));
        assertEquals(2, values.size());
    }

    @Test
    public void testGetDifferentOptionValues() {
        String[] args = {"-param", "blob", "-arg", "yelp"};
        OptionParser parser = new OptionParser(args, OptionsDefinitions.getJavaWsOptions());

        List<String> values = parser.getValues(OptionsDefinitions.OPTIONS.PARAM);
        assertEquals(0, values.indexOf("blob"));
        assertEquals(1, values.size());

        values = parser.getValues(OptionsDefinitions.OPTIONS.ARG);
        assertEquals(0, values.indexOf("yelp"));
        assertEquals(1, values.size());

    }

    @Test
    public void testSupportedOptionValueNotBeingUsed() {
        String[] args = {};
        OptionParser parser = new OptionParser(args, OptionsDefinitions.getJavaWsOptions());

        List<String> values = parser.getValues(OptionsDefinitions.OPTIONS.ARG);
        assertEquals(0, values.size());
    }

    @Test
    public void testOptionValueWithNoArgument() {
        String[] args = {"-arg"};
        OptionParser parser = new OptionParser(args, OptionsDefinitions.getJavaWsOptions());

        List<String> values = parser.getValues(OptionsDefinitions.OPTIONS.ARG);
        assertEquals(0, values.size());
    }

    @Test
    public void testOneOptionMultipleTimesMultipleValues() {
        String[] args = {"-arg", "poke", "blob", "-arg", "meep"};
        OptionParser parser = new OptionParser(args, OptionsDefinitions.getJavaWsOptions());

        List<String> values = parser.getValues(OptionsDefinitions.OPTIONS.ARG);
        assertEquals(3, values.size());
        assertEquals(0, values.indexOf("poke"));
        assertEquals(1, values.indexOf("blob"));
        assertEquals(2, values.indexOf("meep"));
    }

    @Test
    public void testMultipleOptionsMultipleValues() {
        String[] args = {"-param", "poke", "blob", "-arg", "meep", "feep", "blurp"};
        OptionParser parser = new OptionParser(args, OptionsDefinitions.getJavaWsOptions());

        List<String> values = parser.getValues(OptionsDefinitions.OPTIONS.PARAM);
        assertEquals(2, values.size());
        assertEquals(0, values.indexOf("poke"));
        assertEquals(1, values.indexOf("blob"));
        values = parser.getValues(OptionsDefinitions.OPTIONS.ARG);
        assertEquals(3, values.size());
        assertEquals(0, values.indexOf("meep"));
        assertEquals(1, values.indexOf("feep"));
        assertEquals(2, values.indexOf("blurp"));
    }

    @Test
    public void testCheckOptionExists() {
        String[] args = {"-headless", "-fish", "-busybee", "boat"};
        OptionParser parser = new OptionParser(args, OptionsDefinitions.getJavaWsOptions());

        boolean value = parser.hasOption(OptionsDefinitions.OPTIONS.HEADLESS);
        assertTrue(value);
    }

    @Test
    public void testCheckOptionExistsAsNotFirstArg() {
        String[] args = {"-run", "fish", "-castle", "-headless"};
        OptionParser parser = new OptionParser(args, OptionsDefinitions.getJavaWsOptions());

        boolean value = parser.hasOption(OptionsDefinitions.OPTIONS.HEADLESS);
        assertTrue(value);
    }

    @Test
    public void testCheckOptionNotExists() {
        String[] args = {"-run", "fish", "-castle", "cat"};
        OptionParser parser = new OptionParser(args, OptionsDefinitions.getJavaWsOptions());

        boolean value = parser.hasOption(OptionsDefinitions.OPTIONS.HEADLESS);
        assertFalse(value);
    }

    @Test
    public void testMultipleOptionsWithMainArgAtTheEnd() {
        String[] args = {"-arg", "-update=green", "-version",
                "-headless", "-arg", "-about",
                "-arg", "blah1", "blah2", "blah3", "-noupdate", "-arg",
                "blah4", "blah5", "blah6", "File.jnlp"};
        OptionParser parser = new OptionParser(args, OptionsDefinitions.getJavaWsOptions());
        parser.findMainArg();
        List<String> values = parser.getValues(OptionsDefinitions.OPTIONS.ARG);
        assertEquals(6, values.size());
        assertTrue(parser.mainArgExists());
        assertEquals("File.jnlp",parser.getMainArg());
        assertEquals(0, values.indexOf("blah1"));
        assertEquals(1, values.indexOf("blah2"));
        assertEquals(2, values.indexOf("blah3"));
        assertEquals(3, values.indexOf("blah4"));
        assertEquals(4, values.indexOf("blah5"));
        assertEquals(5, values.indexOf("blah6"));
    }

    @Test
    public void testMultipleOptionsWithMainArgAsLastNotOptionValue() {
        String[] args = {"-arg", "-update=green", "-version",
                "-arg", "-about",
                "-arg", "blah1", "blah2", "blah3","-Xtrustall", "-arg",
                "blah4", "blah5", "blah6", "File.jnlp", "-headless", "-noupdate"};
        OptionParser parser = new OptionParser(args, OptionsDefinitions.getJavaWsOptions());
        parser.findMainArg();
        List<String> values = parser.getValues(OptionsDefinitions.OPTIONS.ARG);
        assertEquals(6, values.size());
        assertTrue(parser.mainArgExists());
        assertEquals("File.jnlp",parser.getMainArg());
        assertEquals(0, values.indexOf("blah1"));
        assertEquals(1, values.indexOf("blah2"));
        assertEquals(2, values.indexOf("blah3"));
        assertEquals(3, values.indexOf("blah4"));
        assertEquals(4, values.indexOf("blah5"));
        assertEquals(5, values.indexOf("blah6"));
        assertTrue(parser.hasOption(OptionsDefinitions.OPTIONS.HEADLESS));
        assertTrue(parser.hasOption(OptionsDefinitions.OPTIONS.NOUPDATE));
    }

    @Test
    public void testMultipleOptionsWithNoArgsCombinedWithMultipleOptions() {
        String[] args = {"-arg", "-update=green", "-version",
                "-arg", "-about",
                "-arg", "blah1", "blah2", "blah3","-about", "-arg",
                "blah4", "blah5", "blah6", "File.jnlp", "-headless", "-noupdate"};
        OptionParser parser = new OptionParser(args, OptionsDefinitions.getJavaWsOptions());
        assertTrue(parser.hasOption(OptionsDefinitions.OPTIONS.ABOUT));
    }

    @Test
    public void testMainArgExists() {
        String[] args = {"File.jnlp"};
        OptionParser parser = new OptionParser(args, OptionsDefinitions.getJavaWsOptions());

        assertTrue(parser.mainArgExists());
        assertEquals("File.jnlp",parser.getMainArg());
    }

    @Test
    public void testMultipleMainArgsReturnsFirstMainArg() {
        String[] args = {"File.jnlp", "FileTwo,jnlp", "FileThree.jnlp"};
        OptionParser parser = new OptionParser(args, OptionsDefinitions.getJavaWsOptions());

        assertTrue(parser.mainArgExists());
        assertEquals("File.jnlp",parser.getMainArg());
    }

    @Test
    public void testSameTagMultipleTimesWithMainArg() {
        String[] args = {"-headless", "-headless","File.jnlp", "-headless", "-headless", "-headless"};
        OptionParser parser = new OptionParser(args, OptionsDefinitions.getJavaWsOptions());

        assertTrue(parser.mainArgExists());
        assertEquals("File.jnlp",parser.getMainArg());
        assertTrue(parser.hasOption(OptionsDefinitions.OPTIONS.HEADLESS));
    }

    @Test
    public void testSameTagMultipleTimesWithoutMainArg() {
        String[] args = {"-headless", "-headless", "-headless", "-headless", "-headless"};
        OptionParser parser = new OptionParser(args, OptionsDefinitions.getJavaWsOptions());

        assertFalse(parser.mainArgExists());
        assertEquals("",parser.getMainArg());
        assertTrue(parser.hasOption(OptionsDefinitions.OPTIONS.HEADLESS));
    }

    @Test
    public void testMultipleArgTagSurroundingMainArgAfterNoArgOption() {
        String[] args = {"-arg", "blue", "green", "red", "-headless", "File.jnlp", "-arg", "yellow", "purple"};
        OptionParser parser = new OptionParser(args, OptionsDefinitions.getJavaWsOptions());
        List<String> values = parser.getValues(OptionsDefinitions.OPTIONS.ARG);
        assertEquals(0, values.indexOf("blue"));
        assertEquals(1, values.indexOf("green"));
        assertEquals(2, values.indexOf("red"));
        assertEquals(3, values.indexOf("yellow"));
        assertEquals(4, values.indexOf("purple"));
        assertTrue(parser.mainArgExists());
        assertEquals("File.jnlp",parser.getMainArg());
        assertTrue(parser.hasOption(OptionsDefinitions.OPTIONS.HEADLESS));
    }

    @Test
    public void testOptionWithDashInMiddleWontBeAltered() {
        String[] args = {"ar-g", "blue", "green", "red"};

        OptionParser parser = new OptionParser(args, OptionsDefinitions.getJavaWsOptions());
        List<String> values = parser.getValues(OptionsDefinitions.OPTIONS.ARG);
        assertEquals(values.size(), 0);
    }

    @Test
    public void testOptionHavingDashIsOptional() {
        String[] args = {"arg", "blue", "green", "red"};

        OptionParser parser = new OptionParser(args, OptionsDefinitions.getJavaWsOptions());
        List<String> values = parser.getValues(OptionsDefinitions.OPTIONS.ARG);
        assertEquals(0, values.indexOf("blue"));
        assertEquals(1, values.indexOf("green"));
        assertEquals(2, values.indexOf("red"));
    }

    @Test
    public void testOptionHavingDashIsOptionalWithMainArg() {
        String[] args = {"arg", "blue", "green", "red", "File.jnlp"};

        OptionParser parser = new OptionParser(args, OptionsDefinitions.getJavaWsOptions());
        parser.findMainArg();
        List<String> values = parser.getValues(OptionsDefinitions.OPTIONS.ARG);
        assertEquals(0, values.indexOf("blue"));
        assertEquals(1, values.indexOf("green"));
        assertEquals(2, values.indexOf("red"));
        assertTrue(parser.mainArgExists());
        assertEquals("File.jnlp",parser.getMainArg());
    }

    @Test
    public void testMultipleOptionsHavingDashIsOptional() {
        String[] args = {"arg", "blue", "green", "red", "headless", "arg", "purple", "magenta", "yellow", "verbose"};

        OptionParser parser = new OptionParser(args, OptionsDefinitions.getJavaWsOptions());
        List<String> values = parser.getValues(OptionsDefinitions.OPTIONS.ARG);
        assertEquals(0, values.indexOf("blue"));
        assertEquals(1, values.indexOf("green"));
        assertEquals(2, values.indexOf("red"));
        assertEquals(3, values.indexOf("purple"));
        assertEquals(4, values.indexOf("magenta"));
        assertEquals(5, values.indexOf("yellow"));
        assertTrue(parser.hasOption(OptionsDefinitions.OPTIONS.VERBOSE));
        assertTrue(parser.hasOption(OptionsDefinitions.OPTIONS.HEADLESS));

    }

    @Test
    public void testGetValuesEqualsSignFormatWithNoDash() {
        String[] args = {"arg=blue"};

        OptionParser parser = new OptionParser(args, OptionsDefinitions.getJavaWsOptions());
        List<String> values = parser.getValues(OptionsDefinitions.OPTIONS.ARG);
        assertEquals(0, values.indexOf("blue"));
    }

    @Test
    public void testGetValuesWithNoValueHasNoValues() {
        String[] args = {"arg"};

        OptionParser parser = new OptionParser(args, OptionsDefinitions.getJavaWsOptions());
        List<String> values = parser.getValues(OptionsDefinitions.OPTIONS.ARG);
        assertEquals(0, values.size());
    }

    @Test
    public void testOnlyFirstDashIsAcceptable() {
        String[] args = {"-arg", "blue", "a-rg", "-headless", "-arg", "green", "-ar-g"};

        OptionParser parser = new OptionParser(args, OptionsDefinitions.getJavaWsOptions());
        List<String> values = parser.getValues(OptionsDefinitions.OPTIONS.ARG);
        assertEquals(4, values.size());
        assertEquals(0, values.indexOf("blue"));
        assertEquals(1, values.indexOf("a-rg"));
        assertEquals(2, values.indexOf("green"));
        assertEquals(3, values.indexOf("-ar-g"));
    }

    @Test
    public void testFindMainArgCanHaveADash() {
        String[] args = {"-headless", "-arg", "blue", "green", "-File.jnlp"};

        OptionParser parser = new OptionParser(args, OptionsDefinitions.getJavaWsOptions());
        parser.findMainArg();
        List<String> values = parser.getValues(OptionsDefinitions.OPTIONS.ARG);
        assertEquals(2, values.size());
        assertEquals(0, values.indexOf("blue"));
        assertEquals(1, values.indexOf("green"));
        assertEquals("-File.jnlp", parser.getMainArg());
    }

    @Test
    public void testFindMainArgDoesNotTakeAwayFromOptionWithOnlyOneValueAtEnd() {
        String[] args = {"-headless", "-arg", "blue", "green", "-File.jnlp", "-update", "25"};

        OptionParser parser = new OptionParser(args, OptionsDefinitions.getJavaWsOptions());
        parser.findMainArg();
        List<String> values = parser.getValues(OptionsDefinitions.OPTIONS.ARG);
        assertEquals(2, values.size());
        assertEquals(0, values.indexOf("blue"));
        assertEquals(1, values.indexOf("green"));
        assertEquals("-File.jnlp", parser.getMainArg());
    }

    @Test
    public void testFindMainArgDoesNotTakeOptionLookingForManyValuesWithOnlyOneValueAtEnd() {
        String[] args = {"-headless", "-arg", "blue", "green", "-File.jnlp", "-arg", "yellow"};

        OptionParser parser = new OptionParser(args, OptionsDefinitions.getJavaWsOptions());
        parser.findMainArg();
        List<String> values = parser.getValues(OptionsDefinitions.OPTIONS.ARG);
        assertEquals(3, values.size());
        assertEquals(0, values.indexOf("blue"));
        assertEquals(1, values.indexOf("green"));
        assertEquals(2, values.indexOf("yellow"));
        assertEquals("-File.jnlp", parser.getMainArg());
    }

    @Test
    public void testFindMainArgWillTakeOptionLookingForManyValuesWithManyValueAtEnd() {
        String[] args = {"-headless", "-arg", "blue", "green", "-File.jnlp", "-arg", "yellow", "red"};

        OptionParser parser = new OptionParser(args, OptionsDefinitions.getJavaWsOptions());
        parser.findMainArg();
        List<String> values = parser.getValues(OptionsDefinitions.OPTIONS.ARG);
        assertEquals(4, values.size());
        assertEquals(0, values.indexOf("blue"));
        assertEquals(1, values.indexOf("green"));
        assertEquals(2, values.indexOf("-File.jnlp"));
        assertEquals(3, values.indexOf("yellow"));
        assertEquals("red", parser.getMainArg());
    }
    
    @Test
    public void testOptionsSyntaxPositive() {
        assertTrue(OptionParser.stringEqualsOption("headless", OptionsDefinitions.OPTIONS.HEADLESS));
        assertTrue(OptionParser.stringEqualsOption("-headless", OptionsDefinitions.OPTIONS.HEADLESS));
        assertTrue(OptionParser.stringEqualsOption("--headless", OptionsDefinitions.OPTIONS.HEADLESS));
        assertTrue(OptionParser.stringEqualsOption("---headless", OptionsDefinitions.OPTIONS.HEADLESS));
    }
    
    @Test
    public void testOptionsSyntaxNegative() {
        assertFalse(OptionParser.stringEqualsOption(" -headless", OptionsDefinitions.OPTIONS.HEADLESS));
        assertFalse(OptionParser.stringEqualsOption("h-eadless", OptionsDefinitions.OPTIONS.HEADLESS));
        assertFalse(OptionParser.stringEqualsOption("headless-", OptionsDefinitions.OPTIONS.HEADLESS));
        assertFalse(OptionParser.stringEqualsOption("- -headless", OptionsDefinitions.OPTIONS.HEADLESS));
        assertFalse(OptionParser.stringEqualsOption("--- ---headless", OptionsDefinitions.OPTIONS.HEADLESS));
        assertFalse(OptionParser.stringEqualsOption("- ---headless", OptionsDefinitions.OPTIONS.HEADLESS));
        assertFalse(OptionParser.stringEqualsOption("--- -headless", OptionsDefinitions.OPTIONS.HEADLESS));
    }
}