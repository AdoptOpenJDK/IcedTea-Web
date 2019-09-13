package net.sourceforge.jnlp.util;

import net.adoptopenjdk.icedteaweb.io.FileUtils;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.config.PathsAndFiles;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * This class helps to maintain the Windows shortcuts.
 */
public class WindowsShortcutManager {
    private final static Logger LOG = LoggerFactory.getLogger(WindowsShortcutManager.class);

    private final static File windowsShortcutsFile = new File(PathsAndFiles.CACHE_DIR.getFile(), "shortcutList.txt");

    static File getWindowsShortcutsFile() {
        return  windowsShortcutsFile;
    }

    public static void removeWindowsShortcuts(final String jnlpApp) throws IOException {
        LOG.debug("Clearing Windows shortcuts");
        if (getWindowsShortcutsFile().exists()) {
            List<String> lines = Files.readAllLines(getWindowsShortcutsFile().toPath(), UTF_8);
            Iterator it = lines.iterator();
            boolean fDelete;
            while (it.hasNext()) {
                final String sItem = it.next().toString();
                final String[] sArray = sItem.split(",");
                final String application = sArray[0];
                final String sPath = sArray[1];
                // if application is codebase then delete files
                if (application.equalsIgnoreCase(jnlpApp)) {
                    fDelete = true;
                    it.remove();
                } else {
                    fDelete = false;
                }
                if (jnlpApp.equals("ALL")) {
                    fDelete = true;
                }
                if (fDelete) {
                    LOG.info("Deleting item = {}", sPath);
                    final File scList = new File(sPath);
                    try {
                        FileUtils.recursiveDelete(scList, scList);
                    } catch (Exception e) {
                        LOG.error("Error in deleting windows shortcuts file '" + sPath + "'", e);
                    }
                }
            }
            if (jnlpApp.equals("ALL")) {
                //delete shortcut list file
                Files.deleteIfExists(getWindowsShortcutsFile().toPath());
            } else {
                //write file after application shortcuts have been removed
                Files.write(getWindowsShortcutsFile().toPath(), lines, UTF_8);
            }
        }
    }
}
