/*
Copyright (C) 2011 Red Hat, Inc.
Copyright (C) 2019 Karakun AG

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify it under the
terms of the GNU General Public License as published by the Free Software
Foundation, version 2.

IcedTea is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
IcedTea; see the file COPYING. If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is making a
combined work based on this library. Thus, the terms and conditions of the GNU
General Public License cover the whole combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent modules, and
to copy and distribute the resulting executable under terms of your choice,
provided that you also meet, for each linked independent module, the terms and
conditions of the license of that module. An independent module is a module
which is not derived from or based on this library. If you modify this library,
you may extend this exception to your version of the library, but you are not
obligated to do so. If you do not wish to do so, delete this exception
statement from your version.
*/
package net.adoptopenjdk.icedteaweb.manifest;

import net.adoptopenjdk.icedteaweb.client.parts.dialogs.Dialogs;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity.AppletSecurityLevel;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.appletextendedsecurity.AppletStartupSecuritySettings;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.ExtensionDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.ResourcesDesc;
import net.adoptopenjdk.icedteaweb.jnlp.element.security.ApplicationEnvironment;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.runtime.ApplicationInstance;
import net.sourceforge.jnlp.runtime.ApplicationManager;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.ClasspathMatcher.ClasspathMatchers;
import net.sourceforge.jnlp.util.UrlUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static net.adoptopenjdk.icedteaweb.jnlp.element.security.ApplicationEnvironment.ALL;
import static net.adoptopenjdk.icedteaweb.jnlp.element.security.ApplicationEnvironment.J2EE;
import static net.adoptopenjdk.icedteaweb.jnlp.element.security.ApplicationEnvironment.SANDBOX;
import static net.sourceforge.jnlp.util.UrlUtils.FILE_PROTOCOL;

public class ManifestAttributesChecker {

    private static final Logger LOG = LoggerFactory.getLogger(ManifestAttributesChecker.class);

    private final JNLPFile file;
    private final boolean isFullySigned;
    private final ManifestAttributesReader reader;

