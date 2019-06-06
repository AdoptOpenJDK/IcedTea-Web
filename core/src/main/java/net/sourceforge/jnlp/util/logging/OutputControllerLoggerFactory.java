package net.sourceforge.jnlp.util.logging;

import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory.LoggerFactoryImpl;
import net.sourceforge.jnlp.util.logging.headers.Header;
import net.sourceforge.jnlp.util.logging.headers.JavaMessage;
import net.sourceforge.jnlp.util.logging.headers.MessageWithHeader;

import java.util.Objects;

import static net.sourceforge.jnlp.util.logging.OutputControllerLevel.ERROR_ALL;
import static net.sourceforge.jnlp.util.logging.OutputControllerLevel.MESSAGE_ALL;
import static net.sourceforge.jnlp.util.logging.OutputControllerLevel.MESSAGE_DEBUG;
import static net.sourceforge.jnlp.util.logging.OutputControllerLevel.WARNING_ALL;

/**
 * Factory for creating {@link Logger Loggers} which log to the {@link OutputController}.
 *
 * NOTE:
 * This class is instantiated using reflection from {@link LoggerFactory}.
 */
@SuppressWarnings("unused")
public class OutputControllerLoggerFactory implements LoggerFactoryImpl {

    public Logger getLogger(Class<?> forClass) {
        return new OutputControllerLogger(forClass);
    }

    private static class OutputControllerLogger implements Logger {

        private static final OutputController OUTPUT_CONTROLLER = OutputController.getLogger();
        private final Class<?> forClass;

        private OutputControllerLogger(Class<?> forClass) {
            this.forClass = forClass;
        }

        @Override
        public void debug(String msg) {
            log(MESSAGE_DEBUG, msg, null);
        }

        @Override
        public void debug(String msg, Object... arguments) {
            log(MESSAGE_DEBUG, expand(msg, arguments), null);
        }

        @Override
        public void debug(String msg, Throwable t) {
            log(MESSAGE_DEBUG, msg, t);
        }

        @Override
        public void info(String msg) {
            log(MESSAGE_ALL, msg, null);
        }

        @Override
        public void info(String msg, Object... arguments) {
            log(MESSAGE_ALL, expand(msg, arguments), null);
        }

        @Override
        public void info(String msg, Throwable t) {
            log(MESSAGE_ALL, msg, t);
        }

        @Override
        public void warn(String msg) {
            log(WARNING_ALL, msg, null);
        }

        @Override
        public void warn(String msg, Object... arguments) {
            log(WARNING_ALL, expand(msg, arguments), null);
        }

        @Override
        public void warn(String msg, Throwable t) {
            log(WARNING_ALL, msg, t);
        }

        @Override
        public void error(String msg) {
            log(ERROR_ALL, msg, null);
        }

        @Override
        public void error(String msg, Object... arguments) {
            log(ERROR_ALL, expand(msg, arguments), null);
        }

        @Override
        public void error(String msg, Throwable t) {
            log(ERROR_ALL, msg, t);
        }

        private void log(OutputControllerLevel level, String msg, Throwable t) {
            final Header header = new Header(level, forClass);
            final MessageWithHeader message = new JavaMessage(header, msg, t);
            OUTPUT_CONTROLLER.log(message);
        }

        private String expand(String msg, Object[] args) {
            if (msg == null) {
                return "null";
            }

            if (args == null) {
                return msg;
            }

            final int argsLength = args.length;

            if (argsLength == 0) {
                return msg;
            }

            final StringBuilder result = new StringBuilder();
            int lastIdx = 0;
            for (Object arg : args) {
                final String argString = Objects.toString(arg);
                final int idx = result.indexOf("{}", lastIdx);

                if (idx < 0) {
                    break;
                }

                result.append(msg, lastIdx, idx).append(argString);
                lastIdx = idx + 2;
            }

            result.append(msg.substring(lastIdx));

            return result.toString();
        }
    }
}
