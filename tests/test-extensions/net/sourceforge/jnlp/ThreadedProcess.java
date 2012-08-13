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

package net.sourceforge.jnlp;

import java.io.File;
import java.util.List;

/**
 *
 * wrapper around Runtime.getRuntime().exec(...) which ensures that process is run inside its own, by us controlled, thread.
 * Process builder caused some unexpected and weird behavior :/
 */
class ThreadedProcess extends Thread {

    Process p = null;
    List<String> args;
    Integer exitCode;
    Boolean running;
    String[] variables;
    File dir;
    Throwable deadlyException = null;
    /*
     * before removing this "useless" variable
     * check DeadLockTestTest.testDeadLockTestTerminated2
     */
    private boolean destoyed = false;

    public boolean isDestoyed() {
        return destoyed;
    }

    public void setDestoyed(boolean destoyed) {
        this.destoyed = destoyed;
    }

    public Boolean isRunning() {
        return running;
    }

    public Integer getExitCode() {
        return exitCode;
    }

    public void setVariables(String[] variables) {
        this.variables = variables;
    }

    public String[] getVariables() {
        return variables;
    }



    public ThreadedProcess(List<String> args) {
        this.args = args;
    }

    public ThreadedProcess(List<String> args, File dir) {
        this(args);
        this.dir = dir;
    }

    public ThreadedProcess(List<String> args,String[] vars) {
        this(args);
        this.variables = vars;
    }
    
     public ThreadedProcess(List<String> args, File dir,String[] vars) {
        this(args,dir);
        this.variables = vars;
    }


    public String getCommandLine() {
        String commandLine = "unknown command";
        try {
            if (args != null && args.size() > 0) {
                commandLine = "";
                for (String string : args) {
                    commandLine = commandLine + " " + string;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            return commandLine;
        }
    }

    public Process getP() {
        return p;
    }

    @Override
    public void run() {
        try {
            running = true;
            Runtime r = Runtime.getRuntime();
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
                exitCode = p.waitFor();
                Thread.sleep(500); //this is giving to fast done proecesses's e/o readers time to read all. I would like to know better solution :-/
            } finally {
                destoyed = true;
            }
        } catch (Exception ex) {
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
}
