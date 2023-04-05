package net.adoptopenjdk.icedteaweb.logging;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class BaseLoggerTest {
    @Test
    public void testNullMessageHandledCorrectly() {
        final String message = null;
        final Object[] args = {"ONE"};

        assertThat(BaseLogger.doExpand(message, args), is("null"));
    }

    @Test
    public void testMessageWithoutArgumentsStaysUnchanged() {
        final String message = "Message without arguments stay unchanged.";
        final Object[] args = {};

        assertThat(BaseLogger.doExpand(message, args), is("Message without arguments stay unchanged."));
    }

    @Test
    public void testMessageWitNullArgumentsStaysUnchanged() {
        final String message = "Message with null arguments stay unchanged.";
        final Object[] args = null;

        assertThat(BaseLogger.doExpand(message, args), is("Message with null arguments stay unchanged."));
    }

    @Test
    public void testMessageWithOneArgumentExpandsCorrectly() {
        final String message = "This is a message with {} argument.";
        final Object[] args = {"ONE"};

        assertThat(BaseLogger.doExpand(message, args), is("This is a message with ONE argument."));
    }

    @Test
    public void testMessageWithTwoArgumentExpandsCorrectly() {
        final String message = "This is a message with the arguments {} and {}.";
        final Object[] args = {"ONE", 2};

        assertThat(BaseLogger.doExpand(message, args), is("This is a message with the arguments ONE and 2."));
    }

    @Test
    public void testMessageWithTooManyArgumentExpandsCorrectly() {
        final String message = "This is a message with the arguments {} and {}.";
        final Object[] args = {"ONE", 2, "abc"};

        assertThat(BaseLogger.doExpand(message, args), is("This is a message with the arguments ONE and 2."));
    }

    @Test
    public void testMessageWithTooFewArgumentsExpandsCorrectly() {
        final String message = "This is a message with the arguments {} and {}.";
        final Object[] args = {"ONE"};

        assertThat(BaseLogger.doExpand(message, args), is("This is a message with the arguments ONE and {}."));
    }
}
