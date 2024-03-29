package net.adoptopenjdk.icedteaweb.resources.cache;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.net.URL;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.adoptopenjdk.icedteaweb.StringUtils.isBlank;
import static net.adoptopenjdk.icedteaweb.resources.cache.CacheAction.ActionType.ADD;
import static net.adoptopenjdk.icedteaweb.resources.cache.CacheAction.ActionType.NOOP;
import static net.adoptopenjdk.icedteaweb.resources.cache.CacheAction.ActionType.REMOVE;
import static net.adoptopenjdk.icedteaweb.resources.cache.CacheAction.ActionType.UPDATE_ACCESS_TIME;

class CacheAction {

    private static final Logger logger = LoggerFactory.getLogger(CacheAction.class);

    private static final String DELIMITER = "::";
    private static final String ID_PREFIX = "i=";
    private static final String LOCATION_PREFIX = "l=";
    private static final String VERSION_PREFIX = "v=";
    private static final String ACCESS_TIME_PREFIX = "a=";

    static final CacheAction DO_NOTHING = new CacheAction(NOOP, null, null, -1);

    static CacheAction createAddActionFor(final CacheIndexEntry entry) {
        return new CacheAction(ADD, entry.getId(), entry.getCacheKey(), entry.getLastAccessed());
    }

    static CacheAction createAccessActionFor(final String id, final long lastAccessed) {
        return new CacheAction(UPDATE_ACCESS_TIME, id, null, lastAccessed);
    }

    static CacheAction createRemoveActionFor(final String id) {
        return new CacheAction(REMOVE, id, null, -1);
    }

    static CacheAction parse(final String line) {
        if (isBlank(line)) {
            return DO_NOTHING;
        }
        if (line.startsWith("!") && line.endsWith("!") && line.length() > 2) {
            final String id = line.substring(1, line.length() - 1);
            if (isBlank(id)) {
                logger.debug("encountered remove action line without ID");
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
                        logger.debug("Exception while extracting ID {}", e.getMessage());
                        return DO_NOTHING;
                    }
                } else if (part.startsWith(LOCATION_PREFIX)) {
                    try {
                        location = new URL(part.substring(2));
                    } catch (Exception e) {
                        logger.debug("Exception while extracting location {}", e.getMessage());
                        return DO_NOTHING;
                    }
                } else if (part.startsWith(VERSION_PREFIX)) {
                    try {
                        version = VersionId.fromString(part.substring(2));
                    } catch (Exception e) {
                        logger.debug("Exception while extracting version {}", e.getMessage());
                        return DO_NOTHING;
                    }
                } else if (part.startsWith(ACCESS_TIME_PREFIX)) {
                    try {
                        access = Long.parseLong(part.substring(2));
                    } catch (Exception e) {
                        logger.debug("Exception while extracting access time {}", e.getMessage());
                        return DO_NOTHING;
                    }
                }
            }
            if (access > -1 && !isBlank(id)) {
                if (location != null) {
                    final CacheKey key = new CacheKey(location, version);
                    return createAddActionFor(new CacheIndexEntry(id, access, key));
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
    private final CacheIndexEntry entry;

    private CacheAction(final ActionType type, final String id, final CacheKey key, final long lastAccessed) {
        this.type = type;
        this.entry = new CacheIndexEntry(id, lastAccessed, key);
    }

    boolean applyTo(CacheIndexEntries entries) {
        switch (type) {
            case ADD:
                return entries.addEntry(entry);
            case REMOVE:
                return entries.removeEntry(entry);
            case UPDATE_ACCESS_TIME:
                return entries.markAccessed(entry, entry.getLastAccessed());
            default:
                return false;
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
                return "!" + entry.getId() + "!";
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
