package net.adoptopenjdk.icedteaweb.logging;

/**
 * Factory for creating {@link Logger Loggers}.
 */
public class LoggerFactory {

    private static final String FACTORY_CLASS = "net.sourceforge.jnlp.util.logging.OutputControllerLoggerFactory";
    private static final LoggerFactoryImpl factory;

    static {
        Exception ex = null;
        LoggerFactoryImpl loggerFactory;
        try {
            final Class<?> factoryClass = ClassLoader.getSystemClassLoader().loadClass(FACTORY_CLASS);
            loggerFactory = (LoggerFactoryImpl) factoryClass.newInstance();
        } catch (Exception e) {
            ex = e;
            loggerFactory = new SystemOutLoggerFactory();
        }
        factory = loggerFactory;

        // one can only get a logger after the factory has been set.
        // therefore we postpone all the logging to after this comment

        final Logger LOG = factory.getLogger(LoggerFactory.class);
        if (ex != null) {
            LOG.error("Falling back to SystemOutLogger", ex);
        } else {
            LOG.debug("init logger factory to {}", factory);
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
