package net.adoptopenjdk.icedteaweb.integration;

import java.util.HashMap;
import java.util.Map;

/**
 * Builder for fluent construction of {@code Map<String, String>}.
 */
public class MapBuilder {

    public static MapBuilder replace(String nextKey) {
        return new MapBuilder(nextKey);
    }

    private final Map<String, String> map = new HashMap<>();

    private String nextKey;

    private MapBuilder(String nextKey) {
        this.nextKey = nextKey;
    }

    public MapBuilder and(String nextKey) {
        this.nextKey = nextKey;
        return this;
    }

    public MapBuilder with(int value) {
        return with(Integer.toString(value));
    }

    public MapBuilder with(Class<?> value) {
        return with(value.getName());
    }

    public MapBuilder with(String value) {
        map.put(nextKey, value);
        return this;
    }

    public Map<String, String> build() {
        return map;
    }
}
