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
 * Specifies the level of permissions that the applet needs to run. Specify "sandbox" for the
 * value to run in the sandbox. Specify "all-permissions" to run outside the sandbox.
 * <p/>
 * Applet permissions are specified in the applet html page file as sub element
 * {@code <param name="permissions" value="sandbox"/>} of the applet tag.
 *
 * If this parameter is omitted, {@code default} is assumed which is determined by
 * the {@code Permissions} attribute in the manifest for the main JAR file.
 *
 * (see https://docs.oracle.com/javase/8/docs/technotes/guides/deploy/applet_dev_guide.html#JSDPG709).
 * <p/>
 *  or the
 * {@code Permissions: sandbox} attribute in the manifest for the main JAR file.
 *
 * @see SecurityDesc
 */
public enum AppletPermissionLevel {
    /**
     * The all-permissions indicates that the RIA needs full access the the local
     * system and network.
     */
    ALL("all-permissions"),

    /**
     * Indicates that the RIA runs in the security sandbox and does not require additional permissions.
     */
    SANDBOX("sandbox"),

    /**
     * Indicates that the level of permissions is determined by the {@code Permissions} attribute in the manifest
     * for the main JAR file.
     */
    DEFAULT("default"),

    /**
     * Indicates that no applet permissions are specified.
     * @deprecated
     */
    @Deprecated
    // consider to handle the absence of an permissions param different
    // as there is no such thing as NONE in the specs
    NONE(null);

    private final String value;

    AppletPermissionLevel(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
