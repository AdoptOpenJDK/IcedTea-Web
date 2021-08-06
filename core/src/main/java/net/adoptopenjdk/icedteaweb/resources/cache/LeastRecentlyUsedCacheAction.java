package net.adoptopenjdk.icedteaweb.resources.cache;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;

import java.net.URL;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.adoptopenjdk.icedteaweb.StringUtils.isBlank;
import static net.adoptopenjdk.icedteaweb.resources.cache.LeastRecentlyUsedCacheAction.ActionType.ADD;
import static net.adoptopenjdk.icedteaweb.resources.cache.LeastRecentlyUsedCacheAction.ActionType.NOOP;
import static net.adoptopenjdk.icedteaweb.resources.cache.LeastRecentlyUsedCacheAction.ActionType.REMOVE;
import static net.adoptopenjdk.icedteaweb.resources.cache.LeastRecentlyUsedCacheAction.ActionType.UPDATE_ACCESS_TIME;

class LeastRecentlyUsedCacheAction {

    private static final String DELIMITER = "::";
    private static final String ID_PREFIX = "i=";
    private static final String LOCATION_PREFIX = "l=";
    private static final String VERSION_PREFIX = "v=";
    private static final String ACCESS_TIME_PREFIX = "a=";

    static final LeastRecentlyUsedCacheAction DO_NOTHING = new LeastRecentlyUsedCacheAction(NOOP, null, null, -1);

    static LeastRecentlyUsedCacheAction createAddActionFor(final LeastRecentlyUsedCacheEntry entry) {
        return new LeastRecentlyUsedCacheAction(ADD, entry.getId(), entry.getCacheKey(), entry.getLastAccessed());
    }

    static LeastRecentlyUsedCacheAction createAccessActionFor(final String id, final long lastAccessed) {
        return new LeastRecentlyUsedCacheAction(UPDATE_ACCESS_TIME, id, null, lastAccessed);
    }

    static LeastRecentlyUsedCacheAction createRemoveActionFor(final String id) {
        return new LeastRecentlyUsedCacheAction(REMOVE, id, null, -1);
    }

    static LeastRecentlyUsedCacheAction parse(final String line) {
        if (isBlank(line)) {
            return DO_NOTHING;
        }
        if (line.startsWith("!")) {
            final String id = line.substring(1).trim();
            if (isBlank(id)) {
                return DO_NOTHING;
            }
            return createRemoveActionFor(id);
        }
        if (line.startsWith(DELIMITER) && line.endsWith(DELIMITER)) {
            String id = null;
            URL location = null;
            VersionId version = null;
            long access = -1;

            final String[] parts = line.split(DELIMITER);
            final int numParts = parts.length;
            final int lastPart = numParts - 1;

            for (int i = 0; i < numParts; i++) {
                String part = parts[i];
                for (int next = i + 1; next < lastPart; next++) {
                    // An empty part means that there was an escaped delimiter -> join the next part
                    if (parts[next].isEmpty()) {
                        next++;
                        i++;
                        part += DELIMITER + parts[next];
                    } else {
                        break;
                    }
                }

                if (part.startsWith(ID_PREFIX)) {
                    try {
                        id = part.substring(2);
                    } catch (Exception e) {
                        return DO_NOTHING;
                    }
                } else if (part.startsWith(LOCATION_PREFIX)) {
                    try {
                        location = new URL(part.substring(2));
                    } catch (Exception e) {
                        return DO_NOTHING;
                    }
                } else if (part.startsWith(VERSION_PREFIX)) {
                    try {
                        version = VersionId.fromString(part.substring(2));
                    } catch (Exception e) {
                        return DO_NOTHING;
                    }
                } else if (part.startsWith(ACCESS_TIME_PREFIX)) {
                    try {
                        access = Long.parseLong(part.substring(2));
                    } catch (Exception e) {
                        return DO_NOTHING;
                    }
                }
            }
            if (access > -1 && !isBlank(id)) {
                if (location != null) {
                    final CacheKey key = new CacheKey(location, version);
                    return createAddActionFor(new LeastRecentlyUsedCacheEntry(id, access, key));
                }
                if (version == null) {
                    return createAccessActionFor(id, access);
                }
            }
        }
        return DO_NOTHING;
    }


    enum ActionType {
        ADD,
        REMOVE,
        UPDATE_ACCESS_TIME,
        NOOP
    }

    private final ActionType type;
    private final LeastRecentlyUsedCacheEntry entry;

    private LeastRecentlyUsedCacheAction(final ActionType type, final String id, final CacheKey key, final long lastAccessed) {
        this.type = type;
        this.entry = new LeastRecentlyUsedCacheEntry(id, lastAccessed, key);
    }

    void applyTo(LeastRecentlyUsedCacheFile cacheFile) {
        switch (type) {
            case ADD:
                cacheFile.addEntry(entry);
                break;
            case REMOVE:
                cacheFile.removeEntry(entry);
                break;
            case UPDATE_ACCESS_TIME:
                cacheFile.markAccessed(entry, entry.getLastAccessed());
                break;
            default:
                // do nothing
        }
    }

    String serialize() {
        switch (type) {
            case ADD:
                final CacheKey key = entry.getCacheKey();
                final String version = key.getVersion() == null ? null : VERSION_PREFIX + escapeDelimiter(key.getVersion());
                return Stream.of(
                                ID_PREFIX + entry.getId(),
                                LOCATION_PREFIX + escapeDelimiter(key.getLocation()),
                                version,
                                ACCESS_TIME_PREFIX + entry.getLastAccessed()
                        )
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(DELIMITER, DELIMITER, DELIMITER));
            case REMOVE:
                return "!" + entry.getId();
            case UPDATE_ACCESS_TIME:
                return Stream.of(
                        ID_PREFIX + entry.getId(),
                        ACCESS_TIME_PREFIX + entry.getLastAccessed()
                ).collect(Collectors.joining(DELIMITER, DELIMITER, DELIMITER));
            default:
                throw new IllegalStateException("Cannot serialize action of type " + type);
        }
    }

    private String escapeDelimiter(Object s) {
        return s.toString().replaceAll(DELIMITER, DELIMITER + DELIMITER);
    }
}
