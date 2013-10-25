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

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

public class OutputController {

    public static enum Level {

        MESSAGE_ALL, // - stdout/log in all cases
        MESSAGE_DEBUG, // - stdout/log in verbose/debug mode
        WARNING_ALL, // - stdout+stderr/log in all cases (default for
        WARNING_DEBUG, // - stdou+stde/logrr in verbose/debug mode
        ERROR_ALL, // - stderr/log in all cases (default for
        ERROR_DEBUG, // - stderr/log in verbose/debug mode
        //ERROR_DEBUG is default for Throwable
        //MESSAGE_VERBOSE is defautrl  for String
    }

    private static final class MessageWithLevel {

        public final String message;
        public final Level level;
        public final StackTraceElement[] stack = Thread.currentThread().getStackTrace();

        public MessageWithLevel(String message, Level level) {
            this.message = message;
            this.level = level;
        }
    }
    /*
     * singleton instance
     */
    private static OutputController logger;
    private static final String NULL_OBJECT = "Trying to log null object";
    private FileLog fileLog;
    private PrintStreamLogger outLog;
    private PrintStreamLogger errLog;
    private SingleStreamLogger sysLog;
    private List<MessageWithLevel> messageQue = new LinkedList<MessageWithLevel>();
    private MessageQueConsumer messageQueConsumer = new MessageQueConsumer();

