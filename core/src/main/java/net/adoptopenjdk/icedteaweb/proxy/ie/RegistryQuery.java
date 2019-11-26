package net.adoptopenjdk.icedteaweb.proxy.ie;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class RegistryQuery {

    public static Set<RegistryValue> getAllValuesForKey(final String key) throws IOException, InterruptedException, ExecutionException {
        final Process start = new ProcessBuilder().command("reg", "query", "\"" + key + "\"")
                .redirectErrorStream(true)
                .start();
        final Future<List<String>> linesFuture = getLines(start.getInputStream());
        final int exitValue = start.waitFor();
        if(exitValue != 0) {
            throw new RuntimeException("Process ended with error code: " + exitValue);
        }
        final List<String> lines = linesFuture.get();

        return getRegistryValuesFromLines(key, lines);
    }

    public static Set<RegistryValue> getRegistryValuesFromLines(final String key, final List<String> lines) {
        return lines.stream()
                .filter(l -> !l.contains(key))
                .map(l -> l.trim())
                .filter(l -> !l.isEmpty())
                .map(l -> {
                    final int index = l.indexOf("REG_");
                    final String name = l.substring(0, index).trim();
                    final String[] typeAndValue = l.substring(index).split("\\s+", 2);
                    if(typeAndValue.length != 2) {
                        throw new IllegalStateException("Can not parse value: '" + l + "'");
                    }
                    final RegistryValueType type = RegistryValueType.valueOf(typeAndValue[0].trim());
                    if(type == null) {
                        throw new IllegalStateException("Can not define type: '" + l + "'");
                    }
                    final String value = typeAndValue[1].trim();
                    return new RegistryValue(name, type, value);
                }).collect(Collectors.toSet());
    }

    public static Future<List<String>> getLines(final InputStream src) {
        final CompletableFuture<List<String>> result = new CompletableFuture<>();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                final List<String> lines = new ArrayList<>();
                final Scanner sc = new Scanner(src);
                while (sc.hasNextLine()) {
                    lines.add(sc.nextLine());
                }
                result.complete(lines);
            } catch (final Exception e) {
                result.completeExceptionally(e);
            }
        });
        return result;
    }

    public static Optional<RegistryValue> getRegistryValue(final String key, final String valueName) throws InterruptedException, ExecutionException, IOException {
        return getAllValuesForKey(key).stream().filter(v -> Objects.equals(valueName, v.getName())).findFirst();
    }
}
