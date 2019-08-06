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

package net.adoptopenjdk.icedteaweb.integration.testcase1.applications;

import javax.jnlp.PersistenceService;
import java.util.Arrays;

import static net.adoptopenjdk.icedteaweb.integration.common.ManagedApplicationFileWriter.writeFile;

/**
 * This class represents a basic IcedTea-Web managed application. It is intended to be launched by integration
 * tests to test the launching and application environment of IcedTea-Web (see the launch sequence description
 * in JSR-56, section 5.1 Launch Sequence for details).
 *
 * <p></p>
 *
 * The basic functionality of this class is intended to test the proper download, update and execution according
 * to the definitions in the JNLP files used by the integration tests.
 *
 * <p></p>
 *
 * Functionality provided:
 * <ul>
 * <li> store the system properties to a file using {@link PersistenceService} </li>
 * <li> store the system environment to a file using {@link PersistenceService}</li>
 * <li> store this program's arguments to a file using {@link PersistenceService}</li>
 * </ul>
 */
public class SecureJavaApplication {

    public static final String HELLO_FILE = "hello.txt";
    public static final String ARGUMENTS_FILE = "arguments";

    public static void main(String[] args) throws Exception {
        System.out.println("Simple Java application install and launched by Iced-Tea Web");
        System.out.println("Arguments: " +  Arrays.toString(args));

        writeFile(HELLO_FILE, "Hello from managed app\n");
        writeFile(ARGUMENTS_FILE, Arrays.toString(args));
    }
}
