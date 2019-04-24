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

package net.adoptopenjdk.icedteaweb.testing;

import net.adoptopenjdk.icedteaweb.testing.browsertesting.ReactingProcess;
import org.junit.Assert;

import java.io.File;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;



/**
 * This class wraps execution of ThreadedProcess.
 * Add listeners and allows another setters, eg of ReactingProcess
 *
 */
public class ProcessWrapper {

    private final List<String> args;
    private File dir;
    private final List<ContentReaderListener> stdoutl = new ArrayList<>(1);
    private final List<ContentReaderListener> stderrl = new ArrayList<>(1);
    private String[] vars;
    private ReactingProcess reactingProcess;



    public ProcessWrapper(String toBeExecuted, List<String> otherargs, URL u) {
        this(toBeExecuted, otherargs, u.toString());
    }

    private ProcessWrapper(String toBeExecuted, List<String> otherargs, String s) {
        Assert.assertNotNull(s);
        Assert.assertNotNull(toBeExecuted);
        Assert.assertTrue(toBeExecuted.trim().length() > 1);
        if (otherargs == null) {
            otherargs = new ArrayList<>(1);
        }
        List<String> urledArgs = new ArrayList<>(otherargs);
        urledArgs.add(0, toBeExecuted);
        urledArgs.add(s);
        this.args = urledArgs;
        this.vars=null;
    }

    public ProcessWrapper(String toBeExecuted, List<String> otherargs, URL u, ContentReaderListener stdoutl, ContentReaderListener stderrl, String[] vars) {
        this(toBeExecuted, otherargs, u);
        this.addStdOutListener(stdoutl);
        this.addStdErrListener(stderrl);
        this.vars=vars;
    
    }

    public ProcessWrapper(final List<String> args, File dir, ContentReaderListener stdoutl, ContentReaderListener stderrl) {
        this.args = args;
        this.dir = dir;
        this.addStdOutListener(stdoutl);
        this.addStdErrListener(stderrl);
        this.vars = null;
    }


    private void addStdOutListener(ContentReaderListener l) {
        if (l == null) {
            return;
        }
        stdoutl.add(l);

    }

    private void addStdErrListener(ContentReaderListener l) {
        if (l == null) {
            return;
        }
        stderrl.add(l);

    }

    public ProcessResult execute() throws Exception {
        if (reactingProcess !=null ){
            reactingProcess.beforeProcess("");
        }
        ThreadedProcess t = new ThreadedProcess(args, dir, vars);
        if (ServerAccess.PROCESS_LOG) {
            String connectionMessage = createConnectionMessage(t);
            ServerAccess.log(connectionMessage, true, true);
        }
        ProcessAssassin pa = new ProcessAssassin(t, ServerAccess.PROCESS_TIMEOUT);
        t.setAssassin(pa);
        pa.setReactingProcess(reactingProcess);
        setUpClosingListener(stdoutl, pa);
        setUpClosingListener(stderrl, pa);
        t.setWriter(null);
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

        while (!t.isDestroyed()) {
            Thread.sleep(100);
        }
        pa.setCanRun(false);
        // ServerAccess.logOutputReprint(t.getP().exitValue()); when process is killed, this throws exception

        ProcessResult pr = new ProcessResult(crs.getContent(), cre.getContent(), t.getP(), pa.wasTerminated(), t.getExitCode(), null);
        if (ServerAccess.PROCESS_LOG) {
            ServerAccess.log(pr.stdout, true, false);
            ServerAccess.log(pr.stderr, false, true);
        }
        return pr;
    }

    private static void setUpClosingListener(List<ContentReaderListener> listeners, ProcessAssassin pa) {
        for (ContentReaderListener listener : listeners) {
            if (listener != null && (listener instanceof ClosingListener)) {
                ((ClosingListener) listener).setAssassin(pa);
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
