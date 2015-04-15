/* CertificatePane.java
 Copyright (C) 2015 Red Hat, Inc.

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
package net.sourceforge.jnlp.security;

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
import javax.net.ssl.KeyManagerFactory;
import javax.swing.JOptionPane;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.runtime.Translator;
import net.sourceforge.jnlp.util.logging.OutputController;

class KeystorePasswordAttempter {

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

    /**
     * This password can read any keystore. But if you save with him, integrity
     * of keystore will be lsot for ever.
     */
    static class AllmightyPassword extends SavedPassword {

        public AllmightyPassword() {
            super(null);
        }

    }

    static abstract class KeystoreOperation {

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

        public KeystoreOperation(KeyStore ks, String alias, Key key, Certificate[] certChain, File f) {
            this(null, ks, alias, key, certChain, f);
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
    //static final KeystorePasswordAttempter INSTANCE = new KeystorePasswordAttempter(new SavedPassword(getTrustedCertsPassword()), new AllmightyPassword());
    static final KeystorePasswordAttempter INSTANCE = new KeystorePasswordAttempter(new SavedPassword(getTrustedCertsPassword()));
    private final List<SavedPassword> passes;
    private final Map<KeyStore, SavedPassword> sucesfullPerKeystore = new HashMap<>();

    private KeystorePasswordAttempter(SavedPassword... initialPasswords) {
        passes = new ArrayList<>(initialPasswords.length);
        passes.addAll(Arrays.asList(initialPasswords));
    }

    Key unlockKeystore(KeystoreOperation operation) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, IOException, CertificateException {
        SavedPassword  sucessfullKey = sucesfullPerKeystore.get(operation.ks);
        Exception firstEx = null;
        String messages = "";
        List<SavedPassword>  localPases = new ArrayList<>();
        if (sucessfullKey != null){
            //sucessfull must be firts. If it is not, then writing to keystore by illegal password, will kill kesytore's integrity
            localPases.add(sucessfullKey);
        }
        localPases.addAll(passes);
        for (int i = 0; i < localPases.size(); i++) {
            SavedPassword pass = localPases.get(i);
            try {
                //we expect, that any keystore is loaded before readed.
                //so we are wrting by correct password
                //if no sucessfull passwrod was provided during rading, then finish(firstEx); will save us from overwrite
                Key result = operation.operateKeystore(pass.pass);
                //ok we were sucessfull
                //save the loading password for storing purposes (and another reading too)
                 sucesfullPerKeystore.put(operation.ks, pass);
                return result;
            } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | IOException | CertificateException ex) {
                if (firstEx == null) {
                    firstEx = ex;
                }
                messages += "'" + ex.getMessage() + "' ";
                OutputController.getLogger().log(ex);
                //tried all known, ask for new or finally die
                if (i + 1 == localPases.size()) {
                    String s1 = Translator.R("KSresultUntilNow", messages, operation.getId(), (i + 1));
                    OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, s1);
                    OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, Translator.R("KSinvalidPassword"));
                    if (JNLPRuntime.isHeadless()) {
                        OutputController.getLogger().log(Translator.R("KSheadlesWarning"));
                        finish(firstEx);
                    } else {
                        String s = JOptionPane.showInputDialog(s1 + "\n" + Translator.R("KSnwPassHelp"));
                        if (s == null) {
                            finish(firstEx);
                        }
                        //if input is null, exception is thrown from finish method
                        SavedPassword users = new SavedPassword(s.toCharArray());
                        passes.add(users);
                        localPases.add(users);
                    }
                    //user already read all messages, now show only last one
                    messages = "";
                }
            }
        }
        return null;
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
