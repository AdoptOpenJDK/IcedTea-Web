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

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.lockingfile.LockableFile;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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

    private final static Logger LOG = LoggerFactory.getLogger(PropertiesFile.class);

    /**
     * the file to save to
     */
    private final LockableFile lockableFile;

    /**
     * the header string
     */
    private final String header;

    /**
     * time of last modification, lazy loaded on getProperty
     */
    private long lastStore;

    /**
     * Create a properties object backed by the specified file.
     *
     * @param file the file to save and load to
     */
    public PropertiesFile(final File file) {
        this(file, "netx file");
    }

    /**
     * Create a properties object backed by the specified file.
     *
     * @param file   the file to save and load to
     * @param header the file header
     */
    public PropertiesFile(final File file, final String header) {
        this.lockableFile = LockableFile.getInstance(file);
        this.header = header;
    }

    /**
     * @return the value of the specified key, or null if the key
     * does not exist.
     */
    @Override
    public String getProperty(final String key) {
        if (lastStore == 0) {
            load();
        }
        return super.getProperty(key);
    }

    /**
     * @return the value of the specified key, or the default value
     * if the key does not exist.
     */
    @Override
    public String getProperty(final String key, final String defaultValue) {
        if (lastStore == 0) {
            load();
        }
        return super.getProperty(key, defaultValue);
    }

    /**
     * Sets the value for the specified key.
     *
     * @return the previous value
     */
    @Override
    public Object setProperty(final String key, final String value) {
        if (lastStore == 0) {
            load();
        }
        return super.setProperty(key, value);
    }

    /**
     * @return the file backing this properties object.
     */
    public File getStoreFile() {
        return lockableFile.getFile();
    }

    /**
     * Ensures that the file backing these properties has been
     * loaded; call this method before calling any method defined by
     * a superclass.
     *
     * @return true, if file was (re-)loaded
     * false, if file was still current
     */
    public boolean load() {
        final File file = lockableFile.getFile();
        if (!file.exists()) {
            return false;
        }

        final long currentStore = file.lastModified();
        final long currentTime = System.currentTimeMillis();

        /* (re)load file, if
         *  - it wasn't loaded/stored, yet (lastStore == 0)
         *  - current file modification timestamp has changed since last store (currentStore != lastStore) OR
         *  - current file modification timestamp has not changed since last store AND current system time equals current file modification timestamp
         *    This is necessary because some filesystems seems only to provide accuracy of the timestamp on the level of seconds!
         */
        if (lastStore == 0 || currentStore != lastStore || (currentStore == lastStore && currentStore / 1000 == currentTime / 1000)) {
            try (InputStream s = new FileInputStream(file)) {
                load(s);
                lastStore = currentStore;
                return true;
            } catch (final IOException ex) {
                LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
            }
        }
        return false;
    }

    /**
     * Saves the properties to the file.
     */
    public void store() {
        final File file = lockableFile.getFile();
        try (final FileOutputStream s = new FileOutputStream(file)) {
            file.getParentFile().mkdirs();
            store(s, header);
            // fsync()
            s.getChannel().force(true);
            lastStore = file.lastModified();
        } catch (final IOException ex) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
        }
    }

    public void lock() {
        try {
            lockableFile.lock();
        } catch (final IOException e) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
        }
    }

    public boolean tryLock() {
        try {
            return lockableFile.tryLock();
        } catch (final IOException e) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
        }
        return false;
    }

    /**
     * Unlocks the file. Does not do anything if not holding the lock.
     */

    public void unlock() {
        try {
            lockableFile.unlock();
        } catch (final IOException e) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
        }
    }

    public boolean isHeldByCurrentThread() {
        return lockableFile.isHeldByCurrentThread();
    }

}
