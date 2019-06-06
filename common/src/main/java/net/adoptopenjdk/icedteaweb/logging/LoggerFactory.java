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
            return (LoggerFactoryImpl) factoryClass.newInstance();
        } catch (Exception e) {
            return new SystemOutLoggerFactory();
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
