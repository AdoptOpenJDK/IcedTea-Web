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
package net.sourceforge.jnlp.security.appletextendedsecurity.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.sourceforge.jnlp.security.appletextendedsecurity.ExecuteAppletAction;
import net.sourceforge.jnlp.security.appletextendedsecurity.UnsignedAppletActionEntry;
import net.sourceforge.jnlp.security.appletextendedsecurity.UnsignedAppletActionStorage;
import net.sourceforge.jnlp.util.lockingfile.LockingReaderWriter;
import net.sourceforge.jnlp.util.lockingfile.StorageIoException;

public class UnsignedAppletActionStorageImpl extends LockingReaderWriter implements UnsignedAppletActionStorage {

    protected List<UnsignedAppletActionEntry> items;

    public UnsignedAppletActionStorageImpl(String location) {
        this(new File(location));
    }

    public UnsignedAppletActionStorageImpl(File location) {
        super(location);
    }

    @Override
    public void writeContents() throws IOException {
        super.writeContents();
    }

    @Override
    public synchronized void writeContentsLocked() throws IOException {
        super.writeContentsLocked();
    }

    @Override
    protected void readContents() throws IOException {
        if (items == null) {
            items = new ArrayList<UnsignedAppletActionEntry>();
        } else {
            items.clear();
        }
        super.readContents();
    }

    @Override
    protected void readLine(String line) {
        if (line.trim().length() != 0) {
            this.items.add(UnsignedAppletActionEntry.createFromString(line));
        }
    }

    @Override
    public void writeContent(BufferedWriter bw) throws IOException {
        for (UnsignedAppletActionEntry item : items) {
            item.write(bw);
            bw.newLine();
        }
    }

    @Override
    public void add(final UnsignedAppletActionEntry item) {
        doLocked(new Runnable() {
            @Override
            public void run() {
                try {
                    readContents();
                    items.add(item);
                    writeContents();
                } catch (IOException ex) {
                    throw new StorageIoException(ex);
                }
            }
        });
    }

    @Override
    public void update(final UnsignedAppletActionEntry item) {
        doLocked(new Runnable() {
            @Override
            public void run() {
                try {
                    if (items == null) {
                        throw new StorageIoException("Storage is not initialised, can not update");
                    }
                    if (!items.contains(item)) {
                        throw new StorageIoException("Storage does not contain item you are updating. can not update");
                    }
                    writeContents();
                } catch (IOException ex) {
                    throw new StorageIoException(ex);
                }
            }
        });
    }

    @Override
    public UnsignedAppletActionEntry getMatchingItem(String documentBase, String codeBase, List<String> archives) {
        List<UnsignedAppletActionEntry> results = getMatchingItems(documentBase, codeBase, archives);
        if (results == null || results.isEmpty()) {
            return null;
        }
        // Chose the first result, unless we find a 'stronger' result
        // Actions such as 'always accept' or 'always reject' are 'stronger' than
        // the hints 'was accepted' or 'was rejected'.
        for (UnsignedAppletActionEntry candidate : results) {
                if (candidate.getUnsignedAppletAction() == ExecuteAppletAction.ALWAYS
                    || candidate.getUnsignedAppletAction() == ExecuteAppletAction.NEVER) {
                    //return first found strong
                    return  candidate;
                }
            }
        //no strong found, return first
        return results.get(0);
    }

    public List<UnsignedAppletActionEntry> getMatchingItems(String documentBase, String codeBase, List<String> archives) {
        List<UnsignedAppletActionEntry> result = new ArrayList<UnsignedAppletActionEntry>();
        lock();
        try {
            readContents();
            if (items == null) {
                return result;
            }
            for (UnsignedAppletActionEntry unsignedAppletActionEntry : items) {
                if (isMatching(unsignedAppletActionEntry, documentBase, codeBase, archives)) {
                    result.add(unsignedAppletActionEntry);
                }
            }
        } catch (IOException e) {
            throw new StorageIoException(e);
        } finally {
            unlock();
        }
        return result;
    }

    private boolean isMatching(UnsignedAppletActionEntry unsignedAppletActionEntry, String documentBase, String codeBase, List<String> archives) {
        boolean result = true;
        if (documentBase != null && !documentBase.trim().isEmpty()) {
            result = result && documentBase.matches(unsignedAppletActionEntry.getDocumentBase().getRegEx());
        }
        if (codeBase != null && !codeBase.trim().isEmpty()) {
            result = result && codeBase.matches(unsignedAppletActionEntry.getCodeBase().getRegEx());
        }
        if (archives != null) {
            List<String> saved = unsignedAppletActionEntry.getArchives();
            if (saved == null || saved.isEmpty()) {
                return result;
            }
            result = result && compareArchives(archives, saved);
        }
        return result;
    }

    @Override
    public String toString() {
        return getBackingFile() + " " + super.toString();
    }

    private boolean compareArchives(List<String> archives, List<String> saved) {
        if (archives == null && saved !=null){
            return false;
        }
        if (archives != null && saved ==null){
            return false;
        }
        if (archives == null && saved ==null){
            return true;
        }
        if (archives.size() != saved.size()) {
            return false;
        }
        Collections.sort(archives);
        Collections.sort(saved);
        for (int i = 0; i < saved.size(); i++) {
            String string1 = saved.get(i);
            String string2 = archives.get(i);
            //intentional reference compare
            if (string1 == string2) {
                continue;
            }
            if (string1 == null || string2 == null) {
                return false;
            }
            if (string1.trim().equals(string2.trim())) {
                continue;
            }
            return false;
        }
        return true;
    }

    @Override
    public UnsignedAppletActionEntry getMatchingItemByDocumentBase(String documentBase) {
        return getMatchingItem(documentBase, null, null);
    }

    @Override
    public UnsignedAppletActionEntry getMatchingItemByCodeBase(String codeBase) {
        return getMatchingItem(null, codeBase, null);
    }

    @Override
    public UnsignedAppletActionEntry getMatchingItemByBases(String documentBase, String codeBase) {
        return getMatchingItem(documentBase, codeBase, null);
    }
}
