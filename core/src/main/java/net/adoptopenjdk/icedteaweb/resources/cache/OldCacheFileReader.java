package net.adoptopenjdk.icedteaweb.resources.cache;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.util.PropertiesFile;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;

/**
 * Algorithm to extract {@link CacheIndexEntry} from a properties file.
 * The property file has been deprecated and is no longer used.
 * This algorithm is only used to migrate the existing data.
 */
class OldCacheFileReader {

    private static final Logger LOG = LoggerFactory.getLogger(OldCacheFileReader.class);

    private static final String KEY_LAST_ACCESSED = "lastAccessed";
    private static final String KEY_DELETE = "delete";
    private static final String KEY_HREF = "href";
    private static final String KEY_VERSION = "version";

    private static final char KEY_DELIMITER = '.';

    static List<CacheIndexEntry> loadEntriesFromOldIndex(File file) {
        final PropertiesFile oldIndexProperties = new PropertiesFile(file);
        try {
            oldIndexProperties.lock();
            oldIndexProperties.load();
            return convertPropertiesToEntries(oldIndexProperties);
        } catch (Exception ignored) {
            return emptyList();
        } finally {
            oldIndexProperties.unlock();
        }

    }

    private static List<CacheIndexEntry> convertPropertiesToEntries(PropertiesFile props) {
        // STEP 1
        // group all properties with the same ID together
        // throwing away entries which do not have a valid key
        final Map<String, Map<String, String>> id2ValueMap = new HashMap<>();
        for (Map.Entry<String, String> propEntry : new HashSet<>(props.entrySet())) {
            final String key = propEntry.getKey();
            if (key != null) {
                final String[] keyParts = splitKey(key);
                if (keyParts.length == 2) {
                    final String value = propEntry.getValue();
                    id2ValueMap.computeIfAbsent(keyParts[0], k -> new HashMap<>()).put(keyParts[1], value);
                    continue;
                }
            }

            // if we reach this point something is wrong with the property
            LOG.debug("found broken property: {}", key);
            props.remove(key);
        }

        // STEP 2
        // convert the properties to actual entries
        // collecting the IDs of the ones which have invalid data
        final List<CacheIndexEntry> entries = new ArrayList<>(id2ValueMap.size());
        for (Map.Entry<String, Map<String, String>> valuesEntry : id2ValueMap.entrySet()) {
            final Map<String, String> values = valuesEntry.getValue();

            final String id = valuesEntry.getKey();
            final String lastAccessedValue = values.get(KEY_LAST_ACCESSED);
            final String markedForDeletionValue = values.get(KEY_DELETE);
            final String resourceHrefValue = values.get(KEY_HREF);
            final String versionValue = values.get(KEY_VERSION);

            try {
                final VersionId version = versionValue != null ? VersionId.fromString(versionValue) : null;
                final URL resourceHref = new URL(resourceHrefValue);
                if (!Boolean.parseBoolean(markedForDeletionValue)) {
                    final long lastAccessed = Long.parseLong(lastAccessedValue);
                    entries.add(new CacheIndexEntry(id, lastAccessed, new CacheKey(resourceHref, version)));
                }
            } catch (Exception e) {
                LOG.debug("found broken ID: {}", id);
                props.remove(id + KEY_DELIMITER + KEY_LAST_ACCESSED);
                props.remove(id + KEY_DELIMITER + KEY_DELETE);
                props.remove(id + KEY_DELIMITER + KEY_HREF);
                props.remove(id + KEY_DELIMITER + KEY_VERSION);
            }
        }

        // make sure the entries are sorted most recent accessed to least recent accessed
        Collections.sort(entries);

        return entries;
    }

    private static String[] splitKey(String key) {
        final int i = key.indexOf(KEY_DELIMITER);
        if (i > 0 && i < key.length()) {
            return new String[]{key.substring(0, i), key.substring(i + 1)};
        }
        return new String[0];
    }
}
