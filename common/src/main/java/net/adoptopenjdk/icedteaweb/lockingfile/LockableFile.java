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
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
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

        if (!file.exists()) {
            logger.debug("lockable file {} does not yet exist", file);
            createParentDirIfMissing(file);
            createLockableFile();
        } else if (!file.isFile()) {
            logger.error("lockable file {} is not a file but something else (maybe a directory)", file);
        }

        readOnly = isReadOnly(file);

        if (OsUtil.isWindows()) {
            processLock = new LockFile();
        } else {
            processLock = new NioFileLock();
        }
    }

    private boolean isReadOnly(final File file) {
        final boolean isWritable = canWriteFile(file) && canWriteParent(file);
        return !isWritable;
    }

    private boolean canWriteFile(File file) {
        return file != null && file.isFile() && file.canWrite();
    }

    private boolean canWriteParent(File file) {
        final File parent = file.getParentFile();
        return parent != null && parent.isDirectory() && parent.canWrite();
    }

    private void createParentDirIfMissing(File f) {
        final File dir = f.getParentFile();
        if (dir == null) {
            logger.warn("parent of file {} is null", f);
        } else if (!dir.isDirectory() && !dir.mkdirs()) {
            logger.warn("failed to create parent directory of {}", f);
        }
    }

    private void createLockableFile() {
        if (!canWriteParent(file)) {
            logger.debug("could not create lockable file as the parent is not writable");
            return;
        }

        try {
            if (!file.createNewFile()) {
                logger.warn("could not create file " + file);
            }
        } catch (IOException e) {
            logger.error("Exception while creating lockable file - " + file, e);
        }
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
        final boolean alreadyLocked = threadLock.isHeldByCurrentThread();
        threadLock.lock();
        if (alreadyLocked) {
            // early exit: process locking may not be reentrant causing an error when trying to lock the same file twice
            return;
        }

        createFileIfMissing();
        processLock.lock();
    }

    public boolean tryLock() {
        final boolean alreadyLocked = threadLock.isHeldByCurrentThread();
        final boolean threadLocked = threadLock.tryLock();
        if (alreadyLocked) {
            // early exit: process locking may not be reentrant causing an error when trying to lock the same file twice
            return true;
        }

        try {
            createFileIfMissing();
            final boolean processLocked = processLock.tryLock();
            if (threadLocked && !processLocked) {
                threadLock.unlock();
            }
            return threadLocked && processLocked;
        } catch (IOException e) {
            logger.debug("failed to acquire lock for {} because of {}", file, e.getMessage());
            return false;
        }
    }

    private void createFileIfMissing() throws IOException {
        if (!readOnly && !file.exists()) {
            if (!file.createNewFile()) {
                logger.error("Could not create the lockable file {}", file);
            }
        }
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
     * Note: the implementations may NOT be re-entrant and thus must not be called if
     * the thread is already holding a lock for a file.
     */
    private interface InterProcessLock {
        void lock() throws IOException;

        boolean tryLock() throws IOException;

        void unlock() throws IOException;
    }

    /**
     * This is the recommended way to lock a file system wide (across multiple processes).
     * <p>
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

            if (!file.exists()) {
                return;
            }

            randomAccessFile = new RandomAccessFile(file, readOnly ? "r" : "rws");
            fileChannel = randomAccessFile.getChannel();
            fileLock = fileChannel.lock();
        }

        @Override
        public boolean tryLock() throws IOException {
            if (fileLock != null) {
                return true;
            }

            if (!file.exists()) {
                return false;
            }

            randomAccessFile = new RandomAccessFile(file, readOnly ? "r" : "rws");
            fileChannel = randomAccessFile.getChannel();
            fileLock = fileChannel.tryLock();
            return fileLock != null;
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
     * <p>
     * This is not recommended as there are claims that this is not reliable.
     * Most likely there are issues with remote file systems but the claims do not go into detail.
     */
    private class LockFile implements InterProcessLock {

        public static final double STALENESS_INTERVAL_IN_SEC = 2.0;
        private final File lockFile = new File(file.getAbsoluteFile().getParent(), file.getName() + ".lock");

        @Override
        public void lock() throws IOException {
            if (!file.exists()) {
                return;
            }

            if (lockFile.exists()) {
                deleteStaleLockFile();
            } else {
                createParentDirIfMissing(lockFile);
            }

            while (!readOnly && !lockFile.createNewFile()) {
                try {
                    logger.debug("Trying to create lock file {}", lockFile.getPath());
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            lockFile.deleteOnExit();
        }

        @Override
        public boolean tryLock() throws IOException {
            if (!file.exists()) {
                return false;
            }

            if (lockFile.exists()) {
                deleteStaleLockFile();
            } else {
                createParentDirIfMissing(lockFile);
            }

            if (!readOnly && !lockFile.createNewFile()) {
                logger.debug("Could not create lock file {}", lockFile.getPath());
                return false;
            }
            lockFile.deleteOnExit();
            return true;
        }

        @Override
        public void unlock() throws IOException {
            if (!lockFile.delete()) {
                if (lockFile.exists()) {
                    logger.error("Failed to delete lock file {}", lockFile);
                }
            }
        }

        private void deleteStaleLockFile() throws IOException {
            final BasicFileAttributes attr = Files.readAttributes(lockFile.toPath(), BasicFileAttributes.class);
            final long currentTime = System.currentTimeMillis();
            final long createTime = attr.creationTime().toMillis();
            final double ageInSec = (currentTime - createTime) / 1000.0;
            if (ageInSec > STALENESS_INTERVAL_IN_SEC) {
                logger.debug("Deleting stale lock file {}", lockFile.getPath());
                unlock();
            }
        }
    }
}
