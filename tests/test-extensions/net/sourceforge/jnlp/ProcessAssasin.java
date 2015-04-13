/* ProcessAssasin.java
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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import net.sourceforge.jnlp.browsertesting.ReactingProcess;

/**
 * class which timeout any ThreadedProcess. This killing of 'thread with
 * process' replaced not working process.destroy().
 */
public class ProcessAssasin extends Thread {

    long timeout;
    private final ThreadedProcess p;
    //false == is disabled:(
    private boolean canRun = true;
    private boolean wasTerminated = false;
    //signifies that assasin have been summoned
    private volatile boolean killing = false;
    //signifies that assasin have done its job
    private volatile boolean killed = false;
    /**
     * if this is true, then process is not destroyed after timeout, but just
     * left to its own destiny. Its stdout/err is no longer recorded, and it is
     * leaking system resources until it dies by itself The contorl is returned
     * to main thread with all informations recorded untill now. You will be
     * able to listen to std out from listeners still
     */
    private boolean skipInstedOfDesroy = false;
    private ReactingProcess reactingProcess;

    public ProcessAssasin(ThreadedProcess p, long timeout) {
        this.p = (p);
        this.timeout = timeout;
    }

    public ProcessAssasin(ThreadedProcess p, long timeout, boolean skipInstedOfDesroy) {
        this.p = (p);
        this.timeout = timeout;
        this.skipInstedOfDesroy = skipInstedOfDesroy;
    }

    public void setCanRun(boolean canRun) {
        this.canRun = canRun;
        if (p != null) {
            if (p.getP() != null) {
                ServerAccess.logNoReprint("Stopping assassin for" + p.toString() + " " + p.getP().toString() + " " + p.getCommandLine() + ": ");
            } else {
                ServerAccess.logNoReprint("Stopping assassin for" + p.toString() + " " + p.getCommandLine() + ": ");
            }
        } else {
            ServerAccess.logNoReprint("Stopping assassin for null job: ");
        }
    }

    public boolean isCanRun() {
        return canRun;
    }

    public boolean wasTerminated() {
        return wasTerminated;
    }

    public void setSkipInstedOfDesroy(boolean skipInstedOfDesroy) {
        this.skipInstedOfDesroy = skipInstedOfDesroy;
    }

    public boolean isSkipInstedOfDesroy() {
        return skipInstedOfDesroy;
    }

    void setTimeout(long timeout) {
        this.timeout = timeout;
    }


    @Override
    public void run() {
        long startTime = System.nanoTime() / ServerAccess.NANO_TIME_DELIMITER;
        while (canRun) {
            try {
                long time = System.nanoTime() / ServerAccess.NANO_TIME_DELIMITER;
                //ServerAccess.logOutputReprint(time - startTime);
                //ServerAccess.logOutputReprint((time - startTime) > timeout);
                if ((time - startTime) > timeout) {
                    try {
                        if (p != null) {
                            if (p.getP() != null) {
                                ServerAccess.logErrorReprint("Timed out " + p.toString() + " " + p.getP().toString() + " .. killing " + p.getCommandLine() + ": ");
                            } else {
                                ServerAccess.logErrorReprint("Timed out " + p.toString() + " " + "null  .. killing " + p.getCommandLine() + ": ");
                            }
                            wasTerminated = true;
                            if (p.getP() != null) {
                                try {
                                    if (!skipInstedOfDesroy) {
                                        destroyProcess();
                                    }
                                } catch (Throwable ex) {
                                    if (p.deadlyException == null) {
                                        p.deadlyException = ex;
                                    }
                                    ex.printStackTrace();
                                }
                            }
                            p.interrupt();
//                            while (!ServerAccess.terminated.contains(p)) {
//                                Thread.sleep(100);
//                            }
                            if (p.getP() != null) {
                                ServerAccess.logErrorReprint("Timed out " + p.toString() + " " + p.getP().toString() + " .. killed " + p.getCommandLine());
                            } else {
                                ServerAccess.logErrorReprint("Timed out " + p.toString() + " null  .. killed " + p.getCommandLine());
                            }
                        } else {
                            ServerAccess.logErrorReprint("Timed out null job");
                        }
                        break;
                    } finally {
                        p.setDestoyed(true);
                    }
                }
                Thread.sleep(100);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (p != null) {
            if (p.getP() != null) {
                ServerAccess.logNoReprint("assassin for" + p.toString() + " " + p.getP().toString() + " .. done " + p.getCommandLine() + "  termination " + wasTerminated);
            } else {
                ServerAccess.logNoReprint("assassin for" + p.toString() + " null .. done " + p.getCommandLine() + "  termination " + wasTerminated);
            }
        } else {
            ServerAccess.logNoReprint("assassin for non existing job  termination " + wasTerminated);
        }
    }

    public void destroyProcess() {
        try {
            killing = true;
            destroyProcess(p, reactingProcess);
        } finally {
            killed = true;
        }
    }

    public boolean haveKilled() {
        return killed;
    }

    public boolean isKilling() {
        return killing;
    }



    public static void destroyProcess(ThreadedProcess pp, ReactingProcess reactingProcess) {
        Process p = pp.getP();
        try {
            Field f = p.getClass().getDeclaredField("pid");
            f.setAccessible(true);
            String pid = (f.get(p)).toString();
            if (reactingProcess != null) {
                reactingProcess.beforeKill(pid);
            }
//            sigInt(pid);
            sigTerm(pid);
//            sigKill(pid);
        } catch (Exception ex) {
            ServerAccess.logException(ex);
        } finally {
            if (reactingProcess != null) {
                reactingProcess.afterKill("");
            }
        }
    }

    public static void sigInt(String pid) throws Exception {
        kill(pid, "SIGINT");
    }

    public static void sigKill(String pid) throws Exception {
        kill(pid, "SIGKILL");
    }

    public static void sigTerm(String pid) throws Exception {
        kill(pid, "SIGTERM");
    }

    public static void kill(String pid, String signal) throws InterruptedException, Exception {
        List<String> ll = new ArrayList<String>(4);
        ll.add("kill");
        ll.add("-s");
        ll.add(signal);
        ll.add(pid);
        ServerAccess.executeProcess(ll); //sync, but  acctually release
        //before affected application close
        Thread.sleep(1000);
    }

    void setReactingProcess(ReactingProcess reactingProcess) {
        this.reactingProcess = reactingProcess;
    }
}
