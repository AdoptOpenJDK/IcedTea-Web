// Copyright (C) 2001-2003 Jon A. Maxwell (JAM)
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

package net.sourceforge.jnlp.util;

import net.adoptopenjdk.icedteaweb.lockingfile.LockableFile;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A properties object backed by a specified file without throwing
 * exceptions.  The properties are automatically loaded from the
 * file when the first property is requested, but the save method
 * must be called before changes are saved to the file.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.4 $
 */
public class PropertiesFile {

    private static final Logger LOG = LoggerFactory.getLogger(PropertiesFile.class);

    /**
     * actual properties
     */
    private final Properties delegate = new Properties();

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

    private boolean dirty;

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
    public String getProperty(final String key) {
        if (lastStore == 0) {
            load();
        }
        return delegate.getProperty(key);
    }

    /**
     * @return the value of the specified key, or the default value
     * if the key does not exist.
     */
    public String getProperty(final String key, final String defaultValue) {
        if (lastStore == 0) {
            load();
        }
        return delegate.getProperty(key, defaultValue);
    }

    /**
     * Sets the value for the specified key.
     *
     * @return the previous value
     */
    public Object setProperty(final String key, final String value) {
        if (lastStore == 0) {
            load();
        }
        dirty = true;
        return delegate.setProperty(key, value);
    }

    public boolean containsPropertyKey(String key) {
        if (lastStore == 0) {
            load();
        }
        return delegate.containsKey(key);
    }

    public boolean containsPropertyValue(String value) {
        if (lastStore == 0) {
            load();
        }
        return delegate.containsValue(value);
    }

    public void clear() {
        if (lastStore == 0) {
            load();
        }
        dirty = true;
        delegate.clear();
    }

    /**
     * Removes the key (and its corresponding value) from the properties.
     *
     * @return the previous value
     */
    public synchronized String remove(String key) {
        if (lastStore == 0) {
            load();
        }
        dirty = containsPropertyKey(key);
        return (String) delegate.remove(key);
    }

    public Set<Map.Entry<String, String>> entrySet() {
        if (lastStore == 0) {
            load();
        }
        return delegate.entrySet().stream()
                .filter(e -> e.getKey() instanceof String)
                .filter(e -> e.getValue() instanceof String)
                .map(e -> new SimpleImmutableEntry<>((String)e.getKey(), (String) e.getValue()))
                .collect(Collectors.toSet());
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

        if (dirty) {
            throw new IllegalStateException("loading dirty properties file");
        }

        /* (re)load file, if
         *  - it wasn't loaded/stored, yet (lastStore == 0)
         *  - current file modification timestamp has changed since last store (currentStore != lastStore) OR
         *  - current file modification timestamp has not changed since last store AND current system time equals current file modification timestamp
         *    This is necessary because some filesystems seems only to provide accuracy of the timestamp on the level of seconds!
         */
        if (dirty || lastStore == 0 || currentStore != lastStore || currentStore / 1000 == currentTime / 1000) {
            try (InputStream s = new FileInputStream(file)) {
                delegate.clear();
                delegate.load(s);
                lastStore = currentStore;
                dirty = false;
                return true;
            } catch (final IOException ex) {
                LOG.error("Failed to load", ex);
            }
        } else {
            LOG.debug("not re-loading the properties file {}", lockableFile.getFile());
        }
        return false;
    }

    /**
     * Saves the properties to the file.
     */
    public void store() {
        if (lockableFile.isReadOnly()) {
            LOG.warn("Cannot save properties as the file is read only");
            return;
        }
        final File file = lockableFile.getFile();
        try (final FileOutputStream s = new FileOutputStream(file)) {
            delegate.store(s, header);
            // fsync()
            s.getChannel().force(true);
            lastStore = file.lastModified();
            dirty = false;
        } catch (final IOException ex) {
            LOG.error("Failed to store", ex);
        }
    }

    public void lock() {
        try {
            lockableFile.lock();
        } catch (final IOException e) {
            LOG.error("Error while trying to lock file " + lockableFile.getFile().getName(), e);
        }
    }

    public boolean tryLock() {
        return lockableFile.tryLock();
    }

    /**
     * Unlocks the file. Does not do anything if not holding the lock.
     */

    public void unlock() {
        try {
            lockableFile.unlock();
        } catch (final IOException e) {
            LOG.error("Failed to unlock", e);
        }
    }

    public boolean isHeldByCurrentThread() {
        return lockableFile.isHeldByCurrentThread();
    }
}
