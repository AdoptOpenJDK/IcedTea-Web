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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Process-locked string storage backed by a file.
 * Each string is stored on its own line.
 * Any new-lines must be encoded somehow if they are to be stored.
 */
public abstract class LockingReaderWriter {

    private LockedFile lockedFile;

    /**
     * Create locking file-backed storage.
     * @param file the storage file
     */
    public LockingReaderWriter(File file) {
        this.lockedFile = LockedFile.getInstance(file);
    }

    /**
     * Get the underlying file. 
     * Any access to this file should use lock() & unlock().
     * 
     * @return the file
     */
    public File getBackingFile() {
        return this.lockedFile.getFile();
    }

    public boolean isReadOnly() {
        return this.lockedFile.isReadOnly();
    }

    /**
     * Lock the underlying storage. Lock is reentrant.
     */
    public void lock() {
        try {
            lockedFile.lock();
        } catch (IOException e) {
            throw new StorageIoException(e);
        }
    }

    /**
     * Unlock the underlying storage. Lock is reentrant.
     */
    public void unlock() {
        try {
            lockedFile.unlock();
        } catch (IOException e) {
            throw new StorageIoException(e);
        }
    }

    /**
     * Writes stored contents to file. Assumes lock is held.
     * @throws IOException
     */
    protected void writeContents() throws IOException {
        if (!getBackingFile().isFile()){
            return;
        }
        if (isReadOnly()){
            return;
        }
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(getBackingFile()), "UTF-8"));
            writeContent(writer);
            writer.flush();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    protected abstract void writeContent(BufferedWriter writer) throws IOException;

    /**
     * Reads contents from file. Assumes lock is held.
     * @throws IOException
     */
    protected void readContents() throws IOException {
        if (!getBackingFile().isFile()){
            return;
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(getBackingFile()), "UTF-8"));

            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                readLine(line);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    /**
     * Reads contents from the file, first acquring a lock.
     * @throws IOException
     */
    protected synchronized void readContentsLocked() throws IOException {
        doLocked(new Runnable() {

            @Override
            public void run() {
                try {
                    readContents();
                } catch (IOException ex) {
                    throw new StorageIoException(ex);
                }
            }
        });
    }

    /**
     * Write contents to the file, first acquring a lock.
     * @throws IOException
     */
    protected synchronized void writeContentsLocked() throws IOException {
        doLocked(new Runnable() {

            public void run() {
                try {
                    writeContents();
                } catch (IOException ex) {
                    throw new StorageIoException(ex);
                }
            }
        });

    }

    protected void doLocked(Runnable r) {
        lock();
        try {
            r.run();
        } finally {
            unlock();
        }
    }

    protected abstract void readLine(String line);
}
