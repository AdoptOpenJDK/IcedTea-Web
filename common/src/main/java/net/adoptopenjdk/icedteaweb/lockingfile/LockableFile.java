/*
 Copyright (C) 2013 Red Hat, Inc.

 This file is part of IcedTea.

 IcedTea is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by
 the Free Software Foundation, version 2.

 IcedTea is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with IcedTea; see the file COPYING.  If not, write to
 the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 02110-1301 USA.

 Linking this library statically or dynamically with other modules is
 making a combined work based on this library.  Thus, the terms and
 conditions of the GNU General Public License cover the whole
 combination.

 As a special exception, the copyright holders of this library give you
 permission to link this library with independent modules to produce an
 executable, regardless of the license terms of these independent
 modules, and to copy and distribute the resulting executable under
 terms of your choice, provided that you also meet, for each linked
 independent module, the terms and conditions of the license of that
 module.  An independent module is a module which is not derived from
 or based on this library.  If you modify this library, you may extend
 this exception to your version of the library, but you are not
 obligated to do so.  If you do not wish to do so, delete this
 exception statement from your version.
 */
package net.adoptopenjdk.icedteaweb.lockingfile;

import net.adoptopenjdk.icedteaweb.os.OsUtil;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantLock;

/*
 * Process & thread locked access to a file. Creates file if it does not already exist.
 */
public class LockableFile {

    private static final Map<File, LockableFile> instanceCache = new WeakHashMap<>();

    /**
     * Get a LockedFile for a given File. Ensures that we share the same
     * instance for all threads
     *
     * @param file the file to lock
     * @return a LockedFile instance
     */
    public static LockableFile getInstance(final File file) {
        if (instanceCache.containsKey(file)) {
            return instanceCache.get(file);
        }

        synchronized (instanceCache) {
            if (instanceCache.containsKey(file)) {
                return instanceCache.get(file);
            }

            final LockableFile lockableFile = new LockableFile(file);
            instanceCache.put(file, lockableFile);
            return lockableFile;
        }
    }

    private final File file;
    private final boolean readOnly;

    // internal modifiable state.
    // these fields are not exposed but are used within this class
    private final ReentrantLock threadLock = new ReentrantLock();
    private RandomAccessFile randomAccessFile;
    private FileChannel fileChannel;
    private FileLock processLock;


    private LockableFile(final File file) {
        this.file = file;
        try {
            //just try to create
            this.file.createNewFile();
        } catch (final Exception ex) {
            //intentionally silent
        }
        this.readOnly = isReadOnly(this.file);
    }

    private boolean isReadOnly(final File file) {
        boolean result;
        if (!file.isFile() && file.getParentFile() != null && !file.getParentFile().canWrite()) {
            result = true;
        } else {
            result = !file.canWrite();
            if (!result && file.getParentFile() != null && !file.getParentFile().canWrite()) {
                result = true;
            }
        }
        return result;
    }

    /**
     * Get the file being locked.
     *
     * @return the file
     */
    public File getFile() {
        return file;
    }

    /**
     * @return if the file is read only.
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Lock access to the file.Lock is reentrant.
     * @throws java.io.IOException if an I/O error occurs.
     */
    public void lock() throws IOException {
        // Create if does not already exist, cannot lock non-existing file
        if (!isReadOnly()) {
            this.file.createNewFile();
        }

        this.threadLock.lock();
        lockProcess();
    }

    public boolean tryLock() throws IOException {
        if (this.threadLock.tryLock()) {
            lockProcess();
            return true;
        } else {
            return false;
        }
    }

    private void lockProcess() throws IOException {
        if (OsUtil.isWindows()) {
            return;
        }

        if (this.processLock != null) {
            return;
        }

        if (this.file.exists()) {
            this.randomAccessFile = new RandomAccessFile(this.file, isReadOnly() ? "r" : "rws");
            this.fileChannel = randomAccessFile.getChannel();
            if (!isReadOnly()){
                this.processLock = this.fileChannel.lock();
            }
        }
    }

    /**
     * Unlock access to the file.Lock is reentrant. Does not do anything if not holding the lock.
     * @throws java.io.IOException if an I/O error occurs.
     */
    public void unlock() throws IOException {
        if (this.threadLock.isHeldByCurrentThread()) {
            try {
                if (this.threadLock.getHoldCount() == 1) {
                    unlockProcess();
                }
            } finally {
                this.threadLock.unlock();
            }
        }
    }

    private void unlockProcess() throws IOException {
        if (OsUtil.isWindows()) {
            return;
        }

        if (this.processLock != null) {
            this.processLock.release();
        }
        this.processLock = null;
        //necessary for read only file
        if (this.randomAccessFile != null) {
            this.randomAccessFile.close();
        }
        //necessary for not existing parent directory
        if (this.fileChannel != null) {
            this.fileChannel.close();
        }
    }

    public boolean isHeldByCurrentThread() {
        return this.threadLock.isHeldByCurrentThread();
    }
}
