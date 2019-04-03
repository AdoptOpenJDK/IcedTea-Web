/*
Copyright (C) 2012 Red Hat, Inc.

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
package org.jacoco.operator;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.data.SessionInfoStore;

/**
 * Task for merging a set of execution data store files into a single file
 * 
 * Inspired by:
 * https://raw.github.com/jacoco/jacoco/master/org.jacoco.ant/src/org/jacoco/ant/MergeTask.java
 */
public class MergeTask implements Runnable {

    public static final String DEFAULT_NAME = "jacoco.exec";
    private File destfile;
    private final List<File> files = new ArrayList<File>(1);

    public MergeTask(File destfile) {
        this.destfile = destfile;
    }

    public MergeTask(File destfile, List<File> inputs) {
        this.destfile = destfile;
        files.addAll(inputs);
    }

    /**
     * Sets the location of the merged data store
     *
     * @param destfile Destination data store location
     */
    public void setDestfile(final File destfile) {
        this.destfile = destfile;
    }

    public void addInputFile(final File input) {
        if (input != null) {
            files.add(input);
        }
    }

    public void addInputFiles(final List<File> input) {
        files.addAll(input);
    }

    public void execute() throws IOException {
        if (destfile == null) {
            throw new RuntimeException("Destination file must be supplied");
        }

        final SessionInfoStore infoStore = new SessionInfoStore();
        final ExecutionDataStore dataStore = new ExecutionDataStore();

        loadSourceFiles(infoStore, dataStore);

        OutputStream outputStream = null;
        try {

            outputStream = new BufferedOutputStream(new FileOutputStream(
                    destfile));
            final ExecutionDataWriter dataWriter = new ExecutionDataWriter(
                    outputStream);
            infoStore.accept(dataWriter);
            dataStore.accept(dataWriter);
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }

    }

    private void loadSourceFiles(final SessionInfoStore infoStore, final ExecutionDataStore dataStore) throws IOException {
        if (files == null || files.isEmpty()) {
            throw new RuntimeException("No input files");
        }
        final Iterator<?> resourceIterator = files.iterator();
        while (resourceIterator.hasNext()) {
            final File resource = (File) resourceIterator.next();

            if (resource.isDirectory()) {
                continue;
            }
            InputStream resourceStream = null;
            try {
                resourceStream = new FileInputStream(resource);
                final ExecutionDataReader reader = new ExecutionDataReader(
                        resourceStream);
                reader.setSessionInfoVisitor(infoStore);
                reader.setExecutionDataVisitor(dataStore);
                reader.read();
            } finally {
                if (resourceStream != null) {
                    resourceStream.close();
                }
            }
        }
    }
    
    @Override
    public void run() {
        try {
            execute();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
