/* NonEditableTableModel.java
   Copyright (C) 2013 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify it under the
terms of the GNU General Public License as published by the Free Software
Foundation, version 2.

IcedTea is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE.  See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
IcedTea; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is making a
combined work based on this library.  Thus, the terms and conditions of the GNU
General Public License cover the whole combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent modules, and
to copy and distribute the resulting executable under terms of your choice,
provided that you also meet, for each linked independent module, the terms and
conditions of the license of that module.  An independent module is a module
which is not derived from or based on this library.  If you modify this
library, you may extend this exception to your version of the library, but you
are not obligated to do so.  If you do not wish to do so, delete this exception
statement from your version.
*/
package net.sourceforge.jnlp.util.ui;

import java.util.Vector;
import javax.swing.table.DefaultTableModel;

/**
 * A table model that in effect is a {@link DefaultTableModel} except for no
 * cell being editable.
 * @see DefaultTableModel
 * @since IcedTea-Web 1.5
 */
public class NonEditableTableModel extends DefaultTableModel {
    /**
     * Constructs a {@link javax.swing.table.TableModel} that serves only one
     * purpose: make cells of certificate tables not editable.
     * @see DefaultTableModel#DefaultTableModel()
     */
    public NonEditableTableModel() {
        super();
    }

    /**
     * Constructs a {@link javax.swing.table.TableModel} that serves only one
     * purpose: make cells of certificate tables not editable.
     * @param rowCount the number of rows the table holds
     * @param columnCount the number of columns the table holds
     * @see DefaultTableModel#DefaultTableModel(int,int)
     */
    public NonEditableTableModel(final int rowCount, final int columnCount) {
        super(rowCount, columnCount);
    }

    /**
     * Constructs a {@link javax.swing.table.TableModel} that serves only one
     * purpose: make cells of certificate tables not editable.
     * @param data the data of the table
     * @param columnNames the names of the columns
     * @see DefaultTableModel#DefaultTableModel(Object[][],Object[])
     */
    public NonEditableTableModel(final Object[][] data, final Object[] columnNames) {
        super(data, columnNames);
    }

    /**
     * Constructs a {@link javax.swing.table.TableModel} that serves only one
     * purpose: make cells of certificate tables not editable.
     * @param columnNames {@code array} containing the names of the new columns;
     * if this is {@code null} then the model has no columns
     * @param rowCount the number of rows the table holds
     * @see DefaultTableModel#DefaultTableModel(Object[],int)
     */
    public NonEditableTableModel(final Object[] columnNames, final int rowCount) {
        super(columnNames, rowCount);
    }

    /**
     * Constructs a {@link javax.swing.table.TableModel} that serves only one
     * purpose: make cells of certificate tables not editable.
     * @param columnNames {@code vector} containing the names of the new columns;
     * if this is {@code null} then the model has no columns
     * @param rowCount the number of rows the table holds
     * @see DefaultTableModel#DefaultTableModel(Vector,int)
     */
    public NonEditableTableModel(final Vector<?> columnNames, final int rowCount) {
        super(columnNames, rowCount);
    }

    /**
     * Constructs a {@link javax.swing.table.TableModel} that serves only one
     * purpose: make cells of certificate tables not editable.
     * @param data the data of the table, a {@code Vector} of {@code Vector}s
     * of {@code Object} values
     * @param columnNames {@code vector} containing the names of the new columns
     * @see DefaultTableModel#DefaultTableModel(Vector,Vector)
     */
    public NonEditableTableModel(final Vector<?> data, final Vector<?> columnNames) {
        super(data, columnNames);
    }

    /**
     * This method always returns {@code false} to make the table's cells not
     * editable.
     * @param row the row whose value to be queried
     * @param column the column whose value to be queried
     * @return always {@code false}
     */
    @Override
    public boolean isCellEditable(final int row, final int column) {
        return false;
    }
}
