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

package net.adoptopenjdk.icedteaweb.jnlp.element.resource;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionString;
import net.adoptopenjdk.icedteaweb.xmlparser.ParseException;

import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * The java element (or the j2se element) (sub-element of resources) specifies what Java Runtime Environment (JRE)
 * versions an application is supported on, as well as standard parameters to the Java Virtual Machine.
 * Several JREs can be specified in the JNLP file, which indicates a prioritized list of the supported JREs,
 * with the most preferred version first.
 *
 * @implSpec See <b>JSR-56, Section 4.6 Java Runtime Environment</b>
 * for a detailed specification of this class.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.5 $
 */
public class JREDesc {
    public static final String VERSION_ATTRIBUTE = "version";
    public static final String HREF_ATTRIBUTE = "href";
    public static final String JAVA_VM_ARGS_ATTRIBUTE = "java-vm-args";
    public static final String INITIAL_HEAP_SIZE_ATTRIBUTE = "initial-heap-size";
    public static final String MAX_HEAP_SIZE_ATTRIBUTE = "max-heap-size";

    private static final Pattern heapPattern= Pattern.compile("\\d+[kmg]?");

    /** The platform version or product version according to JSR-56, section 4.6.1 Java Runtime Environment Version Specification
     *  if location is not null */
    private final VersionString version;

    /** the location of a JRE product or null */
    private final URL location;

    /** initial heap size */
    private final String initialHeapSize;

    /** maximum head size */
    private final String maximumHeapSize;

    /** args to pass to the vm */
    private final String vmArgs;

    /** list of ResourceDesc objects */
    private final List<ResourcesDesc> resources;

    /**
     * Create a JRE descriptor.
     *
     * @param version the platform version or the product version
     * if location is not null
     * @param location the location of a JRE product or null
     * @param vmArgs arguments to VM
     * @param initialHeapSize initial heap size
     * @param maximumHeapSize maximum head size
     * @param resources list of ResourceDesc objects
     * @throws ParseException is something goes wrong
     */
    public JREDesc(final VersionString version, final URL location,
                   final String vmArgs, final String initialHeapSize,
                   final String maximumHeapSize, final List<ResourcesDesc> resources) throws ParseException {
        this.version = version;
        this.location = location;
        this.vmArgs = vmArgs;
        this.initialHeapSize = checkHeapSize(initialHeapSize);
        this.maximumHeapSize = checkHeapSize(maximumHeapSize);
        this.resources = resources;
    }

    /**
     * @return the JRE version.  Use isPlatformVersion to
     * determine if this version corresponds to a platform or
     * product version.
     */
    public VersionString getVersion() {
        return version;
    }

    /**
     * @return true if the JRE version is a Java platform version
     * (java.specification.version property) or false if it is a
     * product version (java.version property).
     */
    public boolean isPlatformVersion() {
        return getLocation() == null;
    }

    /**
     * @return the JRE version string.
     */
    public URL getLocation() {
        return location;
    }

    /**
     * @return the maximum heap size in bytes.
     */
    public String getMaximumHeapSize() {
        return maximumHeapSize;
    }

    /**
     * @return the initial heap size in bytes.
     */
    public String getInitialHeapSize() {
        return initialHeapSize;
    }

    /**
     * @return the resources defined for this JRE.
     */
    public List<ResourcesDesc> getResourcesDesc() {
        return resources;
    }

    /**
     * @return the additional arguments to pass to the Java VM
     * Can be null
     */
    public String getVMArgs() {
        return vmArgs;
    }

    /**
     * Check for valid heap size string
     * @return trimmed heapSize if correct
     * @throws ParseException if heapSize is invalid
     */
    static String checkHeapSize(final String heapSize) throws ParseException {
        // need to implement for completeness even though not used in netx
        if (heapSize == null) {
            return null;
        }
        final String realHeapSize = heapSize.trim();
        // the last character must be 0-9 or k/K/m/M/g/G
        //0 or 0k/m/g is also accepted value
        String heapSizeLower = realHeapSize.toLowerCase();
        Matcher heapMatcher = heapPattern.matcher(heapSizeLower);
        if (!heapMatcher.matches()) {
            throw new ParseException("Invalid value for heap size (" + realHeapSize + ")");
        }
        return realHeapSize;
    }

}
