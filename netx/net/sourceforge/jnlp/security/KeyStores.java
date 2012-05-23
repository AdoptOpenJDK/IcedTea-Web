/* KeyStores.java
   Copyright (C) 2010 Red Hat, Inc.

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
import java.security.AllPermission;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.Translator;
import net.sourceforge.jnlp.util.FileUtils;

/**
 * The <code>KeyStores</code> class allows easily accessing the various KeyStores
 * used.
 */
public final class KeyStores {

    /* this gets turned into user-readable strings, see toUserReadableString */

    public enum Level {
        USER,
        SYSTEM,
    }

    public enum Type {
        CERTS,
        JSSE_CERTS,
        CA_CERTS,
        JSSE_CA_CERTS,
        CLIENT_CERTS,
    }

    public static final Map<Integer,String> keystoresPaths=new HashMap<Integer, String>();

    private static DeploymentConfiguration config = null;

    private static final String KEYSTORE_TYPE = "JKS";
    /** the default password used to protect the KeyStores */
    private static final String DEFAULT_PASSWORD = "changeit";

    public static final char[] getPassword() {
        return DEFAULT_PASSWORD.toCharArray();
    }

    /** Set the configuration object to use for getting KeyStore paths */
    public static void setConfiguration(DeploymentConfiguration newConfig) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new AllPermission());
        }

        config = newConfig;
    }

    /**
     * Returns a KeyStore corresponding to the appropriate level level (user or
     * system) and type.
     *
     * @param level whether the KeyStore desired is a user-level or system-level
     * KeyStore
     * @param type the type of KeyStore desired
     * @return a KeyStore containing certificates from the appropriate
     */
    public static final KeyStore getKeyStore(Level level, Type type) {
        boolean create = false;
        if (level == Level.USER) {
            create = true;
        } else {
            create = false;
        }
        return getKeyStore(level, type, create);
    }

    /**
     * Returns a KeyStore corresponding to the appropriate level level (user or
     * system) and type.
     *
     * @param level whether the KeyStore desired is a user-level or system-level
     * KeyStore
     * @param type the type of KeyStore desired
     * @return a KeyStore containing certificates from the appropriate
     */
    public static final KeyStore getKeyStore(Level level, Type type, boolean create) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new AllPermission());
        }

        String location = getKeyStoreLocation(level, type);
        KeyStore ks = null;
        try {
            ks = createKeyStoreFromFile(new File(location), create, DEFAULT_PASSWORD);
            //hashcode is used instead of instance so when no references are left
            //to keystore, then this will not be blocker for garbage collection
            keystoresPaths.put(ks.hashCode(),location);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ks;
    }

    public static String getPathToKeystore(int k) {
        String s = keystoresPaths.get(k);
        if (s == null) {
            return "unknown keystore location";
        }
        return s;
    }

    /**
     * Returns an array of KeyStore that contain certificates that are trusted.
     * The KeyStores contain certificates from different sources.
     *
     * @return an array of KeyStore containing trusted Certificates
     */
    public static final KeyStore[] getCertKeyStores() {
        List<KeyStore> result = new ArrayList<KeyStore>(10);
        KeyStore ks = null;

        /* System-level JSSE certificates */
        ks = getKeyStore(Level.SYSTEM, Type.JSSE_CERTS);
        if (ks != null) {
            result.add(ks);
        }
        /* System-level certificates */
        ks = getKeyStore(Level.SYSTEM, Type.CERTS);
        if (ks != null) {
            result.add(ks);
        }
        /* User-level JSSE certificates */
        ks = getKeyStore(Level.USER, Type.JSSE_CERTS);
        if (ks != null) {
            result.add(ks);
        }
        /* User-level certificates */
        ks = getKeyStore(Level.USER, Type.CERTS);
        if (ks != null) {
            result.add(ks);
        }

        return result.toArray(new KeyStore[result.size()]);
    }

    /**
     * Returns an array of KeyStore that contain trusted CA certificates.
     *
     * @return an array of KeyStore containing trusted CA certificates
     */
    public static final KeyStore[] getCAKeyStores() {
        List<KeyStore> result = new ArrayList<KeyStore>(10);
        KeyStore ks = null;

        /* System-level JSSE CA certificates */
        ks = getKeyStore(Level.SYSTEM, Type.JSSE_CA_CERTS);
        if (ks != null) {
            result.add(ks);
        }
        /* System-level CA certificates */
        ks = getKeyStore(Level.SYSTEM, Type.CA_CERTS);
        if (ks != null) {
            result.add(ks);
        }
        /* User-level JSSE CA certificates */
        ks = getKeyStore(Level.USER, Type.JSSE_CA_CERTS);
        if (ks != null) {
            result.add(ks);
        }
        /* User-level CA certificates */
        ks = getKeyStore(Level.USER, Type.CA_CERTS);
        if (ks != null) {
            result.add(ks);
        }

        return result.toArray(new KeyStore[result.size()]);
    }

    /**
     * Returns KeyStores containing trusted client certificates
     *
     * @return an array of KeyStore objects that can be used to check client
     * authentication certificates
     */
    public static KeyStore[] getClientKeyStores() {
        List<KeyStore> result = new ArrayList<KeyStore>();
        KeyStore ks = null;

        ks = getKeyStore(Level.SYSTEM, Type.CLIENT_CERTS);
        if (ks != null) {
            result.add(ks);
        }

        ks = getKeyStore(Level.USER, Type.CLIENT_CERTS);
        if (ks != null) {
            result.add(ks);
        }

        return result.toArray(new KeyStore[result.size()]);
    }

    /**
     * Returns the location of a KeyStore corresponding to the given level and type.
     *
     * @param level the specified level of the key store to be returned.
     * @param type the specified type of the key store to be returned.
     * @return the location of the key store.
     */
    public static final String getKeyStoreLocation(Level level, Type type) {
        String configKey = null;
        switch (level) {
            case SYSTEM:
                switch (type) {
                    case JSSE_CA_CERTS:
                        configKey = DeploymentConfiguration.KEY_SYSTEM_TRUSTED_JSSE_CA_CERTS;
                        break;
                    case CA_CERTS:
                        configKey = DeploymentConfiguration.KEY_SYSTEM_TRUSTED_CA_CERTS;
                        break;
                    case JSSE_CERTS:
                        configKey = DeploymentConfiguration.KEY_SYSTEM_TRUSTED_JSSE_CERTS;
                        break;
                    case CERTS:
                        configKey = DeploymentConfiguration.KEY_SYSTEM_TRUSTED_CERTS;
                        break;
                    case CLIENT_CERTS:
                        configKey = DeploymentConfiguration.KEY_SYSTEM_TRUSTED_CLIENT_CERTS;
                        break;
                }
                break;
            case USER:
                switch (type) {
                    case JSSE_CA_CERTS:
                        configKey = DeploymentConfiguration.KEY_USER_TRUSTED_JSSE_CA_CERTS;
                        break;
                    case CA_CERTS:
                        configKey = DeploymentConfiguration.KEY_USER_TRUSTED_CA_CERTS;
                        break;
                    case JSSE_CERTS:
                        configKey = DeploymentConfiguration.KEY_USER_TRUSTED_JSSE_CERTS;
                        break;
                    case CERTS:
                        configKey = DeploymentConfiguration.KEY_USER_TRUSTED_CERTS;
                        break;
                    case CLIENT_CERTS:
                        configKey = DeploymentConfiguration.KEY_USER_TRUSTED_CLIENT_CERTS;
                        break;
                }
                break;
        }

        if (configKey == null) {
            throw new RuntimeException("Unspported");
        }

        return config.getProperty(configKey);
    }

    /**
     * Returns a String that can be used as a translation key to create a
     * user-visible representation of this KeyStore. Creates a string by
     * concatenating a level and type, converting everything to Title Case and
     * removing the _'s. (USER,CA_CERTS) becomes UserCaCerts.
     *
     * @param level the level of the key store.
     * @param type the type of the key store.
     * @return the translation key.
     */
    public static final String toTranslatableString(Level level, Type type) {
        StringBuilder response = new StringBuilder();

        response.append("KS");

        if (level != null) {
            String levelString = level.toString();
            response.append(levelString.substring(0, 1).toUpperCase());
            response.append(levelString.substring(1).toLowerCase());
        }

        if (type != null) {
            String typeString = type.toString();
            StringTokenizer tokenizer = new StringTokenizer(typeString, "_");
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                response.append(token.substring(0, 1).toUpperCase());
                response.append(token.substring(1).toLowerCase());
            }
        }

        return response.toString();
    }

    /**
     * Returns a human readable name for this KeyStore
     *
     * @param level the level of the KeyStore
     * @param type the type of KeyStore
     * @return a localized name for this KeyStore
     */
    public static String toDisplayableString(Level level, Type type) {
        return Translator.R(toTranslatableString(level, type));
    }

    /**
     * Reads the file (using the password) and uses it to create a new
     * {@link KeyStore}. If the file does not exist and should not be created,
     * it returns an empty but initialized KeyStore
     *
     * @param file the file to load information from
     * @param password the password to unlock the KeyStore file.
     * @return a KeyStore containing data from the file
     */
    private static final KeyStore createKeyStoreFromFile(File file, boolean createIfNotFound,
            String password) throws IOException, KeyStoreException, NoSuchAlgorithmException,
            CertificateException {
        FileInputStream fis = null;
        KeyStore ks = null;

        try {
            if (createIfNotFound && !file.exists()) {
                File parent = file.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("unable to create " + parent);
                }
                FileUtils.createRestrictedFile(file, true);

                ks = KeyStore.getInstance(KEYSTORE_TYPE);
                ks.load(null, password.toCharArray());
                FileOutputStream fos = new FileOutputStream(file);
                ks.store(fos, password.toCharArray());
                fos.close();
            }

            // TODO catch exception when password is incorrect and prompt user

            if (file.exists()) {
                fis = new FileInputStream(file);
                ks = KeyStore.getInstance(KEYSTORE_TYPE);
                ks.load(fis, password.toCharArray());
            } else {
                ks = KeyStore.getInstance(KEYSTORE_TYPE);
                ks.load(null, password.toCharArray());
            }
        } finally {
            if (fis != null) {
                fis.close();
            }
        }

        return ks;
    }

}
