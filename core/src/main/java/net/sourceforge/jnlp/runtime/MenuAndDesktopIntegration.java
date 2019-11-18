package net.sourceforge.jnlp.runtime;

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.SecurityDialogs;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.jnlp.element.information.ShortcutDesc;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.os.OsUtil;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.AccessWarningPaneComplexReturn;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.security.AccessType;
import net.sourceforge.jnlp.util.GenericDesktopEntry;
import net.sourceforge.jnlp.util.XDesktopEntry;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * ...
 */
class MenuAndDesktopIntegration {
    private static final Logger LOG = LoggerFactory.getLogger(MenuAndDesktopIntegration.class);

    /**
     * Creates menu and desktop entries if required by the jnlp file or settings
     */
    static void addMenuAndDesktopEntries(JNLPFile file) {
        ShortcutDesc sd = file.getInformation().getShortcut();
        if (OsUtil.isWindows()) {
            LOG.debug("Generating windows desktop shortcut");
            try {
                Object instance = null;
                try {
                    Class<?> cl = Class.forName("net.sourceforge.jnlp.util.WindowsDesktopEntry");
                    Constructor<?> cons = cl.getConstructor(JNLPFile.class);
                    instance = cons.newInstance(file);
                    //catch both, for case that mslink was removed after build
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException | NoClassDefFoundError | InstantiationException e) {
                   LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
                }
                GenericDesktopEntry wde = (GenericDesktopEntry) instance;
                if (!wde.getDesktopIconFile().exists()) {
                    // if the desktop shortcut doesn't exist ask
                    AccessWarningPaneComplexReturn ics = getComplexReturn(file, sd);
                    if (ics != null && ics.toBoolean()) {
                        boolean isDesktop = false;
                        if (ics.getDesktop() != null && ics.getDesktop().isCreate()) {
                            isDesktop = true;
                        }
                        boolean isMenu = false;
                        if (ics.getMenu() != null && ics.getMenu().isCreate()) {
                            isMenu = true;
                        }
                        // if setting is always create then ics will be true but "get" properties will be null, so set to create
                        if (ics.getDesktop() == null && ics.toBoolean()) {
                            isDesktop = true;
                        };
                        if (ics.getMenu() == null && ics.toBoolean()) {
                            isMenu = true;
                        };
                        // create shortcuts if its ok
                        if (isDesktop) {
                            wde.createShortcutOnWindowsDesktop();
                        }
                        if (isMenu) {
                            wde.createWindowsMenu();
                        }
                    }
                } else {
                    // refresh shortcut if it already exists
                    wde.createShortcutOnWindowsDesktop();
                    wde.createWindowsMenu();
                }
            } catch (Throwable ex) {
                String message = Translator.R("WinDesktopError");
                LOG.error(message, ex);
            }
        } else {
            // do non-windows desktop stuff
            GenericDesktopEntry entry = new XDesktopEntry(file);
            File possibleDesktopFile = entry.getDesktopIconFile();
            File possibleMenuFile = entry.getLinuxMenuIconFile();
            File generatedJnlp = entry.getGeneratedJnlpFileName();
            //if one of menu or desktop exists, do not bother user
            boolean exists = false;
            if (possibleDesktopFile.exists()) {
                LOG.info("ApplicationInstance.addMenuAndDesktopEntries(): file - {} already exists. Refreshing and not proceeding with desktop additions", possibleDesktopFile.getAbsolutePath());
                exists = true;
                if (JNLPRuntime.isOnline()) {
                    entry.refreshExistingShortcuts(false, true); //update
                }
            }
            if (possibleMenuFile.exists()) {
                LOG.info("ApplicationInstance.addMenuAndDesktopEntries(): file - {} already exists. Refreshing and not proceeding with desktop additions", possibleMenuFile.getAbsolutePath());
                exists = true;
                if (JNLPRuntime.isOnline()) {
                    entry.refreshExistingShortcuts(true, false); //update
                }
            }
            if (generatedJnlp.exists()) {
                LOG.info("ApplicationInstance.addMenuAndDesktopEntries(): generated file - {} already exists. Refreshing and not proceeding with desktop additions", generatedJnlp.getAbsolutePath());
                exists = true;
                if (JNLPRuntime.isOnline()) {
                    entry.refreshExistingShortcuts(true, true); //update
                }
            }
            if (exists) {
                return;
            }
            AccessWarningPaneComplexReturn ics = getComplexReturn(file, sd);
            if (ics != null && ics.toBoolean()) {
                entry.createDesktopShortcuts(ics.getMenu(), ics.getDesktop());
            }
        }
    }

    /**
     * Indicates whether a desktop launcher/shortcut should be created for this
     * application instance
     *
     * @param sd the ShortcutDesc element from the JNLP file
     * @return true if a desktop shortcut should be created
     */
    private static AccessWarningPaneComplexReturn getComplexReturn(JNLPFile file, ShortcutDesc sd) {
        if (JNLPRuntime.isTrustAll()) {
            boolean mainResult = (sd != null && (sd.onDesktop() || sd.getMenu() != null));
            AccessWarningPaneComplexReturn r = new AccessWarningPaneComplexReturn(mainResult);
            if (mainResult){
                if (sd.onDesktop()){
                    r.setDesktop(new AccessWarningPaneComplexReturn.ShortcutResult(true));
                    r.getDesktop().setBrowser(XDesktopEntry.getBrowserBin());
                    r.getDesktop().setShortcutType(AccessWarningPaneComplexReturn.Shortcut.BROWSER);
                }
                if (sd.getMenu() != null){
                    r.setMenu(new AccessWarningPaneComplexReturn.ShortcutResult(true));
                    r.getMenu().setBrowser(XDesktopEntry.getBrowserBin());
                    r.getMenu().setShortcutType(AccessWarningPaneComplexReturn.Shortcut.BROWSER);
                }
            }
            return r;
        }
        String currentSetting = JNLPRuntime.getConfiguration()
                .getProperty(ConfigurationConstants.KEY_CREATE_DESKTOP_SHORTCUT);

        /*
         * check configuration and possibly prompt user to find out if a
         * shortcut should be created or not
         */
        switch (currentSetting) {
            case ShortcutDesc.CREATE_NEVER:
                return new AccessWarningPaneComplexReturn(false);
            case ShortcutDesc.CREATE_ALWAYS:
                return new AccessWarningPaneComplexReturn(true);
            case ShortcutDesc.CREATE_ASK_USER:
                return SecurityDialogs.showAccessWarningDialog(AccessType.CREATE_DESKTOP_SHORTCUT, file, null);
            case ShortcutDesc.CREATE_ASK_USER_IF_HINTED:
                if (sd != null && (sd.onDesktop() || sd.toMenu())) {
                    return SecurityDialogs.showAccessWarningDialog(AccessType.CREATE_DESKTOP_SHORTCUT, file, null);
                }
            case ShortcutDesc.CREATE_ALWAYS_IF_HINTED:
                if (sd != null && (sd.onDesktop() || sd.toMenu())) {
                    return new AccessWarningPaneComplexReturn(true);
                }
        }

        return new AccessWarningPaneComplexReturn(false);
    }
}
