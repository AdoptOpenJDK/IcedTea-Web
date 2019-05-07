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

package net.adoptopenjdk.icedteaweb.manifest;

/**
 * The JAR file manifest contains information about the contents of the JAR file, including security
 * and configuration information. These attributes apply to signed applets and Web Start applications.
 *
 * @implSpec See https://docs.oracle.com/javase/8/docs/technotes/guides/deploy/manifest.html for more details
 * on the security and configuration attributes stored in the JAR file manifest.
 */
public enum ManifestAttributes {
    /**
     * The Permissions attribute is used to verify that the permissions level requested by the RIA when
     * it runs matches the permissions level that was set when the JAR file was created.
     */
    PERMISSIONS("Permissions"),
    /**
     * The Codebase attribute is used to restrict the code base of the JAR file to specific domains.
     * Use this attribute to prevent someone from re-deploying your application on another website
     * for malicious purposes.
     */
    CODEBASE("Codebase"),
    /**
     * The Application-Name attribute is used in security prompts to provide a title for your signed RIA.
     * Use of this attribute is recommended to help users make the decision to trust and run the RIA.
     */
    APPLICATION_NAME("Application-Name"),
    /**
     * The Application-Library-Allowable-Codebase attribute identifies the locations where your signed
     * RIA is expected to be found.
     */
    APPLICATION_LIBRARY_ALLOWABLE_CODEBASE("Application-Library-Allowable-Codebase"),
    /**
     * The Caller-Allowable-Codebase attribute is used to identify the domains from which JavaScript
     * code can make calls to your RIA without security prompts. Set this attribute to the domain
     * that hosts the JavaScript code.
     */
    CALLER_ALLOWABLE_CODEBASE("Caller-Allowable-Codebase"),
    /**
     * The Entry-Point attribute is used to identify the classes that are allowed to be used as entry points to
     * your RIA. Identifying the entry points helps to prevent unauthorized code from being run when a JAR file
     * has more than one class with a main() method, multiple Applet classes, or multiple JavaFX Application
     * classes. Set this attribute to the fully qualified class name that can be used as the entry point for
     * the RIA. To specify more than one class, separate the classes by a space.
     */
    ENTRY_POINT("Entry-Point"),
    /**
     * The Trusted-Only attribute is used to prevent untrusted classes or resources from being loaded for
     * an applet or application.
     */
    TRUSTED_ONLY("Trusted-Only"),
    /**
     * The Trusted-Library attribute is used for applications and applets that are designed to allow
     * untrusted components.
     */
    TRUSTED_LIBRARY("Trusted-Library");

    private final String name;


    ManifestAttributes(final String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }
}
