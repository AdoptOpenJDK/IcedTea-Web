package net.adoptopenjdk.icedteaweb.logging;

import net.adoptopenjdk.icedteaweb.logging.LoggerFactory.LoggerFactoryImpl;

import java.io.PrintStream;
import java.util.Date;

import static net.adoptopenjdk.icedteaweb.OutputUtils.exceptionToString;

/**
 * Implementation of Logger which loges to {@link System#out}.
 */
class SystemOutLoggerFactory implements LoggerFactoryImpl {

    private static final PrintStream out;

    static {
        final PrintStream tmp = System.out;
        if (tmp.getClass().getSimpleName().contains("TeeOutputStream")) {
            throw new RuntimeException("System.out is already a tee stream");
        }
        out = tmp;
    }

    @Override
    public Logger getLogger(final Class<?> forClass) {
        return new SystemOutLogger(forClass);
    }

    private static class SystemOutLogger extends BaseLogger {

        private static final String DEBUG =   "[DEBUG  ]";
        private static final String INFO =    "[INFO   ]";
        private static final String WARNING = "[WARNING]";
        private static final String ERROR =   "[ERROR  ]";

        private final String forClass;

        SystemOutLogger(final Class<?> forClass) {
            this.forClass = forClass.getName();
        }

        @Override
        public void debug(final String msg) {
            log(DEBUG, msg, null);
        }

        @Override
        public void debug(final String msg, final Object... arguments) {
            log(DEBUG, doExpand(msg, arguments), null);
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
            log(INFO, doExpand(msg, arguments), null);
        }

        @Override
        public void info(final String msg, final Throwable t) {
            log(INFO, msg, t);
        }

        @Override
        public void warn(final String msg) {
            log(WARNING, msg, null);
        }

        @Override
        public void warn(final String msg, final Object... arguments) {
            log(WARNING, doExpand(msg, arguments), null);
        }

        @Override
        public void warn(final String msg, final Throwable t) {
            log(WARNING, msg, t);
        }

        @Override
        public void error(final String msg) {
            log(ERROR, msg, null);
        }

        @Override
        public void error(final String msg, final Object... arguments) {
            log(ERROR, doExpand(msg, arguments), null);
        }

        @Override
        public void error(final String msg, final Throwable t) {
            log(ERROR, msg, t);
        }

        private void log(final String level, final String msg, final Throwable t) {
            final boolean isMultiLine = msg.contains("\n") || t != null;

            final String separator = isMultiLine ? "\n" : " ";
            final String header = new Date().toString() + " " + level + " " + forClass + ":";
            final String line = header + separator + msg + (t != null ? separator + exceptionToString(t) : "");

            out.println(line);
        }
    }
}
