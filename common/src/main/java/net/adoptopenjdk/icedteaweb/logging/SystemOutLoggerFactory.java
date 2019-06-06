package net.adoptopenjdk.icedteaweb.logging;

import net.adoptopenjdk.icedteaweb.logging.LoggerFactory.LoggerFactoryImpl;

import java.util.Date;

import static net.adoptopenjdk.icedteaweb.OutputUtils.exceptionToString;

/**
 * Implementation of Logger which loges to {@link System#out}.
 */
class SystemOutLoggerFactory implements LoggerFactoryImpl {

    @Override
    public Logger getLogger(Class<?> forClass) {
        return new SystemOutLogger(forClass);
    }

    private static class SystemOutLogger extends BaseLogger {

        private static final String DEBUG = "DEBUG";
        private static final String INFO = "INFO";
        private static final String WARNING = "WARNING";
        private static final String ERROR = "ERROR";

        private final String forClass;

        public SystemOutLogger(Class<?> forClass) {
            this.forClass = forClass.getName();
        }

        @Override
        public void debug(String msg) {
            log(DEBUG, msg, null);
        }

        @Override
        public void debug(String msg, Object... arguments) {
            log(DEBUG, expand(msg, arguments), null);
        }

        @Override
        public void debug(String msg, Throwable t) {
            log(DEBUG, msg, t);
        }

        @Override
        public void info(String msg) {
            log(INFO, msg, null);
        }

        @Override
        public void info(String msg, Object... arguments) {
            log(INFO, expand(msg, arguments), null);
        }

        @Override
        public void info(String msg, Throwable t) {
            log(INFO, msg, t);
        }

        @Override
        public void warn(String msg) {
            log(WARNING, msg, null);
        }

        @Override
        public void warn(String msg, Object... arguments) {
            log(WARNING, expand(msg, arguments), null);
        }

        @Override
        public void warn(String msg, Throwable t) {
            log(WARNING, msg, t);
        }

        @Override
        public void error(String msg) {
            log(ERROR, msg, null);
        }

        @Override
        public void error(String msg, Object... arguments) {
            log(ERROR, expand(msg, arguments), null);
        }

        @Override
        public void error(String msg, Throwable t) {
            log(ERROR, msg, t);
        }

        private void log(String level, String msg, Throwable t) {
            final boolean isMultiLine = msg.contains("\n") || t != null;

            final String separator = isMultiLine ? "\n" : " ";
            final String header = new Date().toString() + " " + level + " " + forClass + ":";
            final String line = header + separator + msg + (t != null ? separator + exceptionToString(t) : "");

            System.out.println(line);
        }
    }
}
