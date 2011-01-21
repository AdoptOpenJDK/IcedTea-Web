package net.sourceforge.jnlp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.XMLFormatter;

import net.sourceforge.jnlp.util.FileUtils;

/**
 * This class writes log information to file.
 * 
 * @author Andrew Su (asu@redhat.com, andrew.su@utoronto.ca)
 * 
 */
class AppletLog extends Log {
    private static Logger logger;
    static {
        try {
            // If logging is enabled, we create logger.
            if (enableLogging) {
                String fn = icedteaLogDir + "plugin" + java.lang.System.currentTimeMillis() + ".log";
                FileUtils.createRestrictedFile(new File(fn), true);
                FileHandler fh = new FileHandler(fn, false);
                fh.setFormatter(new XMLFormatter());
                String logClassName = AppletLog.class.getName();
                logger = Logger.getLogger(logClassName);
                logger.setLevel(Level.ALL);
                logger.addHandler(fh);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private AppletLog() {
    }

    /**
     * Log the exception to file.
     * 
     * @param e Exception that was thrown.
     */
    public synchronized static void log(Throwable e) {
        if (enableLogging && logger != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            e.printStackTrace(ps);
            logger.log(Level.FINE, baos.toString());
        }
    }
}
