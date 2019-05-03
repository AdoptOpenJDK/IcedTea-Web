// Copyright (C) 2019 Karakun AG
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

package net.adoptopenjdk.icedteaweb.jnlp.element.security;

/**
 * The permissions that could be requested by a trusted environment. JSR-56 specifies two trusted environments,
 * the all-permissions environment and an environment that meets the security specifications of the J2EE
 * Application Client environment.
 * <p/>
 * JSR-56, Section 5.6 also lists the two default permission sets that must be granted to the application's
 * resources to a trusted application or applet requesting all-permissions or
 * j2ee-application-client-permissions.
 * <p/>
 *
 * @implSpec See <b>JSR-56, Section 5.6 Trusted Environments</b>
 * for a detailed specification of this class.
 * @see SecurityDesc
 */
public enum ApplicationPermissionLevel {
    /**
     * The all-permissions indicates that the application needs full access the the local
     * system and network.
     */
    ALL("all-permissions"),

    /**
     * The j2ee-application-client-permissions element indicates that the application needs the set of
     * permissions defined for a J2EE application client.
     */
    J2EE("j2ee-application-client-permissions"),

    /**
     * Indicates that no application permissions are specified.
     * @deprecated
     */
    @Deprecated
    // consider to handle the absence of a security element in the jnlp file different
    // as there is no such thing as NONE in the specs
    NONE(null);

    private final String value;

    ApplicationPermissionLevel(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
