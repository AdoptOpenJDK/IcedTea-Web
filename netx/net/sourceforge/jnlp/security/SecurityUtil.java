/* SecurityUtil.java
   Copyright (C) 2008 Red Hat, Inc.

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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.KeyManagerFactory;
import javax.swing.JOptionPane;
import jdk.nashorn.internal.runtime.StoredScript;
import net.sourceforge.jnlp.runtime.JNLPRuntime;

import net.sourceforge.jnlp.security.KeyStores.Level;
import net.sourceforge.jnlp.security.KeyStores.Type;
import net.sourceforge.jnlp.util.logging.OutputController;

public class SecurityUtil {

    private static final char[] DEFAULT_PASSWORD = "changeit".toCharArray();

    public static String getTrustedCertsFilename() throws Exception {
        return KeyStores.getKeyStoreLocation(Level.USER, Type.CERTS).getFullPath();
    }

    private static char[] getTrustedCertsPassword() {
        return DEFAULT_PASSWORD;
    }

    /**
     * Extracts the CN field from a Certificate principal string. Or, if it
     * can't find that, return the principal unmodified.
     *
     * This is a simple (and hence 'wrong') version. See
     * http://www.ietf.org/rfc/rfc2253.txt for all the gory details.
     */
    public static String getCN(String principal) {

        /*
         * FIXME Incomplete
         *
         * This does not implement RFC 2253 completely
         *
         * Issues:
         * - rfc2253 talks about utf8, java uses utf16.
         * - theoretically, java should have dealt with all byte encodings
         *   so we shouldnt even see cases like \FF
         * - if the above is wrong, then we need to deal with cases like
         *   \FF\FF
         */

        int start = principal.indexOf("CN=");
        if (start == -1) {
            return principal;
        }

        StringBuilder commonName = new StringBuilder();

        boolean inQuotes = false;
        boolean escaped = false;

        /*
         * bit 0 = high order bit. bit 1 = low order bit
         */
        char[] hexBits = null;

        for (int i = start + 3; i < principal.length(); i++) {
            char ch = principal.charAt(i);
            switch (ch) {
                case '"':
                    if (escaped) {
                        commonName.append(ch);
                        escaped = false;
                    } else {
                        inQuotes = !inQuotes;
                    }
                    break;

                case '\\':
                    if (escaped) {
                        commonName.append(ch);
                        escaped = false;
                    } else {
                        escaped = true;
                    }
                    break;

                case ',':
                    /* fall through */
                case ';':
                    /* fall through */
                case '+':
                    if (escaped || inQuotes) {
                        commonName.append(ch);
                        if (escaped) {
                            escaped = false;
                        }
                    } else {
                        return commonName.toString();
                    }
                    break;

                default:
                    if (escaped && isHexDigit(ch)) {
                        hexBits = new char[2];
                        hexBits[0] = ch;
                    } else if (hexBits != null) {
                        if (!isHexDigit(ch)) {
                            /* error parsing */
                            return "";
                        }
                        hexBits[1] = ch;
                        commonName.append((char) Integer.parseInt(new String(hexBits), 16));
                        hexBits = null;
                    } else {
                        commonName.append(ch);
                    }
                    escaped = false;
            }
        }

        return commonName.toString();

    }

    private static boolean isHexDigit(char ch) {
        return ((ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'F') || (ch >= 'a' && ch <= 'f'));
    }

    /**
     * Checks the user's home directory to see if the trusted.certs file exists.
     * If it does not exist, it tries to create an empty keystore.
     * @return true if the trusted.certs file exists or a new trusted.certs
     * was created successfully, otherwise false.
     */
    public static boolean checkTrustedCertsFile() throws Exception {

        File certFile = new File(getTrustedCertsFilename());

        //file does not exist
        if (!certFile.isFile()) {
            File dir = certFile.getAbsoluteFile().getParentFile();
            boolean madeDir = false;
            if (!dir.isDirectory()) {
                madeDir = dir.mkdirs();
            }

            //made directory, or directory exists
            if (madeDir || dir.isDirectory()) {
                KeyStore ks = KeyStore.getInstance("JKS");
                loadKeyStore(ks, null);
                storeKeyStore(ks, certFile);
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    /**
     * Returns the keystore associated with the user's trusted.certs file,
     * or null otherwise.
     */
    public static KeyStore getUserKeyStore() throws Exception {

        KeyStore ks = null;
        FileInputStream fis = null;

        if (checkTrustedCertsFile()) {

            try {
                File file = new File(getTrustedCertsFilename());
                if (file.exists()) {
                    ks = KeyStore.getInstance("JKS");
                    loadKeyStore(ks, file);
                }
            } catch (Exception e) {
                OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
                throw e;
            } finally {
                if (fis != null)
                    fis.close();
            }
        }
        return ks;
    }

    /**
     * Returns the keystore associated with the JDK cacerts file,
         * or null otherwise.
     */
    public static KeyStore getCacertsKeyStore() throws Exception {

        KeyStore caks = null;
        FileInputStream fis = null;

        try {
            File file = new File(System.getProperty("java.home")
                                + "/lib/security/cacerts");
            if (file.exists()) {
                fis = new FileInputStream(file);
                caks = KeyStore.getInstance("JKS");
                caks.load(fis, null);
            }
        } catch (Exception e) {
            caks = null;
        } finally {
            if (fis != null)
                fis.close();
        }

        return caks;
    }

    /**
     * Returns the keystore associated with the system certs file,
     * or null otherwise.
     */
    public static KeyStore getSystemCertStore() throws Exception {

        KeyStore caks = null;
        FileInputStream fis = null;

        try {
            File file = new File(System.getProperty("javax.net.ssl.trustStore"));
            String type = System.getProperty("javax.net.ssl.trustStoreType");
            //String provider = "SUN";
            char[] password = System.getProperty(
                                "javax.net.ssl.trustStorePassword").toCharArray();
            if (file.exists()) {
                fis = new FileInputStream(file);
                caks = KeyStore.getInstance(type);
                caks.load(fis, password);
            }
        } catch (Exception e) {
            caks = null;
        } finally {
            if (fis != null)
                fis.close();
        }

        return caks;
    }
    
    
    public static void initKeyManagerFactory(KeyManagerFactory kmf, KeyStore ks) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        try {
            KeystorePasswordAttempter.INSTANCE.unlockKeystore(KeystorePasswordAttempter.UNLOCK_TYPE.initKeyManagerFactory, kmf, ks, null, null, null, null);
        } catch (IOException | CertificateException ex) {
            throw unexpectedException(ex);
        }
    }

    public static void setKeyEntry(KeyStore ks, String alias, Key key, Certificate[] certChain) throws KeyStoreException {
        try {
            KeystorePasswordAttempter.INSTANCE.unlockKeystore(KeystorePasswordAttempter.UNLOCK_TYPE.setKeyEntry, null, ks, alias, key, certChain, null);
        } catch (NoSuchAlgorithmException | UnrecoverableKeyException | IOException | CertificateException ex) {
            throw unexpectedException(ex);
        }
    }

    public static Key getKey(KeyStore ks, String alias) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        try {
            return KeystorePasswordAttempter.INSTANCE.unlockKeystore(KeystorePasswordAttempter.UNLOCK_TYPE.getKey, null, ks, alias, null, null, null);
        } catch (IOException | CertificateException ex) {
            throw unexpectedException(ex);
        }
    }

    public static void loadKeyStore(KeyStore ks, File f) throws IOException, NoSuchAlgorithmException, CertificateException {
        try {
            KeystorePasswordAttempter.INSTANCE.unlockKeystore(KeystorePasswordAttempter.UNLOCK_TYPE.loadKeyStore, null, ks, null, null, null, f);
        } catch (KeyStoreException | UnrecoverableKeyException ex) {
            throw unexpectedException(ex);
        }

    }

    public static void storeKeyStore(KeyStore ks, File f) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        try {
            KeystorePasswordAttempter.INSTANCE.unlockKeystore(KeystorePasswordAttempter.UNLOCK_TYPE.storeKeyStore, null, ks, null, null, null,  f);
        } catch (UnrecoverableKeyException ex) {
            throw unexpectedException(ex);
        }
    }

    private static RuntimeException unexpectedException(Exception ex) {
        return new RuntimeException("This usage of KeystorePasswordAttempter shopuld not throw this kind of exception", ex);
    }

    private static class KeystorePasswordAttempter {

        private static enum UNLOCK_TYPE {

            initKeyManagerFactory, setKeyEntry, getKey, loadKeyStore, storeKeyStore;
        }

        private static class SavedPassword {

            private final char[] pass;

            public SavedPassword(char[] pass) {
                this.pass = pass;
            }
        }

        private static final KeystorePasswordAttempter INSTANCE = new KeystorePasswordAttempter(getTrustedCertsPassword());
        private final List<SavedPassword> passes;

        private KeystorePasswordAttempter(char[]  
            ... initialPasswords) {
            passes = new ArrayList<>(initialPasswords.length);
            for (char[] pass : initialPasswords) {
                passes.add(new SavedPassword(pass));
            }
        }

        private Key unlockKeystore(UNLOCK_TYPE type, KeyManagerFactory kmf, KeyStore ks, String alias, Key key, Certificate[] certChain, File f) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, IOException, CertificateException {
            Exception firstEx = null;
            String messages = "";
            for (int i = 0; i < passes.size(); i++) {
                SavedPassword pass = passes.get(i);
                try {
                    switch (type) {
                        case initKeyManagerFactory:
                            kmf.init(ks, pass.pass);
                            return null;
                        case setKeyEntry:
                            ks.setKeyEntry(alias, key, pass.pass, certChain);
                            return null;
                        case getKey:
                            return ks.getKey(alias, pass.pass);
                        case loadKeyStore:
                            if (f == null) {
                                ks.load(null, pass.pass);
                            } else {
                                try (FileInputStream fis = new FileInputStream(f)) {
                                    ks.load(fis, pass.pass);
                                }
                            }
                            return null;
                        case storeKeyStore:
                            if (f == null) {
                                ks.store(null, pass.pass);
                            } else {
                                try (FileOutputStream fos = new FileOutputStream(f)) {
                                    ks.store(fos, pass.pass);
                                }
                            }
                            return null;
                        default:
                            throw new IllegalArgumentException("Unknow attempt during unlocking keystore");
                    }
                } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | IOException | CertificateException ex) {
                    if (firstEx == null) {
                        firstEx = ex;
                    }
                    messages += "'" + ex.getMessage() + "' ";
                    OutputController.getLogger().log(ex);
                    //tried all known, ask for new or finally die
                    if (i + 1 == passes.size()) {
                        String s1 = "Got " + messages + " during keystore operation " + type.toString() + ". Attempt " + (i + 1);
                        OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, s1);
                        OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, "Invalid password?");
                        if (JNLPRuntime.isHeadless()) {
                            OutputController.getLogger().log("Headless mode currently dont support runtime-passwords");
                            finish(firstEx);
                        } else {
                            String s = JOptionPane.showInputDialog(s1 + "\n" + "Type new password and press ok. Give up by pressing anything else.");
                            if (s == null) {
                                finish(firstEx);
                            }
                            passes.add(new SavedPassword(s.toCharArray()));

                        }
                        //user already read all messages, now sho only last one
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

}
