/*Copyright (C) 2014 Red Hat, Inc.

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

package net.sourceforge.jnlp.security.policyeditor;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sourceforge.jnlp.util.logging.OutputController;

public class PolicyEditorController {

    public final PolicyFileModel policyFile = new PolicyFileModel();
    private volatile boolean changesMade = false;
    private volatile boolean performingIO = false;

    boolean changesMade() {
        return changesMade;
    }

    void setChangesMade(final boolean b) {
        changesMade = b;
    }

    boolean performingIO() {
        return performingIO;
    }

    void setPerformingIO(final boolean b) {
        performingIO = b;
    }

    public void setFile(final File file) {
        setChangesMade(true);
        policyFile.setFile(file);
    }

    public File getFile() {
        return policyFile.getFile();
    }

    public boolean fileHasChanged() throws FileNotFoundException, IOException {
        return policyFile.hasChanged();
    }

    public boolean addCodebase(final String codebase) {
        final boolean existed = policyFile.addCodebase(codebase);
        if (!existed) {
            setChangesMade(true);
        }
        return existed;
    }

    public void removeCodebase(final String codebase) {
        setChangesMade(true);
        policyFile.removeCodebase(codebase);
    }

    public Set<String> getCodebases() {
        return new HashSet<>(policyFile.getCodebases());
    }

    public Map<String, Map<PolicyEditorPermissions, Boolean>> getCopyOfPermissions() {
        return policyFile.getCopyOfPermissions();
    }

    public void setPermission(final String codebase, final PolicyEditorPermissions permission, final boolean state) {
        if (getPermission(codebase, permission) != state) {
            setChangesMade(true);
        }
        policyFile.setPermission(codebase, permission, state);
    }

    public boolean getPermission(final String codebase, final PolicyEditorPermissions permission) {
        return policyFile.getPermission(codebase, permission);
    }

    public Map<PolicyEditorPermissions, Boolean> getPermissions(final String codebase) {
        policyFile.addCodebase(codebase);
        return new HashMap<>(policyFile.getCopyOfPermissions().get(codebase));
    }

    public void clearPermissions() {
        setChangesMade(true);
        policyFile.clearPermissions();
    }

    public void addCustomPermissions(final String codebase, final Collection<CustomPermission> permissions) {
        if (!policyFile.getCopyOfCustomPermissions().equals(permissions)) {
            setChangesMade(true);
        }
        policyFile.addCustomPermissions(codebase, permissions);
    }

    public void addCustomPermission(final String codebase, final CustomPermission permission) {
        final Map<String, Set<CustomPermission>> customs = policyFile.getCopyOfCustomPermissions();
        if (customs == null || !customs.containsKey(codebase) || (customs.containsKey(codebase) && !customs.get(codebase).contains(permission))) {
            setChangesMade(true);
        }
        addCustomPermissions(codebase, Arrays.asList(permission));
    }

    public Set<CustomPermission> getCustomPermissions(final String codebase) {
        policyFile.addCodebase(codebase);
        return new HashSet<>(policyFile.getCopyOfCustomPermissions().get(codebase));
    }

    public void addPolicyEntry(final PolicyEntry policyEntry) {
        addCodebase(policyEntry.getCodebase());
        for (final PolicyEditorPermissions permission : policyEntry.getPermissions()) {
            setPermission(policyEntry.getCodebase(), permission, true);
        }
        addCustomPermissions(policyEntry.getCodebase(), policyEntry.getCustomPermissions());
    }

    public void clearCustomPermissions() {
        setChangesMade(true);
        policyFile.clearCustomPermissions();
    }

    public void clearCustomCodebase(final String codebase) {
        setChangesMade(true);
        policyFile.clearCustomCodebase(codebase);
    }

    public void openAndParsePolicyFile() throws IOException, InvalidPolicyException {
        try {
            policyFile.getFile().createNewFile();
        } catch (final IOException e) {
            OutputController.getLogger().log(e);
        }

        setPerformingIO(true);
        policyFile.openAndParsePolicyFile();

        setChangesMade(false);
        setPerformingIO(false);
    }

    public void savePolicyFile() throws FileNotFoundException, IOException {
        setPerformingIO(true);
        policyFile.savePolicyFile();

        setChangesMade(false);
        setPerformingIO(false);
    }

    public void copyCodebaseToClipboard(final String codebase) {
        final Map<PolicyEditorPermissions, Boolean> standardPermissions = policyFile.getCopyOfPermissions().get(codebase);
        final Set<CustomPermission> customPermissions = policyFile.getCopyOfCustomPermissions().get(codebase);

        final Set<PolicyEditorPermissions> enabledPermissions = new HashSet<>();
        for (final Map.Entry<PolicyEditorPermissions, Boolean> entry : standardPermissions.entrySet()) {
            if (entry.getValue()) {
                enabledPermissions.add(entry.getKey());
            }
        }
        final PolicyEntry entry = new PolicyEntry(codebase, enabledPermissions, customPermissions);
        final StringSelection clipboardSelection = new StringSelection(entry.toString());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(clipboardSelection, clipboardSelection);
    }

    private static String getClipboardContentsAsString() throws IOException, UnsupportedFlavorException {
        final Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        return (String) transferable.getTransferData(DataFlavor.stringFlavor);
    }

    public static PolicyEntry getPolicyEntryFromClipboard() throws IOException, UnsupportedFlavorException, InvalidPolicyException {
        return PolicyEntry.fromString(getClipboardContentsAsString());
    }

    public String getCodebaseFromClipboard() throws IOException, UnsupportedFlavorException, InvalidPolicyException {
        return getPolicyEntryFromClipboard().getCodebase();
    }

    public Map<PolicyEditorPermissions, Boolean> getPermissionsFromClipboard() throws IOException, UnsupportedFlavorException, InvalidPolicyException {
        final Map<PolicyEditorPermissions, Boolean> ret = new HashMap<>();
        final Set<PolicyEditorPermissions> enabledPermissions = getPolicyEntryFromClipboard().getPermissions();
        for (final PolicyEditorPermissions permission : PolicyEditorPermissions.values()) {
            ret.put(permission, enabledPermissions.contains(permission));
        }
        return ret;
    }

    public Set<CustomPermission> getCustomPermissionsFromClipboard() throws IOException, UnsupportedFlavorException, InvalidPolicyException {
        return getPolicyEntryFromClipboard().getCustomPermissions();
    }

}
