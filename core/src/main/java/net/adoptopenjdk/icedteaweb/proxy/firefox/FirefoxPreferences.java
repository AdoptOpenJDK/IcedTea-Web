package net.adoptopenjdk.icedteaweb.proxy.firefox;

import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.os.OsUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.adoptopenjdk.icedteaweb.JavaSystemProperties.getUserHome;
import static net.adoptopenjdk.icedteaweb.StringUtils.isBlank;

/**
 * <p>
 * A parser for Firefox's preferences file. It can 'load' Firefox's
 * preferences file and expose the preferences in a simple to use format.
 * </p>
 * Sample usage:
 * <pre><code>
 * FirefoxPreferences p = new FirefoxPreferences();
 * p.load();
 * System.out.println("blink allowed: " + p.getStringValue("firefox.blink_allowed"));
 * </code></pre>
 */
final class FirefoxPreferences {

    private static final Logger LOG = LoggerFactory.getLogger(FirefoxPreferences.class);

    private static final String PATH_PREFIX = "Path=";
    private static final String USER_PREF_PREFIX = "user_pref(";
    private static final int USER_PREF_PREFIX_LENGTH = USER_PREF_PREFIX.length();
    private static final String DOUBLE_QUOTE = "\"";

    private final Map<String, String> prefs = new HashMap<>();

    String getStringValue(final String key) {
        return getStringValue(key, null);
    }

    String getStringValue(final String key, final String defaultValue) {
        ensureLoaded();
        final String valueCandidate = prefs.get(key);
        if (isDoubleQuotedString(valueCandidate)) {
            return removeDoubleQuotes(valueCandidate);
        }
        return defaultValue;
    }

    int getIntValue(final String key, final int defaultValue) {
        ensureLoaded();
        final String valueCandidate = prefs.get(key);
        if (!isDoubleQuotedString(valueCandidate)) {
            try {
                return Integer.parseInt(valueCandidate.trim());
            } catch (Exception ignored) {
            }
        }
        return defaultValue;
    }

    boolean getBooleanValue(final String key, final boolean defaultValue) {
        ensureLoaded();
        final String valueCandidate = prefs.get(key);
        if (!isDoubleQuotedString(valueCandidate)) {
            return valueCandidate.trim().equalsIgnoreCase(Boolean.toString(!defaultValue));
        }
        return defaultValue;
    }

    private void ensureLoaded() {
        if (prefs.isEmpty()) {
            throw new IllegalStateException("Preferences have not been loaded");
        }
    }

    /**
     * Parse the preferences file
     *
     * @throws IOException if an exception occurs while reading the
     *                     preferences file.
     */
    public void load() throws IOException {

        final File preferencesFile = findPreferencesFile();

        /*
         * The Firefox preference file is actually in javascript. It does seem
         * to be nicely formatted, so it should be possible to hack reading it.
         * The correct way of course is to use a javascript library and extract
         * the user_pref object
         */
        try (BufferedReader reader = new BufferedReader(new FileReader(preferencesFile))) {
            while (true) {
                final String untrimmedLine = reader.readLine();
                // end of stream
                if (untrimmedLine == null) {
                    break;
                }
                final String line = untrimmedLine.trim();

                /*
                 * we are only interested in lines of the form: user_pref("key",value);
                 * where value can be a string in double quotes or a number or boolean
                 */
                if (line.startsWith(USER_PREF_PREFIX) && line.length() > USER_PREF_PREFIX_LENGTH + 2) {
                    // extract everything between "user_pref(" and ");"
                    final String pref = line.substring(USER_PREF_PREFIX_LENGTH, line.length() - 2);
                    // key and value are separated by a ","
                    final int firstCommaPos = pref.indexOf(',');
                    if (firstCommaPos > 2) { // shortest valid pref string is `"x",0` -> the comma is a position 3
                        final String keyCandidate = pref.substring(0, firstCommaPos).trim();
                        final String valueCandidate = pref.substring(firstCommaPos + 1).trim();

                        final String key = getKeyFromCandidate(keyCandidate);
                        if (key != null) {
                            prefs.put(key, valueCandidate);
                        }
                    }
                }
            }
        }

        LOG.info("Read {} entries from Firefox's preferences", prefs.size());
    }

    private String getKeyFromCandidate(String keyCandidate) {
        if (isDoubleQuotedString(keyCandidate)) {
            final String key = removeDoubleQuotes(keyCandidate);
            if (!isBlank(key)) {
                return key;
            }
        }
        return null;
    }

    private boolean isDoubleQuotedString(String s) {
        return s != null && s.startsWith(DOUBLE_QUOTE) && s.endsWith(DOUBLE_QUOTE) && s.length() > 1;
    }

    private String removeDoubleQuotes(String s) {
        return s.substring(1, s.length() - 1);
    }

    /**
     * Returns a file object representing firefox's preferences file
     *
     * @return a File object representing the preferences file.
     * @throws FileNotFoundException if the preferences file could not be found
     * @throws IOException           if an exception occurs while trying to identify the
     *                               location of the preferences file.
     */
    private File findPreferencesFile() throws IOException {

        final String profilesPath = getConfigPath() + "profiles.ini";

        if (!(new File(profilesPath).isFile())) {
            throw new FileNotFoundException(profilesPath);
        }

        LOG.info("Using firefox's profiles file: {}", profilesPath);


        final List<String> linesInSection = new ArrayList<>();
        boolean foundDefaultSection = false;

        /*
         * The profiles.ini file is an ini file. This is a quick hack to read
         * it. It is very likely to break given anything strange content.
         */

        // find lines of the section with an entry `default=1`
        try (BufferedReader reader = new BufferedReader(new FileReader(profilesPath))) {
            while (true) {
                final String untrimmedLine = reader.readLine();
                if (untrimmedLine == null) {
                    break;
                }

                final String line = untrimmedLine.trim();
                if (line.startsWith("[Profile") && line.endsWith("]")) {
                    if (foundDefaultSection) {
                        break;
                    }
                    // new section
                    linesInSection.clear();
                } else {
                    linesInSection.add(line);
                    if (isDefaultSection(line)) {
                        foundDefaultSection = true;
                    }
                }
            }
        }

        if (!foundDefaultSection && linesInSection.size() == 0) {
            throw new FileNotFoundException("preferences file");
        }

        final String path = linesInSection.stream()
                .filter(line -> line.startsWith(PATH_PREFIX))
                .findFirst()
                .map(line -> line.substring(PATH_PREFIX.length()))
                .orElseThrow(() -> new FileNotFoundException("preferences file"));

        String fullPath = getConfigPath() + path + File.separator + "prefs.js";
        LOG.info("Found preferences file: ", fullPath);
        return new File(fullPath);
    }

    private String getConfigPath() {

        if (OsUtil.isWindows()) {
            final Map<String, String> env = System.getenv();
            if (env != null) {
                final String appdata = env.get("APPDATA");
                if (appdata != null) {
                    return appdata + File.separator + "Mozilla" + File.separator + "Firefox" + File.separator;
                }
            }
        }
        return getUserHome() + File.separator + ".mozilla" + File.separator + "firefox" + File.separator;
    }

    private boolean isDefaultSection(String line) {
        int equalSignPos = line.indexOf('=');
        if (equalSignPos > 0) {
            String key = line.substring(0, equalSignPos).trim();
            String value = line.substring(equalSignPos + 1).trim();
            return key.toLowerCase().equals("default") && value.equals("1");
        }
        return false;
    }

}
