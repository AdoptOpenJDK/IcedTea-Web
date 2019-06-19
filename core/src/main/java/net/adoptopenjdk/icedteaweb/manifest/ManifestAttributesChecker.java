/*
Copyright (C) 2011 Red Hat, Inc.
Copyright (C) 2019 Karakun AG

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
package net.adoptopenjdk.icedteaweb.manifest;

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.SecurityDialogs;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity.AppletSecurityLevel;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity.AppletStartupSecuritySettings;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.ExtensionDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.ResourcesDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.security.AppletPermissionLevel;
import net.adoptopenjdk.icedteaweb.jnlp.element.security.SecurityDesc;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.PluginBridge;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.runtime.JNLPClassLoader.SecurityDelegate;
import net.sourceforge.jnlp.runtime.JNLPClassLoader.SigningState;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.ClasspathMatcher.ClasspathMatchers;
import net.sourceforge.jnlp.util.UrlUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.adoptopenjdk.icedteaweb.config.validators.ValidatorUtils.splitCombination;
import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;

public class ManifestAttributesChecker {

    private final static Logger LOG = LoggerFactory.getLogger(ManifestAttributesChecker.class);

    private final SecurityDesc security;
    private final JNLPFile file;
    private final SigningState signing;
    private final SecurityDelegate securityDelegate;

    public ManifestAttributesChecker(final SecurityDesc security, final JNLPFile file,
            final SigningState signing, final SecurityDelegate securityDelegate) {
        this.security = security;
        this.file = file;
        this.signing = signing;
        this.securityDelegate = securityDelegate;
    }

    public enum MANIFEST_ATTRIBUTES_CHECK {
        ALL,
        NONE,
        PERMISSIONS,
        CODEBASE,
        TRUSTED,
        ALAC,
        ENTRYPOINT
    }

    public void checkAll() throws LaunchException {
        List<MANIFEST_ATTRIBUTES_CHECK> attributesCheck = getAttributesCheck();
        if (attributesCheck.contains(MANIFEST_ATTRIBUTES_CHECK.NONE)) {
            LOG.warn("Manifest attribute checks are disabled.");
        } else {

            if (attributesCheck.contains(MANIFEST_ATTRIBUTES_CHECK.TRUSTED) ||
                    attributesCheck.contains(MANIFEST_ATTRIBUTES_CHECK.ALL)) {
                checkTrustedOnlyAttribute();
            } else {
                LOG.warn("check on {} skipped because property of deployment.manifest.attributes.check was not set to ALL or includes {} in the combination of options", "Trusted-Only", "TRUSTED");
            }

            if (attributesCheck.contains(MANIFEST_ATTRIBUTES_CHECK.CODEBASE) ||
                    attributesCheck.contains(MANIFEST_ATTRIBUTES_CHECK.ALL)) {
                checkCodebaseAttribute();
            } else {
                LOG.warn("check on {} skipped because property of deployment.manifest.attributes.check was not set to ALL or includes {} in the combination of options", "Codebase", "CODEBASE");
            }

            if (attributesCheck.contains(MANIFEST_ATTRIBUTES_CHECK.PERMISSIONS) ||
                    attributesCheck.contains(MANIFEST_ATTRIBUTES_CHECK.ALL)) {
                checkPermissionsAttribute();
            } else {
                LOG.warn("check on {} skipped because property of deployment.manifest.attributes.check was not set to ALL or includes {} in the combination of options", "Permissions", "PERMISSIONS");
            }

            if (attributesCheck.contains(MANIFEST_ATTRIBUTES_CHECK.ALAC) ||
                   attributesCheck.contains(MANIFEST_ATTRIBUTES_CHECK.ALL)) {
                checkApplicationLibraryAllowableCodebaseAttribute();
            } else {
                LOG.warn("check on {} skipped because property of deployment.manifest.attributes.check was not set to ALL or includes {} in the combination of options", "Application Library Allowable Codebase", "ALAC");
            }

            if (attributesCheck.contains(MANIFEST_ATTRIBUTES_CHECK.ENTRYPOINT)
                    || attributesCheck.contains(MANIFEST_ATTRIBUTES_CHECK.ALL)) {
                checkEntryPoint();
            } else {
                LOG.warn("check on {} skipped because property of deployment.manifest.attributes.check was not set to ALL or includes {} in the combination of options", "Entry-Point", "ENTRYPOINT");
            }

        }
    }

    public static List<MANIFEST_ATTRIBUTES_CHECK> getAttributesCheck() {
        final String deploymentProperty = JNLPRuntime.getConfiguration().getProperty(ConfigurationConstants.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK);
        String[] attributesCheck = splitCombination(deploymentProperty);
        List<MANIFEST_ATTRIBUTES_CHECK> manifestAttributesCheckList = new ArrayList<>();
        for (String attribute : attributesCheck) {
            for (MANIFEST_ATTRIBUTES_CHECK manifestAttribute  : MANIFEST_ATTRIBUTES_CHECK.values()) {
                if (manifestAttribute.toString().equals(attribute)) {
                    manifestAttributesCheckList.add(manifestAttribute);
                }
            }
        }
        return manifestAttributesCheckList;
    }

    /**
     * http://docs.oracle.com/javase/7/docs/technotes/guides/jweb/security/manifest.html#entry_pt
     */
    private void checkEntryPoint() throws LaunchException {
        if (signing == SigningState.NONE) {
            return; /*when app is not signed at all, then skip this check*/
        }
        if (file.getEntryPointDesc() == null) {
            LOG.debug("Entry-Point can not be checked now, because of not existing launch info.");
            return;
        }
        if (file.getEntryPointDesc().getMainClass() == null) {
            LOG.debug("Entry-Point can not be checked now, because of unknown main class.");
            return;
        }
        final String[] eps = file.getManifestAttributesReader().getEntryPoints();
        String mainClass = file.getEntryPointDesc().getMainClass();
        if (eps == null) {
            LOG.debug("Entry-Point manifest attribute for yours '{}' not found. Continuing.", mainClass);
            return;
        }
        for (String ep : eps) {
            if (ep.equals(mainClass)) {
                LOG.debug("Entry-Point of {} matches {} continuing.", ep, mainClass);
                return;
            }
        }
        throw new LaunchException("None of the entry points specified: '" + file.getManifestAttributesReader().getEntryPoint() + "' matched the main class " + mainClass + " and applet is signed. This is a security error and the app will not be launched.");
    }

    /**
     * http://docs.oracle.com/javase/7/docs/technotes/guides/jweb/security/manifest.html#trusted_only
     */
    private void checkTrustedOnlyAttribute() throws LaunchException {
        final ManifestBoolean trustedOnly = file.getManifestAttributesReader().isTrustedOnly();
        if (trustedOnly == ManifestBoolean.UNDEFINED) {
            LOG.debug("Trusted Only manifest attribute not found. Continuing.");
            return;
        }

        if (trustedOnly == ManifestBoolean.FALSE) {
            LOG.debug("Trusted Only manifest attribute is false. Continuing.");
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
            signedMsg = R("STOAsignedMsgFully");
        } else if (isFullySigned) {
            signedMsg = R("STOAsignedMsgAndSandbox");
        } else {
            signedMsg = R("STOAsignedMsgPartiall");
        }
        LOG.debug("Trusted Only manifest attribute is \"true\". {} and requests permission level: {}", signedMsg, securityType);
        if (!(isFullySigned && requestsCorrectPermissions)) {
            throw new LaunchException("This application specifies Trusted-only as True in its Manifest. " + signedMsg + " and requests permission level: " + securityType + ". This is not allowed.");
        }
    }

    /**
     * http://docs.oracle.com/javase/7/docs/technotes/guides/jweb/manifest.html#codebase
     */
    private void checkCodebaseAttribute() throws LaunchException {
        if (file.getCodeBase() == null || file.getCodeBase().getProtocol().equals("file")) {
            LOG.warn("The application is a local file. Codebase validation is disabled. See: http://docs.oracle.com/javase/7/docs/technotes/guides/jweb/security/no_redeploy.html for details.");
            return;
        }
        final Object securityType = security.getSecurityType();
        final URL codebase = UrlUtils.guessCodeBase(file);
        final ClasspathMatchers codebaseAtt = file.getManifestAttributesReader().getCodebase();
        if (codebaseAtt == null) {
            LOG.warn("This application does not specify a Codebase in its manifest. Please verify with the applet''s vendor. Continuing. See: http://docs.oracle.com/javase/7/docs/technotes/guides/jweb/security/no_redeploy.html for details.");
            return;
        }
        if (securityType.equals(SecurityDesc.SANDBOX_PERMISSIONS)) {
            if (codebaseAtt.matches(codebase)) {
                LOG.info("Codebase matches codebase manifest attribute, but application is unsigned. Continuing. See: http://docs.oracle.com/javase/7/docs/technotes/guides/jweb/security/no_redeploy.html for details.");
            } else {
                LOG.error("The application''s codebase does NOT match the codebase specified in its manifest, but the application is unsigned. Continuing. See: http://docs.oracle.com/javase/7/docs/technotes/guides/jweb/security/no_redeploy.html for details.");
            }
        } else {
            if (codebaseAtt.matches(codebase)) {
                LOG.info("Codebase matches codebase manifest attribute, and application is signed. Continuing. See: http://docs.oracle.com/javase/7/docs/technotes/guides/jweb/security/no_redeploy.html for details.");
            } else {
                if (file instanceof PluginBridge) {
                    throw new LaunchException("Signed applets are not allowed to run when their actual Codebase does not match the Codebase specified in their manifest. Expected: "+
                            file.getManifestAttributesReader().getCodebase().toString() + ". Actual: " +
                            codebase + ". See: http://docs.oracle.com/javase/7/docs/technotes/guides/jweb/security/no_redeploy.html for details.");
                } else {
                    LOG.error("Application Codebase does NOT match the Codebase specified in the application''s manifest, and this application is signed. You are strongly discouraged from running this application. See: http://docs.oracle.com/javase/7/docs/technotes/guides/jweb/security/no_redeploy.html for details.");
                }
            }
        }

    }

    /**
     * http://docs.oracle.com/javase/7/docs/technotes/guides/jweb/security/manifest.html#permissions
     */
    private void checkPermissionsAttribute() throws LaunchException {
        if (securityDelegate.getRunInSandbox()) {
            LOG.warn("The 'Permissions' attribute of this application is '{}'. You have chosen the Sandbox run option, which overrides the Permissions manifest attribute, or the applet has already been automatically sandboxed.", permissionsToString());
            return;
        }

        final ManifestBoolean sandboxForced = isSandboxForced();
        // If the attribute is not specified in the manifest, prompt the user. Oracle's spec says that the
        // attribute is required, but this breaks a lot of existing applets. Therefore, when on the highest
        // security level, we refuse to run these applets. On the standard security level, we ask. And on the
        // lowest security level, we simply proceed without asking.
        if (sandboxForced == ManifestBoolean.UNDEFINED) {
            final AppletSecurityLevel itwSecurityLevel = AppletStartupSecuritySettings.getInstance().getSecurityLevel();
            if (itwSecurityLevel == AppletSecurityLevel.DENY_UNSIGNED) {
                throw new LaunchException("Your Extended applets security is at 'Very high', and this application is missing the 'permissions' attribute in manifest. This is fatal");
            }
            if (itwSecurityLevel == AppletSecurityLevel.ASK_UNSIGNED) {
                final boolean userApproved = SecurityDialogs.showMissingPermissionsAttributeDialogue(file);
                if (!userApproved) {
                    throw new LaunchException("Your Extended applets security is at 'high' and this application is missing the 'permissions' attribute in manifest. And you have refused to run it.");
                } else {
                    LOG.debug("Your Extended applets security is at 'high' and this application is missing the 'permissions' attribute in manifest. And you have allowed to run it.");
                }
            }
            return;
        }

        final AppletPermissionLevel requestedPermissionLevel = file.getAppletPermissionLevel();
        validateRequestedPermissionLevelMatchesManifestPermissions(requestedPermissionLevel, sandboxForced);
        if (file instanceof PluginBridge) { // HTML applet
            if (isNoneOrDefault(requestedPermissionLevel)) {
                if (sandboxForced == ManifestBoolean.TRUE && signing != SigningState.NONE) {
                    securityDelegate.setRunInSandbox();
                }
            }
        } else { // JNLP
            if (isNoneOrDefault(requestedPermissionLevel)) {
                if (sandboxForced == ManifestBoolean.TRUE && signing != SigningState.NONE) {
                    LOG.warn("The 'permissions' attribute is '{}' and the applet is signed. Forcing sandbox.", permissionsToString());
                    securityDelegate.setRunInSandbox();
                }
                if (sandboxForced == ManifestBoolean.FALSE && signing == SigningState.NONE) {
                    LOG.warn("The 'permissions' attribute is '{}' and the applet is unsigned. Forcing sandbox.", permissionsToString());
                    securityDelegate.setRunInSandbox();
                }
            }
        }
    }

    private static boolean isLowSecurity() {
        return AppletStartupSecuritySettings.getInstance().getSecurityLevel().equals(AppletSecurityLevel.ALLOW_UNSIGNED);
    }

    private static boolean isNoneOrDefault(final AppletPermissionLevel requested) {
        return requested == AppletPermissionLevel.NONE || requested == AppletPermissionLevel.DEFAULT;
    }

    private void validateRequestedPermissionLevelMatchesManifestPermissions(final AppletPermissionLevel requested, final ManifestBoolean sandboxForced) throws LaunchException {
        if (requested == AppletPermissionLevel.ALL && sandboxForced != ManifestBoolean.FALSE) {
            throw new LaunchException("The 'permissions' attribute is '" + permissionsToString() + "' but the applet requested " + requested + ". This is fatal");
        }

        if (requested == AppletPermissionLevel.SANDBOX && sandboxForced != ManifestBoolean.TRUE) {
            throw new LaunchException("The 'permissions' attribute is '" + permissionsToString() + "' but the applet requested " + requested + ". This is fatal");
        }
    }

    private void checkApplicationLibraryAllowableCodebaseAttribute() throws LaunchException {
        //conditions
        URL codebase = file.getCodeBase();
        URL documentBase = null;
        if (file instanceof PluginBridge) {
            documentBase = file.getSourceLocation();
        }
        if (documentBase == null) {
            documentBase = file.getCodeBase();
        }

        //cases
        Set<URL> usedUrls = new HashSet<>();
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
        }
        LOG.debug("Found alaca URLs to be verified");
        for (URL url : usedUrls) {
            LOG.debug(" - {}", url.toExternalForm());
        }
        if (usedUrls.isEmpty()) {
            //I hope this is the case, when the resources is/are
            //only codebase classes. Then it should be safe to return.
            LOG.debug("The application is not using any url resources, skipping Application-Library-Allowable-Codebase Attribute check.");
            return;
        }

        boolean allOk = true;
        for (URL u : usedUrls) {
            if (UrlUtils.urlRelativeTo(u, codebase)
                    && UrlUtils.urlRelativeTo(u, stripDocbase(documentBase))) {
                LOG.debug("OK - {} is from codebase/docbase.", u.toExternalForm());
            } else {
                allOk = false;
                LOG.warn("Warning! {} is NOT from codebase/docbase.", u.toExternalForm());
            }
        }
        if (allOk) {
            //all resources are from codebase or document base. it is ok to proceed.
            LOG.debug("All applications resources ({}) are from codebase/documentbase {}/{}, skipping Application-Library-Allowable-Codebase Attribute check.", usedUrls.toArray(new URL[0])[0], codebase, documentBase);
            return;
        }

        ClasspathMatchers att = null;
        if (signing != SigningState.NONE) {
            // we only consider values in manifest for signed apps (as they may be faked)
            att = file.getManifestAttributesReader().getApplicationLibraryAllowableCodebase();
        }
        if (att == null) {
            final boolean userApproved = SecurityDialogs.showMissingALACAttributePanel(file, documentBase, usedUrls);
            if (!userApproved) {
                throw new LaunchException("The application uses non-codebase resources, has no Application-Library-Allowable-Codebase Attribute, and was blocked from running by the user");
            } else {
                LOG.debug("The application uses non-codebase resources, has no Application-Library-Allowable-Codebase Attribute, and was allowed to run by the user or user's security settings");
                return;
            }
        } else {
            for (URL foundUrl : usedUrls) {
                if (!att.matches(foundUrl)) {
                    throw new LaunchException("The resource from " + foundUrl + " does not match the  location in Application-Library-Allowable-Codebase Attribute " + att + ". Blocking the application from running.");
                } else {
                    LOG.debug("The resource from {} does  match the  location in Application-Library-Allowable-Codebase Attribute {}. Continuing.", foundUrl, att);
                }
            }
        }
        final boolean userApproved = isLowSecurity() || SecurityDialogs.showMatchingALACAttributePanel(file, documentBase, usedUrls);
        if (!userApproved) {
            throw new LaunchException("The application uses non-codebase resources, which do match its Application-Library-Allowable-Codebase Attribute, but was blocked from running by the user.");
        } else {
            LOG.debug("The application uses non-codebase resources, which do match its Application-Library-Allowable-Codebase Attribute, and was allowed to run by the user or user's security settings.");
        }
    }

    //package private for testing
    //not perfect but ok for use case
    static URL stripDocbase(URL documentBase) {
        String s = documentBase.toExternalForm();
        if (s.endsWith("/") || s.endsWith("\\")) {
            return documentBase;
        }
        int i1 = s.lastIndexOf("/");
        int i2 = s.lastIndexOf("\\");
        int i = Math.max(i1, i2);
        if (i <= 8 || i >= s.length()) {
            return documentBase;
        }
        s = s.substring(0, i+1);
        try {
            documentBase = new URL(s);
        } catch (MalformedURLException ex) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
        }
        return documentBase;
    }

    private String permissionsToString() {
        final String value = file.getManifestAttributesReader().getPermissions();
        if (value == null) {
            return "Not defined";
        } else if (value.trim().equalsIgnoreCase(AppletPermissionLevel.SANDBOX.getValue())) {
            return value.trim();
        } else if (value.trim().equalsIgnoreCase(AppletPermissionLevel.ALL.getValue())) {
            return value.trim();
        } else {
            return "illegal";
        }
    }

    private ManifestBoolean isSandboxForced() {
        final String permissionLevel = file.getManifestAttributesReader().getPermissions();
        if (permissionLevel == null) {
            return ManifestBoolean.UNDEFINED;
        } else if (permissionLevel.trim().equalsIgnoreCase(AppletPermissionLevel.SANDBOX.getValue())) {
            return ManifestBoolean.TRUE;
        } else if (permissionLevel.trim().equalsIgnoreCase(AppletPermissionLevel.ALL.getValue())) {
            return ManifestBoolean.FALSE;
        } else {
            throw new IllegalArgumentException(
                    String.format("Unknown value of %s attribute %s. Expected %s or %s",
                            ManifestAttributes.PERMISSIONS.toString(), permissionLevel,
                            AppletPermissionLevel.SANDBOX.getValue(), AppletPermissionLevel.ALL.getValue())
            );
        }
    }
}
