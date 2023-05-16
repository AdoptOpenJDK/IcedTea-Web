/* CertificatePane.java
 Copyright (C) 2015 Red Hat, Inc.

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
package net.sourceforge.jnlp.security;

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.logging.OutputController;

import javax.net.ssl.KeyManagerFactory;
import javax.swing.JOptionPane;
import java.io.File;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class KeystorePasswordAttempter {

    private static final Logger LOG = LoggerFactory.getLogger(KeystorePasswordAttempter.class);

    private static final char[] DEFAULT_PASSWORD = "changeit".toCharArray();

    private static char[] getTrustedCertsPassword() {
        return DEFAULT_PASSWORD;
    }

    static class SavedPassword {

        private final char[] pass;

        public SavedPassword(char[] pass) {
            this.pass = pass;
        }
    }

    abstract static class KeystoreOperation {

        protected final KeyManagerFactory kmf;
        protected final KeyStore ks;
        protected final String alias;
        protected final Key key;
        protected final Certificate[] certChain;
        protected final File f;

        public KeystoreOperation(KeyStore ks, File f) {
            this(null, ks, null, null, null, f);
        }

        public KeystoreOperation(KeyStore ks, String alias, Key key, Certificate[] certChain) {
            this(null, ks, alias, key, certChain, null);
        }

        public KeystoreOperation(KeyManagerFactory kmf, KeyStore ks) {
            this(kmf, ks, null, null, null, null);
        }

        public KeystoreOperation(KeyManagerFactory kmf, KeyStore ks, String alias, Key key, Certificate[] certChain, File f) {
            this.kmf = kmf;
            this.ks = ks;
            this.alias = alias;
            this.key = key;
            this.certChain = certChain;
            this.f = f;
        }

        abstract Key operateKeystore(char[] pass) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, IOException, CertificateException;

        abstract String getId();

    }

    static final KeystorePasswordAttempter INSTANCE = new KeystorePasswordAttempter(new SavedPassword(getTrustedCertsPassword()));
    private final List<SavedPassword> passes;
    private final Map<String, SavedPassword> successfulPerKeystore = new HashMap<>();

    private KeystorePasswordAttempter(SavedPassword... initialPasswords) {
        passes = new ArrayList<>(initialPasswords.length);
        passes.addAll(Arrays.asList(initialPasswords));
    }

    Key unlockKeystore(KeystoreOperation operation) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, IOException, CertificateException {
        final String keyStorePath = operation.f != null ? operation.f.getCanonicalPath() : "Unknown";
        final SavedPassword successfulKey = successfulPerKeystore.get(keyStorePath);
        LOG.debug("unlockKeyStore: For file {}, found successful pass = {} ",   keyStorePath, successfulKey != null);

        Exception firstEx = null;
        String messages = "";
        final List<SavedPassword>  localPasses = new ArrayList<>();
        if (successfulKey != null) {
            //successful must be first. If it is not, then writing to keystore by illegal password, will kill keystore's integrity
            localPasses.add(successfulKey);
        }
        localPasses.addAll(passes);
        for (int i = 0; i < localPasses.size(); i++) {
            final SavedPassword pass = localPasses.get(i);
            try {
                LOG.debug("unlockKeyStore: Operating Keystore {}", keyStorePath);
                //we expect, that any keystore is loaded before read.
                //so we are writing by correct password
                //if no successful password was provided during reading, then finish(firstEx); will save us from overwrite
                final Key result = operation.operateKeystore(pass.pass);
                //ok we were successful
                //save the loading password for storing purposes (and another reading too)
                if (operation.f != null) {
                    LOG.debug("unlockKeyStore: Store successful pass for file {}",  keyStorePath);
                    successfulPerKeystore.put(keyStorePath, pass);
                }
                return result;
            } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | IOException | CertificateException ex) {
                if (firstEx == null) {
                    firstEx = ex;
                }
                messages += "\n'" + ex.getMessage() + "'";
                LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, ex);
                //tried all known, ask for new or finally die
                if (i + 1 == localPasses.size()) {
                    String s1 = Translator.R("KSresultUntilNow", messages, operation.getId(), keyStorePath, localPasses.size());
                    LOG.info(s1);
                    LOG.info("Invalid password For keystore {} ?", keyStorePath);
                    if (JNLPRuntime.isHeadless()) {
                        OutputController.getLogger().printOutLn(s1 + "\n" + Translator.R("KSheadlesWarning"));
                        final String s = OutputController.getLogger().readLine();
                        if (s == null || s.trim().isEmpty()) {
                            finish(firstEx);
                        } else {
                            addPnewPassword(s, localPasses);
                        }
                    } else {
                        final String s = JOptionPane.showInputDialog(null, s1 + "\n" + Translator.R("KSnwPassHelp"), Translator.R("KSTitle"), JOptionPane.OK_CANCEL_OPTION);
                        if (s == null) {
                            finish(firstEx);
                        } else {
                            addPnewPassword(s, localPasses);
                        }
                    }
                    //user already read all messages, now show only last one
                    messages = "";
                }
            }
        }
        return null;
    }

    private void addPnewPassword(String s, List<SavedPassword> localPases) {
        SavedPassword users = new SavedPassword(s.toCharArray());
        passes.add(users);
        localPases.add(users);
    }

    private void finish(Exception ex) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, IOException, CertificateException {
        if (ex instanceof KeyStoreException) {
            throw (KeyStoreException) ex;
        } else if (ex instanceof NoSuchAlgorithmException) {
            throw (NoSuchAlgorithmException) ex;
        } else if (ex instanceof UnrecoverableKeyException) {
            throw (UnrecoverableKeyException) ex;
        } else if (ex instanceof IOException) {
            throw (IOException) ex;
        } else if (ex instanceof CertificateException) {
            throw (CertificateException) ex;
        } else {
            throw new RuntimeException("Unexpected exception", ex);
        }
    }
}