    //bounded to instance
    private class MessageQueConsumer implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    synchronized (OutputController.this) {
                        OutputController.this.wait();
                        if (!(OutputController.this == null || messageQue.isEmpty())) {
                            flush();
                        }
                    }

                } catch (Throwable t) {
                    OutputController.getLogger().log(t);
                }
            }
        }
    };

    public synchronized void flush() {

        while (!messageQue.isEmpty()) {
            consume();
        }
    }
    
    public void close() {
        flush();
        if (LogConfig.getLogConfig().isLogToFile()){
            getFileLog().close();
        }
    }

    private void consume() {
        MessageWithLevel s = messageQue.get(0);
        messageQue.remove(0);
        if (!JNLPRuntime.isDebug() && (s.level == Level.MESSAGE_DEBUG
                || s.level == Level.WARNING_DEBUG
                || s.level == Level.ERROR_DEBUG)) {
            //filter out debug messages
            //must be here to prevent deadlock, casued by exception form jnlpruntime, loggers or configs themselves
            return;
        }
        String message = s.message;
        if (LogConfig.getLogConfig().isEnableHeaders()) {
            if (message.contains("\n")) {
                message = getHeader(s.level, s.stack) + "\n" + message;
            } else {
                message = getHeader(s.level, s.stack) + " " + message;
            }
        }
        if (LogConfig.getLogConfig().isLogToStreams()) {
            if (s.level == Level.MESSAGE_ALL
                    || s.level == Level.MESSAGE_DEBUG
                    || s.level == Level.WARNING_ALL
                    || s.level == Level.WARNING_DEBUG) {
                outLog.log(message);
            }
            if (s.level == Level.ERROR_ALL
                    || s.level == Level.ERROR_DEBUG
                    || s.level == Level.WARNING_ALL
                    || s.level == Level.WARNING_DEBUG) {
                errLog.log(message);
            }
        }
        if (LogConfig.getLogConfig().isLogToFile()) {
            getFileLog().log(message);
        }
        if (LogConfig.getLogConfig().isLogToSysLog()) {
            getSystemLog().log(message);
        }

    }

    private OutputController() {
        this(System.out, System.err);
    }

    /**
     * This should be the only legal way to get logger for ITW
     *
     * @return logging singleton
     */
    synchronized public static OutputController getLogger() {
        if (logger == null) {
            logger = new OutputController();
        }
        return logger;
    }

    /**
     * for testing purposes the logger with custom streams can be created
     * otherwise only getLogger()'s singleton can be called.
     */
     public OutputController(PrintStream out, PrintStream err) {
        if (out == null || err == null) {
            throw new IllegalArgumentException("No stream can be null");
        }
        outLog = new PrintStreamLogger(out);
        errLog = new PrintStreamLogger(err);
        //itw logger have to be fully initialised before start
        Thread t = new Thread(messageQueConsumer);
        t.setDaemon(true);
        t.start();
        //some messages were probably posted before start of consumer
        synchronized (this){
            this.notifyAll();
        }
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                while (!messageQue.isEmpty()) {
                    consume();
                }
            }
        }));
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

    public static String exceptionToString(Throwable t) {
        if (t == null) {
            return null;
        }
        String s = "Error during processing of exception";
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            s = sw.toString();
            pw.close();
            sw.close();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return s;
    }

    public void log(Level level, String s) {
        log(level, (Object) s);
    }

    public void log(Level level, Throwable s) {
        log(level, (Object) s);
    }

    public void log(String s) {
        log(Level.MESSAGE_DEBUG, (Object) s);
    }

    public void log(Throwable s) {
        log(Level.ERROR_DEBUG, (Object) s);
    }

    private synchronized void log(Level level, Object o) {
        if (o == null) {
            messageQue.add(new MessageWithLevel(NULL_OBJECT, level));
        } else if (o instanceof Throwable) {
            messageQue.add(new MessageWithLevel(exceptionToString((Throwable) o), level));
        } else {
            messageQue.add(new MessageWithLevel(o.toString(), level));
        }
        this.notifyAll();
    }

    private FileLog getFileLog() {
        if (fileLog == null) {
            fileLog = new FileLog();
        }
        return fileLog;
    }

    private SingleStreamLogger getSystemLog() {
        if (sysLog == null) {
            if (JNLPRuntime.isWindows()) {
                sysLog = new WinSystemLog();
            } else {
                sysLog = new UnixSystemLog();
            }
        }
        return sysLog;
    }

    public void printErrorLn(String e) {
        getErr().println(e);

    }

    public void printOutLn(String e) {
        getOut().println(e);

    }

    public void printWarningLn(String e) {
        printOutLn(e);
        printErrorLn(e);
    }

    public void printError(String e) {
        getErr().print(e);

    }

    public void printOut(String e) {
        getOut().print(e);

    }

    public void printWarning(String e) {
        printOut(e);
        printError(e);
    }

    public static String getHeader(Level level, StackTraceElement[] stack) {
        StringBuilder sb = new StringBuilder();
        try {
            String user = System.getProperty("user.name");
            sb.append("[").append(user).append("]");
            if (JNLPRuntime.isWebstartApplication()) {
                sb.append("[ITW-JAVAWS]");
            } else {
                sb.append("[ITW]");
            }
            if (level != null) {
                sb.append('[').append(level.toString()).append(']');
            }
            sb.append('[').append(new Date().toString()).append(']');
            if (stack != null) {
                sb.append('[').append(getCallerClass(stack)).append(']');
            }
        } catch (Exception ex) {
            getLogger().log(ex);
        }
        return sb.toString();

    }

    static String getCallerClass(StackTraceElement[] stack) {
        try {
            //0 is always thread
            //1..? is OutputController itself
            //pick up first after.
            StackTraceElement result = stack[0];
            int i = 1;
            for (; i < stack.length; i++) {
                result = stack[i];//at least moving up
                if (stack[i].getClassName().contains(OutputController.class.getName()) ||
                    //PluginDebug.class.getName() not avaiable during netx make
                    stack[i].getClassName().contains("sun.applet.PluginDebug") ) {
                    continue;
                } else {
                    break;
                }
            }
            return result.toString();
        } catch (Exception ex) {
            getLogger().log(ex);
            return "Unknown caller";
        }
    }
    
    
   //package private setters for testing

    void setErrLog(PrintStreamLogger errLog) {
        this.errLog = errLog;
    }

    void setFileLog(FileLog fileLog) {
        this.fileLog = fileLog;
    }

    void setOutLog(PrintStreamLogger outLog) {
        this.outLog = outLog;
    }

    void setSysLog(SingleStreamLogger sysLog) {
        this.sysLog = sysLog;
    }
    
    
}
