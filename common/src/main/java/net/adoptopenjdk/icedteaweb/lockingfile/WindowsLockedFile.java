package net.adoptopenjdk.icedteaweb.lockingfile;

import java.io.File;
import java.io.IOException;

public class WindowsLockedFile extends LockedFile {

    public WindowsLockedFile(final File file) {
        super(file);
    }

    /*Comment why it is different*/
    @Override
    public void lock() throws IOException {
        if (!isReadOnly()) {
            getFile().createNewFile();
        }
        getThreadLock().lock();
    }

    /*Comment why it is different*/
    @Override
    public void unlock() throws IOException {
        if (!isHeldByCurrentThread()) {
            return;
        }
        unlockImpl(false);
    }
}
