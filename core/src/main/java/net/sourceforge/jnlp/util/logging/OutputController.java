/*Copyright (C) 2013 Red Hat, Inc.

 This file is part of IcedTea.

 IcedTea is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by
 the Free Software Foundation, version 2.

 IcedTea is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with IcedTea; see the file COPYING.  If not, write to
 the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 02110-1301 USA.

 Linking this library statically or dynamically with other modules is
 making a combined work based on this library.  Thus, the terms and
 conditions of the GNU General Public License cover the whole
 combination.

 As a special exception, the copyright holders of this library give you
 permission to link this library with independent modules to produce an
 executable, regardless of the license terms of these independent
 modules, and to copy and distribute the resulting executable under
 terms of your choice, provided that you also meet, for each linked
 independent module, the terms and conditions of the license of that
 module.  An independent module is a module which is not derived from
 or based on this library.  If you modify this library, you may extend
 this exception to your version of the library, but you are not
 obligated to do so.  If you do not wish to do so, delete this
 exception statement from your version.
 */
package net.sourceforge.jnlp.util.logging;

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.OutputUtils;
import net.adoptopenjdk.icedteaweb.client.console.JavaConsole;
import net.adoptopenjdk.icedteaweb.os.OsUtil;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.logging.headers.Header;
import net.sourceforge.jnlp.util.logging.headers.JavaMessage;
import net.sourceforge.jnlp.util.logging.headers.MessageWithHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import static java.util.Objects.requireNonNull;


/**
 * 
 * OutputController class (thread) must NOT call JNLPRuntime.getConfiguration()
 * 
 */
public class OutputController {

    private final static Logger LOG = LoggerFactory.getLogger(OutputController.class);

    private static final String NULL_OBJECT = "Trying to log null object";

    private final PrintStreamLogger outLog;
    private final PrintStreamLogger errLog;
    private final List<MessageWithHeader> messageQue = new LinkedList<>();
    //itw logger have to be fully initialised before start
    private final Thread consumerThread = new Thread(new MessageQueConsumer(), "Output controller consumer daemon");
    private final Thread shutdownThread = new Thread(this::flush);
    private boolean javaConsoleInitialized;
     /*stdin reader for headless dialogues*/
    private BufferedReader br;

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
                    LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, t);
                }
            }
        }
    }

    public synchronized void flush() {

        while (!messageQue.isEmpty()) {
            consume();
        }
    }
    
    public void close() throws Exception {
        flush();
        if (LogConfig.getLogConfig().isLogToFile()){
            getFileLog().close();
        }
    }

    private void consume() {
        MessageWithHeader s = messageQue.get(0);
        messageQue.remove(0);
        //filtering is done in console during runtime
        if (LogConfig.getLogConfig().isLogToConsole() && javaConsoleInitialized) {
            JavaConsole.getConsole().addMessage(s);
        }
        //clients app's messages are reprinted only to console
        if (s.getHeader().isClientApp){
            if (LogConfig.getLogConfig().isLogToFile() && LogConfig.getLogConfig().isLogToFileForClientApp()) {
                getAppFileLog().log(proceedHeader(s));
            }
            return;
        }
        if (!JNLPRuntime.isDebug() && (s.getHeader().level == OutputControllerLevel.MESSAGE_DEBUG
                || s.getHeader().level == OutputControllerLevel.WARNING_DEBUG
                || s.getHeader().level == OutputControllerLevel.ERROR_DEBUG)) {
            //filter out debug messages
            //must be here to prevent deadlock, caused by exception form jnlpruntime, loggers or configs themselves
            return;
        }
        String message = proceedHeader(s);
        if (LogConfig.getLogConfig().isLogToStreams()) {
            if (s.getHeader().level.isOutput()) {
                outLog.log(message);
            }
            if (s.getHeader().level.isError()) {
                errLog.log(message);
            }
        }
        if (LogConfig.getLogConfig().isLogToFile()) {
            getFileLog().log(message);
        }
        //only crucial stuff is going to system log
        //only java messages handled here, plugin is on his own
        if (LogConfig.getLogConfig().isLogToSysLog() && 
                (s.getHeader().level.equals(OutputControllerLevel.ERROR_ALL) || s.getHeader().level.equals(OutputControllerLevel.WARNING_ALL)) &&
                !s.getHeader().isC) {
            //no headers here
            getSystemLog().log(s.getMessage());
        }

    }

    private String proceedHeader(MessageWithHeader s) {
        String message = s.getMessage();
        if (LogConfig.getLogConfig().isEnableHeaders()) {
            if (message.contains("\n")) {
                message = s.getHeader().toString() + "\n" + message;
            } else {
                message = s.getHeader().toString() + " " + message;
            }
        }
        return message;
    }

    private OutputController() {
        this(System.out, System.err);
    }
    
    
    private static class OutputControllerHolder {

        //https://en.wikipedia.org/wiki/Initialization_on_demand_holder_idiom
        //https://en.wikipedia.org/wiki/Double-checked_locking#Usage_in_Java
        private static final OutputController INSTANCE = new OutputController();
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
    public OutputController(PrintStream out, PrintStream err) {
        outLog = new PrintStreamLogger(requireNonNull(out, "out"));
        errLog = new PrintStreamLogger(requireNonNull(err, "err"));

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
                JavaConsole.getConsole();
            }
        }
    }

    /**
     *
     * @return current stream for std.out reprint
     */
    public PrintStream getOut() {
        flush();
        return outLog.getStream();
    }

    /**
     *
     * @return current stream for std.err reprint
     */
    public PrintStream getErr() {
        flush();
        return errLog.getStream();
    }

    /**
     * Some tests may require set the output stream and check the output. This
     * is the gate for it.
     */
    public void setOut(PrintStream out) {
        flush();
        this.outLog.setStream(out);
    }

    /**
     * Some tests may require set the output stream and check the output. This
     * is the gate for it.
     */
    public void setErr(PrintStream err) {
        flush();
        this.errLog.setStream(err);
    }

    public void log(OutputControllerLevel level, String s) {
        log(level, (Object) s);
    }


    private void log(OutputControllerLevel level, Object o) {
        String s ="";
        if (o == null) {
            s = NULL_OBJECT;
        } else if (o instanceof Throwable) {
            s = OutputUtils.exceptionToString((Throwable) o);
        } else {
            s=o.toString();
        }
        log(new JavaMessage(new Header(level, false), s));
    }

    public synchronized void log(MessageWithHeader l){
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

    private void printErrorLn(String e) {
        getErr().println(e);

    }

    public void printOutLn(String e) {
        getOut().println(e);

    }

    public void printWarningLn(String e) {
        printOutLn(e);
        printErrorLn(e);
    }

    private void printError(String e) {
        getErr().print(e);

    }

    public void printOut(String e) {
        getOut().print(e);

    }


   //package private setters for testing

    void setFileLog(SingleStreamLogger fileLog) {
        FileLogHolder.INSTANCE = fileLog;
    }

    public synchronized String readLine() throws IOException {
        if (br == null) {
            br = new BufferedReader(new InputStreamReader(System.in));
        }
        return br.readLine();
    }
    
    
}
