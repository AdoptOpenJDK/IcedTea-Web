// Copyright (C) 2009 Red Hat, Inc.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

package net.sourceforge.jnlp.services;

import static net.sourceforge.jnlp.runtime.Translator.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;

import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.FileUtils;

/**
 * This class represents a Lock for single instance jnlp applications
 *
 * The lock is per-session, per user.
 *
 * @author <a href="mailto:omajid@redhat.com">Omair Majid</a>
 */
class SingleInstanceLock {

    JNLPFile jnlpFile;
    File lockFile = null;

    public static final int INVALID_PORT = Integer.MIN_VALUE;

    int port = INVALID_PORT;

    /**
     * Create an object to manage the instance lock for the specified JNLP file.
     *
     * @param jnlpFile the jnlpfile to create the lock for
     */
    public SingleInstanceLock(JNLPFile jnlpFile) {
        this.jnlpFile = jnlpFile;
        lockFile = getLockFile();

    }

    /**
     * Create/overwrite the instance lock for the jnlp file.
     *
     * @param localPort the network port for the lock
     * @throws IOException on any io problems
     */
    public void createWithPort(int localPort) throws IOException {

        FileUtils.createRestrictedFile(lockFile, true);
        BufferedWriter lockFileWriter = new BufferedWriter(new FileWriter(lockFile, false));
        lockFileWriter.write(String.valueOf(localPort));
        lockFileWriter.newLine();
        lockFileWriter.flush();
        lockFileWriter.close();

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
    private boolean exists() {
        return lockFile.exists();
    }

    /**
     * Returns true if the port is free.
     */
    private boolean isPortFree(int port) {
        try {
            ServerSocket socket = new ServerSocket(port);
            socket.close();
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
     */
    private File getLockFile() {
        File baseDir = new File(JNLPRuntime.getConfiguration()
                .getProperty(DeploymentConfiguration.KEY_USER_LOCKS_DIR));

        if (!baseDir.isDirectory()) {
            if (!baseDir.getParentFile().isDirectory() && !baseDir.getParentFile().mkdirs()) {
                throw new RuntimeException(R("RNoLockDir", baseDir));
            }
            try {
                FileUtils.createRestrictedDirectory(baseDir);
            } catch (IOException e) {
                throw new RuntimeException(R("RNoLockDir", baseDir));
            }
        }

        String lockFileName = getLockFileName();
        File applicationLockFile = new File(baseDir, lockFileName);
        return applicationLockFile;
    }

    /**
     * Returns the name of the lock file.
     */
    private String getLockFileName() {
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
     *
     * @throws NumberFormatException
     * @throws IOException
     */
    private void parseFile() throws NumberFormatException, IOException {
        BufferedReader lockFileReader = new BufferedReader(new FileReader(lockFile));
        int port = Integer.valueOf(lockFileReader.readLine());
        lockFileReader.close();
        this.port = port;
    }

    /**
     * Returns a string identifying this display.
     *
     * Implementation note: On systems with X support, this is the DISPLAY
     * variable
     *
     * @return a string that is guaranteed to be not null.
     */
    private String getCurrentDisplay() {
        String display = System.getenv("DISPLAY");
        return (display == null) ? "" : display;
    }

}
