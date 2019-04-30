/* ThreadedProcess.java
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import net.adoptopenjdk.icedteaweb.StreamUtils;

/**
 *
 * wrapper around Runtime.getRuntime().exec(...) which ensures that process is run inside its own, by us controlled, thread.
 * Process builder caused some unexpected and weird behavior :/
 */
public class ThreadedProcess extends Thread {

    private Process p = null;
    private final List<String> args;
    private Integer exitCode;
    private Boolean running;
    private String[] variables;
    private File dir;
    Throwable deadlyException = null;
    /*
     * before removing this "useless" variable
     * check DeadLockTestTest.testDeadLockTestTerminated2
     */
    private boolean destroyed = false;
    private ProcessAssassin assassin;
    private InputStream writer;

    public boolean isDestroyed() {
        return destroyed;
    }

    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }

    public Boolean isRunning() {
        return running;
    }

    public Integer getExitCode() {
        return exitCode;
    }



    public void setWriter(final InputStream writer) {
        this.writer = writer;
    }

    


    private ThreadedProcess(final List<String> args) {
        this.args = args;
    }

    private ThreadedProcess(final List<String> args, File dir) {
        this(args);
        this.dir = dir;
    }

     public ThreadedProcess(final List<String> args, final File dir, final String[] vars) {
        this(args,dir);
        this.variables = vars;
    }


    public String getCommandLine() {
        StringBuilder commandLine = new StringBuilder("unknown command");
        try {
            if (args != null && args.size() > 0) {
                commandLine = new StringBuilder();
                for (String string : args) {
                    commandLine.append(" ").append(string);
                }
            }
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
        return commandLine.toString();
    }

    public Process getP() {
        return p;
    }

    @Override
    public void run() {
        try {
            running = true;
            final Runtime r = Runtime.getRuntime();
            if (dir == null) {
                if (variables == null) {
                    p = r.exec(args.toArray(new String[0]));
                } else {
                    p = r.exec(args.toArray(new String[0]), variables);
                }
            } else {
                p = r.exec(args.toArray(new String[0]), variables, dir);
            }
            try {
                if (writer != null){
                    final Thread t = new Thread(() -> {
                        try (
                                final BufferedReader br = new BufferedReader(new InputStreamReader(writer, "utf-8"));
                                final BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(p.getOutputStream(), "utf-8"))) {
                            while (true) {
                                final String s = br.readLine();
                                if (s == null) {
                                    break;
                                }
                                bw.write(s + System.lineSeparator());
                                bw.flush();

                            }
                        } catch (final Exception ex) {
                            ServerAccess.logException(ex);
                        }
                    });
                    t.start();
                }
                StreamUtils.waitForSafely(p);
                exitCode = p.exitValue();
                Thread.sleep(500); //this is giving to fast done processes's e/o readers time to read all. I would like to know better solution :-/
                while(assassin.isKilling() && !assassin.haveKilled()){
                    Thread.sleep(100);
                }
            } finally {
                destroyed = true;
            }
        } catch (final Exception ex) {
            if (ex instanceof InterruptedException) {
                //add to the set of terminated threaded processes
                deadlyException = ex;
                ServerAccess.logException(deadlyException, false);
                //ServerAccess.terminated.add(this);
            } else {
                //happens when non-existing process is launched, is causing p null!
                //ServerAccess.terminated.add(this);
                deadlyException = ex;
                ServerAccess.logException(deadlyException, false);
                throw new RuntimeException(ex);
            }
        } finally {
            running = false;
        }
    }

    void setAssassin(ProcessAssassin pa) {
        this.assassin =pa;
    }
}
