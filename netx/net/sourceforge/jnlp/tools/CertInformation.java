/* CertInformation.java
   Copyright (C) 2012 Red Hat, Inc.

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

package net.sourceforge.jnlp.tools;

import static net.sourceforge.jnlp.runtime.Translator.R;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.logging.OutputController;

/**
 * Maintains information about a CertPath that has signed at least one of the
 * entries provided by a jar of the app.
 */
public class CertInformation {

    private boolean hasExpiredCert = false;
    private boolean hasExpiringCert = false;

    private boolean isNotYetValidCert = false;

    /* Code signer properties of the certificate. */
    private boolean hasBadKeyUsage = false;
    private boolean hasBadExtendedKeyUsage = false;
    private boolean hasBadNetscapeCertType = false;

    private boolean alreadyTrustPublisher = false;
    private boolean rootInCacerts = false;

    static enum Detail {
        TRUSTED (R("STrustedCertificate")),
        UNTRUSTED (R("SUntrustedCertificate")),
        RUN_WITHOUT_RESTRICTIONS(R("SRunWithoutRestrictions")),
        EXPIRED (R("SHasExpiredCert")),
        EXPIRING (R("SHasExpiringCert")),
        NOT_YET_VALID (R("SNotYetValidCert")),
        BAD_KEY_USAGE (R("SBadKeyUsage")),
        BAT_EXTENDED_KEY_USAGE (R("SBadExtendedKeyUsage")),
        BAD_NETSCAPE_CERT_TYPE (R("SBadNetscapeCertType"));

        private final String message;
        Detail(String issue) {
            message = issue;
        }

        public String message() {
            return message;
        }
    }

    private EnumSet<Detail> details = EnumSet.noneOf(Detail.class);

    /** The jars and their number of entries this cert has signed. */
    private HashMap<String, Integer> signedJars = new HashMap<String, Integer>();

    /**
     * Return if there are signing issues with this certificate.
     * @return {@code true} if there are any issues with expiry, validity or bad key usage.
     */
    public boolean hasSigningIssues() {
        return hasExpiredCert || isNotYetValidCert || hasBadKeyUsage
                || hasBadExtendedKeyUsage || hasBadNetscapeCertType;
    }

    /**
     * Return whether or not the publisher is already trusted.
     *
     * @return {@code true} if the publisher is trusted already.
     */
    public boolean isPublisherAlreadyTrusted() {
        return alreadyTrustPublisher;
    }

    /**
     * Set whether or not the publisher is already trusted.
     */
    public void setAlreadyTrustPublisher() {
        alreadyTrustPublisher = true;
    }

    /**
     * Return whether or not the root is in the list of trusted CA certificates.
     *
     * @return {@code true} if the root is in the list of CA certificates.
     */
    public boolean isRootInCacerts() {
        return rootInCacerts;
    }

    /**
     * Set that this cert's root CA is to be trusted.
     */
    public void setRootInCacerts() {
        rootInCacerts = true;
        details.add(Detail.TRUSTED);
    }

    /**
     * Resets any trust of the root and publisher. Also removes unnecessary
     * details from the list of issues.
     */
    public void resetForReverification() {
        alreadyTrustPublisher = false;
        rootInCacerts = false;
        removeFromDetails(Detail.UNTRUSTED);
        removeFromDetails(Detail.TRUSTED);
    }
    /**
     * Check if this cert is the signer of a jar.
     * @param jarName The absolute path of the jar this certificate has signed.
     * @return {@code true} if this cert has signed the jar found at {@code jarName}.
     */
    public boolean isSignerOfJar(String jarName) {
        return signedJars.containsKey(jarName);
    }

    /**
     * Add a jar to the list of jars this certificate has signed along with the
     * number of entries it has signed in the jar.
     *
     * @param jarName The absolute path of the jar this certificate has signed.
     * @param signedEntriesCount The number of entries this cert has signed in {@code jarName}.
     */
    public void setNumJarEntriesSigned(String jarName, int signedEntriesCount) {
        if (signedJars.containsKey(jarName)) {
            OutputController.getLogger().log(OutputController.Level.ERROR_DEBUG, "WARNING: A jar that has already been "
                        + "verified is being yet again verified: " + jarName);
        } else {
            signedJars.put(jarName, signedEntriesCount);
        }
    }

    /**
     * Find the number of entries this cert has signed in the specified jar.
     * @param jarName The absolute path of the jar this certificate has signed.
     * @return The number of entries this cert has signed in {@code jarName}.
     */
    public int getNumJarEntriesSigned(String jarName) {
        return signedJars.get(jarName);
    }

    /**
     * Get all the jars this cert has signed along with the number of entries
     * in each jar.
     * @return a {link Map} of jars and their number of entries this cert has signed
     */
    public Map<String, Integer> getSignedJars() {
        return signedJars;
    }

    /**
     * Get the details regarding issue(s) with this certificate.
     *
     * @return A list of all the details/issues with this app.
     */
    public List<String> getDetailsAsStrings() {
        List<String> detailsToStr = new ArrayList<String>();
        for (Detail issue : details) {
            detailsToStr.add(issue.message());
        }
        return detailsToStr;
    }

    /**
     * Remove an issue from the list of details of issues with this certificate.
     * List is unchanged if detail was not present.
     *
     * @param detail The issue to be removed regarding this certificate.
     */
    private void removeFromDetails(Detail detail) {
        details.remove(detail);
    }

    /**
     * Set that this cert is expired and add this issue to the list of details.
     */
    public void setHasExpiredCert() {
        hasExpiredCert = true;
        details.add(Detail.RUN_WITHOUT_RESTRICTIONS);
        details.add(Detail.EXPIRED);
    }

    /**
     * Set that this cert is expiring within 6 months and add this issue to
     * the list of details.
     */
    public void setHasExpiringCert() {
        hasExpiringCert = true;
        details.add(Detail.RUN_WITHOUT_RESTRICTIONS);
        details.add(Detail.EXPIRING);
    }

    /**
     * Get whether or not this cert will expire within 6 months.
     * @return {@code true} if the cert will be expired after 6 months.
     */
    public boolean hasExpiringCert() {
        return hasExpiringCert;
    }

    /**
     * Set that this cert is not yet valid
     * and add this issue to the list of details.
     */
    public void setNotYetValidCert() {
        isNotYetValidCert = true;
        details.add(Detail.RUN_WITHOUT_RESTRICTIONS);
        details.add(Detail.NOT_YET_VALID);
    }

    /**
     * Set that this cert has bad key usage
     * and add this issue to the list of details.
     */
    public void setBadKeyUsage() {
        hasBadKeyUsage = true;
        details.add(Detail.RUN_WITHOUT_RESTRICTIONS);
        details.add(Detail.BAD_KEY_USAGE);
    }

    /**
     * Set that this cert has bad extended key usage
     * and add this issue to the list of details.
     */
    public void setBadExtendedKeyUsage() {
        hasBadExtendedKeyUsage = true;
        details.add(Detail.RUN_WITHOUT_RESTRICTIONS);
        details.add(Detail.BAT_EXTENDED_KEY_USAGE);
    }

    /**
     * Set that this cert has a bad netscape cert type
     * and add this issue to the list of details.
     */
    public void setBadNetscapeCertType() {
        hasBadNetscapeCertType = true;
        details.add(Detail.RUN_WITHOUT_RESTRICTIONS);
        details.add(Detail.BAD_NETSCAPE_CERT_TYPE);
    }

    /**
     * Set that this cert and all of its CAs are untrusted so far.
     */
    public void setUntrusted() {
        details.add(Detail.UNTRUSTED);
    }
}
