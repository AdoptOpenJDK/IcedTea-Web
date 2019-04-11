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

package net.adoptopenjdk.icedteaweb.client.policyeditor;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import sun.security.provider.PolicyParser;

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

    boolean isPerformingIO() {
        return performingIO;
    }

    void setPerformingIO(final boolean b) {
        performingIO = b;
    }

    public void setFile(final File file) {
        boolean changedFile = policyFile.setFile(file);
        setChangesMade(changedFile);
    }

    public File getFile() {
        return policyFile.getFile();
    }

    public boolean fileHasChanged() throws IOException {
        return policyFile.hasChanged();
    }

    public boolean addIdentifier(final PolicyIdentifier identifier) {
        final boolean existed = policyFile.addIdentifier(identifier);
        if (!existed) {
            setChangesMade(true);
        }
        return existed;
    }

    public void removeIdentifier(final PolicyIdentifier identifier) {
        setChangesMade(true);
        policyFile.removeIdentifier(identifier);
    }

    public SortedSet<PolicyIdentifier> getIdentifiers() {
        return new TreeSet<>(policyFile.getIdentifiers());
    }

    public Map<PolicyIdentifier, Map<PolicyEditorPermissions, Boolean>> getCopyOfPermissions() {
        return policyFile.getCopyOfPermissions();
    }

    public void setPermission(final PolicyIdentifier identifier, final PolicyEditorPermissions permission, final boolean state) {
        if (getPermission(identifier, permission) != state) {
            setChangesMade(true);
        }
        policyFile.setPermission(identifier, permission, state);
    }

    public boolean getPermission(final PolicyIdentifier identifier, final PolicyEditorPermissions permission) {
        return policyFile.getPermission(identifier, permission);
    }

    public Map<PolicyEditorPermissions, Boolean> getPermissions(final PolicyIdentifier identifier) {
        policyFile.addIdentifier(identifier);
        return new HashMap<>(policyFile.getCopyOfPermissions().get(identifier));
    }

    public void clear() {
        setChangesMade(true);
        policyFile.clearPermissions();
    }

    public void clearPermissions() {
        setChangesMade(true);
        policyFile.clearPermissions();
    }

    public void addCustomPermissions(final PolicyIdentifier identifier, final Collection<PolicyParser.PermissionEntry> permissions) {
        if (!policyFile.getCopyOfCustomPermissions().containsKey(identifier) || !policyFile.getCopyOfCustomPermissions().get(identifier).equals(permissions)) {
            setChangesMade(true);
        }
        policyFile.addCustomPermissions(identifier, permissions);
    }

    public void addCustomPermission(final PolicyIdentifier identifier, final PolicyParser.PermissionEntry permission) {
        final Map<PolicyIdentifier, Set<PolicyParser.PermissionEntry>> customs = policyFile.getCopyOfCustomPermissions();
        if (customs == null || !customs.containsKey(identifier) || (customs.containsKey(identifier) && !customs.get(identifier).contains(permission))) {
            setChangesMade(true);
        }
        addCustomPermissions(identifier, Collections.singletonList(permission));
    }

    public Set<PolicyParser.PermissionEntry> getCustomPermissions(final PolicyIdentifier identifier) {
        policyFile.addIdentifier(identifier);
        return new HashSet<>(policyFile.getCopyOfCustomPermissions().get(identifier));
    }

    public void addPolicyEntry(final PolicyEntry policyEntry) {
        addIdentifier(policyEntry.getPolicyIdentifier());
        for (final PolicyEditorPermissions permission : policyEntry.getPermissions()) {
            setPermission(policyEntry.getPolicyIdentifier(), permission, true);
        }
        addCustomPermissions(policyEntry.getPolicyIdentifier(), policyEntry.getCustomPermissions());
    }

    public void clearCustomPermissions() {
        setChangesMade(true);
        policyFile.clearCustomPermissions();
    }

    public void clearCustomIdentifier(final PolicyIdentifier identifier) {
        setChangesMade(true);
        policyFile.clearCustomIdentifier(identifier);
    }

    public void openAndParsePolicyFile() throws IOException, PolicyParser.ParsingException {
        setPerformingIO(true);
        policyFile.openAndParsePolicyFile();

        setChangesMade(false);
        setPerformingIO(false);
    }

    public void savePolicyFile() throws IOException {
        setPerformingIO(true);
        policyFile.savePolicyFile();

        setChangesMade(false);
        setPerformingIO(false);
    }

    public void copyPolicyEntryToClipboard(final PolicyIdentifier identifier) {
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        final PolicyEntry policyEntry = getPolicyEntry(identifier);
        clipboard.setContents(policyEntry, null);
    }

    public PolicyEntry getPolicyEntry(final PolicyIdentifier identifier) {
        final Collection<PolicyEditorPermissions> enabledPermissions = new HashSet<>();
        for (final Map.Entry<PolicyEditorPermissions, Boolean> entry : getPermissions(identifier).entrySet()) {
            if (entry.getValue()) {
                enabledPermissions.add(entry.getKey());
            }
        }
        return new PolicyEntry.Builder()
                .identifier(identifier)
                .permissions(enabledPermissions)
                .customPermissions(getCustomPermissions(identifier))
                .build();
    }

    public static PolicyEntry getPolicyEntryFromClipboard() throws IOException, UnsupportedFlavorException, PolicyParser.ParsingException {
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        return (PolicyEntry) clipboard.getContents(null).getTransferData(PolicyEntry.POLICY_ENTRY_DATA_FLAVOR);
    }

}
