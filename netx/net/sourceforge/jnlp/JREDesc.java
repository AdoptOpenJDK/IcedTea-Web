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

package net.sourceforge.jnlp;

import static net.sourceforge.jnlp.runtime.Translator.R;

import java.net.*;
import java.util.*;

/**
 * The J2SE/Java element.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.5 $
 */
public class JREDesc {

    /** the platform version or the product version if location is not null */
    private Version version;

    /** the location of a JRE product or null */
    private URL location;

    /** inital heap size */
    private String initialHeapSize;

    /** maximum head size */
    private String maximumHeapSize;

    /** args to pass to the vm */
    private String vmArgs;

    /** list of ResourceDesc objects */
    private List resources;

    /**
     * Create a JRE descriptor.
     *
     * @param version the platform version or the product version
     * if location is not null
     * @param location the location of a JRE product or null
     * @param initialHeapSize inital heap size
     * @param maximumHeapSize maximum head size
     * @param resources list of ResourceDesc objects
     */
    public JREDesc(Version version, URL location,
            String vmArgs, String initialHeapSize,
            String maximumHeapSize, List resources) throws ParseException {
        this.version = version;
        this.location = location;
        this.vmArgs = vmArgs;
        checkHeapSize(initialHeapSize);
        this.initialHeapSize = initialHeapSize;
        checkHeapSize(maximumHeapSize);
        this.maximumHeapSize = maximumHeapSize;
        this.resources = resources;
    }

    /**
     * Returns the JRE version.  Use isPlatformVersion to
     * determine if this version corresponds to a platform or
     * product version.
     */
    public Version getVersion() {
        return version;
    }

    /**
     * Returns true if the JRE version is a Java platform version
     * (java.specification.version property) or false if it is a
     * product version (java.version property).
     */
    public boolean isPlatformVersion() {
        return getLocation() == null;
    }

    /**
     * Returns the JRE version string.
     */
    public URL getLocation() {
        return location;
    }

    /**
     * Returns the maximum heap size in bytes.
     */
    public String getMaximumHeapSize() {
        return maximumHeapSize;
    }

    /**
     * Returns the initial heap size in bytes.
     */
    public String getInitialHeapSize() {
        return initialHeapSize;
    }

    /**
     * Returns the resources defined for this JRE.
     */
    public List getResourcesDesc() {
        return resources;
    }

    /**
     * Returns the additional arguments to pass to the Java VM
     * Can be null
     */
    public String getVMArgs() {
        return vmArgs;
    }

    /**
     * Check for valid heap size string
     * @throws ParseException if heapSize is invalid
     */
    static private void checkHeapSize(String heapSize) throws ParseException {
        // need to implement for completeness even though not used in netx
        if (heapSize == null) {
            return;
        }

        boolean lastCharacterIsDigit = true;
        // the last character must be 0-9 or k/K/m/M
        char lastChar = Character.toLowerCase(heapSize.charAt(heapSize.length() - 1));
        if ((lastChar < '0' || lastChar > '9')) {
            lastCharacterIsDigit = false;
            if (lastChar != 'k' && lastChar != 'm') {
                throw new ParseException(R("PBadHeapSize", heapSize));
            }
        }

        int indexOfLastDigit = heapSize.length() - 1;
        if (!lastCharacterIsDigit) {
            indexOfLastDigit = indexOfLastDigit - 1;
        }

        String size = heapSize.substring(0, indexOfLastDigit);
        try {
            // check that the number is a number!
            Integer.valueOf(size);
        } catch (NumberFormatException numberFormat) {
            throw new ParseException(R("PBadHeapSize", heapSize), numberFormat);
        }

    }

}
