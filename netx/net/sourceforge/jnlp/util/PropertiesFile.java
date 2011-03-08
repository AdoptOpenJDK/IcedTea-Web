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

package net.sourceforge.jnlp.util;

import java.io.*;
import java.util.*;

/**
 * A properties object backed by a specified file without throwing
 * exceptions.  The properties are automatically loaded from the
 * file when the first property is requested, but the save method
 * must be called before changes are saved to the file.<p>
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.4 $
 */
public class PropertiesFile extends Properties {

    /** the file to save to */
    File file;

    /** the header string */
    String header = "netx file";

    /** lazy loaded on getProperty */
    boolean loaded = false;

    /**
     * Create a properties object backed by the specified file.
     *
     * @param file the file to save and load to
     */
    public PropertiesFile(File file) {
        this.file = file;
    }

    /**
     * Create a properties object backed by the specified file.
     *
     * @param file the file to save and load to
     * @param header the file header
     */
    public PropertiesFile(File file, String header) {
        this.file = file;
        this.header = header;
    }

    /**
     * Returns the value of the specified key, or null if the key
     * does not exist.
     */
    public String getProperty(String key) {
        if (!loaded)
            load();

        return super.getProperty(key);
    }

    /**
     * Returns the value of the specified key, or the default value
     * if the key does not exist.
     */
    public String getProperty(String key, String defaultValue) {
        if (!loaded)
            load();

        return super.getProperty(key, defaultValue);
    }

    /**
     * Sets the value for the specified key.
     *
     * @return the previous value
     */
    public Object setProperty(String key, String value) {
        if (!loaded)
            load();

        return super.setProperty(key, value);
    }

    /**
     * Returns the file backing this properties object.
     */
    public File getStoreFile() {
        return file;
    }

    /**
     * Ensures that the file backing these properties has been
     * loaded; call this method before calling any method defined by
     * a superclass.
     */
    public void load() {
        loaded = true;

        InputStream s = null;
        try {
            if (!file.exists())
                return;

            try {
                s = new FileInputStream(file);
                load(s);
            } finally {
                if (s != null) s.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Saves the properties to the file.
     */
    public void store() {
        if (!loaded)
            return; // nothing could have changed so save unnecessary load/save

        OutputStream s = null;
        try {
            try {
                s = new FileOutputStream(file);
                store(s, header);
            } finally {
                if (s != null) s.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
