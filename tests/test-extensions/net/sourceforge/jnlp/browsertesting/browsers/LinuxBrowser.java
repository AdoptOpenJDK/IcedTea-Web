/* LinuxBrowser.java
Copyright (C) 2012 Red Hat, Inc.

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

package net.sourceforge.jnlp.browsertesting.browsers;

import net.sourceforge.jnlp.browsertesting.Browser;


public abstract class LinuxBrowser implements Browser{
      public static final String DEFAULT_PLUGIN_NAME="libjavaplugin.so";
      public static final String DEFAULT_BIN_PATH="/usr/bin/";

      protected final String bin;
      protected String fsdir="unknown";

    public LinuxBrowser(String bin) {
        this.bin = bin;
    }

      

    @Override
    public String getBin() {
        return bin;
    }

//    @Override
//    public void setBin(String bin) {
//        this.bin=bin;
//    }

    @Override
    public String getDefaultBin() {
       return DEFAULT_BIN_PATH+getID().toExec();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Browser)) return false;
        Browser b=(Browser) obj;
        return b.getBin().equals(getBin());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + (this.bin != null ? this.bin.hashCode() : 0);
        return hash;
    }

      @Override
    public String getDefaultPluginExpectedLocation() {
        if (System.getProperty("os.arch").contains("64")) {
            return "/usr/lib64/"+fsdir+"/plugins";
        } else {
            return "/usr/lib/"+fsdir+"/plugins";

        }
    }

    @Override
    public void beforeProcess(String s) {
        
    }

    @Override
    public void afterProcess(String s) {
       
    }

    @Override
    public void beforeKill(String s) {

    }

    @Override
    public void afterKill(String s) {
       
    }

    
}