    public ManifestAttributesChecker(final JNLPFile file, boolean isFullySigned, ManifestAttributesReader reader) {
        this.file = file;
        this.isFullySigned = isFullySigned;
        this.reader = reader;
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
        final List<String> configs = JNLPRuntime.getConfiguration().getPropertyAsList(ConfigurationConstants.KEY_ENABLE_MANIFEST_ATTRIBUTES_CHECK);
        List<MANIFEST_ATTRIBUTES_CHECK> manifestAttributesCheckList = new ArrayList<>();
        for (String attribute : configs) {
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
        if (!isFullySigned) {
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
        final String[] eps = reader.getEntryPoints();
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
        throw new LaunchException("None of the entry points specified: '" + reader.getEntryPoint() + "' matched the main class " + mainClass + " and applet is signed. This is a security error and the app will not be launched.");
    }

    /**
     * http://docs.oracle.com/javase/7/docs/technotes/guides/jweb/security/manifest.html#trusted_only
     */
    private void checkTrustedOnlyAttribute() throws LaunchException {
        final ManifestBoolean trustedOnly = reader.isTrustedOnly();
        if (trustedOnly == ManifestBoolean.UNDEFINED) {
            LOG.debug("Trusted Only manifest attribute not found. Continuing.");
            return;
        }

        if (trustedOnly == ManifestBoolean.FALSE) {
            LOG.debug("Trusted Only manifest attribute is false. Continuing.");
            return;
        }

        //final Object desc = security.getSecurityType();
        final ApplicationEnvironment applicationEnvironment = getApplicationEnvironment();

        final String securityType;
        if (applicationEnvironment == null) {
            securityType = "Not Specified";
        } else if (applicationEnvironment == ALL) {
            securityType = "All-Permission";
        } else if (applicationEnvironment == SANDBOX) {
            securityType = "Sandbox";
        } else if (applicationEnvironment == J2EE) {
            securityType = "J2EE";
        } else {
            securityType = "Unknown";
        }

        final boolean requestsSpecialPermission = applicationEnvironment == ALL || applicationEnvironment == J2EE;
        final String signedMsg;
        if (isFullySigned) {
            signedMsg = "The application is fully signed";
        } else {
            signedMsg = "The application is not fully signed";
        }
        LOG.debug("Trusted Only manifest attribute is \"true\". {} and requests permission level: {}", signedMsg, securityType);
        if (requestsSpecialPermission && !isFullySigned) {
            throw new LaunchException("This application specifies Trusted-only as True in its Manifest. " + signedMsg + " and requests permission level: " + securityType + ". This is not allowed.");
        }
    }

    /**
     * http://docs.oracle.com/javase/7/docs/technotes/guides/jweb/manifest.html#codebase
     */
    private void checkCodebaseAttribute() {
        if (file.getCodeBase() == null || file.getCodeBase().getProtocol().equals(FILE_PROTOCOL)) {
            LOG.warn("The application is a local file. Codebase validation is disabled. See: http://docs.oracle.com/javase/7/docs/technotes/guides/jweb/security/no_redeploy.html for details.");
            return;
        }
        final ApplicationEnvironment applicationEnvironment = getApplicationEnvironment();
        final URL codebase = UrlUtils.guessCodeBase(file);
        final ClasspathMatchers codebaseAtt = reader.getCodebase();
        if (codebaseAtt == null) {
            LOG.warn("This application does not specify a Codebase in its manifest. Please verify with the applet''s vendor. Continuing. See: http://docs.oracle.com/javase/7/docs/technotes/guides/jweb/security/no_redeploy.html for details.");
            return;
        }
        if (applicationEnvironment == SANDBOX) {
            if (codebaseAtt.matches(codebase)) {
                LOG.info("Codebase matches codebase manifest attribute, but application is unsigned. Continuing. See: http://docs.oracle.com/javase/7/docs/technotes/guides/jweb/security/no_redeploy.html for details.");
            } else {
                LOG.error("The application''s codebase does NOT match the codebase specified in its manifest, but the application is unsigned. Continuing. See: http://docs.oracle.com/javase/7/docs/technotes/guides/jweb/security/no_redeploy.html for details.");
            }
        } else {
            if (codebaseAtt.matches(codebase)) {
                LOG.info("Codebase matches codebase manifest attribute, and application is signed. Continuing. See: http://docs.oracle.com/javase/7/docs/technotes/guides/jweb/security/no_redeploy.html for details.");
            } else {
                LOG.error("Application Codebase does NOT match the Codebase specified in the application''s manifest, and this application is signed. You are strongly discouraged from running this application. See: http://docs.oracle.com/javase/7/docs/technotes/guides/jweb/security/no_redeploy.html for details.");
            }
        }

    }

    /**
     * http://docs.oracle.com/javase/7/docs/technotes/guides/jweb/security/manifest.html#permissions
     */
    private void checkPermissionsAttribute() throws LaunchException {
        if (getApplicationEnvironment() == SANDBOX) {
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
                final boolean userApproved = Dialogs.showMissingPermissionsAttributeDialogue(file);
                if (!userApproved) {
                    throw new LaunchException("Your Extended applets security is at 'high' and this application is missing the 'permissions' attribute in manifest. And you have refused to run it.");
                } else {
                    LOG.debug("Your Extended applets security is at 'high' and this application is missing the 'permissions' attribute in manifest. And you have allowed to run it.");
                }
            }
            return;
        }

        final ApplicationEnvironment requestedEnvironment = file.getApplicationEnvironment();
        validateRequestedEnvironmentMatchesManifestPermissions(requestedEnvironment, sandboxForced);
        if (requestedEnvironment == SANDBOX) {
            if (sandboxForced == ManifestBoolean.TRUE && isFullySigned) {
                LOG.warn("The 'permissions' attribute is '{}' and the applet is signed. Forcing sandbox.", permissionsToString());
                getApplicationInstance().setApplicationEnvironment(SANDBOX);
            }
            if (sandboxForced == ManifestBoolean.FALSE && !isFullySigned) {
                LOG.warn("The 'permissions' attribute is '{}' and the applet is unsigned. Forcing sandbox.", permissionsToString());
                getApplicationInstance().setApplicationEnvironment(SANDBOX);
            }
        }
    }

    private static boolean isLowSecurity() {
        return AppletStartupSecuritySettings.getInstance().getSecurityLevel() == AppletSecurityLevel.ALLOW_UNSIGNED;
    }

    private void validateRequestedEnvironmentMatchesManifestPermissions(final ApplicationEnvironment requested, final ManifestBoolean sandboxForced) throws LaunchException {

        if ((requested != SANDBOX && sandboxForced != ManifestBoolean.FALSE) ||
                (requested == SANDBOX && sandboxForced != ManifestBoolean.TRUE)) {
            throw new LaunchException(
                    String.format("The 'permissions' attribute in %s is '%s' but the application requested %s. This is fatal.", reader.getJarName(), permissionsToString(), requested));
        }
    }

    private void checkApplicationLibraryAllowableCodebaseAttribute() throws LaunchException {
        //conditions
        final URL codebase = file.getCodeBase();

        //cases
        final Map<URL, Set<URL>> usedUrls = new HashMap<>();
        final URL sourceLocation = file.getSourceLocation();
        final List<ResourcesDesc> resourcesDescs = file.getResourcesDescs();
        if (sourceLocation != null) {
            final URL urlWithoutFileName = UrlUtils.removeFileName(sourceLocation);
            usedUrls.computeIfAbsent(urlWithoutFileName, url -> new HashSet<>()).add(sourceLocation);
        }
        for (ResourcesDesc resourcesDesc : resourcesDescs) {
            ExtensionDesc[] ex = resourcesDesc.getExtensions();
            if (ex != null) {
                for (ExtensionDesc extensionDesc : ex) {
                    if (extensionDesc != null) {
                        final URL urlWithoutFileName = UrlUtils.removeFileName(extensionDesc.getLocation());
                        usedUrls.computeIfAbsent(urlWithoutFileName, url -> new HashSet<>()).add(extensionDesc.getLocation());
                    }
                }
            }
            JARDesc[] jars = resourcesDesc.getJARs();
            if (jars != null) {
                for (JARDesc jarDesc : jars) {
                    if (jarDesc != null) {
                        final URL urlWithoutFileName = UrlUtils.removeFileName(jarDesc.getLocation());
                        usedUrls.computeIfAbsent(urlWithoutFileName, url -> new HashSet<>()).add(jarDesc.getLocation());
                    }
                }
            }
            JNLPFile jnlp = resourcesDesc.getJNLPFile();
            if (jnlp != null) {
                final URL urlWithoutFileName = UrlUtils.removeFileName(jnlp.getSourceLocation());
                usedUrls.computeIfAbsent(urlWithoutFileName, url -> new HashSet<>()).add(jnlp.getSourceLocation());
            }
        }

        if (usedUrls.isEmpty()) {
            //I hope this is the case, when the resources is/are
            //only codebase classes. Then it should be safe to return.
            LOG.debug("The application is not using any url resources, skipping Application-Library-Allowable-Codebase Attribute check.");
            return;
        }
        final Set<URL> notOkUrls = new HashSet<>();
        final boolean skipResourcesFromFileSystem = Boolean.parseBoolean(JNLPRuntime.getConfiguration().getProperty(ConfigurationConstants.KEY_ASSUME_FILE_STEM_IN_CODEBASE));
        for (URL u : usedUrls.keySet()) {
            if (UrlUtils.urlRelativeTo(u, codebase)) {
                LOG.debug("OK - '{}' is from codebase '{}'.", u, codebase);
            } else if (skipResourcesFromFileSystem && FILE_PROTOCOL.equals(u.getProtocol())) {
                LOG.debug("OK - '{}' is from file system", u);
            } else {
                notOkUrls.add(u);
                LOG.warn("Warning! '{}' is NOT from codebase '{}'.", u, codebase);
            }
        }
        if (notOkUrls.isEmpty()) {
            //all resources are from codebase or document base. it is ok to proceed.
            LOG.debug("All applications resources are from codebase {}, skipping Application-Library-Allowable-Codebase Attribute check.", codebase);
            return;
        }

        final ClasspathMatchers att;
        if (isFullySigned) {
            // we only consider values in manifest for signed apps (as they may be faked)
            att = reader.getApplicationLibraryAllowableCodebase();
        } else {
            att = null;
        }

        final Set<URL> notOkResources = notOkUrls.stream()
                .flatMap(notOk -> usedUrls.get(notOk).stream())
                .collect(Collectors.toSet());

        notOkResources.forEach(url -> LOG.warn("The resource '{}' is not from codebase '{}'", url, codebase));

        if (att == null) {
            final boolean userApproved = Dialogs.showMissingALACAttributePanel(file, codebase, notOkResources);
            if (!userApproved) {
                throw new LaunchException("The application uses non-codebase resources, has no Application-Library-Allowable-Codebase Attribute, and was blocked from running by the user");
            } else {
                LOG.debug("The application uses non-codebase resources, has no Application-Library-Allowable-Codebase Attribute, and was allowed to run by the user or user's security settings");
                return;
            }
        } else {
            for (URL foundUrl : usedUrls.keySet()) {
                if (!att.matches(foundUrl)) {
                    throw new LaunchException("The resources " + usedUrls.get(foundUrl) + " do not match the location in Application-Library-Allowable-Codebase Attribute " + att + ". Blocking the application from running.");
                } else {
                    LOG.debug("The resources from {} do  match the location in Application-Library-Allowable-Codebase Attribute {}. Continuing.", foundUrl, att);
                }
            }
        }

        final boolean userApproved = isLowSecurity() || Dialogs.showMatchingALACAttributePanel(file, codebase, notOkResources);
        if (!userApproved) {
            throw new LaunchException("The application uses non-codebase resources, which do match its Application-Library-Allowable-Codebase Attribute, but was blocked from running by the user.");
        } else {
            LOG.debug("The application uses non-codebase resources, which do match its Application-Library-Allowable-Codebase Attribute, and was allowed to run by the user or user's security settings.");
        }
    }

    private String permissionsToString() {
        final String value = reader.getPermissions();
        if (value == null) {
            return "Not defined";
        } else if (value.trim().equalsIgnoreCase("sandbox")) {
            return value.trim();
        } else if (value.trim().equalsIgnoreCase("all-permissions")) {
            return value.trim();
        } else {
            return "illegal";
        }
    }

    private ManifestBoolean isSandboxForced() {
        final String permissionLevel = reader.getPermissions();
        if (permissionLevel == null) {
            return ManifestBoolean.UNDEFINED;
        } else if (permissionLevel.trim().equalsIgnoreCase("sandbox")) {
            return ManifestBoolean.TRUE;
        } else if (permissionLevel.trim().equalsIgnoreCase("all-permissions")) {
            return ManifestBoolean.FALSE;
        } else {
            throw new IllegalArgumentException(
                    String.format("Unknown value of %s attribute %s. Expected %s or %s",
                            ManifestAttributes.PERMISSIONS.toString(), permissionLevel,
                            "sandbox", "all-permissions")
            );
        }
    }

    private ApplicationEnvironment getApplicationEnvironment() {
        return getApplicationInstance().getApplicationEnvironment();
    }

    private ApplicationInstance getApplicationInstance() {
        return ApplicationManager.getApplication(file).orElseThrow(() -> new IllegalStateException("could not load application instance for jnlp"));
    }
}
