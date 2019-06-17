package net.sourceforge.jnlp.util.logging;


import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.sourceforge.jnlp.util.logging.headers.MessageWithHeader;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static net.sourceforge.jnlp.util.logging.OutputControllerLevel.MESSAGE_DEBUG;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

/**
 * Tests for {@link Logger} provided by {@link OutputControllerLoggerFactory}.
 */
public class OutputControllerLoggerTest {

    private List<MessageWithHeader> loggedMessages;
    private Logger sut;

    @Before
    public void setup() {
        final BasicOutputController outputController = new BasicOutputController() {
            @Override
            public void log(MessageWithHeader l) {
                loggedMessages.add(l);
            }
        };

        loggedMessages = new ArrayList<>();
        sut = new OutputControllerLoggerFactory().getLogger(getClass(), outputController);
    }

    @Test
    public void debugMessageWithoutStacktrace() {
        sut.debug("message {}", "ONE");
        assertThat(loggedMessages, hasSize(1));

        final MessageWithHeader msg = loggedMessages.get(0);
        assertThat(msg.getMessage(), is("message ONE"));
        assertThat(msg.getHeader().caller, is(getClass().getName()));
        assertThat(msg.getHeader().level, is(MESSAGE_DEBUG));
        assertThat(msg.hasStackTrace(), is(false));
        assertThat(msg.getStackTrace(), is(nullValue()));
    }

    @Test
    public void debugMessageWithStacktrace() {
        sut.debug("message TWO", new RuntimeException("Ex Msg"));
        assertThat(loggedMessages, hasSize(1));

        final MessageWithHeader msg = loggedMessages.get(0);
        assertThat(msg.getMessage(), is("message TWO"));
        assertThat(msg.getHeader().caller, is(getClass().getName()));
        assertThat(msg.getHeader().level, is(MESSAGE_DEBUG));
        assertThat(msg.hasStackTrace(), is(true));
        assertThat(msg.getStackTrace(), containsString("Ex Msg"));
        assertThat(msg.getStackTrace(), containsString(getClass().getName()));
    }
}
