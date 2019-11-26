package net.adoptopenjdk.icedteaweb.client.controlpanel;

import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.resources.cache.CacheFile;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CacheTableModel extends AbstractTableModel {

    private final List<CacheFile> data = new ArrayList<>();

    public final static String[] columns = {
            Translator.R("CVCPColName"),
            Translator.R("CVCPColPath"),
            Translator.R("CVCPColType"),
            Translator.R("CVCPColDomain"),
            Translator.R("CVCPColSize"),
            Translator.R("CVCPColLastModified"),
            Translator.R("CVCPColJnlPath")
    };

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        final CacheFile cacheFile = data.get(rowIndex);
        switch (columnIndex) {
            case 0: return cacheFile.getInfoFile();
            case 1: return cacheFile.getParentFile();
            case 2: return cacheFile.getProtocol();
            case 3: return cacheFile.getDomain();
            case 4: return cacheFile.getSize();
            case 5: return cacheFile.getLastModified();
            case 6: return cacheFile.getJnlpPath();
        }
        return null;
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(final int columnIndex) {
        return columns[columnIndex];
    }

    @Override
    public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
        throw new RuntimeException("Mutation of values is not supported!");
    }

    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return false;
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    public void addAll(final Collection<CacheFile> cacheFiles) {
        final int size = data.size();
        data.addAll(cacheFiles);
        fireTableRowsInserted(size, data.size() - 1);
    }

    public void clear() {
        final int size = data.size();
        if (size > 0) {
            data.clear();
            fireTableRowsDeleted(0, size - 1);
        }
    }

    public void removeRow(final int index) {
        data.remove(index);
        fireTableRowsDeleted(index, index);
    }
}
