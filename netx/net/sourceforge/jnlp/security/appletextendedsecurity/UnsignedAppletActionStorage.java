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

import java.util.List;

/**
 * This is abstract access to white/blacklist created from some permanent storage.
 * <p>
 * It is daclaring adding, updating and searching. Intentionally not removing as 
 * during plugin runtime no deletations should be done.
 * </p>
 * <p>
 * Implementations of this interface (unless dummy ones:) should ensure correct
 * communication with permanent storage and be prepared for multiple instances 
 * read/write the same storage at time.
 * </p>
 */
public interface UnsignedAppletActionStorage {

    /**
     * This methods iterates through records in
     * {@link net.sourceforge.jnlp.config.DeploymentConfiguration#getAppletTrustUserSettingsPath} or
     * {@link net.sourceforge.jnlp.config.DeploymentConfiguration#getAppletTrustGlobalSettingsPath}, and is matching
     * regexes saved here against params. So parameters here are NOT regexes,
     * but are matched against saved regexes.
     * <p>
     * {@code null} or empty values are dangerously ignored, user, be aware of it. eg:
     * match only {@code codeBase} will be {@code null} someCodeBase {@code null} {@code null} match only
     * {@code documentBase} will be someDocBase {@code null} {@code null} {@code null} match only applet not
     * regarding code or document base will be {@code null} {@code null} mainClass archives.
     * </p>
     * @param documentBase
     * @param codeBase
     * @param archives
     * @return a matching unsigned applet action entry
     */
    public UnsignedAppletActionEntry getMatchingItem(String documentBase, String codeBase, List<String> archives);

    /**
     * Shortcut {@code getMatchingItem(documentBase, null, null, null)}
     *
     * @param documentBase
     * @return a matching unsigned applet action entry
     */
    public UnsignedAppletActionEntry getMatchingItemByDocumentBase(String documentBase);

    /**
     * Shortcut {@code getMatchingItem(null, codeBase, null, null)}
     *
     * @param codeBase
     * @return a matching unsigned applet action entry
     */
    public UnsignedAppletActionEntry getMatchingItemByCodeBase(String codeBase);

    /**
     * Shortcut {@code getMatchingItem(documentBase, codeBase, null, null)}
     *
     * @param documentBase
     * @param codeBase
     * @return a matching unsigned applet action entry
     */
    public UnsignedAppletActionEntry getMatchingItemByBases(String documentBase, String codeBase);

    /**
     * Will add new record. Note that regexes are stored for bases matching.
     * <p>
     * eg {@link UnsignedAppletActionEntry} which will deny some applet no matter of
     * page will be {@code new }{@link UnsignedAppletActionEntry#UnsignedAppletActionEntry UnsignedAppletActionEntry}{@code (}{@link ExecuteUnsignedApplet#NEVER}{@code , new }{@link java.util.Date#Date() Date()}{@code , null, null, someMain, someArchives)}
     * </p>
     * <p>
     * eg {@link UnsignedAppletActionEntry} which will
     * allow all applets on page with same codebase will be {@code new }{@link UnsignedAppletActionEntry#UnsignedAppletActionEntry UnsignedAppletActionEntry}{@code (}{@link ExecuteUnsignedApplet#NEVER}{@code , new }{@link java.util.Date#Date() Date()}{@code , ".*", ".*", null, null);}
     * </p>
     * @param item
     */
    public void add(final UnsignedAppletActionEntry item);

    /**
     * Will replace (current impl is matching by object's hashcode. This is not
     * reloading the list (but still saving after), so
     * {@link net.sourceforge.jnlp.util.lockingfile.StorageIoException} can be
     * thrown if it was not loaded before.
     * <p>
     * Imho this should be used only to actualise timestamps or change
     * {@link UnsignedAppletActionEntry}
     * </p>
     * @param item
     */
    public void update(final UnsignedAppletActionEntry item);

    /**
     * Lock the storage, if necessary. If no ownership issues arise, can be a no-op.
     */
    public void lock();

    /**
     * Unlock the storage, if necessary. If no ownership issues arise, can be a no-op.
     */
    public void unlock();
}
