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

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.util.FileUtils;
import net.sourceforge.jnlp.util.MD5SumWatcher;
import sun.security.provider.PolicyParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.channels.Channels;
import java.nio.channels.FileLock;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class PolicyFileModel {

    private final static Logger LOG = LoggerFactory.getLogger(PolicyFileModel.class);

    private File file;
    /**
     * Maps Codebases to Maps of Permissions and whether that Permission is set or not. The Codebase keys correspond to
     * the Codebases in the list UI, and the Permission->Boolean maps correspond to the checkboxes associated with
     * each Codebase.
     */
    private final Map<PolicyIdentifier, Map<PolicyEditorPermissions, Boolean>> permissionsMap = Collections.synchronizedMap(new HashMap<PolicyIdentifier, Map<PolicyEditorPermissions, Boolean>>());
    private final Map<PolicyIdentifier, Set<PolicyParser.PermissionEntry>> customPermissionsMap = Collections.synchronizedMap(new HashMap<PolicyIdentifier, Set<PolicyParser.PermissionEntry>>());

    private KeystoreInfo keystoreInfo = new KeystoreInfo(null, null, null, null);
    private MD5SumWatcher fileWatcher;
    private PolicyParser parser = new PolicyParser(false);

    PolicyFileModel(final String filepath) {
        this(new File(filepath));
    }

    PolicyFileModel(final File file) {
        setFile(file);
    }

    PolicyFileModel() {
    }

    synchronized boolean setFile(final File file) {
        this.file = file;
        boolean sameFile = Objects.equals(this.file, file);
        return !sameFile;
    }

    synchronized File getFile() {
        return file;
    }

    synchronized PolicyParser getParser() {
        return parser;
    }

    /**
     * Open the file pointed to by the filePath field. This is either provided by the
     * "-file" command line flag, or if none given, comes from DeploymentConfiguration.
     */
     synchronized void openAndParsePolicyFile() throws IOException, PolicyParser.ParsingException {
        parser = new PolicyParser(false);
        fileWatcher = new MD5SumWatcher(file);
        fileWatcher.update();
        clearPermissions();
        final FileLock fileLock = FileUtils.getFileLock(file.getAbsolutePath(), false, true);
        try {
            parser.read(new BufferedReader(new InputStreamReader(Channels.newInputStream(fileLock.channel()), "UTF-8")));
            keystoreInfo = new KeystoreInfo(parser.getKeyStoreUrl(), parser.getKeyStoreType(), parser.getKeyStoreProvider(), parser.getStorePassURL());
            final Set<PolicyParser.GrantEntry> grantEntries = new HashSet<>(Collections.list(parser.grantElements()));
            synchronized (permissionsMap) {
                synchronized (customPermissionsMap) {
                    for (final PolicyParser.GrantEntry grantEntry : grantEntries) {
                        PolicyIdentifier policyIdentifier =
                                new PolicyIdentifier(grantEntry.signedBy, grantEntry.principals, grantEntry.codeBase);
                        if (PolicyIdentifier.isDefaultPolicyIdentifier(policyIdentifier)) {
                            policyIdentifier = PolicyIdentifier.ALL_APPLETS_IDENTIFIER;
                        }
                        addIdentifier(policyIdentifier);
                        for (final PolicyParser.PermissionEntry permissionEntry : grantEntry.permissionEntries) {
                            final PolicyEditorPermissions editorPermissions = PolicyEditorPermissions.fromPermissionEntry(permissionEntry);
                            if (editorPermissions != null) {
                                permissionsMap.get(policyIdentifier).put(editorPermissions, true);
                            } else {
                                customPermissionsMap.get(policyIdentifier).add(permissionEntry);
                            }
                        }
                    }
                }
            }
        } finally {
            try {
                fileLock.release();
            } catch (final IOException e) {
               LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
            }
        }
    }

    /**
     * Save the policy model into the file pointed to by the filePath field.
     */
    synchronized void savePolicyFile() throws IOException {
        parser = new PolicyParser(false);
        FileLock fileLock = null;
        try {
            fileLock = FileUtils.getFileLock(file.getAbsolutePath(), false, true);
            synchronized (permissionsMap) {
                for (final PolicyIdentifier identifier : permissionsMap.keySet()) {
                    final String codebase;
                    if (identifier.getCodebase().isEmpty()) {
                        codebase = null;
                    } else {
                        codebase = identifier.getCodebase();
                    }
                    final PolicyParser.GrantEntry grantEntry =
                            new PolicyParser.GrantEntry(identifier.getSignedBy(), codebase);
                    for (final Map.Entry<PolicyEditorPermissions, Boolean> entry : permissionsMap.get(identifier).entrySet()) {
                        if (entry.getValue()) {
                            final PolicyEditorPermissions permission = entry.getKey();
                            final String actionsString;
                            if (permission.getActions().equals(PermissionActions.NONE)) {
                                actionsString = null;
                            } else {
                                actionsString = permission.getActions().rawString();
                            }
                            final PolicyParser.PermissionEntry permissionEntry =
                                    new PolicyParser.PermissionEntry(permission.getType().type,
                                            permission.getTarget().target,
                                            actionsString);
                            grantEntry.add(permissionEntry);
                        }
                    }
                    for (final PolicyParser.PermissionEntry customPermission : customPermissionsMap.get(identifier)) {
                        grantEntry.add(customPermission);
                    }
                    grantEntry.principals.addAll(identifier.getPrincipals());
                    parser.add(grantEntry);
                }
            }
            parser.write(new BufferedWriter(new OutputStreamWriter(Channels.newOutputStream(fileLock.channel()), "UTF-8")));
        } catch (final IOException e) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
        } finally {
            if (fileLock != null) {
                try {
                    fileLock.release();
                } catch (final IOException e) {
                    LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
                }

            }
        }
        fileWatcher = new MD5SumWatcher(file);
        fileWatcher.update();
    }

    synchronized boolean hasChanged() throws IOException {
        return fileWatcher != null && fileWatcher.update();
    }

    synchronized SortedSet<PolicyIdentifier> getIdentifiers() {
        return new TreeSet<>(permissionsMap.keySet());
    }

    synchronized KeystoreInfo getKeystoreInfo() {
        return keystoreInfo;
    }

    /**
     * Add a new identifier. No action is taken if the identifier has already been added.
     * @param identifier for which a permissions mapping is required
     * @return true iff there was already an entry for this identifier
     */
    synchronized boolean addIdentifier(final PolicyIdentifier identifier) {
        Objects.requireNonNull(identifier);

        boolean existingCodebase = true;
        if (!permissionsMap.containsKey(identifier)) {
            final Map<PolicyEditorPermissions, Boolean> map = new HashMap<>();
            for (final PolicyEditorPermissions perm : PolicyEditorPermissions.values()) {
                map.put(perm, false);
            }
            permissionsMap.put(identifier, map);
            existingCodebase = false;
        }
        if (!customPermissionsMap.containsKey(identifier)) {
            customPermissionsMap.put(identifier, new HashSet<PolicyParser.PermissionEntry>());
            existingCodebase = false;
        }

        return existingCodebase;
    }

    synchronized void clearPermissions() {
        permissionsMap.clear();
        clearCustomPermissions();
    }

    synchronized void removeIdentifier(final PolicyIdentifier identifier) {
        Objects.requireNonNull(identifier);
        permissionsMap.remove(identifier);
        customPermissionsMap.remove(identifier);
    }

    synchronized void setPermission(final PolicyIdentifier identifier, final PolicyEditorPermissions permission, final boolean state) {
        Objects.requireNonNull(identifier);
        Objects.requireNonNull(permission);
        addIdentifier(identifier);
        permissionsMap.get(identifier).put(permission, state);
    }

    synchronized boolean getPermission(final PolicyIdentifier identifier, final PolicyEditorPermissions permission) {
        Objects.requireNonNull(identifier);
        Objects.requireNonNull(permission);
        if (!permissionsMap.containsKey(identifier)) {
            return false;
        }
        return permissionsMap.get(identifier).get(permission);
    }

    synchronized Map<PolicyIdentifier, Map<PolicyEditorPermissions, Boolean>> getCopyOfPermissions() {
        return new HashMap<>(permissionsMap);
    }

    synchronized void clearCustomPermissions() {
        customPermissionsMap.clear();
    }

    synchronized void clearCustomIdentifier(final PolicyIdentifier identifier) {
        Objects.requireNonNull(identifier);
        if (!customPermissionsMap.containsKey(identifier)) {
            return;
        }
        customPermissionsMap.get(identifier).clear();
    }

    synchronized void addCustomPermissions(final PolicyIdentifier identifier, final Collection<? extends PolicyParser.PermissionEntry> permissions) {
        Objects.requireNonNull(identifier);
        Objects.requireNonNull(permissions);
        addIdentifier(identifier);
        customPermissionsMap.get(identifier).addAll(permissions);
    }

    synchronized Map<PolicyIdentifier, Set<PolicyParser.PermissionEntry>> getCopyOfCustomPermissions() {
        return new HashMap<>(customPermissionsMap);
    }
}
