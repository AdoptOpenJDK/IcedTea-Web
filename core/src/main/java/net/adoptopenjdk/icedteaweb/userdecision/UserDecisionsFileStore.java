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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static net.sourceforge.jnlp.config.PathsAndFiles.USER_DECISIONS_FILE_STORE;

public class UserDecisionsFileStore implements UserDecisions {
    private static final Logger LOG = LoggerFactory.getLogger(UserDecisionsFileStore.class);
    private final static File store = USER_DECISIONS_FILE_STORE.getFile();
    private final LockableFile lockableFile = LockableFile.getInstance(store);

    private final Gson gson;

    public UserDecisionsFileStore() {
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
        // TODO implementation missing
        // TODO prefer entries with jar lists (application) over empty list (domain), order of jars is irrelevant
        // TODO lockable file
        return Optional.empty();
    }

    @Override
    public <T extends Enum<T>> void saveForDomain(final JNLPFile file, final UserDecision<T> userDecision) {
        final URL codebase = file.getNotNullProbableCodeBase();
        save(userDecision, codebase, emptyList());
    }

    @Override
    public <T extends Enum<T>> void saveForApplication(final JNLPFile file, final UserDecision<T> userDecision) {
        final URL codebase = file.getNotNullProbableCodeBase();
        final List<String> jarNames = file.getResourcesDescs().stream()
                .flatMap(resourcesDesc -> Arrays.stream(resourcesDesc.getJARs()))
                .map(jarDesc -> new File(jarDesc.getLocation().toString()).getName())
                .collect(Collectors.toList());
        save(userDecision, codebase, jarNames);
    }

    private <T extends Enum<T>> void save(final UserDecision<T> userDecision, final URL codebase, final List<String> jarNames) {
        final FileStoreEntry fileStoreEntry = createFileStoreEntry(userDecision, codebase, jarNames);

        try {
            lockableFile.lock();

            try {
                final Set<FileStoreEntry> fileStoreEntries = new LinkedHashSet<>(readFileStoreEntries());
                fileStoreEntries.add(fileStoreEntry);
                writeFileStoreEntries(new ArrayList<>(fileStoreEntries));

            } finally {
                lockableFile.unlock();
            }
        }
        catch (IOException ex) {
            LOG.error("Failed to lock/unlock user decisions file store: " + store, ex);
        }
    }

    private <T extends Enum<T>> FileStoreEntry createFileStoreEntry(final UserDecision<T> userDecision, final URL codebase, final List<String> jarNames) {
        final URL domain = getDomain(codebase);
        return new FileStoreEntry(userDecision, domain, jarNames);
    }

    private URL getDomain(final URL codebase) {
        try {
            return new URL(codebase.getProtocol(), codebase.getHost(), codebase.getPort(), "");
        } catch (MalformedURLException ex) {
            LOG.error("Codebase is not a valid URL", ex);
            throw new RuntimeException(ex);
        }
    }

    private List<FileStoreEntry> readFileStoreEntries() {
        try (FileInputStream in = new FileInputStream(store)) {
            final InputStreamReader reader = new InputStreamReader(in);
            return Optional.ofNullable(gson.fromJson(reader, FileStoreEntries.class))
                    .map(FileStoreEntries::getUserDecisions)
                    .orElse(emptyList());

        } catch (IOException ex) {
            LOG.warn("Could not read from user decisions file store.", ex);
            return emptyList();
        }
    }

    private void writeFileStoreEntries(List<FileStoreEntry> entries) {
        try (FileOutputStream out = new FileOutputStream(store)) {
            IOUtils.writeUtf8Content(out, gson.toJson(new FileStoreEntries(entries)));

        } catch (IOException ex) {
            LOG.warn("Could not write to user decisions file store.", ex);
        }
    }
}
