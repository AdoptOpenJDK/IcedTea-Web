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

import java.io.*;
import java.net.*;
import java.util.*;

import net.sourceforge.jnlp.util.logging.OutputController;

/**
 * The extension element.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.8 $
 */
public class ExtensionDesc {

    /** the extension name */
    private final String name;

    /** the required extension version */
    private final Version version;

    /** the location of the extension JNLP file */
    private final URL location;

    /** the JNLPFile the extension refers to */
    private JNLPFile file;

    /** map from ext-part to local part */
    private final Map<String, String> extToPart = new HashMap<>();

    /** eager ext parts */
    private final List<String> eagerExtParts = new ArrayList<>();

    /**
     * Create an extention descriptor.
     *
     * @param name the extension name
     * @param version the required version of the extention JNLPFile
     * @param location the location of the extention JNLP file
     */
    public ExtensionDesc(String name, Version version, URL location) {
        this.name = name;
        this.version = version;
        this.location = location;
    }

    /**
     * Adds an extension part to be downloaded when the specified
     * part of the main JNLP file is loaded.  The extension part
     * will be downloaded before the application is launched if the
     * lazy value is false or the part is empty or null.
     *
     * @param extPart the part name in the extension file
     * @param part the part name in the main file
     * @param lazy whether to load the part before launching
     */
    protected void addPart(String extPart, String part, boolean lazy) {
        extToPart.put(extPart, part);

        if (!lazy || part == null || part.length() == 0)
            eagerExtParts.add(extPart);
    }

    /**
     * @param thisPart unimplemented
     * @return the parts in the extension JNLP file mapped to the
     * part of the main file.
     */
    public String[] getExtensionParts(String thisPart) {

        return null;
    }

    /**
     * @return the name of the extension.
     */
    public String getName() {
        return name;
    }

    /**
     * @return the required version of the extension JNLP file.
     */
    public Version getVersion() {
        return version;
    }

    /**
     * @return the location of the extension JNLP file.
     */
    public URL getLocation() {
        return location;
    }

    /**
     * Resolves the extension by creating a JNLPFile from the file
     * specified by the extension's location property.
     *
     * @throws IOException if the extension JNLPFile could not be resolved.
     * @throws ParseException if the extension JNLPFile could not be
     * parsed or was not a component or installer descriptor.
     */
    public void resolve() throws ParseException, IOException {
        if (file == null) {
            file = new JNLPFile(location);

            OutputController.getLogger().log("Resolve: " + file.getInformation().getTitle());

            // check for it being an extension descriptor
            if (!file.isComponent() && !file.isInstaller())
                throw new ParseException(R("JInvalidExtensionDescriptor", name, location));
        }

    }

    /**
     * @return a JNLPFile for the extension, or null if the JNLP
     * file has not been resolved.
     */
    public JNLPFile getJNLPFile() {
        return file;
    }
}
