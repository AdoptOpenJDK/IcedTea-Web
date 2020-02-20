package net.adoptopenjdk.icedteaweb.client.util.gridbag;

import net.adoptopenjdk.icedteaweb.Assert;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Builder for creating GridBag layouted JPanels.
 */
public class GridBagPanelBuilder {

    private final List<GridBagRow> rows = new ArrayList<>();

    public void addRow(final GridBagRow row) {
        rows.add(Assert.requireNonNull(row, "row"));
    }

    public void addRows(final Collection<GridBagRow> rows) {
        Assert.requireNonNull(rows, "rows").forEach(this::addRow);
    }

    public void addRow(final String key, final String value) {
        rows.add(new KeyValueRow(key, value));
    }

    public void addRow(final JComponent component) {
        rows.add(new ComponentRow(component));
    }

    public void addSeparatorRow(boolean hasLine) {
        if (hasLine) {
            rows.add(SeparatorRow.createHorizontalLine());
        } else {
            rows.add(SeparatorRow.createBlankRow());
        }
    }

    public JPanel createGrid() {
        final JPanel result = new JPanel();
        result.setLayout(new GridBagLayout());
        final int numRows = rows.size();
        for (int i = 0; i < numRows; i++) {
            rows.get(i).addTo(result, i);
        }
        return result;
    }
}
