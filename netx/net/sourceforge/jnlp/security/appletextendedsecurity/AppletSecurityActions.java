/*   Copyright (C) 2014 Red Hat, Inc.

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
package net.sourceforge.jnlp.security.appletextendedsecurity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class AppletSecurityActions implements Iterable<ExecuteAppletAction>{

    private final List<ExecuteAppletAction> actions = new ArrayList<>();
    public static final int UNSIGNED_APPLET_ACTION = 0;
    public static final int MATCHING_ALACA_ACTION = 1;
    /** well this si not nice. We can iterate through all the records to find
    * longest remembered item, but it is unnecessary overhead. The new record is
    * added willingly with some effort. Should be easy to inc also this
    */ 
    public static final int REMEMBER_COLUMNS_COUNT = /*2*/
        Collections.max(Arrays.asList(UNSIGNED_APPLET_ACTION, MATCHING_ALACA_ACTION))+1;


    public static AppletSecurityActions createDefault() {
        AppletSecurityActions asas = new AppletSecurityActions();
        asas.setUnsignedAppletAction(ExecuteAppletAction.UNSET);
        asas.setMatchingAlacaAction(ExecuteAppletAction.UNSET);
        return asas;
    }
    /*
     * quick setup method when new item , with one action, is added
     */
    public static AppletSecurityActions fromAction(int id, ExecuteAppletAction s) {
        if (s == null){
            s = ExecuteAppletAction.UNSET;
        }
        AppletSecurityActions asas = new AppletSecurityActions();
        asas.setAction(id, s);
        return asas;
    }


    static AppletSecurityActions fromString(String s) {
        if (s == null) {
            s = "";
        }
        s = s.trim(); //to not return on leading space, may be dangerous, 
        //but the s shouldbe already trimmed before bubbling here.
        //does " A"  means UNSET(1)+ALWAYS(2)  or ALWAYS(1)+UNSET(2)
        //or UNSET(1)+UNSET(2)?
        AppletSecurityActions asas = new AppletSecurityActions();
        for (char x : s.toCharArray()){
            if (Character.isWhitespace(x)){
                break;
            }
            asas.actions.add(ExecuteAppletAction.fromChar(x));
        }
        return asas;
    }

    public ExecuteAppletAction getAction(int i) {
        if (i>= actions.size()){
            return ExecuteAppletAction.UNSET;
        }
        return actions.get(i);
    }
    
    public void setAction(int i, ExecuteAppletAction a) {
        while (actions.size() <= i){
            actions.add(ExecuteAppletAction.UNSET);
        }
        actions.set(i,a);
    }


    public ExecuteAppletAction getUnsignedAppletAction() {
        return getAction(UNSIGNED_APPLET_ACTION);
    }

    public void setUnsignedAppletAction(ExecuteAppletAction a) {
       setAction(UNSIGNED_APPLET_ACTION,a);
    }


    public ExecuteAppletAction getMatchingAlacaAction() {
        return getAction(MATCHING_ALACA_ACTION);
    }
    
    public void setMatchingAlacaAction(ExecuteAppletAction a) {
        setAction(MATCHING_ALACA_ACTION, a);
    }

    @Override
    public String toString() {
        return toShortString();
    }

    public String toLongString() {
        StringBuilder sb = new StringBuilder();
        for (ExecuteAppletAction executeAppletAction : actions) {
            sb.append(executeAppletAction.toString()).append("; ");
        }
        return sb.toString();
    }


    public String toShortString() {
        StringBuilder sb = new StringBuilder();
        for (ExecuteAppletAction executeAppletAction : actions) {
            sb.append(executeAppletAction.toChar());
        }
        return sb.toString();
    }

    public int getRealCount() {
        return actions.size();
    }


    /**
     * stub for testing 
     * @return 
     */
    List<ExecuteAppletAction> getActions() {
        return actions;
    }

    @Override
    public Iterator<ExecuteAppletAction> iterator() {
        return actions.iterator();
    }

}
