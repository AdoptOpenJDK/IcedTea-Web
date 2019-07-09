// Copyright (C) 2001-2003 Jon A. Maxwell (JAM)
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

package net.adoptopenjdk.icedteaweb.jnlp.element.application;

import net.adoptopenjdk.icedteaweb.jnlp.element.EntryPoint;

import java.util.Arrays;
import java.util.List;

/**
 * The application-desc element contains all information needed to launch an application, given the resources
 * described by the resources element. A JNLP file is an application descriptor if the application-desc element
 * is specified.
 *
 * @implSpec See <b>JSR-56, Section 3.7.1 Application Descriptor for an Application</b>
 * for a detailed specification of this class.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 */
public class ApplicationDesc implements EntryPoint {
    public static final String APPLICATION_DESC_ELEMENT = "application-desc";
    public static final String JAVAFX_DESC_ELEMENT = "javafx-desc";
    public static final String ARGUMENT_ELEMENT = "argument";
    public static final String PROGRESS_CLASS_ATTRIBUTE = "progress-class";
    /**
     * The type of application supported by the JNLP Client. The optional attribute indicates the type of
     * application contained in the resources and identified by the main-class attribute. The default value
     * is {@link ApplicationType#JAVA}.
     * <p/>
     * If given the type attribute value is not supported by the JNLP Client, the launch should be aborted.
     * If a JNLP Client supports other types of applications (such as "JavaFX", or "JRuby"), The meaning and/or
     * use of the other application-desc attributes (main-class and progress-class) and sub-elements (argument
     * and param) may vary as is appropriate for that type of application.
     */
    private final ApplicationType type;

    /**
     * For Java applications this method returns the name of the class containing the public static
     * void main(String[]) method. The name and/or meaning may vary as is appropriate for other types
     * of applications.
     */
    private final String mainClass;

    /**
     * The name of a class containing an implementation of the {@link javax.jnlp.DownloadServiceListener}
     * interface of applications. May be used to indicate download progress.
     */
    private final String progressClass;

    /**
     * Contains an ordered list of arguments for the application.
     */
    private final List<String> arguments;

    /**
     * Creates an application descriptor element.
     *
     * @param mainClass the fully qualified name of the class containing the main method of the application
     * @param arguments the arguments
     */
    public ApplicationDesc(final String mainClass, final String[] arguments) {
        this(ApplicationType.JAVA, mainClass, arguments);
    }

    /**
     * Creates an application descriptor element.
     *
     * @param type the type of application supported by the JNLP Client
     * @param mainClass the fully qualified name of the class containing the main method of the application
     * @param arguments the arguments
     */
    public ApplicationDesc(final ApplicationType type, final String mainClass, final String[] arguments) {
        this(type, mainClass, null, arguments);
    }

   /**
     * Creates an application descriptor element.
     *
     * @param type the type of application supported by the JNLP Client
     * @param mainClass the fully qualified name of the class containing the main method of the application
     * @param progressClass the fully qualified name of the class containing an implementation of the
     * {@link javax.jnlp.DownloadServiceListener} interface
     * @param arguments the arguments
     */
   public ApplicationDesc(final ApplicationType type, final String mainClass, final String progressClass, final String[] arguments) {
        this.type = type;
        this.mainClass = mainClass;
        this.progressClass = progressClass;
        this.arguments = Arrays.asList(arguments);
    }

    /**
     * @return the type of application supported by the JNLP Client
     */
    public ApplicationType getType() {
        return type;
    }

    /**
     * For Java applications this method returns the name of the class containing the public static
     * void main(String[]) method. The name and/or meaning may vary as is appropriate for other types
     * of applications.
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

    /**
     * Contains an ordered list of arguments for the application.
     *
     * @return the arguments
     */
    public String[] getArguments() {
        return arguments.toArray(new String[0]);
    }

    /**
     * Add an argument to the end of the ordered list of arguments.
     *
     * @param argument argument of command
     */
    public void addArgument(final String argument) {
        arguments.add(argument);
    }
}
