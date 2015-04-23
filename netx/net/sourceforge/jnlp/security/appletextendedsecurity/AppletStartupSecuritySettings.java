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
package net.sourceforge.jnlp.security.appletextendedsecurity;

import net.sourceforge.jnlp.security.appletextendedsecurity.impl.UnsignedAppletActionStorageImpl;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.config.PathsAndFiles;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.util.lockingfile.StorageIoException;

public class AppletStartupSecuritySettings {

    private static final AppletStartupSecuritySettings instance = new AppletStartupSecuritySettings();
    private UnsignedAppletActionStorageImpl globalInstance;
    private UnsignedAppletActionStorageImpl customInstance;

    public static AppletStartupSecuritySettings getInstance() {
        return instance;
    }

    public static AppletSecurityLevel getHardcodedDefaultSecurityLevel() {
        return AppletSecurityLevel.getDefault();
    }

    /**
     *
     * @return storage with global items from /etc/
     */
    public UnsignedAppletActionStorage getUnsignedAppletActionGlobalStorage() {
        if (globalInstance == null) {
            globalInstance = new UnsignedAppletActionStorageImpl(PathsAndFiles.APPLET_TRUST_SETTINGS_SYS.getFile());
        }
        return globalInstance;
    }

    /**
     *
     * @return storage with custom items from /home/
     */
    public UnsignedAppletActionStorage getUnsignedAppletActionCustomStorage() {
        if (customInstance == null) {
            customInstance = new UnsignedAppletActionStorageImpl(PathsAndFiles.APPLET_TRUST_SETTINGS_USER.getFile());
        }
        return customInstance;
    }

    /**
     *
     * @return user-set security level or default one if user-set do not exists
     */
    public AppletSecurityLevel getSecurityLevel() {
        DeploymentConfiguration conf = JNLPRuntime.getConfiguration();
        if (conf == null) {
            throw new StorageIoException("JNLPRuntime configuration is null. Try to reinstall IcedTea-Web");
        }
        String s = conf.getProperty(DeploymentConfiguration.KEY_SECURITY_LEVEL);
        if (s == null) {
            return getHardcodedDefaultSecurityLevel();
        }
        return AppletSecurityLevel.fromString(s);
    }
}
