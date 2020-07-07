package net.sourceforge.jnlp.util.logging;

import net.adoptopenjdk.icedteaweb.logging.BaseLogger;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory.LoggerFactoryImpl;
import net.sourceforge.jnlp.util.logging.headers.Header;
import net.sourceforge.jnlp.util.logging.headers.JavaMessage;
import net.sourceforge.jnlp.util.logging.headers.MessageWithHeader;

import static net.sourceforge.jnlp.util.logging.OutputControllerLevel.ERROR;
import static net.sourceforge.jnlp.util.logging.OutputControllerLevel.INFO;
import static net.sourceforge.jnlp.util.logging.OutputControllerLevel.DEBUG;
import static net.sourceforge.jnlp.util.logging.OutputControllerLevel.WARN;

/**
 * Factory for creating {@link Logger Loggers} which log to the {@link OutputController}.
 *
 * NOTE:
 * This class is instantiated using reflection from {@link LoggerFactory}.
 */
@SuppressWarnings("unused")
public class OutputControllerLoggerFactory implements LoggerFactoryImpl {

    public Logger getLogger(final Class<?> forClass) {
        return new OutputControllerLogger(forClass, OutputController.getLogger());
    }

    Logger getLogger(Class<?> forClass, BasicOutputController outputController) {
        return new OutputControllerLogger(forClass, outputController);
    }

    private static class OutputControllerLogger extends BaseLogger {

        private final BasicOutputController outputController;
        private final String caller;

        private OutputControllerLogger(final Class<?> forClass, BasicOutputController outputController) {
            this.caller = forClass.getName();
            this.outputController = outputController;
        }

        @Override
        public void debug(final String msg) {
            log(DEBUG, msg, null);
        }

        @Override
        public void debug(final String msg, final Object... arguments) {
            log(DEBUG, expand(msg, arguments), null);
        }

        @Override
        public void debug(final String msg, final Throwable t) {
            log(DEBUG, msg, t);
        }

        @Override
        public void info(final String msg) {
            log(INFO, msg, null);
        }

        @Override
        public void info(final String msg, final Object... arguments) {
            log(INFO, expand(msg, arguments), null);
        }

        @Override
        public void info(final String msg, final Throwable t) {
            log(INFO, msg, t);
        }

        @Override
        public void warn(final String msg) {
            log(WARN, msg, null);
        }

        @Override
        public void warn(final String msg, final Object... arguments) {
            log(WARN, expand(msg, arguments), null);
        }

        @Override
        public void warn(final String msg, final Throwable t) {
            log(WARN, msg, t);
        }

        @Override
        public void error(final String msg) {
            log(ERROR, msg, null);
        }

        @Override
        public void error(final String msg, final Object... arguments) {
            log(ERROR, expand(msg, arguments), null);
        }

        @Override
        public void error(final String msg, final Throwable t) {
            log(ERROR, msg, t);
        }

        private void log(final OutputControllerLevel level, final String msg, final Throwable t) {
            final Header header = new Header(level, caller);
            final MessageWithHeader message = new JavaMessage(header, msg, t);
            outputController.log(message);
        }
    }
}
