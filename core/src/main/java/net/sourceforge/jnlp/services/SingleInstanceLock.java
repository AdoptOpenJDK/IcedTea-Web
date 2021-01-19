// Copyright (C) 2009 Red Hat, Inc.
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

package net.sourceforge.jnlp.services;

import net.adoptopenjdk.icedteaweb.io.FileUtils;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.config.PathsAndFiles;
import net.sourceforge.jnlp.util.RestrictedFileUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;

/**
 * This class represents a Lock for single instance jnlp applications.
 * <p>
 * The lock is per-session, per user.
 *
 * @author <a href="mailto:omajid@redhat.com">Omair Majid</a>
 */
class SingleInstanceLock {

    private static final Logger LOG = LoggerFactory.getLogger(SingleInstanceLock.class);

    public static final int INVALID_PORT = Integer.MIN_VALUE;

    private final File lockFile;

    private int port = INVALID_PORT; // indicates that this lock has not been activated

    /**
     * Create an object to manage the instance lock for the specified JNLP file.
     *
     * @param jnlpFile the jnlp file to create the lock for
     */
    public SingleInstanceLock(JNLPFile jnlpFile) {
        lockFile = getLockFile(jnlpFile);
    }

    /**
     * Create/overwrite the instance lock for the jnlp file.
     *
     * @param localPort the network port for the lock
     * @throws IOException on any io problems
     */
    public void createWithPort(int localPort) throws IOException {
        if (lockFile.exists()) {
            LOG.error("SingleInstance lock file already present - deleting it.");
            FileUtils.deleteWithErrMesg(lockFile, "Could not delete [" + lockFile + "]");
        }
        RestrictedFileUtils.createRestrictedFile(lockFile);
        lockFile.deleteOnExit();
        try (BufferedWriter lockFileWriter = new BufferedWriter(new FileWriter(lockFile, false))) {
            lockFileWriter.write(String.valueOf(localPort));
            lockFileWriter.newLine();
            lockFileWriter.flush();
        }
    }

    /**
     * Returns true if the lock if valid. That is, the lock exists, and port it
     * points to is listening for incoming messages.
     */
    public boolean isValid() {
        return (exists() && getPort() != INVALID_PORT && !isPortFree(getPort()));
    }

    /**
     * Returns the port in this lock file.
     */
    public int getPort() {
        if (!exists()) {
            return INVALID_PORT;
        }

        try {
            parseFile();
        } catch (NumberFormatException e) {
            port = INVALID_PORT;
        } catch (IOException e) {
            port = INVALID_PORT;
        }
        return port;

    }

    /**
     * Returns true if the lock file already exists.
     */
    boolean exists() {
        return lockFile.exists();
    }

    /**
     * Returns true if the port is free.
     */
    private boolean isPortFree(int port) {
        try (final ServerSocket ignored = new ServerSocket(port)) {
            return true;
        } catch (BindException e) {
            return false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Return a file object that represents the lock file. The lock file itself
     * may or may not exist.
     *
     * @param jnlpFile the applicaton for which to create the lock
     */
    private File getLockFile(final JNLPFile jnlpFile) {
        final File baseDir = PathsAndFiles.LOCKS_DIR.getFile();

        if (!baseDir.isDirectory()) {
            if (!baseDir.getParentFile().isDirectory() && !baseDir.getParentFile().mkdirs()) {
                throw new RuntimeException("Unable to create locks directory (" + baseDir + ")");
            }
            try {
                RestrictedFileUtils.createRestrictedDirectory(baseDir);
            } catch (IOException e) {
                throw new RuntimeException("Unable to create locks directory (" + baseDir + ")");
            }
        }

        final String lockFileName = getLockFileName(jnlpFile);
        final File applicationLockFile = new File(baseDir, lockFileName);
        return applicationLockFile;
    }

    /**
     * Returns the name of the lock file.
     *
     * @param jnlpFile the applicaton for which to create the lock
     */
    private String getLockFileName(JNLPFile jnlpFile) {
        String initialName = "";

        if (jnlpFile.getSourceLocation() != null) {
            initialName = initialName + jnlpFile.getSourceLocation();
        } else {
            initialName = initialName + jnlpFile.getFileLocation();
        }

        if (jnlpFile.getFileVersion() != null) {
            initialName = initialName + jnlpFile.getFileVersion().toString();
        }

        initialName = initialName + getCurrentDisplay();
        return FileUtils.sanitizeFileName(initialName);

    }

    /**
     * Parse the lock file.
     */
    private void parseFile() throws NumberFormatException, IOException {
        try (final BufferedReader lockFileReader = new BufferedReader(new FileReader(lockFile))) {
            this.port = Integer.parseInt(lockFileReader.readLine());
        }
    }

    /**
     * Returns a string identifying this display.
     * <p>
     * Implementation note: On systems with X support, this is the DISPLAY
     * variable
     *
     * @return a string that is guaranteed to be not null.
     */
    private String getCurrentDisplay() {
        final String display = System.getenv("DISPLAY");
        return (display == null) ? "" : display;
    }

}
