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
package net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember;

import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.apptrustwarningpanel.MatchingALACAttributePanel;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.apptrustwarningpanel.UnsignedAppletTrustWarningPanel;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

//from legacy, just wrapper around map
public class AppletSecurityActions implements Iterable<SavedRememberAction> {

    private final static Logger LOG = LoggerFactory.getLogger(AppletSecurityActions.class);

    private final Map<String, SavedRememberAction> actions = new HashMap<>();

    /*
     * quick setup method when new item , with one action, is added
     */
    public static AppletSecurityActions fromAction(Class<? extends RememberableDialog> id, SavedRememberAction s) {
        AppletSecurityActions asas = new AppletSecurityActions();
        asas.setAction(id, s);
        return asas;
    }

    /**
     *
     * ClassName:ACTION{savedvalue};ClassName:ACTION{savedvalue};... eg
     * MatchingALACAttributePanel:A{true};PartiallySignedAppTrustWarningPanel:a{S};
     *
     * @param s
     * @return
     */
    public static AppletSecurityActions fromString(String s) {
        if (s == null) {
            s = "";
        }
        s = s.trim();
        AppletSecurityActions asas = new AppletSecurityActions();
        if (s.isEmpty()) {
            return asas;
        }
        if (s.contains(";") || s.contains(":") || s.contains("{") || s.contains("}")) {
            String[] ss = s.split("};");
            for (String string : ss) {
                string = string.trim();
                if (string.isEmpty()) {
                    continue;
                }
                int nameIndex = string.indexOf(":");
                int valueIndex = string.indexOf("{");
                if (nameIndex < 0 || valueIndex < 0) {
                    continue;
                }
                String name = string.substring(0, nameIndex);
                String action = string.substring(nameIndex + 1, valueIndex);
                String value = string.substring(valueIndex + 1); //rather null if empty? if it si null, and expected, then NPE should never be thrown
                if (value.isEmpty()) {
                    value = null; //or empty string is better. What if saved value really is empty string?
                    //Special sequence for null?
                }
                asas.actions.put(name, new SavedRememberAction(ExecuteAppletAction.fromChar(action.charAt(0)), value));
            }
            return asas;
        } else {
            return readLegacySave(s);
        }
    }

    private static AppletSecurityActions readLegacySave(String s) {
        if (s == null) {
            s = "";
        }
        s = s.trim(); //to not return on leading space, may be dangerous, 
        //but the s should be already trimmed before bubbling here.
        //does " A"  means UNSET(1)+ALWAYS(2)  or ALWAYS(1)+UNSET(2)
        //or UNSET(1)+UNSET(2)?
        AppletSecurityActions asas = new AppletSecurityActions();
        int i = 0;
        for (char x : s.toCharArray()) {
            if (Character.isWhitespace(x)) {
                break;
            }
            if (x == 2) {
                //only two elements were known for 1.6 or older
                break;
            }
            //unset is no op now
            if (x == 'X') {
                i++;
                continue;
            }
            if (x == 's' || x == 'S') {
                continue;
            }
            char nwX = x;
            if (x == 's') {
                nwX = 'y';
            }
            if (x == 'S') {
                nwX = 'A';
            }
            ExecuteAppletAction q = ExecuteAppletAction.fromChar(nwX);
            if (i == 0) {
                SavedRememberAction sa = new SavedRememberAction(q, legacyToCurrent(x));//maybe better switch then toChar?
                asas.actions.put(classToKey(UnsignedAppletTrustWarningPanel.class), sa);
            } else if (i == 1) {
                SavedRememberAction sa = new SavedRememberAction(q, legacyToCurrent(x));//maybe better switch then toChar?
                asas.actions.put(classToKey(MatchingALACAttributePanel.class), sa);
            } else {
                LOG.debug("Unknown saved legacy item on position {} of char: {}", i, x);
            }
            i++;
        }
        return asas;
    }

    private static String legacyToCurrent(char q) {
        switch (q) {
            case 'y':
            case 'A':
                return "YES";
            case 'n':
            case 'N':
                return "NO";
            case 's':
            case 'S':
                return "SANDBOX";
        }
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public ExecuteAppletAction getAction(Class clazz) {
        return getAction(classToKey(clazz));
    }

    public ExecuteAppletAction getAction(String clazz) {
        SavedRememberAction o = getActionEntry(clazz);
        if (o == null) {
            return null;
        }
        return o.getAction();
    }

    public SavedRememberAction getActionEntry(Class clazz) {
        return getActionEntry(classToKey(clazz));
    }

    public SavedRememberAction getActionEntry(String clazz) {
        return actions.get(clazz);

    }

    public void setAction(Class clazz, SavedRememberAction a) {
        setAction(classToKey(clazz), a);
    }

    public void setAction(String i, SavedRememberAction a) {
        actions.put(i, a);
    }
    
    public void removeAction(Class clazz) {
        removeAction(classToKey(clazz));
    }

    public void removeAction(String i) {
        actions.remove(i);
    }

    @Override
    public String toString() {
        return toShortString();
    }

    public String toLongString() {
        StringBuilder sb = new StringBuilder();
        Collection<Entry<String, SavedRememberAction>> l = getEntries();
        for (Entry<String, SavedRememberAction> a : l) {
            sb.append(a.getKey()).append(":").append(a.getValue().toLongString());
        }
        return sb.toString();
    }

    public String toShortString() {
        StringBuilder sb = new StringBuilder();
        Collection<Entry<String, SavedRememberAction>> l = getEntries();
        for (Entry<String, SavedRememberAction> a : l) {
            sb.append(a.getKey()).append(":").append(a.getValue().toShortString()).append(";");
        }
        return sb.toString();
    }

    public int getRealCount() {
        return actions.size();
    }

    /**
     * stub for testing
     *
     * @return
     */
    public Collection<ExecuteAppletAction> getActions() {
        Collection<SavedRememberAction> col = actions.values();
        List<ExecuteAppletAction> l = new ArrayList<>(col.size());
        for (SavedRememberAction savedRememberAction : col) {
            l.add(savedRememberAction.getAction());
        }
        return l;
    }

    public Collection<Entry<String, SavedRememberAction>> getEntries() {
        return actions.entrySet();
    }

    @Override
    public Iterator<SavedRememberAction> iterator() {
        return actions.values().iterator();
    }

    /*
     Simple wrapper. What there wil be need of changing this logic
     */
    private static String classToKey(Class clazz) {
        return clazz.getSimpleName();
    }

    public void refresh(String aValue) {
        AppletSecurityActions nev = fromString(aValue);
        actions.clear();
        actions.putAll(nev.actions);
    }

}
