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

import java.io.File;
import java.io.IOException;
import java.util.Date;
import net.sourceforge.jnlp.security.appletextendedsecurity.ExecuteAppletAction;
import net.sourceforge.jnlp.security.appletextendedsecurity.UnsignedAppletActionEntry;
import net.sourceforge.jnlp.security.appletextendedsecurity.UrlRegEx;
import net.sourceforge.jnlp.util.lockingfile.StorageIoException;

public class UnsignedAppletActionStorageExtendedImpl extends UnsignedAppletActionStorageImpl {

    public UnsignedAppletActionStorageExtendedImpl(String location) {
        this(new File(location));
    }

    public UnsignedAppletActionStorageExtendedImpl(File location) {
        super(location);
    }

    public UnsignedAppletActionEntry[] toArray() {
        lock();
        try {
            readContents();
            return items.toArray(new UnsignedAppletActionEntry[items.size()]);
        } catch (IOException e) {
            throw new StorageIoException(e);
        } finally {
            unlock();
        }
    }

    public void clear() {
        doLocked(new Runnable() {
            public void run() {
                try {
                    items.clear();
                    writeContents();
                } catch (IOException e) {
                    throw new StorageIoException(e);
                }
            }
        });
    }

    public void removeByBehaviour(final ExecuteAppletAction unsignedAppletAction) {
        doLocked(new Runnable() {
            public void run() {
                try {
                    readContents();
                    for (int i = 0; i < items.size(); i++) {
                        UnsignedAppletActionEntry unsignedAppletActionEntry = items.get(i);
                        if (unsignedAppletActionEntry.getUnsignedAppletAction() == unsignedAppletAction) {
                            items.remove(i);
                            i--;
                        }

                    }
                    writeContents();
                } catch (IOException e) {
                    throw new StorageIoException(e);
                }
            }
        });
    }

    private void swap(final int i, final int ii) {
        doLocked(new Runnable() {
            public void run() {
                try {
                    readContents();
                    UnsignedAppletActionEntry backup = items.get(i);
                    items.set(i, items.get(ii));
                    items.set(ii, backup);
                    writeContents();
                } catch (IOException e) {
                    throw new StorageIoException(e);
                }
            }
        });

    }

    public int moveUp(int selectedRow) {
        if (selectedRow <= 0) {
            return selectedRow;
        }
        swap(selectedRow, selectedRow - 1);
        return selectedRow-1;
    }

    public int moveDown(int selectedRow) {
        if (selectedRow >= items.size() - 1) {
            return selectedRow;
        }
        swap(selectedRow, selectedRow + 1);
        return selectedRow+1;
    }

    public void remove(final int item) {
        doLocked(new Runnable() {
            public void run() {
                try {
                    readContents();
                    items.remove(item);
                    writeContents();
                } catch (IOException ex) {
                    throw new StorageIoException(ex);
                }
            }
        });
    }

    public void modify(final UnsignedAppletActionEntry source, final int columnIndex, final Object aValue) {
        Runnable r = new Runnable() {
            public void run() {

                try {
                    if (!items.contains(source)) {
                        throw new StorageIoException("Item to be modified not found in storage");
                    }

                    if (columnIndex == 0) {
                        source.setUnsignedAppletAction((ExecuteAppletAction) aValue);
                    }
                    if (columnIndex == 1) {
                        source.setTimeStamp((Date) aValue);
                    }
                    if (columnIndex == 2) {
                        source.setDocumentBase(new UrlRegEx((String) aValue));
                    }
                    if (columnIndex == 3) {
                        source.setCodeBase(new UrlRegEx((String) aValue));
                    }
                    if (columnIndex == 4) {
                        source.setArchives(UnsignedAppletActionEntry.createArchivesList((String) aValue));
                    }

                    writeContents();
                } catch (IOException ex) {
                    throw new StorageIoException(ex);
                }
            }
        };
        doLocked(r);

    }

    @Override
    public synchronized void writeContentsLocked() throws IOException {
        super.writeContentsLocked();
    }
}
