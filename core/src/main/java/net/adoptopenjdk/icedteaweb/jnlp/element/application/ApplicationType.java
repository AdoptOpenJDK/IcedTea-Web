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

package net.adoptopenjdk.icedteaweb.jnlp.element.application;

/**
 * The type of application supported by the JNLP Client.
 * <p/>
 * If given the type attribute value is not supported by the JNLP Client, the launch should be aborted.
 * If a JNLP Client supports other types of applications (such as "JavaFX", or "JRuby"), The meaning and/or
 * use of the other application-desc attributes (main-class and progress-class) and sub-elements (argument
 * and param) may vary as is appropriate for that type of application.
 * <p/>
 *
 * @implSpec See <b>JSR-56, Section 3.7.1 Application Descriptor for an Application</b>
 * for a detailed specification of this class.
 */
public enum ApplicationType {
    /**
     * A value (default) indicates the application is a Java application.
     */
    JAVA,
    /**
     * A value indicates the application is a JavaFX application.
     */
    JAVAFX
}
