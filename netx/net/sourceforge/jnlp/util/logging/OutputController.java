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
import java.util.LinkedList;
import java.util.List;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.logging.headers.Header;
import net.sourceforge.jnlp.util.logging.headers.JavaMessage;
import net.sourceforge.jnlp.util.logging.headers.MessageWithHeader;


/**
 * 
 * OutputController class (thread) must NOT call JNLPRuntime.getConfiguraion()
 * 
 */
public class OutputController {

   public static enum Level {

        MESSAGE_ALL, // - stdout/log in all cases
        MESSAGE_DEBUG, // - stdout/log in verbose/debug mode
        WARNING_ALL, // - stdout+stderr/log in all cases (default for
        WARNING_DEBUG, // - stdou+stde/logrr in verbose/debug mode
        ERROR_ALL, // - stderr/log in all cases (default for
        ERROR_DEBUG; // - stderr/log in verbose/debug mode
        //ERROR_DEBUG is default for Throwable
        //MESSAGE_DEBUG is default  for String
        
        public  boolean isOutput() {
            return this == Level.MESSAGE_ALL
                    || this == Level.MESSAGE_DEBUG
                    || this == Level.WARNING_ALL
                    || this == Level.WARNING_DEBUG;
        }

        public  boolean isError() {
            return this == Level.ERROR_ALL
                    || this == Level.ERROR_DEBUG
                    || this == Level.WARNING_ALL
                    || this == Level.WARNING_DEBUG;
        }
        
        public  boolean isWarning() {
            return this == Level.WARNING_ALL
                    || this == Level.WARNING_DEBUG;
        }

         public  boolean isDebug() {
            return this == Level.ERROR_DEBUG
                    || this == Level.MESSAGE_DEBUG
                    || this == Level.WARNING_DEBUG;
        }

        public  boolean isInfo() {
            return this == Level.ERROR_ALL
                    || this == Level.WARNING_ALL
                    || this == Level.MESSAGE_ALL;
        }
    }

    /*
     * singleton instance
     */
    private static final String NULL_OBJECT = "Trying to log null object";
    private PrintStreamLogger outLog;
    private PrintStreamLogger errLog;
    private List<MessageWithHeader> messageQue = new LinkedList<MessageWithHeader>();
    private MessageQueConsumer messageQueConsumer = new MessageQueConsumer();
    Thread consumerThread;

    //bounded to instance
    private class MessageQueConsumer implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    synchronized (OutputController.this) {
                        OutputController.this.wait(1000);
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
        MessageWithHeader s = messageQue.get(0);
        messageQue.remove(0);
        //filtering is done in console during runtime
        if (LogConfig.getLogConfig().isLogToConsole()) {
            JavaConsole.getConsole().addMessage(s);
        }
        //clients app's messages are reprinted only to console
        if (s.getHeader().isClientApp){
            return;
        }
        if (!JNLPRuntime.isDebug() && (s.getHeader().level == Level.MESSAGE_DEBUG
                || s.getHeader().level == Level.WARNING_DEBUG
                || s.getHeader().level == Level.ERROR_DEBUG)) {
            //filter out debug messages
            //must be here to prevent deadlock, casued by exception form jnlpruntime, loggers or configs themselves
            return;
        }
        String message = s.getMessage();
        if (LogConfig.getLogConfig().isEnableHeaders()) {
            if (message.contains("\n")) {
                message = s.getHeader().toString() + "\n" + message;
            } else {
                message = s.getHeader().toString() + " " + message;
            }
        }
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
        //only java messages handled here, plugin is onhis own
        if (LogConfig.getLogConfig().isLogToSysLog() && 
                (s.getHeader().level.equals(Level.ERROR_ALL) || s.getHeader().level.equals(Level.WARNING_ALL)) &&
                s.getHeader().isC == false) {
            //no headers here
            getSystemLog().log(s.getMessage());
        }

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
        if (out == null || err == null) {
            throw new IllegalArgumentException("No stream can be null");
        }
        outLog = new PrintStreamLogger(out);
        errLog = new PrintStreamLogger(err);
        //itw logger have to be fully initialised before start
        consumerThread = new Thread(messageQueConsumer, "Output controller consumer daemon");
        consumerThread.setDaemon(true);
        //is started in JNLPRuntime.getConfig() after config is laoded
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                flush();
            }
        }));
    }
     
    public void startConsumer() {
        consumerThread.start();
        //some messages were probably posted before start of consumer
        synchronized (this) {
            this.notifyAll();
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

    private void log(Level level, Object o) {
        String s ="";
        if (o == null) {
            s = NULL_OBJECT;
        } else if (o instanceof Throwable) {
            s = exceptionToString((Throwable) o);
        } else {
            s=o.toString();
        }
        log(new JavaMessage(new Header(level, false), s));
    }

    synchronized void log(MessageWithHeader l){
        messageQue.add(l);
        this.notifyAll();
    }
    
    

    private static class FileLogHolder {
        
        //https://en.wikipedia.org/wiki/Double-checked_locking#Usage_in_Java
        //https://en.wikipedia.org/wiki/Initialization_on_demand_holder_idiom
        private static volatile FileLog INSTANCE = new FileLog();
    }
    private FileLog getFileLog() {
        return FileLogHolder.INSTANCE;
    }

    private static class SystemLogHolder {

        //https://en.wikipedia.org/wiki/Double-checked_locking#Usage_in_Java
        //https://en.wikipedia.org/wiki/Initialization_on_demand_holder_idiom
        private static volatile SingleStreamLogger INSTANCE = initSystemLogger();

        private static SingleStreamLogger initSystemLogger() {
            if (JNLPRuntime.isWindows()) {
                return new WinSystemLog();
            } else {
                return new UnixSystemLog();
            }
        }
    }

    private SingleStreamLogger getSystemLog() {
        return SystemLogHolder.INSTANCE;
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
    
   //package private setters for testing

    void setErrLog(PrintStreamLogger errLog) {
        this.errLog = errLog;
    }

    void setFileLog(FileLog fileLog) {
        FileLogHolder.INSTANCE = fileLog;
    }

    void setOutLog(PrintStreamLogger outLog) {
        this.outLog = outLog;
    }

    void setSysLog(SingleStreamLogger sysLog) {
        SystemLogHolder.INSTANCE = sysLog;
    }
    
    
}
