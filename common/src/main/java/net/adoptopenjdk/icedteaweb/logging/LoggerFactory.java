package net.adoptopenjdk.icedteaweb.logging;

/**
 * Factory for creating {@link Logger Loggers}.
 */
public class LoggerFactory {

    private static final String FACTORY_CLASS = "net.sourceforge.jnlp.util.logging.OutputControllerLoggerFactory";
    private static final LoggerFactoryImpl factory = initFactory();

    private static LoggerFactoryImpl initFactory() {
        try {
            final Class<?> factoryClass = LoggerFactory.class.getClassLoader().loadClass(FACTORY_CLASS);
            final LoggerFactoryImpl loggerFactory = (LoggerFactoryImpl) factoryClass.newInstance();
            loggerFactory.getLogger(LoggerFactory.class).debug("init logger factory to {}", loggerFactory);
            return loggerFactory;
        } catch (Exception e) {
            final SystemOutLoggerFactory loggerFactory = new SystemOutLoggerFactory();
            loggerFactory.getLogger(LoggerFactory.class).error("Falling back to SystemOutLogger", e);
            return loggerFactory;
        }
    }

    private LoggerFactory() {
        // do not instantiate this class
    }

    public static Logger getLogger(final Class<?> forClass) {
        return factory.getLogger(forClass);
    }

    public interface LoggerFactoryImpl {
        Logger getLogger(Class<?> forClass);
    }
}
