// Copyright (C) 2001-2003 Jon A. Maxwell (JAM)
// Copyright (C) 2019 Karakun AG
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

package net.adoptopenjdk.icedteaweb.jnlp.element.extension;

import net.adoptopenjdk.icedteaweb.jnlp.element.EntryPoint;

/**
 * A JNLP file is an installer extension if the installer-desc element is specified. It describes an
 * application that is executed only once, the first time the JNLP file is used on the local system.
 * <p/>
 * The installer extension is intended to install platform-specific native code that requires a more
 * complicated setup than simply loading a native library into the JVM, such as installing a JRE or
 * device driver. The installer executed by the JNLP Client must be a Java Technology-based application.
 * Note that this does not limit the kind of code that can be installed or executed. For example, the
 * installer could be a thin wrapper that executes a traditional native installer, executes a shell
 * script, or unzips a ZIP file with native code onto the disk.
 * <p/>
 * @implSpec See <b>JSR-56, Section 3.8.2 Installer Extension</b>
 * for a detailed specification of this class.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.6 $
 */
public class InstallerDesc implements EntryPoint {
    public static final String INSTALLER_DESC_ELEMENT = "installer-desc";
    public static final String PROGRESS_CLASS_ATTRIBUTE = "progress-class";

    /**
     * For Java applications this method returns the name of the class containing the public static
     * void main(String[]) method of an installer/uninstaller for this extension. This attribute can
     * be omitted if the main class can be found from the Main-Class manifest entry in the main JAR file.
     */
    private final String mainClass;

    /**
     * The name of a class containing an implementation of the {@link javax.jnlp.DownloadServiceListener}
     * interface of applications. May be used to indicate download progress.
     */
    private final String progressClass;

    /**
     * Creates an installer descriptor element.
     *
     * @param mainClass the fully qualified name of the class containing the main method of the application
     */
    public InstallerDesc(final String mainClass) {
        this(mainClass, null);
    }

    /**
     * Creates an installer descriptor element.
     *
     * @param mainClass the fully qualified name of the class containing the main method of the application
     * @param progressClass the fully qualified name of the class containing an implementation of the
     * {@link javax.jnlp.DownloadServiceListener} interface
     */
    public InstallerDesc(final String mainClass, final String progressClass) {
        this.mainClass = mainClass;
        this.progressClass = progressClass;
    }

    /**
     * The name of the class containing the public static void main(String[]) method of an
     * installer/uninstaller for this extension.
     * <p/>
     * This attribute can be omitted if the main class can be found from the Main-Class manifest entry
     * in the main JAR file.
     *
     * @return the fully qualified name of the class containing the main method of the application
     */
    @Override
    public String getMainClass() {
        return mainClass;
    }

    /**
     * The name of a class containing an implementation of the {@link javax.jnlp.DownloadServiceListener}
     * interface of applications.
     *
     * @return the fully qualified name of the class containing an implementation of the
     * {@link javax.jnlp.DownloadServiceListener} interface
     */
    public String getProgressClass() {
        return progressClass;
    }
}
