/*   Copyright (C) 2013 Red Hat, Inc.

 This file is part of IcedTea.

 IcedTea is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by
 the Free Software Foundation, version 2.

 IcedTea is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with IcedTea; see the file COPYING.  If not, write to
 the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 02110-1301 USA.

 Linking this library statically or dynamically with other modules is
 making a combined work based on this library.  Thus, the terms and
 conditions of the GNU General Public License cover the whole
 combination.

 As a special exception, the copyright holders of this library give you
 permission to link this library with independent modules to produce an
 executable, regardless of the license terms of these independent
 modules, and to copy and distribute the resulting executable under
 terms of your choice, provided that you also meet, for each linked
 independent module, the terms and conditions of the license of that
 module.  An independent module is a module which is not derived from
 or based on this library.  If you modify this library, you may extend
 this exception to your version of the library, but you are not
 obligated to do so.  If you do not wish to do so, delete this
 exception statement from your version.
 */

package net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity;

import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.SecurityDialogs;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember.AppletSecurityActions;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember.ExecuteAppletAction;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember.RememberableDialog;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.remember.SavedRememberAction;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.Primitive;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.YesNo;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.YesNoSandbox;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.PluginBridge;
import net.sourceforge.jnlp.runtime.JNLPClassLoader.SecurityDelegate;
import net.sourceforge.jnlp.security.CertVerifier;
import net.sourceforge.jnlp.util.UrlUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class UnsignedAppletTrustConfirmation {

    private final static Logger LOG = LoggerFactory.getLogger(UnsignedAppletTrustConfirmation.class);

    private static final AppletStartupSecuritySettings securitySettings = AppletStartupSecuritySettings.getInstance();

    private static boolean unsignedConfirmationIsRequired() {
        // If we are using the 'high' security setting or higher, we must confirm
        // if the user wishes to run unsigned applets (not applicable to JNLP-launched apps)
        return !(AppletSecurityLevel.ALLOW_UNSIGNED == securitySettings.getSecurityLevel());
    }

    private static boolean unsignedAppletsAreForbidden() {
        // If we are using the 'very high' security setting or higher, we do not
        // run unsigned applets
        return AppletSecurityLevel.DENY_UNSIGNED == securitySettings.getSecurityLevel()
                || AppletSecurityLevel.DENY_ALL == securitySettings.getSecurityLevel();
    }

    /**
     * Gets the remembered decision, first checking the user policy for an ALWAYS/NEVER,
     * and then the global policy.
     *
     * @param file the plugin file
     * @param id of wonted  action
     * @return the remembered decision
     */
    public static UnsignedAppletActionEntry getStoredEntry(JNLPFile file, Class<? extends RememberableDialog> id) {
        UnsignedAppletActionStorage userActionStorage = securitySettings.getUnsignedAppletActionCustomStorage();
        UnsignedAppletActionStorage globalActionStorage = securitySettings.getUnsignedAppletActionGlobalStorage();

        UnsignedAppletActionEntry globalEntry = getMatchingItem(globalActionStorage, file, id);
        UnsignedAppletActionEntry userEntry = getMatchingItem(userActionStorage, file, id);

        ExecuteAppletAction globalAction = globalEntry == null ? null : globalEntry.getAppletSecurityActions().getAction(id);
        ExecuteAppletAction userAction = userEntry == null ? null : userEntry.getAppletSecurityActions().getAction(id);

        if (userAction == ExecuteAppletAction.ALWAYS || userAction == ExecuteAppletAction.NEVER) {
            return userEntry;
        } else if (globalAction == ExecuteAppletAction.ALWAYS || globalAction == ExecuteAppletAction.NEVER) {
            return globalEntry;
        } else {
            return userEntry;
        }
    }
    public static ExecuteAppletAction getStoredAction(JNLPFile file, Class<? extends RememberableDialog> id) {
        UnsignedAppletActionEntry x = getStoredEntry(file, id);
        if (x != null) {
            return x.getAppletSecurityActions().getAction(id);
        }
        return null;
    }

    private static UnsignedAppletActionEntry getMatchingItem(UnsignedAppletActionStorage actionStorage, JNLPFile file, Class<? extends RememberableDialog> id) {
        URL location = (file.getSourceLocation() != null) ? file.getSourceLocation() : file.getFileLocation();
        return actionStorage.getMatchingItem(
                UrlUtils.normalizeUrlAndStripParams(location, true /* encode local files */).toString(),
                UrlUtils.normalizeUrlAndStripParams(file.getNotNullProbableCodeBase(), true /* encode local files */).toString(),
                toRelativePaths(getJars(file), file.getNotNullProbableCodeBase().toExternalForm()), id);
    }

    /* Extract the archives as relative paths */
    static List<String> toRelativePaths(List<String> paths, String rootPath) {
        List<String> fileNames = new ArrayList<>();
        for (String path : paths) {
            if (path.startsWith(rootPath)) {
                fileNames.add(path.substring(rootPath.length()));
            } else {
                fileNames.add(path);
            }
        }
        return fileNames;
    }

    public static void updateAppletAction(JNLPFile file, SavedRememberAction behaviour, Boolean rememberForCodeBase, Class<? extends RememberableDialog> id) {
        UnsignedAppletActionStorage userActionStorage = securitySettings.getUnsignedAppletActionCustomStorage();

        userActionStorage.lock(); // We should ensure this operation is atomic
        try {
            UnsignedAppletActionEntry oldEntry = getMatchingItem(userActionStorage, file, id);

            URL codebase = UrlUtils.normalizeUrlAndStripParams(file.getNotNullProbableCodeBase(), true /* encode local files */);
            URL documentbase = UrlUtils.normalizeUrlAndStripParams(file.getSourceLocation(), true /* encode local files */);

            UrlRegEx codebaseRegex = null;
            UrlRegEx documentbaseRegex = null;
            List<String> archiveMatches = null;

            if (rememberForCodeBase != null) {

                codebaseRegex = UrlRegEx.quote(codebase.toExternalForm());

                if (!rememberForCodeBase) {
                    documentbaseRegex = UrlRegEx.quote(documentbase.toExternalForm()); // Match only this applet
                    archiveMatches = toRelativePaths(getJars(file), file.getNotNullProbableCodeBase().toString()); // Match only this applet
                } else {
                    documentbaseRegex = UrlRegEx.quoteAndStar(UrlUtils.stripFile(documentbase)); // Match any from codebase and sourceFile "base"
                }
            }

            /* Update, if entry exists */
            if (oldEntry != null) {
                oldEntry.getAppletSecurityActions().setAction(id, behaviour);
                oldEntry.setTimeStamp(new Date());
                if (rememberForCodeBase != null) {
                    oldEntry.setDocumentBase(documentbaseRegex);
                    oldEntry.setCodeBase(codebaseRegex);
                }
                oldEntry.setArchives(archiveMatches);
                userActionStorage.update(oldEntry);
                return;
            }
              if (rememberForCodeBase == null){
                  throw new RuntimeException("Trying to create new entry without codebase. That's forbidden.");
              }
                              /* Else, create a new entry */
            UnsignedAppletActionEntry entry = new UnsignedAppletActionEntry(
                    AppletSecurityActions.fromAction(id, behaviour),
                    new Date(),
                    documentbaseRegex,
                    codebaseRegex,
                    archiveMatches
            );

            userActionStorage.add(entry);
        } finally {
            userActionStorage.unlock();
        }
    }

    private static List<String> getJars(JNLPFile file) {
        if (file instanceof PluginBridge)
            return ((PluginBridge) file).getArchiveJars();

        List<JARDesc> jars = Arrays.asList(file.getResources().getJARs());
        List<String> result = new ArrayList<>();
        for (JARDesc jar : jars) {
            result.add(jar.getLocation().toString());
        }
        return result;
    }

    public static void checkUnsignedWithUserIfRequired(JNLPFile file) throws LaunchException {

        if (unsignedAppletsAreForbidden()) {
            LOG.debug("Not running unsigned applet at {} because unsigned applets are disallowed by security policy.", file.getCodeBase());
            throw new LaunchException(file, null, "Fatal", "Application Error", "The applet was unsigned.", "The applet was unsigned.PolicyDenied");
        }

        if (!unsignedConfirmationIsRequired()) {
            LOG.debug("Running unsigned applet at {} does not require confirmation according to security policy.", file.getCodeBase());
            return;
        }

        YesNo warningResponse = SecurityDialogs.showUnsignedWarningDialog(file);

        LOG.debug("Decided action for unsigned applet at {} was {}", file.getCodeBase(), warningResponse);

        if (warningResponse == null || !warningResponse.compareValue(Primitive.YES)) {
            throw new LaunchException(file, null, "Fatal", "Application Error", "The applet was unsigned.", "The applet was unsigned.UserDenied");
        }

    }

    public static void checkPartiallySignedWithUserIfRequired(SecurityDelegate securityDelegate, JNLPFile file,
            CertVerifier certVerifier) throws LaunchException {

        if (!unsignedConfirmationIsRequired()) {
            LOG.debug("Running partially signed applet at {} does not require confirmation according to security policy.", file.getCodeBase());
            return;
        }

        YesNoSandbox warningResponse = SecurityDialogs.showPartiallySignedWarningDialog(file, certVerifier, securityDelegate);

        LOG.debug("Decided action for unsigned applet at {} was {}", file.getCodeBase(), warningResponse);

        if (warningResponse == null || warningResponse.compareValue(Primitive.NO)) {
            throw new LaunchException(file, null, "Fatal", "Application Error", "The applet was partially signed.", "The applet was partially signed.UserDenied");
        }

        //this is due to possible YesNoSandboxLimited
        if (YesNoSandbox.sandbox().compareValue(warningResponse)) {
            securityDelegate.setRunInSandbox();
        }

    }

}
