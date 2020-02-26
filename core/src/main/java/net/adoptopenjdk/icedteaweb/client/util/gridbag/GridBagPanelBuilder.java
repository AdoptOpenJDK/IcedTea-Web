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

    public void addKeyValueRow(final String key, final String value) {
        rows.add(new KeyValueRow(key, value));
    }

    public void addKeyComponentRow(final String key, final JComponent component) {
        rows.add(new KeyComponentRow(key, component));
    }

    public void addComponentRow(final JComponent component) {
        rows.add(new ComponentRow(component));
    }

    public void addHorizontalSpacer() {
        rows.add(SeparatorRow.createBlankRow());
    }

    public void addHorizontalLine() {
        rows.add(SeparatorRow.createHorizontalLine());
    }

    public JPanel createGrid() {
        final JPanel result = new JPanel(new GridBagLayout());
        final int numRows = rows.size();
        for (int i = 0; i < numRows; i++) {
            rows.get(i).addTo(result, i);
        }
        return result;
    }
}
