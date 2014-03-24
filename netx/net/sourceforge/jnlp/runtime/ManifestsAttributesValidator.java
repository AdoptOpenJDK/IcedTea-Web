/* 
Copyright (C) 2011 Red Hat, Inc.

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
package net.sourceforge.jnlp.runtime;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import net.sourceforge.jnlp.ExtensionDesc;
import net.sourceforge.jnlp.JARDesc;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.JNLPFile.ManifestBoolean;
import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.PluginBridge;
import net.sourceforge.jnlp.ResourcesDesc;
import net.sourceforge.jnlp.SecurityDesc;
import net.sourceforge.jnlp.runtime.JNLPClassLoader.SecurityDelegate;
import net.sourceforge.jnlp.runtime.JNLPClassLoader.SigningState;
import net.sourceforge.jnlp.security.SecurityDialogs;
import net.sourceforge.jnlp.security.appletextendedsecurity.AppletSecurityLevel;
import net.sourceforge.jnlp.security.appletextendedsecurity.AppletStartupSecuritySettings;
import net.sourceforge.jnlp.util.ClasspathMatcher.ClasspathMatchers;
import net.sourceforge.jnlp.util.UrlUtils;
import net.sourceforge.jnlp.util.logging.OutputController;

public class ManifestsAttributesValidator {

    private final SecurityDesc security;
    private final JNLPFile file;
    private final SigningState signing;
    private final SecurityDelegate securityDelegate;

    public ManifestsAttributesValidator(final SecurityDesc security, final JNLPFile file,
            final SigningState signing, final SecurityDelegate securityDelegate) {
        this.security = security;
        this.file = file;
        this.signing = signing;
        this.securityDelegate = securityDelegate;
    }

    /**
     * http://docs.oracle.com/javase/7/docs/technotes/guides/jweb/security/manifest.html#trusted_only
     */
    void checkTrustedOnlyAttribute() throws LaunchException {
        final ManifestBoolean trustedOnly = file.getManifestsAttributes().isTrustedOnly();
        if (trustedOnly == ManifestBoolean.UNDEFINED) {
            OutputController.getLogger().log(OutputController.Level.MESSAGE_DEBUG, "Trusted Only manifest attribute not found. Continuing.");
            return;
        }

        if (trustedOnly == ManifestBoolean.FALSE) {
            OutputController.getLogger().log(OutputController.Level.MESSAGE_DEBUG, "Trusted Only manifest attribute is false. Continuing.");
            return;
        }

        final Object desc = security.getSecurityType();

        final String securityType;
        if (desc == null) {
            securityType = "Not Specified";
        } else if (desc.equals(SecurityDesc.ALL_PERMISSIONS)) {
            securityType = "All-Permission";
        } else if (desc.equals(SecurityDesc.SANDBOX_PERMISSIONS)) {
            securityType = "Sandbox";
        } else if (desc.equals(SecurityDesc.J2EE_PERMISSIONS)) {
            securityType = "J2EE";
        } else {
            securityType = "Unknown";
        }

        final boolean isFullySigned = signing == SigningState.FULL;
        final boolean isSandboxed = securityDelegate.getRunInSandbox();
        final boolean requestsCorrectPermissions = (isFullySigned && SecurityDesc.ALL_PERMISSIONS.equals(desc))
                || (isSandboxed && SecurityDesc.SANDBOX_PERMISSIONS.equals(desc));
        final String signedMsg;
        if (isFullySigned && !isSandboxed) {
            signedMsg = "The applet is fully signed";
        } else if (isFullySigned && isSandboxed) {
            signedMsg = "The applet is fully signed and sandboxed";
        } else {
            signedMsg = "The applet is not fully signed";
        }
        OutputController.getLogger().log(OutputController.Level.MESSAGE_DEBUG,
                "Trusted Only manifest attribute is \"true\". " + signedMsg + " and requests permission level: " + securityType);
        if (!(isFullySigned && requestsCorrectPermissions)) {
            throw new LaunchException(Translator.R("STrustedOnlyAttributeFailure", signedMsg, securityType));
        }
    }

    /**
     * http://docs.oracle.com/javase/7/docs/technotes/guides/jweb/manifest.html#codebase
     */
    void checkCodebaseAttribute() throws LaunchException {
        if (file.getCodeBase() == null || file.getCodeBase().getProtocol().equals("file")) {
            OutputController.getLogger().log(OutputController.Level.WARNING_ALL, Translator.R("CBCheckFile"));
            return;
        }
        final Object securityType = security.getSecurityType();
        final URL codebase = UrlUtils.guessCodeBase(file);
        final ClasspathMatchers codebaseAtt = file.getManifestsAttributes().getCodebase();
        if (codebaseAtt == null) {
            OutputController.getLogger().log(OutputController.Level.WARNING_ALL, Translator.R("CBCheckNoEntry"));
            return;
        }
        if (securityType.equals(SecurityDesc.SANDBOX_PERMISSIONS)) {
            if (codebaseAtt.matches(codebase)) {
                OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, Translator.R("CBCheckUnsignedPass"));
            } else {
                OutputController.getLogger().log(OutputController.Level.ERROR_ALL, Translator.R("CBCheckUnsignedFail"));
            }
        } else {
            if (codebaseAtt.matches(codebase)) {
                OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, Translator.R("CBCheckOkSignedOk"));
            } else {
                if (file instanceof PluginBridge) {
                    throw new LaunchException(Translator.R("CBCheckSignedAppletDontMatchException", file.getManifestsAttributes().getCodebase().toString(), codebase));
                } else {
                    OutputController.getLogger().log(OutputController.Level.ERROR_ALL, Translator.R("CBCheckSignedFail"));
                }
            }
        }

    }

    /**
     * http://docs.oracle.com/javase/7/docs/technotes/guides/jweb/security/manifest.html#permissions
     */
    void checkPermissionsAttribute() throws LaunchException {
        final ManifestBoolean permissions = file.getManifestsAttributes().isSandboxForced();
        AppletSecurityLevel level = AppletStartupSecuritySettings.getInstance().getSecurityLevel();
        if (level == AppletSecurityLevel.ALLOW_UNSIGNED || securityDelegate.getRunInSandbox()) {
            OutputController.getLogger().log(OutputController.Level.WARNING_ALL, "Although 'permissions' attribute of this application is '" + file.getManifestsAttributes().permissionsToString()
                    + "' Your Extended applets security is at 'low', or you have specifically chosen to run the applet Sandboxed. Continuing");
            return;
        }
        switch (permissions) {
            case UNDEFINED: {
                if (level == AppletSecurityLevel.DENY_UNSIGNED) {
                    throw new LaunchException("Your Extended applets security is at 'Very high', and this application is missing the 'permissions' attribute in manifest. This is fatal");
                }
                if (level == AppletSecurityLevel.ASK_UNSIGNED) {
                    boolean a = SecurityDialogs.showMissingPermissionsAttributeDialogue(file.getTitle(), file.getCodeBase());
                    if (!a) {
                        throw new LaunchException("Your Extended applets security is at 'high' and  this applicationis missing the 'permissions' attribute in manifest. And you have refused to run it.");
                    } else {
                        OutputController.getLogger().log("Your Extended applets security is at 'high' and  this applicationis missing the 'permissions' attribute in manifest. And you have allowed to run it.");
                    }
                }
                //default for missing is sandbox
                if (!SecurityDesc.SANDBOX_PERMISSIONS.equals(security.getSecurityType())) {
                    throw new LaunchException("The 'permissions' attribute is not specified, and application is requesting permissions. This is fatal");
                }
                break;
            }
            case TRUE: {
                if (SecurityDesc.SANDBOX_PERMISSIONS.equals(security.getSecurityType())) {
                    OutputController.getLogger().log("The permissions attribute of this application is " + file.getManifestsAttributes().permissionsToString() + "' and security is '" + security.getSecurityType() + "'. Thats correct");
                } else {
                    throw new LaunchException("The 'permissions' attribute is '" + file.getManifestsAttributes().permissionsToString() + "' but  security is '" + security.getSecurityType() + "'. This is fatal");
                }
            }
            case FALSE: {
                if (SecurityDesc.SANDBOX_PERMISSIONS.equals(security.getSecurityType())) {
                    throw new LaunchException("The 'permissions' attribute is '" + file.getManifestsAttributes().permissionsToString() + "' but  security is' " + security.getSecurityType() + "'. This is fatal");
                } else {
                    OutputController.getLogger().log("The permissions attribute of this application is '" + file.getManifestsAttributes().permissionsToString() + "' and security is '" + security.getSecurityType() + "'. Thats correct");
                }
            }
        }
    }

    void checkApplicationLibraryAllowableCodebaseAttribute() throws LaunchException {
        if (signing == SigningState.NONE) {
            return; /*when app is not signed at all, then skip this check*/
        }
        //conditions
        URL codebase = file.getCodeBase();
        URL documentBase = null;
        if (file instanceof PluginBridge) {
            documentBase = ((PluginBridge) file).getSourceLocation();
        }
        if (documentBase == null) {
            documentBase = file.getCodeBase();
        }

        //cases
        Set<URL> usedUrls = new HashSet<URL>();
        URL sourceLocation = file.getSourceLocation();
        ResourcesDesc[] resourcesDescs = file.getResourcesDescs();
        if (sourceLocation != null) {
            usedUrls.add(UrlUtils.removeFileName(sourceLocation));
        }
        for (ResourcesDesc resourcesDesc : resourcesDescs) {
            ExtensionDesc[] ex = resourcesDesc.getExtensions();
            if (ex != null) {
                for (ExtensionDesc extensionDesc : ex) {
                    if (extensionDesc != null) {
                        usedUrls.add(UrlUtils.removeFileName(extensionDesc.getLocation()));
                    }
                }
            }
            JARDesc[] jars = resourcesDesc.getJARs();
            if (jars != null) {
                for (JARDesc jarDesc : jars) {
                    if (jarDesc != null) {
                        usedUrls.add(UrlUtils.removeFileName(jarDesc.getLocation()));
                    }
                }
            }
            JNLPFile jnlp = resourcesDesc.getJNLPFile();
            if (jnlp != null) {
                usedUrls.add(UrlUtils.removeFileName(jnlp.getSourceLocation()));
            }

        }
        OutputController.getLogger().log("Found alaca URLs to be verified");
        for (URL url : usedUrls) {
            OutputController.getLogger().log(" - " + url.toExternalForm());
        }
        if (usedUrls.isEmpty()) {
            //I hope this is the case, when the resources is/are
            //only codebase classes. Then it should be safe to return.
            OutputController.getLogger().log("The application is not using any url resources, skipping Application-Library-Allowable-Codebase Attribute check.");
            return;
        }

        if (usedUrls.size() == 1) {
            if (UrlUtils.equalsIgnoreLastSlash(usedUrls.toArray(new URL[0])[0], codebase)
                    && UrlUtils.equalsIgnoreLastSlash(usedUrls.toArray(new URL[0])[0], documentBase)) {
                //all resoources are from codebase or document base. it is ok to proceeed.
                OutputController.getLogger().log("All applications resources (" + usedUrls.toArray(new URL[0])[0] + ") are from codebas/documentbase " + codebase + "/" + documentBase + ", skipping Application-Library-Allowable-Codebase Attribute check.");
                return;
            }
        }
        ClasspathMatchers att = file.getManifestsAttributes().getApplicationLibraryAllowableCodebase();

        if (att == null) {
            boolean a = SecurityDialogs.showMissingALACAttributePanel(file.getTitle(), documentBase, usedUrls);
            if (!a) {
                throw new LaunchException("The application uses non-codebase resources, has no Application-Library-Allowable-Codebase Attribute, and was blocked from running by the user");
            } else {
                OutputController.getLogger().log("The application uses non-codebase resources, has no Application-Library-Allowable-Codebase Attribute, and was allowed to run by the user");
                return;
            }
        } else {
            for (URL foundUrl : usedUrls) {
                if (!att.matches(foundUrl)) {
                    throw new LaunchException("The resource from " + foundUrl + " does not match the  location in Application-Library-Allowable-Codebase Attribute " + att + ". Blocking the application from running.");
                } else {
                    OutputController.getLogger().log("The resource from " + foundUrl + " does  match the  location in Application-Library-Allowable-Codebase Attribute " + att + ". Continuing.");
                }
            }
        }
        boolean a = SecurityDialogs.showMatchingALACAttributePanel(file.getTitle(), documentBase, usedUrls);
        if (!a) {
            throw new LaunchException("The application uses non-codebase resources, which do match its Application-Library-Allowable-Codebase Attribute, but was blocked from running by the user.");
        } else {
            OutputController.getLogger().log("The application uses non-codebase resources, which do match its Application-Library-Allowable-Codebase Attribute, and was allowed to run by the user.");
        }
    }
}
