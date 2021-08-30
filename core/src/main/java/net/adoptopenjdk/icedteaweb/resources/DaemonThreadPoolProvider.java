/*
 Copyright (C) 2011 Red Hat, Inc

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
statement from your version. */
package net.adoptopenjdk.icedteaweb.resources;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class DaemonThreadPoolProvider {

    private static final ExecutorService DAEMON_THREAD_POOL = createFixedDaemonThreadPool(6);

    public static ExecutorService globalFixedThreadPool() {
        return DAEMON_THREAD_POOL;
    }

    public static ExecutorService createCachedDaemonThreadPool() {
        return Executors.newCachedThreadPool(new DaemonThreadFactory());
    }

    public static ExecutorService createFixedDaemonThreadPool(final int numThreads) {
        return Executors.newFixedThreadPool(numThreads, new DaemonThreadFactory());
    }

    public static ExecutorService createSingletonDaemonThreadPool() {
        return Executors.newSingleThreadExecutor(new DaemonThreadFactory());
    }

    /**
     * This is copypasted default factory from java.util.concurrent.Executors.
     * The only difference is, that it creates daemon threads.
     *
     * Except creating new threads, the rest of class is complicated creation of
     * name.
     */
    private static class DaemonThreadFactory implements ThreadFactory {

        private static final AtomicInteger poolNumber = new AtomicInteger(1);

        private final ThreadGroup group;

        private final AtomicInteger threadNumber = new AtomicInteger(1);

        private final String namePrefix;

        public DaemonThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup()
                    : Thread.currentThread().getThreadGroup();
            namePrefix = "itwpool-"
                    + poolNumber.getAndIncrement()
                    + "-itwthread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (!t.isDaemon()) {
                t.setDaemon(true);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }

}
