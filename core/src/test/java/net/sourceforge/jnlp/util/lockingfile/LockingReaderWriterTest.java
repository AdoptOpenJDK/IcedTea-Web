/*
Copyright (C) 2013 Red Hat

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package net.sourceforge.jnlp.util.lockingfile;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class LockingReaderWriterTest {

    private static File storagefile;

    private static TestStringReaderWriter newInstance() {
        return new TestStringReaderWriter(storagefile);
    }

    @Before
    public void setUp() throws IOException {
        storagefile = File.createTempFile("foo", "bar");
    }

    @Test
    public void testSimpleActions() throws IOException {
        TestStringReaderWriter storage = newInstance();

        storage.add("teststring");
        assertTrue(storage.contains("teststring"));
        storage.remove("teststring");
        assertFalse(storage.contains("teststring"));
    }

    @Test
    public void testInterleavedActions() throws IOException {
        TestStringReaderWriter storage1 = newInstance();
        TestStringReaderWriter storage2 = newInstance();

        storage1.add("teststring");
        assertTrue(storage2.contains("teststring"));
        storage2.remove("teststring");
        assertFalse(storage1.contains("teststring"));
    }

    static class TestThread extends Thread {
        String testString;
        int iterations;
        Throwable error = null;

        TestThread(String testString, int iterations) {
            this.testString = testString;
            this.iterations = iterations;
        }

        @Override
        public void run() {
            try {
                TestStringReaderWriter storage = newInstance();
                for (int i = 0; i < iterations; i++) {
                    assertTrue(storage.contains(this.testString));
                    storage.add(this.testString);
                    storage.remove(this.testString);
                    assertTrue(storage.contains(this.testString));
                }
            } catch (Throwable error) {
                error.printStackTrace();
                this.error = error;
            }
        }
    }

    private void concurrentReadWrites(int threadAmount, int iterations,
            String testString) throws InterruptedException {
        TestStringReaderWriter storage = newInstance();

        storage.add(testString);

        List<TestThread> testThreads = new ArrayList<TestThread>();

        for (int i = 0; i < threadAmount; i++) {
            TestThread thread = new TestThread(testString, iterations);
            testThreads.add(thread);
            thread.start();
        }

        for (int i = 0; i < threadAmount; i++) {
            testThreads.get(i).join();
        }

        assertTrue(storage.contains(testString));
        storage.remove(testString);

        // So long as number adds == number writes, we should be left with
        // nothing at end.
        assertFalse(storage.contains(testString));
    }

    // Long testing string, the contents are not important
    private String makeLongTestString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append(Integer.toString(i));
        }
        return sb.toString();
    }

    @Test
    public void testManyReadWrite() throws Exception {
        int oneThread = 1;
        String shortString = "teststring";

        // This was causing 'too many open files' because FileUtils#getFileLock
        // leaks file descriptors. No longer used.
        concurrentReadWrites(oneThread, 500 /* iterations */, shortString);
    }

    @Test
    public void testManyThreads() throws Exception {
        int threadAmount = 25;
        String shortString = "teststring";
        String longString = makeLongTestString();

        concurrentReadWrites(threadAmount, 10 /* per-thread iterations */,
                shortString);
        concurrentReadWrites(threadAmount, 2 /* per-thread iterations */,
                longString);
    }

    /**
     * Concrete implementation to aid in testing LockingReaderWriter
     */
    public static class TestStringReaderWriter extends LockingReaderWriter {
        
        private List<String> cachedContents = new ArrayList<String>();
        
        public TestStringReaderWriter(File file) {
            super(file);
        }
        
        @Override
        public void writeContent(BufferedWriter writer) throws IOException {
            for (String string : cachedContents) {
                writer.write(string);
                writer.newLine();
            }
        }
        
        @Override
        protected void readLine(String line) {
            this.cachedContents.add(line);
        }
        
        @Override
        protected void readContents() throws IOException {
            cachedContents.clear();
            super.readContents();
        }
        
        /*
         * Atomic container abstraction methods.
         */
        synchronized public void add(final String line) {
            doLocked(new Runnable() {
                
                public void run() {
                    try {
                        readContents();
                        cachedContents.add(line);
                        writeContents();
                    } catch (IOException ex) {
                        throw new StorageIoException(ex);
                    }
                }
            });
        }
        
        synchronized public boolean contains(final String line) {
            final boolean[] doesContain = { false };
            doLocked(new Runnable() {
                
                public void run() {
                    try {
                        readContents();
                        doesContain[0] = cachedContents.contains(line);
                    } catch (IOException e) {
                        throw new StorageIoException(e);
                    }
                }
            });
            return doesContain[0];
        }
        
        synchronized public boolean remove(final String line) {
            final boolean[] didRemove = { false };
            
            doLocked(new Runnable() {
                public void run() {
                    try {
                        readContents();
                        didRemove[0] = cachedContents.remove(line);
                        writeContents();
                    } catch (IOException e) {
                        throw new StorageIoException(e);
                    }
                }
            });
            
            return didRemove[0];
        }
    }
}