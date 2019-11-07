package net.adoptopenjdk.icedteaweb.resources.jardiff;

import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.io.IOUtils;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

public class JarDiffMerger {

    private static final Logger LOG = LoggerFactory.getLogger(JarDiffMerger.class);

    private static final Pattern MOVED_PATH_PATTERN = Pattern.compile("(.+[^\\\\])\\s(.*)");
    private static final String ESCAPED_WHITESPACE_PATTERN = Pattern.quote("\\ ");

    private static final String VERSION_INFORMATION = "version 1.0";

    private static final String INDEX_FILE = "META-INF/INDEX.JD";

    private static final String REMOVE_KEYWORD = "remove";

    private static final String MOVE_KEYWORD = "move";

    public static void merge(final JarFile oldJar, final JarFile jarDiff, final JarOutputStream outputStream)
            throws IOException {
        Assert.requireNonNull(oldJar, "oldJar");
        Assert.requireNonNull(jarDiff, "jarDiff");
        Assert.requireNonNull(outputStream, "outputStream");

        LOG.debug("JarDiff merge for original jar '{}' and diff jar '{}' starts", oldJar.getName(), jarDiff.getName());

        try {
            final Set<JarEntry> newContent = getNewContent(jarDiff);
            final Set<String> removedContent = getRemovedContent(jarDiff);
            final Set<MovedJar> movedContent = getMovedContent(jarDiff);
            final Set<JarEntry> unmodifiedContent = oldJar.stream()
                    .filter(e -> !containsEntryWithSameName(newContent, e.getName()))
                    .filter(e -> !removedContent.contains(e.getName()))
                    .filter(e -> !containsOldName(movedContent, e.getName()))
                    .collect(Collectors.toSet());

            newContent.forEach(e -> {
                LOG.debug("JarDiff: Adding new content '{}'", e.getName());
                try (final InputStream inputStream = jarDiff.getInputStream(e)) {
                    writeEntry(inputStream, outputStream, e.getName());
                } catch (IOException ex) {
                    throw new RuntimeException("Error in jardiff merge", ex);
                }
            });

            movedContent.forEach(j -> {
                final String oldName = j.getOldName();
                final String newName = j.getNewName();
                LOG.debug("JarDiff: Adding moved content '{}' -> '{}'", oldName, newName);
                final JarEntry oldEntry = oldJar.getJarEntry(oldName);
                if (oldEntry == null) {
                    throw new IllegalStateException("Error in jardiff merge. Moved entry '" + oldName + "' can not be found in original jar");
                }
                try (final InputStream inputStream = oldJar.getInputStream(oldEntry)) {
                    writeEntry(inputStream, outputStream, newName);
                } catch (final Exception e) {
                    throw new RuntimeException("Error in jardiff merge", e);
                }
            });

            unmodifiedContent.forEach(e -> {
                LOG.debug("JarDiff: Adding unmodified content '{}'", e.getName());
                try (final InputStream inputStream = oldJar.getInputStream(e)) {
                    writeEntry(inputStream, outputStream, e.getName());
                } catch (final IOException ex) {
                    throw new RuntimeException("Error in jardiff merge", ex);
                }
            });
        } finally {
            outputStream.finish();
        }
    }

    private static boolean containsEntryWithSameName(final Collection<JarEntry> collection, final String name) {
        Assert.requireNonNull(collection, "collection");
        Assert.requireNonBlank(name, "name");

        return collection.stream()
                .anyMatch(e -> Objects.equals(name, e.getName()));
    }

    private static boolean containsOldName(final Collection<MovedJar> collection, final String oldName) {
        Assert.requireNonNull(collection, "collection");
        Assert.requireNonBlank(oldName, "oldName");

        return collection.stream()
                .map(MovedJar::getOldName)
                .anyMatch(n -> Objects.equals(n, oldName));
    }

    private static Set<JarEntry> getNewContent(final JarFile jarDiff) {
        return jarDiff.stream().filter(e -> !Objects.equals(e.getName(), INDEX_FILE)).collect(Collectors.toSet());
    }

    private static List<String> getIndexFileLines(final JarFile jarDiff) throws IOException {
        Assert.requireNonNull(jarDiff, "jarDiff");
        final ZipEntry indexEntry = jarDiff.getEntry(INDEX_FILE);
        if (indexEntry == null) {
            throw new IllegalStateException("Given JarFile '" + jarDiff.getName() + "' does not contain a JARDIFF index file");
        }

        try (final InputStream inputStream = jarDiff.getInputStream(indexEntry)) {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            final String line = reader.readLine();
            if (line == null || !line.equals(VERSION_INFORMATION)) {
                throw new IllegalStateException("Index file does is not based on jardiff version 1.0");
            }
            return reader.lines().collect(Collectors.toList());
        }
    }

    private static Set<String> getRemovedContent(final JarFile jarDiff) throws IOException {
        final Set<String> result = getIndexFileLines(jarDiff).stream()
                .filter(l -> l.startsWith(REMOVE_KEYWORD))
                .map(l -> l.substring(REMOVE_KEYWORD.length()).trim())
                .collect(Collectors.toSet());
        return Collections.unmodifiableSet(result);
    }

    private static Set<MovedJar> getMovedContent(final JarFile jarDiff) throws IOException {
        final Set<MovedJar> result = getIndexFileLines(jarDiff).stream()
                .map(String::trim)
                .filter(l -> l.startsWith(MOVE_KEYWORD))
                .map(l -> l.substring(MOVE_KEYWORD.length()).trim())
                .map(l -> {
                    final Matcher matcher = MOVED_PATH_PATTERN.matcher(l);

                    if (!matcher.matches() || matcher.groupCount() != 2) {
                        throw new IllegalStateException("Found invalid move definition: '" + l + "'");
                    }

                    final String first = matcher.group(1).replaceAll(ESCAPED_WHITESPACE_PATTERN, " ");
                    final String second = matcher.group(2).replaceAll(ESCAPED_WHITESPACE_PATTERN, " ");

                    return new MovedJar(first, second);
                })
                .collect(Collectors.toSet());
        return Collections.unmodifiableSet(result);
    }

    private static void writeEntry(final InputStream inputStream, final JarOutputStream jarOutputStream, final String entryName)
            throws IOException {
        Assert.requireNonNull(jarOutputStream, "jarOutputStream");
        Assert.requireNonNull(inputStream, "inputStream");
        Assert.requireNonBlank(entryName, "entryName");
        try {
            jarOutputStream.putNextEntry(new ZipEntry(entryName));
            IOUtils.copy(inputStream, jarOutputStream);
        } catch (final IOException e) {
            throw new IOException("Error in writing jar entry '" + entryName + "'", e);
        }
    }

    private static class MovedJar {

        private final String oldName;

        private final String newName;

        MovedJar(final String oldLocation, final String newLocation) {
            this.newName = newLocation;
            this.oldName = oldLocation;
        }

        String getOldName() {
            return oldName;
        }

        String getNewName() {
            return newName;
        }
    }
}


