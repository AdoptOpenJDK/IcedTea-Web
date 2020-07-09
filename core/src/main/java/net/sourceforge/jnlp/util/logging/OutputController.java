/*Copyright (C) 2013 Red Hat, Inc.

 This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify it under the
terms of the GNU General Public License as published by the Free Software
Foundation, version 2.

IcedTea is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
IcedTea; see the file COPYING. If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is making a
combined work based on this library. Thus, the terms and conditions of the GNU
General Public License cover the whole combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent modules, and
to copy and distribute the resulting executable under terms of your choice,
provided that you also meet, for each linked independent module, the terms and
conditions of the license of that module. An independent module is a module
which is not derived from or based on this library. If you modify this library,
you may extend this exception to your version of the library, but you are not
obligated to do so. If you do not wish to do so, delete this exception
statement from your version.
*/
package net.sourceforge.jnlp.util.logging;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.client.console.JavaConsole;
import net.adoptopenjdk.icedteaweb.os.OsUtil;
import net.sourceforge.jnlp.config.PathsAndFiles;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.logging.headers.Header;
import net.sourceforge.jnlp.util.logging.headers.MessageWithHeader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import static net.sourceforge.jnlp.util.logging.OutputControllerLevel.DEBUG;
import static net.sourceforge.jnlp.util.logging.OutputControllerLevel.ERROR;
import static net.sourceforge.jnlp.util.logging.OutputControllerLevel.WARN;

/**
 * OutputController class (thread) must NOT call JNLPRuntime.getConfiguration()
 */
public class OutputController implements BasicOutputController {

    private StdInOutErrController inOutErrController;

    private final List<MessageWithHeader> messageQue = new LinkedList<>();
    //itw logger have to be fully initialised before start
    private final Thread consumerThread = new Thread(new MessageQueConsumer(), "Output controller consumer daemon");
    private final Thread shutdownThread = new Thread(this::flush);
    private boolean javaConsoleInitialized;

