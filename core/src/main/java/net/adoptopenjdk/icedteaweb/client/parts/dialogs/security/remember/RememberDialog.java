/*
 Copyright (C) 2008-2010 Red Hat, Inc.

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

import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.SecurityDialog;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity.UnsignedAppletActionEntry;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity.UnsignedAppletTrustConfirmation;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.DialogResult;

import java.awt.Component;
import java.awt.Container;

public class RememberDialog {

    public void setOrUpdateRememberedState(SecurityDialog dialog) {
        RememberableDialog found = findRememberablePanel(dialog.getSecurityDialogPanel());
        if (found == null) {
            return;
        }
        String value =  "";
        if (found.getValue()!=null){
            value = found.getValue().writeValue();
        }
        SavedRememberAction action = new SavedRememberAction(createAction(found.getRememberAction().isRemember(), found.getValue()), value);
        setOrUpdateRememberedState(dialog, found.getRememberAction().isCodebase(), action);
    }
    
    /*
     * for headless dialogues
     */
     public void setOrUpdateRememberedState(SecurityDialog dialog, boolean wholeCodebase, SavedRememberAction action) {
        RememberableDialog found = findRememberablePanel(dialog.getSecurityDialogPanel());
        if (found == null) {
            return;
        }
        UnsignedAppletTrustConfirmation.updateAppletAction(found.getFile(), action, wholeCodebase, (Class<RememberableDialog>) found.getClass());
    }

    public SavedRememberAction getRememberedState(SecurityDialog dialog) {
        RememberableDialog found = findRememberablePanel(dialog.getSecurityDialogPanel());
        if (found != null) {
            return getRememberedState(found);
        }
        return null;
    }

    public SavedRememberAction getRememberedState(RememberableDialog found) {
        UnsignedAppletActionEntry entry = UnsignedAppletTrustConfirmation.getStoredEntry(found.getFile(), (found.getClass()));
        //was saved for this class of found
        if (entry != null) {
            SavedRememberAction action = entry.getAppletSecurityActions().getActionEntry(found.getClass());
            return action;
        }
        return null;
    }

    public RememberableDialog findRememberablePanel(Container search) {
        if (search==null){
            return null;
        }
        if (search instanceof  RememberableDialog){
            return (RememberableDialog) search;
        }
        //Breadth-first important
        for (Component comp : search.getComponents()) {
            if (comp instanceof RememberableDialog) {
                return (RememberableDialog) comp;
            }
        }
        for (Component comp : search.getComponents()) {
            if (comp instanceof Container) {
                RememberableDialog candidate = findRememberablePanel((Container) comp);
                if (candidate != null) {
                    return candidate;
                }
            }
        }
        return null;
    }

    public static ExecuteAppletAction createAction(boolean permanent, DialogResult value) {
        if (value == null){
            return ExecuteAppletAction.NO; 
        }
        if (value.toBoolean()){
            if (permanent){
                return ExecuteAppletAction.ALWAYS;
            } else {
                return ExecuteAppletAction.YES;
            }
        } else {
            if (permanent){
                return ExecuteAppletAction.NEVER;
            } else {
                return ExecuteAppletAction.NO;
            }
        }
    }

    private static class RememberDialogueHolder {

        //https://en.wikipedia.org/wiki/Double-checked_locking#Usage_in_Java
        //https://en.wikipedia.org/wiki/Initialization_on_demand_holder_idiom
        private static final RememberDialog INSTANCE = new RememberDialog();

        private static RememberDialog getRememberDialogue() {
            return RememberDialogueHolder.INSTANCE;
        }
    }

    public static RememberDialog getInstance() {
        return RememberDialogueHolder.getRememberDialogue();
    }

}
