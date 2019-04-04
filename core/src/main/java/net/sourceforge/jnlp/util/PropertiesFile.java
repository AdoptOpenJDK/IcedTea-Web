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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import net.sourceforge.jnlp.util.lockingfile.LockedFile;
import net.sourceforge.jnlp.util.logging.OutputController;

/**
 * A properties object backed by a specified file without throwing
 * exceptions.  The properties are automatically loaded from the
 * file when the first property is requested, but the save method
 * must be called before changes are saved to the file.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.4 $
 */
public class PropertiesFile extends Properties {

    /** the file to save to */
    LockedFile lockedFile;

    /** the header string */
    String header = "netx file";
    
    /** time of last modification, lazy loaded on getProperty */
    long lastStore;

    /**
     * Create a properties object backed by the specified file.
     *
     * @param file the file to save and load to
     */
    public PropertiesFile(File file) {
        this.lockedFile = LockedFile.getInstance(file);
    }

    /**
     * Create a properties object backed by the specified file.
     *
     * @param file the file to save and load to
     * @param header the file header
     */
    public PropertiesFile(File file, String header) {
        this.lockedFile = LockedFile.getInstance(file);
        this.header = header;
    }

    /**
     * @return the value of the specified key, or null if the key
     * does not exist.
     */
    @Override
    public String getProperty(String key) {
        if (lastStore == 0)
            load();

        return super.getProperty(key);
    }

    /**
     * @return the value of the specified key, or the default value
     * if the key does not exist.
     */
    @Override
    public String getProperty(String key, String defaultValue) {
        if (lastStore == 0)
            load();

        return super.getProperty(key, defaultValue);
    }

    /**
     * Sets the value for the specified key.
     *
     * @return the previous value
     */
    @Override
    public Object setProperty(String key, String value) {
        if (lastStore == 0)
            load();

        return super.setProperty(key, value);
    }

    /**
     * @return the file backing this properties object.
     */
    public File getStoreFile() {
        return lockedFile.getFile();
    }

    /**
     * Ensures that the file backing these properties has been
     * loaded; call this method before calling any method defined by
     * a superclass.
     * 
     * @return true, if file was (re-)loaded
     *         false, if file was still current
     */
    public boolean load() {
        File file = lockedFile.getFile();
        if (!file.exists()) {
            return false;
        }

        long currentStore = file.lastModified();
        long currentTime = System.currentTimeMillis();

        /* (re)load file, if
         *  - it wasn't loaded/stored, yet (lastStore == 0)
         *  - current file modification timestamp has changed since last store (currentStore != lastStore) OR
         *  - current file modification timestamp has not changed since last store AND current system time equals current file modification timestamp
         *    This is necessary because some filesystems seems only to provide accuracy of the timestamp on the level of seconds!
         */
        if(lastStore == 0 || currentStore != lastStore || (currentStore == lastStore && currentStore / 1000 == currentTime / 1000)) {
            InputStream s = null;
            try {

                try {
                    s = new FileInputStream(file);
                    load(s);
                } finally {
                    if (s != null) {
                        s.close();
                        lastStore=currentStore;
                        return true;
                    }
                }
            } catch (IOException ex) {
                OutputController.getLogger().log(OutputController.Level.ERROR_ALL, ex);
            }
        }

        return false;
    }

    /**
     * Saves the properties to the file.
     */
    public void store() {
        File file = lockedFile.getFile();
        FileOutputStream s = null;
        try {
            try {
                file.getParentFile().mkdirs();
                s = new FileOutputStream(file);
                store(s, header);

                // fsync()
                s.getChannel().force(true);
                lastStore = file.lastModified();
            } finally {
                if (s != null) s.close();
            }
        } catch (IOException ex) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, ex);
        }
    }

    public void lock() {
        try {
            lockedFile.lock();
        } catch (IOException e) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
        }
    }

    public boolean tryLock() {
        try {
            return lockedFile.tryLock();
        } catch (IOException e) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
        }
        return false;
    }

    /**
     * Unlocks the file. Does not do anything if not holding the lock.
     */

    public void unlock() {
        try {
            lockedFile.unlock();
        } catch (IOException e) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
        }
    }

    public boolean isHeldByCurrentThread() {
        return lockedFile.isHeldByCurrentThread();
    }

}
