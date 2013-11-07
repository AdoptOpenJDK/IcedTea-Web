/* ProcessWrapper.java
Copyright (C) 2011,2012 Red Hat, Inc.

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

package net.sourceforge.jnlp;

import java.io.File;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import net.sourceforge.jnlp.browsertesting.ReactingProcess;
import org.junit.Assert;



/**
 * This class wraps execution of ThreadedProcess.
 * Add listeners and allows another setters, eg of ReactingProcess
 *
 */
public class ProcessWrapper {

    private List<String> args;
    private File dir;
    private final List<ContentReaderListener> stdoutl = new ArrayList<ContentReaderListener>(1);
    private final List<ContentReaderListener> stderrl = new ArrayList<ContentReaderListener>(1);
    private String[] vars;
    private ReactingProcess reactingProcess;

    public ProcessWrapper() {
    }

    public ProcessWrapper(String toBeExecuted, List<String> otherargs, URL u) {
        this(toBeExecuted, otherargs, u.toString());
    }

    public ProcessWrapper(String toBeExecuted, List<String> otherargs, String s) {
        Assert.assertNotNull(s);
        Assert.assertNotNull(toBeExecuted);
        Assert.assertTrue(toBeExecuted.trim().length() > 1);
        if (otherargs == null) {
            otherargs = new ArrayList<String>(1);
        }
        List<String> urledArgs = new ArrayList<String>(otherargs);
        urledArgs.add(0, toBeExecuted);
        urledArgs.add(s);
        this.args = urledArgs;
        this.vars=null;
    }

    public ProcessWrapper(String toBeExecuted, List<String> otherargs, URL u, ContentReaderListener stdoutl, ContentReaderListener stderrl, String[] vars) throws Exception {
        this(toBeExecuted, otherargs, u);
        this.addStdOutListener(stdoutl);
        this.addStdErrListener(stderrl);
        this.vars=vars;
    
    }

    public ProcessWrapper(String toBeExecuted, List<String> otherargs, URL u, List<ContentReaderListener> stdoutl, List<ContentReaderListener> stderrl, String[] vars) throws Exception {
        this(toBeExecuted, otherargs, u); 
        this.addStdOutListeners(stdoutl);
        this.addStdErrListeners(stderrl);
        this.vars=vars;    
    }

    ProcessWrapper(final List<String> args, File dir, ContentReaderListener stdoutl, ContentReaderListener stderrl, String[] vars) {
        this.args = args;
        this.dir = dir;
        this.addStdOutListener(stdoutl);
        this.addStdErrListener(stderrl);
        this.vars = vars;
    }

    public ProcessWrapper(final List<String> args, File dir, List<ContentReaderListener> stdoutl, List<ContentReaderListener> stderrl, String[] vars) {
        this.args = args;
        this.dir = dir;
        this.addStdOutListeners(stdoutl);
        this.addStdErrListeners(stderrl);
        this.vars = vars;
    }

    public final void addStdOutListener(ContentReaderListener l) {
        if (l == null) {
            return;
        }
        stdoutl.add(l);

    }

    public final void addStdErrListener(ContentReaderListener l) {
        if (l == null) {
            return;
        }
        stderrl.add(l);

    }

    public final void addStdOutListeners(List<ContentReaderListener> l) {
        if (l == null) {
            return;
        }
        stdoutl.addAll(l);

    }

    public final void addStdErrListeners(List<ContentReaderListener> l) {
        if (l == null) {
            return;
        }
        stderrl.addAll(l);

    }

    /**
     * @return the args
     */
    public List<String> getArgs() {
        return args;
    }

    /**
     * @param args the args to set
     */
    public void setArgs(List<String> args) {
        this.args = args;
    }

    /**
     * @return the dir
     */
    public File getDir() {
        return dir;
    }

    /**
     * @param dir the dir to set
     */
    public void setDir(File dir) {
        this.dir = dir;
    }

    /**
     * @return the stdoutl
     */
    public List<ContentReaderListener> getStdoutListeners() {
        return stdoutl;
    }

    /**
     * @return the stderrl
     */
    public List<ContentReaderListener> getStderrListeners() {
        return stderrl;
    }

    /**
     * @return the vars
     */
    public String[] getVars() {
        return vars;
    }

    /**
     * @param vars the vars to set
     */
    public void setVars(String[] vars) {
        this.vars = vars;
    }

    public ProcessResult execute() throws Exception {
        if (reactingProcess !=null ){
            reactingProcess.beforeProcess("");
        };
        ThreadedProcess t = new ThreadedProcess(args, dir, vars);
        if (ServerAccess.PROCESS_LOG) {
            String connectionMesaage = createConnectionMessage(t);
            ServerAccess.log(connectionMesaage, true, true);
        }
        ProcessAssasin pa = new ProcessAssasin(t, ServerAccess.PROCESS_TIMEOUT);
        t.setAssasin(pa);
        pa.setReactingProcess(reactingProcess);
        setUpClosingListener(stdoutl, pa, t);
        setUpClosingListener(stderrl, pa, t);
        pa.start();
        t.start();
        while (t.getP() == null && t.deadlyException == null) {
            Thread.sleep(100);
        }
        if (t.deadlyException != null) {
            pa.setCanRun(false);
            return new ProcessResult("", "", null, true, Integer.MIN_VALUE, t.deadlyException);
        }
        ContentReader crs = new ContentReader(t.getP().getInputStream(), stdoutl);
        ContentReader cre = new ContentReader(t.getP().getErrorStream(), stderrl);

        OutputStream out = t.getP().getOutputStream();
        if (out != null) {
            out.close();
        }

        new Thread(crs).start();
        new Thread(cre).start();
        while (t.isRunning()) {
            Thread.sleep(100);
        }

        while (!t.isDestoyed()) {
            Thread.sleep(100);
        }
        pa.setCanRun(false);
        // ServerAccess.logOutputReprint(t.getP().exitValue()); when process is killed, this throws exception

        ProcessResult pr = new ProcessResult(crs.getContent(), cre.getContent(), t.getP(), pa.wasTerminated(), t.getExitCode(), null);
        if (ServerAccess.PROCESS_LOG) {
            ServerAccess.log(pr.stdout, true, false);
            ServerAccess.log(pr.stderr, false, true);
        }
        if (reactingProcess != null) {
            reactingProcess.afterProcess("");
        };
        return pr;
    }

    private static void setUpClosingListener(List<ContentReaderListener> listeners, ProcessAssasin pa, ThreadedProcess t) {
        for (ContentReaderListener listener : listeners) {
            if (listener != null && (listener instanceof ClosingListener)) {
                ((ClosingListener) listener).setAssasin(pa);
                ((ClosingListener) listener).setProcess(t);
            }
        }

    }

    private static String createConnectionMessage(ThreadedProcess t) {
        return "Connecting " + t.getCommandLine();
    }
    
     void setReactingProcess(ReactingProcess reactingProcess) {
        this.reactingProcess = reactingProcess;
    }
}
