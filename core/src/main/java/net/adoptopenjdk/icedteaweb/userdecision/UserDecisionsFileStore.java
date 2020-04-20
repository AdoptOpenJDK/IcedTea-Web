// Copyright (C) 2019 Karakun AG
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
package net.adoptopenjdk.icedteaweb.userdecision;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.adoptopenjdk.icedteaweb.io.FileUtils;
import net.adoptopenjdk.icedteaweb.io.IOUtils;
import net.adoptopenjdk.icedteaweb.lockingfile.LockableFile;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.JNLPFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static net.adoptopenjdk.icedteaweb.Assert.requireNonNull;
import static net.sourceforge.jnlp.config.PathsAndFiles.USER_DECISIONS_FILE_STORE;

public class UserDecisionsFileStore implements UserDecisions {

    private static final Logger LOG = LoggerFactory.getLogger(UserDecisionsFileStore.class);

    private final LockableFile lockableFile;
    private final Gson gson;
    private final File store;

    public UserDecisionsFileStore() {
        this(USER_DECISIONS_FILE_STORE.getFile());
    }

    // visible for testing
    UserDecisionsFileStore(final File store) {
        this.store = requireNonNull(store, "store");
        this.lockableFile = LockableFile.getInstance(store);
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        if (!store.isFile()) {
            try {
                FileUtils.createRestrictedFile(store);
            } catch (IOException ex) {
                throw new RuntimeException("Could not create user decisions file store", ex);
            }
        }
    }

    @Override
    public <T extends Enum<T>> Optional<T> getUserDecisions(final UserDecision.Key key, final JNLPFile file, final Class<T> resultType) {
        requireNonNull(file, "file");
        requireNonNull(key, "key");

        final URL domain = getDomain(file);
        final Set<String> jarNames = getJarNames(file);
        try {
            lockableFile.lock();
            try {
                return readJsonFile().getDecision(domain, jarNames, key, resultType);
            } finally {
                lockableFile.unlock();
            }
        } catch (IOException ex) {
            LOG.error("Failed to lock/unlock user decisions file store: " + store, ex);
        }

        return Optional.empty();
    }

    @Override
    public <T extends Enum<T>> void saveForDomain(final JNLPFile file, final UserDecision<T> userDecision) {
        requireNonNull(file, "file");
        requireNonNull(userDecision, "userDecision");

        final URL domain = getDomain(file);
        save(userDecision, domain, emptySet());
    }

    @Override
    public <T extends Enum<T>> void saveForApplication(final JNLPFile file, final UserDecision<T> userDecision) {
        requireNonNull(file, "file");
        requireNonNull(userDecision, "userDecision");

        final URL domain = getDomain(file);
        final Set<String> jarNames = getJarNames(file);
        save(userDecision, domain, jarNames);
    }

    private <T extends Enum<T>> void save(final UserDecision<T> userDecision, final URL domain, final Set<String> jarNames) {
        try {
            lockableFile.lock();
            try {
                final FileStoreEntries userDecisions = readJsonFile();
                userDecisions.addDecision(domain, jarNames, userDecision);
                writeJsonFile(userDecisions);
            } finally {
                lockableFile.unlock();
            }
        } catch (IOException ex) {
            LOG.error("Failed to lock/unlock user decisions file store: " + store, ex);
        }
    }

    private Set<String> getJarNames(final JNLPFile file) {
        return file.getResourcesDescs().stream()
                .flatMap(resourcesDesc -> Arrays.stream(resourcesDesc.getJARs()))
                .map(jarDesc -> new File(jarDesc.getLocation().toString()).getName())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private URL getDomain(final JNLPFile file) {
        final URL codebase = file.getNotNullProbableCodeBase();
        try {
            return new URL(codebase.getProtocol(), codebase.getHost(), codebase.getPort(), "");
        } catch (MalformedURLException ex) {
            LOG.error(format("Failed to determine domain as codebase '%s' is not a valid URL", codebase), ex);
            throw new RuntimeException(ex);
        }
    }

    private FileStoreEntries readJsonFile() {
        try (FileInputStream in = new FileInputStream(store)) {
            final InputStreamReader reader = new InputStreamReader(in);
            return Optional.ofNullable(gson.fromJson(reader, FileStoreEntries.class))
                    .orElse(new FileStoreEntries());
        } catch (IOException ex) {
            LOG.error("Could not read from user decisions file store: " + store, ex);
            return new FileStoreEntries();
        }
    }

    private void writeJsonFile(FileStoreEntries entries) {
        try (FileOutputStream out = new FileOutputStream(store)) {
            IOUtils.writeUtf8Content(out, gson.toJson(entries));
        } catch (IOException ex) {
            LOG.error("Could not write to user decisions file store: " + store, ex);
        }
    }
}
