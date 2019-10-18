package net.adoptopenjdk.icedteaweb.client.controlpanel;

import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.sourceforge.jnlp.cache.cache.CacheFile;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CacheTableModel extends AbstractTableModel {

    private final List<CacheFile> data = new ArrayList<>();

    private final String[] columns = {
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
        if(columnIndex == 0) {
            return cacheFile.getInfoFile();
        }
        if(columnIndex == 1) {
            return cacheFile.getParentFile();
        }
        if(columnIndex == 2) {
            return cacheFile.getProtocol();
        }
        if(columnIndex == 3) {
            return cacheFile.getDomain();
        }
        if(columnIndex == 4) {
            return cacheFile.getSize();
        }
        if(columnIndex == 5) {
            return cacheFile.getLastModified();
        }
        if(columnIndex == 6) {
            return cacheFile.getJnlpPath();
        }
        return null;
    }

    @Override
    public int getColumnCount() {
        return 7;
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

    public void add(final CacheFile cacheFile) {
        data.add(cacheFile);
        fireTableRowsInserted(data.size() - 1, data.size() - 1);
    }

    public void addAll(final Collection<CacheFile> cacheFiles) {
        final int size = data.size();
        data.addAll(cacheFiles);
        fireTableRowsInserted(size, data.size() - 1);
    }

    public void clear() {
        final int size = data.size();
        data.clear();
        fireTableRowsDeleted(0, size - 1);
    }

    public void removeRow(final int index) {
        data.remove(index);
        fireTableRowsDeleted(index, index);
    }
}
