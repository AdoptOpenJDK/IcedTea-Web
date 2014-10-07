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
package net.sourceforge.jnlp.util.lockingfile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantLock;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

/*
 * Process & thread locked access to a file. Creates file if it does not already exist.
 */
public class LockedFile {

    // The file for access
    private RandomAccessFile randomAccessFile;
    private FileChannel fileChannel;
    private File file;
    // A file lock will protect against locks for multiple
    // processes, while a thread lock is still needed within a single JVM.
    private FileLock processLock = null;
    private ReentrantLock threadLock = new ReentrantLock();
    private boolean readOnly;

    private LockedFile(File file) {
        this.file = file;
        try {
            //just try to create
            this.file.createNewFile();
        } catch (Exception ex) {
            //intentionaly silent
        }
        if (!this.file.isFile() && file.getParentFile() != null && !file.getParentFile().canWrite()) {
            readOnly = true;
        } else {
            this.readOnly = !file.canWrite();
            if (!readOnly && file.getParentFile() != null && !file.getParentFile().canWrite()) {
                readOnly = true;
            }
        }
    }

    public boolean isReadOnly() {
        return readOnly;
    }
    // Provide shared access to LockedFile's via weak map
    static private final Map<File, LockedFile> instanceCache = new WeakHashMap<File, LockedFile>();

    /**
     * Get a LockedFile for a given File. Ensures that we share the same
     * instance for all threads
     *
     * @param file the file to lock
     * @return a LockedFile instance
     */
    synchronized public static LockedFile getInstance(File file) {
        if (!instanceCache.containsKey(file)) {
            instanceCache.put(file, new LockedFile(file));
        }

        return instanceCache.get(file);
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
     * Lock access to the file. Lock is reentrant.
     */
    public void lock() throws IOException {
        if (JNLPRuntime.isWindows()) {
            return;
        }
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
     * Unlock access to the file. Lock is reentrant. Does not do anything if not holding the lock.
     */
    public void unlock() throws IOException {
        if (JNLPRuntime.isWindows() || !this.threadLock.isHeldByCurrentThread()) {
            return;
        }
        boolean releaseProcessLock = (this.threadLock.getHoldCount() == 1);
        try {
            if (releaseProcessLock) {
                if (this.processLock != null){
                    this.processLock.release();
                }
                this.processLock = null;
                //necessary for read only file
                if (this.randomAccessFile != null){
                    this.randomAccessFile.close();
                }
                //necessary for not existing parent direcotry
                if (this.fileChannel != null){
                    this.fileChannel.close();
                }
            }
        } finally {
            this.threadLock.unlock();
        }
    }

    public boolean isHeldByCurrentThread() {
        return this.threadLock.isHeldByCurrentThread();
    }
}