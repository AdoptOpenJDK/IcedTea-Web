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

import sun.security.provider.PolicyParser;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * This class represents a "grant" entry in a policy file. This is defined as a policy entry block
 * which begins with the keyword "grant" and ends with the delimiter "};".
 */
public class PolicyEntry implements Serializable, Transferable {

    public static class Builder {
        private String signedBy, codebase;
        private final Set<PolicyEditorPermissions> permissions = EnumSet.noneOf(PolicyEditorPermissions.class);
        private final Set<PolicyParser.PermissionEntry> customPermissions = new HashSet<>();
        private final Set<PolicyParser.PrincipalEntry> principals = new HashSet<>();

        public Builder signedBy(final String signedBy) {
            this.signedBy = signedBy;
            return this;
        }

        public Builder principals(final Collection<PolicyParser.PrincipalEntry> principals) {
            this.principals.addAll(principals);
            return this;
        }

        public Builder codebase(final String codebase) {
            this.codebase = codebase;
            return this;
        }

        public Builder identifier(final PolicyIdentifier identifier) {
            return signedBy(identifier.getSignedBy())
                    .codebase(identifier.getCodebase())
                    .principals(identifier.getPrincipals());
        }

        public Builder permissions(final Collection<PolicyEditorPermissions> permissions) {
            this.permissions.addAll(permissions);
            return this;
        }

        public Builder customPermissions(final Collection<? extends PolicyParser.PermissionEntry> customPermissions) {
            this.customPermissions.addAll(customPermissions);
            return this;
        }

        public PolicyEntry build() {
            return new PolicyEntry(this);
        }
    }

    public static final DataFlavor POLICY_ENTRY_DATA_FLAVOR = new DataFlavor(PolicyEntry.class, "PolicyEntry");

    private final PolicyIdentifier policyIdentifier;
    private final Set<PolicyEditorPermissions> permissions = new HashSet<>();
    private final Set<PolicyParser.PermissionEntry> customPermissions = new HashSet<>();

    private PolicyEntry(final Builder builder) {
        this.policyIdentifier = new PolicyIdentifier(builder.signedBy, builder.principals, builder.codebase);
        this.permissions.addAll(builder.permissions);
        this.permissions.remove(null);
        this.customPermissions.addAll(builder.customPermissions);
        this.customPermissions.remove(null);
    }

    public PolicyIdentifier getPolicyIdentifier() {
        return policyIdentifier;
    }

    public Set<PolicyEditorPermissions> getPermissions() {
        return permissions;
    }

    public Set<PolicyParser.PermissionEntry> getCustomPermissions() {
        return customPermissions;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] { POLICY_ENTRY_DATA_FLAVOR };
    }

    @Override
    public boolean isDataFlavorSupported(final DataFlavor dataFlavor) {
        return Objects.equals(POLICY_ENTRY_DATA_FLAVOR, dataFlavor);
    }

    @Override
    public Object getTransferData(final DataFlavor dataFlavor) throws UnsupportedFlavorException, IOException {
        if (!Arrays.asList(getTransferDataFlavors()).contains(dataFlavor)) {
            throw new UnsupportedFlavorException(dataFlavor);
        }
        return this;
    }

}
