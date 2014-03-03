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
package net.sourceforge.jnlp.controlpanel;

import java.util.Date;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import net.sourceforge.jnlp.runtime.Translator;
import net.sourceforge.jnlp.security.appletextendedsecurity.ExecuteAppletAction;
import net.sourceforge.jnlp.security.appletextendedsecurity.UnsignedAppletActionEntry;
import net.sourceforge.jnlp.security.appletextendedsecurity.UrlRegEx;
import net.sourceforge.jnlp.security.appletextendedsecurity.impl.UnsignedAppletActionStorageExtendedImpl;

public class UnsignedAppletActionTableModel extends AbstractTableModel {

    final UnsignedAppletActionStorageExtendedImpl back;
    private final String[] columns = new String[]{Translator.R("APPEXTSECguiTableModelTableColumnAction"),
        Translator.R("APPEXTSECguiTableModelTableColumnDateOfAction"),
        Translator.R("APPEXTSECguiTableModelTableColumnDocumentBase"),
        Translator.R("APPEXTSECguiTableModelTableColumnCodeBase"),
        Translator.R("APPEXTSECguiTableModelTableColumnArchives")};

    public UnsignedAppletActionTableModel(UnsignedAppletActionStorageExtendedImpl back) {
        this.back = back;
    }

    @Override
    public int getRowCount() {
            return back.toArray().length;
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columns[columnIndex];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return ExecuteAppletAction.class;
        }
        if (columnIndex == 1) {
            return Date.class;
        }
        if (columnIndex == 2) {
            return UrlRegEx.class;
        }
        if (columnIndex == 3) {
            return UrlRegEx.class;
        }
        if (columnIndex == 4) {
            return String.class;
        }
        if (columnIndex == 5) {
            return String.class;
        }
        return Object.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (back.isReadOnly()) {
            return false;
        }
        if (columnIndex == 1) {
            return false;
        }
        if (columnIndex == 0) {
            return true;
        }
        if (getValueAt(rowIndex, columnIndex - 1) == null || getValueAt(rowIndex, columnIndex - 1).toString().trim().isEmpty()) {
            return false;
        }
        return true;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        UnsignedAppletActionEntry source = back.toArray()[rowIndex];
        if (columnIndex == 0) {
            return source.getUnsignedAppletAction();
        }
        if (columnIndex == 1) {
            return source.getTimeStamp();
        }
        if (columnIndex == 2) {
            return source.getDocumentBase();
        }
        if (columnIndex == 3) {
            return source.getCodeBase();
        }
        if (columnIndex == 4) {
            return UnsignedAppletActionEntry.createArchivesString(source.getArchives());
        }
        return null;
    }

    @Override
    public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
        final UnsignedAppletActionEntry source = back.toArray()[rowIndex];
        back.modify(source, columnIndex, aValue);

    }

    public void addRow() {
        int i = getRowCount()-1;
        String s = "\\Qhttp://localhost:80/\\E.*";
        back.add(new UnsignedAppletActionEntry(
                ExecuteAppletAction.NEVER,
                new Date(),
                new UrlRegEx(s),
                new UrlRegEx(s),
                null));
        fireTableRowsInserted(i+1, i+1);
    }

    public void removeRow(int i) {
        int ii = getRowCount()-1;
        if (ii<0){
            return;
        }
        if (i<0){
            return;
        }
        back.remove(i);
        fireTableRowsDeleted(i, i);
    }

    public void clear() {
        int i = getRowCount()-1;
        if (i<0){
            return;
        }
        back.clear();
        fireTableRowsDeleted(0, i);
    }

    void removeByBehaviour(ExecuteAppletAction unsignedAppletAction) {
        int i = getRowCount()-1;
        if (i<0){
            return;
        }
        back.removeByBehaviour(unsignedAppletAction);
        fireTableRowsDeleted(0, i);
    }

    int moveUp(int selectedRow) {
        int i = getRowCount()-1;
        if (i<0){
            return selectedRow;
        }
        int x = back.moveUp(selectedRow);
        fireTableChanged(new TableModelEvent(this, 0, i));
        return x;
    }

    int  moveDown(int selectedRow) {
        int i = getRowCount()-1;
        if (i<0){
            return selectedRow; 
        }
        int x = back.moveDown(selectedRow);
        fireTableChanged(new TableModelEvent(this, 0, i));
        return x;
    }
}
