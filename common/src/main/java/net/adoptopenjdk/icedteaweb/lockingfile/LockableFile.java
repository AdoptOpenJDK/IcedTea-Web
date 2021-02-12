/*
 Copyright (C) 2013 Red Hat, Inc.

 This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify it under the
terms of the GNU General Public License as published by the Free Software
Foundation, version 2.

IcedTea is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
IcedTea; see the file COPYING. If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is making a
combined work based on this library. Thus, the terms and conditions of the GNU
General Public License cover the whole combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent modules, and
to copy and distribute the resulting executable under terms of your choice,
provided that you also meet, for each linked independent module, the terms and
conditions of the license of that module. An independent module is a module
which is not derived from or based on this library. If you modify this library,
you may extend this exception to your version of the library, but you are not
obligated to do so. If you do not wish to do so, delete this exception
statement from your version.
*/
package net.adoptopenjdk.icedteaweb.lockingfile;

import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.os.OsUtil;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Process & thread lockable access to a file. Creates file if it does not already exist.
 */
public class LockableFile {

    private static final Logger logger = LoggerFactory.getLogger(LockableFile.class);

    private static final Map<File, LockableFile> instanceCache = new WeakHashMap<>();

    /**
     * Get a LockableFile for a given File.
     * Ensures that we share the same instance for all threads.
     *
     * @param file the file to lock
     * @return a LockableFile instance
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

    private final ReentrantLock threadLock = new ReentrantLock();
    private final InterProcessLock processLock;

    private LockableFile(final File file) {
        this.file = file;
        try {
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    throw new IOException("could not create file " + file);
                }
            } else if (!file.isFile()) {
                logger.error("lockable file {} is not a file but something else (maybe a directory)", file);
            }
        } catch (final Exception ex) {
            logger.error("Exception while creating lockable file", ex);
        }
        readOnly = isReadOnly(file);

        if (OsUtil.isWindows()) {
            processLock = new LockFile();
        } else {
            processLock = new NioFileLock();
        }
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
     *
     * @throws java.io.IOException if an I/O error occurs.
     */
    public void lock() throws IOException {
        // Create if does not already exist, cannot lock non-existing file
        if (!readOnly && !file.exists()) {
            if (!file.createNewFile()) {
                logger.error("Could not create the lockable file {}", file);
            }
        }

        threadLock.lock();
        processLock.lock();
    }

    public boolean tryLock() {
        try {
            if (threadLock.tryLock()) {
                processLock.lock();
                return true;
            }
        } catch (IOException e) {
            logger.debug("failed to acquire lock for {} because of {}", file, e.getMessage());
        }
        return false;
    }

    /**
     * Unlock access to the file.Lock is reentrant. Does not do anything if not holding the lock.
     *
     * @throws java.io.IOException if an I/O error occurs.
     */
    public void unlock() throws IOException {
        if (threadLock.isHeldByCurrentThread()) {
            try {
                if (threadLock.getHoldCount() == 1) {
                    processLock.unlock();
                }
            } finally {
                threadLock.unlock();
            }
        }
    }

    public boolean isHeldByCurrentThread() {
        return threadLock.isHeldByCurrentThread();
    }

    /**
     * Interface for abstracting away the details of how a file is locked system wide.
     */
    private interface InterProcessLock {
        void lock() throws IOException;
        void unlock() throws IOException;
    }

    /**
     * This is the recommended way to lock a file system wide (across multiple processes).
     *
     * Unfortunately this causes problems during unlocking in Windows.
     * https://bugs.java.com/bugdatabase/view_bug.do?bug_id=4715154
     */
    private class NioFileLock implements InterProcessLock {
        private RandomAccessFile randomAccessFile;
        private FileChannel fileChannel;
        private FileLock fileLock;


        @Override
        public void lock() throws IOException {
            if (fileLock != null) {
                return;
            }

            if (file.exists()) {
                randomAccessFile = new RandomAccessFile(file, readOnly ? "r" : "rws");
                fileChannel = randomAccessFile.getChannel();
                if (!readOnly) {
                    fileLock = fileChannel.lock();
                }
            }

        }

        @Override
        public void unlock() throws IOException {
            if (fileLock != null) {
                fileLock.release();
            }
            fileLock = null;
            //necessary for read only file
            if (randomAccessFile != null) {
                randomAccessFile.close();
            }
            //necessary for not existing parent directory
            if (fileChannel != null) {
                fileChannel.close();
            }
        }
    }

    /**
     * This is an alternative approach to locking a file system wide.
     *
     * This is not recommended as there are claims that this is not reliable.
     * Most likly there are issues with remote file systems but the claims do not go into detail.
     */
    private class LockFile implements InterProcessLock {

        private final File lockFile = new File(file.getAbsoluteFile().getParent(), file.getName() + ".lock");

        @Override
        public void lock() throws IOException {
            synchronized (LockFile.class) {
                if (!file.exists()) {
                    return;
                }

                final File dir = lockFile.getParentFile();
                if (!dir.isDirectory()) {
                    if (!dir.mkdirs()) {
                        logger.warn("failed to create parent directory of {}", lockFile);
                    }
                }
                if (!lockFile.createNewFile()) {
                    throw new IOException("Failed to create lock file " + lockFile.getAbsolutePath() + " because it already exists");
                }
                lockFile.deleteOnExit();
            }
        }

        @Override
        public void unlock() throws IOException {
            if (!lockFile.delete()) {
                if (lockFile.exists()) {
                    logger.error("Failed to delete lock file {}", lockFile);
                }
            }
        }
    }
}