    //bounded to instance
    private class MessageQueConsumer implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    synchronized (OutputController.this) {
                        if (!messageQue.isEmpty()) {
                            flush();
                        }
                        OutputController.this.wait(1000);
                    }

                } catch (Throwable t) {
                    // we must not use the logging framework as this would add a new message to the queue
                    // which most likely will end also in this catch block thus creating an endless loop
                    final File logDir = PathsAndFiles.LOG_DIR.getFile();
                    final File file = new File(logDir, "log-trace-" + System.currentTimeMillis() + ".log");
                    try (FileOutputStream out = new FileOutputStream(file)) {
                        out.write("Exception while logging".getBytes());
                        t.printStackTrace(new PrintStream(out));
                    } catch (final Exception ignored) {
                    }
                }
            }
        }
    }

    public synchronized void flush() {
        while (!messageQue.isEmpty()) {
            consume();
        }
    }

    private void consume() {
        final MessageWithHeader message = messageQue.remove(0);

        if (LogConfig.getLogConfig().isLogToConsole() && javaConsoleInitialized) {
            //filtering is done in console during runtime
            JavaConsole.getConsole().addMessage(message);
        }

        if (message.getHeader().isClientApp) {
            consumeClientAppMessage(message);
        } else {
            consumeItwMessage(message);
        }
    }

    private void consumeClientAppMessage(MessageWithHeader message) {
        final LogConfig logConfig = LogConfig.getLogConfig();
        if (logConfig.isLogToFile() && logConfig.isLogToFileForClientApp()) {
            getAppFileLog().log(convertToPrintableString(message, logConfig));
        }
    }

    private void consumeItwMessage(MessageWithHeader message) {
        final LogConfig logConfig = LogConfig.getLogConfig();
        final Header header = message.getHeader();
        final OutputControllerLevel level = header.level;

        if (!JNLPRuntime.isDebug() && level == DEBUG) {
            return;
        }

        final String messageString = convertToPrintableString(message, logConfig);

        if (logConfig.isLogToStreams()) {
            if (level.printToOutStream()) {
                inOutErrController.getOut().log(messageString);
            }
            if (level.printToErrStream()) {
                inOutErrController.getErr().log(messageString);
            }
        }

        if (logConfig.isLogToFile()) {
            getFileLog().log(messageString);
        }

        //only crucial stuff is going to system log
        //only java messages handled here, plugin is on his own
        if (logConfig.isLogToSysLog() && (level == ERROR || level == WARN)) {
            //no headers here
            getSystemLog().log(message.getMessage());
        }
    }

    private String convertToPrintableString(MessageWithHeader s, LogConfig logConfig) {
        final boolean withHeaders = logConfig.isEnableHeaders();
        final boolean withStackTrace = s.hasStackTrace();
        final boolean isMultiLine = (withHeaders && s.getMessage().contains("\n")) || withStackTrace;

        final String separator = isMultiLine ? "\n" : " ";

        final Header header = s.getHeader();
        return (withHeaders ? header.toString() : header.toShortString()) + separator +
                s.getMessage() +
                (withStackTrace ? separator + s.getStackTrace() : "");
    }

    private static class OutputControllerHolder {
        //https://en.wikipedia.org/wiki/Initialization_on_demand_holder_idiom
        //https://en.wikipedia.org/wiki/Double-checked_locking#Usage_in_Java
        private static final OutputController INSTANCE = new OutputController(StdInOutErrController.getInstance());
    }

    /**
     * This should be the only legal way to get logger for ITW
     *
     * @return logging singleton
     */
    public static OutputController getLogger() {
        return OutputControllerHolder.INSTANCE;
    }

    /**
     * for testing purposes the logger with custom streams can be created
     * otherwise only getLogger()'s singleton can be called.
     */
    public OutputController(OutputStream out, OutputStream err) {
        this(new StdInOutErrController(out, err));
    }

    /**
     * for testing purposes the logger with custom streams can be created
     * otherwise only getLogger()'s singleton can be called.
     */
    private OutputController(StdInOutErrController inOutErrController) {
        this.inOutErrController = Assert.requireNonNull(inOutErrController, "inOutErrController");

        // the consumer thread is started in JNLPRuntime.getConfig() after config is loaded
        consumerThread.setDaemon(true);

        Runtime.getRuntime().addShutdownHook(shutdownThread);
    }

    public void startConsumer() {
        initJavaConsole();
        consumerThread.start();
    }

    private void initJavaConsole() {
        // only if logging to console and not already shutting down
        if (Thread.currentThread() != shutdownThread) {
            LogConfig.resetLogConfig();
            if (LogConfig.getLogConfig().isLogToConsole()) {
                javaConsoleInitialized = true;
                Assert.requireNonNull(JavaConsole.getConsole(), "console");
            }
        }
    }

    /**
     * Some tests may require set the output/error stream and check the output.
     * This is the gate for it.
     */
    public void setInOutErrController(StdInOutErrController inOutErrController) {
        this.inOutErrController = Assert.requireNonNull(inOutErrController, "inOutErrController");
    }

    @Override
    public synchronized void log(MessageWithHeader l) {
        messageQue.add(l);
        this.notifyAll();
    }

    private static class FileLogHolder {

        //https://en.wikipedia.org/wiki/Double-checked_locking#Usage_in_Java
        //https://en.wikipedia.org/wiki/Initialization_on_demand_holder_idiom
        private static volatile SingleStreamLogger INSTANCE = FileLog.createFileLog();
    }

    private SingleStreamLogger getFileLog() {
        return FileLogHolder.INSTANCE;
    }


    private static class AppFileLogHolder {

        //https://en.wikipedia.org/wiki/Double-checked_locking#Usage_in_Java
        //https://en.wikipedia.org/wiki/Initialization_on_demand_holder_idiom
        private static final SingleStreamLogger INSTANCE = FileLog.createAppFileLog();
    }

    private SingleStreamLogger getAppFileLog() {
        return AppFileLogHolder.INSTANCE;
    }

    private static class SystemLogHolder {

        //https://en.wikipedia.org/wiki/Double-checked_locking#Usage_in_Java
        //https://en.wikipedia.org/wiki/Initialization_on_demand_holder_idiom
        private static final SingleStreamLogger INSTANCE = initSystemLogger();

        private static SingleStreamLogger initSystemLogger() {
            if (OsUtil.isWindows()) {
                return new WinSystemLog();
            } else {
                return new UnixSystemLog();
            }
        }
    }

    private SingleStreamLogger getSystemLog() {
        return SystemLogHolder.INSTANCE;
    }

    public void printOutLn(String e) {
        flush();
        inOutErrController.getOutStream().println(e);
    }

    public String readLine() throws IOException {
        return inOutErrController.readLine();
    }

    //package private setters for testing

    void setFileLog(SingleStreamLogger fileLog) {
        FileLogHolder.INSTANCE = fileLog;
    }
}
